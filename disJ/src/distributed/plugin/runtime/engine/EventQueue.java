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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import distributed.plugin.core.DisJException;
import distributed.plugin.core.IConstants;
import distributed.plugin.runtime.Event;

/**
 * @author npiyasin
 * 
 * Ordered queue of events, which is ordered by execTime and priority order of execution
 */
public class EventQueue {

	private List<Event> queue;

	protected EventQueue() {
		this.queue = new ArrayList<Event>();
	}

	protected synchronized boolean isEmpty() {
		return this.queue.isEmpty();
	}

	protected synchronized void pushEvent(Event event) {
		this.queue.add(event);
		this.orderQueue();
	}

	/*
	 * Add an Event into a queue
	 */
	protected synchronized void pushEvents(List<Event> events) {
		for (int i = 0; i < events.size(); i++) {
			this.queue.add(events.get(i));
		}
		this.orderQueue();
	}

	/*
	 * Get a next event that has a smallest timeId and highest priority 
	 * order of execution in the queue without remove
	 */
	protected synchronized Event topEvent() throws DisJException{
		if(this.queue.isEmpty())
			throw new DisJException(IConstants.ERROR_13);
		
		return this.queue.get(0);
	}

	/*
	 * Get and remove all the events that have a smallest timeId in the queue
	 * and follow the priority order of execution
	 */
	protected synchronized List<Event> popEvents() {
		int t = -1;
		int s = -1;
		List<Event> eList = new ArrayList<Event>();
		for (int i = 0; i < this.queue.size(); i++) {
			Event e = this.queue.get(i);
			if (t == -1) {
				t = e.getExecTime();
				s = e.getEventType();
				eList.add(e);
			} else if (t == e.getExecTime() && s == e.getEventType()) {
				eList.add(e);
			} else {
				break;
			}
		}
		this.queue.removeAll(eList);
		return eList;
	}

	/*
	 * Order the event in the queue, base on 
	 * @see distributed.core.runtime.engine.Event#compareTo(java.lang.Object)
	 */
	private void orderQueue() {
		Collections.sort(this.queue);
	}

	public String toString() {
		StringBuffer ele = new StringBuffer(1024);
		for (int i = 0; i < this.queue.size(); i++) {
			ele.append(this.queue.get(i) + "\n");
		}
		return ele.toString();
	}

}