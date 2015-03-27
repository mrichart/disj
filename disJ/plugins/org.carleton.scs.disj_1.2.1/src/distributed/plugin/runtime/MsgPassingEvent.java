/*******************************************************************************
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     DisJ Development Group
 *******************************************************************************/

package distributed.plugin.runtime;

import distributed.plugin.core.IConstants;

/**
 * @author npiyasin A data storage that it will be use for execution for each
 *         message
 */
@SuppressWarnings("serial")
public class MsgPassingEvent extends Event{

	private String hostId; // node sender id

	/*
	 * An edge ID that connected between sender and receiver
	 */
	private String edgeId;  

	private IMessage message;

	/**
	 * 
	 * @param hostId
	 *            an executing node's ID
	 * @param execTime
	 * @param eventId
	 * @param targets
	 *            a set of receiver nodes's name
	 * @param message
	 */
	public MsgPassingEvent(String hostId, short eventType, int execTime, int eventId,
			String target, IMessage message) {
		super(eventType, eventId, execTime);
		
		this.hostId = hostId;
		this.edgeId = target;
		this.message = message;
	}

	/**
	 * @return Returns the message.
	 */
	public IMessage getMessage() {
		return message;
	}

	/**
	 * Get an ID of an owner of this Event
	 * @return Returns the node ID.
	 */
	public String getHostId() {
		return hostId;
	}

	/**
	 * Get an edge ID base on the owner of the event
	 * that has been sent through
	 * 
	 * @return Returns the targets.
	 */
	public String getEdgeId() {
		return edgeId;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MsgPassingEvent))
			return false;

		MsgPassingEvent e = (MsgPassingEvent) obj;
		
		return (e.getHostId().equals(this.hostId) 
				&& e.getExecTime() == this.getExecTime() 
				&& e.getEventType() == this.getEventType());
	}
	
	@Override
	public String toString() {
		return super.toString()+ hostId + IConstants.MAIN_DELIMETER
				+ edgeId + IConstants.MAIN_DELIMETER + message.getLabel();
	}

	@Override
	public void setEventInfo(IMessage msg) {
		this.message = msg;
		
	}

	
	
}