package distributed.plugin.stat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import distributed.plugin.core.Agent;
import distributed.plugin.core.Edge;
import distributed.plugin.core.IConstants;
import distributed.plugin.core.Node;
import distributed.plugin.runtime.engine.AgentModel;
import distributed.plugin.runtime.engine.TokenAgent;

@SuppressWarnings("serial")
public class GraphStat extends Statistic {
	
	private String graphId;
	
	// total number agent at start of simulation
	private int totalAgent;
	
	// total number node at start of simulation
	private int totalNode;
	
	// total number init agent at start of simulation
	private int totalInitAgent;
	
	// total number init node at start of simulation
	private int totalInitNode;
		
	/**
	 * 
	 * @param name
	 */
	public GraphStat(String name) {
		this.graphId = name;
		this.totalAgent = 0;
		this.totalNode = 0;
		this.totalInitAgent = 0;
		this.totalInitNode = 0;
	}

	@Override
	public String getName() {
		return this.graphId;
	}

	public int getTotalAgent() {
		return totalAgent;
	}

	public void setTotalAgent(int totalAgent) {
		this.totalAgent = totalAgent;
	}

	public int getTotalNode() {
		return totalNode;
	}

	public void setTotalNode(int totalNode) {
		this.totalNode = totalNode;
	}

	public int getTotalInitAgent() {
		return totalInitAgent;
	}

	public void setTotalInitAgent(int totalInitAgent) {
		this.totalInitAgent = totalInitAgent;
	}

	public int getTotalInitNode() {
		return totalInitNode;
	}

	public void setTotalInitNode(int totalInitNode) {
		this.totalInitNode = totalInitNode;
	}

	@Override
	public void reset() {

	}

	/**
	 * Get a list of every node and it current state
	 * 
	 * @return {NodeStateId, count}
	 */
	public static Map<Integer, Integer> getNodeCurStateCount(Map<String, Node> nodes){
		int state = 0;
		int c = 0;
		Map<Integer, Integer> count = new HashMap<Integer, Integer>();
		for (String id : nodes.keySet()) {
			Node n = nodes.get(id);
			state = n.getCurState();
			if(count.containsKey(state)){
				c = count.get(state);
				c++;
			}else{
				c = 1;
			}
			count.put(state, c);
		}
		return count;
	}
	
	/**
	 * Get a list state verses number of messages sent
	 * 
	 * @return {NodeStateId, count}
	 */
	public static Map<Integer, Integer> getNodeStateMsgSentCount(Map<String, Node> nodes){
		int temp = 0;
		int c = 0;
		
		// iterate through each node
		Map<Integer, Integer> count = new HashMap<Integer, Integer>();
		for (String id : nodes.keySet()) {
			Node n = nodes.get(id);
			
			// count each sent per state of a node
			Map<Integer, Integer> msg = n.getStat().getStateMsgSent();			
			for (Integer stateId : msg.keySet()) {
				temp = msg.get(stateId);
				if(count.containsKey(stateId)){
					c = count.get(stateId);
					c += temp;
				}else{
					c = temp;
				}
				count.put(stateId, c);
			}
		}
		return count;
	}
	
	
	/**
	 * Get total number of message that every nodes received
	 * 
	 * @param nodes
	 * @return
	 */
	public static int getTotalMsgRecv(Map<String, Node> nodes){
		Iterator<String> its = nodes.keySet().iterator();
		NodeStat stat = null;
		int count = 0;
		for(Node n = null; its.hasNext();){
			n = nodes.get(its.next());
			stat = n.getStat();
			count += stat.getNumMsgRecv();
		}
		return count;
	}
	
	/**
	 * Get total number of message of every nodes sent
	 * 
	 * @param nodes
	 * @return
	 */
	public static int getTotalMsgSent(Map<String, Node> nodes){
		Iterator<String> its = nodes.keySet().iterator();
		NodeStat stat = null;
		int count = 0;
		for(Node n = null; its.hasNext();){
			n = nodes.get(its.next());
			stat = n.getStat();
			count += stat.getNumMsgSent();
		}
		return count;
	}
	
