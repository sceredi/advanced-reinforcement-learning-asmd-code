package it.unibo.model.core.abstractions

import scala.collection.mutable

trait Q[S, A] extends ((S, A) => Double):
  def update(state: S, action: A, value: Double): Unit

object Q:
  def zeros[S, A: Enumerable]: Q[S, A] =
    val mutableMap = mutable.Map[(S, A), Double]().withDefault(_ => 0.0)
    new Q[S, A]:
      override def update(state: S, action: A, value: Double): Unit = mutableMap.update((state, action), value)
      override def apply(state: S, action: A): Double = mutableMap((state, action))
      override def toString(): String = mutableMap.toString()
