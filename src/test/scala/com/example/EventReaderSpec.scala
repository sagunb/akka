package com.example

import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.Props
import akka.testkit.{ImplicitSender, TestActors, TestKit, TestProbe}
import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll

class EventReaderSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("MySpec"))

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "An Event Reader actor" must {
    "send all messages in test file" in {
      val testActor = TestProbe()
      val eventActor = system.actorOf(EventReader.props("/Users/sagun/Downloads/exercises/test.txt", testActor.ref))
      eventActor ! EventReader.Initialize
      testActor.expectMsg(EventReader.Tick(1480534410848L))
      testActor.expectMsg(EventReader.EventMessage(795253081L, 1480534410848L, "/", "google", "firefox"))
      testActor.expectMsg(EventReader.EventMessage(795253081L, 1480534410858L, "/", "google", "firefox"))
      testActor.expectMsg(EventReader.Tick(1480534410955L))
      testActor.expectMsg(EventReader.EventMessage(795253081L, 1480534410955L, "/", "google", "firefox"))
      testActor.expectMsg(EventReader.ShutDownMessage("file finished."))
    }
  }


}
