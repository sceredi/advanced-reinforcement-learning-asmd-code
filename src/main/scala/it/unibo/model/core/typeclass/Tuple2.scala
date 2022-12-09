package it.unibo.model.core.typeclass

import cats.{Align, Applicative, Functor}
import cats.data.Ior

object Tuple2:

  given homogenousTupleAlign: Align[[x] =>> (x, x)] = new Align[[x] =>> (x, x)]:

    override def functor: Functor[[x] =>> (x, x)] = new Functor[[x] =>> (x, x)]:
      override def map[A, B](fa: (A, A))(f: A => B): (B, B) = (f(fa._1), f(fa._2))

    override def align[A, B](fa: (A, A), fb: (B, B)): (Ior[A, B], Ior[A, B]) =
      val elements = Align.catsAlignForList.align(fa.toList, fb.toList)
      (elements.head, elements.tail.head)

  given homogenousTupleApplicative: Applicative[[x] =>> (x, x)] = new Applicative[[x] =>> (x, x)]:
    def pure[A](x: A): (A, A) = (x, x)

    override def ap[A, B](ff: (A => B, A => B))(fa: (A, A)): (B, B) =
      val (left, right) = fa
      val (leftApp, rightApp) = ff
      (leftApp(left), rightApp(right))
