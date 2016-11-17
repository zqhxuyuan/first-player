package controllers

import javax.inject.{Inject, Singleton}

import org.apache.spark.sql.DataFrame
import play.api.mvc._
import play.api.mvc.Controller
import util.SparkCommons

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

}
