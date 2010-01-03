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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.graphics.RGB;

import distributed.plugin.core.DisJException;
import distributed.plugin.core.Edge;
import distributed.plugin.core.IConstants;
import distributed.plugin.core.Node;
import distributed.plugin.random.IRandom;

/**
 * @author Me
 * 
 *         A graph
 */
public class Graph implements Serializable {

	static final long serialVersionUID = IConstants.SERIALIZE_VERSION;

	private int currentNodeId;

	private int currentEdgeId;

	private String id;

	private Map nodes;

	private Map edges;

	private Map stateColors;

	private short globalDelayType; // a type of delay i.e random uniform,

	// synchronize

	private short globalDelaySeed = 1; // it's usufull for synchronize delay and
										// random

	// uniform type

	private IRandom clientRandom;

	private String protocol;

	protected Graph() {
		this("");
	}

	private Graph(String id) {
		this.id = id;
		this.currentNodeId = 0;
		this.currentEdgeId = 0;
		this.stateColors = new HashMap();
		this.nodes = new HashMap();
		this.edges = new HashMap();


		stateColors.put(new Short("99"), new RGB(100, 150, 50));

		this.protocol = "";
	}

	/**
	 * Add a link that used in this graph
	 * 
	 * @param id
	 *            a unique edge id
	 * @param edge
	 * @throws DisJException
	 */
	public void addEdge(String id, Edge edge) throws DisJException {
		if (!edges.containsKey(id))
			edges.put(id, edge);
		else
			throw new DisJException(IConstants.ERROR_2, id);
	}

	/**
	 * Add a node that used in this graph
	 * 
	 * @param id
	 *            a unique node id
	 * @param node
	 * @throws DisJException
	 */
	public void addNode(String id, Node node) throws DisJException {
		if (!nodes.containsKey(id))
			nodes.put(id, node);
		else
			throw new DisJException(IConstants.ERROR_3, id);
	}

	public void addColor(Hashtable ht) {
		this.stateColors.putAll(ht);
		System.out.println("States:"+this.stateColors.keySet());		
	}

	/**
	 * Remove a node with a given id
	 * 
	 * @param id
	 * @throws DisJException
	 */
	public void removeNode(String id) throws DisJException {
		if (!nodes.containsKey(id))
			throw new DisJException(IConstants.ERROR_0, id);

		nodes.remove(id);
	}

	/**
	 * Remove an edge with a given id
	 * 
	 * @param id
	 * @throws DisJException
	 */
	public void removeEdge(String id) throws DisJException {
		if (!this.edges.containsKey(id))
			throw new DisJException(IConstants.ERROR_1, id);

		edges.remove(id);
	}

	public void removeColor(Short state) {
		this.stateColors.remove(state);
	}

	/**
	 * Get an edge with a given id
	 * 
	 * @param id
	 * @return
	 * @throws DisJException
	 */
	public Edge getEdge(String id) {
		try {
			if (!edges.containsKey(id))
				throw new DisJException(IConstants.ERROR_1, id);
			else
				return (Edge) edges.get(id);
		} catch (DisJException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Get a node with a given id
	 * 
	 * @param id
	 * @return
	 * @throws DisJException
	 */
	public Node getNode(String id) {
		try {
			if (!nodes.containsKey(id))
				throw new DisJException(IConstants.ERROR_0, id);
			else
				return (Node) nodes.get(id);
		} catch (DisJException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @return Returns all the edges in this graph
	 */
	public Map getEdges() {
		return edges;
	}

	public Iterator getStateColors() {
		Set s= this.stateColors.keySet();
		ArrayList ll = new ArrayList(s);
		try{
		Collections.sort(ll);
		}catch (Exception e) {
			e.printStackTrace();
		}
		return ll.iterator();
	}

	public RGB getColor(Object state) {
		return (RGB) this.stateColors.get(state);
	}

	public int getNumberOfState() {
		return this.stateColors.size();
	}

	/**
	 * @return Returns the id of this graph
	 */
	public String getId() {
		return this.id;
	}

	public short getGlobalDelayType() {
		return globalDelayType;
	}

	public short getGlobalDelaySeed() {
		return globalDelaySeed;
	}

	public void setGlobalDelaySeed(short delaySeed) {
		this.globalDelaySeed = delaySeed;
	}

	public void setGlobalDelayType(short delayType) {
		this.globalDelayType = delayType;
	}

	/**
	 * Allow to set the ID only once
	 * 
	 * @param id
	 */
	public void setId(String id) {
		if (this.id == null || this.id.trim().equals(""))
			this.id = id;
	}

	/**
	 * @return Returns all the nodes in this graph
	 */
	public Map getNodes() {
		return nodes;
	}

	public int getCurrentEdgeId() {
		return currentEdgeId++;
	}

	public int getCurrentNodeId() {
		return currentNodeId++;
	}

	public IRandom getClientRandom() {
		return clientRandom;
	}

	public void setClientRandom(IRandom clientRandom) {
		this.clientRandom = clientRandom;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

}
