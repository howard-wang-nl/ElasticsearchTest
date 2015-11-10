import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.serializer.KryoSerializer
import org.elasticsearch.spark._

import scala.io.Source

object ElasticsearchSparkTest {
  def main(array: Array[String]): Unit = {
    val inputPath = "/Users/hwang/IdeaProjects/githubstat/data/01/"
    val outFile = "/tmp/esRDD"
    val indexName = "github"
    val indexType = "eventlog"
    val esRes = indexName + "/" + indexType

    val conf = new SparkConf().setAppName("ElasticsearchSparkTest").setMaster("local")
    conf.set("spark.serializer", classOf[KryoSerializer].getName)
    conf.set("es.index.auto.create", "true")
//    conf.set("es.nodes", "localhost")
//    conf.set("es.port", "9200")
//    conf.set("es.input.json", "yes")

    val sc = new SparkContext(conf)

    val files = new java.io.File(inputPath).listFiles
    val filesSel = files.filter(_.getName.endsWith(".json"))

    for (inputFileName <- filesSel) {
      println(s"### Importing $inputFileName into Elasticsearch...")
      val sInput = Source.fromFile(inputFileName)
      val sLines = sInput.getLines().toSeq
      sc.makeRDD(sLines).saveJsonToEs(esRes)
      sInput.close()
    }

//    val q = "?q=type:PushEvent"

    // Filter date range
    val q =
      """
        |{
        |  "filter": {
        |    "range" : {
        |      "created_at" : {
        |        "gte": "2015-01-01T00:00:00Z",
        |        "lte": "2015-01-01T00:30:00Z"
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
     *
    val q =
      """
        |{
        |  "aggs" : {
        |    "event_types" : {
        |      "terms" : { "field" : "type" }
        |    }
        |  }
        |}
      """.stripMargin
     */

    /* Try facet.
     * no exception, esRDD doesn't include facets results but only the original data.
    val q =
      """
        |{
        |  "facets" : {
        |    "event_types" : { "terms" : {"field" : "type"} }
        |  }
        |}
      """.stripMargin
     */

    val rdd = sc.esRDD(esRes, q)

    /* Hint: Spark + ElasticSearch returns RDD[(String, Map[String, Any])]. How can I manipulate Any?
     * http://stackoverflow.com/questions/29829042/spark-elasticsearch-returns-rddstring-mapstring-any-how-can-i-manipul
     */

    // Verify date range.
//    rdd.collect().map(_._2.get("created_at").get.asInstanceOf[java.util.Date]).sorted.foreach(println)

    println("### Results: %d Records.".format(rdd.count))

    // Remove old output folder.
    import sys.process._
    val c = "rm -fr " + outFile
    c !  // !!! Be careful

    // Save data for inspection and verification.
    rdd.saveAsTextFile(outFile)

    sc.stop()
  }
}
