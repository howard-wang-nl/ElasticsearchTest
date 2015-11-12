import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.serializer.KryoSerializer
import org.elasticsearch.spark._

import scala.io.Source
import scala.collection.mutable.Buffer

object ElasticsearchSparkTest {
  def main(args: Array[String]): Unit = {
    val inputPath = "/Users/hwang/IdeaProjects/githubstat/data/01/"
    val outFile = "/tmp/esRDD"
    val indexName = "github"
    val indexType = "eventlog"
    val esRes = indexName + "/" + indexType
    val timeBegin = "2015-01-01T00:00:00Z"
    val timeEnd = "2015-01-01T00:30:00Z"

    val conf = new SparkConf()
      .setAppName("ElasticsearchSparkTest")
      .setMaster("local[*]") // Run Spark locally with as many worker threads as logical cores on your machine.
    conf.set("spark.serializer", classOf[KryoSerializer].getName)
    conf.set("es.index.auto.create", "true")
//    conf.set("es.nodes", "localhost")
//    conf.set("es.port", "9200")
//    conf.set("es.input.json", "yes")

    val sc = new SparkContext(conf)

    if (args.length > 0 && args(0) == "i") { // import data
      val files = new java.io.File(inputPath).listFiles
      val filesSel = files.filter(_.getName.endsWith(".json"))

      for (inputFileName <- filesSel) {
        println(s"### Importing $inputFileName into Elasticsearch...")
        val sInput = Source.fromFile(inputFileName)
        val sLines = sInput.getLines().toSeq
        sc.makeRDD(sLines).saveJsonToEs(esRes)
        sInput.close()
      }
    }

    val q1 = "?q=type:PushEvent"

    // Filter date range
    val q2 =
      s"""
        |{
        |  "filter": {
        |    "range" : {
        |      "created_at" : {
        |        "gte": "$timeBegin",
        |        "lte": "$timeEnd"
        |      }
        |    }
        |  }
        |}
      """.stripMargin

    /* This query doesn't work with elasticsearch-spark because the connector is implemented through
     * scan/scroll which doesn't support aggregation yet.
     * See: How to get aggregations working in Elasticsearch Spark adapter?
     * https://groups.google.com/forum/#!topic/elasticsearch/9ZrJ4zyqgWU
     * https://github.com/elastic/elasticsearch-hadoop/issues/276
     */
    val q3 =
      """
        |{
        |  "aggs" : {
        |    "event_types" : {
        |      "terms" : { "field" : "type" }
        |    }
        |  }
        |}
      """.stripMargin

    /* Try facet.
     * no exception, esRDD doesn't include facets results but only the original data.
     */
    val q4 =
      """
        |{
        |  "facets" : {
        |    "event_types" : { "terms" : {"field" : "type"} }
        |  }
        |}
      """.stripMargin

    // Get only the selected fields to reduce data transferred betw Elasticsearch and Spark.
    val q5 =
      """
        |{ "fields" : ["type", "created_at"] }
      """.stripMargin

    val q = q5
    println(s"### Selected time range: $timeBegin ~ $timeEnd")
    val rdd = sc.esRDD(esRes, q)
    println("### Results: %d Records.".format(rdd.count))

    /* Hint: Spark + ElasticSearch returns RDD[(String, Map[String, Any])]. How can I manipulate Any?
     * http://stackoverflow.com/questions/29829042/spark-elasticsearch-returns-rddstring-mapstring-any-how-can-i-manipul
     */

    // Verify date range.
//    rdd.collect().map(_._2.get("created_at").get.asInstanceOf[java.util.Date]).sorted.foreach(println)

    /* Count distinct events of the "type" field.
     * NOTE:
     *   1. The default returned data which is a tuple (id:String, Map[String, AnyRef]).
     *   2. The value of the Map generalized as AnyRef, which can be converted to concrete types using asInstanceOf[].
     *   3. Simple types like String can be converted using asInstanceOf[String], but List of String should be converted using
     *      asInstanceOf[Buffer[String]].
     *   4. If query with fields selection as in q5, the returned field data such as Strings are always put in Buffer[String].
     *   5. For data type mapping betw Elasticsearch and Scala/Java, see "Type conversion" on https://www.elastic.co/guide/en/elasticsearch/hadoop/current/spark.html
     */
    if (q == q5) {
      // Process result of query with fields selection.
      rdd
        .collect()
        .map(
          _._2.get("type")
            .get.asInstanceOf[Buffer[String]]
        ).groupBy(s => s)
        .mapValues(_.length)
        .foreach(println)
    } else {
      // Process result of query without fields selection.
      rdd
        .collect()
        .map(
          _._2.get("type")
            .get.asInstanceOf[String]
        ).groupBy(s => s)
        .mapValues(_.length)
        .foreach(println)
    }

    // Remove old output folder.
    import sys.process._
    val c = "rm -fr " + outFile
    c !  // !!! Be careful

    // Save data for inspection and verification.
    rdd.saveAsTextFile(outFile)

    sc.stop()
  }
}
