import java.text.SimpleDateFormat
import java.util.Date

import com.appsflyer.lastfm.job.Runner
import org.apache.spark.sql.SparkSession

import scala.util.Try

object Main {

  private def timed[T](block: => T): T = {
    val start = System.currentTimeMillis()
    val res = Try {
      block
    }
    val elapsed = System.currentTimeMillis() - start

    val format = new SimpleDateFormat("mm:ss:SSS").format(new Date(elapsed))
    s"""
       |* * * * * * * * * * * * * * *
       |* Execution time: $format
       |* * * * * * * * * * * * * * *
      """.stripMargin
    println(format)
    res.get
  }

  def main(args: Array[String]): Unit = timed {

    implicit val sparkSession: SparkSession = SparkSession.builder()
      .master("local[*]")
      .appName("Local Spark LastFM music recommender")
      .getOrCreate

    Runner.main(args)

    sparkSession.stop()
  }
}
