import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.client.Client
import org.elasticsearch.node.NodeBuilder.nodeBuilder
import org.scalastuff.esclient.ESClient

import scala.io.Source

object ElasticsearchTest extends App {

  val inputPath = "/Users/hwang/IdeaProjects/githubstat/data/"

  val files = new java.io.File(inputPath).listFiles
  val filesSel = files.filter(_.getName.endsWith(".json"))

  println(s"##3# here.")
  val client : Client = nodeBuilder.node.client
  println(s"##4# here.")

  for (inputFileName <- filesSel) {
    println(s"Importing $inputFileName into Elasticsearch...")
    val sInput = Source.fromFile(inputFileName)
    val iLines = sInput.getLines().toString
    sInput.close()

    println(s"##1# $iLines###")

//    val ir = new IndexRequest("github", "eventlog").source(iLines)

    println(s"##2# reached.")

    /*
    val response : Future[IndexResponse] =
      client.execute(ir)
    println("Document id: " + Await.result(response, 5 seconds).getId)
    */
  }

}
