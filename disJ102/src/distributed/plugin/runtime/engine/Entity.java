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

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import distributed.plugin.core.DisJException;
import distributed.plugin.core.Edge;
import distributed.plugin.core.IConstants;
import distributed.plugin.core.Node;
import distributed.plugin.runtime.ICommunicable;
import distributed.plugin.runtime.IMessage;
import distributed.plugin.ui.IGraphEditorConstants;

/**
 * @author Me
 * 
 *         An entity class use for client for create an algorithm for testing
 */
public abstract class Entity implements ICommunicable {

	private int startState;

	private int lastState = -1;
	
	private Processor processor;

	private Node nodeOwner;

	private MessageConsole console;

	private MessageConsoleStream out;

	protected Entity(int state) {
		this.startState = state;
		this.processor = null;
		this.nodeOwner = null;
		this.console = Entity.findConsole(IGraphEditorConstants.DISJ_CONSOLE);
		this.out = this.console.newMessageStream();
		System.setOut(new PrintStream(this.out));
	}

	/**
	 * Initialize this entity if it is set as initializer entity
	 * 
	 */
	public abstract void init();

	/**
	 * Method invoke when this entity receives the message
	 * 
	 * @param incomingPort
	 *            A receiving port label
	 * @param message
	 *            A received message
	 */
	public abstract void receive(String incomingPort, IMessage message);

	/**
	 * Method invoke when an internal alarm clock of this entity is ring
	 * 
	 */
	public abstract void alarmRing();

	/**
	 * Assign processor and the corresponding node to this entity
	 * 
	 * @param processor
	 * @param owner
	 */
	void initEntity(Processor processor, Node owner, Map<Integer, String> states) {
		if (this.processor == null)
			this.processor = processor;

		if (this.nodeOwner == null)
			this.nodeOwner = owner;

		this.nodeOwner.setStateNames(states);
		this.nodeOwner.initStartState(this.startState);
	}

	/**
	 * @return Returns the nodeOwner.
	 */
	final Node getNodeOwner() {
		return this.nodeOwner;
	}

	/**
	 * Get a name of this entity
	 * 
	 * @return A name of this entity
	 */
	public final String getName() {
		return this.nodeOwner.getName();
	}

	/**
	 * Get a current state of this entity
	 * 
	 * @return a state of this entity
	 */
	public final int getState() {
		try {
			if (this.nodeOwner == null)
				throw new DisJException(IConstants.ERROR_7);

		} catch (DisJException e) {
			e.printStackTrace();
			this.printToConsole(e);

		} catch (RuntimeException e) {
			e.printStackTrace();
			this.printToConsole(e);
		}

		return this.nodeOwner.getCurState();
	}

	/**
	 * Get user input value from the editor
	 * @return A string of value, if value is empty, it returns
	 * an empty string
	 */
	public final String getUserInput(){
		try {
			if (this.nodeOwner == null)
				throw new DisJException(IConstants.ERROR_7);

		} catch (DisJException e) {
			e.printStackTrace();
			this.printToConsole(e);

		} catch (RuntimeException e) {
			e.printStackTrace();
			this.printToConsole(e);
		}
		return this.nodeOwner.getUserInput();
	}
	
	/**
	 * Get all local in and out port labels of this entity. If a port of
	 * bi-directoin link it will consider as one port.
	 * 
	 * @return A list of String of all local port labels
	 */
	public final List<String> getPorts() {		
		List<String> ports = new ArrayList<String>();
		for (String label : this.nodeOwner.getPorts().keySet()) {
			ports.add(label);
		}
		return ports;
	}

	/**
	 * Get all outgoing port label of this entity
	 * 
	 * @return A list of String all out going port labels
	 */
	public final List<String> getOutPorts() {
		return this.nodeOwner.getOutgoingPorts();
	}

	/**
	 * Get all incoming port labels of this entity
	 * 
	 * @return A list of String of all incoming port labels
	 */
	public final List<String> getInPorts() {
		return this.nodeOwner.getIncomingPorts();
	}

