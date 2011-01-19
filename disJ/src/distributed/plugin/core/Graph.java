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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import distributed.plugin.random.IRandom;
import distributed.plugin.stat.GraphStat;
import distributed.plugin.ui.parts.GraphPart;

/**
 * @author Me
 *         A graph instance
 */
public class Graph implements Serializable {

	static final long serialVersionUID = IConstants.SERIALIZE_VERSION;

	private int globalFlowType; // FIFO
	
	private int globalDelayType; // a type of delay i.e random uniform,
	
	private int globalDelaySeed; // it's useful for synch delay and random
	
	private int currentNodeId;

	private int currentEdgeId;

	private String graphId;

	private String protocol;
	
	private int modelId; // a type of protocol
	
	private int maxToken; // a max number of token for token model
	
	private Map<String, Node> nodes;

	private Map<String, Edge> edges;
	
	transient private Map<String, Agent> agents;
	
	transient private IRandom clientRandom;
	
	transient private GraphStat stat;
	
	transient private Map<Integer, String> stateFields;
	
	protected PropertyChangeSupport listeners;
	
	public Graph() {
		this("");
	}

	private Graph(String id) {
		this.graphId = id;
		this.globalFlowType = IConstants.MSGFLOW_FIFO_TYPE;
		this.globalDelayType = IConstants.MSGDELAY_GLOBAL_SYNCHRONOUS;
		this.globalDelaySeed = IConstants.DEFAULT_MSGDELAY_SEED;
		this.currentNodeId = 0;
		this.currentEdgeId = 0;
		this.protocol = "";
		this.modelId = -1;
		this.maxToken = IConstants.DEFAULT_MAX_NUM_TOKEN;
        this.stateFields = null;
		this.stat = new GraphStat(this.graphId);
		
		this.listeners = new PropertyChangeSupport(this);
		
		this.nodes = new HashMap<String, Node>();
		this.edges = new HashMap<String, Edge>();
		this.agents = new HashMap<String, Agent>();
	}

	public void addPropertyChangeListener(PropertyChangeListener l) {
		listeners.addPropertyChangeListener(l);
		
		
		if(!(l instanceof GraphPart)){
			// add view listener to each node
			Node n = null;
			Iterator<String> its = nodes.keySet().iterator();
			for(String id = null; its.hasNext();){
				id = its.next();
				n = nodes.get(id);
				n.addPropertyChangeListener(l);
			}
			
			// add view listener to each edge
			Edge e = null;
			its = edges.keySet().iterator();
			for(String id = null; its.hasNext();){
				id = its.next();
				e = edges.get(id);
				e.addPropertyChangeListener(l);
			}
		}
	}

	public void firePropertyChange(String prop, Object old, Object newValue) {
		listeners.firePropertyChange(prop, old, newValue);
	}	
	
	public GraphStat getStat() {
		return stat;
	}

	public Map<Integer, String> getStateFields(){
		return this.stateFields;
	}
	
    public void setStateFields(Map<Integer, String> stateFields){
    	this.stateFields = stateFields;
    }
   
    /**
     * Send signal to UI observer to display final report of the graph
     */
    public void signalFinalReportDisplay(int procType){
    	
    	if(procType == IConstants.MODEL_MESSAGE_PASSING){
    		this.firePropertyChange(IConstants.PROPERTY_FINAL_MSG_PASSING_REPORT, null,
    				null);
    		
    	}else if (procType == IConstants.MODEL_AGENT_WHITEBOARD){
    		this.firePropertyChange(IConstants.PROPERTY_FINAL_AGENT_BOARD_REPORT, null,
    				null);
    		
    	}else  if (procType == IConstants.MODEL_AGENT_TOKEN){
    		this.firePropertyChange(IConstants.PROPERTY_FINAL_AGENT_TOKEN_REPORT, null,
				null);
    	}
    }
    
	/**
	 * Add a link that used in this graph
	 * 
	 * @param id
	 *            a unique edge id
	 * @param edge
	 * @throws DisJException
	 */
	public void addEdge(String id, Edge edge) throws DisJException {
		if (!edges.containsKey(id)){
			edges.put(id, edge);
			PropertyChangeListener[] lis = this.listeners.getPropertyChangeListeners();
			for(int i =0; i < lis.length; i++){				
				edge.addPropertyChangeListener(lis[i]);
			}
		}else
			throw new DisJException(IConstants.ERROR_2, id);
	}

	/**
	 * Add a node that used in this graph
	 * 
	 * @param id
	 *            a unique node id
	 * @param node
	 * @throws DisJException
	 */
	public void addNode(String id, Node node) throws DisJException {
		if (!nodes.containsKey(id)){
			nodes.put(id, node);
			PropertyChangeListener[] lis = this.listeners.getPropertyChangeListeners();
			for(int i =0; i < lis.length; i++){				
				node.addPropertyChangeListener(lis[i]);
			}
		} else
			throw new DisJException(IConstants.ERROR_3, id);
	}

	public void addAgent(String id, Agent agent) {
		if (!this.agents.containsKey(id)){
			
			// add UI listener
			PropertyChangeListener[] lis = this.listeners.getPropertyChangeListeners();
			for(int i =0; i < lis.length; i++){
				agent.addPropertyChangeListener(lis[i]);
			}
			
			// keep track of it
			this.agents.put(id, agent);
			
			// notify UI
			this.firePropertyChange(IConstants.PROPERTY_CHANGE_ADD_AGENT, null,
					agent);		
		}
	}
	
