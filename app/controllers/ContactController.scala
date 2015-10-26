package controllers

import play.api.mvc.{Action, Controller}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import com.thoughtworks.each.Monadic._
import scalaz.std.list._
import scalaz.std.scalaFuture._

class ContactController extends Controller {

  import services.ContactService._

  /**
   * All the business logic of this application
   */
  private def asyncContactsXhtml: Future[xml.Elem] = monadic[Future] {
    val emailList = asyncGetEmailList().each
    <html>
      <body>
        <table>{
          (for {
            email <- emailList.monadicLoop // Converts emailList to a MonadicLoop
            if email.matches("""[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,6}""")
          } yield {
            <tr>
              <td>
                {asyncGetContactNameByEmail(email).each}
              </td>
              <td>
                {email}
              </td>
            </tr>
          }).underlying // Converts the MonadicLoop returned from for/yield comprehension to a List
        }</table>
      </body>
    </html>
  }

  /**
   * HTTP handler for /contacts
   */
  def contacts = Action.async {
    asyncContactsXhtml.map { xhtml =>
      Ok(xhtml)
    }
  }
}