	/**
	 * Get nodes that received message the most
	 * 
	 * @param nodes
	 * @return
	 */
	public static List<Node> getMaxMsgRecv(Map<String, Node> nodes){
		Iterator<String> its = nodes.keySet().iterator();
		NodeStat stat = null;
		List<Node> list = new ArrayList<Node>();
		
		// find a max number
		int max = 0;
		int tmp= 0;
		for(Node n = null; its.hasNext();){
			n = nodes.get(its.next());
			stat = n.getStat();
			tmp = stat.getNumMsgRecv();
			if(max < tmp){
				max = tmp;
			}
		}
		
		// find a node that has max number
		its = nodes.keySet().iterator();
		for(Node n = null; its.hasNext();){
			n = nodes.get(its.next());
			stat = n.getStat();
			tmp = stat.getNumMsgRecv();
			if(tmp == max){
				list.add(n);
			}
		}
		return list;
	}
	
	/**
	 * Get nodes that sent message the most
	 * 
	 * @param nodes
	 * @return
	 */
	public static List<Node> getMaxMsgSent(Map<String, Node> nodes){
		Iterator<String> its = nodes.keySet().iterator();
		NodeStat stat = null;
		List<Node> list = new ArrayList<Node>();
		
		// find a max number
		int max = 0;
		int tmp= 0;
		for(Node n = null; its.hasNext();){
			n = nodes.get(its.next());
			stat = n.getStat();
			tmp = stat.getNumMsgSent();
			if(max < tmp){
				max = tmp;
			}
		}
		
		// find a node that has max number
		its = nodes.keySet().iterator();
		for(Node n = null; its.hasNext();){
			n = nodes.get(its.next());
			stat = n.getStat();
			tmp = stat.getNumMsgSent();
			if(tmp == max){
				list.add(n);
			}
		}
		return list;
	}
	
	/**
	 * Get nodes that received message the least
	 * 
	 * @param nodes
	 * @return
	 */
	public static List<Node> getMinMsgRecv(Map<String, Node> nodes){
		Iterator<String> its = nodes.keySet().iterator();
		NodeStat stat = null;
		List<Node> list = new ArrayList<Node>();
		
		// find a min number
		int min = -1;
		int tmp = 0;
		for(Node n = null; its.hasNext();){
			n = nodes.get(its.next());
			stat = n.getStat();
			tmp = stat.getNumMsgRecv();
			if(min > tmp || min < 0){
				min = tmp;
			}
		}
		
		// find a node that has min number
		its = nodes.keySet().iterator();
		for(Node n = null; its.hasNext();){
			n = nodes.get(its.next());
			stat = n.getStat();
			tmp = stat.getNumMsgRecv();
			if(tmp == min){
				list.add(n);
			}
		}
		return list;
	}
	
	/**
	 * Get nodes that sent message the least
	 * 
	 * @param nodes
	 * @return
	 */
	public static List<Node> getMinMsgSent(Map<String, Node> nodes){
		Iterator<String> its = nodes.keySet().iterator();
		NodeStat stat = null;
		List<Node> list = new ArrayList<Node>();
		
		// find a min number
		int min = -1;
		int tmp= 0;
		for(Node n = null; its.hasNext();){
			n = nodes.get(its.next());
			stat = n.getStat();
			tmp = stat.getNumMsgSent();
			if(min > tmp || min < 0){
				min = tmp;
			}
		}
		
		// find a node that has min number
		its = nodes.keySet().iterator();
		for(Node n = null; its.hasNext();){
			n = nodes.get(its.next());
			stat = n.getStat();
			tmp = stat.getNumMsgSent();
			if(tmp == min){
				list.add(n);
			}
		}
		return list;
	}
	
