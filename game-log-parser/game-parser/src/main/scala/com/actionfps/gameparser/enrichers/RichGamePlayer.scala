package com.actionfps.gameparser.enrichers

/**
  * Created by me on 29/05/2016.
  */
class RichGamePlayer(gamePlayer: JsonGamePlayer) {

  import gamePlayer._

  def addIpLookupResult(ipLookup: IpLookup.IpLookupResult): JsonGamePlayer = {
    gamePlayer.copy(
      countryCode = ipLookup.countryCode orElse countryCode,
      countryName = ipLookup.countryName orElse countryName,
      timezone = ipLookup.timezone orElse timezone
    )
  }

  def withCountry(implicit lookup: IpLookup): JsonGamePlayer = {
    host.map(lookup.lookup).map(this.addIpLookupResult).getOrElse(gamePlayer)
  }
}
