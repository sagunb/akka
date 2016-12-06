package com.example

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import org.joda.time.DateTime

class SessionActor(statsActor: ActorRef) extends Actor with ActorLogging {
  import SessionActor._

  val history: collection.mutable.ListBuffer[EventReader.EventMessage] = collection.mutable.ListBuffer()

  def receive = {
    case m @ EventReader.EventMessage(sessionId, timestamp, url, referrer, browser) =>
      log.info("In SessionActor - received message: {}", m.toString)
      history.prepend(m)

    case t @ EventReader.Tick(epoch) =>
      // assert(history.nonEmpty) // don't need this
      val diff = epoch - history.head.timestamp
      if (diff >= 50) {
        statsActor ! History(history.toList)
        context.stop(self)
      }

    case s @ EventReader.ShutDownMessage(msg) =>
      log.info("Received terminate message: {}", msg)
      statsActor ! History(history.toList)
      context.stop(self)

  }
}

object SessionActor {
  def props(statsActor: ActorRef): Props = Props(new SessionActor(statsActor))
  case class History(sessionEvents: List[EventReader.EventMessage])
}
