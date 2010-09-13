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

import java.util.HashMap;
import java.util.Map;

import distributed.plugin.core.DisJException;
import distributed.plugin.core.IConstants;

/**
 * @author npiyasin A time generator, that will generate time unit and unique id
 *         in ordered w.r.t a given graph
 */
public class TimeGenerator {

	// a set of current execution time for each graph in workspace {graphId,
	// curTime}
	private Map<String, Integer> execTimes;

	// a set of current lastestId for each graph in workspace {graphId, lastId}
	private Map<String, Integer> lastestId;

	private static TimeGenerator timeGen;

	private static final Object lock = new Object();

	private TimeGenerator() {
		this.execTimes = null;
		this.execTimes = new HashMap<String, Integer>();
		this.lastestId = new HashMap<String, Integer>();
	}

	/**
	 * A singleton TimeGenerator
	 * 
	 * @return An instance of TimeGenerator
	 */
	static TimeGenerator getTimeGenerator() {
		synchronized (lock) {
			if (timeGen == null)
				timeGen = new TimeGenerator();

			return timeGen;
		}
	}

	/**
	 * Add a tracker to TimeGen after a new graph is added into workspace
	 * 
	 * @param graphId
	 */
	public void addGraph(String graphId) {
		if (!this.execTimes.containsKey(graphId)) {
			this.execTimes.put(graphId, new Integer(0));
			this.lastestId.put(graphId, new Integer(0));
		}
	}

	/**
	 * Get a current execution time (a top value on stack)
	 * 
	 * @param graphId
	 * @return A time period
	 * 
	 */
	public int getCurrentTime(String graphId){
		return this.execTimes.get(graphId);
	}

	/**
	 * Set a new current generated time (a top of stack event's execution time)
	 * 
	 * @param graphId
	 * @param time
	 */
	public void setCurrentTime(String graphId, int time) {
		this.execTimes.put(graphId, time);
	}

	/**
	 * Get a next and new unique ID w.r.t a given graph
	 * 
	 * @param graphId A given graph ID
	 * @return @throws
	 *         DistJException
	 */
	public int getNextNewId(String graphId) throws DisJException {
		if (!this.lastestId.containsKey(graphId))
			throw new DisJException(IConstants.ERROR_5, graphId);

		int id = lastestId.get(graphId);
		this.lastestId.put(graphId, id + 1);
		return id;
	}

	public static void main(String[] args) {
	}
}