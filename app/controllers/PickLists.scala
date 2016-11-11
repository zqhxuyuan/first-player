package controllers

import play.api._
import play.api.mvc._
import models.PickList
import java.util.Date
import javax.inject._
import play.twirl.api.Html
import scala.concurrent.{Future, ExecutionContext, future}
import javax.inject._


@Singleton
class PickLists @Inject() extends Controller {

  /**
    * Renders a pick list synchronously in the usual way.
    */
  def preview(warehouse: String) = Action {
    val pickList = PickList.find(warehouse)
    val timestamp = new java.util.Date
    Ok(views.html.picks.pickList(warehouse, pickList, timestamp))
  }

  /**
    * Starts a job to generate a pick list.
    */
  def sendAsync(warehouse: String) = Action {
    import ExecutionContext.Implicits.global
    future {
      val pickList = PickList.find(warehouse)
      send(views.html.picks.pickList(warehouse, pickList, new Date))
    }
    Redirect(routes.PickLists.index())
  }

  def index = Action {
    Ok(views.html.picks.index())
  }

  /**
    * Stub for ‘sending’ the pick list as an HTML document, e.g. by e-mail.
    */
  private def send(html: Html) {
    Logger.info(html.body)
    // Send pick list…
  }

  def backlog(warehouse: String) = Action.async {
    import scala.concurrent.ExecutionContext.Implicits.global

    val backlog : Future[String] = scala.concurrent.Future {
      models.Order.backlog(warehouse)
    }
    backlog.map(value => Ok(value))
  }

}