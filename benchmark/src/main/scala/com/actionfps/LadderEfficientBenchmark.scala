package com.actionfps

import java.nio.file.Paths

import com.actionfps.ladder.parser.{NickToUser, TsvExtractEfficient}
import controllers.LadderController
import org.openjdk.jmh.annotations.{Benchmark, Scope, Setup, State}

/**
  * Created by william on 13/5/17.
  */
@State(Scope.Benchmark)
class LadderEfficientBenchmark {

  var nickToUser: NickToUser = _
  @Setup()
  def setup(): Unit = {
    val (_, users) = FullIteratorBenchmark.fetchClansAndUsers()
    nickToUser = LadderController.nickToUserFromUsers(users)
  }

  @Benchmark
  def benchAccumulator(): Unit = {
    TsvExtractEfficient.buildAggregateEfficient(
      servers = com.actionfps.ladder.parser.validServers,
      path = Paths.get("../journals/journal.tsv"),
      nickToUser = nickToUser)
  }

}
