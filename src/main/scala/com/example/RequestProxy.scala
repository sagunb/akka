package com.example

import akka.actor.{Actor, ActorLogging, Props}

class RequestProxy extends Actor with ActorLogging {
  import RequestProxy._

  def receive = {
    case m: EventReader.EventMessage =>
      log.info("In RequestProxy - received message: {}", m.toString)
//      sender() ! m
  }
}

object RequestProxy {
  val props = Props[RequestProxy]
}
