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
    case msg @ EventReader.EventMessage(sessionId, timestamp, url, referrer, browser) =>
      if (!sessions.contains(sessionId)) {
        val sessionActor = context.actorOf(SessionActor.props(statsActor), "sessionActor" + sessionId.toString)
        context.watch(sessionActor)
        updateSessions(sessionId, sessionActor)
      }
      sessions(sessionId) forward msg

    case tick: EventReader.Tick =>
      for (actor <- actorRefs.keysIterator) actor forward tick

    case shutDown: EventReader.ShutDownMessage =>
      for (actor <- actorRefs.keysIterator) actor forward shutDown
      context.become(terminate)

    case Terminated(actorRef) =>
      deleteActorRef(actorRef)
  }

  def terminate: Receive = {
    case u @ Terminated(actorRef) =>
      deleteActorRef(actorRef)
      if (actorRefs.isEmpty) {
        statsActor forward GenerateReport("All sessions terminated")
        context.stop(self)
      }
  }

}

object RequestProxy {
  def props(actorRef: ActorRef) = Props(new RequestProxy(actorRef))
  case class GenerateReport(msg: String)
}
