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

import distributed.plugin.core.Logger.logTag;
import distributed.plugin.runtime.Event;
import distributed.plugin.runtime.IDistributedModel;
import distributed.plugin.runtime.engine.Entity;
import distributed.plugin.runtime.engine.AgentModel.NotifyType;
import distributed.plugin.stat.NodeStat;

/**
 * @author Me
 * 
 *         A Node Class that stores an information about an entity
 */
public class Node implements Serializable {

	static final long serialVersionUID = IConstants.SERIALIZE_VERSION;

	/*
	 * a unique id of this node for system use 
	 */
	private String nodeId;

	private String graphId;

	/* 
	 * a name of this node for user use 
	 */
	private String name;

	private String latestRecvPort;

	private int curState;

	/*
	 * Number of agent to be started at this node
	 */
	private int numInitAgent;
	
	/*
	 * State indicates whether this node is a host/initializer
	 * node
	 */
	private boolean isInit;
	
	/*
	 * Number of token currently located in this node
	 */
	private int numToken;
	
	private String userInput;


	/*
	 * specify as a starter for init node
	 */
	private boolean isAlive;

	private boolean breakpoint;

	/*
	 * keeping track whether if this is init node has executed init()
	 */
	private boolean initExec;

	/*
	 * Edges that connected to node{port label, edge}
	 */
	private Map<String, Edge> edges;
	
	/*
	 * mapping between port's name and port's type {portlabel, type (Short)} i.e
	 * {portA, BI_DIRECTION} {portB, OUT_DIRECTION} {portC, IN_DIRECTION}
	 */
	private Map<String, Short> ports;
	
	/*
	 * Logger that will log the node activities
	 */
	transient private Logger log;

	/*
	 * keeping track of user internal data that stay at this node
	 * 
	 */
	transient private IDistributedModel entity;

	/*
	 * a list of msg queue for blocking message {localport label, list of
	 * events}
	 */
	transient private Map<String, List<Event>> blockedPorts;
	
	/*
	 * a list of blocking state of each port{port label,boolean}
	 */
	transient private Map<String, Boolean> portStates;

	/*
	 * hold the events if initializer host has other actions before it has been
	 * initialized to notify the change of state a list of state-name pair of
	 * the corresponding entity
	 */
	transient private List<Event> holdEvents;

	/*
	 * A mapping table of every possible state name and value defined by user
	 */
	transient private Map<Integer, String> stateNames;
	
	/*
	 * A list of agent that currently resides in this node
	 */
	transient private List<Agent> curAgents;
	
	/*
	 * A whiteboard belong to this node
	 */
	transient private List<String> whiteboard;
	
	/*
	 * An event registration list of agent
	 */
	transient private Map<String, List<NotifyType>> registees;
	
	transient private NodeStat stat;
	/*
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
		this("unknown", nodeId, "", false, true);
	}

	public Node(String graphId, String nodeId) {
		this(graphId, nodeId, "", false, true);
	}

	public Node(String graphId, String nodeId, int x, int y) {
		this(graphId, nodeId, "", false, true, x, y);
	}

	public Node(String graphId, String nodeId, String name, boolean isInit,
			boolean isStarter) {
		this(graphId, nodeId, name, isInit, isStarter, 0, 0);

	}

	public Node(String graphId, String nodeId, String name, boolean isInit,
			boolean isStartHost, int x, int y) {
		this.x = x;
		this.y = y;
		this.maxX = 0;
		this.maxY = 0;
		this.nodeId = nodeId;
		this.graphId = graphId;
		this.name = name;
		this.breakpoint = false;
		this.isInit = isInit;
		this.numInitAgent = 0;		
		this.initExec = false;
		this.isAlive = isStartHost;
		this.numToken = 0;
		this.latestRecvPort = null;
		this.entity = null;
		this.log = null;
		this.userInput = "";
		this.edges = new HashMap<String, Edge>();
		this.ports = new HashMap<String, Short>();
		this.stat = new NodeStat(nodeId);
		
		this.blockedPorts = new HashMap<String, List<Event>>();		
		this.portStates = new HashMap<String, Boolean>();
		this.holdEvents = new ArrayList<Event>();
		this.stateNames = new HashMap<Integer, String>();
		this.curAgents = new ArrayList<Agent>();
		this.whiteboard = new ArrayList<String>();
		this.registees = new HashMap<String, List<NotifyType>>();

	}

	public void addPropertyChangeListener(PropertyChangeListener l) {
		listeners.addPropertyChangeListener(l);
	}

	public void firePropertyChange(String prop, Object old, Object newValue) {
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

	/*
	 * Return a state's name corresponding to a given state if the state's name
	 * does not find it will return "state not found"
	 * 
	 * @param state
	 * @return
	 */
	private String getStateName(int state) {
		if (this.stateNames.containsKey(state)) {
			return this.stateNames.get(state);
		} else {
			return IConstants.STATE_NOT_FOUND + " " + state;
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
			this.removeRegistee(agent.getAgentId());
		}
	}
	