	/**
	 * Remove a node with a given id
	 * 
	 * @param id
	 * @return True if node exists and has been removed,
	 * otherwise false
	 */
	public boolean removeNode(String id) {
		if (nodes.containsKey(id)){		
			Node n = nodes.remove(id);
			return (n != null);
		}
		return false;
	}

	/**
	 * Remove an edge with a given id
	 * 
	 * @param id
	 * @return True if edge exists and has been removed,
	 * otherwise false
	 */
	public boolean removeEdge(String id) {
		if (!this.edges.containsKey(id)){
			Edge e = edges.remove(id);
			return (e != null);
		}
		return false;
	}

	public boolean removeAgent(String id)  {
		if (this.agents.containsKey(id)){
			Agent a = this.agents.remove(id);
			
			// remove listeners
			PropertyChangeListener[] lis = this.listeners.getPropertyChangeListeners();
			for(int i =0; i < lis.length; i++){
				a.removePropertyChangeListener(lis[i]);
			}
			
			// notify UI
			this.firePropertyChange(IConstants.PROPERTY_CHANGE_REM_AGENT, null,
					a);
			return (a != null);
		}
		return false;
	}
	
	public void removeAllAgents(){
		
		// remove listeners
		PropertyChangeListener[] lis = this.listeners.getPropertyChangeListeners();
		Iterator<String> its = this.agents.keySet().iterator();
		Agent a = null;
		for(String id = null; its.hasNext(); ){
			id = its.next();
			a = this.agents.remove(id);		
			for(int i =0; i < lis.length; i++){				
				a.removePropertyChangeListener(lis[i]);
			}			
			this.firePropertyChange(IConstants.PROPERTY_CHANGE_REM_AGENT, null,
					a);
		}
		
		// double safe lol
		this.agents.clear();

	}
	
	/**
	 * Get an edge with a given id
	 * 
	 * @param id
	 * @return Edge if exists otherwise null
	 */
	public Edge getEdge(String id) {
		try {
			if (!edges.containsKey(id)){
				throw new DisJException(IConstants.ERROR_1, id);
			}else{
				return (Edge) edges.get(id);
			}
		} catch (DisJException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Get a node with a given id
	 * 
	 * @param id
	 * @return Node if exists otherwise null
	 */
	public Node getNode(String id) {
		try {
			if (!nodes.containsKey(id)){
				throw new DisJException(IConstants.ERROR_0, id);
			} else{
				return (Node) nodes.get(id);
			}
		} catch (DisJException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Get an agent with a given id
	 * 
	 * @param id An agent ID
	 * @return Agent if exists otherwise null
	 */
	public Agent getAgent(String id) {
		try {
			if (!this.agents.containsKey(id))
				throw new DisJException(IConstants.ERROR_0, id);
			else
				return this.agents.get(id);
		} catch (DisJException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * @return Returns all the edges in this graph
	 */
	public Map<String, Edge> getEdges() {
		return edges;
	}

	/**
	 * @return Returns the id of this graph
	 */
	public String getId() {
		return this.graphId;
	}

	public int getModelId() {
		return modelId;
	}

	public void setModelId(int modelId) {
		this.modelId = modelId;
	}

	public int getMaxToken() {
		return maxToken;
	}

	public void setMaxToken(int maxToken) {
		this.maxToken = maxToken;
	}

	public int getGlobalDelayType() {
		return globalDelayType;
	}

	public int getGlobalDelaySeed() {
		return globalDelaySeed;
	}

	public void setGlobalDelaySeed(int delaySeed) {
		this.globalDelaySeed = delaySeed;
	}

	public void setGlobalDelayType(int delayType) {
		this.globalDelayType = delayType;
	}
	
	public int getGlobalFlowType() {
		return globalFlowType;
	}

	public void setGlobalFlowType(int globalFlowType) {
		this.globalFlowType = globalFlowType;
	}

	/**
	 * Allow to set the ID only once
	 * 
	 * @param id
	 */
	public void setId(String id) {
		if (this.graphId == null || this.graphId.trim().equals(""))
			this.graphId = id;
	}

	/**
	 * @return Returns all the nodes in this graph
	 */
	public Map<String, Node> getNodes() {
		return nodes;
	}

	public Map<String, Agent> getAgents(){
		return this.agents;
	}
	
	public int getCurrentEdgeId() {
		return currentEdgeId++;
	}

	public int getCurrentNodeId() {
		return currentNodeId++;
	}

	public IRandom getClientRandom() {
		return clientRandom;
	}

	public void setClientRandom(IRandom clientRandom) {
		this.clientRandom = clientRandom;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	/**
	 * Get a statistic of current state vs number of node
	 * 
	 * @return
	 */
	public Map<Integer, Integer> getNodeStateCount(){
		int s, c;
		Map<Integer, Integer> count = new HashMap<Integer, Integer>();
		for (String id : this.nodes.keySet()) {
			Node n = this.nodes.get(id);
			s = n.getCurState();
			if(count.containsKey(s)){
				c = count.get(s);
				c++;
			}else{
				c = 1;
			}
			count.put(s, c);
		}
		return count;
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
    	 this.agents = new HashMap<String, Agent>();
    	 this.stat = new GraphStat(this.graphId);
    	 this.stateFields = null;
     	 this.clientRandom = null;
    }

}
