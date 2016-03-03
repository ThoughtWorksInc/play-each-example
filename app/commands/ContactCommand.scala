package commands

import scalaz.Free

sealed trait ContactCommand[A]

object ContactCommand {

  final case object GetEmailList extends ContactCommand[List[String]]

  final case class GetContactNameByEmail(email: String) extends ContactCommand[String]

  type Script[A] = Free[ContactCommand, A]

  def toScript[A](command: ContactCommand[A]): Script[A] = {
    Free.liftF(command)
  }

}
