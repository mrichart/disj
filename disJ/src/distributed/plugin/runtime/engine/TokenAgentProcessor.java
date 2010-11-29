package distributed.plugin.runtime.engine;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import distributed.plugin.core.Agent;
import distributed.plugin.core.Edge;
import distributed.plugin.core.Graph;
import distributed.plugin.core.IConstants;
import distributed.plugin.core.Logger.logTag;
import distributed.plugin.random.IRandom;
import distributed.plugin.runtime.GraphLoader;
import distributed.plugin.stat.GraphStat;

public class TokenAgentProcessor extends AgentProcessor {

	/*
	 * Client agent Class file (blueprint)
	 */
	private Class<TokenAgent> client;

	public TokenAgentProcessor (Graph graph, Class<TokenAgent> client, 
			Class<IRandom> clientRandom, URL out) {
			
		super(graph, clientRandom, out);
		
		if (client == null){
			throw new NullPointerException(IConstants.RUNTIME_ERROR_0);
		}
		this.client = client;
		this.initClientStateVariables();
	}
	
	/*
	 * Get client (agent) states defined by user
	 */
	private void initClientStateVariables() {
		try {
			Field[] states = this.client.getFields();
			Object obj = this.client.newInstance();
			// IRandom ran = this.clientRandom.newInstace();
			for (int i = 0; i < states.length; i++) {
				int mod = states[i].getModifiers();
				if (Modifier.isPublic(mod) && Modifier.isFinal(mod)
						&& states[i].getType().equals(int.class)) {
					String name = states[i].getName();
					String tmpName = name.toLowerCase();
					if ((tmpName.startsWith("state") || tmpName
							.startsWith("_state"))) {
						Integer value = (Integer) states[i].get(obj);
						this.stateFields.put(value, name);
					}
				}
			}
		} catch (Exception e){
			this.systemOut.println("@initClientStateVariables() " + e.toString());
		}
	}
	
	protected AgentModel createClientAgent() throws Exception{
		int maxTok = this.graph.getMaxToken();
		TokenAgent clientAgent = GraphLoader.createTokenAgentObject(this.client);
		clientAgent.setMaxToken(maxTok);	
		return clientAgent;
	}
	
	@Override
	protected void logAgentInfo() {		
		// log the model and user class name
		this.log.logModel(logTag.MODEL_AGENT_TOKEN, this.client.getName());

		// log agent list
		super.logAgentInfo();
	}

	public void displayStat() {
		GraphStat gStat = this.graph.getStat();
		Map<String, Agent> agents = this.graph.getAgents();
		Map<String, Edge> edges = this.graph.getEdges();
		
		int totalPick = gStat.getTotalTokPick(agents);
		int totalDrop = gStat.getTotalTokDrop(agents);
		int timeUse = gStat.getTotalEdgeDelay(edges);
		
		Map<String, Integer> nodeState = gStat.getTotalNodeVisit(agents);
		Map<Integer, Integer> stateMove = gStat.getTotalStateMove(agents);
		Map<String, Integer> nodeDrop = gStat.getTotalNodeDrop(agents);
		Map<String, Integer> nodePick = gStat.getTotalNodePick(agents);
		
		System.out.println("************** STATISTIC REPORT **************");
		System.out.println("Total Token has been picked: " + totalPick);
		System.out.println("Total Token has been dropped: " + totalDrop);
		System.out.println("Total Dealy time has been accumulated: " + timeUse);
		
		System.out.println();
		Iterator<Integer> its = stateMove.keySet().iterator();
		int count = 0;
		for(int stateId = 0; its.hasNext();){
			stateId = its.next();
			count = stateMove.get(stateId);
			System.out.println("State " + this.stateFields.get(stateId) + " moved " + count);
		}
		
		System.out.println();
		Iterator<String> it = nodeState.keySet().iterator();
		count = 0;
		for(String nodeId = null; it.hasNext();){
			nodeId = it.next();
			count = nodeState.get(nodeId);
			System.out.println("Node " + nodeId + " has been visited " + count);
		}
		
		System.out.println();
		it = nodeDrop.keySet().iterator();
		count = 0;
		for(String nodeId = null; it.hasNext();){
			nodeId = it.next();
			count = nodeDrop.get(nodeId);
			System.out.println("Node " + nodeId + " Token has been dropped " + count);
		}
		
		System.out.println();
		it = nodePick.keySet().iterator();
		count = 0;
		for(String nodeId = null; it.hasNext();){
			nodeId = it.next();
			count = nodePick.get(nodeId);
			System.out.println("Node " + nodeId + " Token has been picked " + count);
		}
	}

}
