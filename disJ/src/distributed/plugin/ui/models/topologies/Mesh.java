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

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.widgets.Shell;

import distributed.plugin.core.Node;
import distributed.plugin.ui.IGraphEditorConstants;
import distributed.plugin.ui.dialogs.MeshDialog;
import distributed.plugin.ui.models.GraphElementFactory;
import distributed.plugin.ui.models.LinkElement;
import distributed.plugin.ui.models.NodeElement;

/**
 * @author Me
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class Mesh extends AbstractGraph {

    private static final int GAP = IGraphEditorConstants.NODE_SIZE * 3;

    private boolean isOriented;
    
    private int cols;

    private int rows;

    private String linkType;

    private NodeElement[][] myNodes;

    /**
     * Constructor
     */
    public Mesh(GraphElementFactory factory, Shell shell) {
    	super(factory, shell);   
    	this.isOriented = false;
        this.rows = 0;
        this.cols = 0;
        this.linkType = IGraphEditorConstants.BI;
        this.myNodes = null;
    }

    /**
     * @see distributed.plugin.ui.models.topologies.ITopology#getName()
     */
    public String getName() {
        return IGraphEditorConstants.CREATE_MESH_COMD;
    }

    /**
     * @see distributed.plugin.ui.models.topologies.ITopology#createTopology()
     */
    public void createTopology() {
    	
    	MeshDialog dialog = new MeshDialog(this.shell);
        dialog.open();
        
        if(!dialog.isCancel()){
        	this.rows = dialog.getNumRows();
            this.cols = dialog.getNumCols();
            this.linkType = dialog.getLinkType();
            this.myNodes = new NodeElement[this.rows][this.cols];
            this.numInit = dialog.getNumInit();
            this.isOriented = dialog.isOriented();
            
	        for (int i = 0; i < this.rows; i++) {
	            for (int j = 0; j < this.cols; j++) {
	                this.myNodes[i][j] = this.factory.createNodeElement();
	            }
	        }
	
	        // links for horizontal directions
	        for (int i = 0; i < this.rows; i++) {
	            for (int j = 0; j < this.cols-1; j++) {
	                LinkElement link;
	                if (this.linkType.equals(IGraphEditorConstants.UNI))
	                    link = this.factory.createUniLinkElement();
	                else
	                    link = this.factory.createBiLinkElement();
	                this.links.add(link);
	            }
	        }
	
	        // links for vertical directions
	        for (int i = 0; i < this.cols; i++) {
	            for (int j = 0; j < this.rows-1; j++) {
	                LinkElement link;
	                if (this.linkType.equals(IGraphEditorConstants.UNI))
	                    link = this.factory.createUniLinkElement();
	                else
	                    link = this.factory.createBiLinkElement();
	                this.links.add(link);
	            }
	        }
	        
	        // set init nodes
	        super.nodes = this.getAllNodes();
	        super.numNode = super.nodes.size();
	        this.setInitNodes();
        }
    }

    /**
     * @see distributed.plugin.ui.models.topologies.ITopology#getAllNodes()
     */
    public List<NodeElement> getAllNodes() {
        List<NodeElement> tmp = new ArrayList<NodeElement>();
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                tmp.add(this.myNodes[i][j]);
            }
        }
        return tmp;
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
        int x = point.x;
        int y = point.y;        
        int px, py;
        
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
            	px = x + (j * GAP);
            	py = y + (i * GAP);           	            
                Point p = new Point(px, py);
                this.myNodes[i][j].setLocation(p);
                this.myNodes[i][j].setSize(new Dimension(
                        IGraphEditorConstants.NODE_SIZE,
                        IGraphEditorConstants.NODE_SIZE));
            }
        }
    }

    /**
     * @see distributed.plugin.ui.models.topologies.ITopology#setConnections()
     */
    public void setConnections() {
        int count = -1;
        
        if(this.rows <= 0){
        	return;
        }
        // horizontal connections
        for (int i = 0; i < this.rows - 1; i++) {
            for (int j = 0; j < this.cols - 1; j++) {
                LinkElement link = (LinkElement) this.links.get(++count);
                link.setSource(this.myNodes[i][j]);
                link.attachSource();
                
                link.setTarget(this.myNodes[i][j + 1]);
                link.attachTarget();
                
                if(this.isOriented){
                	try{
    	            	Node s = this.myNodes[i][j].getNode();
    	            	Node t = this.myNodes[i][j+1].getNode();
    	            	s.setPortLable("east", link.getEdge());
    	            	t.setPortLable("west", link.getEdge());
    	            	
                	}catch(Exception e){
                		System.err.println("@Mesh.setConnections() Cannot do oriented horizontal " + e);
                	}
                }
            }
        }
        // last row
        for (int i = rows - 1; i < this.rows; i++) {
            for (int j = cols - 1; j > 0; j--) {
                LinkElement link = (LinkElement) this.links.get(++count);
                link.setSource(this.myNodes[i][j]);
                link.attachSource();
                link.setTarget(this.myNodes[i][j - 1]);
                link.attachTarget();
                
                if(this.isOriented){
                	try{
    	            	Node s = this.myNodes[i][j].getNode();
    	            	Node t = this.myNodes[i][j-1].getNode();
    	            	s.setPortLable("west", link.getEdge());
    	            	t.setPortLable("east", link.getEdge());
    	            	
                	}catch(Exception e){
                		System.err.println("@Mesh.setConnections() Cannot do oriented horizontal " + e);
                	}
                }
            }
        }

        // vertical connections
        // first column
        for (int i = 0; i < 1; i++) {
            for (int j = this.rows - 1; j > 0; j--) {
                LinkElement link = (LinkElement) this.links.get(++count);
                link.setSource(this.myNodes[j][i]);
                link.attachSource();
                link.setTarget(this.myNodes[j - 1][i]);
                link.attachTarget();
                
                if(this.isOriented){
                	try{
    	            	Node s = this.myNodes[j][i].getNode();
    	            	Node t = this.myNodes[j-1][i].getNode();
    	            	s.setPortLable("north", link.getEdge());
    	            	t.setPortLable("south", link.getEdge());
    	            	
                	}catch(Exception e){
                		System.err.println("@Mesh.setConnections() Cannot do oriented vertical " + e);
                	}
                }
            }
        }
        for (int i = 1; i < this.cols; i++) {
            for (int j = 0; j < this.rows - 1; j++) {
                LinkElement link = (LinkElement) this.links.get(++count);
                link.setSource(this.myNodes[j][i]);
                link.attachSource();
                link.setTarget(this.myNodes[j + 1][i]);
                link.attachTarget();
                
                if(this.isOriented){
                	try{
    	            	Node s = this.myNodes[j][i].getNode();
    	            	Node t = this.myNodes[j+1][i].getNode();
    	            	s.setPortLable("south", link.getEdge());
    	            	t.setPortLable("north", link.getEdge());
    	            	
                	}catch(Exception e){
                		System.err.println("@Mesh.setConnections() Cannot do oriented vertical " + e);
                	}
                }
            }
        }

    }

}
