package scripts

import commands.ContactCommand._

import scalaz.std.list._
import scalaz.syntax.traverse._
import scalaz.syntax.monadError._

object ContactScript {

  def contactsScript: Script[xml.Elem] = {
    toScript(GetEmailList).flatMap { emailList =>
      emailList.traverseM[Script, xml.Elem] { email =>
        if (email.matches("""[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,6}""")) {
          toScript(GetContactNameByEmail(email)).handleError {
            case e: ContactNotFoundException =>
              "Unknown name".point[Script]
            case passThroughError =>
              passThroughError.raiseError[Script, String]
          }.map { name =>
            List(
              <tr>
                <td>
                  {name}
                </td>
                <td>
                  {email}
                </td>
              </tr>
            )
          }
        } else {
          (Nil: List[xml.Elem]).point[Script]
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
}