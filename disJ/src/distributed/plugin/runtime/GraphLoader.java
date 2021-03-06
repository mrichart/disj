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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import distributed.plugin.core.DisJException;
import distributed.plugin.core.Edge;
import distributed.plugin.core.Graph;
import distributed.plugin.core.IConstants;
import distributed.plugin.core.Node;
import distributed.plugin.random.IRandom;
import distributed.plugin.runtime.adversary.AgentControl;
import distributed.plugin.runtime.adversary.MsgPassingControl;
import distributed.plugin.runtime.engine.BoardAgent;
import distributed.plugin.runtime.engine.Entity;
import distributed.plugin.runtime.engine.TokenAgent;

/**
 * @author Me
 * 
 * A utilities API for loading and accessing internal data of a given graph
 */
public class GraphLoader {

    // caching storages; for a long operation and using more than one time

    private static Map<Graph, List<Node>> initNodeTable = new HashMap<Graph, List<Node>>();

    /**
     * Get all the initializer node of a given graph
     * 
     * @param graph
     * @return
     */
    public static List<Node> getInitNodes(Graph graph) {

        if (graph == null)
            throw new NullPointerException(IConstants.RUNTIME_ERROR_0);

        List<Node> initList = initNodeTable.get(graph);
        if (initList == null || initList.isEmpty()) {
        	initList = new ArrayList<Node>();
            Map<String, Node> nodes = graph.getNodes();
            for (String id : nodes.keySet()) {
            	Node initNode = nodes.get(id);
                if (initNode.isInitializer()) {
                    initList.add(initNode);
                }
			}          
            initNodeTable.put(graph, initList);
        }
        return initList;
    }

    /**
     * TODO Clean up all the caches
     * 
     */
    public static void cleanAllCaches() {

    }

    /**
     * Create a client entity object by reflection with empty constructor
     * 
     * @param client
     *            a client class object that need to be created
     * @return
     * @throws Exception
     *             All the exception that caused by reflection processes
     */
    public static Entity createEntityObject(Class<Entity> client) throws Exception {
        if (client == null)
            throw new NullPointerException(IConstants.RUNTIME_ERROR_0);

        Entity entity = null;
        try {
            entity = (Entity) client.newInstance();
        } catch (InstantiationException e) {
            throw new DisJException(e);
        }
        return entity;
    }
    
    /**
     * Create a client Board agent object by reflection with empty constructor
     * 
     * @param client
     *            a client class object that need to be created
     * @return
     * @throws Exception
     *             All the exception that caused by reflection processes
     */
    public static BoardAgent createBoardAgentObject(Class<BoardAgent> client) throws Exception {
        if (client == null)
            throw new NullPointerException(IConstants.RUNTIME_ERROR_0);

        BoardAgent agent = null;
        try {
        	Object obj = client.newInstance();
        	agent = (BoardAgent) obj;
        } catch (Exception e) {
            throw new DisJException(e);
        }
        return agent;
    }
    
    /**
     * Create a client Token agent object by reflection with empty constructor
     * 
     * @param client
     *            a client class object that need to be created
     * @return
     * @throws Exception
     *             All the exception that caused by reflection processes
     */
    public static TokenAgent createTokenAgentObject(Class<TokenAgent> client) throws Exception {
        if (client == null)
            throw new NullPointerException(IConstants.RUNTIME_ERROR_0);

        TokenAgent agent = null;
        try {
        	agent = (TokenAgent) client.newInstance();
        } catch (InstantiationException e) {
            throw new DisJException(e);
        }
        return agent;
    }

    
    /**
     * Create a client MsgPassingControl object by reflection with empty constructor
     * 
     * @param client
     *            a client class object that need to be created
     * @return
     * @throws Exception
     *             All the exception that caused by reflection processes
     */
    public static MsgPassingControl createMsgPassAdversaryObject(Class<MsgPassingControl> client) throws Exception {
        if (client == null)
            throw new NullPointerException(IConstants.RUNTIME_ERROR_0);

        MsgPassingControl adversary = null;
        try {
        	Object obj = client.newInstance();
        	adversary = (MsgPassingControl) obj;
        } catch (Exception e) {
            throw new DisJException(e);
        }
        return adversary;
    }
    
    /**
     * Create a client AgentControl object by reflection with empty constructor
     * 
     * @param client
     *            a client class object that need to be created
     * @return
     * @throws Exception
     *             All the exception that caused by reflection processes
     */
    public static AgentControl createAgentAdversaryObject(Class<AgentControl> client) throws Exception {
        if (client == null)
            throw new NullPointerException(IConstants.RUNTIME_ERROR_0);

        AgentControl adversary = null;
        try {
        	Object obj = client.newInstance();
        	adversary = (AgentControl) obj;
        } catch (Exception e) {
            throw new DisJException(e);
        }
        return adversary;
    }
    
    /**
     * Create a client Random generator object by reflection with empty constructor
     * 
     * @param client
     *            a client class object that need to be created
     * @return
     * @throws Exception
     *             All the exception that caused by reflection processes
     */
    public static IRandom createClientRandomObject(Class<IRandom> client) throws Exception {
        if (client == null)
            throw new NullPointerException(IConstants.RUNTIME_ERROR_0);

        IRandom random = null;
        try {
        	Object obj = client.newInstance();
        	random = (IRandom) obj;
        } catch (Exception e) {
            throw new DisJException(e);
        }
        return random;
    }
    
    /**
     * Get a local port's label of a given edge on a given node
     * 
     * @param node
     * @param edge
     * @return
     */
    public static String getEdgeLabel(Node node, Edge edge) {

        Map<String, Edge> edges = node.getEdges();
        for (String label : edges.keySet()) {
        	 if (edges.get(label).equals(edge))
                 return (String) label;
		}
        return null;
    }

    /**
     * A deep clone graph object by making use of serialiazable
     * 
     * @param graph
     * @return
     */
    public static Graph cloneGraph(Graph graph) throws IOException {
        try {
            return (Graph) GraphLoader.deepClone(graph);
        } catch (DisJException e) {
            e.printStackTrace();
            throw new IOException(e.toString());
        }
    }

    /**
     * Make a deep clone of the given object by making use of serializable
     * 
     * @param object @return
     */
    public static Object deepClone(Object object) throws DisJException,
            IOException {

        if (object == null)
            return null;

        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(object);
            oos.flush();
            ByteArrayInputStream bin = new ByteArrayInputStream(bos
                    .toByteArray());
            ois = new ObjectInputStream(bin);
            return ois.readObject();
        } catch (Exception ie) {
            throw new DisJException(ie);
        } finally {
            if (oos != null)
                oos.close();
            if (ois != null)
                ois.close();
        }
    }
    /**
     * Covert a percentage of execution speed into a sleeping time of 
     * processor
     * 
     * @param value A percentage value of [0-100] inclusive
     * 
     * @return a millisecond of processor to be sleeping between 
     * [0 - 10000] milliseconds inclusive
     */
    public static int speedConverter(int value){
    	int rate = 10000;
    	
    	if(value < IConstants.SPEED_MIN_RATE){
    		value = IConstants.SPEED_MIN_RATE;
    	}
    	if(value > IConstants.SPEED_MAX_RATE){
    		value = IConstants.SPEED_MAX_RATE;
    	}
    	int sleeping = rate - ((value * rate)/IConstants.SPEED_MAX_RATE);
    	
    	return sleeping;
    }

}