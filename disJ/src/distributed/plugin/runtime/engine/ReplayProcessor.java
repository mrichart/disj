package distributed.plugin.runtime.engine;

import java.io.FileReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import distributed.plugin.core.Agent;
import distributed.plugin.core.DisJException;
import distributed.plugin.core.Edge;
import distributed.plugin.core.IConstants;
import distributed.plugin.core.Logger;
import distributed.plugin.core.Node;
import distributed.plugin.core.Logger.logTag;
import distributed.plugin.runtime.Graph;
import distributed.plugin.runtime.IMessage;
import distributed.plugin.runtime.IProcessor;
import distributed.plugin.runtime.engine.AgentModel.NotifyType;
import distributed.plugin.ui.IGraphEditorConstants;

public class ReplayProcessor implements IProcessor {

	private boolean pause;
	
	private boolean stop;
	
	private int speed;
		
	private String fileName;
	
	private String userClassName;
	
	private logTag modelType;

	private Graph graph;
		
	private Map<String, Node> nodes;
	
	private Map<String, Edge> edges;
	
	/*
	 * Global tracking list of all created agents
	 */
	private Map<String, Agent> allAgents;	
	
	/*
	 * A mapping table of every possible state name and value 
	 * of agent which is defined in user algorithm
	 */	
	private Map<Integer, String> stateFields;

	/*
	 * System out delegate to Eclipse plug-in console
	 */
	private MessageConsoleStream systemOut;

	public ReplayProcessor(Graph graph, String fileName){
		
		this.pause = false;
		this.stop = true;
		this.speed = IConstants.SPEED_DEFAULT_RATE;
		this.fileName = fileName;
		this.graph = graph;
		this.nodes = this.graph.getNodes();
		this.edges = this.graph.getEdges();		
		this.allAgents = this.graph.getAgents();
		this.modelType = null;
		this.userClassName = null;
				
		this.stateFields = new HashMap<Integer, String>();
		
		
		this.setSystemOutConsole();
		
	}
	
	/*
	 * Configure system output to Eclipse Plug-in console
	 */
	private void setSystemOutConsole(){
		MessageConsole console = SimulatorEngine.findConsole(IGraphEditorConstants.DISJ_CONSOLE);
		this.systemOut = console.newMessageStream();
		System.setOut(new PrintStream(this.systemOut));
		System.setErr(new PrintStream(this.systemOut));
	}

	public boolean isPause() {
		return pause;
	}

	public void setPause(boolean pause) {
		this.pause = pause;
	}

	public boolean isStop() {
		return stop;
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}

	public int getSpeed() {
		return this.speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}
	@Override
	public void cleanUp() {
		// reset the flag the process is terminated
		this.stop = true;
		this.pause = false;
		
		// clear agents
		this.allAgents.clear();
		
		// clean up the memory
		this.graph.removeAllAgents();
		this.graph = null;
	}

	@Override
	public Map<Integer, String> getStateFields() {
		return this.stateFields;
	}

	@Override
	public MessageConsoleStream getSystemOut() {
		return this.systemOut;
	}

	@Override
	public void processReqeust(String sender, List<String> receivers,
			IMessage message) throws DisJException {

	}

	@Override
	public void run() {
		try{
			this.systemOut.println("\n Start REPLAY on graph " 
					+ this.graph.getId());
			
			this.stop = false;
			this.executeReplay();
			
			this.systemOut.println("\n*****Replay protocol " 
					+ this.userClassName
					+ " on graph " + this.graph.getId() 
					+ " is successfully over.*****");
			
		}catch(Exception e){
			e.printStackTrace();
			this.systemOut.println(e.toString());
			
		}finally{
			this.systemOut.println("\n*****Replay on graph " 
					+ this.graph.getId() 
					+ " is terminated*****");
		}	
	}
	
