/*******************************************************************************
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     DisJ Development Group
 *******************************************************************************/

package distributed.plugin.ui.models;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import distributed.plugin.core.DisJException;
import distributed.plugin.core.IConstants;
import distributed.plugin.core.Node;
import distributed.plugin.ui.GraphEditorPluginException;
import distributed.plugin.ui.IGraphEditorConstants;
import distributed.plugin.ui.validators.EmptyCellEditorValidator;

/**
 *
 */
public class NodeElement extends AdapterElement {

	static final long serialVersionUID = IConstants.SERIALIZE_VERSION;

	// modifiable properties
	private static final String PROPERTY_USER = "N0 User Input";
	private static final String PROPERTY_NAME = "N1 Node Name";
	private static final String PROPERTY_IS_INIT = "N2 Initiator";
	private static final String PROPERTY_IS_ALIVE = "N3 Alive";
	private static final String PROPERTY_BREAKPOINT = "N4 Breakpoint";

	// unmodifiable properties
	private static final String PROPERTY_MSG_RECV = "N5 Number of Received Message";
	private static final String PROPERTY_MSG_SENT = "N6 Number of Sent Message";
	
	// unmodifiable properties with multiple values
	private static final String PROPERTY_OUT_PORTS = "N7 Outgoing Ports";
	private static final String PROPERTY_IN_PORTS = "N8 Incoming Ports";
	private static final String PROPERTY_STATES = "N9 State List";
	
	private static final String[] propertyArray = {PROPERTY_USER, PROPERTY_NAME,
			PROPERTY_IS_INIT, PROPERTY_IS_ALIVE, PROPERTY_BREAKPOINT,
			PROPERTY_MSG_RECV, PROPERTY_MSG_SENT, PROPERTY_OUT_PORTS,
			PROPERTY_IN_PORTS,PROPERTY_STATES};
	
	private static final int NUM_PROPERTIES = propertyArray.length;

	protected static IPropertyDescriptor[] descriptors;	
	static {
		descriptors = new IPropertyDescriptor[NUM_PROPERTIES];

		descriptors[0] =  new TextPropertyDescriptor(PROPERTY_USER,
				PROPERTY_USER);
		descriptors[1] = new TextPropertyDescriptor(PROPERTY_NAME,
				PROPERTY_NAME);
		((PropertyDescriptor) descriptors[0])
				.setValidator(EmptyCellEditorValidator.instance());
		descriptors[2] = new ComboBoxPropertyDescriptor(PROPERTY_IS_INIT,
				PROPERTY_IS_INIT, new String[] { "False", "True" });
		descriptors[3] = new ComboBoxPropertyDescriptor(PROPERTY_IS_ALIVE,
				PROPERTY_IS_ALIVE, new String[] { "False", "True" });
		descriptors[4] = new ComboBoxPropertyDescriptor(PROPERTY_BREAKPOINT,
				PROPERTY_BREAKPOINT, new String[] { "Disable", "Enable" });
		descriptors[5] = new PropertyDescriptor(PROPERTY_MSG_RECV,
				PROPERTY_MSG_RECV);
		descriptors[6] = new PropertyDescriptor(PROPERTY_MSG_SENT,
				PROPERTY_MSG_SENT);
		descriptors[7] = new PropertyDescriptor(PROPERTY_OUT_PORTS,
				PROPERTY_OUT_PORTS);
		descriptors[8] = new PropertyDescriptor(PROPERTY_IN_PORTS,
				PROPERTY_IN_PORTS);
		descriptors[9] = new PropertyDescriptor(PROPERTY_STATES,
				PROPERTY_STATES);
		
	}

	private List<LinkElement> targets;

	private List<LinkElement> sources;

	private Point location;
	
	private int maxX;
	
	private int maxY;

	private String nodeId;
	
	private Dimension size;

	transient private Node node;

	//private Node orgNode;

	//private Map<Integer, Object> hmProperties = new HashMap<Integer, Object>();

	/**
	 * Constructor
	 */
	protected NodeElement(String graphId, String nodeId) {
		this(new Node(graphId, nodeId));
		this.nodeId = nodeId;
	}

	/**
	 * Constructor
	 */
	private NodeElement(Node node) {
		super();
		this.nodeId = node.getNodeId();
		this.node = node;
		this.node.setName(this.nodeId); // by defaults
		this.targets = new ArrayList<LinkElement>();
		this.sources = new ArrayList<LinkElement>();
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		super.addPropertyChangeListener(listener);
		this.node.addPropertyChangeListener(listener);
	}

	/**
	 * Get an actual model
	 * 
	 * @return a node model
	 */
	public Node getNode() {
		return this.node;
	}

