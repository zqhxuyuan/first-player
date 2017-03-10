package actor
import akka.actor._

//companion object
object HelloActor {
  //Defines a props method on its companion object that returns the props for creating it
  def props = Props[HelloActor]

  //The messages it sends/receives, or its protocol, are defined on its companion object
  case class SayHello(name: String)
}

class HelloActor extends Actor {
  import HelloActor._

  def receive = {
    case SayHello(name: String) =>
      Thread.sleep(3000)
      sender() ! "Hello, " + name 
  }
}
