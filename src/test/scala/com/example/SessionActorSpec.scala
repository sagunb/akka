package com.example

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActor, TestActorRef, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class SessionActorSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("MySpec"))

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A Session actor" must {
    "store session history" in {
      val testProbe = TestProbe()
      val sessionActor = TestActorRef[SessionActor](SessionActor.props(testProbe.ref))

      sessionActor ! EventReader.EventMessage(1L, 1L, "/", "google", "firefox")
      sessionActor ! EventReader.EventMessage(1L, 2L, "/", "google", "firefox")
      sessionActor ! EventReader.Tick(2L)

      assert(sessionActor.underlyingActor.history ==
        collection.mutable.ListBuffer(
          EventReader.EventMessage(1L, 2L, "/", "google", "firefox"),
          EventReader.EventMessage(1L, 1L, "/", "google", "firefox")
        )
      )

    }
  }


}
