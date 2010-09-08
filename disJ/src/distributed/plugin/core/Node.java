/*******************************************************************************
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     DisJ Development Group
 *******************************************************************************/

package distributed.plugin.core;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import distributed.plugin.runtime.Event;
import distributed.plugin.runtime.IDistributedModel;

/**
 * @author Me
 * 
 *         A Node Class that stores an information about an entity
 */
public class Node implements Serializable {

	static final long serialVersionUID = IConstants.SERIALIZE_VERSION;

	/** a unique id of this node for system use */
	private String nodeId;

	private String graphId;

	/** a name of this node for user use */
	private String name;

	private int numMsgRecv;

	private int numMsgSend;

	private String latestRecvPort;

	private int curState;

	/*
	 * Number of agent to be started at this node
	 * TODO add this to GUI (follow isInitializer ref)
	 */
	private int numInitPerHost;
	
	private String userInput;


	/**
	 * specify as a starter for init node
	 */
	private boolean isStartHost;

	private boolean breakpoint;

	/**
	 * keeping track whether if this is init node has executed init()
	 */
	private boolean initExec;

	/**
	 * re-ref after assigned
	 */
	private NodeLog log;

	/**
	 * Edges that connected to node{port label, edge}
	 */
	private Map<String, Edge> edges;
	
	/**
	 * a list of blocking state of each port{port label,boolean}
	 */
	private Map<String, Boolean> blockPort;

	/**
	 * mapping between port's name and port's type {portlabel, type (Short)} i.e
	 * {portA, BI_DIRECTION} {portB, OUT_DIRECTION} {portC, IN_DIRECTION}
	 */
	private Map<String, Short> ports;

	/**
	 * a list of msg queue for blocking message {localport label, list of
	 * events}
	 */
	transient private Map<String, List<Event>> blockMsg;

	/**
	 * keeping track of user internal data that stay at this node
	 * 
	 */
	transient private List<IDistributedModel> entity;

	/**
	 * hold the events if initializer host has other actions before it has been
	 * initialized to notify the change of state a list of state-name pair of
	 * the corresponding entity
	 */
	transient private List<Event> holdEvents;

	/**
	 * A mapping table of every possible state name and value defined by user
	 */
	transient private Map<Integer, String> stateNames;
	
	transient private List<Agent> curAgents;
	
	transient private List<String> whiteboard;
	
	/**
	 * X, Y coordinate in graph editor
	 */
	private int x;

	private int y;
	
	private int maxX;
	
	private int maxY;
	
	
	protected PropertyChangeSupport listeners = new PropertyChangeSupport(this);
	
	/**
	 * Constructor
	 * @param nodeId
	 */
	public Node(String nodeId) {
		this("unknown", nodeId, "", 0, true);
	}

	public Node(String graphId, String nodeId) {
		this(graphId, nodeId, "", 0, true);
	}

	public Node(String graphId, String nodeId, int x, int y) {
		this(graphId, nodeId, "", 0, true, x, y);
	}

	public Node(String graphId, String nodeId, String name, int numInit,
			boolean isStarter) {
		this(graphId, nodeId, name, numInit, isStarter, 0, 0);

	}

	public Node(String graphId, String nodeId, String name, int numInit,
			boolean isStartHost, int x, int y) {
		this.x = x;
		this.y = y;
		this.maxX = 0;
		this.maxY = 0;
		this.nodeId = nodeId;
		this.graphId = graphId;
		this.name = name;
		this.breakpoint = false;
		this.numInitPerHost = numInit;		
		this.initExec = false;
		this.isStartHost = isStartHost;
		this.numMsgRecv = 0;
		this.numMsgSend = 0;
		this.latestRecvPort = null;
		this.entity = new ArrayList<IDistributedModel>();
		this.userInput = "";
		this.log = new NodeLog(graphId);
		this.edges = new HashMap<String, Edge>();
		this.ports = new HashMap<String, Short>(4);
		this.blockMsg = new HashMap<String, List<Event>>(4);
		this.blockPort = new HashMap<String, Boolean>(4);
		this.holdEvents = new ArrayList<Event>(4);
		this.stateNames = new HashMap<Integer, String>();
		this.curAgents = new ArrayList<Agent>();
		this.whiteboard = new ArrayList<String>();

	}

	public void addPropertyChangeListener(PropertyChangeListener l) {
		// System.out.println("[Node] addPropertyChangeListener: " + l);
		listeners.addPropertyChangeListener(l);
	}

