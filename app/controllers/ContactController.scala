package controllers

import com.google.inject.Inject
import play.api.libs.json.{JsString, JsArray}
import play.api.libs.ws.WSClient
import play.utils.UriEncoding

import commands.ContactCommand
import commands.ContactCommand._
import play.api.mvc.{Action, Controller}
import scripts.ContactScript

import scala.concurrent.{ExecutionContext, Future}
import scalaz._
import scalaz.std.scalaFuture._
import com.thoughtworks.each.Monadic._

class ContactController @Inject()(wsClient: WSClient)(implicit ec: ExecutionContext) extends Controller {

  private def externalServiceUrlPrefix = "http://api-host-name/contact/"

  private def asyncGetEmailList(): Future[Throwable \/ List[String]] = {
    wsClient.url(externalServiceUrlPrefix).get().map { response =>
      val JsArray(jsonSeq) = response.json
      val emailList = (for {
        JsString(element) <- jsonSeq
      } yield element) (collection.breakOut(List.canBuildFrom))
      \/-(emailList)
    }.recover {
      case e: Exception =>
        -\/(e)
    }
  }

  private def asyncGetContactNameByEmail(email: String): Future[Throwable \/ String] = {
    val url = raw"""$externalServiceUrlPrefix${UriEncoding.encodePathSegment(email, "UTF-8")}"""
    wsClient.url(url).get().map { response =>
      \/-(response.body)
    }.recover {
      case e: Exception =>
        -\/(e)
    }
  }

  private def interpreter = new (ContactCommand ~> Future) {
    override def apply[A](fa: ContactCommand[A]): Future[A] = {
      fa match {
        case GetEmailList =>
          asyncGetEmailList()
        case GetContactNameByEmail(email) =>
          asyncGetContactNameByEmail(email)
      }
    }
  }

  /**
    * HTTP handler for /contacts
    */
  def contacts = Action.async {
    ContactScript.contactsScript.run.foldMap(interpreter).map {
      case -\/(e) =>
        InternalServerError(e.toString)
      case \/-(xhtml) =>
        Ok(xhtml)
    }
  }
}