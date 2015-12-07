package controllers

import play.api.mvc.{Action, Controller}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ContactController extends Controller {

  import services.ContactService._

  /**
   * All the business logic of this application
   */
  private def asyncContactsXhtml: Future[xml.Elem] = {
    asyncGetEmailList().flatMap { emailList =>
      emailList.foldLeft(Future.successful(Seq.empty[xml.Elem])) { (headFuture, email) =>
        if (email.matches( """[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,6}""")) {
          headFuture.flatMap { head =>
            asyncGetContactNameByEmail(email).map { name =>
              head :+
                <tr>
                  <td>
                    {name}
                  </td>
                  <td>
                    {email}
                  </td>
                </tr>
            }
          }
        } else {
          headFuture
        }
      }.map { trs =>
        <html>
          <body>
            <table>
              {trs}
            </table>
          </body>
        </html>
      }
    }
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