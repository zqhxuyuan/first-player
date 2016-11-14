package controllers

import java.io.File
import javax.inject.{Inject, Singleton}

import models.Product4
import play.api._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import play.api.data.{Forms, Form}
import play.api.data.Forms._

@Singleton
class FormDemos @Inject() (val messagesApi: MessagesApi) extends Controller with I18nSupport{
//class FormDemos @Inject() extends Controller {

  //1.手动创建表单

  //创建Form表单
  def createForm() = Action {
    Ok(views.html.forms.maualForm())
  }

  val productForm = Form(mapping(
    "ean" -> longNumber.verifying("This product already exists!", Product4.findByEan(_).isEmpty),
    "name" -> nonEmptyText,
    "description" -> text,
    "pieces" -> number,
    "active" -> boolean)(Product4.apply)(Product4.unapply).
      //global validate
      verifying("Product can not be active if the description is empty",
        product => !product.active || product.description.nonEmpty))

  //保存Form表单
  def create() = Action { implicit request =>
    productForm.bindFromRequest.fold(
      formWithErrors => Forbidden("Oh noes, invalid submission!"),
      value => Ok("created: " + value))
  }

  //2.使用Play自带的模板

  def createFormGen() = Action {
    Ok(views.html.forms.generatedForm(productForm))
  }

  def createGen() = Action { implicit request =>
    productForm.bindFromRequest.fold(
      formWithErrors => Ok(views.html.forms.generatedForm(formWithErrors)),
      value => Ok("created: " + value)
    )
  }

  def updateForm(ean: Long) = Action {
    Product4.findByEan(ean).map { product =>
      val filledForm = productForm.fill(product)
      Ok(views.html.forms.generatedForm(filledForm))
    }.getOrElse(NotFound)
  }


  //3.自定义类型
  val datetimeForm = Form(Forms.single("mydatetime" -> Forms.text))

  def customForm() = Action {
    Ok(views.html.forms.customForm(datetimeForm))
  }

  //4. Bootstrap FieldContstructor
  def createFormBootstrap() = Action {
    Ok(views.html.forms.generatedFormBootstrap(productForm))
  }
  def createBootstrap() = Action { implicit request =>
    productForm.bindFromRequest.fold(
      formWithErrors => Ok(views.html.forms.generatedFormBootstrap(formWithErrors)),
      value => Ok("created: " + value)
    )
  }

  //5.上传
  def showManualUploadForm() = Action {
    Ok(views.html.forms.manualUpload())
  }

  //手动上传(原声的HTML)
  def upload() = Action(parse.multipartFormData) { request =>
    //request.body is of type MultipartFormData[TemporaryFile].
    //You can extract a file by the name of the input field—image in our case.
    //This gives you a FilePart[TemporaryFile], which has a ref property,
    //a reference to the Temporary- File that contains the uploaded file.
    request.body.file("image").map { file =>
      file.ref.moveTo(new File("/tmp/img"))
      Ok("Retrieved file %s" format file.filename)
    }.getOrElse(BadRequest("File missing!"))
  }

  //使用Play的Form表单组件,和普通表单一样,不过页面中设置了enctype
  //由于使用Play的Form表单,需要传递Form,所以这里用一个ignored的Form
  def showUploadForm() = Action {
    //This form does nothing, but it will allow us to invoke the template, which will nicely
    //render an empty HTML form without errors. It’s not super neat, but it works, and you’ll
    //have to decide for yourself whether you want to do this in order to be able to reuse form validation for forms with file uploads.
    val dummyForm = Form(ignored("dummy"))
    Ok(views.html.forms.uploadform(dummyForm))
  }

  def upload2() = Action(parse.multipartFormData) { implicit request =>
    //Even though you don’t use forms for processing files, you can still
    //use them for generating inputs and reporting validation errors.
    //You can use the ignored mapping and a custom validation to validate file uploads with a form
    //虽然处理文件时不需要Form表单,但是仍然可以用Form来生成输入以及验证错误(就像普通的输入框使用Form一样)
    val form = Form(tuple(
      "description" -> text(minLength = 10),
      //上传处理,必须要有文件,如果没有文件,则验证不通过.  ignored方法的参数是Option[MultipartFormData.FilePart[Files.TemporaryFile]]
      //ignored表示忽略了文件的数据(文件的实际内容),但是文件本身并不会忽略,它会用来判断是否上传了文件
      //we used the ignored mapping, which ignores the form data but delivers its parameter as the value,
      //in this case the request.body.file(“image”) value.
      //Then we use a custom validation C to verify whether the Option[FilePart] is defined.
      "image" -> ignored(request.body.file("image")).verifying("File missing", _.isDefined)
    ))

    //文件的Form和普通的productForm使用类似
    form.bindFromRequest.fold(
      formWithErrors => {
        //如果上次错误,重新回到上传页面,同时也会显示上一次上传时的其他字段
        Ok(views.html.forms.uploadform(formWithErrors))
      },
      value => Ok("Found file: %s with description %s" format(value._2.get.filename, value._1))
    )
  }

}