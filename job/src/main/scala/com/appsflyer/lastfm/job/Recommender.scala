package com.appsflyer.lastfm.job

import java.sql.Timestamp

import org.apache.spark.sql._
import org.apache.spark.sql.functions._

class Recommender(source: Source, sink: Sink) {

  /**
    * 1. Computes [[DataFrame]] with columns
    * [[Source.TrackIdCol]] of type [[org.apache.spark.sql.types.StringType]],
    * [[Recommender.CountCol]] of type [[org.apache.spark.sql.types.IntegerType]]
    * 2. Enriches it with track attributes using [[Source.enrichWithAttributes()]]
    * 3. Sends result to [[Sink]]
    * @param maxSessions max number of largest user sessions to consider for computation
    * @param maxTracksToRecommend number of most popular tracks among top sessions to recommend
    * @param sessionTimeout max number of milliseconds after previous song the user
    *                       played to consider current song being played in the same session
    * @param sparkSession
    */
  final def recommend(maxSessions: Int, maxTracksToRecommend: Int, sessionTimeout: Long)(implicit sparkSession: SparkSession): Unit = {

    import sparkSession.implicits._

    val groupedDF = source.source
      .groupBy(Source.UserIdCol)
      .agg(
        collect_list(Source.TimestampCol).as(Source.TimestampCol),
        collect_list(Source.TrackIdCol).as(Source.TrackIdCol)
      )

    val sessionsDF = groupedDF.flatMap { case Row(_, timestamps: Seq[Timestamp], trackIds: Seq[String]) =>
      var sessions: Seq[Seq[String]] = Seq.empty
      var currentSession: Seq[String] = Seq.empty
      var prevTimestamp: Long = -1
      trackIds zip timestamps sortBy (_._2.getTime) foreach { case (trackId, timestamp) =>
        if (currentSession.isEmpty) {
          prevTimestamp = timestamp.getTime
          currentSession +:= trackId
        } else {
          if (timestamp.getTime - prevTimestamp > sessionTimeout) {
            sessions +:= currentSession
            currentSession = Seq.empty
          }
          prevTimestamp = timestamp.getTime
          currentSession +:= trackId
        }
      }
      if (currentSession.nonEmpty) {
        sessions +:= currentSession
      }
      sessions.map(session => (session, session.size))
    }.toDF("session", "track_count")

    val sortedSessionsDF = sessionsDF.sort(desc("track_count"))

    val topTracksDF = sortedSessionsDF.limit(maxSessions)
      .select(col("session"))
      .flatMap { case Row(session: Seq[String]) => session }
      .toDF("track")
      .groupBy(col("track")).agg(size(collect_list("track")).as("tracks_num"))
      .sort(desc("tracks_num"))
      .limit(maxTracksToRecommend)
      .toDF(Source.TrackIdCol, Recommender.CountCol)

    val resultDF = source.enrichWithAttributes(topTracksDF)
    sink.sink(resultDF)
  }
}

object Recommender {
  final val CountCol = "count"
}