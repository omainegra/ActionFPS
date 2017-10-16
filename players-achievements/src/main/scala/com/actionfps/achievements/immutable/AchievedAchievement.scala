package com.actionfps.achievements.immutable

trait Achievement {
  def title: String

  def description: String
}

sealed trait IncompleteAchievement extends Achievement

trait PartialAchievement extends Achievement with IncompleteAchievement {
  def progress: Int
}

trait AwaitingAchievement extends Achievement with IncompleteAchievement

trait CompletedAchievement extends Achievement
