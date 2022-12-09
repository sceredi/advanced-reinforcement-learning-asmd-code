package it.unibo.model.core.learning

import it.unibo.model.core.network.NeuralNetworkEncoding
import it.unibo.model.core.learning.ReplayBuffer.SingleAgentBuffer
import it.unibo.model.core.abstractions.{AI, DecayReference, Enumerable, MultiAgentContext, Scheduler}
import it.unibo.model.core.learning.Learner
import me.shadaj.scalapy.py
import me.shadaj.scalapy.py.SeqConverters
import me.shadaj.scalapy.py.PyQuote
import it.unibo.model.core.network.*

import scala.util.Random
import it.unibo.model.core.abstractions.DecayReference.*

class DeepQAgent[State, Action: Enumerable](
    memory: SingleAgentBuffer[State, Action],
    epsilon: Ref[Double],
    gamma: Double,
    learningRate: Ref[Double],
    hiddenSize: Int = 32,
    batchSize: Int = 32,
    updateEach: Int = 100
)(using stateEncoding: NeuralNetworkEncoding[State], random: Random, scheduler: Scheduler, c: MultiAgentContext)
    extends AI.Agent[State, Action]
    with Learner[State, Action]:
  self =>
  private val targetNetwork = DQN(stateEncoding.elements, hiddenSize, Enumerable[Action].size)
  private val policyNetwork = DQN(stateEncoding.elements, hiddenSize, Enumerable[Action].size)
  private val writer = log.SummaryWriter()

  def slave(): AI.Agent[State, Action] = new AI.Agent[State, Action] with Learner[State, Action]:
    override def record(state: State, action: Action, reward: Double, nextState: State): Unit =
      memory.insert(state, action, reward, nextState)
    val behavioural = self.behavioural
    val optimal = self.optimal

  private val optimizer = optim.RMSprop(policyNetwork.parameters(), learningRate.value)

  val behavioural: State => Action = state =>
    if random.nextDouble() < epsilon then random.shuffle(Enumerable[Action]).head
    else actionFromNet(state, policyNetwork)

  val optimal: State => Action = state => actionFromNet(state, targetNetwork)

  override def record(state: State, action: Action, reward: Double, nextState: State): Unit =
    memory.insert(state, action, reward, nextState)
    val memorySample = memory.sample(batchSize)
    if (memory.sample(batchSize).size == batchSize)
      val states = memorySample.map(_.state).toSeq.map(state => stateEncoding.toSeq(state).toPythonCopy).toPythonCopy
      val action = memorySample.map(_.action).toSeq.map(action => Enumerable[Action].indexOf(action)).toPythonCopy
      val rewards = torch.tensor(memorySample.map(_.reward).toSeq.toPythonCopy)
      val nextState =
        memorySample.map(_.nextState).toSeq.map(state => stateEncoding.toSeq(state).toPythonCopy).toPythonCopy
      val stateActionValue = policyNetwork(torch.tensor(states)).gather(1, torch.tensor(action).view(batchSize, 1))
      val nextStateValues = targetNetwork(torch.tensor(nextState)).max(1).bracketAccess(0).detach()
      val expectedValue = (nextStateValues * gamma) + rewards
      val criterion = nn.SmoothL1Loss()
      val loss = criterion(stateActionValue, expectedValue.unsqueeze(1))
      writer.add_scalar("Loss", loss, scheduler.totalTicks)
      optimizer.zero_grad()
      loss.backward()
      py"[param.grad.data.clamp_(-1, 1) for param in ${policyNetwork.parameters()}]"
      optimizer.step()
      if scheduler.totalTicks % updateEach == 0 then targetNetwork.load_state_dict(policyNetwork.state_dict())

  private def actionFromNet(state: State, network: py.Dynamic): Action =
    val netInput = stateEncoding.toSeq(state)
    py.`with`(torch.no_grad()) { _ =>
      val tensor = torch.tensor(netInput.toPythonCopy).view(1, stateEncoding.elements)
      val actionIndex = network(tensor).max(1).bracketAccess(1).item().as[Int]
      Enumerable[Action].toList(actionIndex)
    }
