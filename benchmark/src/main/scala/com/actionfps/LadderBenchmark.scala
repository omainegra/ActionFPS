package com.actionfps

import com.actionfps.ladder.parser.NickToUser
import controllers.LadderController
import org.openjdk.jmh.annotations.{Benchmark, Scope, Setup, State}
import services.TsvLadderService

/**
  * Created by william on 13/5/17.
  */
@State(Scope.Benchmark)
class LadderBenchmark {

  var nickToUser: NickToUser = _
  @Setup()
  def setup(): Unit = {
    val (_, users) = FullIteratorBenchmark.fetchClansAndUsers()
    nickToUser = LadderController.nickToUserFromUsers(users)
  }

  @Benchmark
  def benchAccumulator(): Unit = {
    TsvLadderService.buildAggregate(
      scala.io.Source.fromFile("../journals/journal.tsv"),
      nickToUser)
  }

}