	/**
	 * Get nodes that its board has been read the most
	 * 
	 * @param nodes
	 * @return
	 */
	public static List<Node> getMaxBoardRead(Map<String, Node> nodes){
		Iterator<String> its = nodes.keySet().iterator();
		NodeStat stat = null;
		List<Node> list = new ArrayList<Node>();
		
		// find a max number
		int max = 0;
		int tmp= 0;
		for(Node n = null; its.hasNext();){
			n = nodes.get(its.next());
			stat = n.getStat();
			tmp = stat.getNumBoardRead();
			if(max > tmp){
				max = tmp;
			}
		}
		
		// find a node that has max number
		its = nodes.keySet().iterator();
		for(Node n = null; its.hasNext();){
			n = nodes.get(its.next());
			stat = n.getStat();
			tmp = stat.getNumBoardRead();
			if(tmp == max){
				list.add(n);
			}
		}
		return list;
	}
	

	/**
	 * Get nodes that its board has been written the most
	 * 
	 * @param nodes
	 * @return
	 */
	public static List<Node> getMaxBoardWrite(Map<String, Node> nodes){
		Iterator<String> its = nodes.keySet().iterator();
		NodeStat stat = null;
		List<Node> list = new ArrayList<Node>();
		
		// find a max number
		int max = 0;
		int tmp= 0;
		for(Node n = null; its.hasNext();){
			n = nodes.get(its.next());
			stat = n.getStat();
			tmp = stat.getNumBoardWrite();
			if(max < tmp){
				max = tmp;
			}
		}
		
		// find a node that has max number
		its = nodes.keySet().iterator();
		for(Node n = null; its.hasNext();){
			n = nodes.get(its.next());
			stat = n.getStat();
			tmp = stat.getNumBoardWrite();
			if(tmp == max){
				list.add(n);
			}
		}
		return list;
	}
	
	/**
	 * Get nodes that its board has been written the least
	 * 
	 * @param nodes
	 * @return
	 */
	public static List<Node> getMinBoardWrite(Map<String, Node> nodes){
		Iterator<String> its = nodes.keySet().iterator();
		NodeStat stat = null;
		List<Node> list = new ArrayList<Node>();
		
		// find a min number
		int min = -1;
		int tmp= 0;
		for(Node n = null; its.hasNext();){
			n = nodes.get(its.next());
			stat = n.getStat();
			tmp = stat.getNumBoardWrite();
			if(min > tmp || min < 0){
				min = tmp;
			}
		}
		
		// find a node that has min number
		its = nodes.keySet().iterator();
		for(Node n = null; its.hasNext();){
			n = nodes.get(its.next());
			stat = n.getStat();
			tmp = stat.getNumBoardWrite();
			if(tmp == min){
				list.add(n);
			}
		}
		return list;
	}
	
	/**
	 * Get nodes that its board has been read the least
	 * 
	 * @param nodes
	 * @return
	 */
	public static List<Node> getMinBoardRead(Map<String, Node> nodes){
		Iterator<String> its = nodes.keySet().iterator();
		NodeStat stat = null;
		List<Node> list = new ArrayList<Node>();
		
		// find a min number
		int min = -1;
		int tmp= 0;
		for(Node n = null; its.hasNext();){
			n = nodes.get(its.next());
			stat = n.getStat();
			tmp = stat.getNumBoardRead();
			if(min > tmp || min < 0){
				min = tmp;
			}
		}
		
		// find a node that has min number
		its = nodes.keySet().iterator();
		for(Node n = null; its.hasNext();){
			n = nodes.get(its.next());
			stat = n.getStat();
			tmp = stat.getNumBoardRead();
			if(tmp == min){
				list.add(n);
			}
		}
		return list;
	}
	
