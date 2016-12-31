package com.actionfps.accumulation

import java.io.File
import java.net.URL

import com.actionfps.api.Game
import com.actionfps.clans.Clanwars
import com.actionfps.gameparser.enrichers._
import com.actionfps.players.PlayersStats
import com.actionfps.reference.{ClanRecord, NicknameRecord, Registration}
import com.actionfps.stats.Clanstats
import jdk.nashorn.api.scripting.URLReader
import play.api.libs.json.Json

/**
  * Created by me on 14/05/2016.
  */
object AccumulationProfilingTestApp extends App {
  val games = {
    val gf = new File("games.txt~")
    if (!gf.exists()) {
      val gamesTxt = new URL("https://actionfps.com/all/games.txt")
      import sys.process._
      (gamesTxt #> gf).!
    }
    val file = scala.io.Source.fromFile(gf)
    try file.getLines().map(str => Json.fromJson[Game](Json.parse(str)).get).toList
    finally file.close()
  }

  val users = {
    val registrationsUrl = new URL("https://actionfps.com/players/?format=registrations-csv")
    val nicknamesUrl = new URL("https://actionfps.com/players/?format=nicknames-csv")
    val registrations = Registration.parseRecords(new URLReader(registrationsUrl))
    val nicknames = NicknameRecord.parseRecords(new URLReader(nicknamesUrl))
    registrations.flatMap(User.fromRegistration(_, nicknames))
      .map(u => u.id -> u).toMap
  }
  val clans = {
    val clansUrl = new URL("https://actionfps.com/clans/?format=csv")
    ClanRecord.parseRecords(new URLReader(clansUrl)).map(Clan.fromClanRecord).map(c => c.id -> c).toMap
  }
  val fit = FullIterator(
    users = users,
    games = Map.empty,
    clans = clans,
    clanwars = Clanwars.empty,
    clanstats = Clanstats.empty,
    achievementsIterator = AchievementsIterator.empty,
    hof = HOF.empty,
    playersStats = PlayersStats.empty,
    playersStatsOverTime = Map.empty
  )
  val result = fit.includeGames(games)
  result.events.map(_ ("text")).foreach(println)
  println("Done")
  //  println(result)
}

// project accumulation
// set cancelable in Global := true
// set fork in test in run := true
// set javaOptions in test in run ++= Seq("-XX:+UnlockCommercialFeatures","-XX:+FlightRecorder")
// set javaOptions in test in run ++= Seq("-XX:StartFlightRecording=delay=5s,duration=60s,name=myrecording,filename=/Users/me/wat.jfr,settings=profile")
// show test:run::javaOptions
// test:run *AccumulationProfilingTestApp
