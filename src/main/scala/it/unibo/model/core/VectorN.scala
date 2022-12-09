package it.unibo.model.core

import scala.compiletime.ops.int.-

// Just for fun, express a fixed homogenous tuple. E.g., VectorN[Int, 3] =:= Int *: Int *: Int *: EmptyTuple
// This could be used as a functor, e.g., in MultiAgentEnvironment, you can overwrite collective as:
// Collective[A] = Vector[A, n] where n is the number of agent of that environment
type VectorN[A, N <: Int] = N match
  case 0 => EmptyTuple
  case N => A *: VectorN[A, N - 1]
