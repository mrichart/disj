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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.Bendpoint;
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
import distributed.plugin.ui.validators.EmptyCellEditorValidator;
import distributed.plugin.ui.validators.NumberCellEditorValidator;
import distributed.plugin.ui.validators.PercentageCellEditorValidator;

/**
 * @author Me
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public abstract class LinkElement extends AdapterElement {

	static final long serialVersionUID = IConstants.SERIALIZE_VERSION;
		
	private static final String PROPERTY_DIRECTION_TYPE = "L00 Type of Direction";   
	private static final String PROPERTY_START_PORT = "L01 Source Port Name";   
	private static final String PROPERTY_END_PORT = "L02 Target Port Name";
	private static final String PROPERTY_MSG_FLOW_TYPE = "L03 Message Flow Type";
	private static final String PROPERTY_DELAY_TYPE = "L04 Delay Type";   
	private static final String PROPERTY_DELAY_SEED = "L05 Delay Seed";   
	private static final String PROPERTY_RELIABLE = "L06 Reliable";   
	private static final String PROPERTY_PROB_FAILURE = "L07 Probability of Failure";
	private static final String PROPERTY_TOTAL_MSG = "L08 Total Traffic";

	private static final String[] propertyArray = {PROPERTY_DIRECTION_TYPE, 
		PROPERTY_START_PORT, PROPERTY_END_PORT, PROPERTY_MSG_FLOW_TYPE, 
		PROPERTY_DELAY_TYPE, PROPERTY_DELAY_SEED, PROPERTY_RELIABLE,
		PROPERTY_PROB_FAILURE,PROPERTY_TOTAL_MSG};

	private static final int NUM_PROPERTIES = propertyArray.length;

    // message flow supported types
    protected static final String FIFO_TYPE = "FIFO";
    protected static final String NO_ORDER_TYPE = "No Order";

    // message delay time supported types
    private static final String FIXED = "Fixed";
    private static final String RANDOM_UNIFORM = "Random Uniform";
    private static final String RANDOM_POISSON = "Random Poisson";
    private static final String RANDOM_CUSTOMS = "Random Customs";
    
    
    //private Map hmProperties = new HashMap();
    
    protected static IPropertyDescriptor[] descriptors;
    static {
        descriptors = new IPropertyDescriptor[NUM_PROPERTIES];

        descriptors[0] = new PropertyDescriptor(
                PROPERTY_DIRECTION_TYPE, PROPERTY_DIRECTION_TYPE);
        
        descriptors[1] = new ComboBoxPropertyDescriptor(PROPERTY_RELIABLE,
                PROPERTY_RELIABLE, new String[] { "False", "True" });
        
        descriptors[2] = new ComboBoxPropertyDescriptor(PROPERTY_MSG_FLOW_TYPE,
                PROPERTY_MSG_FLOW_TYPE,
                // FIXME the index order must corresponding to value in IConstance 
                new String[] {FIFO_TYPE, NO_ORDER_TYPE});
        
        descriptors[3] = new ComboBoxPropertyDescriptor(PROPERTY_DELAY_TYPE,
                //FIXME the index order must corresponding to value in IConstance 
                PROPERTY_DELAY_TYPE, new String[] { FIXED,
                        RANDOM_UNIFORM, RANDOM_POISSON, RANDOM_CUSTOMS });

        descriptors[4] = new PropertyDescriptor(PROPERTY_TOTAL_MSG,
                PROPERTY_TOTAL_MSG);

        descriptors[5] = new TextPropertyDescriptor(PROPERTY_DELAY_SEED,
                PROPERTY_DELAY_SEED);
        ((PropertyDescriptor) descriptors[5])
                .setValidator(NumberCellEditorValidator.instance());

        descriptors[6] = new TextPropertyDescriptor(PROPERTY_START_PORT,
                PROPERTY_START_PORT);
        ((PropertyDescriptor) descriptors[6])
                .setValidator(EmptyCellEditorValidator.instance());

        descriptors[7] = new TextPropertyDescriptor(PROPERTY_END_PORT,
                PROPERTY_END_PORT);
        ((PropertyDescriptor) descriptors[7])
                .setValidator(EmptyCellEditorValidator.instance());
        
        descriptors[8] = new TextPropertyDescriptor(PROPERTY_PROB_FAILURE,
                PROPERTY_PROB_FAILURE);
        ((PropertyDescriptor) descriptors[8])
                .setValidator(PercentageCellEditorValidator.instance());

    }

    private String name;

    private String sId;

    private String tId;

    private String eId;
    
    transient private Edge edge;
    
    transient private NodeElement source, target;

    protected List<Bendpoint> bendpoints;

    //private Edge orgEdge;

    /**
     * Constructor
     */
    protected LinkElement(String graphId, String eId, short direction) {
        try {
            this.sId = null;
            this.tId = null;
            this.eId = eId;
        	this.edge = new Edge(graphId, eId, direction);
            this.bendpoints = new ArrayList<Bendpoint>();
            //this.edge.setLinkElement(this);
            
        } catch (DisJException ignore) {
            System.err.println("@LinkELement.constructor " + ignore);
        }
    }

    /**
     * Get a type of direction flow of this link
     * 
     * @return String
     */
    public abstract String getType();

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
       if (propName.equals(PROPERTY_RELIABLE)) {
            int i = 0;
            if (this.edge.isReliable())
                i = 1;
            return new Integer(i);

        } else if (propName.equals(PROPERTY_DIRECTION_TYPE)) {
            String direction = IGraphEditorConstants.UNI;
            if(this.edge.getDirection() == IConstants.DIRECTION_BI)
                direction = IGraphEditorConstants.BI;
            return direction;

        } else if (propName.equals(PROPERTY_MSG_FLOW_TYPE)) {
            return this.mapFlowType(this.edge.getMsgFlowType());

        } else if (propName.equals(PROPERTY_DELAY_TYPE)) {
            return new Integer(this.mapDelayType(this.edge.getDelayType()));

        } else if (propName.equals(PROPERTY_TOTAL_MSG)) {
            return "" + this.edge.getNumMsgEnter();

        } else if (propName.equals(PROPERTY_DELAY_SEED)) {
            return "" + this.edge.getDelaySeed();

        } else if (propName.equals(PROPERTY_START_PORT)) {
            Object val = "unknown";
            try {
                val = this.edge.getStart().getPortLabel(this.edge);
            } catch (DisJException ignore) {
                System.err.println("[LinkElement].getPropertyValue " + ignore);
            }
            return val;

        } else if (propName.equals(PROPERTY_END_PORT)) {
            Object val = "unknown";
            try {
                val = this.edge.getEnd().getPortLabel(this.edge);
            } catch (DisJException ignore) {
                System.err.println("[LinkElement].getPropertyValue " + ignore);
            }
            return val;

        } else if (propName.equals(PROPERTY_PROB_FAILURE)) {        
              int  val = this.edge.getProbOfFailure();          
            return val+"";

        } else {
            return "unknown property id";
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

        if (id.equals(PROPERTY_RELIABLE)) {
            if (value instanceof Integer) {
                boolean bool = false;
                if (((Integer) value).intValue() == 1){
                    bool = true;
                    // since it is total reliability
                    this.edge.setProbOfFailure(0);
                }
                this.edge.setReliable(bool);
            }
        } else if (id.equals(PROPERTY_PROB_FAILURE)) {
            int val = Integer.parseInt((String)value);
            if(val < 0 || val > 100){
            	// default value
            	 this.edge.setProbOfFailure(IConstants.MSGFAILURE_DEFAULT_PROB);
            	 if(val == 0){
            		 this.edge.setReliable(true);
            	 }
            }else{
            	this.edge.setProbOfFailure(val);
            }

        } else if (id.equals(PROPERTY_MSG_FLOW_TYPE)) {
        	if (value instanceof Integer) {
        		int val = ((Integer)value).intValue();
        		this.edge.setMsgFlowType(this.mapDelayType(val));         		 
        		try {
        			 // since some links have been modified to be different
        			 // so overall of the graph should be mixed
                	 Graph g = GraphFactory.getGraph(this.edge.getGraphId());
                     g.setGlobalFlowType(IConstants.MSGFLOW_MIX_TYPE);
    				 GraphFactory.addGraph(g);    				 
    			} catch (DisJException e) {
    				 e.printStackTrace();
    			}
        	}
        } else if (id.equals(PROPERTY_DELAY_TYPE)) {
        	if (value instanceof Integer) {
        		int val = ((Integer)value).intValue();
        		this.edge.setDelayType(this.mapDelayType(val));         		 
        		try {
        			 // since some links have been modified to be different
        			 // so overall of the graph should be customs
                	 Graph g = GraphFactory.getGraph(this.edge.getGraphId());
                     g.setGlobalDelayType(IConstants.MSGDELAY_GLOBAL_CUSTOMS);
    				 GraphFactory.addGraph(g);    				 
    			} catch (DisJException e) {
    				 e.printStackTrace();
    			}
        	}
        } else if (id.equals(PROPERTY_DELAY_SEED)) {
        	int val = ((Integer)value).intValue();
        	if(val > 255 || val < 1){
        		// default value
        		this.edge.setDelaySeed(IConstants.DEFAULT_MSGDELAY_SEED);
        	} else {
        		this.edge.setDelaySeed(val);
        	}           
            try {
            	Graph g = GraphFactory.getGraph(this.edge.getGraphId());
                g.setGlobalDelayType(IConstants.MSGDELAY_GLOBAL_CUSTOMS);
				GraphFactory.addGraph(g);
			} catch (DisJException e) {
				e.printStackTrace();
			}
            
        } else if (id.equals(PROPERTY_START_PORT)) {
            try {
            	Node n = this.edge.getStart();
            	String oldValue = n.getPortLabel(this.edge);
            	
            	// replace old key with new key
            	Map map = n.getBlockPort();
            	Object obj = map.remove(oldValue);
                map.put(value, obj);
                
                map = n.getEdges();
                obj = map.remove(oldValue);
                map.put(value, obj);
                
                map = n.getPorts();
                obj = map.remove(oldValue);
                map.put(value, obj); 
                
            	n.setPortLable((String) value, this.edge);
            	
            } catch (DisJException ignore) {
                System.err.println("[LinkElement].setPropertyValue " + ignore);
            }
            
        } else if (id.equals(PROPERTY_END_PORT)) {
            try {              	
                Node n = this.edge.getEnd();
            	String oldValue = n.getPortLabel(this.edge);
            	Map map = n.getBlockPort();
            	Object obj = map.remove(oldValue);
                map.put(value, obj);
                
                map = n.getEdges();
                obj = map.remove(oldValue);
                map.put(value, obj);
                
                map = n.getPorts();
                obj = map.remove(oldValue);
                map.put(value, obj);  
                
            	n.setPortLable((String) value, this.edge);
            	
            } catch (DisJException ignore) {
                System.err.println("[LinkElement].setPropertyValue " + ignore);
            }
        } else {
            return;
        }
    }
    
    public void resetPropertyValue(Object propName){

    	/*
        if (propName.equals(PROPERTY_RELIABLE)) {
        	this.edge.setReliable(this.orgEdge.isReliable());
                
        } else if (propName.equals(PROPERTY_MSG_FLOW_TYPE)) {
            this.edge.setMsgFlowType(this.orgEdge.getMsgFlowType());

        } else if (propName.equals(PROPERTY_DELAY_TYPE)) {
            this.edge.setDelayType(this.orgEdge.getDelayType());

        } else if (propName.equals(PROPERTY_DELAY_SEED)) {
            this.edge.setDelaySeed(this.orgEdge.getDelaySeed());

        } else if (propName.equals(PROPERTY_START_PORT)) {
            try {
                this.edge.getStart().setPortLable(this.orgEdge.getStart().getPortLabel(this.orgEdge), this.edge);
            
            } catch (DisJException ignore) {
                System.err.println("[LinkElement].setPropertyValue " + ignore);
            }
        } else if (propName.equals(PROPERTY_END_PORT)) {
            try {
                this.edge.getEnd().setPortLable(this.orgEdge.getEnd().getPortLabel(this.orgEdge), this.edge);
           
            } catch (DisJException ignore) {
                System.err.println("[LinkElement].setPropertyValue " + ignore);
            }
        } else
         if (propName.equals(PROPERTY_TOTAL_MSG)) {
            this.edge.resetNumMsg();

        } else {
            return;
        }   
        */
    }

    /**
     * Get an edge belong to this link element
     * 
     * @return Edge
     */
    public Edge getEdge() {
        return this.edge;
    }
    
    /**
     * Copy the origin state of this edge
     */
    public void copyEdge(){
//        try {
//            this.orgEdge = (Edge)GraphLoader.deepClone(this.edge);
//        } catch (DisJException e) {
//            System.err.println(e);
//        } catch (IOException e) {
//            System.err.println(e);
//        }
    }

    /**
     * Set all states of this edge back to where is was,
     * when method <code>copyEdge</code> is called last.
     */
    public void resetEdge(){
//        for(int i =0; i < descriptors.length; i++){
//            String prop = (String)descriptors[i].getId();
//            this.resetPropertyValue(PROPERTY_TOTAL_MSG);
//        }
    	for (int i = 0; i <  NUM_PROPERTIES; i++) {
			this.resetPropertyValue(propertyArray[i]);
		}
    	 this.setVisible(true); // make every edge visible
    }
    
    public void setEdge(Edge edge) {
        this.edge = edge;
    }
    
    public String getEdgeId(){
        return this.eId;
    }
    
    public abstract void attachSource();

    public abstract void attachTarget();

    public void detachSource() {
        if (source == null){
            return;
        }
        source.disconnectOutLink(this);
    }

    public void detachTarget() {
        if (target == null){
            return;
        }
        target.disconnectInLink(this);
    }

    private final int mapFlowType(int type) {
    	if(type == IConstants.MSGFLOW_FIFO_TYPE){
    		return type; // FIFO
    	}else if(type == IConstants.MSGFLOW_NO_ORDER_TYPE){
    		return IConstants.MSGFLOW_NO_ORDER_TYPE;
    	}else{
    		return IConstants.MSGFLOW_MIX_TYPE;
    	}
}

    private final int mapDelayType(int type) {
    	if (type == 0){
    		return IConstants.MSGDELAY_LOCAL_FIXED;
    	} else if (type == 1){
    		return IConstants.MSGDELAY_LOCAL_RANDOM_UNIFORM;
    	} else if (type == 2){
    		return IConstants.MSGDELAY_LOCAL_RANDOM_POISSON;
    	}else{
    		return IConstants.MSGDELAY_LOCAL_RANDOM_CUSTOMS;
    	}
    	
    }

 /*   private final short mapDelayType(Object type) {
        if (type instanceof Integer)
            return (short) ((Integer) type).intValue();
        else
            return (short) -9;
    }*/

    /**
     * @return Returns the start.
     */
    public NodeElement getSource() {
        return source;
    }

    /**
     * @return Returns the end.
     */
    public NodeElement getTarget() {
        return target;
    }

    public List<Bendpoint> getBendpoints() {
        if(this.bendpoints ==  null)
            this.bendpoints = new ArrayList<Bendpoint>();
        return this.bendpoints;
    }

    /**
     * TODO check about removing
     * 
     * @param source
     *            The source to set.
     */
    public void setSource(NodeElement source) {
    	this.sId = source.getNodeId();
        this.source = source;
        this.edge.setStart(source.getNode());
    }

    /**
     * @param target
     *            The target to set.
     */
    public void setTarget(NodeElement target) {
    	this.tId = target.getNodeId();
        this.target = target;
        this.edge.setEnd(target.getNode());
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Inserts a bendpoint.
     */
    public void insertBendpoint(int index, Bendpoint point) {
        this.getBendpoints().add(index, point);
        firePropertyChange(IConstants.PROPERTY_CHANGE_BENDPOINT, null, null);
    }

    /**
     * Removes a bendpoint.
     */
    public void removeBendpoint(int index) {
        this.getBendpoints().remove(index);
        firePropertyChange(IConstants.PROPERTY_CHANGE_BENDPOINT, null, null);
    }

    /**
     * Sets another location for an existing bendpoint.
     */
    public void setBendpoint(int index, Bendpoint point) {
        this.getBendpoints().set(index, point);
        firePropertyChange(IConstants.PROPERTY_CHANGE_BENDPOINT, null, null);
    }

    public String toString() {
        return "<LinkElement>name: " + this.name + " type: " + this.getType();
    }
    
	public void setVisible(boolean value){
		this.fireVisibilityChange(IConstants.PROPERTY_CHANGE__LINK_INVISIBLE, value);
	}

	protected void fireVisibilityChange(String prop, boolean value){
		listeners.firePropertyChange(prop, null, value);
	}
	
	/*
	 * Tracking IDs for reconstruction of object
	 */
	public String getSourceId(){
		return this.sId;
	}
	public String getTargetId(){
		return this.tId;
	}

	/*
     * Overriding serialize object due to Java Bug4152790
     */
    private void writeObject(ObjectOutputStream os) throws IOException{    	
   	 	// write the object
		os.defaultWriteObject();
	}
    /*
     * Overriding serialize object due to Java Bug4152790
     */
    private void readObject(ObjectInputStream os) throws IOException, ClassNotFoundException  {
    	 // rebuild this object
    	 os.defaultReadObject();    	 	
    }
    

}
