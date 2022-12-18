package it.unibo.model.core.abstractions

import it.unibo.examples.competitive.RockPaperScissor
import it.unibo.model.core.learning.Learner

object AI:
  /** An entity that perceives a observation (e.g., the environment state, an agent observation, ...) and produces
    * actions that could be handled by an environment
    */
  trait Agent[-Observation, Action]:
    /** Using the observation, it chooses an action */
    def act(state: Observation): Action

    /** Record the environment response (reward) when the agent is in this state and performs that action moving the
      * environment in the nextState. This experience will be used to improve the agent in the Training mode.
      */
    def record(state: Observation, action: Action, reward: Double, nextState: Observation): Unit = {}

    /** Reset any internal agent structure using during test/training */
    def reset(): Unit = {}

  /** Sometime certain agent has a limited vision of the environment. This class could be used in this case, creating an
    * agent that has only a partial observability of the environment.
    */
  class AgentAdapter[State, Observation, Action](agent: Agent[Observation, Action], conversion: State => Observation)
      extends Agent[State, Action]:
    override def act(state: State): Action = agent.act(conversion(state))
    override def record(state: State, action: Action, reward: Double, nextState: State): Unit =
      agent.record(conversion(state), action, reward, conversion(nextState))
    override def reset(): Unit = agent.reset()

  // Helper to adapt an agent
  extension [State, Action](agent: Agent[State, Action])
    def adapter[Observation](conversion: Observation => State): Agent[Observation, Action] =
      AgentAdapter[Observation, State, Action](agent, conversion)

  /** Simple agent that repeat the same action */
  class RepeatChoiceAgent[Action](choice: Action) extends Agent[Any, Action]:
    def act(state: Any) = choice

  /** Simple agent that repeats a sequence of action. Example:
    * ```
    * val state: Any = ...
    * val agent = RepeatedSequenceChoiceAgent(1 :: 2 :: 3).
    * agent.act(state) // 1
    * agent.act(state) // 2
    * agent.act(state) // 3
    * ```
    */
  class RepeatedSequenceChoiceAgent[Action](choices: Seq[Action]) extends Agent[Any, Action]:
    private var choicesList: Seq[Action] = choices

    override def act(state: Any): Action =
      val result = choicesList.head
      choicesList = choicesList.tail :+ choicesList.head
      result

    override def reset(): Unit = this.choicesList = choices
