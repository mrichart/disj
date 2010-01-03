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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
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
import distributed.plugin.core.IConstants;
import distributed.plugin.runtime.Graph;
import distributed.plugin.runtime.GraphFactory;
import distributed.plugin.ui.validators.NumberCellEditorValidator;

/**
 * Graph adapter
 * 
 * @version 0.1
 */
public class GraphElement extends AdapterElement {

    public static final String PROPERTY_NAME = "G1 Graph Name";
    public static final String PROPERTY_TOTAL_NODE = "G2 Total Node";
    public static final String PROPERTY_TOTAL_LINK = "G3 Total Link";
    public static final String PROPERTY_TOTAL_MSG_RECV = "G4 Total Message Received";
    public static final String PROPERTY_TOTAL_MSG_SENT = "G5 Total Message Sent";
    public static final String PROPERTY_GLOBAL_DELAY_TYPE = "G6 Global Delay Type";
    public static final String PROPERTY_GLOBAL_DELAY_SEED = "G7 Global Delay Seed";
    public static final String PROPERTY_PROTOCOL = "G8 Protocol";
    
    // message delay time supported types
    public static final String SYNCHRONOUS = "Synchronous";
    
    public static final String CUSTOMS = "Customs";

    public static final String RANDOM_UNIFORM = "Random Uniform";

    public static final String RANDOM_POISSON = "Random Poisson";

    public static final String RANDOM_CUSTOMS = "Random Customs";
    
    protected static IPropertyDescriptor[] descriptors;
    
    static final long serialVersionUID = IConstants.SERIALIZE_VERSION;

