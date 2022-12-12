package it.unibo.model.core.abstractions

/** A multi agent environment given a environment State and an Action agent space */
trait MultiAgentEnvironment[State, Action]:
  def state: State // current environment state
  def act(actions: Seq[Action]): Seq[Double]
  def reset(): Unit
