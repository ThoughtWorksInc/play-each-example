package scripts

import commands.ContactCommand
import commands.ContactCommand.{ContactNotFoundException, GetContactNameByEmail, GetEmailList}
import org.junit.{Assert, Test}
import play.api.libs.json.{JsString, JsArray}
import play.utils.UriEncoding

import scala.concurrent.Future
import scalaz._
import scalaz.Id.Id

class ContactScriptTest {

  @Test
  def testContactScript(): Unit = {

    val interpreter = new (ContactCommand ~> Id) {
      private val data = Map(
        "atryyang@thoughtworks.com" -> "Yang Bo",
        "invalid-mail-address" -> "N/A",
        "john.smith@gmail.com" -> "John Smith"
      )
      override def apply[A](fa: ContactCommand[A]): A = {
        fa match {
          case GetEmailList =>
            \/-(data.keys.toList)
          case GetContactNameByEmail(email) =>
            \/-(data(email))
        }
      }
    }

    val \/-(xhtml) = ContactScript.contactsScript.run.foldMap(interpreter)

    Assert.assertEquals("html", xhtml.label)
    val rows = (xhtml \ "body" \ "table" \ "tr")
    Assert.assertEquals(2, rows.length)

    Assert.assertEquals("Yang Bo", (rows(0) \ "td")(0).text.trim)
    Assert.assertEquals("atryyang@thoughtworks.com", (rows(0) \ "td")(1).text.trim)

    Assert.assertEquals("John Smith", (rows(1) \ "td")(0).text.trim)
    Assert.assertEquals("john.smith@gmail.com", (rows(1) \ "td")(1).text.trim)
  }


  @Test
  def testFallbackTextForContactNotFoundException(): Unit = {
    val interpreter = new (ContactCommand ~> Id) {
      override def apply[A](fa: ContactCommand[A]): A = {
        fa match {
          case GetEmailList =>
            \/-(List("unknown_user@host.com", "known_user@host.com"))
          case GetContactNameByEmail("unknown_user@host.com") =>
            -\/(new ContactNotFoundException)
          case GetContactNameByEmail("known_user@host.com") =>
            \/-("Known User")
        }
      }
    }

    val \/-(xhtml) = ContactScript.contactsScript.run.foldMap(interpreter)

    Assert.assertEquals("html", xhtml.label)
    val rows = (xhtml \ "body" \ "table" \ "tr")
    Assert.assertEquals(2, rows.length)

    Assert.assertEquals("Unknown name", (rows(0) \ "td")(0).text.trim)
    Assert.assertEquals("unknown_user@host.com", (rows(0) \ "td")(1).text.trim)

    Assert.assertEquals("Known User", (rows(1) \ "td")(0).text.trim)
    Assert.assertEquals("known_user@host.com", (rows(1) \ "td")(1).text.trim)
  }

  @Test
  def testPassThroughOtherException(): Unit = {

    object OtherException extends Exception

    val interpreter = new (ContactCommand ~> Id) {
      override def apply[A](fa: ContactCommand[A]): A = {
        fa match {
          case GetEmailList =>
            \/-(List("other_exception@host.com"))
          case GetContactNameByEmail("other_exception@host.com") =>
            -\/(OtherException)
        }
      }
    }

    Assert.assertEquals(-\/(OtherException), ContactScript.contactsScript.run.foldMap(interpreter))
  }

}
