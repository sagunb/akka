package com.example

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout

import scala.annotation.tailrec
import scala.io.StdIn
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

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

  @tailrec
  def commandLoop(): Unit = {
    println("Enter a command: [sessions|events|exit]")

    StdIn.readLine() match {
      case "sessions" =>
        val numSessions = requestProxy ? TerminalCommand.Sessions
        numSessions.foreach(sessions => println(s"Num sessions: $sessions"))
        commandLoop()

      case "events" =>
        val numSessions = requestProxy ? TerminalCommand.Events
        numSessions.foreach(sessions => println(s"Num events: $sessions"))
        commandLoop()

      case "exit" =>
        system.shutdown()

      case _ => println("Invalid command.")
        commandLoop()
    }

  }

}

object TerminalCommand {
  case object Sessions
  case object Events
}