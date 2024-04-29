package it.unibo.model.core.learning

import Learner.*
import it.unibo.model.core.abstractions.{AI, Enumerable}

trait Learner[State, Action]:
  self: AI.Agent[State, Action] =>
  def optimal: State => Action
  def behavioural: State => Action

  private var modeMemory = AgentMode.Test

  /** Enter in training mode (in some agents this could not perform any effect) */
  def trainingMode(): Unit = this.modeMemory = AgentMode.Training

  /** Enter in the test mode */
  def testMode(): Unit = this.modeMemory = AgentMode.Test

  /** Current mode followed by this agent */
  def mode: AgentMode = modeMemory

  override def act(state: State): Action = (if mode == AgentMode.Training then behavioural else optimal) (state)

  override def record(state: State, action: Action, reward: Double, nextState: State, done: Boolean = false): Unit =
    if mode == AgentMode.Training then improve(state, action, reward, nextState, done)

  def improve(state: State, action: Action, reward: Double, nextState: State, done: Boolean): Unit
object Learner:
  /** Agent internal mode. Some agents, even if are configured in the training mode, remain "stupid" and unable to
    * process experience
    */
  enum AgentMode:
    case Training, Test
