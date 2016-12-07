package com.example

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

class ChatActor extends Actor with ActorLogging {
  import ChatActor._

  def receive = {
    case SessionActor.RequestHelp(sessionId) =>
      println(s"Starting chat with $sessionId")

    case SessionActor.Terminate =>
      context.stop(self)
  }
}

object ChatActor {
  val props = Props[ChatActor]
  case class History(sessionEvents: List[EventReader.EventMessage])
}
