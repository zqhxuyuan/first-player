package controllers

import javax.inject.{Inject, Singleton}

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import play.api.mvc.Controller
import models.Product
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.Logger

import Json._

@Singleton
class ProductsJSON @Inject() (val messagesApi: MessagesApi) extends Controller with I18nSupport{

  //Returns an array of products’ EAN codes.
  def list = Action {
    val productCodes = Product.findAll.map(_.ean)
    //productCodes是List[Long]类型,直接使用Json.toJson转换为Json格式:JsValue
    val jsValue = Json.toJson(productCodes)
    //如果是JSON,响应会自动添加Content- Type: application/json
    Ok(jsValue)//.as(JSON)
  }

  def index = Action {
    Ok(views.html.products.index())
  }

  //序列化自定义对象为JSON的方式: 显示Writes,隐式Writes,JsPath(显示和隐式)

  //1.自定义Product转换成JSON的Writes(序列化),显示传递给toJson方法,也可以隐式传递
  object ProductWrites1 extends Writes[Product] {
    def writes(p: Product) = Json.obj(
      "ean" -> Json.toJson(p.ean),
      "name" -> Json.toJson(p.name),
      "description" -> Json.toJson(p.description)
    )
  }
  def details1(ean: Long) = Action {
    Product.findByEan(ean).map { product =>
      Ok(Json.toJson(product)(ProductWrites1))
    }.getOrElse(NotFound)
  }

  //2.Formats a Product instance as JSON. 隐式传递Writes[Product]
  implicit object ProductWrites extends Writes[Product] {
    def writes(p: Product) = Json.obj(
      "ean" -> Json.toJson(p.ean),
      "name" -> Json.toJson(p.name),
      "description" -> Json.toJson(p.description)
    )
  }
  //Returns details of the given product. 详情页面,需要将(从DB查询出来的)Product对象转换为JSON
  def details(ean: Long) = Action {
    Product.findByEan(ean).map { product =>
      Ok(Json.toJson(product))  //Writes[Product]
    }.getOrElse(NotFound)
  }

//  implicit val productWrites1 = Json.writes[Product]

  //3.JsPath方式定义Writes[Product]序列化方式. 当然productWrites参数也可以定义为隐式
  val productWrites2: Writes[Product] = (
    (JsPath \ "ean").write[Long] and
      (JsPath \ "name").write[String] and
      (JsPath \ "description").write[String]
    )(unlift(Product.unapply))
  def details2(ean: Long) = Action {
    Product.findByEan(ean).map { product =>
      Ok(Json.toJson(product)(productWrites2))
    }.getOrElse(NotFound)
  }

  //4.JsPath隐式传递,It's OK,but should not have two implicit together
  /*
  implicit val productWrites3: Writes[Product] = (
    (JsPath \ "ean").write[Long] and
      (JsPath \ "name").write[String] and
      (JsPath \ "description").write[String]
    )(unlift(Product.unapply))
  def details3(ean: Long) = Action {
    Product.findByEan(ean).map { product =>
      Ok(Json.toJson(product))
    }.getOrElse(NotFound)
  }
  */

  //==========================================

  //Parses a JSON object
  implicit val productReads: Reads[Product] = (
    (JsPath \ "ean").read[Long] and
      (JsPath \ "name").read[String] and
      (JsPath \ "description").read[String]
    )(Product.apply _)

  //Saves a product
  def save(ean: Long) = Action(parse.json) { request =>
    Logger.info("start")
    try {
      val productJson = request.body
      val product = productJson.as[Product] //Reads[Product]
      Product.save(product)
      Ok("Saved")
    }
    catch {
      case e:IllegalArgumentException => BadRequest("Product not found")
      case e:Exception => {
        Logger.info("exception = %s" format e)
        BadRequest("Invalid EAN")
      }
    }
  }

  //==========================================

  def postProduct() = Action { request =>
    //正常情况下,需要自己解析成JsValue
    val jsValue1: JsValue = Json.parse("""{ "name" : "Johnny" }""")

    //val jsValue = JsString("Johnny")
    //val name = jsValue.as[String]

    //不过如果请求的类型是:application/json,则可以通过asJson将请求体直接解析成JsValue
    val jsValueOption = request.body.asJson
    jsValueOption.map { json =>
      // Do something with the JSON(即JsValue)
      val age: Option[Int] = json.asOpt[Int] // == None
      val name: Option[String] = json.asOpt[String] // == Some("Johnny")

      val age1 = json.validate[Int] // == JsError
      val name1 = json.validate[String] // == JsSuccess(Johnny,)

    }.getOrElse {
      // Not a JSON body
    }
    Ok("test1")
  }

  //如果只需要处理JSON格式,则可以在Action中指定解析方式,其他类型的不会解析
  def postProduct2() = Action(parse.json) { request =>
    val jsValue = request.body
    // Do something with the JSON
    Ok("test2")
  }

}