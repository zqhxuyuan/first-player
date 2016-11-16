package start.json

import play.api.libs.functional.syntax._
import play.api.libs.json.Json._
import play.api.libs.json._

/**
  * Created by zhengqh on 16/11/16.
  */
class JsonSerDeser {

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