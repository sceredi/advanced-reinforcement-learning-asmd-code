package it.unibo.model.core.abstractions

/** A multi agent environment given a context C */
trait MultiAgentEnvironment[State, Action]:
  def state: State // current environment state
  def act(actions: Seq[Action]): Seq[Double]
  def reset(): Unit
