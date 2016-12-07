package com.example

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props, Terminated}
import org.joda.time.DateTime

class SessionActor(statsActor: ActorRef) extends Actor with ActorLogging {
  import SessionActor._

  val history: collection.mutable.ListBuffer[EventReader.EventMessage] = collection.mutable.ListBuffer()
  val chatActor = context.actorOf(ChatActor.props, "chatActor" + self.path.name)
  context.watch(chatActor)

  def receive = {
    case m @ EventReader.EventMessage(sessionId, timestamp, url, referrer, browser) =>
      log.info("In SessionActor - received message: {}", m.toString)
      if (url == "/help") {
        if ((history.head.url == url) && ((timestamp - history.head.timestamp) > 2000)) {
          chatActor ! RequestHelp(history.head.sessionId)
        }
      }
      history.prepend(m)

    case msg: TerminalMessage =>
      chatActor forward msg

    case t @ EventReader.Tick(epoch) =>
      // assert(history.nonEmpty) // don't need this
      val diff = epoch - history.head.timestamp
      if (diff >= 5000) {
        statsActor ! History(history.toList)
        chatActor ! Terminate
      }

    case s @ EventReader.ShutDownMessage(msg) =>
      log.info("Received terminate message: {}", msg)
      statsActor ! History(history.toList)
      context.stop(self)

    case Terminated(actorRef) =>
      context.stop(self)
  }
}

object SessionActor {
  def props(statsActor: ActorRef): Props = Props(new SessionActor(statsActor))
  case class History(sessionEvents: List[EventReader.EventMessage])
  case class RequestHelp(sessionId: Long)
  case object Terminate
}
