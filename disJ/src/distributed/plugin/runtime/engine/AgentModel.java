package distributed.plugin.runtime.engine;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.console.MessageConsoleStream;

import distributed.plugin.core.Agent;
import distributed.plugin.core.DisJException;
import distributed.plugin.core.IConstants;
import distributed.plugin.core.Node;
import distributed.plugin.runtime.IAgentModel;
import distributed.plugin.runtime.IProcessor;
/**
 * A base agent model that can move inside a network that connected
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
public abstract class AgentModel implements IAgentModel {

	/**
	 * A notification type for client to register event that may
	 * occur on a current host node
	 */
	public enum NotifyType{TOKEN_UPDATE, BOARD_UPDATE, 
		AGENT_ARRIVAL, AGENT_DEPARTURE};
	
	int initState;

	transient Agent agentOwner;

	IProcessor processor;

	MessageConsoleStream systemOut;
	
	protected AgentModel(int state) {
		this.initState = state;
		this.processor = null;
		this.agentOwner = null;
	}

	/**
	 * Initialize all necessary references for an agent
	 * 
	 * @param isOrg 
	 * 			  A flag is True if an agent is not a duplicate agent
	 * 				otherwise False
	 * @param agent
	 *            An agent
	 * @param processor
	 *            A simulation processor where an agent belong
	 */
	void initAgent(boolean isOrg, Agent agent, IProcessor processor) {
		if (this.agentOwner == null){
			this.agentOwner = agent;
			if(isOrg){
				this.agentOwner.setCurState(this.initState);
			}
		}

		if (this.processor == null){
			this.processor = processor;
			this.systemOut = this.processor.getSystemOut();
		}
	}

	// public abstract void arrive(String incomingPort);

	public final String[] getMemorySuitcase() {
		return this.agentOwner.getInfo();
	}

	/**
	 * Get this agent Name
	 */
	public final String getAgentId() {
		return this.agentOwner.getName();
	}

	/**
	 * Get a node name that this agent currently resides
	 */
	public final String getNodeId() {
		return this.agentOwner.getCurNode().getName();
	}

	/**
	 * Get a max number of suitcase memory size that
	 * this agent can carry
	 */
	public final int getMaxMemorySlot() {
		return this.agentOwner.getMaxSlot();
	}

	/**
	 * Command this agent to exit at a given outgoing port
	 * of a node that this agent is currently resides.
	 * 
	 * @param outPort an outgoing port of a node that this agent
	 *       is currently resides
	 */
	public final void moveTo(String outPort) {
		// add destination
		List<String> recv = new ArrayList<String>();
		recv.add(outPort);

		try{
			this.processor.processReqeust(this.getNodeId(), recv, new Message(
					IConstants.MESSAGE_EVENT_MOVE_TO, this.getAgentId()));
		}catch(Exception e){
			throw new IllegalStateException("@moveTo() " + e.toString());
		}
	}

	/**
	 * Get current state of this agent
	 */
	public final int getState() {
		try {
			if (this.agentOwner == null)
				throw new DisJException(IConstants.ERROR_7);
		} catch (Exception e) {
			e.printStackTrace();
			this.systemOut.println("@getState() " + e.toString());
		}
		return this.agentOwner.getCurState();
	}

	/**
	 * Set current state of this agent to be a given state
	 * 
	 * @param state a new state
	 */
	public final void become(int state) {
		try {
			if (this.agentOwner == null)
				throw new DisJException(IConstants.ERROR_7);

			this.agentOwner.setCurState(state);
		} catch (Exception e) {
			e.printStackTrace();
			this.systemOut.println("@become() " + e.toString());
		}
	}

	/**
	 * Get a current state of a node that this agent currently 
	 * resides
	 */
	public final int getNodeState() {
		return this.agentOwner.getCurNode().getCurState();
	}

	/**
	 * Set state of a node that this agent currently resides
	 * 
	 * @param state a new state
	 */
	public final void setNodeState(int state) {
		Node node = this.agentOwner.getCurNode();
		node.setCurState(state);
	}

	/**
	 * Set an internal alarm clock of this agent to ring
	 */
	public final void setAlarm(int time) {
		if (time > 0) {
			// No receiver
			List<String> receivers = new ArrayList<String>();

			// Create action required parameters
			int[] msg = new int[2];
			msg[0] = Integer.parseInt(this.agentOwner.getAgentId());
			msg[1] = time;

			try{
				// send request to process
				this.processor.processReqeust(this.getNodeId(), receivers,
						new Message(IConstants.MESSAGE_SET_ALARM_CLOCK, msg));
			}catch(Exception e){
				throw new IllegalStateException("@setAlarm() " + e.toString());
			}
		}
	}

	/**
	 * Get a home base name of this agent
	 */
	public final String getHomeId() {
		return this.agentOwner.getHomeName();
	}

	/**
	 * Get all outgoing port label of current node
	 * 
	 * @return A list of String all out going port labels
	 */
	public final List<String> getOutPorts() {
		return this.agentOwner.getOutPorts();
	}

	/**
	 * Get all incoming port labels of current node
	 * 
	 * @return A list of String of all incoming port labels
	 */
	public final List<String> getInPorts() {
		return this.agentOwner.getInPorts();
	}
	
	/**
	 * Register a given event type for this agent
	 * at current node
	 * @param type
	 */
	public final void registerHostEvent(NotifyType type){
		Node node = this.agentOwner.getCurNode();
		if(node.isAlive()){
			node.addRegistee(this.getAgentId(), type);
		}
	}
	
	/**
	 * Remove a given event type done by this agent
	 * at current node
	 * @param type
	 */
	public final void unregisterHostEvent(NotifyType type){
		Node node = this.agentOwner.getCurNode();
		if(node.isAlive()){
			node.removeRegistee(this.getAgentId(), type);
		}
	}
	
	/**
	 * Remove all registered events done by this agent
	 * at current node
	 */
	public final void unregisterHostAllEvents(){
		Node node = this.agentOwner.getCurNode();
		if(node.isAlive()){
			node.removeRegistee(this.getAgentId());
		}
	}
	
	/*
	 * Generate notify event for agents
	 */
	void notifyEvent(NotifyType type){
		// notify updated event
		try{
			this.processor.processReqeust(this.getNodeId(), new ArrayList<String>()
					, new Message(IConstants.MESSAGE_EVENT_NOTIFY, type));
		}catch(Exception e){
			throw new IllegalStateException("@notifyEvent() " + e.toString());
		}
	}
	
	/**
	 * Get a size of network that attached to the simulation
	 * @return A number of node in the network
	 */
	public final int getNetworkSize(){
		return this.processor.getNetworkSize();
	}
	
	/**
	 * Create a replica of this agent and present it at a node that
	 * this agent is currently resides.
	 * 
	 * The replica agent is indistinguishable with this agent
	 * at the time it created except an agent name
	 * 
	 * The replica agent is activate right after it has been created
	 * by performing an initialize function init()
	 * 
	 * @throws DisJException
	 */
	public final void duplicate() {
		try {
			((AgentProcessor)this.processor).duplicateAgent(this.agentOwner);
		} catch (DisJException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * Get a node name on the another end that connects to a given port 
	 * of a current node that this agent resides
	 * 
	 * @param portLabel A given port label
	 * @return A name of a node on the another end of this node that connected
	 * via a give port label if exist, otherwise NULL is returned
	 */
	public final String getDestinationNode(String portLabel){
		Node curNode = this.agentOwner.getCurNode();
		try{
			Node desNode = curNode.getDestinationNode(portLabel);
			return desNode.getName();
		}catch(IllegalArgumentException e){
			return null;
		}
	}
}
