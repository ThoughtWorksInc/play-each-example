package services

import play.api.libs.json.{JsString, JsArray}
import play.api.libs.ws.WS
import play.api.Play.current
import play.utils.UriEncoding

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object ContactService {

  final val ExternalServiceUrlPrefix = "http://api-host-name/contact/"

  /**
    * Fetches list of email of all contacts from a remote service.
    */
  def getEmailList(): List[String] = {
    val JsArray(jsonSeq) = Await.result(WS.url(ExternalServiceUrlPrefix).get(), Duration.Inf).json
    (for {
      JsString(element) <- jsonSeq
    } yield element) (collection.breakOut(List.canBuildFrom))
  }

  /**
    * Query the contact full name that corresponds to an email from a remote service.
    */
  def getContactNameByEmail(email: String): String = {
    val url = raw"""$ExternalServiceUrlPrefix${UriEncoding.encodePathSegment(email, "UTF-8")}"""
    Await.result(WS.url(url).get(), Duration.Inf).body
  }

}
