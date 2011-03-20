package distributed.plugin.random;

import java.util.Random;

public class Poisson implements IRandom {
	
	private static Poisson INSTANCE;
	
	private class PoissonHelper extends Random {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		
		
		private PoissonHelper() {
			super();
		}

		private PoissonHelper( long seed ) {
			super( seed );
		}

		int nextPoisson(double lambda) {
			double elambda = Math.exp(-1*lambda);
			double product = 1;
			int count =  0;
			 int result=0;
			while (product >= elambda) {
				product *= nextDouble();
				result = count;
				count++; // keep result one behind
				}
			return result;
			}

		double nextExponential(double b) {
			double randx;
			 double result;
			randx = nextDouble();
			result = -1*b*Math.log(randx);
			return result;
		}
	}

	private PoissonHelper rand;
	
	public static synchronized IRandom getInstance( long seed ){
		if(INSTANCE == null){
			INSTANCE = new Poisson( seed );
		}
		return INSTANCE;
	}
	
	private Poisson(){
		rand = new PoissonHelper();
	}
	
	private Poisson( long seed ){
		rand = new PoissonHelper( seed );
	}
	
	public int nextInt(int n ) {
		return rand.nextPoisson(n);
	}

	public double nextExponential(double b){
		return rand.nextExponential(b);
	}
	
	public void setSeed( long seed ) {
		rand.setSeed( seed );

	}

}
