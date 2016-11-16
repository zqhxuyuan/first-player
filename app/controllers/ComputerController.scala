package controllers

import javax.inject.Inject

import models._
import play.api.data.Forms._
import play.api.data._
import play.api.i18n._
import play.api.mvc._
import views._

class ComputerController @Inject() (computerService: ComputerService,
                                    companyService: CompanyService,
                                    val messagesApi: MessagesApi)
  extends Controller with I18nSupport {

  val Home = Redirect(routes.ComputerController.list(0, 2, ""))

  val computerForm = Form(
    mapping(
      "id" -> ignored(None:Option[Long]),
      "name" -> nonEmptyText,
      "introduced" -> optional(date("yyyy-MM-dd")),
      "discontinued" -> optional(date("yyyy-MM-dd")),
      "company" -> optional(longNumber)
    )(Computer.apply)(Computer.unapply)
  )

  def index = Action { Home }

  def list(page: Int, orderBy: Int, filter: String) = Action { implicit request =>
    Ok(html.computer.list(
      computerService.list(page = page, orderBy = orderBy, filter = ("%"+filter+"%")),
      orderBy, filter
    ))
  }

  def edit(id: Long) = Action {
    computerService.findById(id).map { computer =>
      Ok(html.computer.editForm(id, computerForm.fill(computer), companyService.options))
    }.getOrElse(NotFound)
  }

  def update(id: Long) = Action { implicit request =>
    computerForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.computer.editForm(id, formWithErrors, companyService.options)),
      computer => {
        computerService.update(id, computer)
        Home.flashing("success" -> "Computer %s has been updated".format(computer.name))
      }
    )
  }

  def create = Action {
    Ok(html.computer.createForm(computerForm, companyService.options))
  }

  def save = Action { implicit request =>
    computerForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.computer.createForm(formWithErrors, companyService.options)),
      computer => {
        computerService.insert(computer)
        Home.flashing("success" -> "Computer %s has been created".format(computer.name))
      }
    )
  }

  def delete(id: Long) = Action {
    computerService.delete(id)
    Home.flashing("success" -> "Computer has been deleted")
  }

}

