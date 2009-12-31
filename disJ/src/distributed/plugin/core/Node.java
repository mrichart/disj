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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import distributed.plugin.runtime.Event;
import distributed.plugin.runtime.engine.Entity;

/**
 * @author Me
 * 
 * A Node Class that stores an information about an entity
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

    private boolean isInitializer;

    /**
     * specify as a starter for init node
     */
    private boolean isStarter;

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
     * keeping track of user internal data of this entity and it cannot
     * 
     */
    transient private Entity entity; 

    /**
     * Edges that connected to node{port label, edge}
     */
    private Map edges;

    /**
     * mapping between port's name and port's type {portlabel, type (Short)}
     * i.e {portA, BI_DIRECTION} {portB, OUT_DIRECTION} {portC, IN_DIRECTION}
     */
    private Map ports;

    /**
     * a list of msg queue for blocking message {localport label, list of events}
     */
    private Map blockMsg; 

    /**
     * a lis of blocking state of each port{port label,boolean}
     */
    private Map blockPort; 

    /**
     * hold the events if initializer node receive message
     * before it has been initialized to notify the change of state
     * a list of state-name pair of the corresponding entity
     */
    private List holdEvents;
    
    /**
     * X, Y coordinate in graph editor
     */
    private int x;
    
    private int y;
    
    transient private Map stateNames;

    protected PropertyChangeSupport listeners = new PropertyChangeSupport(this);

    public Node(String nodeId) {
        this("unknown", nodeId, "", false, true);
    }

    public Node(String graphId, String nodeId) {
        this(graphId, nodeId, "", false, true);
    }

    public Node (String graphId, String nodeId, int x, int y){
    	this(graphId, nodeId, "", false, true, x , y);
    }
    
    public Node(String graphId, String nodeId, String name, boolean isInit,
            boolean isStarter) {
        this(graphId, nodeId, name, isInit, isStarter, 0, 0);

    }

    public Node(String graphId, String nodeId, String name, boolean isInit,
            boolean isStarter, int x, int y) {
    	this.x = x;
    	this.y = y;
        this.nodeId = nodeId;
        this.graphId = graphId;
        this.name = name;
        this.breakpoint = false;
        this.isInitializer = isInit;
        this.initExec = false;
        this.isStarter = isStarter;
        this.numMsgRecv = 0;
        this.numMsgSend = 0;
        this.latestRecvPort = null;
        this.entity = null;
        this.log = new NodeLog(graphId);
        this.edges = new HashMap();
        this.ports = new HashMap(4);
        this.blockMsg = new HashMap(4);
        this.blockPort = new HashMap(4);
        this.holdEvents = new ArrayList(0);
        this.stateNames = new HashMap();

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
    public void setStateNames(Map states) {
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
        Object key = new Integer(state);
        if (this.stateNames.containsKey(key)) {
            return (String) this.stateNames.get(key);
        } else {
            return IConstants.STATE_NOT_FOUND;
        }
    }

    /**
     * Add more link out of this node
     * 
     * @param label
     *            A local lable of the edge for this node
     * @param type
     *            A type of port
     * @param edge
     * @throws DisJException
     */
    public void addEdge(String label, short type, Edge edge) throws DisJException {
           
    	if( label == null){
    		throw new DisJException(IConstants.ERROR_22, "@Node.addEdge() link type: " + type + edge);
    	}
        if (!this.edges.containsKey(label) && !this.edges.containsValue(edge)) {
            this.edges.put(label, edge);
            this.ports.put(label, new Short(type));
            this.blockPort.put(label, new Boolean(false));
            
        } else{
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

    /**
     * Get a port label of this node that connected to a given edge
     * 
     * @param edge
     * @return
     * @throws DisJException
     */
    public String getPortLabel(Edge edge) throws DisJException {
        if (this.edges.containsValue(edge)) {
            Iterator it = this.edges.keySet().iterator();
            while (it.hasNext()) {
                Object label = it.next();
                if (this.edges.get(label).equals(edge))
                    return (String) label;
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
        if (this.edges.containsKey(port))
            return;

        // set the edges collections
        String old = this.getPortLabel(edge);
        this.edges.remove(old);
        this.edges.put(port, edge);

        // set the port type tracking collection
        Object portType = this.ports.get(old);
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
            throw new DisJException(IConstants.ERROR_1, "@Node.setPortBlock() " + label);

        if (state == true) {
        	// create a block message holding place
            if (!this.blockMsg.containsKey(label))
                this.blockMsg.put(label, new ArrayList());
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

        List events = (List) this.blockMsg.get(portLabel);
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
    public List getBlockedEvents(String portLabel) {
        return (List) this.blockMsg.get(portLabel);
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
     * Assign a user algorithm representative (Entity object) into this node
     * 
     * @param entity
     * @throws DisJException
     */
    public void assignEntity(Entity entity) throws DisJException {

        if (this.entity != null)
            throw new DisJException(IConstants.ERROR_6);

        this.entity = entity;
    }

    /**
     * Increment number of message receive and log the receiver TODO
     */
    public void incMsgRecv() {
        this.numMsgRecv++;
    }

    /**
     * Increment number of message is sent and log the msg sent TODO
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
                + "\nInit: " + this.isInitializer + "\nStarter: "
                + this.isStarter + "\nNum Msg Received: " + this.numMsgRecv
                + "\nNum Msg Sent: " + this.numMsgSend);
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
    public Entity getEntity() {
        return entity;
    }

    /**
     * @return Returns the isInit.
     */
    public boolean isInitializer() {
        return isInitializer;
    }

    /**
     * @param isInit
     *            The isInit to set.
     */
    public void setInit(boolean isInit) {
        this.isInitializer = isInit;
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
        String newState = this.getStateName(this.curState);
        this.firePropertyChange(IConstants.PROPERTY_CHANGE_NODE_STATE, null,
                new Integer(this.curState));
        this.log.logState(newState);
    }

    public void resetState(int state) {
        this.curState = state;
    }

    /**
     * @return Returns the edges.
     */
    public Map getEdges() {
        return edges;
    }

    public List getStateList() {
        return this.log.getStates();
    }

    public void setStateList(List list) {
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
     * @return Returns the isStarter.
     */
    public boolean isStarter() {
        return isStarter;
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
    public List getHoldEvents() {
        return holdEvents;
    }

    /**
     * @return Returns the ports.
     */
    public Map getPorts() {
        return this.ports;
    }

    /**
     * Get all possible outgoing ports
     * @return
     */
    public List getOutgoingPorts() {
        List out = new ArrayList();
        Iterator its = this.ports.keySet().iterator();
        while (its.hasNext()) {
            Object port = its.next();
            Short type = ((Short) this.ports.get(port));
            if (type.shortValue() == IConstants.OUT_DIRECTION
                    || type.shortValue() == IConstants.BI_DIRECTION) {
                out.add(port);
            }
        }
        return out;
    }

    /**
     * Get all possible incoming ports
     * @return
     */
    public List getIncomingPorts() {
        List in = new ArrayList();
        Iterator its = ports.keySet().iterator();
        while (its.hasNext()) {
            Object port = its.next();
            Short type = ((Short) ports.get(port));
            if (type.shortValue() == IConstants.IN_DIRECTION
                    || type.shortValue() == IConstants.BI_DIRECTION) {
                in.add(port);
            }
        }
        return in;
    }

    /**
     * @param isInitializer
     *            The isInitializer to set.
     */
    public void setInitializer(boolean isInitializer) {
        this.isInitializer = isInitializer;
    }

    /**
     * @param isStarter
     *            The isStarter to set.
     */
    public void setStarter(boolean isStarter) {
        this.isStarter = isStarter;
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

    public void setEntity(Entity entity) {
        this.entity = entity;
    }
    
    public Map getBlockPort(){
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
    
    
    
}

