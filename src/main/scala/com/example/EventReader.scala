package com.example

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

import scala.io.Source

class EventReader(fileName: String, requestProxy: ActorRef) extends Actor with ActorLogging {
  import EventReader._

  var counter = 0

  val requestPattern = "Request\\((\\d.+),(\\d.+),(.+),(.+),(.+)\\)".r

  def receive = {
    case Initialize =>
      log.info("In EventReader - starting EventReader")
      for (line <- Source.fromFile(fileName).getLines()) {

        val message = line match {
          case requestPattern(sessionId, timestamp, url, referred, browser) =>
            EventMessage(sessionId.toLong, timestamp.toLong, url, referred, browser)
        }
        requestProxy ! message
      }
      requestProxy ! ShutDownMessage("file finished.")
      context.stop(self)

//    case PongActor.PongMessage(text) =>
//      log.info("In PingActor - received message: {}", text)
//      counter += 1
//      if (counter == 3) context.system.shutdown()
//      else sender() ! EventMessage("ping")
  }
}

object EventReader {
//  def props(filePath: String, proxyActor: ActorRef): Props = Props(classOf[EventReader], filePath, proxyActor)
  def props(filePath: String, proxyActor: ActorRef): Props = Props(new EventReader(filePath, proxyActor))
  case object Initialize
  case class EventMessage(sessionId: Long, timestamp: Long, url: String, referrer: String, browser: String)
  case class ShutDownMessage(msg: String)
}