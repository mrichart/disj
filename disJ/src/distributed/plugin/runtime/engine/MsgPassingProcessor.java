/*******************************************************************************
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     DisJ Development Group
 *******************************************************************************/

package distributed.plugin.runtime.engine;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import distributed.plugin.core.DisJException;
import distributed.plugin.core.Edge;
import distributed.plugin.core.Graph;
import distributed.plugin.core.IConstants;
import distributed.plugin.core.Logger;
import distributed.plugin.core.Node;
import distributed.plugin.core.Logger.logTag;
import distributed.plugin.random.IRandom;
import distributed.plugin.runtime.Event;
import distributed.plugin.runtime.GraphLoader;
import distributed.plugin.runtime.IMessage;
import distributed.plugin.runtime.IProcessor;
import distributed.plugin.runtime.MsgPassingEvent;
import distributed.plugin.runtime.adversary.MsgPassingControl;
import distributed.plugin.stat.GraphStat;
import distributed.plugin.ui.IGraphEditorConstants;

/**
 * An core processor that executes message passing model event for 
 * a given graph. The relation between a processor and a Graph is 
 * one-to-one mapping
 */
public class MsgPassingProcessor implements IProcessor {

	private boolean stop;

	private boolean pause;

	private int speed;

	private int callingChain;
	
	private String procName;

	private Class<Entity> client;

	private Map<Integer, String> stateFields;

	private EventQueue queue;

	private Graph graph;

	private TimeGenerator timeGen;

	/*
	 * Logger to log every activities of this process
	 * for replay action
	 */
	private Logger log;
		
	/*
	 * System out delegate to Eclipse plug-in console
	 */
	private MessageConsoleStream systemOut;
	
	/*
	 * Adversary controller
	 */
	private MsgPassingControl adversary;

	/*
	 * Client custom random generator
	 */
	private IRandom randomGen;

	/**
	 * Constructor
	 * 
	 * @param graph A graph model that used by the processor
	 * @param client A client Class<Entity> object that hold algorithm
	 * @param clientRandom A client Class<IRandom> object the hold algorithm
	 * @param clientAdver An adversary Class<MsgPassingControl> object the hold algorithm
	 * @param out A URL to a location of log files directory
	 * @throws IOException
	 */
	MsgPassingProcessor(Graph graph, Class<Entity> client, Class<IRandom> clientRandom,
			Class<MsgPassingControl> clientAdver, URL out) throws IOException {
		
		if (graph == null || client == null){
			throw new NullPointerException(IConstants.RUNTIME_ERROR_0);
		}
	
		this.callingChain = 0;
		this.speed = IConstants.SPEED_DEFAULT_RATE;
		this.stop = false;
		this.pause = false;		
		
		this.graph = graph;
		this.procName = graph.getId();
		this.client = client;
		this.queue = new EventQueue();
		this.stateFields = new HashMap<Integer, String>();
		
		this.timeGen = TimeGenerator.getTimeGenerator();
		this.timeGen.addGraph(this.graph.getId());		
		
		this.log = new Logger(this.graph.getId(), out, this.timeGen);		

		this.setSystemOutConsole();
		
		this.initClientStateVariables();
		if (clientRandom != null) {
			this.randomGen = this.initClientRandom(clientRandom);
			this.graph.setClientRandom(this.randomGen);
		}
		if(clientAdver != null){
			this.adversary = this.initClientAdversary(clientAdver);
		}
	}

	/**
	 * Push all given events to a processing queue
	 * 
	 * @param events
	 */
	public void pushEvents(List<Event> events){
		this.queue.pushEvents(events);
	}
	
