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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import distributed.plugin.core.DisJException;
import distributed.plugin.core.Edge;
import distributed.plugin.core.IConstants;
import distributed.plugin.core.Node;
import distributed.plugin.random.IRandom;
import distributed.plugin.runtime.Event;
import distributed.plugin.runtime.Graph;
import distributed.plugin.runtime.GraphFactory;
import distributed.plugin.runtime.GraphLoader;
import distributed.plugin.runtime.IMessage;
import distributed.plugin.runtime.IProcesses;
import distributed.plugin.runtime.MsgPassingEvent;
import distributed.plugin.ui.IGraphEditorConstants;
import distributed.plugin.ui.editor.GraphEditor;

/**
 * An core processor that executes message passing model event for 
 * a given graph. The relation between a processor and a Graph is 
 * one-to-one mapping
 */
public class MsgPassingProcessor implements IProcesses {

	private int callingChain;

	private boolean stop;

	private boolean pause;

	private int speed;

	private String procName;

	private PrintWriter nodeFile;

	private PrintWriter edgeFile;

	private PrintWriter ConsoleOutputFile;

	private Class<Entity> client;

	private Class<IRandom> clientRandom;

	private EventQueue queue;

	private Graph graph;

	private TimeGenerator timeGen;

	private Map<Integer, String> stateFields;

	private boolean stepForward;

	private PrintWriter RecFile;

	private GraphEditor ge;

	// private SystemLog sysLog;

	/*
	 * System out delegate to Eclipse plug-in console
	 */
	private MessageConsoleStream systemOut;
	
	/**
	 * Constructor
	 * 
	 * @param ge An instance of GraphEditor that control the processor
	 * @param graph A graph model that used by the processor
	 * @param client A client Entity object that hold algorithm
	 * @param clientRandom A client IRandom object the hold algorithm
	 * @param out A URL to a location of log files directory
	 * @throws IOException
	 */
	MsgPassingProcessor(GraphEditor ge, Graph graph, Class<Entity> client, Class<IRandom> clientRandom,
			URL out) throws IOException {
		if (graph == null || client == null || ge == null){
			throw new NullPointerException(IConstants.RUNTIME_ERROR_0);
		}
	
		this.callingChain = 0;
		this.speed = IConstants.SPEED_DEFAULT_RATE;
		this.stop = false;
		this.pause = false;		
		
		this.ge = ge;
		this.graph = graph;
		this.procName = graph.getId();
		this.client = client;
		this.clientRandom = clientRandom;
		this.queue = new EventQueue();
		this.timeGen = TimeGenerator.getTimeGenerator();
		this.timeGen.addGraph(graph.getId() + "");
		this.stateFields = new HashMap<Integer, String>();

		this.setSystemOutConsole();
		
		this.initLogFile(out);
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
		try {
			IRandom ran = (IRandom) this.clientRandom.newInstance();
			this.graph.setClientRandom(ran);
			GraphFactory.addGraph(this.graph);
		} catch (Exception e) {
			e.printStackTrace();
			this.systemOut.println(e.toString());
		}
	}

	/*
	 * FIXME LOG file does not work!!!
	 * Initialize the log's files
	 */
	private void initLogFile(URL url) throws IOException {
		File dir = new File(url.getPath());
		StringTokenizer tok = new StringTokenizer(this.graph.getId(), ".");
		String name = tok.nextToken();
		File n = new File(dir, name + "_Node" + ".log");
		File e = new File(dir, name + "_Edge" + ".log");
		File c = new File(dir, "ConsoleOutput" + ".log");
		File r = new File(dir, name + ".rec");

		if (ge.getRecFileNameForSaving() != null) {
			r = new File(ge.getRecFileNameForSaving());
		}

		this.nodeFile = new PrintWriter(new FileWriter(n));
		this.edgeFile = new PrintWriter(new FileWriter(e));
		this.ConsoleOutputFile = new PrintWriter(new FileWriter(c));
		this.RecFile = new PrintWriter(new FileWriter(r));
	}

	public void setRecFilename(String path) {
		File r = new File(path);
		try {
			this.RecFile = new PrintWriter(new FileWriter(r));
			
		} catch (Exception e) {
			this.systemOut.println(e.toString());
		}
	}

