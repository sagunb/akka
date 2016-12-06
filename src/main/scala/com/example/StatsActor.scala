package com.example

import java.io.{File, PrintWriter}

import akka.actor.{Actor, ActorLogging, Props}

class StatsActor extends Actor with ActorLogging {
  import StatsActor._

  val referrers: collection.mutable.HashMap[String, Long] = collection.mutable.HashMap()
  val minuteCounts: collection.mutable.HashMap[Long, Long] = collection.mutable.HashMap()
  val urlCounts: collection.mutable.HashMap[String, Long] = collection.mutable.HashMap()
  val urlTimeSpans: collection.mutable.HashMap[String, List[Long]] = collection.mutable.HashMap()
  val browserCounts: collection.mutable.HashMap[String, Long] = collection.mutable.HashMap()
  var numSessions = 0
  var numEvents = 0

  val statsFile = "stats.log"

  def receive = {
    case SessionActor.History(sessionEvents) =>
      log.info("getting messages to statsActor")
      numSessions += 1
      updateStats(sessionEvents)

    case RequestProxy.GenerateReport(msg) =>
      log.info("Generate report requested: {}", msg)
      generateReport()

  }

  private def updateStats(sessionEvents: List[EventReader.EventMessage]): Unit = {
    for (e <- sessionEvents) {
      numEvents += 1
      urlCounts.update(e.url, urlCounts.getOrElse(e.url, 0L) + 1L)
      browserCounts.update(e.browser, browserCounts.getOrElse(e.browser, 0L) + 1L)
      referrers.update(e.referrer, referrers.getOrElse(e.referrer, 0L) + 1L)

      // TODO: fix minute computation
      val minute = e.timestamp % 60000
      minuteCounts.update(minute, minuteCounts.getOrElse(minute, 0L) + 1L)
    }
  }

  private def generateReport(): Unit = {
    val pw = new PrintWriter(new File(statsFile))
    pw.write(s"Total number of sessions: $numSessions\n")
    pw.write(s"Total number of events: $numEvents\n")
    pw.write("\n----\n")

    val topLandingPages = urlCounts.toSeq.sortBy(-_._2).take(2)
    val topBrowsers = browserCounts.toSeq.sortBy(-_._2).take(3)
    val topReferrers = referrers.toSeq.sortBy(-_._2).take(3)

    pw.write(s"Top referrer counts:\n")
    for ((referrer, count) <- topReferrers) pw.write(s"$referrer: $count\n")
    pw.write("\n----\n")

    pw.write(s"Top browser counts:\n")
    for ((browser, count) <- topBrowsers) pw.write(s"$browser: $count\n")
    pw.write("\n----\n")

    pw.write(s"Top landing page counts:\n")
    for ((url, count) <- topLandingPages) pw.write(s"$url: $count\n")
    pw.write("\n----\n")

    pw.write(s"Referrer counts:\n")
    for ((referrer, count) <- referrers) pw.write(s"$referrer: $count\n")
    pw.write("\n----\n")

    pw.write(s"URL counts:\n")
    for ((url, count) <- urlCounts) pw.write(s"$url: $count\n")
    pw.write("\n----\n")

    pw.write(s"Browser counts:\n")
    for ((browser, count) <- browserCounts) pw.write(s"$browser: $count\n")
    pw.write("\n----\n")

    pw.write(s"QPM by minute of day:\n")
    for ((minute, count) <- minuteCounts) pw.write(s"$minute: $count\n")
    pw.write("\n----\n")

    pw.close()
  }

}

object StatsActor {
  val props = Props[StatsActor]
}
