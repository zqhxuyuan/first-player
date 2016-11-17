package controllers

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject._
import io.joaovasques.example.LongPiJob
import net.codingwell.scalaguice.ScalaModule
import org.apache.spark.SparkConf
import play.api.mvc.{Action, Controller}
import play.modules.io.joaovasques.playspark.akkaguice.GuiceAkkaExtension
import play.modules.io.joaovasques.playspark.core.{CoreActor, CoreModule}
import play.modules.io.joaovasques.playspark.persistence.PersistenceModule
import play.modules.io.joaovasques.playspark.spark.SparkMessages._
import play.modules.io.joaovasques.playspark.spark.SparkModule
import play.modules.io.joaovasques.playspark.stats.StatsModule

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

@Singleton
class SubmitSparkJob @Inject() (_system: ActorSystem) extends Controller with Core{

  private final implicit val duration = 30.seconds
  private final implicit val timeout = Timeout(30 seconds)

  //val core = _system.actorOf(GuiceAkkaExtension(_system).props(CoreActor.name))

  def this() = this(ActorSystem("SparkJobSubmissionSpec"))

  override def _sys = _system

  def sparkJob() = Action {
    val conf = new SparkConf()
      .setMaster("local[2]")
      .setAppName("JobSubmissionSpec")

    val futureResult = (core ? new SaveContext(conf)).mapTo[Try[Unit]]
    val result = Await.result(futureResult, duration)

    result match {
      case Success(_) => {
        val request = new StartSparkJob(new LongPiJob(), "", true)
        val futRes = (core ? request).mapTo[SparkJobResult]
        val res = Await.result(futRes, duration)
        res match {
          case c: JobCompleted => println(c.result)
          case f: JobFailed => println(f.error.getMessage())
        }
      }
      case Failure(ex) => println(ex.getMessage())
    }

    Ok("success")
  }

  private def removeContext() = {
    val futureResult = (core ? new StopContext()).mapTo[Try[Unit]]
    Await.result(futureResult, duration)
  }

}


trait AkkaGuiceInjector {
  def _sys: ActorSystem
  var injector: Injector = _

  def initInjector(system: ActorSystem, testModules: Module*) = {
    val modules = List(
      new AbstractModule with ScalaModule {
        override def configure(): Unit = { }
        @Provides
        def provideSystem() = _sys
      }
    ) ++ testModules

    injector = Guice.createInjector(modules.asJava)
    GuiceAkkaExtension(system).initialize(injector)
  }
}

trait Core extends AkkaGuiceInjector {
  initInjector(
    _sys,
    new PersistenceModule(),
    new StatsModule(),
    new SparkModule(),
    new CoreModule()
  )
  val core = _sys.actorOf(GuiceAkkaExtension(_sys).props(CoreActor.name))
}