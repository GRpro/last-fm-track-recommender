import sbt.Keys.{mainClass, test, _}
import sbtassembly.AssemblyPlugin.autoImport.assemblyMergeStrategy


val commonSettings = Seq(
  organization := "com.appsflyer.lastfm",
  version := "0.1",
  scalaVersion := "2.11.8",
  javacOptions ++= Seq("-encoding", "UTF-8")
)

val sparkVersion = "2.3.1"

val job = project.in(file("job"))
  .settings(commonSettings: _*)
  .settings(
    name := "job"
  )
  .settings(
    resolvers ++= Seq("Spark Packages Repo" at "https://dl.bintray.com/spark-packages/maven"),
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
      "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
      "com.databricks" %% "spark-csv" % "1.5.0",
      "org.scalatest" %% "scalatest" % "3.0.1" % "test"
    )
  )

val localRunner = project.in(file("local_runner"))
  .settings(commonSettings: _*)
  .settings(
    name := "local_runner"
  )
  .settings(
    resolvers ++= Seq("Spark Packages Repo" at "https://dl.bintray.com/spark-packages/maven"),
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-sql" % sparkVersion exclude("org.slf4j", "slf4j-log4j12"),
      "org.apache.spark" %% "spark-core" % sparkVersion exclude("org.slf4j", "slf4j-log4j12"),
      "ch.qos.logback" % "logback-classic" % "1.1.7"
    ),
    // assembly configuration
    test in assembly := {},
    mainClass in assembly := Some("Main"),
    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", "services", "org.apache.hadoop.fs.FileSystem") => MergeStrategy.filterDistinctLines
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case x => MergeStrategy.first
    }
  )
  .dependsOn(job)

val root = project.in(file("."))
  .settings(commonSettings: _*)
  .settings(
    name := "root"
  )
  .aggregate(job, localRunner)
