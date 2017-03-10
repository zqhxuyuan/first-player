package controllers

import javax.inject.{Inject, Singleton}

import play.api.i18n.{MessagesApi, I18nSupport}
import play.api.mvc._
import play.api.mvc.Controller

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import views._
import models._
import scala.collection.mutable.ArrayBuffer
import play.api.libs.json._
import play.api.libs.json.Json
import play.api.libs.json.Json._
import play.api.data.format.Formats._  // needed for `of[Double]` in mapping (which i initially used)
import java.util.Calendar

class TransactionController @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport{

  val _transaction: Form[Transaction] = Form(
    mapping(
      "id" -> longNumber,
      "symbol" -> nonEmptyText,
      "ttype" -> nonEmptyText,
      "quantity" -> number,
      "datetime" -> date("yyyy-MM-dd"),
      "notes" -> text
    )(Transaction.apply)(Transaction.unapply) //apply和unapply
  )

  val transactionForm: Form[Transaction] = Form(
    // the names you use in this mapping (such as 'symbol') must match the names that will be POSTed to your methods in JSON.
    // note: skipping `id` field and `datetime` field
    mapping(
      // verifying here creates a field-level error; if your test returns false, the error is shown
      "symbol" -> nonEmptyText,
      "ttype" -> nonEmptyText,
      "quantity" -> number,
      "notes" -> text
    )
      // transactionForm -> Transaction
      ((symbol, ttype, quantity, notes) => Transaction(0, symbol, ttype, quantity, Calendar.getInstance.getTime, notes))
      // Transaction -> TransactionForm
      ((t: Transaction) => Some(t.symbol, t.ttype, t.quantity, t.notes))
  )

  def list = Action {
    val transactions = Transaction.list()
    Ok(views.html.transaction.list(transactions))
  }

  def add = Action {
    Ok(views.html.transaction.createForm(transactionForm))
  }

  def save = Action { implicit request =>
    transactionForm.bindFromRequest.fold(
      errors => {
        Ok(Json.toJson(Map("success" -> toJson(false), "msg" -> toJson("Boom!"), "id" -> toJson(0))))
      },
      transaction => {
        val id = Transaction.insert(transaction)
        id match {
          case Some(autoIncrementId) =>
            val result = Map("success" -> toJson(true), "msg" -> toJson("Success!"), "id" -> toJson(autoIncrementId))
            Ok(Json.toJson(result))
          case None =>
            // TODO inserts can fail; i need to handle this properly.
            val result = Map("success" -> toJson(true), "msg" -> toJson("Success!"), "id" -> toJson(-1))
            Ok(Json.toJson(result))
        }
      }
    )
  }

}