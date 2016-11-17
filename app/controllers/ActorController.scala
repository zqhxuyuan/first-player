package controllers

import javax.inject.{Inject, Singleton}

import actor.HelloActor
import actor.HelloActor.SayHello
import akka.actor.ActorSystem
import akka.util.Timeout
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}

/**
  * Created by zhengqh on 16/11/17.
  *
  * ActorController必须设置为单例模式,ActorSystem.actorOf会创建一个新的HelloActor
  * 创建完Actor会将引用存储到ActorController上. 如果没有设置为单例,那么每次创建Controller时都会创建一个新的Actor
  * 但是不允许在同一ActorSystem中有两个相同名字的Actor(这里的名字为:hello-actor)
  *
  */
@Singleton
class ActorController @Inject() (system: ActorSystem) extends Controller {

  val helloActor = system.actorOf(HelloActor.props, "hello-actor")

  //同步
  def sayHello1(name: String) = Action {
    helloActor ! SayHello(name)
    Ok("Ok...")
  }

  import play.api.libs.concurrent.Execution.Implicits.defaultContext
  import scala.concurrent.duration._
  import akka.pattern.ask //the ask pattern must have a timeout
  implicit val timeout = Timeout(5.seconds) //define implicit timeout

  //异步
  def sayHello(name: String) = Action.async {
    (helloActor ? SayHello(name)).mapTo[String].map { message =>
      Ok(message)
    }
  }

}
