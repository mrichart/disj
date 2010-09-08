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
public interface IBoardModel extends IDistributedModel{
	
	/**
	 * Method invoke when an agent arrives at a host
	 * 
	 * @param incomingPort is a port that the agent entering
	 */
	public abstract void arrive(String incomingPort);
	
	
	/**
	 * Moving an agent into a given out-going port of a current node that an agent
	 * resides
	 * 
	 * @param port an outgoing port of current node that agent resides
	 * @throws IllegalArgumentException if the port does not exist
	 */
	public void moveTo(String port);
	
	/**
	 * Retrieve a memory storage for this agent with a max length set
	 * by user in GraphEditor before an execution of the simulator
	 * 
	 * TODO not yet implemented for input max length
	 * 
	 * @return An array of String object
	 * 
	 */
	public String[] getMemorySuitcase();
	
	/**
	 * Get a maximum number of memory slot that agent can carry
	 * 
	 * @return a positive integer
	 */
	public int getMaxMemorySlot();
	
	/**
	 * Get agent ID
	 * 
	 * @return agent ID in String format
	 */
	public String getAgentId();
	
	/**
	 * Get a node ID that agent currently resides
	 * 
	 * @return node ID in String format
	 */
	public String getNodeId();
	
	/**
	 * Get a home node ID that agent has been initiated
	 * 
	 * @return node Id in String format
	 */
	public String getHomeId();
	
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
	public void appendTo(String info);
	
	/**
	 * Delete a given record from a board
	 * 
	 * @param info a data record that written on a board and would like
	 * to be removed
	 *  
	 * @return true if the data found and successfully remove otherwise false
	 */
	public boolean remove(String info);
	
	/**
	 * Get a state of a node that agent currently resides
	 * 
	 * @return A state of a node at current time
	 */
	public int getNodeState();
	
	/**
	 * Set a state of current host node
	 * 
	 * @param state A new state of this host
	 */
	public void setNodeState(int state);
}
