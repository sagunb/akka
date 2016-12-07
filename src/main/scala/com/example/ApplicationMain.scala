package com.example

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout

import scala.io.StdIn
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object ApplicationMain {

  implicit val timeout: Timeout = 1 second

  val system = ActorSystem("MyActorSystem")
  val statsActor = system.actorOf(StatsActor.props, "statsActor")
  val requestProxy = system.actorOf(RequestProxy.props(statsActor), "requestProxyActor")
  val eventReader = system.actorOf(EventReader.props("/Users/sagun/Downloads/exercises/events-1k.txt", requestProxy), "eventReaderActor")

  def main(args: Array[String]) {
    eventReader ! EventReader.Read
    commandLoop()
  }

  def commandLoop(): Unit = {
    println("Enter a command: [openSessions|completedSessions|events|exit]")

    StdIn.readLine() match {
      case "openSessions" =>
        val numSessions = requestProxy ? TerminalCommand.OpenSessions
        numSessions.onComplete {
          case Success(value) => println(s"Sessions active: $value"); commandLoop()
          case Failure(e) => println(s"Unable to retrieve sessions due to: $e"); commandLoop()
        }

      case "completedSessions" =>
        val numSessions = statsActor ? TerminalCommand.CompletedSessions
        numSessions.onComplete {
          case Success(value) => println(s"Sessions completed: $value"); commandLoop()
          case Failure(e) => println(s"Unable to retrieve sessions due to: $e"); commandLoop()
        }

      case "events" =>
        val numSessions = statsActor ? TerminalCommand.Events
        numSessions.foreach(sessions => println(s"Num events: $sessions"))
        commandLoop()

      case "exit" =>
        system.shutdown()

      case msg: String =>
        requestProxy ! TerminalMessage(msg)
        commandLoop()
    }

  }

}

object TerminalCommand {
  case object CompletedSessions
  case object OpenSessions
  case object Events
}

case class TerminalMessage(msg: String)
