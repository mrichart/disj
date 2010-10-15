package distributed.plugin.core;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import distributed.plugin.core.Logger.logTag;
import distributed.plugin.runtime.engine.AgentModel;
import distributed.plugin.stat.AgentStat;

@SuppressWarnings("serial")
public class Agent implements Serializable{

	/*
	 * Flag checking whether agent is alive
	 */
	private boolean alive;
	
	/*
	 * Flag for initializing right on when process starts
	 */
	private boolean starter;
	
	/*
	 * Flag for checking whether it has been initialized
	 */
	private boolean hasInitExec;
	
	/*
	 * Current state of this agent
	 */
	private int curState;
	
	/*
	 * Max number of memory slot that agent can carry
	 * TODO currently is very large but finite
	 */
	private int maxSlot;
	
	/*
	 * An ID of this agent
	 */
	private String agentId;
	
	/*
	 * A home host ID of this agent when it initiated
	 */
	private String homeId;	
	
	/*
	 * An external name label
	 */
	private String name;
	
	/*
	 * A port that it entered w.r.t a current node
	 */
	private String lastPortEnter;
	
	/*
	 * Utility record for replay
	 */
	private Object data;
	
	/*
	 * A node that agent currently resided 
	 */
	transient private Node curNode;
	
	/*
	 * A client entity that own this agent
	 */
	transient private AgentModel entity;

	/*
	 * Logger that will log the node activities
	 */
	transient private Logger log;

	/*
	 * A memory suitcase for agent to carry while traveling
	 * in a network
	 */
	transient private String[] info;

	/*
	 * A mapping table of every possible state name and value defined by user
	 */
	transient private Map<Integer, String> stateNames;
	
	transient private AgentStat stat;
	
	protected PropertyChangeSupport listeners = new PropertyChangeSupport(this);
	

	/**
	 * Constructor 
	 * 
	 * @param agentId
	 * @param hostId
	 */
	public Agent(String agentId, String hostId){
		this.curState = -1;
		this.starter = true;
		this.hasInitExec = false;
		this.alive = true;
		this.agentId = agentId;
		this.homeId = hostId;
		this.maxSlot = 128;
		this.lastPortEnter = null;
		this.data = null;
		this.curNode = null;
		this.log = null;
		this.info = null;
		this.entity = null;
		this.stat = new AgentStat(this.agentId);

		this.stateNames = new HashMap<Integer, String>();
	}
	
	
	public void addPropertyChangeListener(PropertyChangeListener l) {
		listeners.addPropertyChangeListener(l);
	}

	public void firePropertyChange(String prop, Object old, Object newValue) {
		listeners.firePropertyChange(prop, old, newValue);
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
	 * Get a current state, which is the last element in the list
	 * 
	 * @return Returns the state.
	 */
	public int getCurState() {
		return this.curState;
	}

	
	public AgentStat getStat() {
		return stat;
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
			// FIXME
			//this.firePropertyChange(IConstants.PROPERTY_CHANGE_NODE_STATE, null,
			//		new Integer(this.curState));
		
			// it is not a reset action
			if(this.curState != -1){
				this.stat.addState(this.getStateName(state));
				if(log != null){
					this.log.logAgent(logTag.AGENT_STATE, this.agentId, 
							this.curState+"");
				}
			}
		}
	}

	public List<String> getStateList() {
		return this.stat.getPastStates();
	}

	/**
	 * Get logger
	 * 
	 * @return logger
	 */
	public Logger getLogger(){
		return this.log;
	}
	
	/**
	 * Set logger
	 * 
	 * @param log
	 */
	public void setLogger(Logger log){
		this.log = log;
	}
	
	public boolean isAlive() {
		return alive;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
		if(alive == false){
			String value = this.getCurNode().getNodeId();
			if(log != null){
				this.log.logAgent(logTag.AGENT_DIE, this.agentId, value);
			}
		}
	}

	public boolean isStarter() {
		return starter;
	}

	public void setStarter(boolean starter) {
		this.starter = starter;
	}

	public boolean hasInitExec() {
		return hasInitExec;
	}

	public void setHasInitExec(boolean hasInitExec) {
		this.hasInitExec = hasInitExec;
	}

	public String getLastPortEnter() {
		return lastPortEnter;
	}

	public void setLastPortEnter(String lastPortEnter) {
		this.lastPortEnter = lastPortEnter;
	}

	public Node getCurNode() {
		return curNode;
	}

	public void setCurNode(Node curNode) {
		this.curNode = curNode;
	}

	public String getAgentId() {
		return agentId;
	}

	public String getHomeId() {
		return homeId;
	}

	public String getName() {
		return name;
	}

	/**
	 * Binding client implementation object
	 * 
	 * @param entity
	 */
	public void setClientEntity(AgentModel entity){
		this.entity = entity;
	}
	
	public AgentModel getClientEntity(){
		return this.entity;
	}
	
	public int getMaxSlot(){
		return this.maxSlot;
	}
	
	public void setMaxSlot(int maxSlot){
		this.maxSlot = maxSlot;
	}

	public String[] getInfo() {
		return info;
	}

	public void setInfo(String[] info) {
		this.info = info;
	}
	
	
	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	/**
	 * Get all outgoing port label of current node
	 * 
	 * @return A list of String all out going port labels
	 */
	public List<String> getOutPorts() {
		return this.curNode.getOutgoingPorts();
	}

	/**
	 * Get all incoming port labels of current node
	 * 
	 * @return A list of String of all incoming port labels
	 */
	public List<String> getInPorts() {
		return this.curNode.getIncomingPorts();
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
    }

}
