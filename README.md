# This project was a group project for the TTU CSC 4240 course. The code in this repository is taken directly from my branch of the project, meaning that, besides the foundation of the project provided by Dr. Eberle, everything in this repository was written by myself.


# 1a) 
To stop the agent from continuously using the vacuums even when they aren't necessary, we decided that punishing the agent for using the vacuums would ensure that they would be used as little as possible. However, the reward for capturing bugs would outweigh the cost of using the vacuum, meaning that the agent would still want to capture bugs.


# 1b) 
We added the ability for the agent to use all power levels of the vacuum, including the vacuum being off.


# 1c) 
**Performance measured as bugs captures over bugs escaped.**

*LearnerOne's* performance over 3 rounds:  55 / 76  |  69 / 59  | 86 / 54   = 1.16 (average bugs per run: 133)

*Team2Agent's* performance over 3 rounds: 313 / 147 | 262 / 142 | 278 / 118 = 2.11 (average bugs per run: 420)

If given more time to run, we believe that *Team2Agent* would continue to increase the difference between *LearnerOne*'s average bugs per run due to the better vacuum power efficiency allowing the simulation to last longer.


# 1d) 
We modified the rewards given to the agent in the function "step" in Team2Agent.jar when the agent captures a bug and when the agent uses the vacuum at all five power levels (including no power). The values of the rewards were changed, and then each set of rewards was tested three times with the average bugs captures to bugs escaped ratio used to determine the efficiency of the rewards. 

Through testing, we determined the following: both punishing and rewarding the agent for leaving the vacuum off was detremental to the final score; high punishments for using any power level of the vacuum caused the agent to never capture any bugs, and high rewards for using any power level of the vacuum caused the agent to always leave the vacuum on regardless of whether it was capturing bugs; lowering the reward for capturing a bug relative to the punishment of using the vacuum resulted in the agent capturing less bugs. 

Our motivation for testing these variables was to try and get the agent to use the vacuum only when necessary to capture bugs in an attempt to save power. Saving power then allows the game to run for a longer duration, resulting in more opprotunities for the agent to capture bugs.

# 2) a
Goal: To change the method of tie breaking for equally valued actions so that
the agent is more likely to keep doing the same action if two or more have the same
action value.

First we implemented comparison methods for the Agent Action class.
```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AgentAction action = (AgentAction) o;
    return power == action.power && facing == action.facing;
}

@Override
public int hashCode() {
    return Objects.hash(power, facing);
}
```
Next we modify findBestAction() in our QMap to accept the last action as a parameter.
```java
public AgentAction findBestAction(boolean verbose, AgentAction lastAction) {
    ...
}
```
Finally we insert the following piece of code into findBestAction()
```java
if (maxcount > 1) {
    for (int x = 0; x < utility.length; x++) {
        if (utility[x] == maxi && actions[x].equals(lastAction) && RN.nextDouble() > 0.2) {
            return actions[x];
        }
    }
}
```
This piece of code ensures that if there are more than one best action and one of those
actions is the last action that was taken then we will choose it 80% of the time.


# 2) b
Goal: To implement a Q learning agent.

