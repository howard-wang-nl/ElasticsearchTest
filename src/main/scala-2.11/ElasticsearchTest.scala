import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.client.Client
import org.elasticsearch.node.NodeBuilder.nodeBuilder
import org.scalastuff.esclient.ESClient

object ElasticsearchTest extends App {
  val node = nodeBuilder.node()
  val client = node.client()
    val json = "{" +
      "\"user\":\"kimchy4a\"," +
      "\"postDate\":\"2013-01-30\"," +
      "\"message\":\"trying out Elasticsearch\"" +
      "}";

  val json2 = "{" +
    "\"user\":\"kimchy3a\"," +
    "\"postDate\":\"2013-01-30\"," +
    "\"message\":\"trying out Elasticsearch\"" +
    "}";

    val ir = new IndexRequest("twitter", "tweet", "12asl4").source(json)

    val response : Future[IndexResponse] =
      client.execute(ir)
    println("Document id: " + Await.result(response, 5 seconds).getId)

  val ir2 = new IndexRequest("twitter", "tweet", "22sdfs4").source(json2)

  val response2 : Future[IndexResponse] =
    client.execute(ir2)
  println("Document id: " + Await.result(response2, 5 seconds).getId)

  node.close()
}