	public void firePropertyChange(String prop, Object old, Object newValue) {
		// System.out.println("[Node] firePropertyChange: " + prop);
		listeners.firePropertyChange(prop, old, newValue);
	}

	/**
	 * Get a unique ID for this node
	 * 
	 * @return String a unique number
	 */
	public String getNodeId() {
		return this.nodeId;
	}

	/**
	 * Set a state-name pairs list of this node
	 * 
	 * @param states
	 */
	public void setStateNames(Map<Integer, String> states) {
		this.stateNames = states;
	}

	/**
	 * Return a state's name corresponding to a given state if the state's name
	 * does not find it will return "state not found"
	 * 
	 * @param state
	 * @return
	 */
	public String getStateName(int state) {
		if (this.stateNames.containsKey(state)) {
			return (String) this.stateNames.get(state);
		} else {
			return IConstants.STATE_NOT_FOUND;
		}
	}
	
	/**
	 * Add agent who currently visiting this node
	 * @param agent
	 */
	public void addAgent(Agent agent){
		if(!curAgents.contains(agent)){
			this.curAgents.add(agent);
		}
	}
	
	/**
	 * Remove agent who leaving this node
	 * @param agent
	 */
	public void removeAgent(Agent agent){
		if(curAgents.contains(agent)){
			this.curAgents.remove(agent);
		}
	}
	
	/**
	 * Get a list of all agent currently reside at this node
	 * @return
	 */
	public List<Agent> getAllAgents(){
		return this.curAgents;
	}

	/**
	 * Add more link out of this node
	 * 
	 * @param label
	 *            A local label of the edge for this node
	 * @param type
	 *            A type of port
	 * @param edge
	 * @throws DisJException
	 */
	public void addEdge(String label, short type, Edge edge)
			throws DisJException {

		if (label == null) {
			throw new DisJException(IConstants.ERROR_22,
					"@Node.addEdge() link type: " + type + edge);
		}
		if (!this.edges.containsKey(label) && !this.edges.containsValue(edge)) {
			this.edges.put(label, edge);
			this.ports.put(label, new Short(type));
			this.blockPort.put(label, new Boolean(false));

		} else {
			throw new DisJException(IConstants.ERROR_2, label);
		}
	}

	/**
	 * Remove the given edge that connected to this node
	 * 
	 * @param edge
	 * @throws DisJException
	 */
	public void removeEdge(Edge edge) throws DisJException {
		if (!this.edges.containsValue(edge))
			return;

		String key = this.getPortLabel(edge);
		this.edges.remove(key);
		this.ports.remove(key);
		this.blockPort.remove(key);
	}

	/**
	 * Get a link from this node w.r.t a given name
	 * 
	 * @param label
	 *            A local label of the edge
	 * @return
	 * @throws DisJException
	 */
	public Edge getEdge(String label) throws DisJException {
		if (this.edges.containsKey(label))
			return (Edge) this.edges.get(label);
		else
			throw new DisJException(IConstants.ERROR_1, label);
	}

	public List<String> getWhiteboard() {
		return whiteboard;
	}

	public void setWhiteboard(List<String> whiteboard) {
		this.whiteboard = whiteboard;
	}

	/**
	 * Get a port label of this node that connected to a given edge
	 * 
	 * @param edge
	 * @return
	 * @throws DisJException
	 */
	public String getPortLabel(Edge edge) throws DisJException {
		if (this.edges.containsValue(edge)) {
			Iterator<String> it = this.edges.keySet().iterator();
			while (it.hasNext()) {
				String label = it.next();
				if (this.edges.get(label).equals(edge))
					return label;
			}
			throw new DisJException(IConstants.ERROR_1,
					"This should not happen");

		} else
			throw new DisJException(IConstants.ERROR_1, edge.toString());
	}

	/**
	 * Set a new local port of this node that connected to a given edge
	 * 
	 * @param port
	 *            a new port name
	 * @param edge
	 *            an edge that connect to this node
	 * @throws DisJException
	 *             if edge not found
	 */
	public void setPortLable(String port, Edge edge) throws DisJException {

		// port already exist
		if (this.edges.containsKey(port)){
			return;
		}
		
		// set the edges collections
		String old = this.getPortLabel(edge);
		this.edges.remove(old);
		this.edges.put(port, edge);

		// set the port type tracking collection
		Short portType = this.ports.get(old);
		this.ports.remove(old);
		this.ports.put(port, portType);

	}

	/**
	 * Get a current state of a given port
	 * 
	 * @param label
	 * @return
	 * @throws DisJException
	 */
	public boolean isBlocked(String label) throws DisJException {
		if (!this.blockPort.containsKey(label))
			throw new DisJException(IConstants.ERROR_1, label);
		return ((Boolean) this.blockPort.get(label)).booleanValue();
	}

