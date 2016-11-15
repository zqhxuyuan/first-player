package controllers

import javax.inject.{Inject, Singleton}

import models.Product4
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import play.api.data.Forms._
import play.api.data.validation.Constraints._

@Singleton
class PlayBootstrap @Inject() (val messagesApi: MessagesApi) extends Controller with I18nSupport {

  val fooForm = Form(single("foo" -> text(maxLength = 20)))

  def bootstrap() = Action { implicit request =>
    Ok(views.html.bootstrap.form(fooForm))
  }

  def mixed() = Action {
    Ok("")
  }

  val productForm = Form(mapping(
    "ean" -> longNumber.verifying("This product already exists!", Product4.findByEan(_).isEmpty),
    "name" -> nonEmptyText,
    "description" -> text,
    "pieces" -> number,
    "active" -> boolean)(Product4.apply)(Product4.unapply))

  def createFormGen() = Action { implicit request =>
    Ok(views.html.bootstrap.form2(productForm))
  }

  def createGen() = Action { implicit request =>
    productForm.bindFromRequest.fold(
      formWithErrors => Ok(views.html.bootstrap.form2(formWithErrors)),
      value => Ok("created: " + value)
    )
  }
}