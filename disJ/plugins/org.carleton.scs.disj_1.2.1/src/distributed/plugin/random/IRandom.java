package distributed.plugin.random;

public interface IRandom {
	
	/**
	 * Get a random positive integer between [0-n)
	 * 
	 * @param n An upper bound (exclusive) positive integer number
	 * @return A positive number between 0 inclusive and n exclusive
	 */
	public int nextInt(int n);
	
	/**
	 * Sets the seed of this random number generator using a single long seed.  
	 * @param seed
	 */
	public void setSeed(long seed);
	
}
