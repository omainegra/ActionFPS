package actionfps
package master
package client

package object authentication {

  val challengeToServerCode = 0
  val serverRespondsToChallengeCode = 1
  val tellServerChallengeIsGood = 2
  val challengeToClientCode = 3
  val clientResponseToChallengeCode = 4
  val serverConfirmsGoodChallenge = 5
}