	/**
     * 
     */
	public void processReqeust(String sender, List<String> receivers, IMessage message)
			throws DisJException {

		Random ran = new Random(System.currentTimeMillis());
		List<Event> newEvents = new ArrayList<Event>();
		Node sNode = graph.getNode(sender);
		String msgLabel = message.getLabel();
		Serializable data = message.getContent();
	
		// sending message
		for (int i = 0; i < receivers.size(); i++) {
			Edge recv = sNode.getEdge(receivers.get(i));
			this.updateEdgeLog(recv, msgLabel, sender);

			// validate the probability of failure
			if (!recv.isReliable()) {
				int prob = ran.nextInt(100) + 1;
				if (prob <= recv.getProbOfFailure()) {
					this.logEdgeFailurMsg(recv, message.getLabel(), sender);
					continue;
				}
			}
			
			int execTime = recv.getDelayTime(sNode, this.timeGen
					.getCurrentTime(graph.getId() + ""));
			int eventId = this.timeGen.getLastestId(graph.getId() + "");
			
			// tracking message sent to each target
			this.graph.countMsgSent(msgLabel);
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
		this.timeGen.setCurrentTime(graph.getId() + "", this.queue.topEvent()
				.getExecTime());
		
		// update log
		this.updateSentLog(sNode, receivers, message.getLabel());
	}

	/*
	 * 
	 */
	protected void internalNotify(String owner, IMessage message)
			throws DisJException {
		
		if (message.getLabel().equals(IConstants.MESSAGE_SET_ALARM_CLOCK)) {
			
			int eventId = this.timeGen.getLastestId(graph.getId() + "");
			int delay = ((Integer) message.getContent()).intValue();
			int execTime = this.timeGen.getCurrentTime(graph.getId() + "")
					+ delay;

			// add an event into a queue
			Event e = new MsgPassingEvent(owner, IConstants.EVENT_ALARM_RING_TYPE, execTime,
					eventId, owner, message);
			this.queue.pushEvent(e);

			// update time's lower bound, which may be the alarm ringing is
			// a smallest
			this.timeGen.setCurrentTime(graph.getId() + "", this.queue
					.topEvent().getExecTime());

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
					int time = timeGen.getCurrentTime(graph.getId() + "");
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
	private void updateSentLog(Node sender, List<String> recvPorts, String msgLabel) {
		/*
		StringBuffer buff = new StringBuffer();
		buff.append(msgLabel);
		buff.append(IConstants.MAIN_DELIMETER);
		*/
		for (int i = 0; i < recvPorts.size(); i++) {
//			if (i > 0)
//				buff.append(IConstants.SUB_DELIMETER);
//			buff.append(recvPorts.get(i));
			sender.incMsgSend();
		}
		/*
		sender.logMsgSent(buff.toString());
		this.nodeFile.println(IConstants.SEND_TAG + sender.getNodeId()
				+ IConstants.DELIMETER + buff.toString());
		*/
	}

	/*
	 * Update node's log after received message
	 */
	private void updateRecvLog(Node receiver, String port, String msgLabel) {
		receiver.incMsgRecv();
		/*
		receiver.logMsgRecv(msgLabel + IConstants.MAIN_DELIMETER + port);
		this.nodeFile.println(IConstants.RECV_TAG + receiver.getNodeId()
				+ IConstants.DELIMETER + msgLabel + IConstants.MAIN_DELIMETER
				+ port);
		*/
	}

	/*
	 * Update edge's log
	 */
	private void updateEdgeLog(Edge edge, String msgLabel, String sender) {
		edge.incNumMsg();
		/*
		edge.logMsgPassed(msgLabel, sender);
		this.edgeFile.println(IConstants.EDGE_TAG + edge.getEdgeId()
				+ IConstants.DELIMETER + msgLabel + IConstants.MAIN_DELIMETER
				+ sender);
		*/
	}

	/*
	 * Update edge's log
	 */
	private void logEdgeFailurMsg(Edge edge, String msgLabel, String sender) {
		edge.incNumMsg();
		/*
		edge.logMsgPassed(msgLabel, sender);
		this.edgeFile.println("Lost message @ " + IConstants.EDGE_TAG
				+ edge.getEdgeId() + IConstants.DELIMETER + msgLabel
				+ IConstants.MAIN_DELIMETER + sender);
		*/
	}

	/**
	 * TODO Flush the logs and clean up memories. Also generate reports
	 * 
	 * @throws DisJException
	 */
	public void cleanUp() {

		String title = "*****Statistics report of the simulation in text format*****";
/*
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

		if(this.nodeFile != null){
			this.nodeFile.flush();
			this.nodeFile.close();
			this.nodeFile = null;
		}
		
		if(this.edgeFile != null){
			this.edgeFile.flush();
			this.edgeFile.close();
			this.edgeFile = null;
		}
		
		if(this.ConsoleOutputFile != null){
			this.ConsoleOutputFile.flush();
			this.ConsoleOutputFile.close();
			this.ConsoleOutputFile = null;
		}
		
		if(this.RecFile != null){
			this.RecFile.flush();
			this.RecFile.close();
			this.RecFile = null;
		}

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
				Entity clientObj = GraphLoader.createEntityObject(client);
				clientObj.initEntity(this, init, this.stateFields);
				init.addEntity(clientObj);
			}

			Random r = new Random(System.currentTimeMillis());
			List<Event> events = new ArrayList<Event> ();
			// create a set of init events
			for (int i = 0; i < initNodes.size(); i++) {
				Node init = initNodes.get(i);
				if(init.isAlive()){
					int eventId = timeGen.getLastestId(graph.getId() + "");
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
			this.timeGen.setCurrentTime(graph.getId() + "", this.queue
					.topEvent().getExecTime());

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
									.println("@[Processor].executeEvent() breakpoint=true: "
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
			} else if ((recv.hasInitializer() == false)
					|| (recv.isInitExec() == true)) {
				
				// update log
				this.updateRecvLog(recv, port, e.getMessage().getLabel());
				
				// track number of msg received
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
				// create and assign it to the node
				clientObj = GraphLoader.createEntityObject(this.client);
				clientObj.initEntity(this, node, this.stateFields);
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
		return timeGen.getCurrentTime(graph.getId() + "");
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

	public void appendConsoleOutput(String string) {
		this.ConsoleOutputFile.println(string);
	}

	public void appendToRecFile(String string) {
		this.RecFile.println(string);
	}
	 
}