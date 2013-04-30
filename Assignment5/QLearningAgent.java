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
import java.util.Random;	

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

	private double totalCumulativeReward = 0;
	private double cumulativeReward = 0;
	private double[] weights;
	private double totalPredictedReward = 0;
	private int episode = 1;
	private int learningEpisodes = 10;
	private int learningEpisodesInterval = 9;
	private int testingEpisodesInterval = 4;
	private double alpha = .0001;
	private double discount = .9;
	private double epsilon = .02;
	private int step;
	private int lastEvent = -10;
	private int maxStepInterval = 10;
	private Map<Integer, Integer> footmenTargets;
	private StateView currentState;
	private String savedFile = "save.ser";
	
	public QLearningAgent(int playernum, String[] arguments) {
		super(playernum);
		learningEpisodes = Integer.parseInt(arguments[0]);
	}

	@Override
	public Map<Integer, Action> initialStep(StateView newstate, History.HistoryView statehistory) {
		step = -1;
		footmenTargets = new HashMap<Integer, Integer>();
		currentState = newstate;
		weights = generateWeights();
		//loadData();
		return middleStep(newstate, statehistory);
	}

	@Override
	public Map<Integer,Action> middleStep(StateView newState, History.HistoryView statehistory) {
		step++;
		currentState = newState;
		double reward = getReward(statehistory);
		updateWeights(reward);
		boolean reassignActions = eventOccured(statehistory);
		Map<Integer,Action> actions = new HashMap<Integer, Action>();
		if(reassignActions){
			actions = getFootmenActions();
			lastEvent = step;
		}
		cumulativeReward += reward;
		return actions;
	}

	// Return the map of the actions to take for each friendly footman.
	private Map<Integer, Action> getFootmenActions(){
		Map<Integer, Action> actions = getFootmenActions(getFootmen(playernum), getFootmen(getEnemyId()));
		return actions;
	}
	
	private Map<Integer, Action> getFootmenActions(List<UnitView> friendlies, List<UnitView> enemies){
		Map<Integer, Action> actions = new HashMap<Integer, Action>();
		footmenTargets = new HashMap<Integer, Integer>();
		double sumReward = 0;
		for(UnitView friendly : friendlies){
			UnitView bestEnemy = null;
			double bestReward = 0;
			for(UnitView enemy : enemies){
				if(bestEnemy == null){
					bestEnemy = enemy;
					bestReward = Q(friendly, enemy);
				}
				else{
					double reward = Q(friendly, enemy);
					if(reward > bestReward){
						bestReward = reward;
						bestEnemy = enemy;
					}
				}
			}
			sumReward += bestReward;
			Random generator = new Random();
			double probability = generator.nextDouble();
			UnitView enemy = bestEnemy;
			enemy = randomUnit(enemies);
			actions.put(friendly.getID(), Action.createCompoundAttack(friendly.getID(), enemy.getID()));
			footmenTargets.put(friendly.getID(), enemy.getID());
		}
		totalPredictedReward = sumReward;
		return actions;
	}
	
	private double Q(UnitView friendly, UnitView enemy){
		return Learner.Q(currentState, footmenTargets, friendly.getID(), enemy.getID(), weights, 0);
	}
	
	private UnitView randomUnit(List<UnitView> units){
		Random generator = new Random();
		return units.get(generator.nextInt(units.size()));
	}
	
	// Gets the reward for the new state
	private double getReward(History.HistoryView stateHistory){
		double reward = 0;
		List<DeathLog> friendlyDeaths = getDeaths(stateHistory, playernum);
		List<DeathLog> enemyDeaths = getDeaths(stateHistory, getEnemyId());
		List<DamageLog> friendlyDamages = getDamage(stateHistory, playernum);
		List<DamageLog> enemyDamages = getDamage(stateHistory, getEnemyId());
		
		for(DeathLog friendlyDeath : friendlyDeaths){
			reward -= 100;
		}
		
		for(DeathLog enemyDeath : enemyDeaths){
			reward += 100;
		}
		
		for(DamageLog friendlyDamage : friendlyDamages){
			reward -= friendlyDamage.getDamage();
		}
		
		for(DamageLog enemyDamage : enemyDamages){
			reward += enemyDamage.getDamage();
		}
		
		reward -= .1;
		return reward;
	}
	
	private List<DeathLog> getDeaths(HistoryView stateHistory, int playerNumber){
		List<DeathLog> deaths = new ArrayList<DeathLog>();
		List<DeathLog> allDeaths = stateHistory.getDeathLogs(step-1);
		
		for(DeathLog log : allDeaths){
			if(log.getController() == playerNumber){
				deaths.add(log);
			}
		}
		return deaths;
	}
	
	private List<DamageLog> getDamage(HistoryView stateHistory, int playerNumber){
		List<DamageLog> damages = new ArrayList<DamageLog>();
		List<DamageLog> allDamages = stateHistory.getDamageLogs(step-1);
		
		for(DamageLog log : allDamages){
			if(log.getDefenderController() == playerNumber){
				damages.add(log);
			}
		}
		return damages;
	}
	private void updateWeights(double reward){
		for(int i = 0; i< weights.length; i++){
			weights[i] = weights[i] + alpha*(reward - totalPredictedReward);
		}
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
	
	private double[] generateWeights(){
		double[] w = new double[6];
		Random generator = new Random();
		for(int i = 0; i<w.length; i++){
			w[i] = (generator.nextDouble() -.5)*2;
		}
		return w;
	}
	
	private List<UnitView> getFootmen(int playerNumber){
		List<UnitView> units = currentState.getUnits(playerNumber);
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
	
	private void printWeights(double[] w){
		System.out.println("Weights:");
		for(int i = 0; i<w.length; i++){
			System.out.println(w[i]);
		}
	}
	
	@Override
	public void terminalStep(StateView newstate, History.HistoryView statehistory) {
		step++;
		/*try{
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(savedFile));
			savePlayerData(out);
			out.flush();
			out.close();
		}
		catch(Exception e){
			writeLineVisual("There was a problem saving the file. Saving could not be completed at this time");
		}*/
		
		if(logger.isLoggable(Level.FINE))
		{
			logger.fine("Congratulations! You have finished the task!");
		}
		if(episode>= learningEpisodes){
			System.exit(0);
		}
		else if(learningEpisodesInterval >0){
			alpha = .0001;
			learningEpisodesInterval--; 
			episode++;
		}
		else if(testingEpisodesInterval > 0){
			alpha = 0;
			testingEpisodesInterval--;
			totalCumulativeReward += cumulativeReward;
		}
		else{
			System.out.println(episode + ": " + totalCumulativeReward/5);
			totalCumulativeReward = 0;
			learningEpisodesInterval = 10;
			testingEpisodesInterval = 5;
		}
		cumulativeReward = 0;
	}

	public static String getUsage() {
		return "Gathers 2000 gold losing as few peasants as possible";
	}
	
	@Override
	public void savePlayerData(OutputStream os) {
		try{
			ObjectOutputStream o = (ObjectOutputStream) os;
			o.writeObject(weights);
			o.writeDouble(epsilon);
			o.writeDouble(cumulativeReward);
			o.writeInt(episode+1);
		}
		catch(Exception ex){
			writeLineVisual("There was a problem writing to the output file. Saving could not be completed at this time");
		}
	}
	
	private void loadData(){
		try{
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(savedFile));
			loadPlayerData(in);
			in.close();
		}
		catch(Exception ex){
			writeLineVisual("Could not find any saved file.");
		}
	}
	
	@Override
	public void loadPlayerData(InputStream is) {
		try{
			ObjectInputStream in = (ObjectInputStream)is;
			weights = (double[])in.readObject();
			epsilon = (double)in.readDouble();
			cumulativeReward = (double)in.readDouble();
			episode = (int)in.readInt();
		}
		catch(Exception ex){
			writeLineVisual("There was a problem loading the saved file. The file could not be loaded");
		}
	}
}
