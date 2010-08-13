package distributed.plugin.runtime;

import java.util.List;

/**
 * Black/White board model API for communication interface of
 * agents, boards and nodes in distributed agent environment
 * 
 * Note: An agent is meant to use memory slot only as an internal 
 * storage that stay with agent and can be carried to difference nodes. 
 * 
 * Any external memory storage e.g. Java Collections, arrays etc. are 
 * for local computing only and SHOULD not be used as Class instance variable
 * in which it breaks the model because it can replace memory slots. 
 * Beside these instance variable storage will not be include in statistic 
 * report and analysis.
 * 
 * @author rpiyasin
 *
 */
public interface IBoardModel {


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
	 * Read info that posted on a board of current host
	 * 
	 * @return a current list of record (in a String format) that currently
	 * posted (if exist), otherwise an empty list is returned
	 */
	public List<String> readFrom();
	
	/**
	 * Append info to the end of a board of current host
	 * 
	 * Note: A new entry will be appended into the end of the list of records,
	 * if the list is full, the oldest entry will be removed by the system
	 * before a new entry appended
	 * 
	 * @param info a String of data that will be posted on a board
	 */
	public void writeTo(String info);
	
	/**
	 * Delete a given record from a board
	 * 
	 * @param info a data record that written on a board and would like
	 * to be removed
	 *  
	 * @return true if the data successfully remove otherwise false
	 */
	public boolean remove(String info);
}
