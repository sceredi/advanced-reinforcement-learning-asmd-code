package it.unibo.model.core.abstractions

/** A multi agent environment given a context C */
trait MultiAgentEnvironment[+C <: MultiAgentContext](using val context: C):
  import context.* // to get the type
  def state: State // current environment state
  def act(actions: Collective[Action]): Collective[Reward]
  def reset(): Unit

trait PartialObservability[+C <: MultiAgentContext with PartialObservabilityContext](using val context: C):
  self: MultiAgentEnvironment[C] =>
  import context.*
  def observations: Collective[Observation]

trait SingleAgentEnvironment[+C <: SingleAgentContext](using override val context: C) extends MultiAgentEnvironment[C]

object SingleAgentEnvironment:
  extension [C <: SingleAgentContext](env: MultiAgentEnvironment[C])
    def asSingle: SingleAgentEnvironment[env.context.type] =
      given c: env.context.type = env.context
      new SingleAgentEnvironment[env.context.type]:
        override def state: context.State = env.state
        override def act(actions: context.Action): context.Reward = env.act(actions)
        override def reset(): Unit = env.reset()