	/**
	 * Get nodes with max number of tokens have been
	 * dropped
	 * 
	 * @param nodes
	 * @return
	 */
	public static List<Node> getNodeMaxTokDrop(Map<String, Node> nodes){
		Iterator<String> its = nodes.keySet().iterator();
		NodeStat stat = null;
		List<Node> list = new ArrayList<Node>();
		
		// find a max number
		int max = 0;
		int tmp= 0;
		for(Node n = null; its.hasNext();){
			n = nodes.get(its.next());
			stat = n.getStat();
			tmp = stat.getNumTokDrop();
			if(max < tmp){
				max = tmp;
			}
		}
		
		// find a node that has max number
		its = nodes.keySet().iterator();
		for(Node n = null; its.hasNext();){
			n = nodes.get(its.next());
			stat = n.getStat();
			tmp = stat.getNumTokDrop();
			if(tmp == max){
				list.add(n);
			}
		}
		return list;
	}
	
	/**
	 * Get nodes with max number of tokens have been picked
	 * 
	 * @param nodes
	 * @return
	 */
	public static List<Node> getNodeMaxTokPick(Map<String, Node> nodes){
		Iterator<String> its = nodes.keySet().iterator();
		NodeStat stat = null;
		List<Node> list = new ArrayList<Node>();
		
		// find a max number
		int max = 0;
		int tmp= 0;
		for(Node n = null; its.hasNext();){
			n = nodes.get(its.next());
			stat = n.getStat();
			tmp = stat.getNumTokPick();
			if(max < tmp){
				max = tmp;
			}
		}
		
		// find a node that has max number
		its = nodes.keySet().iterator();
		for(Node n = null; its.hasNext();){
			n = nodes.get(its.next());
			stat = n.getStat();
			tmp = stat.getNumTokPick();
			if(tmp == max){
				list.add(n);
			}
		}
		return list;
	}
	

	/**
	 * Get a count of different type of board access of every board
	 * 
	 * @return {AccessType, count}
	 */
	public static Map<Integer, Integer> getBoardAccessCount(Map<String, Node> nodes){
		int c = 0;
		Map<Integer, Integer> count = new HashMap<Integer, Integer>();
		for (String id : nodes.keySet()) {
			Node n = nodes.get(id);
			int del = n.getStat().getNumBoardDel();
			int wrt = n.getStat().getNumBoardWrite();
			int rea = n.getStat().getNumBoardRead();
			
			// delete		
			if(count.containsKey(IConstants.BOARD_DEL)){
				c = count.get(IConstants.BOARD_DEL);
				c += del;
			}else{
				c = del;
			}
			count.put(IConstants.BOARD_DEL, c);
			
			// write			
			if(count.containsKey(IConstants.BOARD_WRITE)){
				c = count.get(IConstants.BOARD_WRITE);
				c += wrt;
			}else{
				c = wrt;
			}
			count.put(IConstants.BOARD_WRITE, c);
			
			// read			
			if(count.containsKey(IConstants.BOARD_READ)){
				c = count.get(IConstants.BOARD_READ);
				c += rea;
			}else{
				c = rea;
			}
			count.put(IConstants.BOARD_READ, c);
			
		}
		return count;
	}
	
	
	/**
	 * Get total number of move of every agents
	 * 
	 * @param agents
	 * @return
	 */
	public static int getTotalAgentMove(Map<String, Agent> agents){
		Iterator<String> its = agents.keySet().iterator();
		AgentStat stat = null;
		int count = 0;
		for(Agent n = null; its.hasNext();){
			n = agents.get(its.next());
			stat = n.getStat();
			count += stat.getNumMove();
		}
		return count;
	}
	
	/**
	 * Get list of nodes with number of agent visited
	 * 
	 * @param agents
	 * @return {nodeId, count}
	 */
	public static Map<String, Integer> getNodeVisitCount(Map<String, Agent> agents){
		Iterator<String> its = agents.keySet().iterator();		
		Map<String, Integer> nodes = new HashMap<String, Integer>();
		Map<String, Integer> tmp = null;
		AgentStat stat = null;
		for(Agent n = null; its.hasNext();){
			n = agents.get(its.next());
			stat = n.getStat();
			tmp = stat.getNodeVisit();
			Iterator<String> itp = tmp.keySet().iterator();
			int count = 0;
			for(String nodeId = null; itp.hasNext();){
				nodeId = itp.next();
				if(nodes.containsKey(nodeId)){
					count = nodes.get(nodeId);
					count += tmp.get(nodeId);					
				}else{
					count = tmp.get(nodeId);
				}
				nodes.put(nodeId, count);
			}
		}
		return nodes;
	}
	
