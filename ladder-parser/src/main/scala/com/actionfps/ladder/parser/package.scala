package com.actionfps.ladder

/**
  * Created by me on 02/05/2016.
  */
package object parser {

  private[ladder] val killWords = Set("busted",
                                      "picked off",
                                      "peppered",
                                      "sprayed",
                                      "punctured",
                                      "shredded",
                                      "busted",
                                      "busted")
  private[ladder] val gibWords =
    Set("slashed", "splattered", "headshot", "gibbed")

  val validServers = Set(
    "192.184.63.69 califa AssaultCube[califapublic]",
    "192.184.63.69 califa AssaultCube[local#28763]",
    "45.34.167.87 califa AssaultCube[califapublic]",
    "62-210-131-155.rev.poneytelecom.eu aura AssaultCube[local#10000]",
    "62-210-131-155.rev.poneytelecom.eu aura AssaultCube[local#28763]",
    "62-210-131-155.rev.poneytelecom.eu woop AssaultCube[local#10000]",
    "62-210-131-155.rev.poneytelecom.eu woop AssaultCube[local#28763]",
    "62.210.131.155 aura AssaultCube[local#28763]",
    "aura.woop.ac:10000",
    "aura.woop.ac:28763",
    "califa.actionfps.com califa AssaultCube[califapublic]"
  )

}
