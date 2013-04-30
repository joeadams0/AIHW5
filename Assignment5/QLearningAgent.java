 package Assignment5;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Iterator;
import java.io.*;
import java.util.PriorityQueue;		

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionType;
import edu.cwru.sepia.action.TargetedAction;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.ResourceNode.Type;
import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.environment.model.state.Template.TemplateView;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.experiment.Configuration;
import edu.cwru.sepia.experiment.ConfigurationValues;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.state.ResourceNode.Type;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.util.Direction;

/**
 * This agent will first collect gold to produce a peasant,
 * then the two peasants will collect gold and wood separately until reach goal.
 * @author Feng
 *
 */
public class QLearningAgent extends Agent {
	private static final long serialVersionUID = -4047208702628325380L;
	private static final Logger logger = Logger.getLogger(QLearningAgent.class.getCanonicalName());

	private double cumulativeReward = 0;
	private double[] weights;
	private double averagePredictedReward = 0;
	private double alpha = .0001;
	private double discount = .9;
	private double epsilon = .02;
	private int step;
	private StateView currentState;
	private Map<Integer, Integer> healthMap;
	
	public QLearningAgent(int playernum, String[] arguments) {
		super(playernum);
	}

	@Override
	public Map<Integer, Action> initialStep(StateView newstate, History.HistoryView statehistory) {
		step = 0;
		return middleStep(newstate, statehistory);
	}

	@Override
	public Map<Integer,Action> middleStep(StateView newState, History.HistoryView statehistory) {
		step++;
		updateWeights(currentState, newState);
		double reward = getReward(currentState, newState);
		boolean reassignActions = eventOccured(currentState, newState);
		currentState = newState;
		Map<Integer,Action> actions = new HashMap<Integer, Action>();
		if(reassignActions){
			actions = getFootmanActions();
		}
		cumulativeReward = reward;
		return actions;
	}

	// Return the map of the actions to take for each friendly footman.
	private Map<Integer, Action> getFootmanActions(){
		Map<Integer, Action> actions = new HashMap<Integer, Action>();
		return actions;
	}
	
	// Gets the reward for the new state
	private double getReward(StateView oldState, StateView newState){
		return -.1;
	}
	
	private void updateWeights(StateView oldState, StateView newState){
	}
	
	private bool eventOccured(StateView oldState, StateView newState){
		return false;
	}
	
	@Override
	public void terminalStep(StateView newstate, History.HistoryView statehistory) {
		step++;
		if(logger.isLoggable(Level.FINE))
		{
			logger.fine("Congratulations! You have finished the task!");
		}
		System.out.println("=> Step: " + step);
	}

	public static String getUsage() {
		return "Gathers 2000 gold losing as few peasants as possible";
	}
	
	@Override
	public void savePlayerData(OutputStream os) {
		//this agent lacks learning and so has nothing to persist.
		
	}
	
	@Override
	public void loadPlayerData(InputStream is) {
		//this agent lacks learning and so has nothing to persist.
	}
}
