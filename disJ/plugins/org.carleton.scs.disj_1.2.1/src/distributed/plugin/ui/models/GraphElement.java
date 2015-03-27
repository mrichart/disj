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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import distributed.plugin.core.DisJException;
import distributed.plugin.core.Edge;
import distributed.plugin.core.Graph;
import distributed.plugin.core.IConstants;
import distributed.plugin.core.Node;
import distributed.plugin.runtime.GraphFactory;
import distributed.plugin.stat.GraphStat;
import distributed.plugin.ui.IGraphEditorConstants;
import distributed.plugin.ui.validators.NumberCellEditorValidator;

/**
 * Graph adapter
 * 
 * @version 0.1
 */
public class GraphElement extends AdapterElement {

	static final long serialVersionUID = IConstants.SERIALIZE_VERSION;
	
	private static final String PROPERTY_NAME = "G00 Graph Name";
	private static final String PROPERTY_TOTAL_NODE = "G01 Total Nodes";
	private static final String PROPERTY_TOTAL_LINK = "G02 Total Links";
	private static final String PROPERTY_TOTAL_MSG_RECV = "G03 Total Messages Received";
	private static final String PROPERTY_TOTAL_MSG_SENT = "G04 Total Messages Sent";
	private static final String PROPERTY_GLOBAL_MSG_FLOW_TYPE = "G05 Global Message Flow Type";
	private static final String PROPERTY_GLOBAL_DELAY_TYPE = "G06 Global Delay Type";
	private static final String PROPERTY_GLOBAL_DELAY_SEED = "G07 Global Delay Seed";
	private static final String PROPERTY_PROTOCOL = "G08 Protocol";
	private static final String PROPERTY_MAX_TOKEN = "G09 Max Number of Tokens per Agent";
	private static final String PROPERTY_TOTAL_INIT_AGENT = "G10 Number of Agents at Start";
	private static final String PROPERTY_TOTAL_ALIVE_AGENT = "G11 Current Number of Agents";
    
	private static final String[] propertyArray = {PROPERTY_NAME, PROPERTY_TOTAL_NODE,
		PROPERTY_TOTAL_LINK, PROPERTY_TOTAL_MSG_RECV, PROPERTY_TOTAL_MSG_SENT,
		PROPERTY_GLOBAL_MSG_FLOW_TYPE, PROPERTY_GLOBAL_DELAY_TYPE, PROPERTY_GLOBAL_DELAY_SEED, 
		PROPERTY_PROTOCOL, PROPERTY_MAX_TOKEN, PROPERTY_TOTAL_INIT_AGENT, PROPERTY_TOTAL_ALIVE_AGENT};
	
	private static final int NUM_PROPERTIES = propertyArray.length;
	 
