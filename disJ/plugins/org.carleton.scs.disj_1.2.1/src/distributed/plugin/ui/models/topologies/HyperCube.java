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
import distributed.plugin.ui.dialogs.HyperCubeDialog;
import distributed.plugin.ui.models.GraphElementFactory;
import distributed.plugin.ui.models.LinkElement;
import distributed.plugin.ui.models.NodeElement;

/**
 * @author Me
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class HyperCube extends AbstractGraph {

    private static final int GAP = IGraphEditorConstants.NODE_SIZE * 2;
    private static final int IN_GAP = IGraphEditorConstants.NODE_SIZE * 3;
    private static final int OUT_GAP = IGraphEditorConstants.NODE_SIZE * 7;
    private static final int INTER_GAP = OUT_GAP + IN_GAP;

    private boolean isOriented;
    
    private int next1;
    
    private int next2;

    private int dimension;
   
    private String linkType;

    private List<NodeElement[][]> packs3D;

    /**
     * 
     */
    public HyperCube(GraphElementFactory factory, Shell shell) {
    	super(factory, shell);
    	this.isOriented = false;
        this.next1 = -1;
        this.next2 = -1;         
    	this.dimension = 0;  	
    	this.linkType = IGraphEditorConstants.BI;    
        this.packs3D = new ArrayList<NodeElement[][]>();
    }

    /**
     * @see distributed.plugin.ui.models.topologies.ITopology#getName()
     */
    public String getName() {
        return IGraphEditorConstants.CREATE_HYPECUBE_COMD;
    }

    /**
     * @see distributed.plugin.ui.models.topologies.ITopology#createTopology()
     */
    public void createTopology() {
    	
    	HyperCubeDialog dialog = new HyperCubeDialog(this.shell);
        dialog.open();
        if(!dialog.isCancel()){
	        this.dimension = dialog.getDimension();
	        this.numNode = (int) Math.pow(2, this.dimension);
	        this.numLink = (this.numNode * this.dimension) / 2;
	        this.linkType = dialog.getLinkType();
	        this.numInit = dialog.getNumInit();
	        this.isOriented = dialog.isOriented();
        
	        for (int i = 0; i < this.numNode; i++) {
	            this.nodes.add(this.factory.createNodeElement());
	        }
	
	        if (this.linkType.equals(IGraphEditorConstants.BI)) {
	            for (int i = 0; i < this.numLink; i++) {
	                this.links.add(this.factory.createBiLinkElement());
	            }
	        } else {
	            for (int i = 0; i < this.numLink; i++) {
	                this.links.add(this.factory.createUniLinkElement());
	            }
	        }
	        
	        // set init nodes
	        this.setInitNodes();
        }
    }

    private NodeElement nextNode() {
        return (NodeElement) this.nodes.get(++next1);
    }

    private LinkElement nextLink(){
        return (LinkElement)this.links.get(++next2);
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
	        if (this.dimension == 2) {
	            NodeElement[][] pack = this.get2DPack();
	            this.draw2Dimension(point, IN_GAP, pack);
	            this.packs3D.add(pack);
	        } else if (this.dimension == 3) {
	            this.draw3Dimension(point);
	        } else if (this.dimension == 4) {
	            this.draw4Dimension(point);
	        } else if (this.dimension == 5) {
	            this.draw5Dimension(point);
	        } else if (this.dimension == 6) {
	            this.draw4Dimension(point);
	        } else if (this.dimension == 7) {
	            this.draw5Dimension(point);
	        }else{
	        	//not support
	        }
    	}
    }

    private NodeElement[][] get2DPack() {
        NodeElement[][] pack = new NodeElement[2][2];
        for (int i = 0; i < pack.length; i++)
            for (int k = 0; k < pack[i].length; k++) {
                pack[i][k] = this.nextNode();
                pack[i][k].setSize(new Dimension(
                        IGraphEditorConstants.NODE_SIZE,
                        IGraphEditorConstants.NODE_SIZE));
            }
        return pack;
    }

    private void draw2Dimension(Point point, int gap, NodeElement[][] pack) {
        for (int i = 0; i < pack.length; i++)
            for (int k = 0; k < pack[i].length; k++) {
                Point p = new Point(point.x + (i * gap), point.y + (k * gap));
                pack[i][k].setLocation(p);
            }
    }

    private void draw3Dimension(Point point) {
        NodeElement[][] outBox = this.get2DPack();
        this.draw2Dimension(point, OUT_GAP, outBox);

        Point p = new Point(point.x + GAP, point.y + GAP);
        NodeElement[][] inBox = this.get2DPack();
        this.draw2Dimension(p, IN_GAP, inBox);

        this.packs3D.add(outBox);
        this.packs3D.add(inBox);
    }

    private void draw4Dimension(Point point) {
        this.draw3Dimension(point);

        Point p = new Point(point.x + INTER_GAP, point.y);
        this.draw3Dimension(p);
    }

    private void draw5Dimension(Point point) {
        this.draw4Dimension(point);

        Point p = new Point(point.x, point.y + INTER_GAP);
        this.draw4Dimension(p);
    }

    // FIXME the gap is wrong (probably)
    private void draw6Dimension(Point point) {
        this.draw5Dimension(point);

        Point p = new Point(point.x + INTER_GAP*2, point.y);
        this.draw5Dimension(p);
    }

    private void draw7Dimension(Point point) {
        this.draw6Dimension(point);

        Point p = new Point(point.x, point.y + INTER_GAP*2);
        this.draw6Dimension(p);
    }

    /**
     * @see distributed.plugin.ui.models.topologies.ITopology#setConnections()
     */
    public void setConnections() {
        if (this.dimension == 2) {
            this.connect2D((NodeElement[][])this.packs3D.get(0));

        } else if (this.dimension == 3) {
            NodeElement[][] out = (NodeElement[][])this.packs3D.get(0);
            NodeElement[][] in  = (NodeElement[][])this.packs3D.get(1);
            this.connect3D(out, in);
            
        } else if (this.dimension == 4) {
            this.connect4(0,1,2,3);
            
        } else if (this.dimension == 5) {
            this.connect5(0);
            
        } else if (this.dimension == 6){
            this.connect6();
        }


    }
    
    private List<NodeElement[][]> connect4(int i0, int i1, int i2, int i3){
        NodeElement[][] out1 = this.packs3D.get(i0);
        NodeElement[][] in1  = this.packs3D.get(i1);
        this.connect3D(out1, in1);
        
        NodeElement[][] out2 = this.packs3D.get(i2);
        NodeElement[][] in2  = this.packs3D.get(i3);
        this.connect3D(out2, in2);  
        
        this.connecXD(4, out1, in1, out2, in2);
        
        List<NodeElement[][]> list = new ArrayList<NodeElement[][]>();
        list.add(out1);
        list.add(in1);
        list.add(out2);
        list.add(in2);
        return list;
    }
    
    private List<NodeElement[][]> connect5(int s){
        List<NodeElement[][]> part1 = this.connect4(s++, s++,s++,s++);
        NodeElement[][] out1 = part1.get(0);
        NodeElement[][] in1 = part1.get(1);
        NodeElement[][] out2 = part1.get(2);
        NodeElement[][] in2 = part1.get(3);
        
        List<NodeElement[][]> part2 = this.connect4(s++,s++,s++,s++);
        NodeElement[][] out3 = part2.get(0);
        NodeElement[][] in3 = part2.get(1);
        NodeElement[][] out4 = part2.get(2);
        NodeElement[][] in4 = part2.get(3);
        
        this.connecXD(5, out1,in1, out3, in3);
        this.connecXD(5, out2, in2, out4, in4);
        
        List<NodeElement[][]> list = new ArrayList<NodeElement[][]>();
        list.add(out1);
        list.add(in1);
        list.add(out2);
        list.add(in2);
        list.add(out3);
        list.add(in3);
        list.add(out4);
        list.add(in4);
        
        return list;       
    }
    
    private List<NodeElement[][]> connect6(){
        List<NodeElement[][]> part1 = this.connect5(0);
        NodeElement[][] out1 = part1.get(0);
        NodeElement[][] in1 = part1.get(1);
        NodeElement[][] out2 = part1.get(2);
        NodeElement[][] in2 = part1.get(3);
        NodeElement[][] out3 = part1.get(4);
        NodeElement[][] in3 = part1.get(5);
        NodeElement[][] out4 = part1.get(6);
        NodeElement[][] in4 = part1.get(7);
        
        List<NodeElement[][]> part2 = this.connect5(8);
        NodeElement[][] out5 = part2.get(0);
        NodeElement[][] in5 = part2.get(1);
        NodeElement[][] out6 = part2.get(2);
        NodeElement[][] in6 = part2.get(3);
        NodeElement[][] out7 = part2.get(4);
        NodeElement[][] in7 = part2.get(5);
        NodeElement[][] out8 = part2.get(6);
        NodeElement[][] in8 = part2.get(7);
        
        this.connecXD(6, out1, in1, out5, in5);
        this.connecXD(6, out2, in2, out6, in6);
        this.connecXD(6, out3, in3, out7, in7);
        this.connecXD(6, out4, in4, out8, in8);
        
        List<NodeElement[][]> list = new ArrayList<NodeElement[][]>();
        list.add(out1);
        list.add(in1);
        list.add(out2);
        list.add(in2);
        list.add(out3);
        list.add(in3);
        list.add(out4);
        list.add(in4);
        list.add(out5);
        list.add(in5);
        list.add(out6);
        list.add(in6);
        list.add(out7);
        list.add(in7);
        list.add(out8);
        list.add(in8);
        
        return list;       
    }
    
    private void connect2D(NodeElement[][] pack){
        for(int i=0; i < pack.length; i++){
            for(int k=0; k < pack[i].length-1; k++){
                LinkElement link = this.nextLink();
                link.setSource(pack[i][k]);
                link.attachSource();
                link.setTarget(pack[i][k + 1]);
                link.attachTarget();
                
                if(this.isOriented){
                	try{
    	            	Node s = pack[i][k].getNode();
    	            	Node t = pack[i][k+1].getNode();
    	            	s.setPortLable("d1", link.getEdge());
    	            	t.setPortLable("d1", link.getEdge());
    	            	
                	}catch(Exception e){
                		System.err.println("@Hypercube.setConnections() Cannot do oriented " + e);
                	}
                }   
            }
        }
        
        for(int i=0; i < pack.length-1; i++){
            for(int k=0; k < pack[i].length; k++){
                LinkElement link = this.nextLink();
                link.setSource(pack[i][k]);
                link.attachSource();
                link.setTarget(pack[i+1][k]);
                link.attachTarget();
                
                if(this.isOriented){
                	try{
    	            	Node s = pack[i][k].getNode();
    	            	Node t = pack[i+1][k].getNode();
    	            	s.setPortLable("d2", link.getEdge());
    	            	t.setPortLable("d2", link.getEdge());
    	            	
                	}catch(Exception e){
                		System.err.println("@Hypercube.setConnections2() Cannot do oriented " + e);
                	}
                }
            }  
        }
    }
    
    private void connect3D(NodeElement[][] out, NodeElement[][] in){
        this.connect2D(out);
        this.connect2D(in);
        
        for(int i=0; i < out.length; i++)
            for(int k =0; k < out[i].length; k++){
                LinkElement link = this.nextLink();
                link.setSource(out[i][k]);
                link.attachSource();
                link.setTarget(in[i][k]);
                link.attachTarget();
                
                if(this.isOriented){
                	try{
    	            	Node s = out[i][k].getNode();
    	            	Node t = in[i][k].getNode();
    	            	s.setPortLable("d3", link.getEdge());
    	            	t.setPortLable("d3", link.getEdge());
    	            	
                	}catch(Exception e){
                		System.err.println("@Hypercube.setConnections3() Cannot do oriented " + e);
                	}
                }
            }
    }
    
    private void connecXD(int dimention, NodeElement[][] out1, NodeElement[][] in1, 
    		NodeElement[][] out2, NodeElement[][] in2){
    	String dim = "d"+dimention;
        for(int i=0; i < out1.length; i++)
            for(int k =0; k < out1[i].length; k++){
                LinkElement link = this.nextLink();
                link.setSource(out1[i][k]);
                link.attachSource();
                link.setTarget(out2[i][k]);
                link.attachTarget();
                
                if(this.isOriented){
                	try{
    	            	Node s = out1[i][k].getNode();
    	            	Node t = out2[i][k].getNode();
    	            	s.setPortLable(dim, link.getEdge());
    	            	t.setPortLable(dim, link.getEdge());
    	            	
                	}catch(Exception e){
                		System.err.println("@Hypercube.setConnections4() Cannot do oriented " + e);
                	}
                }
            }
        
        for(int i=0; i < in1.length; i++)
            for(int k =0; k < in1[i].length; k++){
                LinkElement link = this.nextLink();
                link.setSource(in1[i][k]);
                link.attachSource();
                link.setTarget(in2[i][k]);
                link.attachTarget();
                
                if(this.isOriented){
                	try{
    	            	Node s = in1[i][k].getNode();
    	            	Node t = in2[i][k].getNode();
    	            	s.setPortLable(dim, link.getEdge());
    	            	t.setPortLable(dim, link.getEdge());
    	            	
                	}catch(Exception e){
                		System.err.println("@Hypercube.setConnections4() Cannot do oriented " + e);
                	}
                }
            }
    }

}