	/**
	 * Get list of agent's states with number of move
	 * 
	 * @param agents
	 * @return {AgentStateId, count}
	 */
	public static Map<Integer, Integer> getStateMoveCount(Map<String, Agent> agents){
		Iterator<String> its = agents.keySet().iterator();
		Map<Integer, Integer> states = new HashMap<Integer, Integer>();
		Map<Integer, Integer> tmp = null;
		AgentStat stat = null;
		for(Agent n = null; its.hasNext();){
			n = agents.get(its.next());
			stat = n.getStat();
			tmp = stat.getStateMove();
			Iterator<Integer> itp = tmp.keySet().iterator();
			int count = 0;
			for(Integer stateId = null; itp.hasNext();){
				stateId = itp.next();
				if(states.containsKey(stateId)){
					count = states.get(stateId);
					count += tmp.get(stateId);					
				}else{
					count = tmp.get(stateId);
				}
				states.put(stateId, count);
			}
		}
		return states;
	}
	
	/**
	 * Get a list of agent's state count
	 * 
	 * @param agents
	 * @return {AgentStateId, count}
	 */
	public static Map<Integer, Integer> getAgentStateCount(Map<String, Agent> agents){
		Iterator<String> its = agents.keySet().iterator();
		Map<Integer, Integer> states = new HashMap<Integer, Integer>();
		int stateId = -1;
		int count = 0;
		for(Agent n = null; its.hasNext();){
			n = agents.get(its.next());
			stateId = n.getCurState();			
			if(states.containsKey(stateId)){
				count = states.get(stateId);
				count++;					
			}else{
				count = 1;
			}
			states.put(stateId, count);		
		}
		return states;
	}
	
	/**
	 * Get total number of board read access by all agents
	 * 
	 * @param agents
	 * @return
	 */
	public static int getTotalBoardRead(Map<String, Agent> agents){
		Iterator<String> its = agents.keySet().iterator();
		AgentStat stat = null;
		int count = 0;
		for(Agent n = null; its.hasNext();){
			n = agents.get(its.next());
			stat = n.getStat();
			count += stat.getNumBoardRead();
		}
		return count;
	}
	
	/**
	 * Get total number of board write access by all agents
	 * 
	 * @param agents
	 * @return
	 */
	public static int getTotalBoardWrite(Map<String, Agent> agents){
		Iterator<String> its = agents.keySet().iterator();
		AgentStat stat = null;
		int count = 0;
		for(Agent n = null; its.hasNext();){
			n = agents.get(its.next());
			stat = n.getStat();
			count += stat.getNumBoardWrite();
		}
		return count;
	}
	
	/**
	 * Get total number of board remove access by all agents
	 * 
	 * @param agents
	 * @return
	 */
	public static int getTotalBoardDel(Map<String, Agent> agents){
		Iterator<String> its = agents.keySet().iterator();
		AgentStat stat = null;
		int count = 0;
		for(Agent n = null; its.hasNext();){
			n = agents.get(its.next());
			stat = n.getStat();
			count += stat.getNumBoardDel();
		}
		return count;
	}
	
	/**
	 * Get total number of token picked by all agents
	 * 
	 * @param agents
	 * @return
	 */
	public static int getTotalTokPick(Map<String, Agent> agents){
		Iterator<String> its = agents.keySet().iterator();
		AgentStat stat = null;
		int count = 0;
		for(Agent n = null; its.hasNext();){
			n = agents.get(its.next());
			stat = n.getStat();
			count += stat.getNumTokPick();
		}
		return count;
	}
	
