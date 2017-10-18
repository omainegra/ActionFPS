package com.actionfps.accumulation.user

import java.io.File

import com.actionfps.gameparser.enrichers.IpLookup
import com.maxmind.geoip.{LookupService, timeZone}

/**
  * Created by me on 10/04/2016.
  */
object GeoIpLookup extends IpLookup {
  lazy val file: File = {
    val A = new File("resources/GeoLiteCityv6.dat")
    val B = new File("web/resources/GeoLiteCityv6.dat")
    val C = new File("../resources/GeoLiteCityv6.dat")
    val D = new File("geoip-resources/GeoLiteCityv6.dat")
    val E = new File("../geoip-resources/GeoLiteCityv6.dat")
    val F = Option(System.getProperty("geolitecity.dat")).map(f => new File(f))
    try {
      (List(A, B, C, D, E) ++ F).filter(_.exists()).head
    } catch {
      case e: Throwable =>
        throw new IllegalArgumentException(
          s"Coult not find GeoLiteCityv6.dat. Might want to set System property 'geolitecity.dat'")
    }
  }

  lazy val ls = new LookupService(file, LookupService.GEOIP_MEMORY_CACHE)

  def lookup(ip: String): IpLookup.IpLookupResult = {
    Option(ls.getLocationV6(ip)) match {
      case None => IpLookup.IpLookupResult.empty
      case Some(loc) =>
        IpLookup.IpLookupResult(
          countryCode = Option(loc.countryCode),
          countryName = Option(loc.countryName),
          timezone = Option(
            timeZone.timeZoneByCountryAndRegion(loc.countryCode, loc.region))
        )
    }
  }

}
