package com.appsflyer.lastfm.job

import java.sql.Timestamp

import org.apache.spark.sql.functions.col
import org.apache.spark.sql.types.{StringType, StructField, StructType, TimestampType}
import org.apache.spark.sql.{DataFrame, SparkSession}

class LastFMCSVSource(path: String, inferTrackId: Boolean) extends Source {

  final val UserIdCol = "userid"
  final val TimestampCol = "timestamp"
  final val TrackArtistIdCol = "musicbrainz-artist-id"
  final val TrackArtistNameCol = "artist-name"
  final val TrackIdCol = "musicbrainz-track-id"
  final val TrackNameCol = "track-name"

  /*
  Experimental.
  Some tracks don't have trackId but have other attributes (track name, track artist id, track artist name).
  These attributes are used to construct trackId.
  This approach may cause situation when different songs are considered the same (collisions in attributes), it assumes:
  1. users may like different tracks of one artist in the same way
  2. users may like different tracks with the same name in the same way
   */
  private def withInferredTrackId(allDF: DataFrame)(implicit sparkSession: SparkSession): DataFrame = {
    import sparkSession.implicits._

    allDF.map { row => {
      val userIdCol = row.getAs[String](UserIdCol)
      val timestamp = row.getAs[Timestamp](TimestampCol)
      val trackArtistId = row.getAs[String](TrackArtistIdCol)
      val trackArtistName = row.getAs[String](TrackArtistNameCol)
      var trackId = row.getAs[String](TrackIdCol)
      val trackName = row.getAs[String](TrackNameCol)

      trackId = Option(trackId).getOrElse {
        val name = Option(trackName).getOrElse("")
        val artistId = Option(trackArtistId).getOrElse("")
        val artistName = Option(trackArtistName).getOrElse("")

        if (name.isEmpty && artistId.isEmpty && artistName.isEmpty) null
        else s"$name-$artistId-$artistName"
      }
      (userIdCol, timestamp, trackArtistId, trackArtistName, trackId, trackName)
    }
    }.toDF(UserIdCol, TimestampCol, TrackArtistIdCol, TrackArtistNameCol, TrackIdCol, TrackNameCol)

  }

  private var filteredDF: DataFrame = _

  override def enrichWithAttributes(dataFrame: DataFrame)(implicit sparkSession: SparkSession): DataFrame = {
    val right = filteredDF.select(TrackArtistIdCol, TrackArtistNameCol, TrackIdCol, TrackNameCol).distinct()
    val left = dataFrame
    left.join(right, col(Source.TrackIdCol) === col(TrackIdCol))
    .select(
      left.col(Recommender.CountCol),
      right.col(TrackIdCol),
      right.col(TrackNameCol),
      right.col(TrackArtistIdCol),
      right.col(TrackArtistNameCol)
    )
  }

  override def source(implicit sparkSession: SparkSession): DataFrame = {
    val schema = StructType(
      Seq(
        StructField(UserIdCol, StringType),
        StructField(TimestampCol, TimestampType),
        StructField(TrackArtistIdCol, StringType),
        StructField(TrackArtistNameCol, StringType),
        StructField(TrackIdCol, StringType),
        StructField(TrackNameCol, StringType)
      )
    )

    val allDF = sparkSession.read.format("com.databricks.spark.csv")
      .option("header", "false")
      .option("sep", "\\t")
      .schema(schema)
      .load(path)

    val sourceDF = if (inferTrackId) withInferredTrackId(allDF) else allDF

    this.filteredDF = sourceDF.filter(
      col(TrackIdCol).isNotNull and col(UserIdCol).isNotNull and col(TimestampCol).isNotNull
    )

    filteredDF.select(
      col(UserIdCol).as(Source.UserIdCol),
      col(TimestampCol).as(Source.TimestampCol),
      col(TrackIdCol).as(Source.TrackIdCol)
    )
  }
}