	private void executeReplay() throws Exception {
		Scanner sc = null;
		try {
			sc = new Scanner(new FileReader(this.fileName));
			String line = null;
			String[] value = null;
			boolean first = true;
			boolean second = false;			
			while (sc.hasNextLine() && stop == false) {
				
				// Slow down the simulation speed
				try {
					Thread.sleep(this.speed);
				} catch (InterruptedException ignore) {
					//this.systemOut.println("@ReplayProcessor.executeEvent() " +
					//		"Slow down process with speed: " + this.speed);
				}
				
				while (this.pause && this.stop == false) {					
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ignore) {
						this.systemOut.println("@AgentProcessor.executeEvent()" +
								" pause = true");
					}
				}
				if (this.stop){
					break;
				}
				
				// Execute next line of record
				line = sc.nextLine();
				if(first){
					// get state list from first line
					this.stateFields = Logger.readStates(line);

					// init node
					this.loadNode();				
					
					first = false;
					second = true;
					
				}else if(second){
					// get a model type and user class name used
					value = Logger.readLogLine(line);
					int time = Integer.parseInt(value[0]);
					this.modelType = logTag.valueOf(value[1]);
					this.userClassName = value[2];
					this.graph.setProtocol(this.userClassName);
					
					second = false;
					
				}else {
					// get log data in block
					value = Logger.readLogLine(line);										
					if(value[1].startsWith("EDGE_")){
						this.rebuildEdge(value);
						
					} else if (value[1].startsWith("NODE_")){
						this.rebuildNode(value);
						
					}  else if (value[1].startsWith("AGENT_")){
						this.rebuildAgent(value);
					}				
				}			
			}
		} finally{
			if(sc != null){
				sc.close();
				sc = null;
			}
		}
	}
	
	/*
	 * Initialize all nodes from graph
	 */
	private void loadNode(){
		Map<String, Node> nodes = this.graph.getNodes();
		
		Iterator<String> it = nodes.keySet().iterator();
		for(String id = null; it.hasNext();){
			id = it.next();
			Node node = nodes.get(id);
			node.setStateNames(this.stateFields);
			node.setLogger(null); // no logging while replay
			nodes.put(id, node);
		}
	}
	
	/*
	 * Rebuild edge event based on log
	 */
	private void rebuildEdge(String[] value){
		int time = Integer.parseInt(value[0]);
		logTag tag = logTag.valueOf(value[1]);
		String edgeId = value[2];
		String nodeId = value[3];
		String msgLabel = value[4];
		Edge edge = this.edges.get(edgeId);
		
		if(tag == logTag.EDGE_MSG){						
			edge.incNumMsgEnter();
			edge.recMsgPassed(msgLabel, nodeId);
			this.graph.countMsgSent(msgLabel);
			
		} else if (tag == logTag.EDGE_LOST){
			edge.incNumMsgEnter();
			edge.recMsgPassed(msgLabel, nodeId);
			this.graph.countMsgSent(msgLabel);
			this.graph.countMsgLost(msgLabel);
		}
	}
	
	/*
	 * Rebuild node event based on log
	 */
	private void rebuildNode(String[] value){
		int time = Integer.parseInt(value[0]);
		logTag tag = logTag.valueOf(value[1]);
		String nodeId = value[2];
		Node node = this.nodes.get(nodeId);
		String port = null;
		String msgLabel = null;
		
		if(tag == logTag.NODE_STATE){
			int state = Integer.parseInt(value[3]);
			node.setCurState(state);
			
		} else if (tag == logTag.NODE_SEND){
			port = value[3];
			msgLabel = value[4];
			node.incMsgSend();
			this.graph.countMsgSent(msgLabel);
			
		} else if (tag == logTag.NODE_RECV){
			port = value[3];
			msgLabel = value[4];
			node.incMsgRecv();
			this.graph.countMsgRecv(msgLabel);
			
		} else if (tag == logTag.NODE_INIT){
			node.setInitExec(true);		
			
		} else if (tag == logTag.NODE_DIE){
			node.setAlive(false);
		}
	}
	
	/*
	 * Rebuild node event based on log
	 */
	private void rebuildAgent(String[] value){
		int time = Integer.parseInt(value[0]);
		logTag tag = logTag.valueOf(value[1]);
		String agentId = value[2];
		Agent agent = this.allAgents.get(agentId);
		
		if(tag == logTag.AGENT_STATE){
			int state = Integer.parseInt(value[3]);
			agent.setCurState(state);
			
		} else if (tag == logTag.AGENT_NOTIFIED){
			String nodeId = value[3];
			NotifyType type = NotifyType.valueOf(value[4]);
			this.systemOut.println("@ReplayProcessor() Agent " + agentId
					+ " got " + type + " notified @ node " + nodeId);
			
		} else if (tag == logTag.AGENT_LEAVE){
			String nodeId = value[3];
			String fromPort = value[4];
			Node node = this.nodes.get(nodeId);
			node.removeAgent(agent);
			
		} else if (tag == logTag.AGENT_ARRIVE){
			String nodeId = value[3];
			String fromPort = value[4];
			Node node = this.nodes.get(nodeId);
			node.addAgent(agent);
			
		} else if (tag == logTag.AGENT_SLEEP){
			String nodeId = value[3];
			int awakTime = Integer.parseInt(value[4]);
			this.systemOut.println("@ReplayProcessor() Agent " + agentId
					+ " will sleep @ node " + nodeId + " until " + awakTime);
			
		} else if (tag == logTag.AGENT_AWAKE){
			this.systemOut.println("@ReplayProcessor() Agent " + agentId
					+ " awakes");
			
		} else if (tag == logTag.AGENT_DIE){
			agent.setAlive(false);
			this.allAgents.put(agentId, agent);
			this.graph.removeAgent(agentId);
			
		} else if (tag == logTag.AGENT_WRITE_TO_BOARD){
			String nodeId = value[3];
			String info = value[4];
			Node node = this.nodes.get(nodeId);
			List<String> board = node.getWhiteboard();
			board.add(info);
			node.setWhiteboard(board);
			
		} else if (tag == logTag.AGENT_DELETE_FROM_BOARD){
			String nodeId = value[3];
			String info = value[4];
			Node node = this.nodes.get(nodeId);
			List<String> board = node.getWhiteboard();
			board.remove(info);
			node.setWhiteboard(board);
			
		} else if (tag == logTag.AGENT_DROP_TOKEN){
			String nodeId = value[3];
			Node node = this.nodes.get(nodeId);
			node.incrementToken(1);
			int curToken = (Integer)agent.getData();
			curToken--;
			agent.setData(curToken);
			
		} else if (tag == logTag.AGENT_PICK_TOKEN){
			String nodeId = value[3];
			int amount = Integer.parseInt(value[4]);
			Node node = this.nodes.get(nodeId);
			node.decrementToken(amount);
			int curToken = (Integer)agent.getData();
			curToken += amount;
			agent.setData(curToken);
			
		} else if (tag == logTag.AGENT_INIT){
			String hostId = value[3];
			Node host = this.nodes.get(hostId);
			agent = this.allAgents.get(agentId);
			agent.setCurNode(host);
			agent.setStateNames(this.stateFields);
			
			if(this.modelType == logTag.MODEL_AGENT_TOKEN){
				int maxToken = this.graph.getMaxToken();
				agent.setData(maxToken);
			}			
			// add to home host
			host.addAgent(agent);						
			
		} else if (tag == logTag.AGENT_LIST){
			// construct a list of all agent
			this.allAgents = Logger.readAgentList(value);
			
			Iterator<String> its = this.allAgents.keySet().iterator();
			for(agentId = null; its.hasNext();){
				agentId = its.next();
				agent = this.allAgents.get(agentId);
				
				// add to global/graph for UI tracking list
				this.graph.addAgent(agentId, agent);
			}	
		}		
	}
	

}
