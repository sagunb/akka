package com.example

import akka.actor.ActorSystem

object ApplicationMain extends App {
  val system = ActorSystem("MyActorSystem")
  //  val pingActor = system.actorOf(PingActor.props, "pingActor")
  val statsActor = system.actorOf(StatsActor.props, "statsActor")
  val requestProxy = system.actorOf(RequestProxy.props(statsActor), "requestProxyActor")
  val eventReader = system.actorOf(EventReader.props("/Users/sagun/Downloads/exercises/events-1k.txt", requestProxy), "eventReaderActor")

  eventReader ! EventReader.Initialize

//  pingActor ! PingActor.Initialize
  // This example app will ping pong 3 times and thereafter terminate the ActorSystem - 
  // see counter logic in PingActor
  system.awaitTermination()
}