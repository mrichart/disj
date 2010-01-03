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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.Bendpoint;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import distributed.plugin.core.DisJException;
import distributed.plugin.core.Edge;
import distributed.plugin.core.IConstants;
import distributed.plugin.core.Node;
import distributed.plugin.runtime.Graph;
import distributed.plugin.runtime.GraphFactory;
import distributed.plugin.runtime.GraphLoader;
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

    public static final String PROPERTY_DIRECTION_TYPE = "L1 Type of Direction";
    
    public static final String PROPERTY_START_PORT = "L2 Source Port Name";
    
    public static final String PROPERTY_END_PORT = "L3 Target Port Name";

    public static final String PROPERTY_MSG_FLOW_TYPE = "L4 Message Flow Type";

    public static final String PROPERTY_DELAY_TYPE = "L5 Delay Type";
    
    public static final String PROPERTY_DELAY_SEED = "L6 Delay Seed";
    
    public static final String PROPERTY_RELIABLE = "L7 Reliable";
    
    public static final String PROPERTY_PROB_FAILURE = "L8 Probability of Failure";

    public static final String PROPERTY_TOTAL_MSG = "L9 Total Traffic";

    

    static final long serialVersionUID = 1;

    protected static IPropertyDescriptor[] descriptors;

    // message flow supported types
    protected static final String FIFO_TYPE = "FIFO";

    protected static final String NO_ORDER_TYPE = "No Order";

    // message delay time supported types
    public static final String FIXED = "Fixed";

    public static final String RANDOM_UNIFORM = "Random Uniform";

    public static final String RANDOM_POISSON = "Random Poisson";

    public static final String RANDOM_CUSTOMS = "Random Customs";
    
    private HashMap hmProperties = new HashMap();
    
    private static final int NUM_PROPERTIES = 9;

    static {
        descriptors = new IPropertyDescriptor[NUM_PROPERTIES];

        descriptors[0] = new PropertyDescriptor(
                PROPERTY_DIRECTION_TYPE, PROPERTY_DIRECTION_TYPE);
        
        descriptors[1] = new ComboBoxPropertyDescriptor(PROPERTY_RELIABLE,
                PROPERTY_RELIABLE, new String[] { "False", "True" });
        
        descriptors[2] = new ComboBoxPropertyDescriptor(PROPERTY_MSG_FLOW_TYPE,
                PROPERTY_MSG_FLOW_TYPE,
                new String[] { FIFO_TYPE, NO_ORDER_TYPE });
        
        descriptors[3] = new ComboBoxPropertyDescriptor(PROPERTY_DELAY_TYPE,
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

    private Edge edge;
    
    private Edge orgEdge;

    private NodeElement source, target;
    
    transient protected List bendpoints;

    /**
     * Contructor
     */
    protected LinkElement(String graphId, String id, short direction) {
        try {
            this.edge = new Edge(graphId, id, direction);
            this.orgEdge = null;
            this.bendpoints = new ArrayList();
            this.edge.setLinkElement(this);
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
            if(this.edge.getDirection() == IConstants.BI_DIRECTION)
                direction = IGraphEditorConstants.BI;
            return direction;

        } else if (propName.equals(PROPERTY_MSG_FLOW_TYPE)) {
            return this.mapFlowType(this.edge.getMsgFlowType());

        } else if (propName.equals(PROPERTY_DELAY_TYPE)) {
            return new Integer(this.mapDelayType(this.edge.getDelayType()));

        } else if (propName.equals(PROPERTY_TOTAL_MSG)) {
            return "" + this.edge.getNumMsg();

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
                }
                this.edge.setReliable(bool);
            }

        } else if (id.equals(PROPERTY_MSG_FLOW_TYPE)) {
            this.edge.setMsgFlowType(this.mapFlowType(value));

        } else if (id.equals(PROPERTY_DELAY_TYPE)) {
            this.edge.setDelayType(this.mapDelayType(value));

            Graph g = GraphFactory.getGraph(this.edge.getGraphId());
            g.setGlobalDelayType(IConstants.GLOBAL_CUSTOMS);
            
            System.out.println("Global delay type: " + g.getGlobalDelayType());
            try {
				GraphFactory.addGraph(g);
			} catch (DisJException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

        } else if (id.equals(PROPERTY_DELAY_SEED)) {
        	int val = Integer.parseInt((String)value);
        	if(val > 255 || val < 1){
        		// default value
        		this.edge.setDelaySeed((short)1);
        	} else {
        		this.edge.setDelaySeed((short)val);
        	}

            Graph g = GraphFactory.getGraph(this.edge.getGraphId());
            g.setGlobalDelayType(IConstants.GLOBAL_CUSTOMS);
            try {
				GraphFactory.addGraph(g);
			} catch (DisJException e) {
				// TODO Auto-generated catch block
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
        } else if (id.equals(PROPERTY_PROB_FAILURE)) {
            int val = Integer.parseInt((String)value);
            if(val < 0 || val > 100){
            	// default value
            	 this.edge.setProbOfFailure(5);
            }else{
            	this.edge.setProbOfFailure(val);
            }

        } else {
            return;
        }
    }
    
    public void resetPropertyValue(Object propName){

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
        } else if (propName.equals(PROPERTY_TOTAL_MSG)) {
            this.edge.resetNumMsg();

        } else {
            return;
        }   
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
    	 this.resetPropertyValue(PROPERTY_TOTAL_MSG);
    	 this.setVisible(true); // make every edge visible
    }
    
    public void setEdge(Edge edge) {
        this.edge = edge;
    }
    
    public String getEdgeId(){
        return this.edge.getEdgeId();
    }
    
    public abstract void attachSource();

    public abstract void attachTarget();

    public void detachSource() {
        if (source == null)
            return;
        source.disconnectInLink(this);
    }

    public void detachTarget() {
        if (target == null)
            return;
        target.disconnectOutLink(this);
    }

    private final Integer mapFlowType(short type) {
        return new Integer(type);
    }

    private final short mapFlowType(Object type) {
        if (type instanceof Integer)
            return (short) ((Integer) type).intValue();
        else
            return (short) -9;
    }

    private final short mapDelayType(Object type) {
    	// FIXME quick fix for now
    	short local_type;
    	
    	if (type instanceof Short) {
			Short new_name = (Short) type;
			local_type =  new_name.shortValue();
		}else{
			Integer i = (Integer)type;
			local_type = (short) i.intValue();
		}

    	if (local_type == 0){
    		return IConstants.LOCAL_FIXED;
    	} else if (local_type == 1){
    		return IConstants.LOCAL_RANDOM_UNIFORM;
    	} else if (local_type == 2){
    		return IConstants.LOCAL_RANDOM_POISSON;
    	}else{
    		return IConstants.LOCAL_RANDOM_CUSTOMS;
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

    public List getBendpoints() {
        if(this.bendpoints ==  null)
            this.bendpoints = new ArrayList();
        return this.bendpoints;
    }

    /**
     * TODO check about removing
     * 
     * @param source
     *            The source to set.
     */
    public void setSource(NodeElement source) {
        this.source = source;
        this.edge.setStart(source.getNode());
    }

    /**
     * @param target
     *            The target to set.
     */
    public void setTarget(NodeElement target) {
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
//	    System.err.println("[AdapterElement] fireStructureChange");
		listeners.firePropertyChange(prop, null, value);
	}
}
