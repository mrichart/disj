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
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

import distributed.plugin.core.DisJException;
import distributed.plugin.core.Edge;
import distributed.plugin.core.IConstants;
import distributed.plugin.core.Node;
import distributed.plugin.runtime.Event;
import distributed.plugin.runtime.Graph;
import distributed.plugin.runtime.GraphFactory;
import distributed.plugin.runtime.GraphLoader;
import distributed.plugin.runtime.IMessage;
import distributed.plugin.runtime.IProcesses;
import distributed.plugin.ui.models.LinkElement;
import distributed.plugin.random.IRandom;

/**
 * @author npiyasin An core processor that exceutes an event for a given graph.
 *         The relation between a Processor and a Graph is bijection mapping
 */
public class Processor implements IProcesses {

	private int callingChain;
	
    private boolean stop;

    private boolean pause;

    private int speed;

    private String procName;

    private PrintWriter nodeFile;

    private PrintWriter edgeFile;

    private Class client;
    
    private Class clientRandom;

    private EventQueue queue;

    private Graph graph;

    private TimeGenerator timeGen;

    private Map stateFields;

    // private SystemLog sysLog;

    Processor(Graph graph, Class client, Class clientRandom, URL out) throws IOException {
    	this.callingChain = 0;
        this.speed = IConstants.SPEED_DEFAULT_RATE;
        this.stop = false;
        this.pause = false;

        if (graph == null)
            throw new NullPointerException(IConstants.RUNTIME_ERROR_0);

        if (client == null)
            throw new NullPointerException(IConstants.RUNTIME_ERROR_0);

        this.graph = graph;
        this.procName = graph.getId();
        this.client = client;
        this.clientRandom = clientRandom;
        this.queue = new EventQueue();
        this.timeGen = TimeGenerator.getTimeGenerator();
        this.timeGen.addGraph(graph.getId() + "");
        this.stateFields = new HashMap();

        this.initClientStateVariables();
        if(this.clientRandom != null){
        	this.initClientRandomStateVariables();
        }
        this.initLogFile(out);
    }

