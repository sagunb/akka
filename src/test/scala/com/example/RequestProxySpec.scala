package com.example

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class RequestProxySpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll  {

  def this() = this(ActorSystem("MySpec"))

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "An Request Proxy actor" must {
    "should store all sessions" in {
      val eventActor = TestActorRef[RequestProxy](RequestProxy.props)
      eventActor ! EventReader.EventMessage(1L, 1L, "/", "google", "firefox")
      eventActor ! EventReader.EventMessage(1L, 10L, "/", "google", "firefox")
      eventActor ! EventReader.EventMessage(2L, 1L, "/", "google", "firefox")
      assert(eventActor.underlyingActor.sessions.keySet == collection.mutable.Set(1L, 2L))
    }
  }

  "An Request Proxy actor" must {
    "send ticks to all session actors" in {
      val testProbe1 = TestProbe()
      val testProbe2 = TestProbe()
      val eventActor = TestActorRef[RequestProxy](RequestProxy.props)

      eventActor.underlyingActor.updateSessions(1L, testProbe1.ref)
      eventActor.underlyingActor.updateSessions(2L, testProbe2.ref)

      eventActor ! EventReader.Tick(10L)

      testProbe1.expectMsg(EventReader.Tick(10L))
      testProbe2.expectMsg(EventReader.Tick(10L))

    }
  }

}
