package com.example

import akka.actor.{Actor, ActorLogging, Props}

class RequestProxy extends Actor with ActorLogging {
  import RequestProxy._

  def receive = {
    case m: EventReader.EventMessage =>
      log.info("In RequestProxy - received message: {}", m.toString)
//      sender() ! m

    case EventReader.ShutDownMessage(msg) =>
      log.info("In RequestProxy - received terminate message: {}", msg)
      context.stop(self)

  }
}

object RequestProxy {
  val props = Props[RequestProxy]
  case class FinishedMessage(msg: String)
}
