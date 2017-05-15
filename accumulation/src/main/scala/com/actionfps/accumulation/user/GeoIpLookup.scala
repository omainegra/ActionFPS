package com.actionfps.accumulation.user

import java.io.File

import com.actionfps.gameparser.enrichers.IpLookup
import com.maxmind.geoip.{LookupService, timeZone}

/**
  * Created by me on 10/04/2016.
  */
case class GeoIpLookup(file: File) extends IpLookup {
  val ls = new LookupService(file, LookupService.GEOIP_MEMORY_CACHE)

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
