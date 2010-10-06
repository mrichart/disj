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

	// a set of current latest unique ID for each graph in workspace {graphId, lastId}
	private Map<String, Integer> latestId;

	private static TimeGenerator timeGen;

	private static final Object lock = new Object();

	private TimeGenerator() {
		this.execTimes = null;
		this.execTimes = new HashMap<String, Integer>();
		this.latestId = new HashMap<String, Integer>();
	}

	/**
	 * A singleton TimeGenerator
	 * 
	 * @return An instance of TimeGenerator
	 */
	static TimeGenerator getTimeGenerator() {
		synchronized (lock) {
			if (timeGen == null){
				timeGen = new TimeGenerator();
			}
			return timeGen;
		}
	}

	/**
	 * Add a tracker to TimeGen after a new graph is added into workspace
	 * 
	 * @param graphId
	 */
	protected void addGraph(String graphId) {
		if (!this.execTimes.containsKey(graphId)) {
			this.execTimes.put(graphId, new Integer(0));
			this.latestId.put(graphId, new Integer(0));
		}
	}

	/**
	 * Get a current execution time (a top value on stack)
	 * 
	 * @param graphId A graph ID of the simulation
	 * @return A current simulation time unit if the graph ID exists, 
	 * otherwise -1 is returned
	 * 
	 */
	public int getCurrentTime(String graphId){
		Integer time = this.execTimes.get(graphId);
		if(time == null){
			return -1;
		}else{
			return time.intValue();
		}
	}

	/**
	 * Set a new current generated time (a top of stack event's execution time)
	 * 
	 * @param graphId
	 * @param time
	 */
	protected void setCurrentTime(String graphId, int time) {
		this.execTimes.put(graphId, time);
	}

	/**
	 * Reset time and unique ID of a given graph ID
	 * @param graphId
	 */
	protected void reset(String graphId){
		this.execTimes.remove(graphId);
		this.latestId.remove(graphId);
		
		this.addGraph(graphId);
	}
	
	/**
	 * Get a next and new unique ID w.r.t a given graph
	 * 
	 * @param graphId A given graph ID
	 * @return @throws
	 *         DistJException
	 */
	protected int getNextNewId(String graphId) throws DisJException {
		if (!this.latestId.containsKey(graphId))
			throw new DisJException(IConstants.ERROR_5, graphId);

		int id = latestId.get(graphId);
		this.latestId.put(graphId, id + 1);
		return id;
	}

}