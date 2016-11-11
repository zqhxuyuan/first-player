package models

case class Cart(products: Map[Int, Product3]) {
  def productCount = products.keys.foldLeft(0)(_ + _)
}

object Cart {
  def demoCart() = {
    Cart(Map(
      3 -> ProductDAO.list(0),
      1 -> ProductDAO.list(1)))
  }
}

case class Product3(
                    id: Long,
                    name: String,
                    price: BigDecimal,
                    description: String)

object ProductDAO {

  def list: Seq[Product3] = List(
    Product3(123, "400 small paperclips", 4.95, "A box of 400 small paperclips"),
    Product3(124, "150 big paperclips", 5.95, "A box of 150 big paperclips"),
    Product3(233, "Blue ballpoint", 1.75, "Blue ballpoint pen"))
}