	public void setNode(Node node){
		if(node != null){
			this.node = node;
		}
	}

	/**
	 * Copy the origin state of this node
	 */
	public void copyNode() {
		// try {
		// this.orgNode = (Node)GraphLoader.deepClone(this.node);
		// } catch (DisJException ignore) {
		// } catch (IOException e) {
		// }
		/*for (int i = 0; i < NUM_PROPERTIES; i++){
			hmProperties.put(i, this.getPropertyValue(propertyArray[i]));
		}*/
	}

	/**
	 * Set all states of this node back to where is was, when method
	 * <code>copyNode</code> is called last.
	 */
	public void resetNode() {				
		this.node.resetState();
		this.node.removeEntity();
		
		for (int i = 0; i <  NUM_PROPERTIES; i++) {
			this.resetPropertyValue(propertyArray[i]);
		}
		
		// this.node.setCurState(this.orgNode.getCurState());
		// this.node = node;
		// firePropertyChange(IConstants.PROPERTY_CHANGE_NODE_STATE, null,
		// new Integer(this.node.getState()));
		// for(int i =0; i < descriptors.length; i++){
		// this.resetPropertyValue(descriptors[i].getId());
		// }

	}

	/**
	 * Get an ID belong to the corresponding node
	 * 
	 * @return
	 */
	public String getNodeId() {
		return this.nodeId;
	}
	
	public String getName(){
		return this.node.getName();
	}

	public int getNumMsgRecieved() {
		return this.node.getNumMsgRecv();
	}

	public int getNumMsgSent() {
		return this.node.getNumMsgSend();
	}

	public Point getLocation() {
		return this.location;
	}

	public void setLocation(Point location) {
		this.location = location;
		this.node.setX(this.location.x);
		this.node.setY(this.location.y);
		firePropertyChange(IConstants.PROPERTY_CHANGE_LOCATION, null, location);
	}	

	public int getMaxX() {
		return maxX;
	}

	public void setMaxX(int maxX) {
		this.maxX = maxX;
		this.node.setMaxX(maxX);
	}

	public int getMaxY() {
		return maxY;
	}

	public void setMaxY(int maxY) {
		this.maxY = maxY;
		this.node.setMaxY(maxY);
	}

	public Dimension getSize() {
		return size;
	}

	public void setSize(Dimension size) {
		this.size = size;
		// firePropertyChange(AdapterElement.PROPERTY_CHANGE_SIZE, null, size);
	}

	/**
	 * Returns useful property descriptors for the use in property sheets. this
	 * supports location and size.
	 * 
	 * @return Array of property descriptors.
	 */
	public IPropertyDescriptor[] getPropertyDescriptors() {
		return descriptors;
	}

	/**
	 * Returns an Object which represents the appropriate value for the property
	 * name supplied.
	 * 
	 * @param propName
	 *            Name of the property for which the the values are needed.
	 * @return Object which is the value of the property.
	 */
	public Object getPropertyValue(Object propName) {
		if (propName.equals(PROPERTY_NAME)) {
			return this.node.getName();

		} else if (propName.equals(PROPERTY_USER)) {
			return this.node.getUserInput();

		} else if (propName.equals(PROPERTY_IS_INIT)) {
			int i = 0;
			if (this.node.hasInitializer())
				i = this.node.getNumInit();
			return new Integer(i);

		} else if (propName.equals(PROPERTY_IS_ALIVE)) {
			int i = 0;
			if (this.node.isAlive())
				i = 1;
			return new Integer(i);

		} else if (propName.equals(PROPERTY_BREAKPOINT)) {
			int i = 0;
			if (this.node.getBreakpoint())
				i = 1;
			return new Integer(i);

		} else if (propName.equals(PROPERTY_MSG_RECV)) {

			return "" + this.node.getNumMsgRecv();

		} else if (propName.equals(PROPERTY_MSG_SENT)) {
			return "" + this.node.getNumMsgSend();

		} else if (propName.equals(PROPERTY_OUT_PORTS)) {
			return this.node.getOutgoingPorts();

		} else if (propName.equals(PROPERTY_IN_PORTS)) {
			return this.node.getIncomingPorts();

		} else if (propName.equals(PROPERTY_STATES)) {
			return this.node.getStateList();

		} else {
			return "unknown property id";
		}
	}

