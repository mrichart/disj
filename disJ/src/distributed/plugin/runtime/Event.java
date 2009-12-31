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
public class Event implements Comparable {

	private short eventType;

	private int eventId;

	private String owner; // node id

	private int execTime;

	/**
	 * an edge label's name (used by owner node) that
	 * connect to a receiver
	 */
	private String edgeName;  

	private IMessage message;

	/**
	 * 
	 * @param owner
	 *            an executing node's name
	 * @param execTime
	 * @param eventId
	 * @param targets
	 *            a set of receiver nodes's name
	 * @param message
	 */
	public Event(String owner, short eventType, int execTime, int eventId,
			String target, IMessage message) {
		this.owner = owner;
		this.eventType = eventType;
		this.execTime = execTime;
		this.eventId = eventId;
		this.edgeName = target;
		this.message = message;
	}

	/**
	 * @return Returns the eventId.
	 */
	public int getEventId() {
		return eventId;
	}

	/**
	 * @return Returns the execTime.
	 */
	public int getExecTime() {
		return execTime;
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
	public String getOwner() {
		return owner;
	}

	/**
	 * @return Returns the eventType.
	 */
	public short getEventType() {
		return eventType;
	}

	/**
	 * Get an local edge's name base on the owner of the event
	 * that has been sent through
	 * 
	 * @return Returns the targets.
	 */
	public String getEdgeName() {
		return edgeName;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof Event))
			return false;

		Event e = (Event) obj;
		return (e.owner == this.owner && e.execTime == this.execTime && e.eventType == this.eventType);
	}

	public int hashCode() {
		return this.execTime + this.eventType;
	}

	public int compareTo(Object obj) {
		Event e = (Event) obj;
		if (this.execTime < e.getExecTime())
			return -1;
		else if (this.execTime == e.getExecTime()) {
			if (this.eventType < e.getEventType())
				return -1;
			else if (this.eventType == e.getEventType())
				return 0;
			else
				return 1;
		} else
			return 1;
	}

	public String toString() {
		return eventId + IConstants.MAIN_DELIMETER + owner + IConstants.MAIN_DELIMETER
				+ execTime + IConstants.MAIN_DELIMETER + edgeName
				+ IConstants.MAIN_DELIMETER + message.getLabel();
	}
	/**
	 * @param execTime The execTime to set.
	 */
	public void setExecTime(int execTime) {
		this.execTime = execTime;
	}
}