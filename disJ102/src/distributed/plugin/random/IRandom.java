package distributed.plugin.random;

public interface IRandom {
	
	public int nextInt( double n );
	public void setSeed( long seed );
	
}
