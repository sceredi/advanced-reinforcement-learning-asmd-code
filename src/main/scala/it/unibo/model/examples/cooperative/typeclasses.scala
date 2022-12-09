package it.unibo.model.examples.cooperative

import it.unibo.model.core.network.NeuralNetworkEncoding

class StateEncoding(agents: Int, maxBound: Float) extends NeuralNetworkEncoding[List[(Int, Int)]]:
  private val stateSpaceSize = 2
  override def elements: Int = agents * stateSpaceSize

  override def toSeq(elem: List[(Int, Int)]): Seq[Double] = elem.flatMap { case (l, r) =>
    List(l / maxBound, r / maxBound)
  }
