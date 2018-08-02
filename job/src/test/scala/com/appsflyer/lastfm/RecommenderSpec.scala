package com.appsflyer.lastfm

import java.sql.Timestamp
import java.time.Instant

import com.appsflyer.lastfm.job._
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration._

/**
  * Test for [[Recommender]]
  */
class RecommenderSpec extends WordSpec with Matchers {

  "Recommender" should {
    "produce correct top track list" in {

      val data = Seq(
        ("u1", "2008-04-25T12:00:00Z", "t1"), // s1
        ("u1", "2008-04-25T12:01:00Z", "t1"), // s1
        ("u1", "2008-04-25T12:02:00Z", "t2"), // s1
        ("u1", "2008-04-25T12:03:00Z", "t3"), // s1

        ("u1", "2008-04-25T12:24:00Z", "t4"), // s2
        ("u1", "2008-04-25T12:25:00Z", "t1"), // s2

        ("u1", "2008-04-25T12:46:00Z", "t2"), // s3

        ("u2", "2008-04-25T12:00:00Z", "t7"), // s4
        ("u2", "2008-04-25T12:01:00Z", "t2"), // s4

        ("u2", "2008-04-25T12:22:00Z", "t1"), // s5
        ("u2", "2008-04-25T12:23:00Z", "t2"), // s5
        ("u2", "2008-04-25T12:24:00Z", "t3"), // s5

        ("u3", "2008-04-25T12:01:00Z", "t1"), // s6

        ("u3", "2008-04-25T12:22:00Z", "t1"), // s7
        ("u3", "2008-04-25T12:23:00Z", "t2"), // s7
        ("u3", "2008-04-25T12:24:00Z", "t1"), // s7
        ("u3", "2008-04-25T12:25:00Z", "t4")  // s7
      )

      // longest 3 sessions:
      // s1 - 4 tracks
      // s7 - 4 tracks
      // s5 - 3 tracks

      val expectedTracks = Seq(
        ("t1", 5),
        ("t2", 3),
        ("t3", 2),
        ("t4", 1)
      )
      val recNum = 5
      val sesNum = 3
      val sessionTimeoutMillis = 20.minutes.toMillis

      implicit val sparkSession: SparkSession = SparkSession.builder()
        .master("local[*]")
        .appName("Spark music recommender test")
        .getOrCreate

      val testSource = new Source {
        override def source(implicit sparkSession: SparkSession): DataFrame =
          sparkSession.createDataFrame(
            data.map { case (userId, timestamp, trackId) =>
              (userId, Timestamp.from(Instant.parse(timestamp)), trackId)
            }
          ).toDF(Source.UserIdCol, Source.TimestampCol, Source.TrackIdCol)
      }

      val testSink = new Sink {
        override def sink(trackCounts: DataFrame)(implicit sparkSession: SparkSession): Unit = {
          val recommendations = trackCounts.collect()
            .map { case Row(trackId: String, count: Int) => (trackId, count) }
          recommendations shouldBe expectedTracks
        }
      }

      new Recommender(testSource, testSink).recommend(sesNum, recNum, sessionTimeoutMillis)

      sparkSession.stop()
    }
  }
}
