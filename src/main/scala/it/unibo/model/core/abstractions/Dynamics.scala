package it.unibo.model.core.abstractions
import scala.util.Random
import it.unibo.model.core.abstractions.SingleAgentEnvironment.*

/** TODO: idea, show how to create environment from dynamics, i.e, MDP, markov games, .. */
trait Distribution[A](using random: Random):
  def sample: A

object Distribution:
  def uniform[A: Enumerable](using random: Random): Distribution[A] = new Distribution[A]:
    override def sample: A = random.shuffle(Enumerable[A]).head

trait StochasticGame[C <: MultiAgentContext](using val context: C):
  import context.*
  def agents: Int // agents
  def initialState: Distribution[State]
  def transitionFunction: (State, Collective[Action]) => Distribution[State]
  def rewardFunction: (State, Collective[Action], State) => Collective[Reward]

trait MDP[C <: SingleAgentContext](using override val context: C) extends StochasticGame[C]:
  import context.*
  override val agents = 1

object StochasticGame:
  def createEnvironment[C <: MultiAgentContext](
      stochasticGame: StochasticGame[C]
  ): MultiAgentEnvironment[stochasticGame.context.type] =
    import stochasticGame.context.*
    import stochasticGame.context
    new MultiAgentEnvironment:
      var state: State = stochasticGame.initialState.sample
      override def act(actions: Collective[Action]): Collective[Reward] =
        val previousState = state
        val nextState = stochasticGame.transitionFunction(state, actions)
        state = nextState.sample
        stochasticGame.rewardFunction(previousState, actions, state)
      override def reset(): Unit = state = stochasticGame.initialState.sample
object MDP:
  def createEnvironment[C <: SingleAgentContext](mdp: MDP[C]): SingleAgentEnvironment[mdp.context.type] =
    summon[SingleAgentEnvironment[mdp.context.type] <:< MultiAgentEnvironment[mdp.context.type]]
    StochasticGame.createEnvironment(mdp).asSingle

// Todo create a dsl for creating "dynamics"
