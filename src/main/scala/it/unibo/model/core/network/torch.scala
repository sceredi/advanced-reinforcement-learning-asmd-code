package it.unibo.model.core.network

import me.shadaj.scalapy.py

/** Module imports from torch in order to use the python library */
val torch = py.module("torch")
val nn = py.module("torch.nn")
val optim = py.module("torch.optim")
val log = py.module("torch.utils.tensorboard")
