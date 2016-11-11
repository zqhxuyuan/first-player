package controllers

import javax.inject.{Inject, Singleton}

import models.{Cart, ProductDAO}
import play.api.i18n.MessagesApi
import play.api.mvc.{RequestHeader, Action, Controller}

@Singleton
class ProductsLayout @Inject() extends Controller {

  def catalog() = Action {
    val products = ProductDAO.list
//    Ok(views.html.shop.catalog(products))
//    Ok(views.html.shop.catalog1(products))
//    Ok(views.html.shop.catalog2(products))
//    Ok(views.html.shop.catalog3(products))
    Ok(views.html.shop.catalog4(products))
  }

  //normal invoke, must pass Cart Object
  def catalog10() = Action { request =>
    val products = ProductDAO.list
    Ok(views.html.shop.catalog10(products, cart(request)))
  }
  def cart(request: RequestHeader) = {
    Cart.demoCart()
  }

  //implicit invoke, no need to pass Cart
  def catalog11() = Action { implicit request =>
    val products = ProductDAO.list
    //too many arguments for method apply: (content: C)(implicit writeable: play.api.http.Writeable[C])play.api.mvc.Result in class Status
    //Ok(views.html.shop.catalog11(products), cart(request))

    //如果没有定义implicit的方法,是不会直接使用上面的cart方法的
    //could not find implicit value for parameter cart: models.Cart
    Ok(views.html.shop.catalog11(products))
  }

  //方法名称任意,只要定义成implicit即可
  //注意:如果去掉返回类型: models.Cart,也会报错说找不到models.Cart,所以要显示声明返回类型
  implicit def cartImplicit(implicit request: RequestHeader): models.Cart = {
    Cart.demoCart()
  }
  //除了方法,也可以使用trait接口的形式
}

trait WithCart {
  implicit def cart(implicit request: RequestHeader) = {
    // Get cart from session
    Cart.demoCart()
  }
}
