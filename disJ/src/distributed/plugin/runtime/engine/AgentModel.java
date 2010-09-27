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
	
	int curState;

	transient Agent agentOwner;

	IProcessor processor;

	MessageConsoleStream systemOut;
	
	protected AgentModel(int state) {
		this.curState = state;
		this.processor = null;
		this.agentOwner = null;
	}

	/**
	 * Initialize all necessary references for an agent
	 * 
	 * @param agentId
	 *            An agent unique ID
	 * @param processor
	 *            A simulation processor where an agent belong
	 */
	void initAgent(Agent agent, IProcessor processor) {
		if (this.agentOwner == null)
			this.agentOwner = agent;

		if (this.processor == null){
			this.processor = processor;
			this.systemOut = this.processor.getSystemOut();
		}
	}

	// public abstract void arrive(String incomingPort);

	@Override
	public final String[] getMemorySuitcase() {
		return this.agentOwner.getInfo();
	}

	@Override
	public final String getAgentId() {
		return this.agentOwner.getAgentId();
	}

	@Override
	public final String getNodeId() {
		return this.agentOwner.getCurNode().getNodeId();
	}

	@Override
	public final int getMaxMemorySlot() {
		return this.agentOwner.getMaxSlot();
	}

	@Override
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

	@Override
	public final int getState() {
		try {
			if (this.agentOwner == null)
				throw new DisJException(IConstants.ERROR_7);
		} catch (Exception e) {
			e.printStackTrace();
			this.systemOut.println("@getState() " + e.toString());
		}
		return this.curState;
	}

	@Override
	public final void become(int state) {
		try {
			if (this.agentOwner == null)
				throw new DisJException(IConstants.ERROR_7);

			this.curState = state;
			this.agentOwner.setCurState(this.curState);
		} catch (Exception e) {
			e.printStackTrace();
			this.systemOut.println("@become() " + e.toString());
		}
	}

	public final int getNodeState() {
		return this.agentOwner.getCurNode().getCurState();
	}

	@Override
	public final void setNodeState(int state) {
		Node node = this.agentOwner.getCurNode();
		node.setCurState(state);
	}

	@Override
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

	@Override
	public final String getHomeId() {
		return this.agentOwner.getHomeId();
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
}
