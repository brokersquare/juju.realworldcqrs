package juju.realworld.app

import akka.actor.{Actor, ActorLogging}
import juju.messages.Command
import realworld.domain.Book.CreateBook
import realworld.domain.Manager.CreateManager
import realworld.domain.Order.PlaceOrder
import spray.http.{HttpEntity, MediaTypes}
import spray.httpx.unmarshalling.{FromRequestUnmarshaller, Unmarshaller}
import spray.routing.{HttpService, Route}

import scala.reflect.ClassTag
import scala.reflect.runtime.{universe => ru}

class RealWorldFrontend extends FrontendService {
  implicit def unmarshallerCommand[T <: Command : ClassTag] = CommandService.unmarshallerCommand

  override val backendApiRoute: Route =
    pathPrefix("api") {
      post {
        path("book") {handleCommand[CreateBook]} ~ path("manager") {handleCommand[CreateManager]} ~ path("order") {handleCommand[PlaceOrder]}
      }
    }
}

trait FrontendService extends Actor with ActorLogging with CommandService {
  def actorRefFactory = context

  def receive = runRoute(backendApiRoute)
}

object CommandService {
  implicit def unmarshallerCommand[T <: Command : ClassTag] = Unmarshaller[T](MediaTypes.`application/x-www-form-urlencoded`) {
    case e: HttpEntity.NonEmpty => {
      val u = spray.httpx.unmarshalling.FormDataUnmarshallers.UrlEncodedFormDataUnmarshaller(e)
      u match {
        case Right(data) => {
          val commandType = implicitly[ClassTag[T]].runtimeClass
          val map = data.fields.toMap

          val properties = getCaseClassParameters[T]

          val parameters = properties.map { p =>
            val fieldname = p._1.decodedName.toString
            //TODO:add type check and coversion
            map.getOrElse(fieldname, () => throw new IllegalArgumentException(s"cannot construct command ${commandType.getSimpleName} due to missing parameter $fieldname"))
          }

          val constructor = commandType.getConstructors.head
          val command = constructor.newInstance(parameters: _*)
          command.asInstanceOf[T]
        }
        case Left(ex) => ???
      }
    }
  }

  private def companionMembers(clazzTag: scala.reflect.ClassTag[_]): ru.MemberScope = {
    val runtimeClass = clazzTag.runtimeClass
    val rootMirror = ru.runtimeMirror(runtimeClass.getClassLoader)
    val classSymbol = rootMirror.classSymbol(runtimeClass)
    // get the companion here
    classSymbol.companion.typeSignature.members
  }


  private def getCaseClassParameters[T : ClassTag] =
    companionMembers(scala.reflect.classTag[T])
      .filter { m => m.isMethod && m.name.toString == "apply"}
      .head.asMethod.paramLists.head.map(p => (p.name, p.info)).toSeq

}

trait CommandService extends HttpService {
  val backendApiRoute: Route

  protected def handleCommand[C <: Command : ClassTag : FromRequestUnmarshaller] = {
    entity(as[C]) { command =>
      complete {
        s"received command $command \n"
      }
    }
  }
}