	/**
	 * Get all messages currently traveling under a given link
	 * and heading to a given destination node
	 * 
	 * @param edgeId An ID of link
	 * @param nodeId An ID of destination node
	 * @return A list that contains every message (with expected
	 * arrival time) that is currently traveling in the link, 
	 * empty list is returned if no message in the link
	 */
	public Map<IMessage, Integer> getTravelingMsg(String edgeId, String nodeId){
		Map<IMessage, Integer> tmp = new HashMap<IMessage, Integer>();
		List<Event> list = this.queue.getAllEvents();
		MsgPassingEvent e = null;
		for(int i =0; i < list.size(); i++){
			e = (MsgPassingEvent)list.get(i);
			if(e.getEventType() == IConstants.EVENT_ARRIVAL_TYPE){
				if(edgeId.equals(e.getEdgeId()) && 
						!e.getHostId().equals(nodeId)){
					tmp.put(e.getMessage(), e.getExecTime());
				}
			}
		}
		return tmp;
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
	
	public MessageConsoleStream getSystemOut() {
		return systemOut;
	}
	
	/*
	 * Get client (node) states defined by user
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
			this.systemOut.println(e.toString());
		}
		this.graph.setStateFields(this.stateFields);
	}

	private IRandom initClientRandom(Class<IRandom> client) {
		IRandom ran = null;
		try {
			ran = GraphLoader.createClientRandomObject(client);
		} catch (Exception e) {			
			e.printStackTrace();
		}
		return ran;
	}
	
	private MsgPassingControl initClientAdversary(Class<MsgPassingControl> client){
		MsgPassingControl adv = null;
		try {
			adv = GraphLoader.createMsgPassAdversaryObject(client);			
		} catch (Exception e) {			
			e.printStackTrace();
		}
		return adv;
	}
	
	/**
     * 
     */
	public void processReqeust(String sender, List<String> receivers, IMessage message)
			throws DisJException {

		Random ran = new Random(System.currentTimeMillis());
		List<Event> newEvents = new ArrayList<Event>();
		Node sNode = this.graph.getNode(sender);
		String msgLabel = message.getLabel();
		Serializable data = message.getContent();
	
		// sending message
		for (int i = 0; i < receivers.size(); i++) {
			String port = receivers.get(i);
			Edge recvEdge = sNode.getEdge(port);
			
			// tracking message sent/recv to each target
			this.updateEdgeLog(recvEdge, sender, message);
			this.updateSentLog(sNode, port, message);			
						
			// update statistic
			sNode.getStat().incNumMsgSent();
			sNode.getStat().incStateMsgSent(sNode.getCurState());
			recvEdge.getStat().incEnterEdge();
			recvEdge.getStat().incMsgTypeCount(msgLabel);
			recvEdge.recMsgPassed(msgLabel, sender);
			
			// validate the probability of failure
			if(this.adversary == null){
				if (!recvEdge.isReliable()) {
					int prob = ran.nextInt(100) + 1;
					if (prob <= recvEdge.getProbOfFailure()) {
						// log for losing msg
						this.logEdgeFailurMsg(recvEdge, sender, message);
						continue;
					}
				}
			}else{
				Node rNode = recvEdge.getOthereEnd(sNode);
				if(this.adversary.setDrop(message, recvEdge.getEdgeId(), rNode.getNodeId())){
					// log for losing msg
					this.logEdgeFailurMsg(recvEdge, sender, message);
					continue;		
				}
			}

			int curTime = this.getCurrentTime();
			int execTime = 0;
			if(this.adversary == null){
				execTime = recvEdge.getDelayTime(sNode, curTime);
				
			}else{
				Node rNode = recvEdge.getOthereEnd(sNode);
				execTime = this.adversary.setArrivalTime(message, recvEdge.getEdgeId(), rNode.getNodeId());
				if(execTime <= curTime){
					execTime = curTime + 1;
				}
			}
			
			// update statistic
			recvEdge.getStat().addTimeUse(execTime - curTime);
			
			int eventId = this.getNextId();		
			 try {
				 IMessage msg = new Message(message.getLabel(), SimulatorEngine.deepClone(data));
				 
				 newEvents.add(new MsgPassingEvent(sender, IConstants.EVENT_ARRIVAL_TYPE,
						 execTime, eventId, recvEdge.getEdgeId(), msg));
			 } catch (Exception ie) {
				 throw new DisJException(ie);
			 }
		}
		
		// add new events into the queue
		this.pushEvents(newEvents);		
	}

	/*
	 * 
	 */
	protected void internalNotify(String owner, IMessage message)
			throws DisJException {
		
		if (message.getLabel().equals(IConstants.MESSAGE_SET_ALARM_CLOCK)) {
			
			int eventId = this.getNextId();
			int delay = ((Integer) message.getContent()).intValue();
			int execTime = this.getCurrentTime()+ delay;

			// add an event into a queue
			Event e = new MsgPassingEvent(owner, IConstants.EVENT_ALARM_RING_TYPE, execTime,
					eventId, owner, message);
			this.queue.pushEvent(e);

		} 
		/*
		else if (message.getLabel().equals(IConstants.MESSAGE_SET_BLOCK_MSG)) {
			
			// set blocked/unblocked message
			Object[] msg = (Object[]) message.getContent();
			String port = (String) msg[0];
			boolean block = ((Boolean) msg[1]).booleanValue();
			Node node = this.graph.getNode(owner);
			if (!block) {
				// flush blocked messages after unblock port
				List<Event> events = node.getBlockedEvents(port);
				if (events != null) {
					// put them in a queue with the current execution time
					int time = this.getCurrentTime();
					for (int i = 0; i < events.size(); i++) {
						MsgPassingEvent e = (MsgPassingEvent) events.get(i);
						e.setExecTime(time);
					}
					this.pushEvents(events);
					node.removeBlockedPort(port);
				}
			}
			node.setPortBlock(port, block);
		}
		*/
	}

	/*
	 * Update node's log after sent message
	 */
	private void updateInitLog(Node node) {			
		this.log.logNode(logTag.NODE_INIT, node.getNodeId(), null);			
	}

	
	/*
	 * Update node's log after sent message
	 */
	private void updateSentLog(Node sender, String port, IMessage msg) {
		sender.incMsgSend();
		String[] value = new String[2];
		value[0] = port;
		value[1] = msg.getLabel();			
		this.log.logNode(logTag.NODE_SEND, sender.getNodeId(), value);			
	}

	/*
	 * Update node's log after received message
	 */
	private void updateRecvLog(Node receiver, String port, IMessage msg) {
		receiver.incMsgRecv();
		String[] value = new String[2];
		value[0] = port;
		value[1] = msg.getLabel();			
		this.log.logNode(logTag.NODE_RECV, receiver.getNodeId(), value);	
	}

	/*
	 * Update edge's log for entering message
	 */
	private void updateEdgeLog(Edge edge, String nodeId, IMessage msg) {
		edge.incNumMsgEnter();
		String[] value = new String[2];
		value[0] = nodeId;
		value[1] = msg.getLabel();	
		this.log.logEdge(logTag.EDGE_MSG, edge.getEdgeId(), value);
	}

	/*
	 * Update edge's log for losing message
	 */
	private void logEdgeFailurMsg(Edge edge, String nodeId, IMessage msg) {
		String[] value = new String[2];
		value[0] = nodeId;
		value[1] = msg.getLabel();	
		this.log.logEdge(logTag.EDGE_LOST, edge.getEdgeId(), value);
	}

	/**
	 * TODO Flush the logs and clean up memories. Also generate reports
	 * 
	 * @throws DisJException
	 */
	public void cleanUp() {
		
		// reset the flag the process is terminated
		this.stop = true;
		this.pause = false;
	
		// clear all nodes and edges
		Map<String, Node> nodes = this.graph.getNodes();
		Iterator<String> its = nodes.keySet().iterator();
		for(Node n = null; its.hasNext();){
			n = nodes.get(its.next());
			n.cleanUp();
		}
		
		Map<String, Edge> edges = this.graph.getEdges();
		its = edges.keySet().iterator();
		for(Edge e = null; its.hasNext();){
			e = edges.get(its.next());
			e.cleanUp();
		}
			
		// reset time generator
		this.timeGen.reset(this.graph.getId());
		
		// clear cache memory
		this.graph = null;
	}

	/**
	 * Start running a process w.r.t a given graph
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
			// init logger
			this.log.initLog();
			
			// log state name list
			this.log.logStates(logTag.STATE_FIELD, this.stateFields);
			
			// log the model and user class name
			this.log.logModel(logTag.MODEL_MSG_PASS, this.client.getName());

			// load and init any necessary data
			this.loadInitNodes();

			// start execute events
			this.executeEvents();

			this.systemOut.println("\n*****Simulation for " + this.procName
					+ " is successfully over.*****");
			
			// display statistic report
			this.displayStat();
						
			while(stop == false){
				try{
					Thread.sleep(3000);
				}catch(Exception e){}
			}			
		} catch (Exception e) {
			e.printStackTrace();
			
		} finally {
			
			// clean up logger
			this.log.cleanUp();

			// clean up necessary data
			this.cleanUp();
			
			this.systemOut.println("\n*****The simulation of " + this.procName
					+ " is terminated.*****");
		}
	}

	/*
	 * Load client object into init nodes and create init event(s)
	 */
	private void loadInitNodes() throws DisJException {
		
		List<Node> initNodes = GraphLoader.getInitNodes(this.graph);
		try {
			for (int i = 0; i < initNodes.size(); i++) {
				Node init = initNodes.get(i);
				
				// set up logger and state list
				init.setLogger(this.log);
				init.setStateNames(this.stateFields);
				
				Entity clientObj = GraphLoader.createEntityObject(this.client);
				clientObj.initEntity(this, init);
				init.addEntity(clientObj);				
			}
			
			//Random r = new Random(System.currentTimeMillis());
			List<Event> events = new ArrayList<Event> ();
			// create a set of init events
			for (int i = 0; i < initNodes.size(); i++) {
				Node init = initNodes.get(i);
				if(init.isAlive()){
					int eventId = this.getNextId();
					int execTime = 0;
					
					// it has delay initialize, set active time to random
					// OR TODO get a delay initialize time from user input
					//if (!init.hasDelayInit()){
					//	execTime = r.nextInt(IConstants.MAX_RANDOM_RANGE);
					//}
	
					// add an event into a queue
					IMessage msg = new Message("Initialized", new Integer(execTime));
					Event e = new MsgPassingEvent(init.getNodeId(), IConstants.EVENT_INITIATE_TYPE,
							execTime, eventId, init.getNodeId(), msg);
	
					events.add(e);
				}
			}
			// add to the queue
			this.pushEvents(events);

		} catch (Exception e) {
			throw new DisJException(IConstants.ERROR_8, e.toString());
		}
	}

	/*
	 * Start executing event in the queue
	 */
	private void executeEvents() throws Exception {
		int sleepTime = 0;
		while (!this.queue.isEmpty() && this.stop == false) {
			
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
					this.systemOut.println("@MsgPassingProcessor.executeEvent()" +
							" pause = true");
				}
			}				
			if (this.stop){
				break;
			}
			
			// Execute event
			//MsgPassingEvent e = (MsgPassingEvent) this.queue.topEvent();
			// tracking a length of message calling chain
			// this.callingChain++;
			
			// Get all events with smallest time
			List<Event> list = this.queue.popEvents();
			MsgPassingEvent e = null;
			boolean count = false;
			for(int i =0; i < list.size(); i++){
				e = (MsgPassingEvent)list.get(i);		
				if (e.getEventType() == IConstants.EVENT_ARRIVAL_TYPE) {
					this.invokeReceive(e);
					if(!count){
						this.callingChain++;
						count = true;
					}
				} else if (e.getEventType() == IConstants.EVENT_ALARM_RING_TYPE) {
					this.invokeAlarmRing(e);
	
				} else if( e.getEventType() == IConstants.EVENT_INITIATE_TYPE){
					this.invokeInit(e);
				}
			}			
			// set a current time to be a smallest of existing events
			int time = this.queue.getSmallestTime();
			if(time > -1){
				this.setCurrentTime(time);
			}else{
				// the queue is empty
			}
		}
	}

	/*
	 * Invoke client's init()
	 */
	private void invokeInit(MsgPassingEvent event) throws Exception {
		
		Node node = this.graph.getNode(event.getHostId());

		// Will NOT execute a Fail node
		if (node.isAlive()) {
			int execTime = 0;
			
			if(this.adversary != null){
				execTime = this.adversary.initTimeControl(node.getNodeId());
				if(execTime > this.getCurrentTime()){
					// update event execution time
					event.setExecTime(execTime);
					
					// push event back to the queue
					this.queue.pushEvent(event);
					return;
				}
			}
			
			// get client entity algorithm
			Entity entity = this.loadEntity(node);
						
			// update log
			this.updateInitLog(node);
			
			// execute client code
			node.setInitExec(true);
			entity.init();
			
			// catching up the holding events
			// this can happen only to recvMsg
			// since setAlarm will not be able to
			// occurred until initNode is initialized
			List<String> ports = node.getIncomingPorts();
			String port;
			MsgPassingEvent e ;
			for(int i =0; i < ports.size(); i++){
				port = ports.get(i);
				// unblock all the ports
				node.setBlockPort(port, false);
				
				// catching up the events
				List<Event> list = node.getBlockedEvents(port);
				if(list != null){
					for(int j =0; j < list.size(); j++){
						e = (MsgPassingEvent)list.get(j);
						this.invokeReceive(e);
					}
				}
			}			
		}
		// clear all blocked events
		node.clearAllBlockedEvents();		
	}

	/*
	 * Invoke client's receive() w.r.t the target node
	 */
	private void invokeReceive(MsgPassingEvent event) throws Exception {
		// retrieve a receiver node for this event
		Edge link = this.graph.getEdge(event.getEdgeId());
		Node recv = link.getOthereEnd(this.graph.getNode(event.getHostId()));
		String port = recv.getPortLabel(link);

		// update statistic
		link.getStat().incLeaveEdge();
		
		// Will NOT execute a Fail node
		if (recv.isAlive()){
			
			// not allow to receive msg if it is init node and
			// it has not yet execute init()
			if(recv.isInitializer() && !recv.isInitExec()){
				// block the incoming port
				recv.setBlockPort(port, true);
				
				// add event to a block queue
				recv.addEventToBlockList(port, event);
				
			}else{
				
				if(this.adversary != null){	
					// let check adversary commands
					this.adversary.arrivalControl(event.getMessage(), port, recv.getNodeId());					
				}
				
				// check if the port is blocked
				if (recv.isBlocked(port) == true) {						
					// add event to a block queue
					recv.addEventToBlockList(port, event);
					return;
				}
				
				// check whether a given msg label is blocked
				String msgLabel = event.getMessage().getLabel();
				if(recv.isVisitorBlocked(msgLabel, port)){
					// add event to a block queue
					recv.addEventToBlockList(port, event);
					return;
				}
				
				// update receive log
				this.updateRecvLog(recv, port, event.getMessage());
				
				// update statistic
				recv.getStat().incNumMsgRecv();
				
				Entity entity = this.loadEntity(recv);
				String recvPort = GraphLoader.getEdgeLabel(recv, link);			
				entity.getNodeOwner().setLatestRecvPort(recvPort);
				
				// Invoke client code
				entity.receive(recvPort, event.getMessage());				
			}
		}		
	}

	/*
	 * Internal clock ring, invoke client's alarmRing()
	 */
	private void invokeAlarmRing(MsgPassingEvent event) throws DisJException {		
		Node node = this.graph.getNode(event.getHostId());
		// Will NOT execute a Fail node
		if (node.isAlive()) {
			Entity entity = this.loadEntity(node);
			entity.alarmRing();
		}		
	}

	/*
	 * Lazy initialize entity
	 */
	private Entity loadEntity(Node node) throws DisJException {
		// get client at a given node
		Entity clientObj = node.getEntity();		
		
		// lazy init: If there is no client at the node yet	
		if(clientObj == null){
			try {
				// setup logger and state list
				node.setLogger(this.log);
				node.setStateNames(this.stateFields);
				
				// create and assign it to the node
				clientObj = GraphLoader.createEntityObject(this.client);
				clientObj.initEntity(this, node);
				node.addEntity(clientObj);
				
				
			} catch (Exception ex) {
				throw new DisJException(IConstants.ERROR_8, ex.toString());
			}
		}
		return clientObj;
	}

	/**
	 * @return Returns the graph of this processor
	 */
	public Graph getGraph() {
		return this.graph;
	}

	public boolean isStop() {
		return stop;
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}

	public boolean isPause() {
		return pause;
	}

	public void setPause(boolean pause) {
		this.pause = pause;
	}

	public int getSpeed() {
		return this.speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public int getCurrentTime() {
		return this.timeGen.getCurrentTime(graph.getId());
	}

	public void setCurrentTime(int time) {
		this.timeGen.setCurrentTime(graph.getId(), time);
	}
	
	public int getNextId(){
		int id = -1;
		try{ 
			id = this.timeGen.getNextNewId(this.graph.getId());
		}catch (DisJException e){
			this.systemOut.println("@MsgPassingProcessor.getNextId() [critical]  " + e.toString());
		}
		return id;
	}
	
	public int getCallingChain() {
		return this.callingChain;
	}

	public Map<Integer, String> getStateFields() {
		return this.stateFields;
	}

	public void displayStat() {
		
		// signal UI to display one time final graph report
		this.graph.signalFinalReportDisplay(IConstants.MODEL_MESSAGE_PASSING);

		GraphStat gStat = this.graph.getStat();
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
			System.out.println("State " + this.stateFields.get(stateId) + " has " + count);
		}
		
		System.out.println();
		Iterator<String> it = msgTypes.keySet().iterator();
		count = 0;
		for(String type = null; it.hasNext();){
			type = it.next();
			count = msgTypes.get(type);
			System.out.println("Message " + type + " has been created " + count);
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
}