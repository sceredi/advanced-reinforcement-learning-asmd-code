package it.unibo.model.examples.competitive
import RockPaperScissor.Choice.*
import RockPaperScissor.Choice
import it.unibo.model.core.abstractions.{AI, Q, Scheduler}
import it.unibo.model.core.learning.QAgent
import it.unibo.model.examples.Simulation
import it.unibo.model.examples.competitive.MultiAgentTest.{episodeLength, episodes, simulator}
import it.unibo.model.examples.competitive.RockPaperScissor.*
import scala.util.Random

object SingleAgentTest:
  val episodes = 100
  val episodeLength = 1000
  given Random = Random(42)
  given Scheduler = Scheduler()
  val environment = RockPaperScissor.Environment()
  val simulator = Simulation[RockPaperScissor.State, RockPaperScissor.Action](environment)

  @main def simpleStrategy(): Unit =
    val qLeaner = QAgent[State, Action](Q.zeros[State, Action], 0.1, 0.9, 0.05)
    println("Training... ")

    simulator.simulate(episodes, episodeLength, List(qLeaner, AI.RepeatChoiceAgent(Rock)))
    println("Test...")
    simulator.simulate(episodes, episodeLength, List(qLeaner, AI.RepeatChoiceAgent(Rock)), false)

  @main def repeatedSequencedStrategy(): Unit =
    val qLearner = QAgent(Q.zeros[State, Action], 0.1, 0.9, 0.05)
    val ai = AI.RepeatedSequenceChoiceAgent(Rock :: Paper :: Scissor :: Nil)
    println("Training... ")
    simulator.simulate(episodes, episodeLength, List(qLearner, ai))
    println("Test...")
    simulator.simulate(episodes, episodeLength, List(qLearner, ai), false)

  @main def repeatedSequencedStrategyWithoutMemory(): Unit =
    val qLeaner = QAgent(Q.zeros[Unit, Action], 0.1, 0.9, 0.05)
    val adapter = AI.AgentAdapter[State, Unit, Action](qLeaner, _ => ())
    val ai = AI.RepeatedSequenceChoiceAgent(Rock :: Paper :: Scissor :: Nil)
    println("Training... ")
    simulator.simulate(episodes, episodeLength, List(adapter, ai))
    println("Test...")
    simulator.simulate(episodes, episodeLength, List(adapter, ai), false)
