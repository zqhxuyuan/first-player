package controllers

import javax.inject.{Inject, Singleton}

import org.apache.spark.launcher.SparkLauncher
import org.apache.spark.sql.DataFrame
import play.api.mvc._
import play.api.mvc.Controller
import spark.SparkCommons

import play.api.Play.current

@Singleton
class SparkController @Inject() extends Controller {

  val dataFile = "resources/tweet-json"
  lazy val rdd = SparkCommons.sqlContext.read.json(dataFile)

  def toJsonString(rdd:DataFrame):String = "["+rdd.toJSON.collect.toList.mkString(",\n")+"]"

  def count = Action {
    println(libs)
    Ok(rdd.count.toString)
  }

  def list = Action {
    Ok(toJsonString(rdd))
  }

  def filter(text:String) = Action {
    Ok(toJsonString(rdd.filter(rdd("text").contains(text))))
  }

  //run activator dist to generate zip, and unzip it
  //import import play.api.Play.current,
  def libs : Seq[String] = {
    val libDir = play.api.Play.application.getFile("target/universal/first-player-1.0-SNAPSHOT/lib")
    return if ( libDir.exists ) {
      libDir.listFiles().map(_.getCanonicalFile().getAbsolutePath()).filter(_.endsWith(".jar"))
    } else {
      throw new IllegalStateException(s"lib dir is missing: $libDir")
    }
  }

  /**
    * 将play打包后,放在6.53上并解压,运行
    * @return
    */
  def launcher() = Action {
    val spark = new SparkLauncher()
      .setAppResource(SparkCommons.sparkLibPath)
      .setMainClass("spark.SimpleApp.main")
      .setMaster("mesos://192.168.6.52:5050")
      .setSparkHome("/usr/install/spark-1.6.2-bin-2.4.1-new")
      .setConf(SparkLauncher.DRIVER_MEMORY, "1g")
      .launch();
    spark.waitFor();
    Ok("...")
  }

}
