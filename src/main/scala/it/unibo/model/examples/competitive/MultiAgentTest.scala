package it.unibo.model.examples.competitive

import it.unibo.model.core.abstractions.{AI, Q, Scheduler}
import it.unibo.model.examples.competitive.RockPaperScissor.Choice.Rock
import it.unibo.model.examples.competitive.RockPaperScissor.Context.{Action, State}
import it.unibo.model.core.learning.QAgent

import scala.util.Random

object MultiAgentTest:

  given Random = Random(42)
  given Scheduler = Scheduler()
  @main def againstItself(): Unit =
    val qLeaner = QAgent[State, Action](Q.zeros[State, Action], 0.1, 0.9, 0.05)
    println("Training... ")
    simulation((qLeaner, qLeaner))
    println("Test...")
    simulation((qLeaner, qLeaner), false)

  @main def concurrentLearning(): Unit =
    val agentLeft = QAgent(Q.zeros[Option[Action], Action], 0.1, 0.9, 0.05)
      .adapter[State](state => state._2)
    val agentRight = QAgent(Q.zeros[Option[Action], Action], 0.1, 0.9, 0.05)
      .adapter[State](state => state._1)
    println("Training... ")
    simulation((agentLeft, agentRight))
    println("Test...")
    simulation((agentLeft, agentRight), false)

  @main def unfairConcurrentLearning(): Unit =
    val agentLeft = QAgent(Q.zeros[State, Action], 0.1, 0.9, 0.05)
    val agentRight = QAgent(Q.zeros[Unit, Action], 0.1, 0.9, 0.05)
      .adapter[State](_ => ())
    println("Training... ")
    simulation((agentLeft, agentRight))
    println("Test...")
    simulation((agentLeft, agentRight), false)
