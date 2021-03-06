package controllers

import javax.inject._
import models.Product
import play.api._
import play.api.mvc._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() extends Controller {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def hello(name: String) = Action {
    //Ok("Hello " + name)
    Ok(views.html.hello(name))
  }

  def echarts = Action {
    Ok(views.html.echarts())
  }
}
