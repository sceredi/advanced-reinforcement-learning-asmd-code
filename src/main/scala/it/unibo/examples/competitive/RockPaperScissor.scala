package it.unibo.examples.competitive

import it.unibo.model.core.abstractions.{
  Distribution,
  Enumerable,
  MultiAgentEnvironment,
  StochasticGame,
  AI as GeneralAI
}
import RockPaperScissor.Choice.*

object RockPaperScissor:
  private val PayOff = 1
  enum Choice derives Enumerable:
    case Rock, Paper, Scissor

  type State = Seq[Option[Action]]
  type Action = Choice

  class Dynamics extends StochasticGame[State, Action]:
    override def agents: Int = 2

    override def initialState: Distribution[State] = Distribution.one(Seq(None, None))
    override def transitionFunction: (State, Seq[Action]) => Distribution[State] =
      (_, actions) => Distribution.one(actions.map(Some(_)))

    override def rewardFunction: (State, Seq[Action], State) => Seq[Double] = (_, actions, _) => payoff(actions.toList)
    private def payoff(actions: List[Choice]): Seq[Double] = actions match
      case left :: right :: Nil if left == right => List(0, 0)
      case Rock :: Scissor :: Nil => List(PayOff, -PayOff)
      case Scissor :: Paper :: Nil => List(PayOff, -PayOff)
      case Paper :: Rock :: Nil => List(PayOff, -PayOff)
      case left :: right :: Nil =>
        payoff(right :: left :: Nil).reverse
