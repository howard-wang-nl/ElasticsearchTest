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

  val node = nodeBuilder.node()
  val client = node.client()

  for (inputFileName <- filesSel) {
    println(s"Importing $inputFileName into Elasticsearch...")
    val sInput = Source.fromFile(inputFileName)
    val iLines = sInput.getLines()

    for (l <- iLines) {
      val ir = new IndexRequest("github", "eventlog").source(l)

      val response : Future[IndexResponse] = client.execute(ir)
      Await.result(response, 5 seconds).getId
    }

    sInput.close()
  }

  node.close()

}
