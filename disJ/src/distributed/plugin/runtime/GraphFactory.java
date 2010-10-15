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

import java.util.HashMap;
import java.util.Map;

import distributed.plugin.core.DisJException;
import distributed.plugin.core.Graph;
import distributed.plugin.core.IConstants;

/**
 * @author Me
 * 
 * A factory that create and keep track of graphs in workspace
 */
public final class GraphFactory {

	//private static final Object lock = new Object();
	
	private static Map<String, Graph> GRAPH_LIST = new HashMap<String, Graph>(4);

	private GraphFactory() {
		
	}
	
	/**
	 * Factory that create a graph with unique id within workspace
	 * @return
	 */
	public static Graph createGraph() {
		Graph g = new Graph();
		return g;
	}
	
	/**
	 * Get a list of graphs currently in workspace
	 * 
	 * @return Returns the graphList.
	 */
	public static Map<String, Graph> getGraphList() {
		return GRAPH_LIST;
	}
	
	/**
	 * Add a new graph into workspace
	 * 
	 * @param g
	 * @throws DisJException
	 */
	public static void addGraph(Graph g) throws DisJException {	
		if(g == null){
			throw new DisJException(IConstants.ERROR_22);
		}
		String id = g.getId();		
		GRAPH_LIST.put(id, g);		
		 						
	}
	
	/**
	 * Give a graph w.r.t a given id
	 * 
	 * @param graphId
	 * @return
	 */
	public static Graph getGraph(String graphId){
		return GRAPH_LIST.get(graphId);
	}
	

}