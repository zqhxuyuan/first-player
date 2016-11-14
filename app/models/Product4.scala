package models

case class Product4(
                    ean: Long,
                    name: String,
                    description: String,
                    pieces: Int,
                    active: Boolean)

object Product4 {
  def findByEan(ean: Long) = None
}
