package it.unibo.view

import it.unibo.model.core.abstractions.MultiAgentContext

trait Render[State]:
  def render(state: State): Unit = {}

object Render:
  def empty[C <: MultiAgentContext](using context: C): Render[context.State] = new Render[context.State] {}
