package it.unibo.model.examples

import it.unibo.model.core.abstractions.{AI, MultiAgentContext, MultiAgentEnvironment, Scheduler}
import cats.{Align, Applicative, Foldable}
import cats.syntax.align.{*, given}
import cats.implicits.*
import it.unibo.model.core.network.log
import it.unibo.view.Render

class Simulation[C <: MultiAgentContext](using scheduler: Scheduler)(val environment: MultiAgentEnvironment[C])(
    renderer: Render[environment.context.State]
):
  import environment.*
  import environment.context.*

  private val writer = log.SummaryWriter()
  // Kind of hard, better to avoid to show
  def simulate(episodes: Int, episodeLength: Int, agents: Collective[AI.Agent[State, Action]], learn: Boolean)(using
      Align[Collective],
      Applicative[Collective],
      Foldable[Collective]
  ): Unit =
    Applicative[Collective].map(agents)(agent => if learn then agent.trainingMode() else agent.testMode())
    scheduler.reset()
    for episode <- 0 to episodes do
      Applicative[Collective].map(agents)((agent: AI.Agent[State, Action]) => agent.reset())
      var totalRewards = Applicative[Collective].map(agents)(_ => 0.0)
      environment.reset()
      for _ <- 0 to episodeLength do
        val currentState = environment.state
        renderer.render(currentState)
        val actions = agents.map(_.act(currentState))
        val rewards = environment.act(actions)
        totalRewards = totalRewards.padZipWith(rewards) { case (Some(total), Some(partial)) => total + partial }
        val nextState = environment.state
        val actionAndRewards = actions.padZipWith(rewards) { case (Some(action), Some(reward)) =>
          (action, reward)
        }
        if learn then
          agents.padZipWith(actionAndRewards) { case (Some(agent), Some(action, reward)) =>
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
      agent: AI.Agent[State, Collective[Action]],
      learn: Boolean
  )(using Align[Collective], Applicative[Collective], Foldable[Collective]): Unit =
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
        totalRewards = totalRewards.padZipWith(rewards) { case (Some(total), Some(partial)) =>
          total + partial
        }
        val nextState = environment.state
        if learn then agent.record(state, actions, rewards.sumAll, nextState)
        scheduler.tickStep()
      scheduler.tickEpisode()
      writer.add_scalar(s"Reward+$learn", totalRewards.sumAll, scheduler.episode)
      println(s"Episode $episode, statistics: $totalRewards")
