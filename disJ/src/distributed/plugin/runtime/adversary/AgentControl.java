package distributed.plugin.runtime.adversary;

import java.util.Map;
import java.util.Random;

import distributed.plugin.core.Edge;
import distributed.plugin.core.Node;
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
	 * Allow adversary to control and manipulate incoming agent
	 * to a node. This will be called right before it entering
	 * a port of a node
	 * 
	 * @param agentId An ID of agent that is arriving
	 * @param arrivalPort A port that agent is entering through
	 * @param nodeId An ID of a destination node
	 */
	public void arrivalControl(String agentId, String arrivalPort, String nodeId){
		// do nothing here
		// up to adversary to implement
	}
	
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
	public int setArrivalTime(String agentId, String edgeId, String nodeId) {
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
	public boolean setDrop(String agentId, String edgeId, String nodeId) {
		Random ran = new Random(System.currentTimeMillis());
		Edge edge = this.getEdge(edgeId);
		if(!edge.isReliable()){
			int prob = ran.nextInt(100) + 1;
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
	protected final Map<String, Integer> getTravelingAgents(String edgeId, String nodeId){
		Map<String, Integer> tmp = ((AgentProcessor)this.proc).getMovingAgents(edgeId, nodeId);
		return tmp;
	}
	
}
