package com.example

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

class RequestProxy extends Actor with ActorLogging {
  import RequestProxy._

  var sessions: collection.mutable.HashMap[Long, ActorRef] = collection.mutable.HashMap()

  def receive = {
    case m @ EventReader.EventMessage(sessionId, timestamp, url, referrer, browser) =>
      log.info("In RequestProxy - received message: {}", m.toString)
      if (!sessions.contains(sessionId)) {
        val sessionActor = context.actorOf(SessionActor.props, "sessionActor" + sessionId.toString)
        log.info("Detected new session: {}", sessionId.toString)
        sessions.put(sessionId, sessionActor)
      }
      sessions(sessionId) ! m

    case t @ EventReader.Tick(timestamp) =>
      for (session <- sessions.values) {
        log.info("In RequestProxy - recieved tick: {}", t.toString)
        session ! t
      }

    case EventReader.ShutDownMessage(msg) =>
      log.info("In RequestProxy - received terminate message: {}", msg)
      context.stop(self)

  }
}

object RequestProxy {
  val props = Props[RequestProxy]
}
