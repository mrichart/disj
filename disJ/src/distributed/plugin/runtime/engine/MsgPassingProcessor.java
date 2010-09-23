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
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import distributed.plugin.core.DisJException;
import distributed.plugin.core.Edge;
import distributed.plugin.core.IConstants;
import distributed.plugin.core.Logger;
import distributed.plugin.core.Node;
import distributed.plugin.core.Logger.logTag;
import distributed.plugin.random.IRandom;
import distributed.plugin.runtime.Event;
import distributed.plugin.runtime.Graph;
import distributed.plugin.runtime.GraphLoader;
import distributed.plugin.runtime.IMessage;
import distributed.plugin.runtime.IProcessor;
import distributed.plugin.runtime.MsgPassingEvent;
import distributed.plugin.ui.IGraphEditorConstants;

/**
 * An core processor that executes message passing model event for 
 * a given graph. The relation between a processor and a Graph is 
 * one-to-one mapping
 */
public class MsgPassingProcessor implements IProcessor {

	private int callingChain;

	private boolean stop;

	private boolean pause;

	private boolean stepForward;

	private int speed;

	private String procName;

	private Class<Entity> client;

	private Class<IRandom> clientRandom;

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
	
	
	/**
	 * Constructor
	 * 
	 * @param graph A graph model that used by the processor
	 * @param client A client Entity object that hold algorithm
	 * @param clientRandom A client IRandom object the hold algorithm
	 * @param out A URL to a location of log files directory
	 * @throws IOException
	 */
	MsgPassingProcessor(Graph graph, Class<Entity> client, Class<IRandom> clientRandom,
			URL out) throws IOException {
		
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
		this.clientRandom = clientRandom;
		this.queue = new EventQueue();
		this.stateFields = new HashMap<Integer, String>();
		
		this.timeGen = TimeGenerator.getTimeGenerator();
		this.timeGen.addGraph(this.graph.getId());		
		
		this.log = new Logger(this.graph.getId(), out, this.timeGen);		

		this.setSystemOutConsole();
		
		this.initClientStateVariables();
		if (this.clientRandom != null) {
			this.initClientRandomStateVariables();
		}		
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
	}

	// FIXME need to do something here!!!
	private void initClientRandomStateVariables() {
		
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
			Edge recv = sNode.getEdge(port);
					
			// tracking message sent to each target
			this.updateEdgeLog(recv, sender, message);
			this.updateSentLog(sNode, port, message);
			this.graph.countMsgSent(msgLabel);
			
			// validate the probability of failure
			if (!recv.isReliable()) {
				int prob = ran.nextInt(100) + 1;
				if (prob <= recv.getProbOfFailure()) {
					// log for losing msg
					this.logEdgeFailurMsg(recv, sender, message);
					continue;
				}
			}
			
			int execTime = recv.getDelayTime(sNode, this.getCurrentTime());
			int eventId = this.getNextId();		
			 try {
				 IMessage msg = new Message(message.getLabel(), SimulatorEngine.deepClone(data));
				 
				 newEvents.add(new MsgPassingEvent(sender, IConstants.EVENT_ARRIVAL_TYPE,
						 execTime, eventId, recv.getEdgeId(), msg));
			 } catch (Exception ie) {
				 throw new DisJException(ie);
			 }
		}
		
		// add new events into the queue
		this.queue.pushEvents(newEvents);
		
		// update time's lower bound
		this.setCurrentTime(this.queue.topEvent().getExecTime());
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

