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
import edu.cwru.sepia.environment.model.history.History.HistoryView;
import edu.cwru.sepia.environment.model.history.DamageLog;
import edu.cwru.sepia.environment.model.history.DeathLog;
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
	private double maximumPredictedReward = 0;
	private double alpha = .0001;
	private double discount = .9;
	private double epsilon = .02;
	private int step;
	private int lastEvent = -10;
	private int maxStepInterval = 10;
	private StateView currentState;
	
	public QLearningAgent(int playernum, String[] arguments) {
		super(playernum);
	}

	@Override
	public Map<Integer, Action> initialStep(StateView newstate, History.HistoryView statehistory) {
		step = -1;
		currentState = newstate;
		return middleStep(newstate, statehistory);
	}

	@Override
	public Map<Integer,Action> middleStep(StateView newState, History.HistoryView statehistory) {
		step++;
		currentState = newState;
		updateWeights(statehistory);
		double reward = getReward(statehistory);
		boolean reassignActions = eventOccured(statehistory);
		Map<Integer,Action> actions = new HashMap<Integer, Action>();
		if(reassignActions){
			actions = getFootmanActions();
			lastEvent = step;
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
	private double getReward(History.HistoryView stateHistory){
		return -.1;
	}
	
	private void updateWeights(History.HistoryView stateHistory){
		
	}
	
	// Events:
	// Death of any footman
	// Our footman being hurt
	// Max of 10 steps since last event.
	private boolean eventOccured(History.HistoryView stateHistory){
		if(step-lastEvent >= maxStepInterval){
			return true;
		}
		else{
			// Check deaths of our footmen
			boolean event = deathOccured(stateHistory, playernum);
			// Check deaths of enemy footmen
			event = event || deathOccured(stateHistory, playernum);
			// Check if footmen has been hurt
			event = event || hasBeenDamaged(stateHistory, playernum);
			return event;
		}
	}
	
	private boolean deathOccured(HistoryView stateHistory, int playerNumber){
		List<DeathLog> deaths = stateHistory.getDeathLogs(step-1);
		for(DeathLog death : deaths){
			if(death.getController() == playerNumber){
				return true;
			}
		}
		return false;
	}
	
	private boolean hasBeenDamaged(HistoryView stateHistory, int playerNumber){
		List<DamageLog> damages = stateHistory.getDamageLogs(step-1);
		for(DamageLog damage : damages){
			if(damage.getDefenderController() == playerNumber){
				return true;
			}
		}
		return false;
	}
	
	/// HELPERS
	private List<UnitView> getFootmen(StateView state, int playerNumber){
		List<UnitView> units = state.getUnits(playerNumber);
		Iterator<UnitView> itr = units.iterator();
		while(itr.hasNext()){
			if(!(itr.next().getTemplateView().getName().equals("Footman"))){
				itr.remove();
			}
		}
		return units;
	}
	
	private int getEnemyId(){
		Integer[] playerNums = currentState.getPlayerNumbers();
		for(int i = 0; i< playerNums.length; i++){
			if(playerNums[i] != playernum){
				return playerNums[i];
			}
		}
		return -1;
	}
	private void printUnits(List<UnitView> units){
		for(UnitView unit : units){
			printUnit(unit);
		}
	}
	
	private void printUnit(UnitView unit){
		System.out.println(unit.getTemplateView().getName() + "(" + unit.getID() + ") - (" + unit.getXPosition() + ", " + unit.getYPosition() + ")");
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
