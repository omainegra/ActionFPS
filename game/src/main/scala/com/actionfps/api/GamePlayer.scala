package com.actionfps.api

/**
  * Created by me on 29/05/2016.
  */
case class GamePlayer(name: String,
                      host: Option[String],
                      score: Option[Int],
                      flags: Option[Int],
                      frags: Int,
                      deaths: Int,
                      user: Option[String],
                      clan: Option[String],
                      countryCode: Option[String],
                      countryName: Option[String],
                      timezone: Option[String])
