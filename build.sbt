import AssemblyPlugin.assemblySettings

name := "ElasticsearchTest"

version := "1.0"

//scalaVersion := "2.11.7"
scalaVersion := "2.10.6"

lazy val meta = """META.INF(.)*""".r

libraryDependencies ++= Seq (
  "org.elasticsearch" % "elasticsearch" % "1.7.3",
  "org.apache.spark" %% "spark-core" % "1.5.1"  % "provided",
  "org.elasticsearch" %% "elasticsearch-spark" % "2.1.2"
//  "org.elasticsearch" %% "elasticsearch-spark" % "2.1.0"
)

assemblyMergeStrategy in assembly := {
  case meta(_) | "pom.properties"
  => MergeStrategy.discard
  case x
  => MergeStrategy.first
}