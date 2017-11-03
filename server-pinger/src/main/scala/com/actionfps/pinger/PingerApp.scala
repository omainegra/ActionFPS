package com.actionfps.pinger

import akka.actor.ActorDSL.actor
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import play.api.libs.json.Json

import scala.concurrent.duration._

/**
  * Created by me on 14/01/2017.
  */
object PingerApp extends App {

  val servers: List[(String, Int)] = {
    val source = scala.io.Source.fromURL(ConfigFactory.load().getString("servers.csv"))
    try {
      val lines = source.getLines
      val headings = lines.next().split(",", -1).toList
      val hostnameHeading = headings.indexOf("Hostname")
      val portHeading = headings.indexOf("Port")
      if (hostnameHeading < 0 && portHeading < 0) throw new IllegalArgumentException
      lines.map(_.split(",", -1)).map(c => c(hostnameHeading) -> c(portHeading).toInt).toList
    }
    finally source.close()

    List("aura.woop.ac" -> 7654)
  }


  implicit val actorSystem = ActorSystem()
  private val listenerActor = actor(factory = actorSystem, name = "pinger")(new ListenerActor({
    g => println(Json.toJson(g))
  }, { h =>
    println(Json.toJson(h))
  }))
  import actorSystem.dispatcher

  private val schedule = actorSystem.scheduler.schedule(0.seconds, 5.seconds) {
    servers.foreach { case (hostname, port) =>
      listenerActor ! SendPings(hostname, port)
    }
  }
}