    protected static IPropertyDescriptor[] descriptors;      
    static {
        descriptors = new IPropertyDescriptor[NUM_PROPERTIES];

        descriptors[0] = new PropertyDescriptor(PROPERTY_NAME,
                PROPERTY_NAME);
        descriptors[1] = new PropertyDescriptor(PROPERTY_TOTAL_NODE,
                PROPERTY_TOTAL_NODE);
        descriptors[2] = new PropertyDescriptor(PROPERTY_TOTAL_LINK,
                PROPERTY_TOTAL_LINK);      
        descriptors[3] = new PropertyDescriptor(PROPERTY_TOTAL_MSG_RECV,
                PROPERTY_TOTAL_MSG_RECV);
        descriptors[4] = new PropertyDescriptor(PROPERTY_TOTAL_MSG_SENT,
                PROPERTY_TOTAL_MSG_SENT);
        
        descriptors[5] = new ComboBoxPropertyDescriptor(PROPERTY_GLOBAL_MSG_FLOW_TYPE,
                PROPERTY_GLOBAL_MSG_FLOW_TYPE,
                // NOTE the index order must corresponding to value in IConstance
                new String[] {IConstants.FIFO_TYPE, IConstants.NO_ORDER_TYPE, 
        		IConstants.MIX_ORDER_TYPE});
        
        descriptors[6] = new ComboBoxPropertyDescriptor(PROPERTY_GLOBAL_DELAY_TYPE,
                PROPERTY_GLOBAL_DELAY_TYPE, 
                // NOTE the index order must corresponding to value in IConstance
                new String[] {IConstants.SYNCHRONOUS, IConstants.RANDOM_UNIFORM, 
        		IConstants.RANDOM_POISSON, IConstants.RANDOM_CUSTOMS, 
        		IConstants.MIX_TYPE });
        
        descriptors[7] = new TextPropertyDescriptor(PROPERTY_GLOBAL_DELAY_SEED,
                PROPERTY_GLOBAL_DELAY_SEED);
        ((PropertyDescriptor) descriptors[7])
                .setValidator(NumberCellEditorValidator.instance());
        
        descriptors[8] = new PropertyDescriptor(PROPERTY_PROTOCOL,PROPERTY_PROTOCOL);
        
        descriptors[9] = new TextPropertyDescriptor(PROPERTY_MAX_TOKEN,PROPERTY_MAX_TOKEN);
        ((PropertyDescriptor) descriptors[9])
        		.setValidator(NumberCellEditorValidator.instance());
        
        descriptors[10] = new PropertyDescriptor(PROPERTY_TOTAL_INIT_AGENT, PROPERTY_TOTAL_INIT_AGENT);
        
        descriptors[11] = new PropertyDescriptor(PROPERTY_TOTAL_ALIVE_AGENT, PROPERTY_TOTAL_ALIVE_AGENT);
    }
    
    private String graphId;
    
    transient private Shell shell;
    
    private Graph graph;

    private Map<Integer, RGB> stateColors;  
    
    private List<NodeElement> nodeElements;

    private List<LinkElement> linkElements;
    
    //transient private IUpdateListener viewListener;
    
