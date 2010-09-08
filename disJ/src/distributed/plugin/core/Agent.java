package distributed.plugin.core;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import distributed.plugin.runtime.IDistributedModel;

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
	 * Max number of memory slot that agent can carry
	 * TODO currently is very large but finite
	 */
	private int maxSlot = 128;
	
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
	
	transient private String[] info;

	/*
	 * A node that agent currently resided 
	 */
	transient private Node curNode;
	
	/*
	 * A client entity that own this agent
	 */
	transient private IDistributedModel entity;
	
	protected PropertyChangeSupport listeners = new PropertyChangeSupport(this);
	
	public void addPropertyChangeListener(PropertyChangeListener l) {
		// System.out.println("[Node] addPropertyChangeListener: " + l);
		listeners.addPropertyChangeListener(l);
	}

	public void firePropertyChange(String prop, Object old, Object newValue) {
		// System.out.println("[Node] firePropertyChange: " + prop);
		listeners.firePropertyChange(prop, old, newValue);
	}

	
	public Agent(String agentId, String hostId){
		this.starter = true;
		this.hasInitExec = false;
		this.alive = true;
		this.agentId = agentId;
		this.homeId = hostId;
	}
	
	public boolean isAlive() {
		return alive;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}

	public boolean isStarter() {
		return starter;
	}

	public void setStarter(boolean starter) {
		this.starter = starter;
	}

	public boolean isHasInitExec() {
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
	public void setClientEntity(IDistributedModel entity){
		this.entity = entity;
	}
	
	public IDistributedModel getClientEntity(){
		return this.entity;
	}
	
	public int getMaxSlot(){
		return this.maxSlot;
	}

	public String[] getInfo() {
		return info;
	}

	public void setInfo(String[] info) {
		this.info = info;
	}
	
	/**
	 * Get a copy of current snapshot of whiteboard of this node
	 * 
	 * @return A copy list of data written on the whiteboard
	 */
	public List<String> readFromBoard(){
		List<String> board = this.curNode.getWhiteboard();
		List<String> temp = new ArrayList<String>();
		for (Iterator<String> iterator = board.iterator(); iterator.hasNext();) {
			String msg = iterator.next();
			temp.add(msg);
		}
		return temp;
	}
	
	/**
	 * Append a message into the end of whitboard of this node
	 * 
	 * @param msg
	 */
	public void appendToBoard(String msg){
		List<String> board = this.curNode.getWhiteboard();
		board.add(msg);
		this.curNode.setWhiteboard(board);
	}
	
	/**
	 * Remove a given message from whiteboard of this node if exist
	 * 
	 * @param msg A message that want to be removed
	 * @return Ture if the message found and removed, otherwise false
	 */
	public boolean removeFromBoard(String msg){
		List<String> board = this.curNode.getWhiteboard();
		return board.remove(msg);
	}
}
