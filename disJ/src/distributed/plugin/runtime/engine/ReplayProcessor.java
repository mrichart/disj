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
import distributed.plugin.core.Graph;
import distributed.plugin.core.IConstants;
import distributed.plugin.core.Logger;
import distributed.plugin.core.Logger.logTag;
import distributed.plugin.core.Node;
import distributed.plugin.runtime.Event;
import distributed.plugin.runtime.GraphLoader;
import distributed.plugin.runtime.IMessage;
import distributed.plugin.runtime.IProcessor;
import distributed.plugin.runtime.engine.AgentModel.NotifyType;
import distributed.plugin.stat.GraphStat;
import distributed.plugin.ui.IGraphEditorConstants;

public class ReplayProcessor implements IProcessor {

	private boolean pause;
	
	private boolean stop;
	
	private int speed;
		
	private int curTime;
	
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
		
		this.curTime = 0;
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

	public Map<Integer, String> getStateFields() {
		return this.stateFields;
	}

	public MessageConsoleStream getSystemOut() {
		return this.systemOut;
	}

	public void processReqeust(String sender, List<String> receivers,
			IMessage message) throws DisJException {
	}

	public void run() {
		boolean ran = false;
		String gId = this.graph.getId();
		try{		
			if(this.fileName == null){
				// no file selected
				return;
			}
			ran = true;
			this.systemOut.println("\n Start REPLAY on graph " 
					+ gId);
		
			this.stop = false;
			this.executeReplay();
			this.systemOut.println("\n*****Replay protocol " 
					+ this.userClassName
					+ " on graph " + gId 
					+ " is successfully over.*****");			
			

			// display statistic report
			this.displayStat();
		
			while(stop == false){
				try{
					Thread.sleep(3000);
				}catch(Exception e){}
			}			
		}catch(Exception e){
			e.printStackTrace();			
			
		}finally{
			if(ran == true){			
				// clean up necessary data
				this.cleanUp();
				
				this.systemOut.println("\n*****Replay on graph " 
						+ gId + " is terminated*****");
			}
		}	
	}
	