    /**
     * TODO it can create a graph with a given partName
     *
     */
    public GraphElement() {
        this.graph = GraphFactory.createGraph();
        this.graphId = "";
        this.stateColors = new HashMap<Integer, RGB>();
        this.nodeElements = new ArrayList<NodeElement>();
        this.linkElements = new ArrayList<LinkElement> ();
    }
    
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		super.addPropertyChangeListener(listener);
		this.graph.addPropertyChangeListener(listener);
	}
	
    /**
     * @return
     */
    public String getGraphId(){
        return this.graphId;
    }
    
    public void setGraphId(String partName){
        try {        	
        	this.graph.setId(partName);
			this.graphId = partName;
        	GraphFactory.addGraph(this.graph);
		} catch (Exception e) {
			System.err.println("Error add graph into map "+ e);
		}
//        System.err.println("[GraphElement] setGraphId " + this.graph + " <" + this.graph.getId() + ">");
    }
  
    public Graph getGraph(){   	
        return this.graph;
    }
        
    public Shell getShell(){
        return this.shell;
    }
    
    public void setShell(Shell shell){
        this.shell = shell;
    }
    
    public void copyGraphElement(){       
        for(int i =0; i < this.nodeElements.size(); i++){
            NodeElement e = this.nodeElements.get(i);
            e.copyNode();     
        }       
        for(int i =0; i < this.linkElements.size(); i++){
            LinkElement e = this.linkElements.get(i);
            e.copyEdge();
        }
    }
    
    public void resetGraphElement(){ 
    	for (int i = 0; i <  NUM_PROPERTIES; i++) {
			this.resetPropertyValue(propertyArray[i]);
		}
        for(int i =0; i < this.nodeElements.size(); i++){
            NodeElement e = this.nodeElements.get(i);
            e.resetNode();          
        }      
        for(int i =0; i < this.linkElements.size(); i++){
            LinkElement e = this.linkElements.get(i);
            e.resetEdge();
        }
        
    }
    
    public void addNode(String id, final NodeElement element) {
        try {
            this.graph.addNode(id, element.getNode());
            this.nodeElements.add(element);
            GraphFactory.addGraph(this.graph);
         
            // set maxX and maxY location
            this.setMaxXandY();
            
            // update DisJView display
            firePropertyChange(IConstants.PROPERTY_CHANGE_ADD_NODE, null, element.getNode());
            
        } catch (Exception ignore) {
            System.err.println("[GraphElement] "+ignore);
        }
    }

    /*
     * Setting current Max X and Max Y location of Node(s)
     * in a graph
     */
	private void setMaxXandY() {
		// update maxX and maxY
		NodeElement node;
		int maxX = 0;
		int maxY = 0;
		Point p;
		for (int i = 0; i < this.nodeElements.size(); i++) { 
			node = this.nodeElements.get(i);            	
			p = node.getLocation();
			
			// find max
		    if (maxX < p.x){
		    	maxX = p.x;
		    }
		    if(maxY < p.y){
		    	maxY = p.y;
		    }
		}      
		// set max
		for (int i = 0; i < this.nodeElements.size(); i++) { 
			node = this.nodeElements.get(i);
			node.setMaxX(maxX);
			node.setMaxY(maxY);
		}
	}

    public void addEdge(String id, final LinkElement element) {
        try {
            this.graph.addEdge(id, element.getEdge());
            this.linkElements.add(element);
            GraphFactory.addGraph(this.graph);
        } catch (Exception ignore) {
            System.err.println("[GraphElement] "+ignore);
        }
    }
    
    public void addStateColor(Map<Integer, RGB> stateMap){
        this.stateColors.putAll(stateMap);		
    }

    public void removeNode(String id, final NodeElement element) {
        try {
            this.graph.removeNode(id);
            this.nodeElements.remove(element);
            GraphFactory.addGraph(this.graph);
            
            // set maxX and maxY location
            this.setMaxXandY();           
            
            // update DisJView display
            firePropertyChange(IConstants.PROPERTY_CHANGE_REM_NODE, null, element.getNode());
                                 
        } catch (Exception ignore) {
            System.err.println("[GraphElement] "+ignore);
        }
    }

    public void removeEdge(String id, final LinkElement element) {
        try {
            this.graph.removeEdge(id);
            this.linkElements.remove(element);
            GraphFactory.addGraph(this.graph);
        } catch (Exception ignore) {
            System.err.println("[GraphElement] "+ignore);
        }
    }
    
    public void removeStateColor(Integer state){      
       this.stateColors.remove(state);
    }
    
    public void removeAllStateColor(){       
       this.stateColors.clear();
    }

    public List<NodeElement> getNodeElements() {
        return this.nodeElements;
    }

    public List<LinkElement> getLinkElements() {
        return this.linkElements;
    }
    
    public Map<Integer, RGB> getStateColors(){
        return this.stateColors;
    }
    
    public Color getColor(Integer state){
        RGB rgb = this.stateColors.get(state);
        if(rgb != null){
            return  new Color(this.shell.getDisplay(), rgb);
        } else {
    		// user default color if state has no color given
    		return IGraphEditorConstants.DEFAULT_NODE_COLOR;
        }
    }
    
    public int getNumStateColor(){
        return this.stateColors.size();
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
    	// get a graph with a latest updated
    	this.graph = GraphFactory.getGraph(this.graphId);
    	
        if (propName.equals(PROPERTY_NAME)) {
            return this.getGraphId();
            
        } else if (propName.equals(PROPERTY_TOTAL_NODE)) {
            return this.nodeElements.size();
            
        } else if (propName.equals(PROPERTY_TOTAL_LINK)) {
            return this.linkElements.size();
            
        } else if (propName.equals(PROPERTY_TOTAL_MSG_RECV)) {           
        	int sum = GraphStat.getTotalMsgRecv(this.graph.getNodes());           
            return sum;
            
        } else if (propName.equals(PROPERTY_TOTAL_MSG_SENT)) {
        	int sum = GraphStat.getTotalMsgSent(this.graph.getNodes());           
            return sum;
            
        } else if (propName.equals(PROPERTY_GLOBAL_MSG_FLOW_TYPE)) {
            return this.graph.getGlobalFlowType();
           
        } else if (propName.equals(PROPERTY_GLOBAL_DELAY_TYPE)) {
            return this.graph.getGlobalDelayType();
           
        } else if (propName.equals(PROPERTY_GLOBAL_DELAY_SEED)) {
            return "" + this.graph.getGlobalDelaySeed();
            
        } else if (propName.equals(PROPERTY_PROTOCOL)) {
            return this.graph.getProtocol();
            
        } else if (propName.equals(PROPERTY_MAX_TOKEN)) {
            return "" + this.graph.getMaxToken();
            
        } else if (propName.equals(PROPERTY_TOTAL_INIT_AGENT)) {
        	int count = graph.getStat().getTotalAgent();
        	
        	// process hasn't started yet
        	if(count == 0){       		
        		// then read from GUI
        		count = this.getTotalInitAgent();
        	}
            return "" + count;
            
        } else if (propName.equals(PROPERTY_TOTAL_ALIVE_AGENT)) {        	
            return "" + graph.getAgents().size();
            
        } else {
            return  propName;
        }
    }
    
    /*
     * Get total of number of init agent from node that receives directly
     * from user input at GUI properties view
     */
    private int getTotalInitAgent(){
    	Map<String, Node> map = this.graph.getNodes();
    	int count = 0;
    	for (String id : map.keySet()) {
			Node n = map.get(id);
			count += n.getNumInitAgent();
		}
    	return count;
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
    	   	
        if(id.equals(PROPERTY_PROTOCOL)){
        	// nothing to set here
        	this.graph.setProtocol((String)value);
        	
        } else if (id.equals(PROPERTY_GLOBAL_MSG_FLOW_TYPE)){
      		int globalType = ((Integer)value).intValue();        	
        	this.graph.setGlobalFlowType(globalType); 
        	
        	// set local to be the same except Mix
        	if (globalType == IConstants.MSGDELAY_GLOBAL_MIX){
        		// Mix flow	
        		
        	} else {
    			Map<String, Edge> edges = this.graph.getEdges();
	        	for (String label : edges.keySet()) {
	        		Edge ed = edges.get(label);             
	        		ed.setMsgFlowType(globalType);
				}
    		}
        } else if (id.equals(PROPERTY_GLOBAL_DELAY_TYPE)){
      		int globalType = ((Integer)value).intValue();
      		this.graph.setGlobalDelayType(globalType);
      	
        	// get corresponding local type with new global type
      		int type = this.mapGlobalDelayType(globalType);
        	if(type > -1){
	    		Map<String, Edge> edges = this.graph.getEdges();
	        	for (String label : edges.keySet()) {
	        		Edge ed = edges.get(label);          
	        		ed.setDelayType(type);
				}
        	}
        } else if (id.equals(PROPERTY_GLOBAL_DELAY_SEED)) {
        	int val;
        	boolean flag = true;
        	try{
        		val = Integer.parseInt(value.toString());
        		
        	}catch(NumberFormatException e){
        		val = IConstants.MSGDELAY_SEED_DEFAULT;
        	}
        	if(val == 0){
        		// Mix order
        		// display 0 in global and change nothing at local
        		flag = false;
        		
        	}else if(val > 255 || val < 1){
        		// bad input from user, set to be default value
        		val = IConstants.MSGDELAY_SEED_DEFAULT;            		
        		
        	} else {        		
        		// valid input for global value       		
        	}
        	
        	// set value to graph record
        	this.graph.setGlobalDelaySeed(val);
        	if(flag == true){
        		Map<String, Edge> edges = this.graph.getEdges();
        		for (String label : edges.keySet()) {
        			Edge ed = (Edge)edges.get(label);
            		ed.setDelaySeed(val);
				}
        	}       	
        } else if (id.equals(PROPERTY_MAX_TOKEN)) {
        	// allow to set max token only before the start
        	// of the simulation
        	if(this.graph.getAgents().isEmpty()){
        		int val;
	        	try{
	        		Integer i = Integer.valueOf(value.toString());
	        		val = i.intValue();
	        		
	        	}catch(NumberFormatException e){
	        		val = IConstants.DEFAULT_MAX_NUM_TOKEN;
	        	}
	        	if(val < 1){
	        		// default value
	        		val = IConstants.DEFAULT_MAX_NUM_TOKEN;       		
	        	}
	        	this.graph.setMaxToken(val);
        	}
        } else {
        	// unknown prop do nothing
            return;
        }
        
        try {
        	// store new updated
			GraphFactory.addGraph(this.graph);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    

    public void resetPropertyValue(Object propName) {
   	/*
    	if (propName.equals("PROPERTY_GLOBAL_DELAY_SEED")) {
			this.graph.setGlobalDelaySeed(0);

		} else if (propName.equals("PROPERTY_GLOBAL_DELAY_TYPE")) {
			this.graph.setGlobalDelayType(IConstants.MSGDELAY_GLOBAL_SYNCHRONOUS);

		} else if (propName.equals("PROPERTY_PROTOCOL")) {
			this.graph.setProtocol("");

		} else if (propName.equals(PROPERTY_TOTAL_MSG_SENT)) {
			this.graph.resetMsgSentCounter();

		} else if (propName.equals(PROPERTY_TOTAL_MSG_RECV)) {
			this.graph.resetMsgRecvCounter();
		}
		
    	try {
    		// store new updated
 			GraphFactory.addGraph(this.graph);
 		} catch (DisJException e) {
 			e.printStackTrace();
 		} 
 		*/      
    }
    

    /*
     * 
     */
    private final short mapGlobalDelayType(int type) {   	
    	if (type == IConstants.MSGDELAY_GLOBAL_SYNCHRONOUS){
    		return IConstants.MSGDELAY_LOCAL_FIXED;
    		
    	} else if (type == IConstants.MSGDELAY_GLOBAL_RANDOM_UNIFORM){
    		return IConstants.MSGDELAY_LOCAL_RANDOM_UNIFORM;
    		
    	} else if (type == IConstants.MSGDELAY_GLOBAL_RANDOM_POISSON){
    		return IConstants.MSGDELAY_LOCAL_RANDOM_POISSON;
    		
    	} else if (type == IConstants.MSGDELAY_GLOBAL_RANDOM_CUSTOMS){
    		return IConstants.MSGDELAY_LOCAL_RANDOM_CUSTOMS;
    		
    	} else {
    		return -1;
    	}
    }
  
    /*
     * Overriding serialize object due to Java Bug4152790
     */
    private void writeObject(ObjectOutputStream os) throws IOException{
		os.defaultWriteObject();
	}
    /*
     * Overriding serialize object due to Java Bug4152790
     */
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException  {
    	// rebuild this object
    	 ois.defaultReadObject();

    	 // rebuild transient instances in Node/Link Element objects	
		try {
			String nId;
			Node n = null;
			for (NodeElement ne : this.nodeElements) {
				nId = ne.getNodeId();
				n = this.graph.getNode(nId);
				ne.setNode(n);
			}
			String eId;
			Edge ed = null;
			for (LinkElement le : this.linkElements) {
				eId = le.getEdgeId();
				ed = this.graph.getEdge(eId);
				le.setEdge(ed);
				
				// rebuild source/target of each LinkElement
				String sId = le.getSourceId();
				le.setSource(this.getNodeElement(sId));

				String tId = le.getTargetId();
				le.setTarget(this.getNodeElement(tId));
			}
			
			
			
		} catch (Exception e) {
			System.err.println("@GraphElement.readObject() " + e);
		}    	
		
    	 //System.out.println(this.graph);
    }

    private NodeElement getNodeElement(String nId) throws DisJException{
    	for (NodeElement ne : this.nodeElements) {
			if(ne.getNodeId().equals(nId)){
				return ne;
			}
		}   	
    	throw new DisJException(IConstants.ERROR_0, nId);
    }

}
