package com.example

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

import scala.io.Source

class EventReader(fileName: String, requestProxy: ActorRef) extends Actor with ActorLogging {
  import EventReader._

  var counter = 0

  val requestPattern = "Request\\((\\d.+),(\\d.+),(.+),(.+),(.+)\\)".r

  def receive = {
    case Read =>
      log.info("In EventReader - starting EventReader")
      var currentEpoch = 0L
      for (line <- Source.fromFile(fileName).getLines()) {
        val message = line match {
          case requestPattern(sessionId, timestamp, url, referred, browser) =>
            if (timestamp.toLong - currentEpoch > 100) {
              currentEpoch = timestamp.toLong
              requestProxy ! Tick(currentEpoch)
            }
            Thread.sleep(100)
            EventMessage(sessionId.toLong, timestamp.toLong, url, referred, browser)
        }
        requestProxy ! message
      }
      requestProxy ! ShutDownMessage("file finished.")
      context.stop(self)
  }
}

object EventReader {
//  def props(filePath: String, proxyActor: ActorRef): Props = Props(classOf[EventReader], filePath, proxyActor)
  def props(filePath: String, proxyActor: ActorRef): Props = Props(new EventReader(filePath, proxyActor))
  case object Read
  case class EventMessage(sessionId: Long, timestamp: Long, url: String, referrer: String, browser: String)
  case class ShutDownMessage(msg: String)
  case class Tick(epoch: Long)

}