package distributed.plugin.runtime;

import distributed.plugin.core.IConstants;

@SuppressWarnings("serial")
public class AgentEvent extends Event {

	enum actionType {WRITE_TO_BOARD, READ_FROM_BOARD, REMOVE_FROM_BOARD, 
		DROP_TOKEN, PICK_TOKEN;
	}
	
	private String targetNodeId;
	
	private String agentId;
	
	/*
	 * Holding event info of {action_type : data require for action}
	 * e.g. write to board {write_board: "A was here"}
	 * 		drop token {numToken: location}
	 *      move to port {move_out: port_label}
	 *      init {init: execTime}
	 *      set alarm {alarm: alarm Time}
	 */
	private IMessage info;
	
	
	public AgentEvent(short eventType, int eventId, int execTime, 
			String targetId, String agentId, IMessage info) {
		super(eventType, eventId, execTime);

		this.targetNodeId = targetId;
		this.agentId = agentId;
		this.info = info;

	}
	
	/*
	 * Allow internally modify info (by Adversary)
	 */
	public void setMessage(IMessage msg){
		// TODO do nothing here
	}

	public String getNodeId() {
		return targetNodeId;
	}

	public String getAgentId() {
		return agentId;
	}

	public IMessage getInfo() {
		return info;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof AgentEvent))
			return false;

		AgentEvent e = (AgentEvent) obj;
		return (e.getNodeId().equals(this.targetNodeId) 
				&& e.getAgentId() == this.agentId
				&& e.getExecTime() == this.getExecTime() 
				&& e.getEventType() == this.getEventType());
	}

	public String toString() {
		return super.toString()+ this.targetNodeId + IConstants.MAIN_DELIMETER
				+ this.agentId + IConstants.MAIN_DELIMETER + info.getLabel();
	}

	

}
