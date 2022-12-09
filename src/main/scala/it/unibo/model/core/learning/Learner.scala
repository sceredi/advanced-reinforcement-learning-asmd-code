package it.unibo.model.core.learning

import it.unibo.model.core.abstractions.AI.AgentMode
import it.unibo.model.core.abstractions.{AI, Enumerable}

trait Learner[State, Action]:
  self: AI.Agent[State, Action] =>
  def optimal: State => Action
  def behavioural: State => Action
  override def act(state: State): Action = (if mode == AgentMode.Training then behavioural else optimal) (state)
