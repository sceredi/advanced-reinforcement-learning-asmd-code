package it.unibo.examples.cooperative

import it.unibo.model.core.abstractions.{Enumerable, MultiAgentEnvironment}

import scala.util.Random

enum MovementAction derives Enumerable:
  case Up, Down, Right, Left, NoOp
  def updatePosition(position: (Int, Int), bound: Int): (Int, Int) =
    def pacmanEffect(coordinate: Int): Int = if coordinate < 0 then bound else coordinate
    val (x, y) = position
    this match
      case Up => (x, (y + 1) % bound)
      case Down => (x, pacmanEffect(y - 1))
      case Left => (pacmanEffect(x - 1), y)
      case Right => ((x + 1) % bound, y)
      case NoOp => position

object BoundedWorldEnvironment:
  type State = List[(Int, Int)]
  type Action = MovementAction

class BoundedWorldEnvironment(using random: Random)(agents: Int, boundSize: Int)
    extends MultiAgentEnvironment[BoundedWorldEnvironment.State, BoundedWorldEnvironment.Action]:
  import BoundedWorldEnvironment.*
  var state: State = generatePosition
  def act(actions: Seq[Action]): Seq[Double] =
    state = state.zip(actions).map { case (state, action) => action.updatePosition(state, boundSize) }
    val ys = state.distinctBy(_._1).size
    val xs = state.distinctBy(_._2).size
    // Check if the agents are in the same line
    if ys == 1 || xs == 1 then state.map(_ => 10) else state.map(_ => -1) // simple reward, idea
  override def reset(): Unit = state = generatePosition

  private def generatePosition =
    (0 until agents).map(_ => (random.nextInt(boundSize), random.nextInt(boundSize))).toList
