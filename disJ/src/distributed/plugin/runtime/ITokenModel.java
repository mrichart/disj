package distributed.plugin.runtime;


/**
 * Token model API for communication interface of agents and nodes
 * in distributed agent environment
 *  
 * @author rpiyasin
 *
 */
public interface ITokenModel {
	
	/**
	 * Count token currently located at current host
	 * 
	 * @return a positive integer number
	 */
	public int countHostToken();
	
	/**
	 * Count token of agent currently holding
	 * 
	 * @return a positive integer number
	 */
	public int countMyToken();
	
	/**
	 * Get maximum number of token that agent can hold
	 * 
	 * @return a positive integer number
	 */
	public int getMaxToken();
	
	/**
	 * Drop a token at current host
	 * 
	 * @throws IllegalStateException if there is no token left for 
	 * agent to drop
	 */
	public void dropToken();
	
	/**
	 * Pick up token(s) located at current host. 
	 * 
	 * @param amount a number of token to pick up
	 * @throws IllegalArgumentException if number of token planing to 
	 * pick is more than number of token located at the host. 
	 * 
	 * Also, if the number of picking up token plus number of existing 
	 * token is more than number of max token allow
	 * 
	 * Therefore, there will be no token is picked up by an agent in 
	 * both cases
	 */
	public void pickupToken(int amount);
	
}
