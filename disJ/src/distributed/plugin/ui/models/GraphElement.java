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
	private static final String PROPERTY_TOTAL_NODE = "G01 Total Node";
	private static final String PROPERTY_TOTAL_LINK = "G02 Total Link";
	private static final String PROPERTY_TOTAL_MSG_RECV = "G03 Total Messages Received";
	private static final String PROPERTY_TOTAL_MSG_SENT = "G04 Total Messages Sent";
	private static final String PROPERTY_GLOBAL_MSG_FLOW_TYPE = "G05 Global Message Flow Type";
	private static final String PROPERTY_GLOBAL_DELAY_TYPE = "G06 Global Delay Type";
	private static final String PROPERTY_GLOBAL_DELAY_SEED = "G07 Global Delay Seed";
	private static final String PROPERTY_PROTOCOL = "G08 Protocol";
	private static final String PROPERTY_MAX_TOKEN = "G09 Maximum Token Agent can Carry";
	private static final String PROPERTY_TOTAL_AGENT = "G10 Total Agent in Network";
    
	private static final String[] propertyArray = {PROPERTY_NAME, PROPERTY_TOTAL_NODE,
		PROPERTY_TOTAL_LINK, PROPERTY_TOTAL_MSG_RECV, PROPERTY_TOTAL_MSG_SENT,
		PROPERTY_GLOBAL_MSG_FLOW_TYPE, PROPERTY_GLOBAL_DELAY_TYPE, PROPERTY_GLOBAL_DELAY_SEED, 
		PROPERTY_PROTOCOL, PROPERTY_MAX_TOKEN, PROPERTY_TOTAL_AGENT};
	
	private static final int NUM_PROPERTIES = propertyArray.length;
	
    // message delay time supported types
	private static final String SYNCHRONOUS = "Synchronous";   
	private static final String CUSTOMS = "Customs";
	private static final String RANDOM_UNIFORM = "Random Uniform";
	private static final String RANDOM_POISSON = "Random Poisson";
	private static final String RANDOM_CUSTOMS = "Random Customs";
	
	private static final String FIFO_TYPE = "FIFO";
	private static final String NO_ORDER_TYPE = "No Order";
	private static final String MIX_TYPE = "Mix Order";
    
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
                // FIXME the index order must corresponding to value in IConstance
                new String[] {FIFO_TYPE, NO_ORDER_TYPE, MIX_TYPE});
        
        descriptors[6] = new ComboBoxPropertyDescriptor(PROPERTY_GLOBAL_DELAY_TYPE,
                PROPERTY_GLOBAL_DELAY_TYPE, 
                // FIXME the index order must corresponding to value in IConstance
                new String[] {SYNCHRONOUS, RANDOM_UNIFORM, RANDOM_POISSON, RANDOM_CUSTOMS, CUSTOMS });
        
        descriptors[7] = new TextPropertyDescriptor(PROPERTY_GLOBAL_DELAY_SEED,
                PROPERTY_GLOBAL_DELAY_SEED);
        ((PropertyDescriptor) descriptors[7])
                .setValidator(NumberCellEditorValidator.instance());
        
        descriptors[8] = new PropertyDescriptor(PROPERTY_PROTOCOL,PROPERTY_PROTOCOL);
        
        descriptors[9] = new TextPropertyDescriptor(PROPERTY_MAX_TOKEN,PROPERTY_MAX_TOKEN);
        ((PropertyDescriptor) descriptors[9])
        		.setValidator(NumberCellEditorValidator.instance());
        
        descriptors[10] = new PropertyDescriptor(PROPERTY_TOTAL_AGENT,PROPERTY_TOTAL_AGENT);
    }
    
    private String graphId;
    
    transient private Shell shell;
    
    private Graph graph;

    private Map<Short, RGB> stateColors;
    
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
        this.stateColors = new HashMap<Short, RGB>();
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
		} catch (DisJException e) {
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
            firePropertyChange(IConstants.PROPERTY_CHANGE_NODE, null, element);
            GraphFactory.addGraph(this.graph);
        } catch (DisJException ignore) {
            System.err.println("[GraphElement] "+ignore);
        }
    }

    public void addEdge(String id, final LinkElement element) {
        try {
            this.graph.addEdge(id, element.getEdge());
            this.linkElements.add(element);
            GraphFactory.addGraph(this.graph);
        } catch (DisJException ignore) {
            System.err.println("[GraphElement] "+ignore);
        }
    }
    
    public void addStateColor(Map<Short, RGB> stateMap){
        this.stateColors.putAll(stateMap);		
    }
    
    public void setStateColor(Map<Short, RGB> stateMap){
       this.stateColors = stateMap;
    }

    public void removeNode(String id, final NodeElement element) {
        try {
            this.graph.removeNode(id);
            this.nodeElements.remove(element);
            firePropertyChange(IConstants.PROPERTY_CHANGE_NODE, element, null);
            GraphFactory.addGraph(this.graph);
        } catch (DisJException ignore) {
            System.err.println("[GraphElement] "+ignore);
        }
    }

    public void removeEdge(String id, final LinkElement element) {
        try {
            this.graph.removeEdge(id);
            this.linkElements.remove(element);
            GraphFactory.addGraph(this.graph);
        } catch (DisJException ignore) {
            System.err.println("[GraphElement] "+ignore);
        }
    }
    
    public void removeStateColor(Short state){      
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
    
    public Map<Short, RGB> getStateColors(){
        return this.stateColors;
    }
    
    public Color getColor(Short state){
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
        	int sum = this.graph.getStat().getTotalMsgRecv(this.graph.getNodes());           
            return sum;
            
        } else if (propName.equals(PROPERTY_TOTAL_MSG_SENT)) {
        	int sum = this.graph.getStat().getTotalMsgSent(this.graph.getNodes());           
            return sum;
            
        } else if (propName.equals(PROPERTY_GLOBAL_MSG_FLOW_TYPE)) {
            return this.mapGlobalFlowType(this.graph.getGlobalFlowType());
           
        } else if (propName.equals(PROPERTY_GLOBAL_DELAY_TYPE)) {
            return this.mapGlobalDelayType(this.graph.getGlobalDelayType());
           
        } else if (propName.equals(PROPERTY_GLOBAL_DELAY_SEED)) {
            return "" + this.graph.getGlobalDelaySeed();
            
        } else if (propName.equals(PROPERTY_PROTOCOL)) {
            return this.graph.getProtocol();
            
        } else if (propName.equals(PROPERTY_MAX_TOKEN)) {
            return "" + this.graph.getMaxToken();
            
        } else if (propName.equals(PROPERTY_TOTAL_AGENT)) {
            return "" + graph.getAgents().size();
            
        } else {
            return  propName;
        }
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
      		int local_type;
        	int global_type = this.mapGlobalFlowType(((Integer)value).intValue());
        	
        	this.graph.setGlobalFlowType(global_type); 
        	if (global_type == IConstants.MSGDELAY_GLOBAL_CUSTOMS){
        		// no specific setting for local       		
        		
        	}else {
    			local_type = global_type;
    			Map<String, Edge> edges = this.graph.getEdges();
	        	for (String label : edges.keySet()) {
	        		Edge ed = edges.get(label);             
	        		ed.setMsgFlowType(local_type);
				}
    		}
        } else if (id.equals(PROPERTY_GLOBAL_DELAY_TYPE)){
      		int local_type;
      		int temp = ((Integer)value).intValue();
        	int global_type = this.mapGlobalDelayType(temp);
        	
        	this.graph.setGlobalDelayType(global_type); 
        	if (global_type == IConstants.MSGDELAY_GLOBAL_CUSTOMS){
        		// no specific setting for local
        		local_type = -9;
        		
        	}else if(global_type == IConstants.MSGDELAY_GLOBAL_RANDOM_CUSTOMS){
    			local_type = IConstants.MSGDELAY_LOCAL_RANDOM_CUSTOMS;
    		}
    		else if(global_type == IConstants.MSGDELAY_GLOBAL_RANDOM_UNIFORM){
    			local_type = IConstants.MSGDELAY_LOCAL_RANDOM_UNIFORM;
    		}
    		else if(global_type == IConstants.MSGDELAY_GLOBAL_RANDOM_POISSON){
    			local_type = IConstants.MSGDELAY_LOCAL_RANDOM_POISSON;
    		}
    		else{
    			local_type = IConstants.MSGDELAY_LOCAL_FIXED;
    		}
        	if(local_type != -9){
	    		Map<String, Edge> edges = this.graph.getEdges();
	        	for (String label : edges.keySet()) {
	        		Edge ed = edges.get(label);
	        		ed.setDelaySeed(this.graph.getGlobalDelaySeed());              
	        		ed.setDelayType(local_type);
				}
        	}
        } else if (id.equals(PROPERTY_GLOBAL_DELAY_SEED)) {
        	int val;
        	try{
        		Integer i = Integer.valueOf(value.toString());
        		val = i.intValue();
        		
        	}catch(NumberFormatException e){
        		val = IConstants.DEFAULT_MSGDELAY_SEED;
        	}
        	if(val > 255 || val < 1){
        		// default value
        		val = IConstants.DEFAULT_MSGDELAY_SEED;       		
        	} else {
        		this.graph.setGlobalDelaySeed(val);
        		
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
		} catch (DisJException e) {
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
    

    private final int mapGlobalDelayType(int type) {   	
    	if (type == 0){
    		return IConstants.MSGDELAY_GLOBAL_SYNCHRONOUS;
    		
    	} else if (type == 1){
    		return IConstants.MSGDELAY_GLOBAL_RANDOM_UNIFORM;
    		
    	} else if (type == 2){
    		return IConstants.MSGDELAY_GLOBAL_RANDOM_POISSON;
    		
    	} else if (type == 3){
    		return IConstants.MSGDELAY_GLOBAL_RANDOM_CUSTOMS;
    		
    	}else{
    		return IConstants.MSGDELAY_GLOBAL_CUSTOMS;
    	}
    }
    
    private final int mapGlobalFlowType(int type) {
        if(type == IConstants.MSGFLOW_FIFO_TYPE){
        	return IConstants.MSGFLOW_FIFO_TYPE;
        	
        } else if(type == IConstants.MSGFLOW_NO_ORDER_TYPE){
            return IConstants.MSGFLOW_NO_ORDER_TYPE;
            
        }else{
        	 return IConstants.MSGFLOW_MIX_TYPE;
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
