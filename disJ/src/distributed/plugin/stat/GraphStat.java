package distributed.plugin.stat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import distributed.plugin.core.Agent;
import distributed.plugin.core.Edge;
import distributed.plugin.core.Node;

@SuppressWarnings("serial")
public class GraphStat extends Statistic {
	
	private String graphId;
		
	/**
	 * 
	 * @param name
	 */
	public GraphStat(String name) {
		this.graphId = name;
	}

	@Override
	public String getName() {
		return this.graphId;
	}

	@Override
	public void reset() {

	}

	/**
	 * Get a list of every node and it current state
	 * 
	 * @return a map of state ID and count
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
	 * Get total number of message that every nodes sent
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
			count += stat.getNumMsgSend();
		}
		return count;
	}
	
	/**
	 * Get total number of move that every agents did
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
	 * Get total number of agent visited at each node
	 * 
	 * @param agents
	 * @return
	 */
	public static Map<String, Integer> getTotalNodeVisit(Map<String, Agent> agents){
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
	 * Get total number of agent move at each state
	 * 
	 * @param agents
	 * @return
	 */
	public static Map<Integer, Integer> getTotalStateMove(Map<String, Agent> agents){
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
	 * Get total number of board read access that every agents did
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
	 * Get total number of board write access that every agents did
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
	 * Get total number of board remove access that every agents did
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
	 * Get total number of token picked by every agents
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
	 * Get total number of token dropped by every agents
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
	 * Get total number of token has been dropped at each node
	 * 
	 * @param agents
	 * @return
	 */
	public static Map<String, Integer> getTotalNodeDrop(Map<String, Agent> agents){
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
	 * Get total number of token has been picked at each node
	 * 
	 * @param agents
	 * @return
	 */
	public static Map<String, Integer> getTotalNodePick(Map<String, Agent> agents){
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
	 * Get total delay time of message traveled in every links
	 * 
	 * @param edge
	 * @return
	 */
	public static int getTotalEdgeDelay(Map<String, Edge> edges){
		Iterator<String> its = edges.keySet().iterator();
		EdgeStat stat = null;
		int count = 0;
		for(Edge n = null; its.hasNext();){
			n = edges.get(its.next());
			stat = n.getStat();
			count += stat.getTotalTimeUse();
		}
		return count;
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
	 * Get total number of message has been send in difference message type
	 * 
	 * @param edges
	 * @return
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
