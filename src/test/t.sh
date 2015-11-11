curl -X POST "http://localhost:9200/github/_search?pretty=true" -d '
{
  "facets" : {
    "event_types" : { "terms" : {"field" : "type"} }
  }
}
'
