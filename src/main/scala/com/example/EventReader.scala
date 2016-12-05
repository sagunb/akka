package com.example

import akka.actor.{Actor, ActorLogging, Props}

import scala.io.Source

class EventReader(fileName: String) extends Actor with ActorLogging {
  import EventReader._

  var counter = 0
//  val pongActor = context.actorOf(PongActor.props, "pongActor")

  val requestPattern = "Request\\((\\d.+),(\\d.+),(.+),(.+),(.+)\\)".r

  def receive = {
    case Initialize =>
      log.info("In EventReader - starting EventReader")
      for (line <- Source.fromFile(fileName).getLines()) {

        val message = line match {
          case requestPattern(sessionId, timestamp, url, referred, browser) =>
            EventMessage(sessionId.toLong, timestamp.toLong, url, referred, browser)
        }

        log.info(message.toString)
//        sender ! message
      }

//    case PongActor.PongMessage(text) =>
//      log.info("In PingActor - received message: {}", text)
//      counter += 1
//      if (counter == 3) context.system.shutdown()
//      else sender() ! EventMessage("ping")
  }
}

object EventReader {
  def props(filePath: String): Props = Props(classOf[EventReader], filePath)
  case object Initialize
  case class EventMessage(sessionId: Long, timestamp: Long, url: String, referrer: String, browser: String)
}