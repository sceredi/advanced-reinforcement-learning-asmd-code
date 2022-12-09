package it.unibo.model.core.learning

import it.unibo.model.core.network.NeuralNetworkEncoding
import it.unibo.model.core.abstractions.{AI, DecayReference, Enumerable, Scheduler}
import it.unibo.model.core.learning.Learner
import me.shadaj.scalapy.py
import me.shadaj.scalapy.py.SeqConverters
import me.shadaj.scalapy.py.PyQuote
import it.unibo.model.core.network.*

import scala.util.Random
import it.unibo.model.core.abstractions.DecayReference.*

class DeepQAgent[State, Action: Enumerable](
    memory: ReplayBuffer[State, Action],
    epsilon: Ref[Double],
    gamma: Double,
    learningRate: Ref[Double],
    hiddenSize: Int = 32,
    batchSize: Int = 32,
    updateEach: Int = 100
)(using stateEncoding: NeuralNetworkEncoding[State], random: Random, scheduler: Scheduler)
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
    memory.insert(state, action, reward, nextState) // record the current experience in the replay buffer
    val memorySample = memory.sample(batchSize) // sample from the experience to improve the policy network
    if (memory.sample(batchSize).size == batchSize) // wait to have enough samples
      // get S_t, A_t, R_t+1 from the buffer
      // Shape: [buffer_size, state_size]
      val states = memorySample.map(_.state).toSeq.map(state => stateEncoding.toSeq(state).toPythonCopy).toPythonCopy
      // Shape: [buffer_size, action_size]
      val action = memorySample.map(_.action).toSeq.map(action => Enumerable[Action].indexOf(action)).toPythonCopy
      // Shape: [buffer_size, 1]
      val rewards = torch.tensor(memorySample.map(_.reward).toSeq.toPythonCopy)
      val nextState =
        memorySample.map(_.nextState).toSeq.map(state => stateEncoding.toSeq(state).toPythonCopy).toPythonCopy
      // Compute the next action, here will be perform the gradient descent
      val stateActionValue = policyNetwork(torch.tensor(states)).gather(1, torch.tensor(action).view(batchSize, 1))
      // Get an approximation of max_Q(s_t, a_t) using the target network
      val nextStateValues = targetNetwork(torch.tensor(nextState)).max(1).bracketAccess(0).detach()
      // Compute the usual expected value
      val expectedValue = (nextStateValues * gamma) + rewards
      // Simular to MSE, but with L1 regularization
      val criterion = nn.SmoothL1Loss()
      val loss = criterion(stateActionValue, expectedValue.unsqueeze(1))
      writer.add_scalar("Loss", loss, scheduler.totalTicks)
      optimizer.zero_grad() // clear old gradient
      loss.backward() // compute new gradient
      py"[param.grad.data.clamp_(-1, 1) for param in ${policyNetwork.parameters()}]" // clip the gradient, avoid overfitting and exploding gradient
      optimizer.step() // improve the newtwork
      // each updateEach, update the target network (moving target...)
      if scheduler.totalTicks % updateEach == 0 then targetNetwork.load_state_dict(policyNetwork.state_dict())

  private def actionFromNet(state: State, network: py.Dynamic): Action =
    val netInput = stateEncoding.toSeq(state)
    py.`with`(torch.no_grad()) { _ =>
      val tensor = torch.tensor(netInput.toPythonCopy).view(1, stateEncoding.elements)
      val actionIndex = network(tensor).max(1).bracketAccess(1).item().as[Int]
      Enumerable[Action].toList(actionIndex)
    }