	/**
	 * Get a list of all agent currently reside at this node
	 * @return
	 */
	public List<Agent> getAllAgents(){
		return this.curAgents;
	}

	public void clearAllAgents(){
		this.curAgents.clear();
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
			this.ports.put(label, type);
			this.portStates.put(label, false);

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
		this.portStates.remove(key);
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

	public void clearWhieboard(){
		if(this.whiteboard != null){
			this.whiteboard.clear();
		}else{
			this.whiteboard = new ArrayList<String>();
		}
	}
	/**
	 * Get a list of all registred agent to this node
	 * 
	 * @return
	 */
	public Map<String, List<NotifyType>> getRegistees() {
		return registees;
	}

	/**
	 * Add an agent with type of notification for this node event
	 * 
	 * @param agentId
	 * @param type
	 */
	public void addRegistee(String agentId, NotifyType type){
		if(this.registees.containsKey(agentId)){
			List<NotifyType> types = this.registees.get(agentId);
			if(!types.contains(type)){
				types.add(type);
				this.registees.put(agentId, types);
			}
		}else{
			List<NotifyType> types = new ArrayList<NotifyType>();
			types.add(type);
			this.registees.put(agentId, types);
		}
	}

	/**
	 * Remove a notification of an agent from this node event
	 * 
	 * @param agentId
	 * @param type
	 */
	public void removeRegistee(String agentId, NotifyType type){		
		if(this.registees.containsKey(agentId)){
			List<NotifyType> types = this.registees.get(agentId);
			if(types.contains(type)){
				types.remove(type);
				if(types.isEmpty()){
					this.registees.remove(agentId);
				}else{
					this.registees.put(agentId, types);
				}
			}
		}
	}
	
	/**
	 * Remove agent and all of it's notification types
	 * @param agentId
	 */
	public void removeRegistee(String agentId){
		if(this.registees.containsKey(agentId)){
			this.registees.remove(agentId);			
		}
	}
	
	public void clearRegistees(){
		if(this.registees != null){
			this.registees.clear();
		}else{
			this.registees = new HashMap<String, List<NotifyType>>();
		}
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
		if (!this.portStates.containsKey(label)){
			throw new DisJException(IConstants.ERROR_1, label);
		}
		return ((Boolean) this.portStates.get(label)).booleanValue();
	}

	/**
	 * Set a new state of a given port
	 * 
	 * @param label
	 * @param state
	 * @throws DisJException
	 */
	public void setPortBlock(String label, boolean state) throws DisJException {
		if (!this.portStates.containsKey(label))
			throw new DisJException(IConstants.ERROR_1, "@Node.setPortBlock() "
					+ label);

		if (state == true) {
			// create a block message holding place
			if (!this.blockedPorts.containsKey(label))
				this.blockedPorts.put(label, new ArrayList<Event>());
		}
		this.portStates.put(label, new Boolean(state));
	}

	/**
	 * Add a new event to a blocked list for a given port of this node
	 * 
	 * @param portLabel
	 *            A port that has been blocked
	 * @param event
	 */
	public void addEventToBlockedPort(String portLabel, Event event)
			throws DisJException {
		if (!this.blockedPorts.containsKey(portLabel))
			throw new DisJException(IConstants.ERROR_14, portLabel);

		List<Event> events = this.blockedPorts.get(portLabel);
		events.add(event);
		this.blockedPorts.put(portLabel, events);
	}

	/**
	 * Get all event that has been blocked on a given port
	 * 
	 * @param portLabel
	 * @return
	 */
	public List<Event> getBlockedEvents(String portLabel) {
		return this.blockedPorts.get(portLabel);
	}

	/**
	 * Remove all event that has been blocked on a given port
	 * 
	 * @param portLabel
	 */
	public void removeBlockedPort(String portLabel) {
		if(this.blockedPorts != null){
			this.blockedPorts.remove(portLabel);
		}
	}

	public void clearAllBlockedPorts(){
		if(this.blockedPorts != null){
			this.blockedPorts.clear();
		}else{
			this.blockedPorts = new HashMap<String, List<Event>>();
		}
	}
	/**
	 * Add a user algorithm representative who will stay at this node
	 * 
	 * @param entity A user algorithm object
	 * @throws DisJException
	 */
	public void addEntity(Entity entity) throws DisJException {
		if (entity == null)
			throw new DisJException(IConstants.ERROR_22);

		this.entity = entity;
	}

	/**
	 * Set logger
	 * 
	 * @param log
	 */
	public void setLogger(Logger log){
		this.log = log;
	}
	
	/**
	 * Increment number of message receive and log the receiver
	 */
	public void incMsgRecv() {
		this.stat.incNumMsgRecv();
	}

	/**
	 * Increment number of message is sent and log the msg sent
	 * 
	 */
	public void incMsgSend() {
		this.stat.incNumMsgSend();
	}

	/**
	 * Write the current state into a readable format
	 */
	public String toString() {
		return ("\n\nNode: " + this.name + "\nState: " + this.curState
				+ "\nInit: " + this.isInitializer() + "\nStarter: "
				+ this.isAlive);
	}

	/**
	 * @return Returns the numMsgRecv.
	 */
	public int getNumMsgRecv() {
		return this.stat.getNumMsgRecv();
	}

	/**
	 * @return Returns the numMsgSend.
	 */
	public int getNumMsgSend() {
		return this.stat.getNumMsgSend();
	}

	/**
	 * @return Returns the entity.
	 */
	public Entity getEntity() {
		return (Entity) this.entity;
	}
	
	/**
	 * Check whether a node contains any initializer
	 * @return Returns the isInit.
	 */
	public boolean isInitializer() {
		return isInit;
	}

	/**
	 * Set this node to be OR not to be an initializer
	 * @param isInit True is to be an initializer,
	 * otherwise false
	 */
	public void setInit(boolean isInit) {
		this.isInit = isInit;
	}
	
	/**
	 * Get number of initializer of this node
	 * @return
	 */
	public int getNumInitAgent() {
		return numInitAgent;
	}
	
	/**
	 * Set number of initializer to this node
	 * @param numInit number of init agent to
	 * be started at this node
	 *            
	 */
	public void setNumInitAgent(int numInit) {
		this.numInitAgent = numInit;
	}

	public void setGraphId(String id) {
		this.graphId = id;
	}

	public int getNumToken() {
		return numToken;
	}

	public void clearTokens() {
		this.numToken = 0;
	}
	
	public void decrementToken(int numDecrease) {
		if(this.numToken < numDecrease){
			throw new IllegalArgumentException("@Node.decrementToken()"
					+ " Number of decrease token " + numDecrease 
					+ " is more than available token "
					+ this.numToken);
		}
		this.numToken = numDecrease;
	}

	public void incrementToken(int numIncrease) {
		if(numIncrease < 1){
			throw new IllegalArgumentException("@Node.incrementToken()"
					+ " Number of increase token " + numIncrease 
					+ " must be more than 0");
		}
		this.numToken += numIncrease;
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
	 * Add a new state into the last element of the list
	 * 
	 * @param state
	 *            The state to set.
	 */
	public void setCurState(int state) {
		if(this.curState == state){
			return;
		}else{
			this.curState = state;
		}
				
		this.firePropertyChange(IConstants.PROPERTY_CHANGE_NODE_STATE, null,
				new Integer(this.curState));
	
		// it is not a reset action
		if(this.curState != -1){
			this.stat.addState(this.getStateName(state));
			if(log != null){
				this.log.logNode(logTag.NODE_STATE, this.nodeId, this.curState+"");	
			}
		}
	}

	/**
	 * Initialize a start state of this node
	 * 
	 * @param state
	 */
	public void initStartState(int state) {
		this.setCurState(state);		
	}

	public void resetState() {		
		this.setCurState(-1);
	}

	public List<String> getStateList() {
		return this.stat.getPastStates();
	}

	public void resetStateList() {
		if(this.stat != null){
			this.stat.clearState();
		}
	}

	/**
	 * @return Returns the edges.
	 */
	public Map<String, Edge> getEdges() {
		return edges;
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
	 * Check whether this node is alive
	 * @return 
	 */
	public boolean isAlive() {
		return isAlive;
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
	
	public void clearHoldEvents(){
		if(this.holdEvents != null){
			this.holdEvents.clear();
		}else{
			this.holdEvents = new ArrayList<Event>();
		}
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
	 * Set this node to be alive (not a failure node)
	 * @param alive
	 *            
	 */
	public void setAlive(boolean alive) {
		this.isAlive = alive;
		if(alive = false){
			if(log != null){
				this.log.logNode(logTag.NODE_DIE, this.nodeId, null);
			}
			
			// every agent resides in the node must die as well
			for(int i = 0; i < this.curAgents.size(); i++){
				Agent agent = this.curAgents.get(i);
				agent.setAlive(false);
				this.removeAgent(agent);
			}
		}
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

	/**
	 * remove a given entity from this node
	 * @param entity
	 */
	public void removeEntity(){
		this.entity = null;
	}

	public Map<String, Boolean> getBlockPort() {
		return this.portStates;
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
    	 this.numToken = 0;
    	 this.stat = new NodeStat(this.nodeId);
    	 this.blockedPorts = new HashMap<String, List<Event>>();
    	 this.portStates = new HashMap<String, Boolean>();
    	 this.holdEvents = new ArrayList<Event>();
    	 this.stateNames = new HashMap<Integer, String>();	 
    	 this.curAgents = new ArrayList<Agent>();
    	 this.whiteboard = new ArrayList<String>();
    	 this.registees = new HashMap<String, List<NotifyType>>();
    	 
    	 // re-initialize all ports label to blocked port map
    	 Iterator<String> it = this.ports.keySet().iterator();
    	 for(;it.hasNext();){
    		 String port = it.next();
    		 this.portStates.put(port, false);
    	 }
    }

    public void cleanUp(){
    	this.clearAllAgents();
    	this.clearAllBlockedPorts();
    	this.clearHoldEvents();
    	this.clearTokens();
    	this.clearWhieboard();
    	this.clearRegistees();
    	this.resetStateList();
    	this.stat.reset();
    }

}
