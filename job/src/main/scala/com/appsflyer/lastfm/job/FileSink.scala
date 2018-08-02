package com.appsflyer.lastfm.job

import org.apache.spark.sql.{DataFrame, SparkSession}

class FileSink(path: String) extends Sink {

  override def sink(trackCounts: DataFrame)(implicit sparkSession: SparkSession): Unit =
    trackCounts.coalesce(1).write
      .format("com.databricks.spark.csv")
      .option("header", "true")
      .save(path)
}
