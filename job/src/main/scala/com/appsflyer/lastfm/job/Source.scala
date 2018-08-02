package com.appsflyer.lastfm.job

import org.apache.spark.sql.{DataFrame, SparkSession}

trait Source {

  /**
    * Provides [[DataFrame]] with columns:
    * [[Source.UserIdCol]] of type [[org.apache.spark.sql.types.StringType]],
    * [[Source.TimestampCol]] of type [[org.apache.spark.sql.types.TimestampType]],
    * [[Source.TrackIdCol]] of type [[org.apache.spark.sql.types.StringType]]
    */
  def source(implicit sparkSession: SparkSession): DataFrame

  /**
    * Creates new [[DataFrame]] with appended attribute columns
    * @param dataFrame [[DataFrame]] with column [[Source.TrackIdCol]]
    * @param sparkSession [[SparkSession]] conducting the application
    * @return enriched [[DataFrame]]
    */
  def enrichWithAttributes(dataFrame: DataFrame)(implicit sparkSession: SparkSession): DataFrame = dataFrame
}

object Source {
  final val UserIdCol = "user"
  final val TimestampCol = "timestamp"
  final val TrackIdCol = "trackId"
}
