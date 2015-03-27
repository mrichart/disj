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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.widgets.Shell;

import distributed.plugin.core.Node;
import distributed.plugin.ui.IGraphEditorConstants;
import distributed.plugin.ui.dialogs.TreeDialog;
import distributed.plugin.ui.models.GraphElementFactory;
import distributed.plugin.ui.models.LinkElement;
import distributed.plugin.ui.models.NodeElement;

/**
 * @author Me
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class Tree extends AbstractGraph {

    private static final int GAP = IGraphEditorConstants.NODE_SIZE + 10;
    private static final int HIGHT = IGraphEditorConstants.NODE_SIZE * 4;
 
    private boolean isRootedTree;
    private int depth;   
    private String linkType;
    
    private int[] trunk;
    private List<NodeElement[]> tree;
    private Random ran;
    
    /**
     * Constructor
     */
    public Tree(GraphElementFactory factory, Shell shell) {
    	super(factory, shell);   
        this.depth = 0;
        this.linkType = IGraphEditorConstants.BI;
        this.isRootedTree = false;      
        this.trunk = null;
        this.tree = null;
        this.ran = new Random(System.currentTimeMillis());
    }

    /**
     * @see distributed.plugin.ui.models.topologies.ITopology#getName()
     */
    public String getName() {
        return IGraphEditorConstants.CREATE_TREE_COMD;
    }

    /**
	 * @see distributed.plugin.ui.models.topologies.ITopology#createTopology()
	 */
	public void createTopology() {
		TreeDialog dialog = new TreeDialog(this.shell);
	    dialog.open();
	    
	    if(!dialog.isCancel()){
	        this.numNode = dialog.getNumNode();
	        this.depth = dialog.getDepthLength();
	        this.linkType = dialog.getLinkType();
	        this.isRootedTree = dialog.isRooted();
	        this.numInit = dialog.getNumInit();
	        this.trunk = new int[this.depth];	        	       
	       
	        // generate number of node at each level
	        this.initStructure();
	     	        
	        // initialize a tree with nodes without connection
	        this.initNodes();
	        
	        // set init nodes
	        this.setInitNodes();
	    }
	}

	/*
     * Generate tree structure with defining number of nodes that
 	 * can be at each tree level
     */
    private void initStructure(){
    	
    	int numRoot = 1;
    	
    	// get average minus root node and a level of a root
    	int avgNum = (this.numNode - numRoot)/(this.depth - 1);
    	
    	// find a remainder
    	int remainder = this.numNode - ((avgNum * (this.depth - 1)) + numRoot);
    	   	
    	// assign a root into at the beginning of a trunk
    	this.trunk[0] = numRoot;
    	
    	// assign number of internal nodes into the trunk
    	// at each level
    	for(int i = 1; i < this.trunk.length; i++){
    		this.trunk[i] = avgNum;
    	}
    	
    	// add remainder into a last level (leaves)
    	this.trunk[this.trunk.length - 1] += remainder;
    	
    }

    /*
     * Create nodes into a tree structure
     */
    private void initNodes(){
    	
    	this.tree = new ArrayList<NodeElement[]>();
    	
    	// create nodes at each level of the tree
    	for(int i = 0; i < this.trunk.length; i++){    		
    		NodeElement[] nodes = new NodeElement[this.trunk[i]];
    		this.tree.add(nodes);
    	}
    	
    	// add nodes into tracker
    	for(int i =0 ; i < this.tree.size(); i++){
    		NodeElement[] nodes = this.tree.get(i);    		
    		for(int k = 0; k < nodes.length; k++){
    			nodes[k] = this.factory.createNodeElement();
    			super.nodes.add(nodes[k]);
    		}
    	}
    }
    
    private LinkElement getLink(){
    	if (this.linkType.equals(IGraphEditorConstants.UNI)){
    		return this.factory.createUniLinkElement();        
        } else {
        	return this.factory.createBiLinkElement();
        }
    }
    
    /*
     * Random generate children - parent relationship between
     * level, and connect links between nodes
     */
    private void connectTree(){
    	
    	// start from a root down
    	int parIndex;
    	for(int i = 1; i < this.tree.size(); i++){
    		NodeElement[] parents = this.tree.get(i-1);
    		NodeElement[] children = this.tree.get(i);
    		
    		for(int k = 0; k < children.length; k++){

                // create and add link into a tracker
                LinkElement link = this.getLink();               
                super.links.add(link);
                
                NodeElement source = children[k];
                link.setSource(source);
                link.attachSource();

                // find and connect to a random parent (target) of this source
                parIndex = this.getRandomParent(parents.length);
                NodeElement target = parents[parIndex];
                link.setTarget(target);
                link.attachTarget();
                
                // set port label
                if(this.isRootedTree){
	                try{
		            	Node s = source.getNode();
		            	Node t = target.getNode();
		            	s.setPortLable("parent", link.getEdge());
		            	t.setPortLable("child:"+link.getEdgeId(), link.getEdge());
		            	
	            	}catch(Exception e){
	            		System.err.println("@Tree.setConnections() Cannot do rooted tree " + e);
	            	}
                }
    		}
    	}
    }
    
    /*
     * Generate random number between [0 - parentSize)
     */
    private int getRandomParent(int parentSize){   	    	
    	int r = this.ran.nextInt(parentSize);    	
    	return r;
    }
    
    /**
	 * @see distributed.plugin.ui.models.topologies.ITopology#setConnections()
	 */
	public void setConnections() {
		if(this.numNode > 1){
			this.connectTree();
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
    	if(this.numNode > 0){
    		this.drawRootedTree(point, 2);	        
    	}
    }

    private void drawRootedTree(Point point, int varies) {
    	NodeElement[] lev = this.tree.get(0);
    	lev[0].setLocation(point); // root
    	lev[0].setSize(new Dimension(IGraphEditorConstants.NODE_SIZE,
                 IGraphEditorConstants.NODE_SIZE));
    	int half;
    	int hight = HIGHT;
    	Point p = null;
    	for(int i = 1; i < this.tree.size(); i++){
    		lev = this.tree.get(i);
    		half = lev.length/2;
    		
    		for(int k = 0; k < lev.length; k++){
    		   	
    			 // divide children into 2 halves
	             if (k < half){
	                 p = new Point(point.x - ((GAP + varies) * (half - k)),
	                         point.y + hight);
	                 
	             } else if (k == half) {
	                 p = new Point(point.x, point.y + hight);
	                 
	             } else {
	                 p = new Point(point.x + ((GAP + varies) * (k - half)),
	                         point.y + hight);	   
	             }
    			
    			lev[k].setLocation(p);
    			lev[k].setSize(new Dimension(IGraphEditorConstants.NODE_SIZE,
                        IGraphEditorConstants.NODE_SIZE));
	                      
    		}
    		hight += HIGHT;
    	}    
    }
}
