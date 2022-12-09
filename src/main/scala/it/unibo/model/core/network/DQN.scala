package it.unibo.model.core.network

import me.shadaj.scalapy.py

object DQN:
  def apply(input: Int, hidden: Int, output: Int): py.Dynamic =
    nn.Sequential(
      nn.Linear(input, hidden),
      nn.ReLU(),
      nn.Linear(hidden, hidden),
      nn.ReLU(),
      nn.Linear(hidden, output)
    )
