package distributed.plugin.random;

import java.util.Random;

public class Uniform implements IRandom {
	
	private static Uniform INSTANCE;
	
	private Random rand;
	
	private Uniform() {
		super();
		this.rand = new Random();
	}
	private Uniform( long seed ) {
		super();
		rand = new Random( seed );
	}

	public static synchronized IRandom getInstance( long seed ){
		if(INSTANCE == null){
			INSTANCE = new Uniform( seed );
		}
		return INSTANCE;
	}
	
	@Override
	public int nextInt( double n ) {
		return rand.nextInt((int)n);
	}

	@Override
	public void setSeed( long seed ) {
		rand.setSeed( seed );

	}

}
