package com.appsflyer.lastfm.job

import org.apache.spark.sql.SparkSession

object Runner {

  /**
    * Run LastFM top tracks recommender job
    *
    * @param inputPath path to input file
    * @param outputPath path to outputDirectory which shouldn't exist
    * @param numSessions number of top user sessions to consider when calculating recommendation
    * @param numTracks number of most popular tracks among top sessions to recommend
    * @param sessionTimeoutMillis max number of milliseconds after previous song the user
    *                             played to consider current song being played in the same session
    * @param inferTrackId some tracks in the dataset miss song id but have some other attributes defined.
    *                     If true these attributes are used to construct trackId where it is missing.
    *                     Tracks with missing trackId are filtered out from the computation.
    * @param sparkSession [[SparkSession]] conducting the application
    */
  def run(inputPath: String,
          outputPath: String,
          numSessions: Int, numTracks: Int, sessionTimeoutMillis: Long, inferTrackId: Boolean)(implicit sparkSession: SparkSession): Unit = {

    lazy val source = new LastFMCSVSource(inputPath, inferTrackId)
    lazy val sink = new FileSink(outputPath)

    new Recommender(source, sink).recommend(numSessions, numTracks, sessionTimeoutMillis)
  }

  def main(args: Array[String]): Unit = {
    val inputPath: String = args(0)
    val outputPath: String = args(1)
    val numSessions: Int = args(2).toInt
    val numTracks: Int = args(3).toInt
    val sessionTimeoutMillis = args(4).toLong
    val inferTrackId = args(5).toBoolean

    implicit lazy val sparkSession: SparkSession = SparkSession.builder
      .appName("LastFM music recommender")
      .getOrCreate()

    run(inputPath, outputPath, numSessions, numTracks, sessionTimeoutMillis, inferTrackId)
  }
}
