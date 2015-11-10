import AssemblyPlugin.assemblySettings

name := "ElasticsearchTest"

version := "1.0"

// For the Scala API, Spark 1.5.1 uses Scala 2.10. You will need to use a compatible Scala version (2.10.x).
// http://spark.apache.org/docs/latest/
scalaVersion := "2.10.6"

lazy val meta = """META.INF(.)*""".r

/* In some cases users will want to create an "uber jar" containing their application along with its dependencies.
 * The user's jar should never include Hadoop or Spark libraries, however, these will be added at runtime.
 * http://spark.apache.org/docs/latest/cluster-overview.html
 * If your code depends on other projects, you will need to package them alongside your application in order to distribute the code to a Spark cluster.
 * To do this, create an assembly jar (or “uber” jar) containing your code and its dependencies.
 * http://spark.apache.org/docs/latest/submitting-applications.html
 */

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