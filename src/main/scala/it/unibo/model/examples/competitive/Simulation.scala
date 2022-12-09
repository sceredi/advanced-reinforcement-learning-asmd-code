package it.unibo.model.examples.competitive

import cats.data.Ior
import cats.{Align, Applicative, Functor}
import it.unibo.model.core.VectorN
import it.unibo.model.examples.competitive.RockPaperScissor.Context.{Action, Reward, State}
import it.unibo.model.core.abstractions.AI
import it.unibo.model.core.abstractions.Scheduler
import it.unibo.model.examples.Simulation
import it.unibo.model.core.network.log
val episodes = 100
val episodesLength = 1000

type Agent = AI.Agent[State, Action]

def simulation(agents: (Agent, Agent), learn: Boolean = true)(using scheduler: Scheduler): Unit =
  val writer = log.SummaryWriter()
  agents.toList.foreach((agent: Agent) => if learn then agent.trainingMode() else agent.testMode())
  val (leftAgent, rightAgent) = agents
  val environment = new RockPaperScissor.Environment
  scheduler.reset()
  for episode <- 0 to episodes do
    environment.reset()
    agents.toList.foreach((agent: Agent) => agent.reset())
    var totalRewards = List(0.0, 0.0)
    for _ <- 0 to episodesLength do
      val currentState = environment.state
      val actions = (leftAgent.act(currentState), rightAgent.act(currentState))
      val rewards = environment.act(actions)
      totalRewards = totalRewards.zip(rewards.toList).map { case (total, current) => total + current }
      val nextState = environment.state
      val actionAndRewards = actions.zip(rewards)
      scheduler.tickStep()
      if learn then
        agents.zip(actionAndRewards).toList.foreach { case (agent: Agent, (action: Action, reward: Reward)) =>
          agent.record(currentState, action, reward, nextState)
        }
    scheduler.tickEpisode()
    writer.add_scalar(s"Reward Left $learn", totalRewards.head, scheduler.episode)
    writer.add_scalar(s"Reward Right $learn", totalRewards.tail.head, scheduler.episode)
    println(s"Episode $episode, statistics: (left: ${totalRewards.head}, right: ${totalRewards.tail.head})")