	/**
	 * Get total number of token dropped by all agents
	 * 
	 * @param agents
	 * @return
	 */
	public static int getTotalTokDrop(Map<String, Agent> agents){
		Iterator<String> its = agents.keySet().iterator();
		AgentStat stat = null;
		int count = 0;
		for(Agent n = null; its.hasNext();){
			n = agents.get(its.next());
			stat = n.getStat();
			count += stat.getNumTokDrop();
		}
		return count;
	}

	/**
	 * Get list of agent with number of token its currently carrying
	 * 
	 * @param agents 
	 * @return {AgentId, count}
	 */
	public static Map<String, Integer> getAgentTokHoldCount(Map<String, Agent> agents){
		Iterator<String> its = agents.keySet().iterator();
		Map<String, Integer> counts = new HashMap<String, Integer>();
		for(Agent n = null; its.hasNext();){
			n = agents.get(its.next());
			AgentModel a = n.getClientEntity();
			if(a instanceof TokenAgent){
				TokenAgent t = (TokenAgent)a;
				counts.put(a.getAgentId(), t.countMyToken());
			}			
		}
		return counts;
	}
	
	/**
	 * Get list of agent states with number of all tokens have been picked
	 * 
	 * @param agents 
	 * @return {AgentStateId, count}
	 */
	public static Map<Integer, Integer> getStateTokenPickCount(Map<String, Agent> agents){
		Map<Integer, Integer> res;
		Iterator<String> its = agents.keySet().iterator();
		Map<Integer, Integer> counts = new HashMap<Integer, Integer>();
		int c = 0;
		for(Agent n = null; its.hasNext();){
			n = agents.get(its.next());
			res = n.getStat().getStatePick();
			for (Integer stateId : res.keySet()) {
				if (counts.containsKey(stateId)){
					c = counts.get(stateId);
					c += res.get(stateId);
				}else{
					c = res.get(stateId);
				}
				counts.put(stateId, c);
			}
		}
		return counts;
	}
	
	/**
	 * Get list of agent states with number of all tokens have been dropped
	 * 
	 * @param agents 
	 * @return {AgentStateId, count}
	 */
	public static Map<Integer, Integer> getStateTokenDropCount(Map<String, Agent> agents){
		Map<Integer, Integer> res;
		Iterator<String> its = agents.keySet().iterator();
		Map<Integer, Integer> counts = new HashMap<Integer, Integer>();
		int c = 0;
		for(Agent n = null; its.hasNext();){
			n = agents.get(its.next());
			res = n.getStat().getStateDrop();
			for (Integer stateId : res.keySet()) {
				if (counts.containsKey(stateId)){
					c = counts.get(stateId);
					c += res.get(stateId);
				}else{
					c = res.get(stateId);
				}
				counts.put(stateId, c);
			}
		}
		return counts;
	}
	
	/**
	 * Get a list of node with number of token has been dropped
	 * 
	 * @param agents
	 * @return {NodeId, count}
	 */
	public static Map<String, Integer> getNodeTokDropCount(Map<String, Agent> agents){
		Iterator<String> its = agents.keySet().iterator();		
		Map<String, Integer> nodes = new HashMap<String, Integer>();
		Map<String, Integer> tmp = null;
		AgentStat stat = null;
		for(Agent n = null; its.hasNext();){
			n = agents.get(its.next());
			stat = n.getStat();
			tmp = stat.getTokDrop();
			Iterator<String> itp = tmp.keySet().iterator();
			int count = 0;
			for(String nodeId = null; itp.hasNext();){
				nodeId = itp.next();
				if(nodes.containsKey(nodeId)){
					count = nodes.get(nodeId);
					count += tmp.get(nodeId);					
				}else{
					count = tmp.get(nodeId);
				}
				nodes.put(nodeId, count);
			}
		}
		return nodes;
	}
	