	/**
	 * Set state of this entity to be at a given state
	 * 
	 * @param state
	 *            A state that need to be set
	 */
	public final void become(int state) {
		if (lastState==state) {return;}
		else{
			lastState=state;
		}
		try {
			if (this.nodeOwner == null)
				throw new DisJException(IConstants.ERROR_7);

			this.nodeOwner.setCurState(state);
			
			//state change record
			//this.processor.appendToRecFile("sc:"+this.getId()
			//		+ IGraphEditorConstants.STATE_CHANGE + state);
			

		} catch (DisJException e) {
			e.printStackTrace();
			this.printToConsole(e);

		} catch (RuntimeException e) {
			e.printStackTrace();
			this.printToConsole(e);
		}
	}

	private String getNodeId() {
		return this.nodeOwner.getNodeId();
	}

	// FIXME what is lvc ?????
	public final void setLinkVisibility(String port, boolean visible) {
//		try {
//			getNodeOwner().getEdge(port).getLinkElement().setVisible(visible);
//			//this.processor.appendToRecFile("lvc:"+port
//			//		+ IGraphEditorConstants.LINK_VISIBILITY_CHANGE + visible);
//			//link visibility change record
//		} catch (DisJException e) {
//			e.printStackTrace();
//		}
	}

	// FIXME this will NOT work!!!
	public final String getNeighbourName(String port) {
		String returnValue = "";
		try {
//			String name1 = getNodeOwner().getEdge(port).getLinkElement()
//					.getSource().getNode().getName();
			String name1 = getNodeOwner().getEdge(port).getStart().getName();
			
//			String name2 = getNodeOwner().getEdge(port).getLinkElement()
//					.getTarget().getNode().getName();
			
			String name2 = getNodeOwner().getEdge(port).getEnd().getName();

			
			if (name1.endsWith(this.getName())) {
				return returnValue = name2;
			} else {
				return returnValue = name1;
			}
		} catch (DisJException e) {
			e.printStackTrace();
		}
		return returnValue;
	}

	/**
	 * Set an internal clock to be alarmed after a give time unit If time is
	 * less than or equal to zero, it will ignore
	 * 
	 * Time unit is a DisJ simulation clock
	 * 
	 * @see distributed.plugin.runtime.ICommunicable#setAlarm(short)
	 */
	public final void setAlarm(int time) {
		try {
			if (time > 0) {
				this.processor.internalNotify(this.nodeOwner.getNodeId(),
						new Message(IConstants.SET_ALARM_CLOCK, new Integer(
								time)));
			}
		} catch (DisJException e) {
			e.printStackTrace();
			this.printToConsole(e);
		} catch (RuntimeException e) {
			e.printStackTrace();
			this.printToConsole(e);
		}
	}

	/**
	 * Block and unblock any incoming message through a given port label, state
	 * of block is true and unblock is false.
	 * 
	 * @see distributed.plugin.runtime.ICommunicable#blockPort(java.lang.String,
	 *      boolean)
	 */
	public final void blockPort(String label, boolean state) {
		try {
			Object[] msg = new Object[] { label, new Boolean(state) };
			this.processor.internalNotify(this.nodeOwner.getNodeId(),
					new Message(IConstants.SET_BLOCK_MSG, msg));
		} catch (DisJException e) {
			e.printStackTrace();
			this.printToConsole(e);

		} catch (RuntimeException e) {
			e.printStackTrace();
			this.printToConsole(e);
		}
	}

