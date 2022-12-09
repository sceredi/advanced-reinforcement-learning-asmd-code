package it.unibo.model.examples.competitive
import RockPaperScissor.Context.*
import RockPaperScissor.Choice.*
import RockPaperScissor.Choice
import it.unibo.model.core.abstractions.{AI, Q, Scheduler}
import it.unibo.model.core.learning.QAgent

import scala.util.Random

object SingleAgentTest:

  given Random = Random(42)
  given Scheduler = Scheduler()

  @main def simpleStrategy(): Unit =
    val qLeaner = QAgent[State, Action](Q.zeros[State, Action], 0.1, 0.9, 0.05)
    println("Training... ")
    simulation((qLeaner, AI.RepeatChoiceAgent(Rock)))
    println("Test...")
    simulation((qLeaner, AI.RepeatChoiceAgent(Rock)), false)

  @main def repeatedSequencedStrategy(): Unit =
    val qLeaner = QAgent(Q.zeros[State, Action], 0.1, 0.9, 0.05)
    val ai = AI.RepeatedSequenceChoiceAgent(Rock :: Paper :: Scissor :: Nil)
    println("Training... ")
    simulation((qLeaner, ai))
    println("Test...")
    simulation((qLeaner, ai), false)

  @main def repeatedSequencedStrategyWithoutMemory(): Unit =
    val qLeaner = QAgent(Q.zeros[Unit, Action], 0.1, 0.9, 0.05)
    val adapter = AI.AgentAdapter[State, Unit, Action](qLeaner, _ => ())
    val ai = AI.RepeatedSequenceChoiceAgent(Rock :: Paper :: Scissor :: Nil)
    println("Training... ")
    simulation((adapter, ai))
    println("Test...")
    simulation((adapter, ai), false)
