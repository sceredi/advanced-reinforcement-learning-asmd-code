package it.unibo.examples.competitive

import RockPaperScissor.Choice.*
import RockPaperScissor.Choice
import it.unibo.model.core.abstractions.{AI, Q, Scheduler}
import it.unibo.model.core.learning.QAgent
import MultiAgentTest.{episodeLength, episodes, simulator}
import RockPaperScissor.*
import it.unibo.examples.Simulation
import it.unibo.model.core.abstractions.StochasticGame
import scala.util.Random

object SingleAgentTest:
  val episodes = 100
  val episodeLength = 1000
  given Random = Random(42)
  given Scheduler = Scheduler()
  val environment = StochasticGame.createEnvironment(RockPaperScissor.Dynamics())
  val simulator = Simulation[RockPaperScissor.State, RockPaperScissor.Action](environment)

  @main def simpleStrategy(): Unit =
    val qLeaner = QAgent[State, Action](Q.zeros[State, Action], 0.1, 0.9, 0.05)
    println("Training... ")
    qLeaner.trainingMode()
    simulator.simulate(episodes, episodeLength, List(qLeaner, AI.RepeatChoiceAgent(Rock)))
    println("Test...")
    qLeaner.testMode()
    simulator.simulate(episodes, episodeLength, List(qLeaner, AI.RepeatChoiceAgent(Rock)))

  @main def repeatedSequencedStrategy(): Unit =
    val qLearner = QAgent(Q.zeros[State, Action], 0.1, 0.9, 0.05)
    val ai = AI.RepeatedSequenceChoiceAgent(Rock :: Paper :: Scissor :: Nil)
    println("Training... ")
    qLearner.trainingMode()
    simulator.simulate(episodes, episodeLength, List(qLearner, ai))
    println("Test...")
    qLearner.testMode()
    simulator.simulate(episodes, episodeLength, List(qLearner, ai))

  @main def repeatedSequencedStrategyWithoutMemory(): Unit =
    val qLeaner = QAgent(Q.zeros[Unit, Action], 0.1, 0.9, 0.05)
    val adapter = AI.AgentAdapter[State, Unit, Action](qLeaner, _ => ())
    val ai = AI.RepeatedSequenceChoiceAgent(Rock :: Paper :: Scissor :: Nil)
    println("Training... ")
    qLeaner.trainingMode()
    simulator.simulate(episodes, episodeLength, List(adapter, ai))
    println("Test...")
    qLeaner.testMode()
    simulator.simulate(episodes, episodeLength, List(adapter, ai))
