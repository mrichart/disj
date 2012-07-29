/*******************************************************************************
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     DisJ Development Group
 *******************************************************************************/

package distributed.plugin.ui.models.topologies;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.widgets.Shell;

import distributed.plugin.core.Node;
import distributed.plugin.ui.IGraphEditorConstants;
import distributed.plugin.ui.dialogs.RingDialog;
import distributed.plugin.ui.models.GraphElementFactory;
import distributed.plugin.ui.models.LinkElement;
import distributed.plugin.ui.models.NodeElement;

/**
 * @author Me
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class Ring extends AbstractGraph {

    private static final int CONSTANT = 4;

    private static final int BASE = IGraphEditorConstants.NODE_SIZE * 4;

    private int radius;

    private String linkType;
    
    private boolean isOriented;
    
    private boolean isCancel;

    /**
     * Constructor;
     */
    public Ring(GraphElementFactory factory, Shell shell) {
    	super(factory, shell);
    	this.isOriented = false;
    	this.isCancel = true;
        this.radius = 0;
        this.linkType = IGraphEditorConstants.BI;      
      
    }

    /*
     * Find a good proportion of radius base on a size of network
     */
    private int calculateRadius(int numNode) {
        if (numNode < CONSTANT)
            return BASE;
        else
            return (int) Math.ceil(numNode / CONSTANT) * BASE;

    }

    /**
     * @see distributed.plugin.ui.models.topologies.ITopology#createTopology(int)
     */
    public void createTopology() {  	
    	RingDialog dialog = new RingDialog(this.shell);
        dialog.open();
        
        if(!dialog.isCancel()){
        	this.isCancel = false;
	        this.numNode = dialog.getNumNode();
	        this.linkType = dialog.getLinkType();
	        this.numInit = dialog.getNumInit();
	        this.isOriented = dialog.isOriented();
	        this.radius = this.calculateRadius(this.numNode);
	        
	        // Ring: num of link == num of node
	        for (int i = 0; i < this.numNode; i++) {          
	            if(this.linkType.equals(IGraphEditorConstants.UNI))
	                this.links.add(this.factory.createUniLinkElement());
	            else
	                this.links.add(this.factory.createBiLinkElement());
	            
	            this.nodes.add(this.factory.createNodeElement());
	        }
	        
	        // Bi-Ring with size 2 has only one link
	        if(this.numNode == 2 && this.linkType.equals(IGraphEditorConstants.BI)){
	        	 this.links.remove(1);
	        }
	        
	        // set init nodes
	        this.setInitNodes();
        }
    }
    
    /**
     * @see distributed.plugin.ui.models.topologies.ITopology#getConnectionType()
     */
    public String getConnectionType() {
        return this.linkType;
    }

    /**
     * @see distributed.plugin.ui.models.topologies.ITopology#applyLocation(org.eclipse.draw2d.geometry.Point)
     */
    public void applyLocation(Point point) {
    	if(this.isCancel){
    		return;
    	}
    	
        NodeElement node;
        double dTheta = 360.0 / (double) this.numNode;
        double thetaDeg, thetaRad, x, y;
        
        for (int i = 0; i < this.nodes.size(); i++) {            
            thetaDeg = 360.0 - (double) i * dTheta;
            thetaRad = Math.toRadians(thetaDeg);
            x = radius * Math.cos(thetaRad) + point.x;
            y = radius * Math.sin(thetaRad) + point.y;
                
            node = this.nodes.get(i);
            node.setLocation(new Point(x, y));
            node.setSize(new Dimension(IGraphEditorConstants.NODE_SIZE,
                    IGraphEditorConstants.NODE_SIZE));
        }               
    }

    /**
     * @see distributed.plugin.ui.models.topologies.ITopology#getName()
     */
    public String getName() {
        return IGraphEditorConstants.CREATE_RING_COMD;
    }

    /**
     * @see distributed.plugin.ui.models.topologies.ITopology#setConnections()
     */
    public void setConnections() {
    	if(this.isCancel){
    		return;
    	}
    	
        for(int i =0; i < this.links.size(); i++){
            LinkElement link = (LinkElement)this.links.get(i);
            NodeElement source = (NodeElement)this.nodes.get(i);
            
            link.setSource(source);
            link.attachSource();          
            
            NodeElement target = (NodeElement)this.nodes.get((i+1)%this.nodes.size());
            link.setTarget(target);
            link.attachTarget();
            
            if(this.isOriented){
            	try{
	            	Node s = source.getNode();
	            	Node t = target.getNode();
	            	s.setPortLable("right", link.getEdge());
	            	t.setPortLable("left", link.getEdge());
	            	
            	}catch(Exception e){
            		System.err.println("@Ring.setConnections() Cannot do oriented " + e);
            	}
            }            
        }
    }


}
