package it.unibo.model.examples.single

import it.unibo.model.core.abstractions.{Distribution, Enumerable, MDP, SingleAgentContext, SingleAgentEnvironment}
import LampState.*
import LampAction.*
import scala.util.Random

/** Example used to show how to create environment from dynamics. */
enum LampState derives Enumerable:
  case On, Off

enum LampAction derives Enumerable:
  case TurnOn, TurnOff

trait LampContext extends SingleAgentContext:
  override type State = LampState
  override type Action = LampAction

/** MDP dynamics */
class LampMdp(using context: LampContext, random: Random) extends MDP[LampContext]:
  import context.*
  override def initialState = Distribution.uniform[LampState]

  override def transitionFunction: (State, Action) => Distribution[State] = (_, _) => Distribution.uniform[LampState]

  override def rewardFunction: (State, Action, State) => Reward = (_, _, _) => 1.0

@main def main =
  given Random = new Random(42)
  given LampContext = new LampContext {}
  val mdp = new LampMdp
  // environment creation
  val env = MDP.createEnvironment(mdp)
  println(env.state) // sample from the distribution
  env.reset()
  println(env.state) // it could be different
  env.act(LampAction.TurnOn) // show move the environment in On state
  println(env.state)
