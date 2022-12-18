package it.unibo.examples.competitive

import it.unibo.model.core.abstractions.{AI, Enumerable, Q, Scheduler, StochasticGame}
import RockPaperScissor.Choice.Rock
import RockPaperScissor.{Action, State}
import it.unibo.examples.Simulation
import it.unibo.model.core.learning.QAgent

import scala.util.Random

object MultiAgentTest:

  val episodes = 100
  val episodeLength = 1000
  given Random = Random(42)
  given Scheduler = Scheduler()
  given Enumerable[Seq[Option[Action]]] = Enumerable.productOf(2)
  val environment = StochasticGame.createEnvironment(RockPaperScissor.Dynamics())
  val simulator = Simulation[RockPaperScissor.State, RockPaperScissor.Action](environment)

  @main def againstItself(): Unit =
    val qTable = Q.zeros[State, Action]
    val qLeaner = QAgent[State, Action](qTable, 0.1, 0.9, 0.05)
    println("Training... ")
    qLeaner.trainingMode()
    simulator.simulate(episodes, episodeLength, List(qLeaner, qLeaner))
    qLeaner.testMode()
    println("Test...")
    simulator.simulate(episodes, episodeLength, List(qLeaner, qLeaner))
    Q.renderBest(qTable)

  @main def concurrentLearning(): Unit =
    val leftQ = Q.zeros[Option[Action], Action]
    val rightQ = Q.zeros[Option[Action], Action]
    val agentLeft = QAgent(leftQ, 0.1, 0.9, 0.05)
    val agentRight = QAgent(rightQ, 0.1, 0.9, 0.05)
    println("Training... ")
    agentLeft.trainingMode()
    agentRight.trainingMode()
    val adapterLeft = agentLeft.adapter[State](state => state(1))
    val adapterRight = agentRight.adapter[State](state => state.head)
    simulator.simulate(episodes, episodeLength, List(adapterLeft, adapterRight))
    agentLeft.testMode()
    agentRight.testMode()
    simulator.simulate(episodes, episodeLength, List(adapterLeft, adapterRight))
    println("LEFT Q")
    Q.renderBest(leftQ)
    println("RIGHT Q")
    Q.renderBest(rightQ)

  @main def unfairConcurrentLearning(): Unit =
    val agentLeft = QAgent(Q.zeros[State, Action], 0.1, 0.9, 0.05)
    val agentRight = QAgent(Q.zeros[Unit, Action], 0.1, 0.9, 0.05)
    println("Training... ")
    val rightAdapter = agentRight.adapter(_ => ())
    agentLeft.trainingMode()
    agentRight.trainingMode()
    simulator.simulate(episodes, episodeLength, List(agentLeft, rightAdapter))
    println("Test...")
    agentLeft.testMode()
    agentRight.testMode()
    simulator.simulate(episodes, episodeLength, List(agentLeft, rightAdapter))
