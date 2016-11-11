package controllers

import javax.inject._
import models.Product
import play.api._
import play.api.mvc._

import play.api.data.Form
import play.api.data.Forms.{mapping, longNumber, nonEmptyText}
import play.api.i18n.Messages
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi

import play.api.mvc.Flash

/**
  * Created by zhengqh on 16/11/8.
  */
@Singleton
class Products @Inject() (val messagesApi: MessagesApi) extends Controller with I18nSupport{
  val ImageResolution = 144

  def products = Action { implicit request =>
    val products = Product.findAll
    Ok(views.html.products.list(products))
  }

  def show(ean: Long) = Action { implicit request =>
    Product.findByEan(ean).map { product =>
      Ok(views.html.products.details(product))
    }.getOrElse(NotFound)
  }

  def barcode(ean: Long) = Action {
    import java.lang.IllegalArgumentException
    val MimeType = "image/png"
    try {
      val imageData = ean13BarCode(ean, MimeType)
      Ok(imageData).as(MimeType)
    }
    catch {
      case e: IllegalArgumentException =>
          BadRequest("Couldn’t generate bar code. Error: " + e.getMessage)
    }
  }
  def ean13BarCode(ean: Long, mimeType: String): Array[Byte] = {
    import java.io.ByteArrayOutputStream
    import java.awt.image.BufferedImage
    import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider
    import org.krysalis.barcode4j.impl.upcean.EAN13Bean
    val output: ByteArrayOutputStream = new ByteArrayOutputStream
    val canvas: BitmapCanvasProvider =
      new BitmapCanvasProvider(output, mimeType, ImageResolution,
        BufferedImage.TYPE_BYTE_BINARY, false, 0)
    val barcode = new EAN13Bean()
    barcode.generateBarcode(canvas, String valueOf ean)
    canvas.finish
    output.toByteArray
  }


  private val productForm: Form[Product] = Form(
    mapping(
      "ean" -> longNumber.verifying("validation.ean.duplicate", Product.findByEan(_).isEmpty),
      "name" -> nonEmptyText,
      "description" -> nonEmptyText
    )(Product.apply)(Product.unapply)
  )

  def newProduct = Action { implicit request =>
    val form = request.flash.get("error")  match {
      case Some(_) => productForm.bind(request.flash.data)
      case None => productForm
    }
    Ok(views.html.products.edit(form))
  }

  def save = Action { implicit request =>
    val newProductForm = productForm.bindFromRequest()
    newProductForm.fold(
      hasErrors = { form =>
        Redirect(routes.Products.newProduct()).flashing(Flash(form.data) + ("error" -> Messages("validation.errors")))
      },
      success = { newProduct =>
        Product.add(newProduct)
        val message = Messages("products.new.success", newProduct.name)
        //重定向到修改页面
        //Redirect(routes.Products.show(newProduct.ean)).flashing("success" -> message)
        //重定向到列表页面
        Redirect(routes.Products.products()).flashing("success" -> message)
      }
    )
  }
}
