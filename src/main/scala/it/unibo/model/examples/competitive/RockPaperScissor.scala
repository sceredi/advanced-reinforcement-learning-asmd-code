package it.unibo.model.examples.competitive

import it.unibo.model.core.abstractions.{Enumerable, MultiAgentContext, MultiAgentEnvironment}
import it.unibo.model.examples.competitive.RockPaperScissor.Choice.*
import it.unibo.model.core.abstractions.{AI => GeneralAI}

object RockPaperScissor {

  enum Choice derives Enumerable:
    case Rock, Paper, Scissor

  object Context extends MultiAgentContext:
    override type Collective[A] = (A, A) // two agent
    override type State =
      (Option[Action], Option[Action]) // the state consist in the previous action take by the agents
    override type Action = Choice

  given Context.type = Context

  class Environment extends MultiAgentEnvironment[Context.type]:
    var state = (None, None)

    private def payoff(actions: (Choice, Choice)): (Double, Double) = actions match
      case (left, right) if left == right => (0, 0)
      case (Rock, Scissor) => (1, -1)
      case (Scissor, Paper) => (1, -1)
      case (Paper, Rock) => (1, -1)
      case (left: Choice, right: Choice) =>
        payoff((right, left)).swap

    override def act(actions: (Choice, Choice)): (context.Reward, context.Reward) =
      state = (Some(actions._1), Some(actions._2))
      payoff(actions)

    def reset() = this.state = (None, None)
}
