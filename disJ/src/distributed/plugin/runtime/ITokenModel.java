package distributed.plugin.runtime;

import java.util.List;

/**
 * Token model API for communication interface of agents and nodes
 * in distributed agent environment
 *  
 * @author rpiyasin
 *
 */
public interface ITokenModel {


	/**
	 * Initialize this entity if it is set as initializer entity
	 * 
	 */
	public abstract void init();

	
	/**
	 * Get a current internal state of this entity
	 *
	 */
	public int getState() ;
	
	/**
	 * Set an internal state of an entity. The default value at the beginning is 0
	 * 
	 * @param state
	 */
	public void become(int state) ;
	
	/**
	 * Method invoke when an internal alarm clock of this entity is ring
	 * 
	 */
	public abstract void alarmRing();

	/**
	 * Set an alarm internal clock time interval, The clock will ring after
	 * a given time from the time that is set.
	 * 
	 * @param time An offset of waiting time (positive integer)
	 * 
	 */
	public void setAlarm(int time) ;
	
	/**
	 * Moving an agent into a given out-going port of a current node that an agent
	 * resides
	 * 
	 * @param port an outgoing port of current node that agent resides
	 * @throws IllegalArgumentException if the port does not exist
	 */
	public void moveTo(String port);
	
	/**
	 * Retrieve data stored in an agent with respect to a given index of slot 
	 * that stores data
	 * 
	 * @param index a slot index
	 * @return A data contains in a slot if exist otherwise null
	 * @throws IndexOutOfBoundsException if the slot index does not exist
	 * 
	 */
	public String getInfo(int index);
	
	/**
	 * Retrieve all data that currently stored in an agent memory slots 
	 * 
	 * @return A list of data if exist, otherwise an empty list returned 
	 * @throws IndexOutOfBoundsException if the slot index does not exist
	 * 
	 */
	public List<String> getAllInfo();
	
	/**
	 * Write data, in String format, into a slot with respect to a given slot index
	 * 
	 * Note: if there exist data in a given slot index, a new data will override
	 * the old data.
	 * 
	 * @param index a slot index
	 * @param data info to store in a slot
	 * @throws IndexOutOfBoundsException if the slot index does not exist
	 * 
	 */
	public void recordInfo(int index, String data);
	
	/**
	 * Get a maximum number of slot that agent can have
	 * 
	 * @return a positive integer
	 */
	public int getMaxSlot();
	
	/**
	 * Get a node (host) ID that agent currently resides
	 * 
	 * @return node ID in String format
	 */
	public String getHostId();
	
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
	 * Pick up token(s) located at current host
	 * 
	 * @param amount a number of token to pick up
	 * @throws IllegalArgumentException if number of token planing to 
	 * pick is more than number of token located at the host. Therefore,
	 * there will be no token is picked up by an agent.
	 */
	public void pickupToken(int amount);
	
}