    private static final int NUM_PROPERTIES = 8;
    
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
        descriptors[5] = new ComboBoxPropertyDescriptor(PROPERTY_GLOBAL_DELAY_TYPE,
                PROPERTY_GLOBAL_DELAY_TYPE, new String[] { SYNCHRONOUS,
                RANDOM_UNIFORM, RANDOM_POISSON, RANDOM_CUSTOMS, CUSTOMS });
        descriptors[6] = new TextPropertyDescriptor(PROPERTY_GLOBAL_DELAY_SEED,
                PROPERTY_GLOBAL_DELAY_SEED);
        ((PropertyDescriptor) descriptors[6])
                .setValidator(NumberCellEditorValidator.instance());
        descriptors[7] = new TextPropertyDescriptor(PROPERTY_PROTOCOL,PROPERTY_PROTOCOL);
    }
    
    transient private Shell shell;
    
    private Graph graph;

    private List nodeElements;

    private List linkElements;
    

    /**
     * TODO it can create a graph with a given partName
     *
     */
    public GraphElement() {
        this.graph = GraphFactory.createGraph();
        this.nodeElements = new ArrayList();
        this.linkElements = new ArrayList();
        //System.out.println("[GraphElement] created "   + this.graph);
    }
    
    /**
     * @return
     */
    public String getGraphId(){
        return this.graph.getId();
    }
    
    public void setGraphId(String partName){
        this.graph.setId(partName);
        try {
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
            NodeElement e = (NodeElement)this.nodeElements.get(i);
            e.copyNode();     
        }       
        for(int i =0; i < this.linkElements.size(); i++){
            LinkElement e = (LinkElement)this.linkElements.get(i);
            e.copyEdge();
        }
    }
    
    public void resetGraphElement(){       
        for(int i =0; i < this.nodeElements.size(); i++){
            NodeElement e = (NodeElement)this.nodeElements.get(i);
            e.resetNode();
            e.getNode().setEntity(null);           
        }      
        for(int i =0; i < this.linkElements.size(); i++){
            LinkElement e = (LinkElement)this.linkElements.get(i);
            e.resetEdge();
        }
        this.resetPropertyValue("");
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
    
    public void addStateColor(Hashtable ht){
        this.graph.addColor(ht);
        try {
			GraphFactory.addGraph(this.graph);
		} catch (DisJException e) {
			System.err.println("Error add graph into map "+ e);
		}
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
        this.graph.removeColor(state);
        try {
			GraphFactory.addGraph(this.graph);
		} catch (DisJException e) {
			System.err.println("Error add graph into map "+ e);
		}
    }

    public List getNodeElements() {
        return this.nodeElements;
    }

    public List getLinkElements() {
        return this.linkElements;
    }
    
    public Iterator getStateColors(){
        return this.graph.getStateColors();
    }
    
    public Color getStateColor(Object state){
        RGB rgb = (RGB)this.graph.getColor(state);
        if(rgb != null)
            return  new Color(this.shell.getDisplay(), rgb);
        else
            return null;
    }
    
    public int getNumberOfState(){
        return this.graph.getNumberOfState();
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
        if (propName.equals(PROPERTY_NAME)) {
            return this.getGraphId();
            
        } else if (propName.equals(PROPERTY_TOTAL_NODE)) {
            return new Integer(this.nodeElements.size());
            
        } else if (propName.equals(PROPERTY_TOTAL_LINK)) {
            return new Integer(this.linkElements.size());
            
        } else if (propName.equals(PROPERTY_TOTAL_MSG_RECV)) {
            int sum = 0;
            for(int i=0; i < this.nodeElements.size(); i++){
                NodeElement n = (NodeElement)this.nodeElements.get(i);
                sum += n.getNumMsgRecieved();
            }
            return new Integer(sum);
            
        } else if (propName.equals(PROPERTY_TOTAL_MSG_SENT)) {
            int sum = 0;
            for(int i=0; i < this.nodeElements.size(); i++){
                NodeElement n = (NodeElement)this.nodeElements.get(i);
                sum += n.getNumMsgSent();
            }
            return new Integer(sum);
            
        } else if (propName.equals(PROPERTY_TOTAL_MSG_SENT)) {
            int sum = 0;
            for(int i=0; i < this.nodeElements.size(); i++){
            	LinkElement n = (LinkElement)this.linkElements.get(i);
                // NOTE do this
            }
            return new Integer(sum);

        } else if (propName.equals(PROPERTY_GLOBAL_DELAY_TYPE)) {
            return new Integer(this.mapGlobalDelayType(this.graph.getGlobalDelayType()));
           

        } else if (propName.equals(PROPERTY_GLOBAL_DELAY_SEED)) {
            return "" + this.graph.getGlobalDelaySeed();
        } 
        else if (propName.equals(PROPERTY_PROTOCOL)) {
            return this.graph.getProtocol();
        } 
        else {
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
        super.setPropertyValue(id, value);
        // nothing to set here
        if(id.equals(PROPERTY_PROTOCOL)){
        	graph.setProtocol((String)value);
        }
        
        if (id.equals(PROPERTY_GLOBAL_DELAY_TYPE)){
        	short global_type = mapGlobalDelayType( value );
        	graph.setGlobalDelayType(global_type);
    		short local_type;
        	if (global_type == IConstants.GLOBAL_CUSTOMS){
        		try {
    				GraphFactory.addGraph(this.graph);
    			} catch (DisJException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
        		return;        		
        	}else if(global_type == IConstants.GLOBAL_RANDOM_CUSTOMS){
    			local_type = IConstants.LOCAL_RANDOM_CUSTOMS;
    		}
    		else if(global_type == IConstants.GLOBAL_RANDOM_UNIFORM){
    			local_type = IConstants.LOCAL_RANDOM_UNIFORM;
    		}
    		else if(global_type == IConstants.GLOBAL_RANDOM_POISSON){
    			local_type = IConstants.LOCAL_RANDOM_POISSON;
    		}
    		else{
    			local_type = IConstants.LOCAL_FIXED;
    		}
        	
        	Map edges = this.graph.getEdges();
        	Iterator itr = edges.keySet().iterator();
        	while(itr.hasNext()){
        		Object key = itr.next();
        		Edge ed = (Edge)edges.get(key);
        		ed.setDelaySeed(this.graph.getGlobalDelaySeed());
                System.out.print("graph: local delay seed: "+ this.graph.getGlobalDelaySeed());
        		ed.setDelayType(local_type);
        	}
        	try {
				GraphFactory.addGraph(this.graph);
			} catch (DisJException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        else if (id.equals(PROPERTY_GLOBAL_DELAY_SEED)) {
        	System.out.print("graph: global delay seed is set ");
        	int val = Integer.parseInt((String)value);
        	if(val > 255 || val < 1){
        		// default value
        		this.graph.setGlobalDelaySeed((short)1);
        		
        		Map edges = this.graph.getEdges();
            	Iterator itr = edges.keySet().iterator();
            	while(itr.hasNext()){   
            		Object key = itr.next();
            		Edge ed = (Edge)edges.get(key);
            		ed.setDelaySeed((short)1);
            	}
        	} else {
        		this.graph.setGlobalDelaySeed((short)val);
        		
        		Map edges = this.graph.getEdges();
            	Iterator itr = edges.keySet().iterator();
            	while(itr.hasNext()){   
                	Object key = itr.next();
            		Edge ed = (Edge)edges.get(key);
            		ed.setDelaySeed((short)val);
            	}
        	}
        	try {
				GraphFactory.addGraph(this.graph);
			} catch (DisJException e) {
				e.printStackTrace();
			}
        }else {
            return;
        }
    }
    

    public void resetPropertyValue(Object propName) {
        super.resetPropertyValue(propName);
        // nothinng to set here
    }
    

    private final short mapGlobalDelayType(Object type) {
    	
    	short global_type;
    	if (type instanceof Short) {
    		System.out.print("graph: its short");
			Short new_name = (Short) type;
			System.out.println(": " + new_name );
			global_type = new_name.shortValue();
		}else{
			System.out.print("graph: its int");
			Integer i = (Integer)type;
			System.out.println(": " + i );
			global_type = (short) i.intValue(); 
		}
    	
    	if (global_type == 0){
    		return IConstants.GLOBAL_SYNCHRONOUS;
    	} else if (global_type == 1){
    		return IConstants.GLOBAL_RANDOM_UNIFORM;
    	} else if (global_type == 2){
    		return IConstants.GLOBAL_RANDOM_POISSON;
    	} else if (global_type == 3){
    		return IConstants.GLOBAL_RANDOM_CUSTOMS;
    	}else{
    		return IConstants.GLOBAL_CUSTOMS;
    	}
    }

}
