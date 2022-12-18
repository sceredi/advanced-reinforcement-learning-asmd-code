package it.unibo.examples.competitive
import org.scalatest.flatspec.AnyFlatSpec
import it.unibo.model.core.abstractions.StochasticGame
import it.unibo.examples.competitive.RockPaperScissor.Choice.*
import org.scalatest.*
class RockPaperScissorTest extends AnyFlatSpec with BeforeAndAfterEach:
  var environment = StochasticGame.createEnvironment(RockPaperScissor.Dynamics())
  "Rock paper scissor environment" should "has as an initial state, (None, None)" in {
    assert(environment.state == Seq(None, None))
  }

  "Rock paper scissor environment" should "has as a previous state, the agent's action" in {
    val actions = Seq(Rock, Paper)
    environment.act(actions)
    assert(environment.state == actions.map(Some(_)))
  }

  "Rock paper scissor environment" should "give good reward for the right move, and negative for the bad move" in {
    assert(environment.act(Seq(Rock, Paper)) == Seq(-1, 1))
  }

  "Rock paper scissor environment" should "give 0 as reward when both action are the same" in {
    assert(environment.act(Seq(Rock, Rock)) == Seq(0, 0))
  }