First we implemented a QTable:
```java
import jig.misc.rd.AirCurrentGenerator;
import jig.misc.rd.Direction;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class QTable implements Serializable {

    /**
     * enumerates all the possible actions.
     */
    public static HashMap<AgentAction, Double> actions = new HashMap<AgentAction, Double>();
    public static ArrayList<AgentAction> actionIndices = new ArrayList<AgentAction>();
    static {
        Direction[] dirs = Direction.values();
        AgentAction act;
        for(Direction d: dirs) {
            // set to 4
            for (int level = 4; level <= AirCurrentGenerator.POWER_SETTINGS; level++) {
                act = new AgentAction(level, d);
                actions.put(act, 0.0);
                actionIndices.add(act);
            }
        }
    }

    // Decimal formatter for toString
    DecimalFormat decimalFormat;

    // random number generator
    Random random;

    // Action value table
    public HashMap<StateVector, HashMap<AgentAction, Double>> qTable;

    /**
     * Constructor.
     */
    public QTable() {
        qTable = new HashMap<StateVector, HashMap<AgentAction, Double>>();
        decimalFormat = new DecimalFormat("#.##");
        random = new Random();
    }

    /**
     * Sets the value of an action taken at a given state.
     * @param stateVector the given state
     * @param agentAction the given action
     * @param value the new value for the action
     */
    public void setValue(StateVector stateVector, AgentAction agentAction, double value) {
        if (!qTable.containsKey(stateVector)) {
            qTable.put(stateVector, (HashMap<AgentAction, Double>) actions.clone());
        }
        qTable.get(stateVector).replace(agentAction, value);
    }

    /**
     * Gets the value of a certain action from a given state.
     * @param stateVector the given state
     * @param agentAction the given action
     * @return the value of the action
     */
    public double getValue(StateVector stateVector, AgentAction agentAction) {
        if (!qTable.containsKey(stateVector)) {
            qTable.put(stateVector, (HashMap<AgentAction, Double>) actions.clone());
        }
        return qTable.get(stateVector).get(agentAction);
    }

    /**
     * Returns "a" best action for a given state.
     * @param  stateVector the given state
     * @return the best action
     */
    public AgentAction getBestAction(StateVector stateVector) {
        if (!qTable.containsKey(stateVector)) {
            qTable.put(stateVector, (HashMap<AgentAction, Double>) actions.clone());
        }
        ArrayList<AgentAction> best = getBestActions(stateVector);
        return best.get(random.nextInt(best.size()));
    }

    /**
     * Returns the actions with the best value for a given state.
     * @param stateVector the given state
     * @return an array of best actions
     */
    public ArrayList<AgentAction> getBestActions(StateVector stateVector) {
        if (!qTable.containsKey(stateVector)) {
            qTable.put(stateVector, (HashMap<AgentAction, Double>) actions.clone());
        }
        ArrayList<AgentAction> bestActions = new ArrayList<AgentAction>();
        for (AgentAction agentAction : qTable.get(stateVector).keySet()) {
            if (bestActions.size() == 0) {
                bestActions.add(agentAction);
            } else if (qTable.get(stateVector).get(agentAction).compareTo(qTable.get(stateVector).get(bestActions.get(0))) > 0) {
                bestActions.clear();
                bestActions.add(agentAction);
            } else  if (qTable.get(stateVector).get(agentAction).equals(qTable.get(stateVector).get(bestActions.get(0)))) {
                bestActions.add(agentAction);
            }
        }
        return bestActions;
    }

    /**
     * Returns the value of the best action for a given state.
     * @param stateVector the given state
     * @return the value of the best action
     */
    public double getBestActionValue(StateVector stateVector) {
        if (!qTable.containsKey(stateVector)) {
            qTable.put(stateVector, (HashMap<AgentAction, Double>) actions.clone());
        }
        return qTable.get(stateVector).get(getBestAction(stateVector));
    }

    /**
     * To string method.
     * @return string representation of the state-action-value table
     */
    @Override
    public String toString() {
        String matrix = "[ QTable - \tSize: " + qTable.size() + "\n";
        for (StateVector stateVector : qTable.keySet()) {
            matrix += "\t" + stateVector.hashCode() + " [";
            int size = actionIndices.size();
            for (AgentAction agentAction : actionIndices) {
                matrix += decimalFormat.format(qTable.get(stateVector).get(agentAction));
                if (size > 1) {
                    matrix += ", ";
                }
                size--;
            }
            matrix += "]\n";
        }
        matrix += "]";
        return matrix;
    }
}
```

We will use the equation 
```
Q(s,a) = (1-lr) * Q(s,a) + lr * (R + g * Q(ns,BestAction(ns)))
where..
Q(state,action) is the value a state action pair.
lr is the learning rate
R is the reward for that action
g is the discount factor
s is the the state
a is our action 
ns is our new state
```

Our method for random exploration is mostly the same
```java
if (random.nextDouble() > 0.8) {
    action = QTable.actionIndices.get(random.nextInt(QTable.actionIndices.size()));
} else {
    action = qTable.getBestAction(state);
}
```
In order to cause our Q Table to converge the correct function we will store our experiance 
in a buffer and then replay that buffer every time we change state.
```java
buffer.add(new Quartet(last_state, last_action, state, reward));
for (int i = 0; i < buffer.size(); i++) {
    Quartet tuple = buffer.get(i);
    double maxNextValue = qTable.getBestActionValue(tuple.nextState);
    double newValue = (1 - lr) * qTable.getValue(tuple.state, tuple.action) + (lr * (tuple.reward + gamma * maxNextValue));
    qTable.setValue(tuple.state, tuple.action, newValue);
}
```
### Algorithm Performance
We started by setting gamma to 0 which effectively made our QAgent act like the default
algorithm we were provided with. Because if gamma equals 0 the value of the next action 
state is not taken into consideration. When we did this we were able to capture about
65% of the bugs; however, as soon as we started to increase the value of gamma our 
performance stated to decrees.

### Our Conclusions
In our opinion for base unaltered environment the Q learning algorithm is not 
the best approach. The reason for this is that an action may or may not actually have
an effect on the next state. Consider the following situation:\
![Example State](example.png)\
Sucking in a downward direction has no effect on the movement of the bug; however,
because the bugs almost without fail travel straight from left to right this will
give the agent the impression that there is a connection between taking that action and
the next state. That being said if the RADIUS of the StateVector class was extended
(and possible the number of actions for each state limited by direction),
the Agent might (given enough time) do better because it would learn to suck in bugs
from a distance; however, in the short term the Q Learning algorithm is destructive
to agent performance.
