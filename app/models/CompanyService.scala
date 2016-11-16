package models

import javax.inject.Inject

import anorm.SqlParser._
import anorm._

import play.api.db.DBApi

case class Company(id: Option[Long] = None, name: String)

@javax.inject.Singleton
class CompanyService @Inject() (dbapi: DBApi) {

  private val db = dbapi.database("default")

  /**
    * Parse a Company from a ResultSet
    */
  val simple = {
    get[Option[Long]]("company.id") ~
      get[String]("company.name") map {
      case id~name => Company(id, name)
    }
  }

  /**
    * Construct the Map[String,String] needed to fill a select options set.
    */
  def options: Seq[(String,String)] = db.withConnection {
    implicit connection =>
      SQL("select * from company order by name").as(simple *).
        foldLeft[Seq[(String, String)]](Nil) {
          (cs, c) =>  //cs: column sets(result), c: column(each record)
            //cs: Seq[(String, String)], c: (String,String), c.id: String
            c.id.fold(cs) {  //use column's id to fold with cs
              id => //this id is c.id
                cs :+ (id.toString -> c.name) //finally gather to cs
            }
        }
  }

}


