package Assignment5;

public class ProbabilityMap{
	
	public static double[] features(StateView s, int fId, int eId){
		double[] featureArray = new double[6];
		
		//# of friendlies attacking e
    		featureArray[0]=0;
		
		//health of friendly f
    		featureArray[1]=0;

    		//health of enemy e
    		featureArray[2]=0;

    		//is  e the enemy f is attacking
    		featureArray[3]=0;

    		//is e the closest to f
    		featureArray[4]=0;

    		//is f the closet to e
    		featureArray[5]=0;
		
		return featureArray;
	}

	public double friendlysAttackingE(StateView s, int fId, int eId){}
	public double healthOfF(StateView s, int fId, int eId){{
	public double HealthOfE(StateView s, int fId, int eId){}
	public double fAttackingE(StateView s, int fId, int eId){}
	public double EclosestEnemy(StateView s, int fId, int eId){}
	public double FclosestFriendly(StateView s, int fId, int eId){}
	
	public static double reward(int enemiesKilled, int friendliesKilled, int friendlyDamage, int enemyDamage, int moves_required){
		return enemiesKilled*100 - friendliesKilled*100 + enemyDamage - friendlyDamage - ((double)moves_required)*.01;
	}

	public static double Q(StateView s, int fId, int eId, double[] weights, double[] ogWeights){
		//Q(s, a) = w * f(s, a) +w0
		double q = dotProduct(features(s, fId, eId), weights) + ogWeights;
		return q;
	}
	public static double dotProduct(double[] a, double[] b){
		if( a.size != b.size)
			return 0;
		int prod = 0;
		for(int i = 0; i<a.size; i++){
			prod += a[i]*b[i];
		return prod;
	}
}
