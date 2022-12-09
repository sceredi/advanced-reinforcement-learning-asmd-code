package it.unibo.model.examples.competitive

import it.unibo.model.core.abstractions.{AI, Q, Scheduler, StochasticGame}
import it.unibo.model.examples.competitive.RockPaperScissor.Choice.Rock
import it.unibo.model.examples.competitive.RockPaperScissor.{Action, State}
import it.unibo.model.core.learning.QAgent
import it.unibo.model.examples.Simulation

import scala.util.Random

object MultiAgentTest:

  val episodes = 100
  val episodeLength = 1000
  given Random = Random(42)
  given Scheduler = Scheduler()
  val environment = StochasticGame.createEnvironment(RockPaperScissor.Dynamics())
  val simulator = Simulation[RockPaperScissor.State, RockPaperScissor.Action](environment)

  @main def againstItself(): Unit =
    val qLeaner = QAgent[State, Action](Q.zeros[State, Action], 0.1, 0.9, 0.05)
    println("Training... ")
    simulator.simulate(episodes, episodeLength, List(qLeaner, qLeaner))
    println("Test...")
    simulator.simulate(episodes, episodeLength, List(qLeaner, qLeaner), false)

  @main def concurrentLearning(): Unit =
    val agentLeft = QAgent(Q.zeros[Option[Action], Action], 0.1, 0.9, 0.05)
      .adapter[State](state => state(1)) // second agent
    val agentRight = QAgent(Q.zeros[Option[Action], Action], 0.1, 0.9, 0.05)
      .adapter[State](state => state.head) // first agent
    println("Training... ")
    simulator.simulate(episodes, episodeLength, List(agentLeft, agentRight))
    println("Test...")
    simulator.simulate(episodes, episodeLength, List(agentLeft, agentRight), false)

  @main def unfairConcurrentLearning(): Unit =
    val agentLeft = QAgent(Q.zeros[State, Action], 0.1, 0.9, 0.05)
    val agentRight = QAgent(Q.zeros[Unit, Action], 0.1, 0.9, 0.05)
      .adapter[State](_ => ())
    println("Training... ")
    simulator.simulate(episodes, episodeLength, List(agentLeft, agentRight))
    println("Test...")
    simulator.simulate(episodes, episodeLength, List(agentLeft, agentRight), false)
