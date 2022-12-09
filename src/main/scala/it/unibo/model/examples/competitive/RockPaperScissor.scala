package it.unibo.model.examples.competitive

import it.unibo.model.core.abstractions.{Enumerable, MultiAgentEnvironment}
import it.unibo.model.examples.competitive.RockPaperScissor.Choice.*
import it.unibo.model.core.abstractions.{AI => GeneralAI}

object RockPaperScissor {

  enum Choice derives Enumerable:
    case Rock, Paper, Scissor

  type State = Seq[Option[Action]]
  type Action = Choice

  class Environment extends MultiAgentEnvironment[State, Action]:
    var state: State = Seq(None, None)

    private def payoff(actions: List[Choice]): Seq[Double] = actions match
      case left :: right :: Nil if left == right => List(0, 0)
      case Rock :: Scissor :: Nil => List(1, -1)
      case Scissor :: Paper :: Nil => List(1, -1)
      case Paper :: Rock :: Nil => List(1, -1)
      case left :: right :: Nil =>
        payoff(right :: left :: Nil)

    override def act(actions: Seq[Choice]): Seq[Double] =
      state = actions.map(Some(_))
      payoff(actions.toList)

    def reset() = this.state = List(None, None)
}
