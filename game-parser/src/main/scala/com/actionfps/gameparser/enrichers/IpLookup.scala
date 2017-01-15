package com.actionfps.gameparser.enrichers

/**
  * Created by me on 29/05/2016.
  *
  * Used to attach user geographical information based on their IP address.
  */
object IpLookup {

  case class IpLookupResult(countryCode: Option[String], countryName: Option[String],
                            timezone: Option[String])

  object IpLookupResult {
    def empty: IpLookupResult = IpLookupResult(
      countryCode = None,
      countryName = None,
      timezone = None
    )
  }

}

trait IpLookup {
  def lookup(ip: String): IpLookup.IpLookupResult
}
