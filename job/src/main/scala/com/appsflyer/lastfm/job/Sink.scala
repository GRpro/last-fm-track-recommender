package com.appsflyer.lastfm.job

import org.apache.spark.sql.{DataFrame, SparkSession}

/**
  * Stores computation result, no business logic here.
  */
trait Sink {

  /**
    * Stores the result [[DataFrame]]
    */
  def sink(trackCounts: DataFrame)(implicit sparkSession: SparkSession): Unit
}
