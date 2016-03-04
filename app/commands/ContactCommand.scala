package commands

import scalaz.{\/, EitherT, Free}

sealed trait ContactCommand[+A]

object ContactCommand {

  class ContactNotFoundException(message: String = null, cause: Throwable = null) extends Exception(message, cause) {
    def this(cause: Throwable) = this(cause.toString, cause)
  }

  final case object GetEmailList extends ContactCommand[Throwable \/ List[String]]

  final case class GetContactNameByEmail(email: String) extends ContactCommand[Throwable \/ String]

  type RawScript[A] = Free[ContactCommand, A]

  type Script[A] = EitherT[RawScript, Throwable, A]

  def toScript[A](command: ContactCommand[Throwable \/ A]): Script[A] = {
    new EitherT[RawScript, Throwable, A](Free.liftF(command))
  }

}