    private void initClientStateVariables() {

        try {
            Field[] states = this.client.getFields();
            Object obj = this.client.newInstance();
          //  IRandom ran = this.clientRandom.newInstace();
            for (int i = 0; i < states.length; i++) {
                int mod = states[i].getModifiers();
                if (Modifier.isPublic(mod) && Modifier.isFinal(mod)
                        && states[i].getType().equals(int.class)) {
                    String name = states[i].getName();
                    String tmpName = name.toLowerCase();
                    if ((tmpName.startsWith("state") || tmpName
                            .startsWith("_state"))) {
                        Object value = states[i].get(obj);
                        this.stateFields.put(value, name);
                    }
                }
            }
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    
    private void initClientRandomStateVariables() {

        try {
            Field[] states = this.clientRandom.getFields();
            IRandom ran = (IRandom)this.clientRandom.newInstance();
            this.graph.setClientRandom(ran);
           
    		GraphFactory.addGraph(this.graph);
    		
    	} catch (DisJException e) {
    		System.err.println("Error add graph into map "+ e);
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /*
     * Initialize the log's files
     */
    private void initLogFile(URL url) throws IOException {
        File dir = new File(url.getPath());
        StringTokenizer tok = new StringTokenizer(this.graph.getId(), ".");
        String name = tok.nextToken();
        File n = new File(dir, name + "_Node" + ".log");
        File e = new File(dir, name + "_Edge" + ".log");
        this.nodeFile = new PrintWriter(new FileWriter(n));
        this.edgeFile = new PrintWriter(new FileWriter(e));
    }

    /**
     * 
     */
    public void processMessage(String sender, List receivers, IMessage message)
            throws DisJException {

        Random ran = new Random(System.currentTimeMillis());
        List newEvents = new ArrayList();
        Node sNode = graph.getNode(sender);
        // sending message
        for (int i = 0; i < receivers.size(); i++) {
            Edge recv = sNode.getEdge((String) receivers.get(i));
            this.updateEdgeLog(recv, message.getLabel(), sender);

            // validate the probability of failure
            if (!recv.isReliable()) {
            	int prob = ran.nextInt(100) + 1;
				if (prob <= recv.getProbOfFailure()) {
					this.logFailMsg(recv, message.getLabel(), sender);
					continue;
				}
            }
            
            
            
            int execTime = recv.getDelayTime(sNode, this.timeGen
                    .getCurrentTime(graph.getId() + ""));
            int eventId = this.timeGen.getLastestId(graph.getId() + "");
            try {
                IMessage msg = new Message(message.getLabel(), GraphLoader
                        .deepClone(message.getContent()));
                newEvents.add(new Event(sender, IConstants.RECEIVE_MSG_TYPE,
                        execTime, eventId, recv.getEdgeId(), msg));
            } catch (IOException ie) {
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

    public void internalNotify(String owner, IMessage message)
            throws DisJException {
        if (message.getLabel().equals(IConstants.SET_ALARM_CLOCK)) {
            int eventId = this.timeGen.getLastestId(graph.getId() + "");
            int delay = ((Integer) message.getContent()).intValue();
            int execTime = this.timeGen.getCurrentTime(graph.getId() + "") + delay;

            // add an event into a queue
            Event e = new Event(owner, IConstants.ALARM_RING_TYPE, execTime,
                    eventId, owner, message);
            this.queue.pushEvent(e);

            // update time's lower bound, which may be the alarm ringing is
            // a smallest
            this.timeGen.setCurrentTime(graph.getId() + "", this.queue
                    .topEvent().getExecTime());
            
        } else if( message.getLabel().equals(IConstants.SET_BLOCK_MSG)){
            // set blocked/unblocked message
            Object[] msg = (Object[]) message.getContent();
            String port = (String) msg[0];
            boolean block = ((Boolean) msg[1]).booleanValue();
            Node node = this.graph.getNode(owner);
            if (!block) {
            	// flush blocked messages after unblock port
                List events = node.getBlockedEvents(port);
                if (events != null) {
                    // put them in a queue with the current execution time
                    int time = timeGen.getCurrentTime(graph.getId() + "");
                    for (int i = 0; i < events.size(); i++) {
                        Event e = (Event) events.get(i);
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
    private void updateSentLog(Node sender, List recvPorts, String msgLabel) {
        StringBuffer buff = new StringBuffer();
        buff.append(msgLabel);
        buff.append(IConstants.MAIN_DELIMETER);
        for (int i = 0; i < recvPorts.size(); i++) {
            if (i > 0)
                buff.append(IConstants.SUB_DELIMETER);
            buff.append(recvPorts.get(i));
            sender.incMsgSend();
        }
        sender.logMsgSent(buff.toString());
        this.nodeFile.println(IConstants.SEND_TAG + sender.getNodeId()
                + IConstants.DELIMETER + buff.toString());
    }

    /*
     * Update node's log after received message
     */
    private void updateRecvLog(Node receiver, String port, String msgLabel) {
        receiver.incMsgRecv();
        receiver.logMsgRecv(msgLabel + IConstants.MAIN_DELIMETER + port);
        this.nodeFile.println(IConstants.RECV_TAG + receiver.getNodeId()
                + IConstants.DELIMETER + msgLabel + IConstants.MAIN_DELIMETER
                + port);
    }

    /*
     * Update edge's log
     */
    private void updateEdgeLog(Edge edge, String msgLabel, String sender) {
        edge.incNumMsg();
        edge.logMsgPassed(msgLabel, sender);
        this.edgeFile.println(IConstants.EDGE_TAG + edge.getEdgeId()
                + IConstants.DELIMETER + msgLabel + IConstants.MAIN_DELIMETER
                + sender);
    }

    /*
     * Update edge's log
     */
    private void logFailMsg(Edge edge, String msgLabel, String sender) {
        edge.incNumMsg();
        edge.logMsgPassed(msgLabel, sender);
        this.edgeFile.println("Lost message @ " + IConstants.EDGE_TAG + edge.getEdgeId()
                + IConstants.DELIMETER + msgLabel + IConstants.MAIN_DELIMETER
                + sender);
    }
    /**
     * TODO Flush the logs and clean up memories. Also generate reports
     * 
     * @throws DisJException
     */
    public void cleanUp() {
        
        System.out.println("*****Statistics report of the simulation in text format*****");
        Map nodes = graph.getNodes();
        Iterator it = nodes.keySet().iterator();
        while (it.hasNext()) {
            System.out.println(this.graph.getNode((String) it.next()));
        }

        Map edges = graph.getEdges();
        it = edges.keySet().iterator();
        while (it.hasNext()) {
            System.out.println(this.graph.getEdge((String) it.next()));
        }
        // clean up the memory
        this.graph = null;

        this.nodeFile.close();
        this.edgeFile.close();

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
            // load and init any neccessary data
            this.loadInitNodes();

            // start execute events
            this.executeEvents();

            System.out.println("\n*****Simulation for " + this.procName
                    + " is successfully over.*****");

        } catch (DisJException e) {
            // system log here
            e.printStackTrace();
            System.err.println(e);

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            this.cleanUp();
            System.out.println("\n*****The simulation of " + this.procName
                    + " is terminated.*****");
        }
    }

    /*
     * Load client object into init nodes and create init event(s)
     */
    private void loadInitNodes() throws DisJException {

        List initNodes = GraphLoader.getInitNodes(graph);
        try {
            for (int i = 0; i < initNodes.size(); i++) {
                Node init = (Node) initNodes.get(i);
                Entity clientObj = GraphLoader.createEntityObject(client);
                clientObj.initEntity(this, init, this.stateFields);
                init.assignEntity(clientObj);
            }

            Random r = new Random(System.currentTimeMillis());
            List events = new ArrayList();
            // create a set of init events
            for (int i = 0; i < initNodes.size(); i++) {
                Node init = (Node) initNodes.get(i);
                int eventId = timeGen.getLastestId(graph.getId() + "");
                int execTime = 0;
                if (!init.isStarter())
                    execTime = r.nextInt(IConstants.MAX_RANDOM_RANGE);

                // add an event into a queue
                IMessage msg = new Message("Initialized", new Integer(execTime));
                Event e = new Event(init.getNodeId(), IConstants.INITIATE_TYPE,
                        execTime, eventId, init.getNodeId(), msg);

                events.add(e);
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
     * 
     */
    private void executeEvents() throws DisJException {

        while (!this.queue.isEmpty()) {
            if (this.stop)
                break;

            // To slow down the simulation speed
            try {
                Thread.sleep(this.speed);
            } catch (InterruptedException ignore) {
                System.out.println("slow down process with speed: "
                        + this.speed + " =>" + ignore);
            }

            // suspend the process and/or hit breakpoint
            if (!this.pause) {
                Event e = (Event) this.queue.topEvent();
                Node thisNode = this.graph.getNode(e.getOwner());

                if (thisNode.getBreakpoint() == true) {
                    this.setPause(true);
                    while (this.pause) {
                        if (this.stop)
                            break;
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ignore) {
                            System.err
                                    .println("@[Processor].executeEvent() breakpoint=true: "
                                            + ignore);
                        }
                    }
                }

                if (this.stop)
                    break;

                if (e.getEventType() == IConstants.RECEIVE_MSG_TYPE) {
                    this.invokeReceive(this.queue.popEvents());

                    // tracking a length of message calling chain
                    this.callingChain++;
                    
                } else if (e.getEventType() == IConstants.ALARM_RING_TYPE) {
                    this.invokeAlarmRing(this.queue.popEvents());
                    
                } else {
                    this.invokeInit(this.queue.popEvents());
                }
                
                // FIXME print out the message to console that related to this
                // entity then clean it up.
                
                
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignore) {
                    System.err.println("@Processor.executeEvent() pause=true: "
                            + ignore);
                }
            }
           
        }
    }

    /*
     * Invoke client's init()
     */
    private void invokeInit(List events) throws DisJException {
        for (int i = 0; i < events.size(); i++) {
            Event e = (Event) events.get(i);
            Node node = this.graph.getNode(e.getOwner());
            
            // Will NOT execute a Fail node
            if(node.isStarter()){          	
	            Entity entity = this.loadEntity(node);
	            node.setInitExec(true);
	            entity.init();
	
	            // catching up the holding events
	            // this can happen only to recvMsg
	            // since setAlarm will not be able to
	            // occured until initNode is initailized
	            this.invokeReceive(node.getHoldEvents());
            }
        }
    }

    /*
     * Invoke client's receive() w.r.t the target node
     */
    private void invokeReceive(List events) throws DisJException {
        List invokeList = new ArrayList();
        for (int i = 0; i < events.size(); i++) {
            Event e = (Event) events.get(i);

            // retrieve a receiver node for this event
            Edge link = this.graph.getEdge(e.getEdgeName());
            Node recv = link.getOthereEnd(this.graph.getNode(e.getOwner()));
            String port = recv.getPortLabel(link);

            // Will NOT execute a Fail node
            if(!recv.isStarter())
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
                // update log
                this.updateRecvLog(recv, port, e.getMessage().getLabel());

                Entity entity = this.loadEntity(recv);
                String recvPort = GraphLoader.getEdgeLabel(recv, link);
                Object[] params = new Object[] { e, entity, recvPort };
                invokeList.add(params);

            } else {
            	throw new DisJException(IConstants.ERROR_23, "@Processor.invokeReceiver() ");             
            }
        }

        // invoke receive() on a target node
        for (int i = 0; i < invokeList.size(); i++) {
            Object[] params = (Object[]) invokeList.get(i);
            Event e = (Event) params[0];
            Entity entity = (Entity) params[1];
            String portLabel = (String) params[2];
            entity.getNodeOwner().setLatestRecvPort(portLabel);
            entity.receive(portLabel, e.getMessage());
        }
    }

    /*
     * Internal clock ring, invoke client's alarmRing()
     */
    private void invokeAlarmRing(List events) throws DisJException {
        for (int i = 0; i < events.size(); i++) {
            Event e = (Event) events.get(i);
            Node node = this.graph.getNode(e.getOwner());
            
            // Will NOT execute a Fail node
            if(node.isStarter()){           
	            Entity entity = this.loadEntity(node);
	            entity.alarmRing();
            }
        }
    }

    /*
     * Lazy initialize entity
     */
    private Entity loadEntity(Node node) throws DisJException {
        // get client from receiver node
        Entity clientObj = node.getEntity();
        if (clientObj == null) {
            try {
                // lazy init
                clientObj = GraphLoader.createEntityObject(this.client);
                clientObj.initEntity(this, node, this.stateFields);
                node.assignEntity(clientObj);
            } catch (Exception ex) {
                throw new DisJException(IConstants.ERROR_8, ex.toString());
            }
        }
        return node.getEntity();
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
    
    public int getCurrentTime(){
    	return timeGen.getCurrentTime(graph.getId() + "");
    }
    
    public int getCallingChain(){
    	return this.callingChain;
    }
}