package scripts

import commands.ContactCommand._

import scalaz.std.list._

import com.thoughtworks.each.Monadic._

object ContactScript {

  def contactsScript: Script[xml.Elem] = throwableMonadic[Script] {
    val emailList = toScript(GetEmailList).each
    <html>
      <body>
        <table>{
          (for {
            email <- emailList.monadicLoop // Converts emailList to a MonadicLoop
            if email.matches("""[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,6}""")
          } yield {
            <tr>
              <td>{
                try {
                  toScript(GetContactNameByEmail(email)).each
                } catch {
                  case _: ContactNotFoundException =>
                    "Unknown name"
                }
              }</td>
              <td>
                {email}
              </td>
            </tr>
          }).underlying // Converts the MonadicLoop returned from for/yield comprehension to a List
        }</table>
      </body>
    </html>
  }

}
