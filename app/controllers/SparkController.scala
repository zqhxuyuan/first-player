package controllers

import javax.inject.{Inject, Singleton}

import org.apache.spark.sql.DataFrame
import play.api.mvc._
import play.api.mvc.Controller
import spark.SparkCommons

@Singleton
class SparkController @Inject() extends Controller {

  val dataFile = "resources/tweet-json"
  lazy val rdd = SparkCommons.sqlContext.read.json(dataFile)

  def toJsonString(rdd:DataFrame):String = "["+rdd.toJSON.collect.toList.mkString(",\n")+"]"

  def count = Action {
    Ok(rdd.count.toString)
  }

  def list = Action {
    Ok(toJsonString(rdd))
  }

  def filter(text:String) = Action {
    Ok(toJsonString(rdd.filter(rdd("text").contains(text))))
  }

  /*
  def libs : Seq[String] = {
    val libDir = play.api.Play.application.getFile("lib")
    return if ( libDir.exists ) {
      libDir.listFiles().map(_.getCanonicalFile().getAbsolutePath()).filter(_.endsWith(".jar"))
    } else {
      throw new IllegalStateException(s"lib dir is missing: $libDir")
    }
  }
  */
}
