package distributed.plugin.runtime.engine;

import java.util.ArrayList;
import java.util.List;

import distributed.plugin.core.Agent;
import distributed.plugin.core.IConstants;
import distributed.plugin.core.Node;
import distributed.plugin.runtime.IBoardModel;

public abstract class BoardAgent implements IBoardModel {

	private int curState;
	
	private transient Agent agentOwner;
	
	private BoardAgentProcessor processor;
	
	protected BoardAgent(int state){
		this.curState = state;
		this.processor = null;
		this.agentOwner = null;
	}
	
	/**
	 * Initialize all necessary references for an agent
	 * 
	 * @param agentId An agent unique ID
	 * @param processor A simulation processor where an agent
	 * 					belong
	 */
	void initAgent(Agent agent, BoardAgentProcessor processor) {
		if(this.agentOwner == null)
			this.agentOwner = agent;
		
		if (this.processor == null)
			this.processor = processor;		
	}
	
	//public abstract void arrive(String incomingPort);

	@Override
	public final String[] getMemorySuitcase() {
		return this.agentOwner.getInfo();
	}
	
	@Override
	public final String getAgentId(){
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
		
		this.processor.processReqeust(this.getNodeId(), recv,
				new Message(IConstants.MESSAGE_EVENT_MOVE_TO, this.getAgentId()));					
	}

	@Override
	public final List<String> readFrom() {
		return this.agentOwner.readFromBoard();
	}

	@Override
	public final boolean remove(String info) {		
		return this.agentOwner.removeFromBoard(info);
	}

	@Override
	public final void appendTo(String info) {
		this.agentOwner.appendToBoard(info);
	}

	@Override
	public final int getState() {
		return this.curState;
	}

	@Override
	public final void become(int state) {
		this.curState = state;
	}

	public final int getNodeState(){
		return this.agentOwner.getCurNode().getCurState();
	}
	
	@Override
	public final void setNodeState(int state){
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
			
			// send request to process
			this.processor.processReqeust(this.getNodeId(), receivers,
					new Message(IConstants.MESSAGE_SET_ALARM_CLOCK, msg));
		}		
	}
	
	@Override
	public final String getHomeId(){
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
}
