package com.example

import akka.actor.{Actor, ActorLogging, Props}
import org.joda.time.DateTime

class SessionActor extends Actor with ActorLogging {
  import SessionActor._

  var history: collection.mutable.ListBuffer[EventReader.EventMessage] = collection.mutable.ListBuffer()

  def receive = {
    case m @ EventReader.EventMessage(sessionId, timestamp, url, referrer, browser) =>
      log.info("In SessionActor - received message: {}", m.toString)
      history = m +: history

    case t @ EventReader.Tick(epoch) =>
      // assert(history.nonEmpty) // don't need this
      val diff = epoch - history.head.timestamp
      if (diff >= 5000) {
        log.info("Session dead")
      }
  }
}

object SessionActor {
  val props = Props[SessionActor]
}
