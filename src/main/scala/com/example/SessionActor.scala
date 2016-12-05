package com.example

import akka.actor.{Actor, ActorLogging, Props}
import org.joda.time.DateTime

class SessionActor extends Actor with ActorLogging {
  import SessionActor._

  val history: collection.mutable.ListBuffer[EventReader.EventMessage] = collection.mutable.ListBuffer()

  def receive = {
    case m @ EventReader.EventMessage(sessionId, timestamp, url, referrer, browser) =>
      log.info("In SessionActor - received message: {}", m.toString)
      history.prepend(m)

    case t @ EventReader.Tick(epoch) =>
      // assert(history.nonEmpty) // don't need this
      val diff = epoch - history.head.timestamp
      if (diff >= 5000) {
        log.info("Session dead")
      }

    case s @ EventReader.ShutDownMessage(msg) =>
      log.info("Received terminate message: {}", msg)
      context.stop(self)

  }
}

object SessionActor {
  val props = Props[SessionActor]
}
