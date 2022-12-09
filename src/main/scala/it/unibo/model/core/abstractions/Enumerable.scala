package it.unibo.model.core.abstractions

import it.unibo.model.core.abstractions.Enumerable
import scala.compiletime.summonAll
import scala.deriving.Mirror
import scala.quoted.{Expr, Quotes, Type}

/** Given a type, it enumerates all the instances of that type (it should be finite) */
trait Enumerable[A]:
  def elements: IndexedSeq[A]

object Enumerable:
  def apply[A: Enumerable]: IndexedSeq[A] = summon[Enumerable[A]].elements

  /** given a set of instance for the type A, it returns: A x A x .. x A following the elements parameter. e.g.; enum
    * State derives Enumerable: case On, Off
    *
    * productOf[State](2) ==> List((On, On), (On, Off), (Off, On), (Off, Off)) productOf[State](3) ==> List((On, On,
    * On), (Off, On, On), (Off, Off, On), (Off, Off, Off), ...)
    */
  def productOf[A: Enumerable](elements: Int): Enumerable[List[A]] =
    def product(n: Int, elements: List[A]): List[List[A]] =
      if n == 1 then elements.map(elem => List(elem))
      else product(n - 1, elements).flatMap(elem => elements.map(_ :: elem))
    product(elements, Enumerable[A].toList).asEnumerable

  /** Create an enumerable from a sequence of elements */
  extension [A](iterable: Iterable[A])
    def asEnumerable: Enumerable[A] = new Enumerable[A]:
      override def elements: IndexedSeq[A] = iterable.toIndexedSeq

  /** Giving a set of instances of the type T, it returns that set plus the None type: enum State derives Enumerable:
    * case On, Off
    *
    * fromOption[State] ==> (Some(On), Some(Off), None)
    */
  given fromOption[T: Enumerable]: Enumerable[Option[T]] =
    (Enumerable[T].map(Some(_)) ++ Iterable.single(None)).asEnumerable

  given fromTuple[A: Enumerable, B: Enumerable]: Enumerable[(A, B)] =
    Enumerable[A].flatMap(elem => Enumerable[B].map(inner => (elem, inner))).asEnumerable

  /** return Enumerable(()) */
  given fromUnit: Enumerable[Unit] = Iterable(()).asEnumerable

  // macro for creating enumerable from enum
  inline def enumValues[E]: Array[E] = ${ enumValuesImpl[E] }

  def enumValuesImpl[E: Type](using quotes: Quotes): Expr[Array[E]] =
    import quotes.reflect.*
    val companion = Ref(TypeTree.of[E].symbol.companionModule)
    Select.unique(companion, "values").asExprOf[Array[E]]

  inline given derived[T]: Enumerable[T] = enumValues[T].toIndexedSeq.asEnumerable // derives!!