			// update time's lower bound, which may be the alarm ringing is
			// a smallest
			this.setCurrentTime(this.queue.topEvent().getExecTime());

		} else if (message.getLabel().equals(IConstants.MESSAGE_SET_BLOCK_MSG)) {
			
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
					this.queue.pushEvents(events);
					node.clearBlockedPort(port);
				}
			}
			node.setPortBlock(port, block);
		}
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
		Object[] value = new Object[2];
		value[0] = port;
		value[1] = msg;			
		this.log.logNode(logTag.NODE_SEND, sender.getNodeId(), value);			
	}

	/*
	 * Update node's log after received message
	 */
	private void updateRecvLog(Node receiver, String port, IMessage msg) {
		receiver.incMsgRecv();
		Object[] value = new Object[2];
		value[0] = port;
		value[1] = msg;			
		this.log.logNode(logTag.NODE_RECV, receiver.getNodeId(), value);	
	}

	/*
	 * Update edge's log for entering message
	 */
	private void updateEdgeLog(Edge edge, String nodeId, IMessage msg) {
		edge.incNumMsg();
		Object[] value = new Object[2];
		value[0] = nodeId;
		value[1] = msg;	
		this.log.logEdge(logTag.EDGE_MSG, edge.getEdgeId(), value);
	}

	/*
	 * Update edge's log for losing message
	 */
	private void logEdgeFailurMsg(Edge edge, String nodeId, IMessage msg) {
		Object[] value = new Object[2];
		value[0] = nodeId;
		value[1] = msg;	
		this.log.logEdge(logTag.EDGE_LOST, edge.getEdgeId(), value);
	}

	/**
	 * TODO Flush the logs and clean up memories. Also generate reports
	 * 
	 * @throws DisJException
	 */
	public void cleanUp() {

/*
		String title = "*****Statistics report of the simulation in text format*****";

		//System.out.println(title);
		appendToRecFile(title);

		Map<String, Node> nodes = this.graph.getNodes();
		for (String id : nodes.keySet()) {
			String nodeSummary = this.graph.getNode(id).toString();	
			this.appendToRecFile(nodeSummary);
		}
		
		Map<String, Edge> edges = this.graph.getEdges();
		for (String label : edges.keySet()) {
			String nodeSummary = this.graph.getEdge(label).toString();	
			this.appendToRecFile(nodeSummary);
		}
*/		
		Map<String, Integer> msg = this.graph.getMsgSentCounter();
		for(String msgType : msg.keySet()){
			int count = msg.get(msgType);
			if(msgType.equals("")){
				msgType = "Others";
			}
			//this.appendToRecFile("\n" + msgType + " = " + count);
			System.out.println("@MsgPassingProcessor.Cleanup() Message: " + msgType + " = " + count + " messages");
		}
		
		Map<Integer, Integer> count = this.graph.getNodeStateCount();
		for (int s : count.keySet()) {
			int c = count.get(s);
			//this.appendToRecFile("\n State " + s + " = " + c);
			System.out.println("@MsgPassingProcessor.Cleanup() State: " + s + " = " + c + " nodes");
		}
		
		// clean up the memory
		this.graph = null;

		// reset the flag the process is terminated
		this.stop = true;
		this.pause = false;

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
			
			// load and init any necessary data
			this.loadInitNodes();

			// start execute events
			this.executeEvents();

			this.systemOut.println("\n*****Simulation for " + this.procName
					+ " is successfully over.*****");

		} catch (Exception e) {
			e.printStackTrace();
			this.systemOut.println(e.toString());
			
		} finally {			
			// clean up logger
			this.log.cleanUp();

			this.cleanUp();
			this.systemOut.println("\n*****The simulation of " + this.procName
					+ " is terminated.*****");
		}
	}

	/*
	 * Load client object into init nodes and create init event(s)
	 */
	private void loadInitNodes() throws DisJException {

		List<Node> initNodes = GraphLoader.getInitNodes(graph);
		try {
			for (int i = 0; i < initNodes.size(); i++) {
				Node init = initNodes.get(i);
				
				// set up logger and state list
				init.setLogger(this.log);
				init.setStateNames(this.stateFields);
				
				Entity clientObj = GraphLoader.createEntityObject(client);
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
			this.queue.pushEvents(events);

			// update time's lower bound
			this.setCurrentTime(this.queue.topEvent().getExecTime());

		} catch (Exception e) {
			throw new DisJException(IConstants.ERROR_8, e.toString());
		}
	}

	/*
	 * Start executing event in the queue
	 */
	private void executeEvents() throws DisJException {

		while (!this.queue.isEmpty()) {
			if (this.stop)
				break;

			// To slow down the simulation speed
			try {
				Thread.sleep(this.speed);
			} catch (InterruptedException ignore) {
				System.out.println("@MsgPassingProcessor.executeEvent() Slow down process with speed: "
						+ this.speed + " =>" + ignore);
			}

			// suspend the process and/or hit breakpoint
			if (!this.pause) {
				MsgPassingEvent e = (MsgPassingEvent) this.queue.topEvent();
				Node thisNode = this.graph.getNode(e.getHostId());

				if (thisNode.getBreakpoint() == true) {
					this.setPause(true);
					while (this.pause) {
						if (this.stop)
							break;
						try {
							Thread.sleep(1000);
						} catch (InterruptedException ignore) {
							System.out
									.println("@MsgPassingProcessor.executeEvent() breakpoint=true: "
											+ ignore);
						}
					}
				}

				if (this.stop)
					break;

				if (e.getEventType() == IConstants.EVENT_ARRIVAL_TYPE) {
					this.invokeReceive(this.queue.popEvents());

					// tracking a length of message calling chain
					this.callingChain++;

				} else if (e.getEventType() == IConstants.EVENT_ALARM_RING_TYPE) {
					this.invokeAlarmRing(this.queue.popEvents());

				} else {
					this.invokeInit(this.queue.popEvents());
				}

				if (this.stepForward) {
					this.pause = true;
				}

				// FIXME print out the message to console that related to this
				// entity then clean it up.

			} else {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ignore) {
					System.out.println("@MsgPassingProcessor.executeEvent() pause=true: "
							+ ignore);
				}
			}

		}
	}

	/*
	 * Invoke client's init()
	 */
	private void invokeInit(List<Event> events) throws DisJException {
		for (int i = 0; i < events.size(); i++) {
			MsgPassingEvent e = (MsgPassingEvent)events.get(i);
			Node node = this.graph.getNode(e.getHostId());

			// Will NOT execute a Fail node
			if (node.isAlive()) {
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
				List<Event> list = node.getHoldEvents();
				if(!list.isEmpty()){
					this.invokeReceive(list);
				}
			}
		}
	}

	/*
	 * Invoke client's receive() w.r.t the target node
	 */
	private void invokeReceive(List<Event> events) throws DisJException {
		
		List<Object[]> invokeList = new ArrayList<Object[]>();
		for (int i = 0; i < events.size(); i++) {
			MsgPassingEvent e = (MsgPassingEvent)events.get(i);

			// retrieve a receiver node for this event
			Edge link = this.graph.getEdge(e.getEdgeName());
			Node recv = link.getOthereEnd(this.graph.getNode(e.getHostId()));
			String port = recv.getPortLabel(link);

			// Will NOT execute a Fail node
			if (!recv.isAlive())
				continue;

			// check if the port is blocked
			if (recv.isBlocked(port) == true) {
				// add event to a block queue
				recv.addEventToBlockedList(port, e);
				// this.updateBlockedLog(recv, port, e.getMessage().getLabel());

				// not allow to execute receive msg if it is init node and
				// it has not yet execute init()
			} else if ((recv.isInitializer() == false)
					|| (recv.isInitExec() == true)) {
				
				// update receive log
				this.updateRecvLog(recv, port, e.getMessage());
				this.graph.countMsgRecv(e.getMessage().getLabel());
				
				Entity entity = this.loadEntity(recv);
				String recvPort = GraphLoader.getEdgeLabel(recv, link);
				Object[] params = new Object[] {e, entity, recvPort };
				invokeList.add(params);

			} else {
				throw new DisJException(IConstants.ERROR_23,
						"@MsgPassingProcessor.invokeReceiver() ");
			}
		}

		Entity entity = null;
		try{
			// invoke receive() on a target node
			for (int i = 0; i < invokeList.size(); i++) {
				Object[] params = (Object[]) invokeList.get(i);
				MsgPassingEvent e = (MsgPassingEvent) params[0];
				entity = (Entity) params[1];
				String portLabel = (String) params[2];
				entity.getNodeOwner().setLatestRecvPort(portLabel);
				entity.receive(portLabel, e.getMessage());
			}
		}catch(Exception e){					
			throw new RuntimeException(e);
		}
	}

	/*
	 * Internal clock ring, invoke client's alarmRing()
	 */
	private void invokeAlarmRing(List<Event> events) throws DisJException {
		for (int i = 0; i < events.size(); i++) {
			MsgPassingEvent e = (MsgPassingEvent)events.get(i);
			Node node = this.graph.getNode(e.getHostId());

			// Will NOT execute a Fail node
			if (node.isAlive()) {
				Entity entity = this.loadEntity(node);
				entity.alarmRing();
			}
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

	public boolean isStepForward() {
		return stepForward;
	}

	public void setStepForward(boolean stepForward) {
		this.stepForward = stepForward;
	}

	@Override
	public Map<Integer, String> getStateFields() {
		return this.stateFields;
	}
	 
}