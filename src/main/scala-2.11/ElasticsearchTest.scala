import org.elasticsearch.action.count.CountResponse
import org.elasticsearch.index.query.QueryBuilders

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.client.Client
import org.elasticsearch.node.NodeBuilder.nodeBuilder
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilders._;
import org.elasticsearch.index.query.FilterBuilders._;
import org.scalastuff.esclient.ESClient
import org.elasticsearch.search.aggregations.AggregationBuilders;

import scala.io.Source

object ElasticsearchTest extends App {

  val inputPath = "/Users/hwang/IdeaProjects/githubstat/data/01/"

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

  Thread.sleep(2000); // wait for the server to finish indexing.

//  val q = termQuery("type", "PushEvent")
  val q = matchAllQuery()
  println("Query=" + q.toString)

  val a = AggregationBuilders.terms("eventTypes").field("type")

  val response: SearchResponse = client.prepareSearch("github")
    .setTypes("eventlog")
    .setQuery(q)
    .setSize(0) // Only count statistics with aggregation.
    .addAggregation(a)
    .execute()
    .actionGet()

  println("Response: " + response.toString)
  println("RequestStatus: " + response.status.name)

  node.close()

}
