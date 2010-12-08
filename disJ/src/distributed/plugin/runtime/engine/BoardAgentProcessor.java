package distributed.plugin.runtime.engine;

import java.io.IOException;
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

public class BoardAgentProcessor extends AgentProcessor {

	/*
	 * Client agent Class file (blueprint)
	 */
	private Class<BoardAgent> client;

	/**
	 * Constructor
	 * 
	 * @param graph A graph model that used by the processor
	 * @param client A client BoardAgent object that hold algorithm
	 * @param clientRandom A client IRandom object the hold algorithm
	 * @param out A URL to a location of log files directory
	 * @throws IOException
	 */
	BoardAgentProcessor(Graph graph, Class<BoardAgent> client, 
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
		this.graph.setStateFields(this.stateFields);
	}
	
	protected AgentModel createClientAgent() throws Exception{
		BoardAgent clientAgent = GraphLoader.createBoardAgentObject(this.client);		
		return clientAgent;
	}


	@Override
	protected void logAgentInfo() {		
		// log the model and user class name
		this.log.logModel(logTag.MODEL_AGENT_BOARD, this.client.getName());
		
		// log agent list
		super.logAgentInfo();

	}
	
	public void displayStat() {
		GraphStat gStat = this.graph.getStat();
		Map<String, Agent> agents = this.graph.getAgents();
		Map<String, Edge> edges = this.graph.getEdges();
		
		int totalMove = gStat.getTotalAgentMove(agents);
		int totalRead = gStat.getTotalBoardRead(agents);
		int totalWrite = gStat.getTotalBoardWrite(agents);
		int totalDel = gStat.getTotalBoardDel(agents);
		int timeUse = gStat.getTotalEdgeDelay(edges);
		
		Map<String, Integer> nodeState = gStat.getTotalNodeVisit(agents);
		Map<Integer, Integer> stateMove = gStat.getTotalStateMove(agents);
		
		System.out.println("************** STATISTIC REPORT **************");
		System.out.println("Total Agents moved: " + totalMove);
		System.out.println("Total Board read: " + totalRead);
		System.out.println("Total Board write: " + totalWrite);
		System.out.println("Total Board delete: " + totalDel);
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
	}

}