	private void executeReplay() throws Exception {
		Scanner sc = null;
		int sleepTime = 0;
		try {			
			sc = new Scanner(new FileReader(this.fileName));
			String line = null;
			String[] value = null;
			boolean first = true;
			boolean second = false;			
			while (sc.hasNextLine() && stop == false) {
				
				// Slow down the simulation speed
				try {
					sleepTime = GraphLoader.speedConverter(this.speed);
					Thread.sleep(sleepTime);
				} catch (InterruptedException ignore) {					
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
					this.graph.setStateFields(this.stateFields);
					
					// init node
					this.loadNode();				
					
					first = false;
					second = true;
					
				}else if(second){
					// get a model type and user class name used
					value = Logger.readLogLine(line);
					
					// set current time
					this.curTime = Integer.parseInt(value[0]);
					
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
		// set current time
		this.curTime = Integer.parseInt(value[0]);
		
		logTag tag = logTag.valueOf(value[1]);
		String edgeId = value[2];
		String nodeId = value[3];
		String msgLabel = value[4];
		
		Edge edge = this.edges.get(edgeId);
		edge.recMsgPassed(msgLabel, nodeId);
		
		if(tag == logTag.EDGE_MSG){
			// update statistic
			edge.incNumMsgEnter();
			
		} else if (tag == logTag.EDGE_LOST){
			// update statistic
			edge.incNumMsgEnter();
		}
	}
	
	/*
	 * Rebuild node event based on log
	 */
	private void rebuildNode(String[] value){
		// set current time
		this.curTime = Integer.parseInt(value[0]);
		
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
			
			// update statistic
			node.incMsgSend();
			
		} else if (tag == logTag.NODE_RECV){
			port = value[3];
			msgLabel = value[4];
			
			// update statistic
			node.incMsgRecv();
			
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
		// set current time
		this.curTime = Integer.parseInt(value[0]);
		
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
			
			// update statistic
			agent.getStat().incMove();
			agent.getStat().incStateMove(agent.getCurState());
			
		} else if (tag == logTag.AGENT_ARRIVE){
			String nodeId = value[3];
			String fromPort = value[4];
			Node node = this.nodes.get(nodeId);
			node.addAgent(agent);
			
			// update statistic
			agent.getStat().incNodeVisit(nodeId);
			
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
			
		} else if (tag == logTag.AGENT_READ_TO_BOARD){
			String nodeId = value[3];
			Node node = this.nodes.get(nodeId);
			
			// update statistic
			agent.getStat().incRead();
			node.getStat().incNumBoardRead();
			
		} else if (tag == logTag.AGENT_WRITE_TO_BOARD){
			String nodeId = value[3];
			String info = value[4];
			Node node = this.nodes.get(nodeId);
			List<String> board = node.getWhiteboard();
			board.add(info);
			node.setWhiteboard(board);
			
			// update statistic
			agent.getStat().incWrite();
			node.getStat().incNumBoardWrite();
			
		} else if (tag == logTag.AGENT_DELETE_FROM_BOARD){
			String nodeId = value[3];
			String info = value[4];
			Node node = this.nodes.get(nodeId);
			List<String> board = node.getWhiteboard();
			board.remove(info);
			node.setWhiteboard(board);
			
			// update statistic
			agent.getStat().incDelete();
			node.getStat().incNumBoardDel();
			
		} else if (tag == logTag.AGENT_DROP_TOKEN){
			String nodeId = value[3];
			Node node = this.nodes.get(nodeId);
			node.incrementToken(1);
			int curToken = (Integer)agent.getData();
			curToken--;
			agent.setData(curToken);
			
			// update statistic
			agent.getStat().incDrop();
			agent.getStat().incNodeTokDrop(nodeId);
			
		} else if (tag == logTag.AGENT_PICK_TOKEN){
			String nodeId = value[3];
			int amount = Integer.parseInt(value[4]);
			Node node = this.nodes.get(nodeId);
			node.decrementToken(amount);
			int curToken = (Integer)agent.getData();
			curToken += amount;
			agent.setData(curToken);
			
			// update statistic
			for(int i =0; i < amount; i++){
				agent.getStat().incPick();
				agent.getStat().incNodeTokPick(nodeId);
			}
			
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
	
	public void displayStat() {
		GraphStat gStat = this.graph.getStat();
				
		if(this.modelType == logTag.MODEL_MSG_PASS){
			this.displayMsgPassingStat(gStat);			
		} else if (this.modelType == logTag.MODEL_AGENT_BOARD){
			this.displayAgentBoardStat(gStat);			
			
		} else if (this.modelType == logTag.MODEL_AGENT_TOKEN){
			this.displayAgentTokStat(gStat);
		}		
	}

	private void displayMsgPassingStat(GraphStat gStat) {
		Map<String, Node> nodes = this.graph.getNodes();
		Map<String, Edge> edges = this.graph.getEdges();
		
		int totalRecv = gStat.getTotalMsgRecv(nodes);
		int totalSent = gStat.getTotalMsgSent(nodes);
		int totalEnter = gStat.getTotalEnter(edges);
		int totalLeave = gStat.getTotalLeave(edges);
		int timeUse = gStat.getAverageEdgeDelay(edges);
		
		Map<Integer, Integer> nodeState = gStat.getNodeCurStateCount(nodes);
		Map<String, Integer> msgTypes = gStat.getTotalMsgTypeCount(edges);
		
		System.out.println("************** STATISTIC REPORT **************");
		System.out.println("Total Message has been sent: " + totalSent);
		System.out.println("Total Message has been received: " + totalRecv);
		System.out.println("Total Message has entered link: " + totalEnter);
		System.out.println("Total Message has leaved link: " + totalLeave);
		System.out.println("Total Average delay time has been accumulated: " + timeUse);
		
		System.out.println();
		Iterator<Integer> its = nodeState.keySet().iterator();
		int count = 0;
		for(int stateId = 0; its.hasNext();){
			stateId = its.next();
			count = nodeState.get(stateId);
			System.out.println("Stat " + this.stateFields.get(stateId) + " has " + count);
		}
		
		System.out.println();
		Iterator<String> it = msgTypes.keySet().iterator();
		count = 0;
		for(String type = null; it.hasNext();){
			type = it.next();
			count = msgTypes.get(type);
			System.out.println("Message " + type + " has been created " + count + " time(s)");
		}
	}

	private void displayAgentTokStat(GraphStat gStat) {
		Map<String, Agent> agents = this.graph.getAgents();
		Map<String, Edge> edges = this.graph.getEdges();
		
		int totalPick = gStat.getTotalTokPick(agents);
		int totalDrop = gStat.getTotalTokDrop(agents);
		int timeUse = gStat.getAverageEdgeDelay(edges);
		
		Map<String, Integer> nodeVisit = gStat.getNodeVisitCount(agents);
		Map<Integer, Integer> stateCount = gStat.getAgentStateCount(agents);

		Map<Integer, Integer> stateMove = gStat.getStateMoveCount(agents);
		Map<String, Integer> nodeDrop = gStat.getNodeTokDropCount(agents);
		Map<String, Integer> nodePick = gStat.getNodeTokPickCount(agents);
		Map<String, Integer> tokenHold = gStat.getAgentTokHoldCount(agents);
		
		System.out.println("************** STATISTIC REPORT **************");
		System.out.println("Total Token has been picked by all agents: " + totalPick);
		System.out.println("Total Token has been dropped by all agents: " + totalDrop);
		System.out.println("Total Average delay time has been accumulated: " + timeUse);
		
		System.out.println();
		Iterator<Integer> its = stateMove.keySet().iterator();
		int count = 0;
		for(int stateId = 0; its.hasNext();){
			stateId = its.next();
			count = stateMove.get(stateId);
			System.out.println("Stat " + this.stateFields.get(stateId) + " moved " + count);
		}
		
		System.out.println();
		its = stateCount.keySet().iterator();
		count = 0;
		for(int stateId = 0; its.hasNext();){
			stateId = its.next();
			count = stateCount.get(stateId);
			System.out.println("Agent State " + this.stateFields.get(stateId) + " count " + count);
		}
		
		System.out.println();
		Iterator<String> it = nodeVisit.keySet().iterator();
		count = 0;
		for(String nodeId = null; it.hasNext();){
			nodeId = it.next();
			count = nodeVisit.get(nodeId);
			System.out.println("Node " + nodeId + " has been visited " + count + " time(s)");
		}
		
		System.out.println();
		it = nodeDrop.keySet().iterator();
		count = 0;
		for(String nodeId = null; it.hasNext();){
			nodeId = it.next();
			count = nodeDrop.get(nodeId);
			System.out.println("Node " + nodeId + " Token has been dropped " + count + " time(s)");
		}
		
		System.out.println();
		it = nodePick.keySet().iterator();
		count = 0;
		for(String nodeId = null; it.hasNext();){
			nodeId = it.next();
			count = nodePick.get(nodeId);
			System.out.println("Node " + nodeId + " Token has been picked " + count + " time(s)");
		}
		
		System.out.println();
		it = tokenHold.keySet().iterator();
		count = 0;
		for(String agentId = null; it.hasNext();){
			agentId = it.next();
			count = tokenHold.get(agentId);
			System.out.println("Agent " + agentId + " hold " + count + " token(s)" + " time(s)");
		}
	}

	private void displayAgentBoardStat(GraphStat gStat) {
		Map<String, Agent> agents = this.graph.getAgents();
		Map<String, Edge> edges = this.graph.getEdges();
		
		int totalMove = gStat.getTotalAgentMove(agents);
		int totalRead = gStat.getTotalBoardRead(agents);
		int totalWrite = gStat.getTotalBoardWrite(agents);
		int totalDel = gStat.getTotalBoardDel(agents);
		int timeUse = gStat.getAverageEdgeDelay(edges);
		
		Map<String, Integer> nodeVisit = gStat.getNodeVisitCount(agents);
		Map<Integer, Integer> stateMove = gStat.getStateMoveCount(agents);
		Map<Integer, Integer> stateCount = gStat.getAgentStateCount(agents);

		System.out.println("************** STATISTIC REPORT **************");
		System.out.println("Total Agents moved: " + totalMove);
		System.out.println("Total Board read: " + totalRead);
		System.out.println("Total Board write: " + totalWrite);
		System.out.println("Total Board delete: " + totalDel);
		System.out.println("Total Average delay time has been accumulated: " + timeUse);
		
		System.out.println();
		Iterator<Integer> its = stateMove.keySet().iterator();
		int count = 0;
		for(int stateId = 0; its.hasNext();){
			stateId = its.next();
			count = stateMove.get(stateId);
			System.out.println("Stat " + this.stateFields.get(stateId) + " moved " + count + " time(s)");
		}
		
		System.out.println();
		Iterator<String> it = nodeVisit.keySet().iterator();
		count = 0;
		for(String nodeId = null; it.hasNext();){
			nodeId = it.next();
			count = nodeVisit.get(nodeId);
			System.out.println("Node " + nodeId + " has been visited " + count + " time(s)");
		}

		System.out.println();
		its = stateCount.keySet().iterator();
		count = 0;
		for(int stateId = 0; its.hasNext();){
			stateId = its.next();
			count = stateCount.get(stateId);
			System.out.println("Agent State " + this.stateFields.get(stateId) + " count " + count);
		}
	}
	
	public int getNetworkSize(){
		int size = 0;
		if(this.graph != null){
			Map<String, Node> nodes = this.graph.getNodes();
			if(nodes != null){
				size = nodes.size();
			}
		}
		return size;
	}

	public int getCurrentTime() {
		return this.curTime;
	}

	public Graph getGraph() {
		return this.graph;
	}

	public void pushEvents(List<Event> events) {}

}
