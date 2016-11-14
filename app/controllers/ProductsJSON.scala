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

  // JSON序列化和反序列化测试

  def jsonSerTest(): Unit ={
    val jsonString = Json.toJson("Johnny")
    val jsonNumber = Json.toJson(Some(42))
    val jsonObject = Json.toJson(
      Map("first_name" -> "Johnny", "last_name" -> "Johnson")
    )

    val category = JsString("paperclips")
    val quantity = JsNumber(42)

    val product = Json.obj(
      "name" -> JsString("Blue Paper clips"),
      "ean" -> JsString("12345432123"),
      "description" -> JsString("Big box of paper clips"),
      "pieces" -> JsNumber(500),
      "manufacturer" -> Json.obj(
        "name" -> JsString("Paperclipfactory Inc."),
        "contact_details" -> Json.obj(
          "email" -> JsString("contact@paperclipfactory.example.com"),
          "fax" -> JsNull,
          "phone" -> JsString("+12345654321")
        ) ),
      "tags" -> Json.arr(
        JsString("paperclip"),
        JsString("coated")
      ),
      "active" -> JsBoolean(true)
    )

    val productJsonString = Json.stringify(product)
    val productJsonString2 = product.toString()

    val path = JsPath \ "ean"
    val writer = path.write[Long]
    val jsValue = Json.toJson(5010255079763L)(writer) //显示传递Writer

    implicit val writer2 = path.write[Long]
    val jsValue2 = Json.toJson(5010255079763L)
  }

  def jsonDeser(): Unit ={
    val json: JsValue = toJson(Map(
      "name" -> toJson("Johnny"),
      "age" -> toJson(42),
      "tags" -> toJson(List("constructor", "builder")),
      "company" -> toJson(Map(
        "name" -> toJson("Constructors Inc.")))))

    val name = (json \ "name").as[String]//(play.api.libs.json.Reads.StringReads)
    val age = (json \ "age").asOpt[Int]
    val companyName = (json \ "company" \ "name").as[String]
    val firstTag = (json \ "tags")(0).as[String]
    val allNames = (json \\ "name").map(_.as[String])
    val zipNonExist = (json \ "company" \ "address" \ "zipcode").asOpt[String]

    val pricedProductJsonString = """{
      "name": "Sample name",
      "description": "Sample description",
      "purchase_price" : 20,
      "selling_price": 35
    }"""

    implicit val productReads: Reads[PricedProduct] = (
      (JsPath \ "name").read[String] and
        (JsPath \ "description").readNullable[String] and
        (JsPath \ "purchase_price").read[BigDecimal] and
        (JsPath \ "selling_price").read[BigDecimal]
      )(PricedProduct.apply _)

    val productJsValue = Json.parse(pricedProductJsonString)
    val pricedProduct = productJsValue.as[PricedProduct]

    //序列化Writes[Product]和反序列化Reads[Product]结合起来

    //方式1:直接定义
    implicit val productFormat = (
      (JsPath \ "name").format[String] and
        (JsPath \ "description").formatNullable[String] and
        (JsPath \ "purchase_price").format[BigDecimal] and
        (JsPath \ "selling_price").format[BigDecimal]
      )(PricedProduct.apply, unlift(PricedProduct.unapply))

    //方式2:先分别定义Writes和Reads,再组成Format
    implicit val productWrites: Writes[PricedProduct] = (
      (JsPath \ "name").write[String] and
        (JsPath \ "description").writeNullable[String] and
        (JsPath \ "purchase_price").write[BigDecimal] and
        (JsPath \ "selling_price").write[BigDecimal]
      )(unlift(PricedProduct.unapply))

    implicit val productFormat2 = Format(productReads, productWrites)

    //根据Format获取对应的Reads和Writes
    implicit val productReads2 = Json.reads[PricedProduct]
    implicit val productWrites2 = Json.writes[PricedProduct]
  }
}

case class PricedProduct(
                          name: String,
                          description: Option[String],
                          purchasePrice: BigDecimal,
                          sellingPrice: BigDecimal)