	/**
	 * Set a new state of a given port
	 * 
	 * @param label
	 * @param state
	 * @throws DisJException
	 */
	public void setPortBlock(String label, boolean state) throws DisJException {
		if (!this.blockPort.containsKey(label))
			throw new DisJException(IConstants.ERROR_1, "@Node.setPortBlock() "
					+ label);

		if (state == true) {
			// create a block message holding place
			if (!this.blockMsg.containsKey(label))
				this.blockMsg.put(label, new ArrayList<Event>());
		}
		this.blockPort.put(label, new Boolean(state));
	}

	/**
	 * Add a new event to a blocked list for a given port of this node
	 * 
	 * @param portLabel
	 *            A port that has been blocked
	 * @param event
	 */
	public void addEventToBlockedList(String portLabel, Event event)
			throws DisJException {
		if (!this.blockMsg.containsKey(portLabel))
			throw new DisJException(IConstants.ERROR_14, portLabel);

		List<Event> events = this.blockMsg.get(portLabel);
		events.add(event);
		this.blockMsg.put(portLabel, events);
	}

	/**
	 * Log a message that this node received
	 * 
	 * @param log
	 */
	public void logMsgRecv(String log) {
		this.log.logMsgRecv(log);
	}

	/**
	 * Log messages that sent by this node
	 * 
	 * @param log
	 */
	public void logMsgSent(String log) {
		this.log.logMsgSent(log);
	}

	/**
	 * Get all event that has been blocked on a given port
	 * 
	 * @param portLabel
	 * @return
	 */
	public List<Event> getBlockedEvents(String portLabel) {
		return this.blockMsg.get(portLabel);
	}

	/**
	 * Remove all event that has been blocked on a given port
	 * 
	 * @param portLabel
	 */
	public void clearBlockedPort(String portLabel) {
		this.blockMsg.remove(portLabel);
	}

	/**
	 * Add a user algorithm representative who will stay at this node
	 * 
	 * @param entity A user algorithm object
	 * @throws DisJException
	 */
	public void addEntity(IDistributedModel entity) throws DisJException {
		if (entity == null)
			throw new DisJException(IConstants.ERROR_22);

		this.entity.add(entity);
	}

	/**
	 * Increment number of message receive and log the receiver
	 */
	public void incMsgRecv() {
		this.numMsgRecv++;
	}

	/**
	 * Increment number of message is sent and log the msg sent
	 * 
	 */
	public void incMsgSend() {
		this.numMsgSend++;
	}

	/**
	 * Write the current state into a readable format
	 */
	public String toString() {
		return ("\n\nNode: " + this.name + "\nState: " + this.curState
				+ "\nInit: " + this.hasInitializer() + "\nStarter: "
				+ this.isStartHost);
	}

	/**
	 * @return Returns the numMsgRecv.
	 */
	public int getNumMsgRecv() {
		return numMsgRecv;
	}

	/**
	 * @return Returns the numMsgSend.
	 */
	public int getNumMsgSend() {
		return numMsgSend;
	}

	/**
	 * @return Returns the entity.
	 */
	public List<IDistributedModel> getAllEntities() {
		return entity;
	}

	/**
	 * Check whether a node contains any initializer
	 * @return Returns the isInit.
	 */
	public boolean hasInitializer() {
		return this.numInitPerHost > 0;		
	}	
	
	/**
	 * Get number of initializer of this node
	 * @return
	 */
	public int getNumInit() {
		return numInitPerHost;
	}
	
	/**
	 * Set number of initializer to this node
	 * @param numInit
	 *            
	 */
	public void setNumInit(int numInit) {
		this.numInitPerHost = numInit;
	}

	public void setGraphId(String id) {
		this.graphId = id;
	}

	/**
	 * Get a current state, which is the last element in the list
	 * 
	 * @return Returns the state.
	 */
	public int getCurState() {
		return this.curState;
	}

	/**
	 * Initialize a start state of this node
	 * 
	 * @param state
	 */
	public void initStartState(int state) {
		this.curState = state;
		this.log.logState(this.getStateName(this.curState));
	}

	/**
	 * Add a new state into the last element of the list
	 * 
	 * @param state
	 *            The state to set.
	 */
	public void setCurState(int state) {
		this.curState = state;
		this.firePropertyChange(IConstants.PROPERTY_CHANGE_NODE_STATE, null,
				new Integer(this.curState));
		try {
			String newState = this.getStateName(this.curState);
			this.log.logState(newState);
		} catch (Exception e) {
			// do nothing
		}
	}

