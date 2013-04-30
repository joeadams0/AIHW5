package Assignment5;

import edu.cwru.sepia.environment.model.state.State.StateView;

public class Learner{
	
	public static double[] features(StateView s, int fId, int eId){
		double[] featureArray = new double[6];
		
		//# of friendlies attacking e
    		featureArray[0]=friendlysAttackingE(s, fId, eId);
		
		//health of friendly f
    		featureArray[1]=healthOfE(s, fId, eId);

    		//health of enemy e
    		featureArray[2]=healthOfF(s, fId, eId);

    		//is  e the enemy f is attacking
    		featureArray[3]=fAttackingE(s, fId, eId);

    		//is e the closest to f
    		featureArray[4]=EclosestEnemy(s, fId, eId);

    		//is f the closet to e
    		featureArray[5]=FclosestFriendly(s, fId, eId);
		
		return featureArray;
	}

	public static double friendlysAttackingE(StateView s, int fId, int eId){
		return 0;
	}
	public static double healthOfF(StateView s, int fId, int eId){
		return 0;
	}
	public static double healthOfE(StateView s, int fId, int eId){
		return 0;
	}
	public static double fAttackingE(StateView s, int fId, int eId){
		return 0;
	}
	public static double EclosestEnemy(StateView s, int fId, int eId){
		return 0;
	}
	public static double FclosestFriendly(StateView s, int fId, int eId){
		return 0;
	}
	
	public static double reward(int enemiesKilled, int friendliesKilled, int friendlyDamage, int enemyDamage, int moves_required){
		return enemiesKilled*100 - friendliesKilled*100 + enemyDamage - friendlyDamage - ((double)moves_required)*.01;
	}

	public static double Q(StateView s, int fId, int eId, double[] weights, double ogWeights){
		//Q(s, a) = w * f(s, a) +w0
		double q = dotProduct(features(s, fId, eId), weights) + ogWeights;
		return q;
	}
	
	public static double dotProduct(double[] a, double[] b){
		if( a.length != b.length)
			return 0;
		double prod = 0;
		for(int i = 0; i<a.length; i++){
			prod += a[i]*b[i];
		}
		return prod;
	}
}
