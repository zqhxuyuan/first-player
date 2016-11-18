package spark

import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

object SparkCommons {

  //Configuration.load().getString("foo").getOrElse("boo")

  val sparkCluster = play.Play.application.configuration.getString("pontus.spark.master.local")

  lazy val conf = {
    new SparkConf(false)
      .setMaster(sparkCluster)
      .set("spark.ui.port","18080")
      .setAppName("play demo")
  }

  lazy val sc = SparkContext.getOrCreate(conf)
  lazy val sqlContext = new SQLContext(sc)
}