	/**
	 * Get a list of node with number of token has been picked
	 * 
	 * @param agents
	 * @return {nodeId, count}
	 */
	public static Map<String, Integer> getNodeTokPickCount(Map<String, Agent> agents){
		Iterator<String> its = agents.keySet().iterator();	
		Map<String, Integer> nodes = new HashMap<String, Integer>();
		Map<String, Integer> tmp = null;
		AgentStat stat = null;
		for(Agent n = null; its.hasNext();){
			n = agents.get(its.next());
			stat = n.getStat();
			tmp = stat.getTokPick();
			Iterator<String> itp = tmp.keySet().iterator();
			int count = 0;
			for(String nodeId = null; itp.hasNext();){
				nodeId = itp.next();
				if(nodes.containsKey(nodeId)){
					count = nodes.get(nodeId);
					count += tmp.get(nodeId);					
				}else{
					count = tmp.get(nodeId);
				}
				nodes.put(nodeId, count);
			}
		}
		return nodes;
	}
	
	/**
	 * Get agents with highest number of token picked
	 * 
	 * @param agents
	 * @return 
	 */
	public static List<Agent> getAgentMaxTokPick(Map<String, Agent> agents){
		List<Agent> list = new ArrayList<Agent>();
		Iterator<String> its = agents.keySet().iterator();
		AgentStat stat = null;

		// find a max number
		int max = 0;
		int tmp= 0;
		for(Agent n = null; its.hasNext();){
			n = agents.get(its.next());
			stat = n.getStat();
			tmp = stat.getNumTokPick();
			if(max < tmp){
				max = tmp;
			}
		}
		
		// find an agent that has max number
		its = agents.keySet().iterator();
		for(Agent n = null; its.hasNext();){
			n = agents.get(its.next());
			stat = n.getStat();
			tmp = stat.getNumTokPick();
			if(tmp == max){
				list.add(n);
			}
		}
		
		return list;
	}
	
	/**
	 * Get agents with lowest number of token picked
	 * 
	 * @param agents
	 * @return 
	 */
	public static List<Agent> getAgentMinTokPick(Map<String, Agent> agents){
		List<Agent> list = new ArrayList<Agent>();
		Iterator<String> its = agents.keySet().iterator();
		AgentStat stat = null;

		// find a min number
		int min = -1;
		int tmp = 0;
		for(Agent n = null; its.hasNext();){
			n = agents.get(its.next());
			stat = n.getStat();
			tmp = stat.getNumTokPick();
			if(min > tmp || min < 0){
				min = tmp;
			}
		}
		
		// find an agent that has min number
		its = agents.keySet().iterator();
		for(Agent n = null; its.hasNext();){
			n = agents.get(its.next());
			stat = n.getStat();
			tmp = stat.getNumTokPick();
			if(tmp == min){
				list.add(n);
			}
		}
		return list;
	}
	
	/**
	 * Get agents with highest number of token dropped
	 * 
	 * @param agents
	 * @return 
	 */
	public static List<Agent> getAgentMaxTokDrop(Map<String, Agent> agents){
		List<Agent> list = new ArrayList<Agent>();
		Iterator<String> its = agents.keySet().iterator();
		AgentStat stat = null;

		// find a max number
		int max = 0;
		int tmp= 0;
		for(Agent n = null; its.hasNext();){
			n = agents.get(its.next());
			stat = n.getStat();
			tmp = stat.getNumTokDrop();
			if(max < tmp){
				max = tmp;
			}
		}
		
		// find an agent that has max number
		its = agents.keySet().iterator();
		for(Agent n = null; its.hasNext();){
			n = agents.get(its.next());
			stat = n.getStat();
			tmp = stat.getNumTokDrop();
			if(tmp == max){
				list.add(n);
			}
		}
		
		return list;
	}
	
