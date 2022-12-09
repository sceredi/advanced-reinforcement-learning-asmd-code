package it.unibo.model.core.abstractions

/** Context for creating a multi agent environment It contains the main type definition */
trait MultiAgentContext:
  type State // environment state
  type Action // agent action set (if the agents are heterogeneous, it is the sum type of all the agent action)
  type Reward = Double
  type Collective[A] // A container for a collective type, i.e., something that represent the whole agents

/** A context in which agents have an observation space that is different from the environment state */
trait PartialObservabilityContext:
  self: MultiAgentContext => // Should be mixed with something that is a MultiAgentContext
  type Observation

/** A single agent context is when we have only one agent => Collective is the type itself */
trait SingleAgentContext extends MultiAgentContext:
  override type Collective[A] = A
