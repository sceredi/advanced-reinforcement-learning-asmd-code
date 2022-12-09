package it.unibo.model.examples.cooperative
import cats.{Eval, Reducible}
import it.unibo.model.examples.Simulation
import cats.Reducible
import it.unibo.model.core.abstractions.Enumerable.*

import scala.util.Random
import cats.implicits.*
import it.unibo.model.core.abstractions.{AI, DecayReference, Enumerable, Q, Scheduler}
import it.unibo.model.core.learning.{DeepQAgent, QAgent, ReplayBuffer}
import it.unibo.model.core.network.NeuralNetworkEncoding
import it.unibo.model.core.learning.ReplayBuffer.SingleAgentBuffer
import it.unibo.view.Render

object BoundedWorldTest:
  // Constants
  val agents = 4
  val boundSize = 5
  val trainingEpisodes = 1000
  val longTraining = 10000
  val testEpisodes = 100
  val episodeLength = 10
  val bufferSize = 3000
  val showEachInTest = 10
  val showInTraining = 100
  val waitNextFrame = 100

  // Context
  given context: BoundedWorldContext = new BoundedWorldContext
  given Random = Random(42)
  given Enumerable[List[MovementAction]] = Enumerable.productOf(agents)
  given NeuralNetworkEncoding[context.State] = StateEncoding(agents, boundSize)
  given Scheduler = Scheduler()
  val environment = BoundedWorldEnvironment(agents, boundSize)
  val render = GridWorldRender(boundSize, showInTraining, waitNextFrame)
  val simulator = new Simulation(environment)(render)
  import context.* // for types

  @main def sharedQ(): Unit =
    val same = Q.zeros[State, Action]
    val agents =
      environment.state.map(_ => QAgent(same, 0.1, 0.99, DecayReference.exponentialDecay(0.9, 0.01).bounded(0.01)))
    simulator.simulate(longTraining, episodeLength, agents, true)
    render.renderEach(showEachInTest)
    simulator.simulate(testEpisodes, episodeLength, agents, false)

  @main def independentLearner(): Unit =
    val agents = environment.state.map(_ =>
      QAgent(Q.zeros[State, Action], 0.05, 0.99, DecayReference.exponentialDecay(0.9, 0.01).bounded(0.01))
    )
    simulator.simulate(trainingEpisodes, episodeLength, agents, true)
    render.renderEach(showEachInTest)
    simulator.simulate(testEpisodes, episodeLength, agents, false)

  @main def centralController(): Unit =
    import environment.context.*
    val qTable = Q.zeros[State, List[Action]]
    val centralAgent = QAgent(qTable, 0.05, 0.99, 0.05)
    simulator.simulateCentralController(trainingEpisodes, episodeLength, centralAgent, true)
    render.renderEach(showEachInTest)
    simulator.simulateCentralController(testEpisodes, episodeLength, centralAgent, false)

  @main def deepQLearner(): Unit =
    val agents = environment.state.map(_ =>
      val memory: SingleAgentBuffer[State, Action] = ReplayBuffer.bounded(bufferSize)
      DeepQAgent(memory, DecayReference.exponentialDecay(0.9, 0.01).bounded(0.01), 0.99, 0.05, 32, 2000)
    )
    simulator.simulate(trainingEpisodes, episodeLength, agents, true)
    render.renderEach(showEachInTest)
    simulator.simulate(testEpisodes, episodeLength, agents, false)

  @main def sharedDeepQLearner(): Unit =
    val memory: SingleAgentBuffer[State, Action] = ReplayBuffer.bounded(bufferSize)
    val qLearner = DeepQAgent(memory, DecayReference.exponentialDecay(0.9, 0.01).bounded(0.01), 0.99, 0.005, 64, 1000)
    val agents = qLearner :: environment.state.tail.map(_ => qLearner.slave())
    simulator.simulate(trainingEpisodes, episodeLength, agents, true)
    render.renderEach(showEachInTest)
    simulator.simulate(testEpisodes, episodeLength, agents, false)

  @main def centralControllerDeep(): Unit =
    import environment.context.*
    val memory: SingleAgentBuffer[State, List[Action]] = ReplayBuffer.bounded(bufferSize)
    val learner = DeepQAgent(memory, DecayReference.exponentialDecay(0.9, 0.01).bounded(0.01), 0.99, 0.005, 64, 5000)
    simulator.simulateCentralController(trainingEpisodes, episodeLength, learner, true)
    render.renderEach(showEachInTest)
    simulator.simulateCentralController(testEpisodes, episodeLength, learner, false)
