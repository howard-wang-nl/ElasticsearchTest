import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.client.Client
import org.elasticsearch.node.NodeBuilder.nodeBuilder
import org.scalastuff.esclient.ESClient

object ElasticsearchTest extends App {
  val client : Client = nodeBuilder.node.client

  val json = "{" +
    "\"user\":\"kimchy\"," +
    "\"postDate\":\"2013-01-30\"," +
    "\"message\":\"trying out Elasticsearch\"" +
    "}";

  val ir = new IndexRequest("twitter", "tweet").source(json)

  val response : Future[IndexResponse] =
    client.execute(ir)
  println("Document id: " + Await.result(response, 5 seconds).getId)
}