	/**
	 * Send a given message through a given port name. If a port name does not
	 * exist, there will be no message sending.
	 * 
	 * @param portLabel
	 *            An out going port label
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendTo(String portLabel, boolean message) {
		this.sendTo("", portLabel, new Boolean(message));
	}

	/**
	 * Send a given message through a given port name. If a port name does not
	 * exist, there will be no message sending.
	 * 
	 * @param portLabel
	 *            An out going port label
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendTo(String portLabel, short message) {
		this.sendTo("", portLabel, new Short(message));
	}

	/**
	 * Send a given message through a given port name. If a port name does not
	 * exist, there will be no message sending.
	 * 
	 * @param portLabel
	 *            An out going port label
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendTo(String portLabel, int message) {
		this.sendTo("", portLabel, new Integer(message));
	}

	/**
	 * Send a given message through a given port name. If a port name does not
	 * exist, there will be no message sending.
	 * 
	 * @param portLabel
	 *            An out going port label
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendTo(String portLabel, float message) {
		this.sendTo("", portLabel, new Float(message));
	}

	/**
	 * Send a given message through a given port name. If a port name does not
	 * exist, there will be no message sending.
	 * 
	 * @param portLabel
	 *            An out going port label
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendTo(String portLabel, long message) {
		this.sendTo("", portLabel, new Long(message));
	}

	/**
	 * Send a given message through a given port name. If a port name does not
	 * exist, there will be no message sending.
	 * 
	 * @param portLabel
	 *            An out going port label
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendTo(String portLabel, double message) {
		this.sendTo("", portLabel, new Double(message));
	}

	/**
	 * Send a given message through a given port name. If a port name does not
	 * exist, there will be no message sending.
	 * 
	 * @param portLabel
	 *            An out going port label
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendTo(String portLabel, Serializable message) {
		this.sendTo("", portLabel, message);

	}

	/**
	 * Send a given message with a given message label through a given port
	 * name. If a port name does not exist, there will be no message sending.
	 * 
	 * @param msgLabel
	 *            A message label
	 * @param portLabel
	 *            An out going port label
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendTo(String msgLabel, String portLabel, boolean message) {
		this.sendTo(msgLabel, portLabel, new Boolean(message));
	}

	/**
	 * Send a given message with a given message label through a given port
	 * name. If a port name does not exist, there will be no message sending.
	 * 
	 * @param msgLabel
	 *            A message label
	 * @param portLabel
	 *            An out going port label
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendTo(String msgLabel, String portLabel, short message) {
		this.sendTo(msgLabel, portLabel, new Short(message));
	}

	/**
	 * Send a given message with a given message label through a given port
	 * name. If a port name does not exist, there will be no message sending.
	 * 
	 * @param msgLabel
	 *            A message label
	 * @param portLabel
	 *            An out going port label
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendTo(String msgLabel, String portLabel, int message) {
		this.sendTo(msgLabel, portLabel, new Integer(message));
	}

	/**
	 * Send a given message with a given message label through a given port
	 * name. If a port name does not exist, there will be no message sending.
	 * 
	 * @param msgLabel
	 *            A message label
	 * @param portLabel
	 *            An out going port label
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendTo(String msgLabel, String portLabel, float message) {
		this.sendTo(msgLabel, portLabel, new Float(message));
	}

	/**
	 * Send a given message with a given message label through a given port
	 * name. If a port name does not exist, there will be no message sending.
	 * 
	 * @param msgLabel
	 *            A message label
	 * @param portLabel
	 *            An out going port label
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendTo(String msgLabel, String portLabel, long message) {
		this.sendTo(msgLabel, portLabel, new Long(message));
	}

	/**
	 * Send a given message with a given message label through a given port
	 * name. If a port name does not exist, there will be no message sending.
	 * 
	 * @param msgLabel
	 *            A message label
	 * @param portLabel
	 *            An out going port label
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendTo(String msgLabel, String portLabel, double message) {
		this.sendTo(msgLabel, portLabel, new Double(message));
	}

	/**
	 * Send a given message with a given message label through a given port
	 * name. If a port name does not exist, there will be no message sending.
	 * 
	 * @param msgLabel
	 *            A message label
	 * @param portLabel
	 *            An out going port label
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendTo(String msgLabel, String portLabel,
			Serializable message) {
		try {
			if (this.processor == null)
				throw new DisJException(IConstants.ERROR_7);

			List<String> recv = new ArrayList<String>();
			recv.add(portLabel);
			
			this.processor.processMessage(this.nodeOwner.getNodeId(), recv,
					new Message(msgLabel, message));
			
		} catch (DisJException e) {
			e.printStackTrace();
			this.printToConsole(e);
		} catch (RuntimeException e) {
			e.printStackTrace();
			this.printToConsole(e);
		}
	}

	/**
	 * Send a given message through a given list of port names. If a port name
	 * does not exist, there will be no message sending.
	 * 
	 * @param portLabels
	 *            A list of out going port labels
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendTo(String[] portLabels, boolean message) {
		this.sendTo("", portLabels, new Boolean(message));
	}

	/**
	 * Send a given message through a given list of port names. If a port name
	 * does not exist, there will be no message sending.
	 * 
	 * @param portLabels
	 *            A list of out going port labels
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendTo(String[] portLabels, short message) {
		this.sendTo("", portLabels, new Short(message));
	}

	/**
	 * Send a given message through a given list of port names. If a port name
	 * does not exist, there will be no message sending.
	 * 
	 * @param portLabels
	 *            A list of out going port labels
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendTo(String[] portLabels, int message) {
		this.sendTo("", portLabels, new Integer(message));
	}

	/**
	 * Send a given message through a given list of port names. If a port name
	 * does not exist, there will be no message sending.
	 * 
	 * @param portLabels
	 *            A list of out going port labels
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendTo(String[] portLabels, float message) {
		this.sendTo("", portLabels, new Float(message));
	}

	/**
	 * Send a given message through a given list of port names. If a port name
	 * does not exist, there will be no message sending.
	 * 
	 * @param portLabels
	 *            A list of out going port labels
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendTo(String[] portLabels, long message) {
		this.sendTo("", portLabels, new Long(message));
	}

	/**
	 * Send a given message through a given list of port names. If a port name
	 * does not exist, there will be no message sending.
	 * 
	 * @param portLabels
	 *            A list of out going port labels
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendTo(String[] portLabels, double message) {
		this.sendTo("", portLabels, new Double(message));
	}

	/**
	 * Send a given message through a given list of port names. If a port name
	 * does not exist, there will be no message sending.
	 * 
	 * @param portLabels
	 *            A list of out going port labels
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendTo(String[] portLabels, Serializable message) {
		this.sendTo("", portLabels, message);

	}

	/**
	 * Send a given message with a given message label through a given list of
	 * port names. If a port name does not exist, there will be no message
	 * sending.
	 * 
	 * @param msgLabel
	 *            A message label
	 * @param portLabels
	 *            A list of out going port labels
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendTo(String msgLabel, String[] portLabels,
			boolean message) {
		this.sendTo(msgLabel, portLabels, new Boolean(message));
	}

	/**
	 * Send a given message with a given message label through a given list of
	 * port names. If a port name does not exist, there will be no message
	 * sending.
	 * 
	 * @param msgLabel
	 *            A message label
	 * @param portLabels
	 *            A list of out going port labels
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendTo(String msgLabel, String[] portLabels, short message) {
		this.sendTo(msgLabel, portLabels, new Short(message));
	}

	/**
	 * Send a given message with a given message label through a given list of
	 * port names. If a port name does not exist, there will be no message
	 * sending.
	 * 
	 * @param msgLabel
	 *            A message label
	 * @param portLabels
	 *            A list of out going port labels
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendTo(String msgLabel, String[] portLabels, int message) {
		this.sendTo(msgLabel, portLabels, new Integer(message));
	}

	/**
	 * Send a given message with a given message label through a given list of
	 * port names. If a port name does not exist, there will be no message
	 * sending.
	 * 
	 * @param msgLabel
	 *            A message label
	 * @param portLabels
	 *            A list of out going port labels
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendTo(String msgLabel, String[] portLabels, float message) {
		this.sendTo(msgLabel, portLabels, new Float(message));
	}

	/**
	 * Send a given message with a given message label through a given list of
	 * port names. If a port name does not exist, there will be no message
	 * sending.
	 * 
	 * @param msgLabel
	 *            A message label
	 * @param portLabels
	 *            A list of out going port labels
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendTo(String msgLabel, String[] portLabels, long message) {
		this.sendTo(msgLabel, portLabels, new Long(message));
	}

	/**
	 * Send a given message with a given message label through a given list of
	 * port names. If a port name does not exist, there will be no message
	 * sending.
	 * 
	 * @param msgLabel
	 *            A message label
	 * @param portLabels
	 *            A list of out going port labels
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendTo(String msgLabel, String[] portLabels,
			double message) {
		this.sendTo(msgLabel, portLabels, new Double(message));
	}

	/**
	 * Send a given message with a given message label through a given list of
	 * port names. If a port name does not exist, there will be no message
	 * sending.
	 * 
	 * @param msgLabel
	 *            A message label
	 * @param portLabels
	 *            A list of out going port labels
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendTo(String msgLabel, String[] portLabels,
			Serializable message) {
		try {
			if (this.processor == null)
				throw new DisJException(IConstants.ERROR_7);

			if (portLabels == null)
				throw new IllegalArgumentException(new DisJException(
						IConstants.ERROR_20).toString());

			// send the message to all given ports if it exists
			List<String> recv = new ArrayList<String>();
			Map<String, Short> ports = this.nodeOwner.getPorts();
			for (int i = 0; i < portLabels.length; i++) {
				if (ports.containsKey(portLabels[i])) {
					short portType = ports.get(portLabels[i]);
					if (portType == IConstants.DIRECTION_OUT
							|| portType == IConstants.DIRECTION_BI) {
						recv.add(portLabels[i]);
					}
				}
			}
			if (!recv.isEmpty()) {
				this.processor.processMessage(this.nodeOwner.getNodeId(), recv,
						new Message(msgLabel, message));
			}
		} catch (DisJException e) {
			e.printStackTrace();
			this.printToConsole(e);
		} catch (RuntimeException e) {
			e.printStackTrace();
			this.printToConsole(e);
		}

	}

	/**
	 * Send a given message through all out going ports
	 * 
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendToAll(boolean message) {
		this.sendToAll("", new Boolean(message));
	}

	/**
	 * Send a given message through all out going ports
	 * 
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendToAll(short message) {
		this.sendToAll("", new Short(message));
	}

	/**
	 * Send a given message through all out going ports
	 * 
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendToAll(int message) {
		this.sendToAll("", new Integer(message));
	}

	/**
	 * Send a given message through all out going ports
	 * 
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendToAll(float message) {
		this.sendToAll("", new Float(message));
	}

	/**
	 * Send a given message through all out going ports
	 * 
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendToAll(long message) {
		this.sendToAll("", new Long(message));

	}

	/**
	 * Send a given message through all out going ports
	 * 
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendToAll(double message) {
		this.sendToAll("", new Double(message));

	}

	/**
	 * Send a given message through all out going ports
	 * 
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendToAll(Serializable message) {
		this.sendToAll("", message);
	}

	/**
	 * Send a given message with a given label through all out going ports
	 * 
	 * @param msgLabel
	 *            A message label
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendToAll(String msgLabel, boolean message) {
		this.sendToAll(msgLabel, new Boolean(message));
	}

	/**
	 * Send a given message with a given label through all out going ports
	 * 
	 * @param msgLabel
	 *            A message label
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendToAll(String msgLabel, short message) {
		this.sendToAll(msgLabel, new Short(message));
	}

	/**
	 * Send a given message with a given label through all out going ports
	 * 
	 * @param msgLabel
	 *            A message label
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendToAll(String msgLabel, int message) {
		this.sendToAll(msgLabel, new Integer(message));
	}

	/**
	 * Send a given message with a given label through all out going ports
	 * 
	 * @param msgLabel
	 *            A message label
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendToAll(String msgLabel, float message) {
		this.sendToAll(msgLabel, new Float(message));
	}

	/**
	 * Send a given message with a given label through all out going ports
	 * 
	 * @param msgLabel
	 *            A message label
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendToAll(String msgLabel, long message) {
		this.sendToAll(msgLabel, new Long(message));
	}

	/**
	 * Send a given message with a given label through all out going ports
	 * 
	 * @param msgLabel
	 *            A message label
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendToAll(String msgLabel, double message) {
		this.sendToAll(msgLabel, new Double(message));
	}

	/**
	 * Send a given message with a given label through all out going ports
	 * 
	 * @param msgLabel
	 *            A message label
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendToAll(String msgLabel, Serializable message) {
		try {
			if (this.processor == null){
				throw new DisJException(IConstants.ERROR_7);
			}
			
			List<String> recv = this.getOutgoingPorts();
			
			this.processor.processMessage(this.nodeOwner.getNodeId(), recv,
					new Message(msgLabel, message));

		} catch (DisJException e) {
			e.printStackTrace();
			this.printToConsole(e);
		} catch (RuntimeException e) {
			e.printStackTrace();
			this.printToConsole(e);
		}
	}

	/*
     * 
     */
	private List<String> getOutgoingPorts() {
		// retrieve all outgoing port's labels of sender's node
		Map<String, Edge> edges = this.nodeOwner.getEdges();
		Map<String, Short> ports = this.nodeOwner.getPorts();
		List<String> recv = new ArrayList<String>();
		for (String pLabel : edges.keySet()) {
			short p = ports.get(pLabel);
			if (p== IConstants.DIRECTION_OUT
					|| p == IConstants.DIRECTION_BI) {
				recv.add(pLabel);
			}
		}
		return recv;
	}

