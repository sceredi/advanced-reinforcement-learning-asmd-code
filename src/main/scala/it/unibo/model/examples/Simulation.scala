package it.unibo.model.examples

import it.unibo.model.core.abstractions.{AI, MultiAgentEnvironment, Scheduler}
import cats.{Align, Applicative, Foldable}
import cats.syntax.align.{*, given}
import cats.implicits.*
import it.unibo.model.core.network.log
import it.unibo.view.Render

class Simulation[State, Action](using scheduler: Scheduler)(
    val environment: MultiAgentEnvironment[State, Action],
    renderer: Render[State] = Render.empty[State]
):
  import environment.*
  private val writer = log.SummaryWriter()
  // Kind of hard, better to avoid to show
  def simulate(episodes: Int, episodeLength: Int, agents: Seq[AI.Agent[State, Action]], learn: Boolean = true): Unit =
    agents.foreach(agent => if learn then agent.trainingMode() else agent.testMode())
    scheduler.reset()
    for episode <- 0 to episodes do
      agents.foreach(_.reset())
      var totalRewards = agents.map(_ => 0.0)
      environment.reset()
      for _ <- 0 to episodeLength do
        val currentState = environment.state
        renderer.render(currentState)
        val actions = agents.map(_.act(currentState))
        val rewards = environment.act(actions)
        totalRewards = totalRewards.zip(rewards).map(_ + _)
        val nextState = environment.state
        val actionAndRewards = actions.zip(rewards)
        if learn then
          agents.zip(actionAndRewards).foreach { case (agent, (action, reward)) =>
            agent.record(currentState, action, reward, nextState)
          }
        scheduler.tickStep()
      scheduler.tickEpisode()
      writer.add_scalar(s"Reward+$learn", totalRewards.sumAll, scheduler.episode)
      println(s"Episode $episode, statistics: $totalRewards")

  // Kind of hard, better to avoid to show
  def simulateCentralController(
      episodes: Int,
      episodeLength: Int,
      agent: AI.Agent[State, Seq[Action]],
      learn: Boolean
  ): Unit =
    if (learn) agent.trainingMode() else agent.testMode()
    val space = agent.act(environment.state)
    scheduler.reset()
    for episode <- 0 to episodes do
      agent.reset()
      var totalRewards = space.map(_ => 0.0)
      environment.reset()
      for _ <- 0 to episodeLength do
        val currentState = environment.state
        renderer.render(currentState)
        val actions = agent.act(currentState)
        val rewards = environment.act(actions)
        totalRewards = totalRewards.zip(rewards).map(_ + _)
        val nextState = environment.state
        if learn then agent.record(state, actions, rewards.sumAll, nextState)
        scheduler.tickStep()
      scheduler.tickEpisode()
      writer.add_scalar(s"Reward+$learn", totalRewards.sumAll, scheduler.episode)
      println(s"Episode $episode, statistics: $totalRewards")
