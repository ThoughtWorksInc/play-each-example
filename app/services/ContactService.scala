package services

import play.api.libs.json.{JsString, JsArray}
import play.api.libs.ws.WS
import play.api.Play.current
import play.utils.UriEncoding

import scala.concurrent.{ExecutionContext, Future}

object ContactService {

  final val ExternalServiceUrlPrefix = "http://api-host-name/contact/"

  /**
    * Fetches list of email of all contacts from a remote service.
    */
  def asyncGetEmailList()(implicit ec: ExecutionContext): Future[List[String]] = {
    WS.url(ExternalServiceUrlPrefix).get().map { response =>
      val JsArray(jsonSeq) = response.json
      (for {
        JsString(element) <- jsonSeq
      } yield element) (collection.breakOut(List.canBuildFrom))
    }
  }

  /**
    * Query the contact full name that corresponds to an email from a remote service.
    */
  def asyncGetContactNameByEmail(email: String)(implicit ec: ExecutionContext): Future[String] = {
    val url = raw"""$ExternalServiceUrlPrefix${UriEncoding.encodePathSegment(email, "UTF-8")}"""
    WS.url(url).get().map { response =>
      response.body
    }
  }
}
