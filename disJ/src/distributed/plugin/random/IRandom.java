package distributed.plugin.random;

public interface IRandom {
	
	public int nextInt(int n);
	public void setSeed(long seed);
	
}
