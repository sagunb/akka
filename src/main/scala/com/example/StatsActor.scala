package com.example

import akka.actor.{Actor, ActorLogging, Props}

class StatsActor extends Actor with ActorLogging {
  import StatsActor._

  val totalHistory: collection.mutable.ListBuffer[EventReader.EventMessage] = collection.mutable.ListBuffer()

  def receive = {
    case SessionActor.History(sessionEvents) =>
      log.info("getting messages to statsActor")
      totalHistory.prepend(sessionEvents:_*)

  }
}

object StatsActor {
  val props = Props[StatsActor]
}
