package com.example

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}

class RequestProxy(statsActor: ActorRef) extends Actor with ActorLogging {
  import RequestProxy._

  val sessions: collection.mutable.HashMap[Long, ActorRef] = collection.mutable.HashMap()
  val actorRefs: collection.mutable.HashMap[ActorRef, Long] = collection.mutable.HashMap()

  def updateSessions(sessionId: Long, actorRef: ActorRef): Unit = {
    sessions.put(sessionId, actorRef)
    actorRefs.put(actorRef, sessionId)
  }

  def deleteSession(sessionId: Long): Unit = {
    if (sessions.contains(sessionId)) {
      val actorRef = sessions(sessionId)
      sessions.remove(sessionId)
      actorRefs.remove(actorRef)
    }
  }

  def deleteActorRef(actorRef: ActorRef): Unit = {
    if (actorRefs.contains(actorRef)) {
      val sessionId = actorRefs(actorRef)
      sessions.remove(sessionId)
      actorRefs.remove(actorRef)
    }
  }

  def receive = {
    case m @ EventReader.EventMessage(sessionId, timestamp, url, referrer, browser) =>
      log.info("In RequestProxy - received message: {}", m.toString)
      if (!sessions.contains(sessionId)) {
        val sessionActor = context.actorOf(SessionActor.props(statsActor), "sessionActor" + sessionId.toString)
        context.watch(sessionActor)
        updateSessions(sessionId, sessionActor)
        log.info("Detected new session: {}", sessionId.toString)
      }
      sessions(sessionId) ! m

    case t @ EventReader.Tick(timestamp) =>
      for (actor <- actorRefs.keys) {
        log.info("In RequestProxy - recieved tick: {}", t.toString)
        actor ! t
      }

    case s @ EventReader.ShutDownMessage(msg) =>
      log.info("In RequestProxy - recieved shutdown message: {}", s.toString)
      for (actor <- actorRefs.keys) {
        actor ! s
      }

    case u @ Terminated(actorRef) =>
      log.info("Child actor {} was terminated", actorRef.toString())
      deleteActorRef(actorRef)
      if (actorRefs.isEmpty) {
        log.info("All children have teriminated. Terminating request proxy.")
        context.stop(self)
      }

  }
}

object RequestProxy {
  def props(actorRef: ActorRef) = Props(new RequestProxy(actorRef))
}
