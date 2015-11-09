name := "ElasticsearchTest"

version := "1.0"

scalaVersion := "2.11.7"
//scalaVersion := "2.10.4"

libraryDependencies ++= Seq (
  "org.elasticsearch" % "elasticsearch" % "1.7.3",
  "org.apache.spark" %% "spark-core" % "1.5.1",
  "org.elasticsearch" %% "elasticsearch-spark" % "2.1.2"
//  "org.elasticsearch" %% "elasticsearch-spark" % "2.1.0"
)