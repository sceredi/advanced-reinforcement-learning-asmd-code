package it.unibo.model.core.learning

import it.unibo.model.core.learning.ReplayBuffer.Experience

import scala.util.Random

/** A memory used during learning to store experience perceived in the exploration phase. It is mainly used in case of
  * deep learning to avoid catastrophic forgetting. For more information, please refer to:
  * https://www.cs.toronto.edu/~vmnih/docs/dqn.pdf
  */
trait ReplayBuffer[State, Action]:
  /** Insert a trajectory into the experience replay. */
  def insert(state: State, action: Action, reward: Double, nextState: State, done: Boolean): Unit

  /** Sample a sequence of experience from the experience replay */
  def sample(batchSize: Int)(using Random): Iterable[Experience[State, Action]]
  def reset(): Unit

object ReplayBuffer:
  private class QueueBuffer[State, Action](maxSize: Int) extends ReplayBuffer[State, Action]:
    private var memory: List[Experience[State, Action]] = List.empty
    override def insert(state: State, action: Action, reward: Double, nextState: State, done: Boolean): Unit =
      memory = (Experience(state, action, reward, nextState, done) :: memory).take(maxSize)

    override def sample(batchSize: Int)(using random: Random): Iterable[Experience[State, Action]] =
      random.shuffle(memory).take(batchSize)

    override def reset(): Unit = memory = List.empty

  /** Experience a single experience recorded into the buffer.
    *
    * It contains a trajectory: s_t, a_t, r_{t+1}, s_{t+1}
    */
  case class Experience[State, Action](state: State, action: Action, reward: Double, nextState: State, done: Boolean)

  /** Create a replay buffer with a bounded storage capability. When the buffer is full, it will drop the oldest
    * experience.
    * @param size
    *   how many samples this buffer is capable to store
    */
  def bounded[State, Action](size: Int): ReplayBuffer[State, Action] =
    QueueBuffer(size)
