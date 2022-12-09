package it.unibo.view

trait Render[State]:
  def render(state: State): Unit = {}

object Render:
  def empty[State]: Render[State] = new Render[State] {}
