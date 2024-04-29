package it.unibo.model.core.abstractions
import scala.util.Random

/** TODO: idea, show how to create environment from dynamics, markov games, .. */
trait Distribution[A]:
  def sample: A

object Distribution:
  def uniform[A: Enumerable](using random: Random): Distribution[A] = new Distribution[A]:
    override def sample: A = random.shuffle(Enumerable[A]).head
  def one[A](value: A): Distribution[A] = new Distribution[A]:
    override def sample: A = value

trait StochasticGame[State, Action]:
  def agents: Int // agents
  def initialState: Distribution[State]
  def transitionFunction: (State, Seq[Action]) => Distribution[State]
  def rewardFunction: (State, Seq[Action], State) => Seq[Double]

object StochasticGame:
  def createEnvironment[State, Action](
      stochasticGame: StochasticGame[State, Action]
  ): MultiAgentEnvironment[State, Action] =
    new MultiAgentEnvironment[State, Action]:
      var state: State = stochasticGame.initialState.sample
      override def act(actions: Seq[Action]): Seq[Double] =
        val previousState = state
        val nextState = stochasticGame.transitionFunction(state, actions)
        state = nextState.sample
        stochasticGame.rewardFunction(previousState, actions, state)
      override def reset(): Unit = state = stochasticGame.initialState.sample
