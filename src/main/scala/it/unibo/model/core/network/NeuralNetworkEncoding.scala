package it.unibo.model.core.network

/** Encoding that convert a type representation A into a "tensor-like" data (e.g., a sequence of double)
  * @tparam A
  *   the representation of the data that will be encoded
  */
trait NeuralNetworkEncoding[A]:
  def elements: Int
  def toSeq(elem: A): Seq[Double]