	/**
	 * Send a message to all out going ports, except a port that it just
	 * received.
	 * 
	 * NOTE: This will work only for Bidirectional link. For Uni-directional
	 * link, it will work only if a label of incoming port is the same as out
	 * going port label. Otherwise, all out going ports will be sent.
	 * 
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendToOthers(boolean message) {
		this.sendToOthers("", new Boolean(message));
	}

	/**
	 * Send a message to all out going ports, except a port that it just
	 * received.
	 * 
	 * NOTE: This will work only for Bidirectional link. For Uni-directional
	 * link, it will work only if a label of incoming port is the same as out
	 * going port label. Otherwise, all out going ports will be sent.
	 * 
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendToOthers(short message) {
		this.sendToOthers("", new Short(message));
	}

	/**
	 * Send a message to all out going ports, execept a port that it just
	 * received.
	 * 
	 * NOTE: This will work only for Bidirectional link. For Uni-directional
	 * link, it will work only if a lable of incomming port is the same as out
	 * going port label. Otherwise, all out going ports will be sent.
	 * 
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendToOthers(int message) {
		this.sendToOthers("", new Integer(message));
	}

	/**
	 * Send a message to all out going ports execept a port that it received
	 * latest.
	 * 
	 * NOTE: This will work only for Bidirectional link. For Uni-directional
	 * link, it will work only if a lable of incomming port is the same as out
	 * going port label. Otherwise, all out going ports will be sent.
	 * 
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendToOthers(float message) {
		this.sendToOthers("", new Float(message));
	}

	/**
	 * Send a message to all out going ports, execept a port that it just
	 * received.
	 * 
	 * NOTE: This will work only for Bidirectional link. For Uni-directional
	 * link, it will work only if a lable of incomming port is the same as out
	 * going port label. Otherwise, all out going ports will be sent.
	 * 
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendToOthers(long message) {
		this.sendToOthers("", new Long(message));
	}

	/**
	 * Send a message to all out going ports, execept a port that it just
	 * received.
	 * 
	 * NOTE: This will work only for Bidirectional link. For Uni-directional
	 * link, it will work only if a lable of incomming port is the same as out
	 * going port label. Otherwise, all out going ports will be sent.
	 * 
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendToOthers(double message) {
		this.sendToOthers("", new Double(message));
	}

	/**
	 * Send a message to all out going ports, execept a port that it just
	 * received.
	 * 
	 * NOTE: This will work only for Bidirectional link. For Uni-directional
	 * link, it will work only if a lable of incomming port is the same as out
	 * going port label. Otherwise, all out going ports will be sent.
	 * 
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendToOthers(Serializable message) {
		this.sendToOthers("", message);
	}

	/**
	 * Send a message to all out going ports, execept a port that it just
	 * received.
	 * 
	 * NOTE: This will work only for Bidirectional link. For Uni-directional
	 * link, it will work only if a lable of incomming port is the same as out
	 * going port label. Otherwise, all out going ports will be sent.
	 * 
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendToOthers(String msgLabel, boolean message) {
		this.sendToOthers(msgLabel, new Boolean(message));
	}

	/**
	 * Send a message with a given message label to all out going ports execept
	 * a port that it just received.
	 * 
	 * 
	 * NOTE: This will work only for Bidirectional link. For Uni-directional
	 * link, it will work only if a lable of incomming port is the same as out
	 * going port label. Otherwise, all out going ports will be sent.
	 * 
	 * @param msgLabel
	 *            A message label
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendToOthers(String msgLabel, short message) {
		this.sendToOthers(msgLabel, new Short(message));
	}

	/**
	 * Send a message with a given message label to all out going ports execept
	 * a port that it just received.
	 * 
	 * 
	 * NOTE: This will work only for Bidirectional link. For Uni-directional
	 * link, it will work only if a lable of incomming port is the same as out
	 * going port label. Otherwise, all out going ports will be sent.
	 * 
	 * @param msgLabel
	 *            A message label
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendToOthers(String msgLabel, int message) {
		this.sendToOthers(msgLabel, new Integer(message));
	}

	/**
	 * Send a message with a given message label to all out going ports execept
	 * a port that it just received.
	 * 
	 * 
	 * NOTE: This will work only for Bidirectional link. For Uni-directional
	 * link, it will work only if a lable of incomming port is the same as out
	 * going port label. Otherwise, all out going ports will be sent.
	 * 
	 * @param msgLabel
	 *            A message label
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendToOthers(String msgLabel, float message) {
		this.sendToOthers(msgLabel, new Float(message));
	}

	/**
	 * Send a message with a given message label to all out going ports execept
	 * a port that it just received.
	 * 
	 * 
	 * NOTE: This will work only for Bidirectional link. For Uni-directional
	 * link, it will work only if a lable of incomming port is the same as out
	 * going port label. Otherwise, all out going ports will be sent.
	 * 
	 * @param msgLabel
	 *            A message label
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendToOthers(String msgLabel, long message) {
		this.sendToOthers(msgLabel, new Long(message));
	}

	/**
	 * Send a message with a given message label to all out going ports execept
	 * a port that it received latest.
	 * 
	 * 
	 * NOTE: This will work only for Bidirectional link. For Uni-directional
	 * link, it will work only if a lable of incomming port is the same as out
	 * going port label. Otherwise, all out going ports will be sent.
	 * 
	 * @param msgLabel
	 *            A message label
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendToOthers(String msgLabel, double message) {
		this.sendToOthers(msgLabel, new Double(message));
	}

	/**
	 * Send a message with a given message label to all out going ports, execept
	 * a port that it just received.
	 * 
	 * 
	 * NOTE: This will work only for Bidirectional link. For Uni-directional
	 * link, it will work only if a lable of incomming port is the same as out
	 * going port label. Otherwise, all out going ports will be sent.
	 * 
	 * @param msgLabel
	 *            A message label
	 * @param message
	 *            A message that need to be sent
	 */
	public final void sendToOthers(String msgLabel, Serializable message) {
		try {
			if (this.processor == null)
				throw new DisJException(IConstants.ERROR_7);

			String recvPort = this.nodeOwner.getLatestRecvPort();
			if (recvPort == null) {
				// call the method at a wrong place
				this.printToConsole(DisJException.ERROR_CODE[21]);
				return;
			}

			// retrieve all outgoing port's labels of sender's node
			// minus a latest received port
			Map<String, Edge> edges = this.nodeOwner.getEdges();
			Map<String, Short> ports = this.nodeOwner.getPorts();
			List<String> recv = new ArrayList<String>();
			for (String pLabel : edges.keySet()) {
				short portType = ports.get(pLabel);
				if (portType == IConstants.DIRECTION_OUT
						|| portType == IConstants.DIRECTION_BI) {
					if (!recvPort.equals(pLabel))
						recv.add(pLabel);
				}
			}
			
			if (!recv.isEmpty()) {
				this.processor.processMessage(this.nodeOwner.getNodeId(), recv,
						new Message(msgLabel, message));
			}

		} catch (DisJException e) {
			e.printStackTrace();
			this.printToConsole(e);
		} catch (RuntimeException e) {
			e.printStackTrace();
			this.printToConsole(e);
		}
	}

