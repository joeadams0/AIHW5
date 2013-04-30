package Assignment5;

import edu.cwru.sepia.environment.model.state.State.StateView;

public class Learner{
	
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

	public static double reward(StateView s, int fId, int eId){
		return 0.0;
	}

	public static double Q(StateView s, int fId, int eId, double[] weights, double ogWeights){
		//Q(s, a) = w * f(s, a) +w0
		//double q = dotProduct(features(s, fId, eId), weights) + ogWeights;
		return 0.0;
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
