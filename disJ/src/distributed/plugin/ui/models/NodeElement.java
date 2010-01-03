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
import java.util.ArrayList;
import java.util.HashMap;
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
import distributed.plugin.runtime.GraphLoader;
import distributed.plugin.ui.GraphEditorPluginException;
import distributed.plugin.ui.IGraphEditorConstants;
import distributed.plugin.ui.validators.EmptyCellEditorValidator;

/**
 * @author Me
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class NodeElement extends AdapterElement {

	// editable propterties
	public static final String[] propertyArray = { "PROPERTY_NAME",
			"PROPERTY_IS_INIT", "PROPERTY_IS_STARTER", "PROPERTY_BREAKPOINT",
			"PROPERTY_MSG_RECV", "PROPERTY_MSG_SENT", "PROPERTY_OUT_PORTS",
			"PROPERTY_IN_PORTS","PROPERTY_STATES" };

	public static final String PROPERTY_NAME = "N1 Node Name";

	public static final String PROPERTY_IS_INIT = "N2 Initiator";

	public static final String PROPERTY_IS_STARTER = "N3 Starter";

	public static final String PROPERTY_BREAKPOINT = "N4 Breakpoint";

	// uneditable properties
	public static final String PROPERTY_MSG_RECV = "N5 Number of Message Recveived";

	public static final String PROPERTY_MSG_SENT = "N6 Number of Message Sent";

	// uneditable properties with multi values
	public static final String PROPERTY_OUT_PORTS = "N7 Outgoing Ports";

	public static final String PROPERTY_IN_PORTS = "N8 Incoming Ports";

	public static final String PROPERTY_STATES = "N9 State List";

	protected static IPropertyDescriptor[] descriptors;

	static final long serialVersionUID = IConstants.SERIALIZE_VERSION;

	// A cheated way in specify number of properties of this element
	private static final int NUM_PROPERTIES = 9;

	static {
		descriptors = new IPropertyDescriptor[NUM_PROPERTIES];

		descriptors[0] = new TextPropertyDescriptor(PROPERTY_NAME,
				PROPERTY_NAME);
		((PropertyDescriptor) descriptors[0])
				.setValidator(EmptyCellEditorValidator.instance());

		descriptors[1] = new ComboBoxPropertyDescriptor(PROPERTY_IS_INIT,
				PROPERTY_IS_INIT, new String[] { "False", "True" });
		descriptors[2] = new ComboBoxPropertyDescriptor(PROPERTY_IS_STARTER,
				PROPERTY_IS_STARTER, new String[] { "False", "True" });
		descriptors[3] = new ComboBoxPropertyDescriptor(PROPERTY_BREAKPOINT,
				PROPERTY_BREAKPOINT, new String[] { "Disable", "Enable" });

		descriptors[4] = new PropertyDescriptor(PROPERTY_MSG_RECV,
				PROPERTY_MSG_RECV);
		descriptors[5] = new PropertyDescriptor(PROPERTY_MSG_SENT,
				PROPERTY_MSG_SENT);

		descriptors[6] = new PropertyDescriptor(PROPERTY_OUT_PORTS,
				PROPERTY_OUT_PORTS);
		descriptors[7] = new PropertyDescriptor(PROPERTY_IN_PORTS,
				PROPERTY_IN_PORTS);
		descriptors[8] = new PropertyDescriptor(PROPERTY_STATES,
				PROPERTY_STATES);
	}

	private List targets;

	private List sources;

	private Point location;

	private Dimension size;

	private Node node;

	private Node orgNode;

	private HashMap hmProperties = new HashMap();

	/**
	 * Constructor
	 */
	protected NodeElement(String graphId, String nodeId) {
		this(new Node(graphId, nodeId));
	}

	/**
	 * Constructor
	 */
	private NodeElement(Node node) {
		super();
		this.node = node;
		this.orgNode = null;
		this.node.setName(node.getNodeId()); // by defaults
		this.targets = new ArrayList();
		this.sources = new ArrayList();
	}

	public void addPropertyChangeListener(PropertyChangeListener l) {
		super.addPropertyChangeListener(l);
		this.node.addPropertyChangeListener(l);
	}

	/**
	 * Get an actual model
	 * 
	 * @return a node model
	 */
	public Node getNode() {
		return this.node;
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
		for (int i = 0; i < NUM_PROPERTIES; i++)
			hmProperties.put(i, getPropertyValue(propertyArray[i]));
	}

	/**
	 * Set all states of this node back to where is was, when method
	 * <code>copyNode</code> is called last.
	 */
	public void resetNode() {
		// this.node.setCurState(this.orgNode.getCurState());
		this.node.setCurState(new Short("99"));
		for (int i = 0; i <  NUM_PROPERTIES; i++) {
			this.resetPropertyValue(propertyArray[i]);
		}
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
		return this.node.getNodeId();
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
		// System.out.println("[NodeElement] getLocation() return ="
		// + this.location);
		return this.location;
	}

	public void setLocation(Point location) {
		System.out.println("[NodeElement] setLocation()=" + location);

		this.location = location;
		this.node.setX(this.location.x);
		this.node.setY(this.location.y);
		firePropertyChange(IConstants.PROPERTY_CHANGE_LOCATION, null, location);
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

		} else if (propName.equals(PROPERTY_IS_INIT)) {
			int i = 0;
			if (this.node.isInitializer())
				i = 1;
			return new Integer(i);

		} else if (propName.equals(PROPERTY_IS_STARTER)) {
			int i = 0;
			if (this.node.isStarter())
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
				boolean bool = false;
				if (((Integer) value).intValue() == 1)
					bool = true;
				this.node.setInit(bool);
			}

		} else if (id.equals(PROPERTY_IS_STARTER)) {
			if (value instanceof Integer) {
				boolean bool = false;
				if (((Integer) value).intValue() == 1)
					bool = true;
				this.node.setStarter(bool);
			}

		} else if (id.equals(PROPERTY_BREAKPOINT)) {
			if (value instanceof Integer) {
				boolean bool = false;
				if (((Integer) value).intValue() == 1)
					bool = true;
				this.node.setBreakpoint(bool);
			}

		} else {
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
			if (propName.equals("PROPERTY_MSG_RECV")) {
			this.node.setNumMsgRecv(0);
			return;

		} else if (propName.equals("PROPERTY_MSG_SENT")) {
			this.node.setNumMsgSend(0);
			return;

		} else if (propName.equals("PROPERTY_STATES")) {
			this.node.setStateList(new ArrayList());
			return;
		}

	}

	/**
	 * TODO revert the In and Out by Out=source, In=target
	 * 
	 * @param link
	 */
	public void connectInLink(LinkElement link) {
		try {
			// System.out.println("@NodeElement.connectInLink() " + link);
			short type;
			if (link.getType().equals(IGraphEditorConstants.UNI))
				type = IConstants.OUT_DIRECTION;
			else
				type = IConstants.BI_DIRECTION;

			// add port label of this edge with default value (edgeID)
			this.node.addEdge(link.getEdge().getEdgeId(), type, link.getEdge());
			this.sources.add(link);
			fireStructureChange(IConstants.PROPERTY_CHANGE_INPUT, link);
		} catch (DisJException ignore) {
			System.err.println("[NodeElement].connectInLink() " + ignore);
		}
	}

	public void connectOutLink(LinkElement link) {
		try {
			// System.out.println("@NodeElement.connectOutLink() " + link);
			short type;
			if (link.getType().equals(IGraphEditorConstants.UNI))
				type = IConstants.IN_DIRECTION;
			else
				type = IConstants.BI_DIRECTION;

			// add port label of this edge with default value (edgeID)
			this.node.addEdge(link.getEdge().getEdgeId(), type, link.getEdge());
			this.targets.add(link);
			fireStructureChange(IConstants.PROPERTY_CHANGE_OUTPUT, link);
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
			this.sources.remove(link);
			fireStructureChange(IConstants.PROPERTY_CHANGE_INPUT, link);
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
			this.targets.remove(link);
			this.fireStructureChange(IConstants.PROPERTY_CHANGE_OUTPUT, link);
		} catch (DisJException ignore) {
			System.err.println("@NodeElement.disconnectOutLink() "
					+ this.node.getNodeId() + " : " + ignore);
		}
	}

	/**
	 * TODO must return a copy
	 * 
	 * @return
	 */
	public List getSourceConnections() {
		return sources;
	}

	/**
	 * TODO must return a copy
	 * 
	 * @return
	 */
	public List getTargetConnections() {
		return targets;
	}

	public String toString() {
		return "<NodeElement>Name: " + this.node.getName() + " at: "
				+ getLocation();
	}

}