	public void resetState(int state) {
		this.curState = state;
	}

	/**
	 * @return Returns the edges.
	 */
	public Map<String, Edge> getEdges() {
		return edges;
	}

	public List<String> getStateList() {
		return this.log.getStates();
	}

	public void setStateList(List<String> list) {
		this.log.setStates(list);
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	public String getGraphId() {
		return this.graphId;
	}

	/**
	 * @return Returns the latestRecvFrom.
	 */
	public String getLatestRecvPort() {
		return latestRecvPort;
	}

	/**
	 * @param latestRecvFrom
	 *            The latestRecvFrom to set.
	 */
	public void setLatestRecvPort(String latestRecvFrom) {
		this.latestRecvPort = latestRecvFrom;
	}

	/**
	 * Check whether this node is a host
	 * @return 
	 */
	public boolean isStartHost() {
		return isStartHost;
	}

	/**
	 * @return Returns the hasInit.
	 */
	public boolean isInitExec() {
		return initExec;
	}

	/**
	 * @param hasInit
	 *            The hasInit to set.
	 */
	public void setInitExec(boolean hasInit) {
		this.initExec = hasInit;
	}

	/**
	 * @return Returns the holdEvents.
	 */
	public List<Event> getHoldEvents() {
		return this.holdEvents;
	}

	/**
	 * @return Returns the ports.
	 */
	public Map<String, Short> getPorts() {
		return this.ports;
	}

	/**
	 * Get all possible outgoing ports
	 * 
	 * @return
	 */
	public List<String> getOutgoingPorts() {
		List<String> out = new ArrayList<String>();
		for (String port : this.ports.keySet()) {
			short type = this.ports.get(port);
			if (type == IConstants.DIRECTION_OUT
					|| type == IConstants.DIRECTION_BI) {
				out.add(port);
			}
		}
		return out;
	}

	/**
	 * Get all possible incoming ports
	 * 
	 * @return
	 */
	public List<String> getIncomingPorts() {
		List<String> in = new ArrayList<String>();
		for (String port : this.ports.keySet()) {
			short type = this.ports.get(port);
			if (type == IConstants.DIRECTION_IN
					|| type == IConstants.DIRECTION_BI) {
				in.add(port);
			}
		}
		return in;
	}

	/**
	 * Set this node to be a host
	 * @param isStarter
	 *            
	 */
	public void setStarHost(boolean isStarter) {
		this.isStartHost = isStarter;
	}

	/**
	 * @param name
	 *            The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	public boolean getBreakpoint() {
		return this.breakpoint;
	}

	public void setBreakpoint(boolean breakpoint) {
		this.breakpoint = breakpoint;
	}

	public void setNumMsgRecv(int numMsgRecv) {
		this.numMsgRecv = numMsgRecv;
	}

	public void setNumMsgSend(int numMsgSend) {
		this.numMsgSend = numMsgSend;
	}

	/**
	 * Clear all entities that stay at this node
	 */
	public void clearEntities() {
		this.entity.clear();
	}
	
	/**
	 * remove a given entity from this node
	 * @param entity
	 */
	public boolean removeEntity(IDistributedModel entity){
		return this.entity.remove(entity);
	}

	public Map<String, Boolean> getBlockPort() {
		return this.blockPort;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public String getUserInput() {
		return this.userInput;
	}

	public void setUserInput(String userInput) {
		if(userInput == null){
			userInput = "";
		}
		this.userInput = userInput;
	}

	public int getMaxX() {
		return maxX;
	}

	public void setMaxX(int maxX) {
		this.maxX = maxX;
	}

	public int getMaxY() {
		return maxY;
	}

	public void setMaxY(int maxY) {
		this.maxY = maxY;
	}
	
	/*
     * Overriding serialize object due to Java Bug4152790
     */
    private void writeObject(ObjectOutputStream os) throws IOException{    	
   	 	// write the object
		os.defaultWriteObject();
	}
    /*
     * Overriding serialize object due to Java Bug4152790
     */
    private void readObject(ObjectInputStream os) throws IOException, ClassNotFoundException  {
    	 // rebuild this object
    	 os.defaultReadObject();
    	 this.blockMsg = new HashMap<String, List<Event>>();
    	 this.holdEvents = new ArrayList<Event>();
    	 this.stateNames = new HashMap<Integer, String>();
    	
    	 // clean up log
    	 this.log.clear();
    	 	
    }


}