	/**
	 * Sets the value of a given property with the value supplied. Also fires a
	 * property change if necessary.
	 * 
	 * @param id
	 *            Name of the parameter to be changed.
	 * @param value
	 *            Value to be set to the given parameter.
	 */
	public void setPropertyValue(Object id, Object value) {
		if (id.equals(PROPERTY_NAME)) {
			this.node.setName((String) value);
			this.firePropertyChange(IConstants.PROPERTY_CHANGE_NAME, "", value);

		} else if (id.equals(PROPERTY_IS_INIT)) {
			if (value instanceof Integer) {
				this.node.setNumInit(((Integer) value).intValue());
			}

		} else if (id.equals(PROPERTY_IS_ALIVE)) {
			if (value instanceof Integer) {
				boolean bool = false;
				if (((Integer) value).intValue() == 1)
					bool = true;
				this.node.setAlive(bool);
			}

		} else if (id.equals(PROPERTY_BREAKPOINT)) {
			if (value instanceof Integer) {
				boolean bool = false;
				if (((Integer) value).intValue() == 1)
					bool = true;
				this.node.setBreakpoint(bool);
			}

		} else if (id.equals(PROPERTY_USER)) {
			if(value == null){		
				value = "user input";
			}
			this.node.setUserInput(value.toString());
		}else {
			return;
		}
	}

	public void resetPropertyValue(Object propName) {

//		if (propName.equals("PROPERTY_NAME") ){
//			this.node.setName(this.orgNode.getName());
//			return;
//
//		} else if (propName.equals("PROPERTY_IS_INIT")) {
//			this.node.setInit(this.orgNode.isInitializer());
//			return;
//
//		} else if (propName.equals("PROPERTY_IS_STARTER")) {
//			this.node.setStarter(this.orgNode.isStarter());
//			return;
//
//		} else if (propName.equals("PROPERTY_BREAKPOINT")) {
//			this.node.setBreakpoint(this.orgNode.getBreakpoint());
//			return;

//		} else 
			if (propName.equals(PROPERTY_MSG_RECV)) {
			this.node.setNumMsgRecv(0);
			return;

		} else if (propName.equals(PROPERTY_MSG_SENT)) {
			this.node.setNumMsgSend(0);
			return;

		} else if (propName.equals(PROPERTY_STATES)) {
			this.node.resetStateList();
			return;
		}

	}

	public void connectInLink(LinkElement link) {
		try {
			short type;
			if (link.getType().equals(IGraphEditorConstants.UNI))
				type = IConstants.DIRECTION_IN;
			else
				type = IConstants.DIRECTION_BI;

			// add port label of this edge with default value (edgeID)
			this.node.addEdge(link.getEdge().getEdgeId(), type, link.getEdge());
			this.targets.add(link);
			this.fireStructureChange(IConstants.PROPERTY_CHANGE_INPUT, link);
		} catch (DisJException ignore) {
			System.err.println("[NodeElement].connectInLink() " + ignore);
		}
	}

	public void connectOutLink(LinkElement link) {
		try {
			short type;
			if (link.getType().equals(IGraphEditorConstants.UNI))
				type = IConstants.DIRECTION_OUT;
			else
				type = IConstants.DIRECTION_BI;

			// add port label of this edge with default value (edgeID)
			this.node.addEdge(link.getEdge().getEdgeId(), type, link.getEdge());
			this.sources.add(link);
			this.fireStructureChange(IConstants.PROPERTY_CHANGE_OUTPUT, link);
		} catch (DisJException ignore) {
			System.err.println("@NodeElement.connectOutLink() " + ignore);
		}
	}

	/**
	 * Disconnect the link that connected to node element
	 * 
	 * @param link
	 */
	public void disconnectInLink(LinkElement link) {
		try {
			// System.out.println(this.node.getNodeId() +
			// "@NodeElement.disconnectInLink() " + link);
			this.node.removeEdge(link.getEdge());
			this.targets.remove(link);
			this.fireStructureChange(IConstants.PROPERTY_CHANGE_INPUT, link);
		} catch (DisJException ignore) {
			System.err.println("[NodeElement].disconnectInLink() "
					+ this.node.getNodeId() + " : " + ignore);
		}
	}

	/**
	 * Disconnect the link that connected to node element
	 * 
	 * @param link
	 * @throws GraphEditorPluginException
	 */
	public void disconnectOutLink(LinkElement link) {
		try {
			// System.out.println(this.node.getNodeId() +
			// "@NodeElement.disconnectOutLink() " + link);
			this.node.removeEdge(link.getEdge());
			this.sources.remove(link);
			this.fireStructureChange(IConstants.PROPERTY_CHANGE_OUTPUT, link);
		} catch (DisJException ignore) {
			System.err.println("@NodeElement.disconnectOutLink() "
					+ this.node.getNodeId() + " : " + ignore);
		}
	}

	public List<LinkElement> getSourceConnections() {
		return this.sources;
	}

	public List<LinkElement> getTargetConnections() {
		return this.targets;
	}

	public String toString() {
		return "<NodeElement>Name: " + this.node.getName() + " at: "
				+ getLocation();
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