	public static MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMgr = plugin.getConsoleManager();
		IConsole[] existing = conMgr.getConsoles();
		for (int i = 0; i < existing.length; i++){
			if (name.equals(existing[i].getName())){
				return (MessageConsole) existing[i];
			}
		}
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMgr.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}

	/**
	 * Print out a text String into an Eclipse Console
	 * 
	 * Note: This work only with eclipse plugin
	 * 
	 * @param text
	 */
	public void printToConsole(Object text) {
		if (text != null) {
			this.out.println(text.toString());
			this.processor.appendConsoleOutput(text.toString());
		} else {
			this.out.println("null");
		}
	}

	protected MessageConsole getConsole() {
		return this.console;
	}

	protected MessageConsoleStream getConsoleStream() {
		return this.out;
	}

	/**
	 * Get a current simulation time unit, a time value starts from zero when
	 * the process is start running
	 * 
	 * @return A current time unit at the time method is calling
	 */
	public int getTime() {
		return this.processor.getCurrentTime();
	}

	/**
	 * Get current messages casual time<br>
	 * 
	 * A message casual time is a sum of successive messages that has been
	 * caused by another message.<br>
	 * 
	 * Example<br>
	 * <code>
	 * 	msgA -> msgB -> msgC -> msgE<br>
	 * 	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	 * 			msgB -> msgD -> msgF -> msgG<br>
     * </code>
	 * <p>
	 * In this example msgA causes the creation of msgB, and msgB causes the
	 * creation of msgC and msgD, and msgD casue msgF and so on, while msgE has
	 * no cause to creation of any message.
	 * </p>
	 * 
	 * <p>
	 * The current message casual time when msgG is created is 4 becasue there
	 * are 4 hops from msgA to msgG from this illustration
	 * </p>
	 * 
	 * 
	 * @return a message casual time which start from the beging of the process
	 *         to a current call of this method
	 */
	public int getCurrentMessageCasualTime() {
		return this.processor.getCallingChain();
	}

	/**
	 * Get X coordinate of a node represented in Graph Editor
	 * 
	 * @return
	 */
	public final int getLocationX() {
		return this.nodeOwner.getX();
	}

	/**
	 * Get Y coordinate of a node represented in Graph Editor
	 * 
	 * @return
	 */
	public final int getLocationY() {
		return this.nodeOwner.getY();
	}
	
	public final int getMaxXLocation(){
		return this.nodeOwner.getMaxX();
	}
	
	public final int getMaxYLocation(){
		return this.nodeOwner.getMaxY();
	}

}