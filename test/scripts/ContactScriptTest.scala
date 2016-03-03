package scripts

import commands.ContactCommand
import commands.ContactCommand.{GetContactNameByEmail, GetEmailList}
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
            data.keys.toList
          case GetContactNameByEmail(email) =>
            data(email)
        }
      }
    }

    val xhtml = ContactScript.contactsScript.foldMap(interpreter)
    
    Assert.assertEquals("html", xhtml.label)
    val rows = (xhtml \ "body" \ "table" \ "tr")
    Assert.assertEquals(2, rows.length)

    // Text in first cell in first row should be "Yang Bo"
    Assert.assertEquals("Yang Bo", (rows(0) \ "td")(0).text.trim)

    // Text in second cell in first row should be "atryyang@thoughtworks.com"
    Assert.assertEquals("atryyang@thoughtworks.com", (rows(0) \ "td")(1).text.trim)

    // Text in first cell in second row should be "John Smith"
    Assert.assertEquals("John Smith", (rows(1) \ "td")(0).text.trim)

    // Text in second cell in second row should be "john.smith@gmail.com"
    Assert.assertEquals("john.smith@gmail.com", (rows(1) \ "td")(1).text.trim)
  }

}
