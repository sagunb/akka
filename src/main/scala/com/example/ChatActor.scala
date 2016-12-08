package com.example

import akka.actor.{Actor, ActorLogging, FSM, Props}
import scala.concurrent.duration._

class ChatActor extends Actor with FSM[ChatActor.State, ChatActor.Data] with ActorLogging {

  import ChatActor._

  println("Chatbot: Hello, what's your name?")

  startWith(State.Initialize, Data())

  when(State.Initialize, State.Initialize.timeout) {
    case Event(StateTimeout, data) =>
      stay

    case Event(TerminalMessage(msg), data) =>
      println(s"Hi $msg, how can I help you?")
      goto(State.Request) using Data(Some(msg))
  }

  when(State.Request, State.Request.timeout) {
    case Event(StateTimeout, data) =>
      println(s"Hi ${data.userId.get}, how can I help you?")
      stay

    case Event(TerminalMessage(msg), data) =>
      println(s"Have you tried searching our FAQ, ${data.userId.get}?")
      goto(State.Resolution)
  }

  when(State.Resolution, State.Resolution.timeout) {
    case Event(StateTimeout, data) =>
      println(s"Have you tried searching our FAQ, ${data.userId.get}?")
      stay

    case Event(TerminalMessage(msg), data) =>
      msg match {
        case "yes" => println("We'll get a human being to call your number, bye.")
        case "no" => println("You can find our faq here: www.example.com")
        case _ => println("")
      }
      stop()
  }

  initialize()
}

object ChatActor {
  val props = Props[ChatActor]
  case class History(sessionEvents: List[EventReader.EventMessage])

  sealed trait State

  object State {
    object Initialize extends State { val timeout = 100 seconds }
    object Request extends State { val timeout = 200 seconds }
    object Resolution extends State { val timeout = 200 seconds }
  }

  case class Data(userId: Option[String] = None)
}
