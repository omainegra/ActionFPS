package com.actionfps.accumulation

import java.io.File

import com.actionfps.gameparser.enrichers.IpLookup
import com.maxmind.geoip.{timeZone, LookupService}

/**
  * Created by me on 10/04/2016.
  */
object GeoIpLookup extends IpLookup {
  lazy val file = {
    val A = new File("resources/GeoLiteCityv6.dat")
    val B = new File("web/resources/GeoLiteCityv6.dat")
    val C = new File("../resources/GeoLiteCityv6.dat")
    if (B.exists()) B else if (C.exists()) C else A
  }

  lazy val ls = new LookupService(file, LookupService.GEOIP_MEMORY_CACHE)

  def lookup(ip: String): IpLookup.IpLookupResult = {
    Option(ls.getLocationV6(ip)) match {
      case None => IpLookup.IpLookupResult.empty
      case Some(loc) =>
        IpLookup.IpLookupResult(
          countryCode = Option(loc.countryCode),
          countryName = Option(loc.countryName),
          timezone = Option(timeZone.timeZoneByCountryAndRegion(loc.countryCode, loc.region))
        )
    }
  }

}
