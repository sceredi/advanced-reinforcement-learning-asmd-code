package it.unibo.model.core.learning
import it.unibo.model.core.abstractions.DecayReference.Ref
import it.unibo.model.core.abstractions.{AI, Enumerable, Q}

import scala.util.Random

class QAgent[State, Action: Enumerable](
    q: Q[State, Action],
    alpha: Ref[Double],
    gamma: Double,
    epsilon: Ref[Double]
)(using random: Random)
    extends AI.Agent[State, Action]
    with Learner[State, Action]:
  val optimal: State => Action = state => bestOutcome(state)._1
  val behavioural: State => Action = state =>
    if random.nextDouble() < epsilon.value then random.shuffle(Enumerable[Action]).head
    else optimal(state)

  override def improve(
      state: State,
      action: Action,
      reward: Double,
      nextState: State
  ): Unit =
    val qT = q(state, action)
    val (_, qMax) = bestOutcome(nextState)
    val update = qT + alpha.value * (reward + gamma * qMax - qT)
    q.update(state, action, update)

  private def bestOutcome(state: State): (Action, Double) =
    Enumerable[Action].map(action => (action, q(state, action))).maxBy(_._2)