	/**
	 * Get agents with lowest number of token dropped
	 * 
	 * @param agents
	 * @return 
	 */
	public static List<Agent> getAgentMinTokDrop(Map<String, Agent> agents){
		List<Agent> list = new ArrayList<Agent>();
		Iterator<String> its = agents.keySet().iterator();
		AgentStat stat = null;

		// find a min number
		int min = -1;
		int tmp = 0;
		for(Agent n = null; its.hasNext();){
			n = agents.get(its.next());
			stat = n.getStat();
			tmp = stat.getNumTokDrop();
			if(min > tmp || min < 0){
				min = tmp;
			}
		}
		
		// find an agent that has min number
		its = agents.keySet().iterator();
		for(Agent n = null; its.hasNext();){
			n = agents.get(its.next());
			stat = n.getStat();
			tmp = stat.getNumTokDrop();
			if(tmp == min){
				list.add(n);
			}
		}
		return list;
	}
	
	
	/**
	 * Get average delay time of message traveled in every links
	 * 
	 * @param edge
	 * @return
	 */
	public static int getAverageEdgeDelay(Map<String, Edge> edges){
		Iterator<String> its = edges.keySet().iterator();
		EdgeStat stat = null;
		int count = 0;
		for(Edge n = null; its.hasNext();){
			n = edges.get(its.next());
			stat = n.getStat();
			count += stat.getTotalTimeUse();
		}
		return count/edges.size();
	}
	
	/**
	 * Get link(s) with a max delay time of message traveled
	 * 
	 * @param edge
	 * @return
	 */
	public static List<Edge> getMaxEdgeDelay(Map<String, Edge> edges){
		Iterator<String> its = edges.keySet().iterator();
		EdgeStat stat = null;
		List<Edge> list = new ArrayList<Edge>();
		
		// find max delay time
		int max = 0;
		int tmp = 0;
		for(Edge n = null; its.hasNext();){
			n = edges.get(its.next());
			stat = n.getStat();
			tmp = stat.getTotalTimeUse();
			if(max < tmp){
				max = tmp;
			}
		}
		
		its = edges.keySet().iterator();
		for(Edge n = null; its.hasNext();){
			n = edges.get(its.next());
			stat = n.getStat();
			tmp = stat.getTotalTimeUse();
			if(tmp == max){
				list.add(n);
			}
		}
		
		return list;
	}
	
	/**
	 * Get total number of message entered from every links
	 * 
	 * @param edge
	 * @return
	 */
	public static int getTotalEnter(Map<String, Edge> edges){
		Iterator<String> its = edges.keySet().iterator();
		EdgeStat stat = null;
		int count = 0;
		for(Edge n = null; its.hasNext();){
			n = edges.get(its.next());
			stat = n.getStat();
			count += stat.getTotalEdgeEnter();
		}
		return count;
	}
	
	/**
	 * Get total number of message leaved from every links
	 * 
	 * @param edge
	 * @return
	 */
	public static int getTotalLeave(Map<String, Edge> edges){
		Iterator<String> its = edges.keySet().iterator();
		EdgeStat stat = null;
		int count = 0;
		for(Edge n = null; its.hasNext();){
			n = edges.get(its.next());
			stat = n.getStat();
			count += stat.getTotalEdgeLeave();
		}
		return count;
	}
	
	/**
	 * Get list of message type with number of message has been sent
	 * 
	 * @param edges
	 * @return {messageType, count}
	 */
	public static Map<String, Integer> getTotalMsgTypeCount(Map<String, Edge> edges){
		Iterator<String> its = edges.keySet().iterator();	
		Map<String, Integer> msgTypes = new HashMap<String, Integer>();
		Map<String, Integer> tmp = null;
		EdgeStat stat = null;
		for(Edge n = null; its.hasNext();){
			n = edges.get(its.next());
			stat = n.getStat();
			tmp = stat.getMsgTypes();
			Iterator<String> itp = tmp.keySet().iterator();
			int count = 0;
			for(String type = null; itp.hasNext();){
				type = itp.next();
				if(msgTypes.containsKey(type)){
					count = msgTypes.get(type);
					count += tmp.get(type);					
				}else{
					count = tmp.get(type);
				}
				msgTypes.put(type, count);
			}
		}
		return msgTypes;
	}
}
