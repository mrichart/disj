package distributed.plugin.runtime.adversary;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import distributed.plugin.core.Edge;
import distributed.plugin.core.IConstants;
import distributed.plugin.core.Node;
import distributed.plugin.runtime.AgentEvent;
import distributed.plugin.runtime.Event;
import distributed.plugin.runtime.engine.AgentProcessor;

public abstract class AgentControl extends AbstractControl {
		
	/**
	 * Allow adversary to specify the time of initiation of 
	 * a given agent ID
	 * 
	 * Note: A returning time MUST be more than a current time, 
	 * otherwise a current time will be returned
	 */
	public int initTimeControl(String agentId){
		// do nothing here, use default value,
		// up to adversary to implement
		
		return this.getCurrentTime();
	}
	
	/**
	 * Allow adversary to control and manipulate actions of a node before 
	 * a message arrives at the node. Also allows the adversary to override
	 * any blocking/unblocking that may occur to the message at the node	 
	 * 
	 * Note: It is adversary duty to make sure any blocked message will be 
	 * 		unblocked
	 * 
	 * @param agentId An ID of an agent that arrives
	 * @param edgeId An ID of an edge that agent is traveling
	 * @param nodeId An ID of a destination node
	 * 
	 * @return BlockFlag.PASS if this agent must arrive now, 
	 * 		   BlockFlag.BLOCK if the agent has to be blocked,
	 * 		   BlockFlag.DEFAULT no overriding
	 */
	public abstract BlockFlag arrivalControl(String agentId, String edgeId, String nodeId);

	
	/**
	 * Allow adversary to configure an arrival time of agent to 
	 * a destination before it enters an edge
	 * 
	 * Note: A returning time MUST be more than a current time, 
	 * otherwise a current time + 1 will be returned. 
	 * 
	 * @param agentId An ID of agent that is entering
	 * @param edgeId An ID of a link that will be used by agent
	 * @param nodeId An ID of a node destination of a agent
	 * 
	 * @return A simulation time that the agent will be arrived at the node
	 */
	public int arrivalTimeControl(String agentId, String edgeId, String nodeId) {
		Node sender;
		int delay = 0;
		int curTime = this.getCurrentTime();
		Edge edge = this.getEdge(edgeId);
		sender = edge.getOthereEnd(this.getNode(nodeId));
		delay = edge.getDelayTime(sender, curTime);
		return delay;
	}

	/**
	 * Allow adversary to configure the lost of agent that will
	 * traveling on an edge to a node. This drop will be determined 
	 * right after the agent entered the edge
	 * 
	 * @param agentId An ID of agent
	 * @param edgeId An ID of a link that agent is traveling through
	 * @param nodeId An ID of a destination node of the agent
	 * @return True if the agent will die before arrive at a node,
	 * otherwise false
	 */
	public boolean dropControl(String agentId, String edgeId, String nodeId) {
		Edge edge = this.getEdge(edgeId);
		if(!edge.isReliable()){
			int prob = this.getRandomGen().nextInt(100);
			return (prob <= edge.getProbOfFailure());
		}else{
			return false;
		}
	}
	
	/**
	 * Get a list of agents(with expected arrival time) currently in 
	 * a link and heading to a given destination node
	 * 
	 * @param edgeId An ID of a link
	 * @param nodeId An ID of a destination node
	 * @return a list of agents with expected arrival time if exist, 
	 * otherwise empty list is returned
	 */
	public final Map<String, Integer> getTravelingAgents(String edgeId, String nodeId){
		Map<String, Integer> tmp = ((AgentProcessor)this.proc).getMovingAgents(edgeId, nodeId);
		return tmp;
	}
	
	/**
	 * Allow adversary to block a given agent at a given port of a node
	 * 
	 * @param agentId An ID of agent that want to block
	 * @param incomingPort A port that want to block
	 * @param nodeId An ID of a node that wants to block
	 */
	public final void blockAgent(String agentId, String incomingPort, String nodeId){
		Node recv = this.getNode(nodeId);
		if(this.isInPortExist(nodeId, incomingPort)){
			recv.blockVisitor(agentId, incomingPort);
		}else{
			throw new IllegalArgumentException("@blockmsg() an in-port "
					+ incomingPort + " does not exist for node ID " + nodeId);
		}
	}
	
	/**
	 * Allow adversary to unblock an agent at given port and node.	
	 * 
	 * @param agentId An ID of agent that want to unblock
	 * @param incomingPort A port that want to unblock
	 * @param nodeId An ID of a node that wants to unblock
	 */
	public final void unblockAgent(String agentId, String incomingPort, String nodeId){
		Node recv = this.getNode(nodeId);
		
		if(!this.isInPortExist(nodeId, incomingPort)){
			throw new IllegalArgumentException("@unblockMsg() an in-port "
					+ incomingPort + " does not exist for node ID " + nodeId);
		}
		
		recv.unblockVisitor(agentId, incomingPort);
		List<Event> events = recv.getBlockedEvents(incomingPort);
		List<Event> out = new ArrayList<Event>();
		Iterator<Event> its = events.iterator();
		
		// put them in a queue with the current execution time
		int time = this.getCurrentTime();
		for (AgentEvent e = null; its.hasNext(); ){
			e = (AgentEvent)its.next();
			if(e.getEventType() == IConstants.EVENT_ARRIVAL_TYPE){
				if(e.getAgentId().equals(agentId)){
					events.remove(e);
					e.setExecTime(time);
					out.add(e);					
				}
			}
		}
		// pump it to the processor
		this.proc.pushEvents(out);
	}
	
	/**
	 * Check whether a given agent is blocked at a given port and node
	 * 
	 * @param agentId An ID of agent
	 * @param incomingPort An incoming port of the node
	 * @param nodeId An ID of a node that is checking
	 * @return True if the port exists and it blocks arrival agent with
	 * a given ID, otherwise false
	 */
	public final boolean isAgentBlocked(String agentId, String incomingPort, String nodeId) {
		Node recv = this.getNode(nodeId);
		return recv.isVisitorBlocked(agentId, incomingPort);
	}
	
	/**
	 * Count current number of agent that has been initiated and still alive in 
	 * the environment
	 * 
	 * @return a number of agent that is alive and has been initiated
	 */
	public final int getTotalAliveAgent(){
		return ((AgentProcessor)this.proc).countAliveAgent();
	}
}
