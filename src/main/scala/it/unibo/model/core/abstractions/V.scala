package it.unibo.model.core.abstractions
import scala.collection.mutable
trait V[S] extends (S => Double):
  def update(state: S, value: Double): Unit

object V:
  def zeros[S: Enumerable]: V[S] =
    val memory = mutable.Map[S, Double]().withDefault(_ => 0)
    new V[S]:
      export memory.{apply, update}
