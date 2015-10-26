package controllers

import play.api.mvc.{Action, Controller}

class ContactController extends Controller {

  import services.ContactService._

  /**
   * All the business logic of this application
   */
  private def contactsXhtml: xml.Elem = {
    val emailList = getEmailList()
    <html>
      <body>
        <table>{
          for {
            email <- emailList
            if email.matches( """[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,6}""")
          } yield {
            <tr>
              <td>
                {getContactNameByEmail(email)}
              </td>
              <td>
                {email}
              </td>
            </tr>
          }
        }</table>
      </body>
    </html>
  }

  /**
   * HTTP handler for /contacts
   */
  def contacts = Action {
    Ok(contactsXhtml)
  }
}