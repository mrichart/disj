package distributed.plugin.runtime.engine;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;

import distributed.plugin.core.IConstants;
import distributed.plugin.core.Logger.logTag;
import distributed.plugin.random.IRandom;
import distributed.plugin.runtime.Graph;
import distributed.plugin.runtime.GraphLoader;

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

}
