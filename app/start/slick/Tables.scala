package start.slick

import slick.driver.H2Driver.api._
import slick.lifted.{ForeignKeyQuery, ProvenShape}

// A Suppliers table with 6 columns: id, name, street, city, state, zip
class Suppliers(tag: Tag)
  extends Table[(Int, String, String, String, String, String)](tag, "SUPPLIERS") {

  // This is the primary key column:
  def id: Rep[Int] = column[Int]("SUP_ID", O.PrimaryKey)
  def name: Rep[String] = column[String]("SUP_NAME")
  def street: Rep[String] = column[String]("STREET")
  def city: Rep[String] = column[String]("CITY")
  def state: Rep[String] = column[String]("STATE")
  def zip: Rep[String] = column[String]("ZIP")
  
  // Every table needs a * projection with the same type as the table's type parameter
  def * : ProvenShape[(Int, String, String, String, String, String)] =
    (id, name, street, city, state, zip)
}

// A Coffees table with 5 columns: name, supplier id, price, sales, total
class Coffees(tag: Tag)
  extends Table[(String, Int, Double, Int, Int)](tag, "COFFEES") {

  def name: Rep[String] = column[String]("COF_NAME", O.PrimaryKey)
  def supID: Rep[Int] = column[Int]("SUP_ID")
  def price: Rep[Double] = column[Double]("PRICE")
  def sales: Rep[Int] = column[Int]("SALES")
  def total: Rep[Int] = column[Int]("TOTAL")
  
  def * = (name, supID, price, sales, total)

  // A reified foreign key relation that can be navigated to create a join
  def supplier: ForeignKeyQuery[Suppliers, (Int, String, String, String, String, String)] = 
    foreignKey("SUP_FK", supID, TableQuery[Suppliers])(_.id)
}

object Tables {
  //声明一个类型是tuple元组(tuple不仅仅有两个,可以有多个字段)
  type Person = (Int,String,Int,Int)

  //等价于class People(tag: Tag) extends Table[(Int,String,Int,Int)](tag,"person")
  class People(tag: Tag) extends Table[Person](tag, "PERSON") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def name = column[String]("NAME")
    def age = column[Int]("AGE")
    def addressId = column[Int]("ADDRESS_ID")

    //等价于 def * : ProvenShape[(Int, String Int, Int)] = (id, name, age, addressId) 类型省略后,就变成下面的了
    def * = (id,name,age,addressId)
    def address = foreignKey("ADDRESS",addressId,addresses)(_.id)
  }
  lazy val people = TableQuery[People]

  type Address = (Int,String,String)
  class Addresses(tag: Tag) extends Table[Address](tag, "ADDRESS") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def street = column[String]("STREET")
    def city = column[String]("CITY")
    def * = (id,street,city)
  }
  lazy val addresses = TableQuery[Addresses]
}