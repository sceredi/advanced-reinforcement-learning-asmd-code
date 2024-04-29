package it.unibo.model.core.network.torch

import org.scalatest.flatspec.AnyFlatSpec
import it.unibo.model.core.abstractions.StochasticGame
import it.unibo.examples.competitive.RockPaperScissor.Choice.*
import org.scalatest.*
import it.unibo.model.core.network.log
class TorchTest extends AnyFlatSpec:

  "Logger" should "correctly log using scalapy" in:
    val logger = log.SummaryWriter()
    val data = 10
    val x = 1
    logger.add_scalar("tag", data, x)
