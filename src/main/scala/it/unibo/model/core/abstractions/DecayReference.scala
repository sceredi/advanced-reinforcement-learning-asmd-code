package it.unibo.model.core.abstractions
import Numeric.Implicits.*
import Fractional.Implicits.*
import math.Ordering.Implicits.*

/** A variable reference that changes according to some temporal updates (e.g., from a scheduler).
  * @tparam V
  *   the type of the data wrapped by this reference
  */
trait DecayReference[V]:
  /** Inner update logic, it cannot be called outside the reference => side effects here!! */
  protected def update(): Unit = {} //

  /** @return Give the current value wrapped from this reference */
  def value: V

  override def toString: String = s"DecayReferenceOf: $value"

object DecayReference:
  type Ref[V] = DecayReference[V] // Shortcut

  // Factories

  /** A constant reference, i.e., it doesn't change over time */
  def constant[V](constant: V): DecayReference[V] = new DecayReference[V]:
    override def value: V = constant

  /** A reference that decays of (finalValue - initialValue) / 2 for each new episodes */
  def linearDecay[V: Fractional](initialValue: V, finalValue: V, steps: Int)(using
      scheduler: Scheduler
  ): DecayReference[V] = new DecayReference[V]:
    private var currentValue = initialValue
    private val tick: V = Fractional[V].minus(initialValue, finalValue) / Fractional[V].fromInt(steps)
    override protected def update(): Unit = currentValue = Fractional[V].minus(currentValue, tick)
    scheduler.eachEpisode(_ => update()) // side effect
    override def value: V = tick

  /** a reference that decays following an exponential eacay: (initialValue) * (1-rate)^epsides */
  def exponentialDecay(initialValue: Double, rate: Double)(using scheduler: Scheduler): DecayReference[Double] =
    new DecayReference[Double]:
      var time: Int = 0
      override protected def update(): Unit = time += 1
      scheduler.eachEpisode(_ => update()) // side effect
      override def value: Double = initialValue * math.pow(1 - rate, time)

  // Conversions
  given constantConversion[V]: Conversion[V, DecayReference[V]] = constant(_)
  given refToValue[V]: Conversion[DecayReference[V], V] = _.value

  // Extensions
  extension [V: Numeric](reference: DecayReference[V])
    def bounded(min: V): DecayReference[V] = new DecayReference[V]:
      override def value: V = if reference.value < min then min else reference.value
