import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.serializer.KryoSerializer
import org.elasticsearch.spark._

import scala.io.Source

object ElasticsearchSparkTest extends App {
  override def main(array: Array[String]): Unit = {
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
    val rdd = sc.esRDD(esRes, q)

    println("### Results:")
    rdd.saveAsTextFile(outFile)
    sc.stop()
  }
}
