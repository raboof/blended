package de.woq.blended.itestsupport.condition

import java.util.UUID

import scala.concurrent.duration._

class AlwaysTrue extends Condition {
  val id = UUID.randomUUID().toString
  override def satisfied(): Boolean = true
  override def toString: String = s"AlwaysTrueCondition[$id]"
  override def timeout = 100.millis
}

class NeverTrue extends Condition {
  val id = UUID.randomUUID().toString
  override def satisfied(): Boolean = false
  override def toString: String = s"NeverTrueCondition[$id]"
  override def timeout = 100.millis
}

trait ConditionProvider {
  def alwaysTrue() = new AlwaysTrue
  def neverTrue() = new NeverTrue
}
