import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest
import org.elasticsearch.action.index.{IndexRequest, IndexResponse}
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.index.query.QueryBuilders._
import org.elasticsearch.node.NodeBuilder.nodeBuilder
import org.elasticsearch.search.aggregations.AggregationBuilders

import scala.io.Source

object ElasticsearchTest {

  val inputPath = "/Users/hwang/IdeaProjects/githubstat/data/01/"
  val indexName = "github"
  val indexType = "eventlog"

  val files = new java.io.File(inputPath).listFiles
  val filesSel = files.filter(_.getName.endsWith(".json"))

  val node = nodeBuilder.node()
  val client = node.client()
  val iac = client.admin().indices()
  val gir = iac.prepareGetIndex().execute().actionGet()
  println("### GetIndexResponse Array size: " + gir.getIndices.size)
  gir.getIndices.foreach(x => println(x)) // FIXME: Why sometimes get empty results even if consecutive exists check returns correct result?

  if (iac.exists(new IndicesExistsRequest(indexName)).actionGet().isExists) {
    println("### Delete old github data...")
    iac.delete(new DeleteIndexRequest(indexName)).actionGet()
  } else {
    println("### Old github data not found?") // FIXME: Why sometimes exists check return false when there is in fact index data there?
  }

  for (inputFileName <- filesSel) {
    println(s"### Importing $inputFileName into Elasticsearch...")
    val sInput = Source.fromFile(inputFileName)
    val iLines = sInput.getLines()

    for (l <- iLines) {
      val ir = new IndexRequest(indexName, indexType).source(l)
      val response : IndexResponse = client.index(ir).actionGet()
    }

    sInput.close()
  }

  iac.prepareRefresh("github").execute().actionGet()

//  val q = termQuery("type", "PushEvent")
  val q = matchAllQuery()
  println("### Query=" + q.toString)

  val a = AggregationBuilders.terms("eventTypes").field("type")

  val response: SearchResponse = client.prepareSearch("github")
    .setTypes("eventlog")
    .setQuery(q)
    .setSize(0) // Only count statistics with aggregation.
    .addAggregation(a)
    .execute()
    .actionGet()

  println("### Response: " + response.toString)
  println("### RequestStatus: " + response.status.name)

  client.close()
  node.close()

}
