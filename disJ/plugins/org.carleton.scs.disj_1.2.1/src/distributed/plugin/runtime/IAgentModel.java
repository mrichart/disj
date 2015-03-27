package distributed.plugin.runtime;

import distributed.plugin.runtime.engine.AgentModel.NotifyType;

public interface IAgentModel extends IDistributedModel {
	
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

	/**
	 * An agent that register to a host node event will get 
	 * notified when a registered NotifyType has been occurred 
	 * while the agent still resides at the host node or the
	 * NotifyType has not yet been unregistered
	 * 
	 * @param type
	 */
	public void notified(NotifyType type);
}
