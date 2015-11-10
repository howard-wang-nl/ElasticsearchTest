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

    val q = "?q=type:PushEvent"

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
     * no exception, but result esRDD doesn't include facets results but only the original data.
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
//    rdd.collect().foreach(println)

    println("### Results: %d Records.".format(rdd.count))
    rdd.saveAsTextFile(outFile)
    sc.stop()
  }
}
