# Introduction to Multi-Agent Reinforcement learning (MARL)
## Code for pervasive computing seminar
This repository shows some basic examples of multi-agent reinforcement learning. It contains:
a) facades for the multi-agent environment and environment dynamics definition
b) interfaces for agents definition
c) implementation of Q learning and Deep Q learning
The MARL topic is really broad and consists of several algorithms and environment dynamics. 
For the interested ones, I suggest readings the papers listed in this repository: [https://github.com/LantaoYu/MARL-Papers](https://github.com/LantaoYu/MARL-Papers). 

Unfortunately, a good book on the topics still doesn't exist, 
so the only way to get into it is to read research papers. 
If you have any questions, please feel free to contact me at gianluca[dot]aguzzi[at]unibo[dot]it

## Structure
```mermaid
classDiagram

class Distribution~E~ {
    <<trait>>
    def sample(): E
}
class StochasticGame~State,Action~ {
    <<trait>>
    + def agents: Int
    + def initialState: Distribution~State~
    + def transitionFunction: Function2~State, Seq~Action~, Action~
    + def rewardFunction: Function3~State, Seq~Action~, State~, Seq~Double~~
}

class MultiAgentEnvironment~State,Action~ {
    <<trait>>
    def act(actions: Seq~Action~): State
    def state(): State
    def reset(): Unit
}


class Agent~State, Action~ {
    <<trait>>
    def act(state: State): Action
    def record(state: State, action: Action, reward: Double, nextState: State): Unit
    def reset(): Unit
}

class Scheduler {
    <<trait>>
    def episode: Int
    def step: Int
}

class SchedulerListener {
    <<trait>>
    def onEpisodeChange(episode: Int): Unit
    def onStepChange(episode: Int): Unit
}

class Learner~State, Action~ {
    <<trait>>
    + def mode: Training | Test
    + def trainingMode(): Unit
    + def testMode(): Unit
    # def improve(state: State, action: Action, reward: Double, nextState: State): Unit
}

class Simulation~State, Action~ {
    def simulate(episode: Int, episodeLength: Int, agents: Seq~Agent~State,Action~~): Unit
}

class Render~State~ {
    <<trait>>
    def render(/state: State): Unit
}

class DecayReference~V~ {
    # def update(): Unit
    def value: V
}


StochasticGame -- Distribution: << uses >>
StochasticGame --> MultiAgentEnvironment: << creates >>
Agent -- MultiAgentEnvironment: lives
Agent <|-- Learner
Learner <|-- QLearner
Learner <|-- DeepQLearner
Simulation o-- MultiAgentEnvironment
Simulation o-- Render
Simulation o-- Scheduler
Scheduler o-- SchedulerListener: listen
SchedulerListener -- DecayReference: << uses >>
QLearner -- DecayReference: << uses >>
DeepQLearner -- DecayReference: << uses >>
```
A `StochasticGame` is the extension of MDP in the realm of multi-agent systems (more details [here](https://en.wikipedia.org/wiki/Stochastic_game)) 
and it describes the *dynamics* of a `MultiAgentEnvironment`. 
The latter is a broader term, which means that the dynamics of it could strictly follow the one described by stochastics games.
In a `MultiAgentEnvironment`, a family of `Agent` lives in it, interacting with the environment and perceiving its `State`. 
If an `Agent` has the capability to improve himself through experience, it means that he is a `Learner`.
In order to animate and `MultiAgentEnvironment`, you should use a `Simulation`. 
It performs several runs, each of which has a length equal to `episodeLength`, using the agents passed at the simulator.
### Examples
Using this general structure, in this repository I show two examples:
- Competitive task, **Rock Paper Scissor**: there are two agents that play the game of rock paper scissor (details [here](https://direct.mit.edu/isal/proceedings/alife2018/30/404/99610)). 
 I show what happens if both agents use the same learning algorithm and what happens when one of the two only partially observes the environment. 
 Here there should be a broader discussion on equilibria and multi-agent dynamics but for the goal of this lesson, these topics are out of the scope. 
 For those interested, I suggest reading this beautiful PhD thesis: [Many-agent Reinforcement Learning](https://discovery.ucl.ac.uk/id/eprint/10124273/)
- Cooperative, agents alignment: there are N agents that wanna align themselves in a row or in a column. This is a cooperative setting since the agents need to cooperate in order to reach the right position collectively. Here I should several settings that are common in cooperative tasks (independent learners, team learning/shared Q table and centralised learning). This will be a useful introduction towards "infinite" agent learning.
