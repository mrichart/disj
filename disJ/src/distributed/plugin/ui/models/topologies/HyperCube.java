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
public class HyperCube implements ITopology {

    private static final int GAP = IGraphEditorConstants.NODE_SIZE * 2;
    private static final int IN_GAP = IGraphEditorConstants.NODE_SIZE * 3;
    private static final int OUT_GAP = IGraphEditorConstants.NODE_SIZE * 7;
    private static final int INTER_GAP = OUT_GAP + IN_GAP;

    private int next1;
    
    private int next2;

    private int dimension;

    private int numNode;

    private int numLink;

    private String linkType;

    private List nodes;

    private List links;

    private Shell shell;

    private GraphElementFactory factory;

    private List packs3D;

    /**
     * 
     */
    public HyperCube(GraphElementFactory factory, Shell shell) {
        this.next1 = -1;
        this.next2 = -1;
        this.factory = factory;
        this.shell = shell;
        HyperCubeDialog dialog = new HyperCubeDialog(this.shell);
        dialog.open();
        this.dimension = dialog.getDimension();
        this.numNode = (int) Math.pow(2, this.dimension);
        this.numLink = (this.numNode * this.dimension) / 2;
        this.linkType = dialog.getLinkType();
        this.nodes = new ArrayList();
        this.links = new ArrayList();
        this.packs3D = new ArrayList();
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
    }

    /**
     * @see distributed.plugin.ui.models.topologies.ITopology#getAllNodes()
     */
    public List getAllNodes() {
        return this.nodes;
    }

    private NodeElement nextNode() {
        return (NodeElement) this.nodes.get(++next1);
    }

    /**
     * @see distributed.plugin.ui.models.topologies.ITopology#getAllLinks()
     */
    public List getAllLinks() {
        return this.links;
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
        }
    }

    private NodeElement[][] get2DPack() {
        NodeElement[][] pack = new NodeElement[2][2];
        for (int i = 0; i < pack.length; i++)
            for (int j = 0; j < pack[i].length; j++) {
                pack[i][j] = this.nextNode();
                pack[i][j].setSize(new Dimension(
                        IGraphEditorConstants.NODE_SIZE,
                        IGraphEditorConstants.NODE_SIZE));
            }
        return pack;
    }

    private void draw2Dimension(Point point, int gap, NodeElement[][] pack) {
        for (int i = 0; i < pack.length; i++)
            for (int j = 0; j < pack[i].length; j++) {
                Point p = new Point(point.x + (i * gap), point.y + (j * gap));
                pack[i][j].setLocation(p);
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
    
    private List connect4(int i0, int i1, int i2, int i3){
        NodeElement[][] out1 = (NodeElement[][])this.packs3D.get(i0);
        NodeElement[][] in1  = (NodeElement[][])this.packs3D.get(i1);
        this.connect3D(out1, in1);
        
        NodeElement[][] out2 = (NodeElement[][])this.packs3D.get(i2);
        NodeElement[][] in2  = (NodeElement[][])this.packs3D.get(i3);
        this.connect3D(out2, in2);  
        
        this.connecXD(out1, in1, out2, in2);
        
        ArrayList list = new ArrayList();
        list.add(out1);
        list.add(in1);
        list.add(out2);
        list.add(in2);
        return list;
    }
    
    private List connect5(int s){
        List part1 = this.connect4(s++, s++,s++,s++);
        NodeElement[][] out1 = (NodeElement[][])part1.get(0);
        NodeElement[][] in1 = (NodeElement[][])part1.get(1);
        NodeElement[][] out2 = (NodeElement[][])part1.get(2);
        NodeElement[][] in2 = (NodeElement[][])part1.get(3);
        
        List part2 = this.connect4(s++,s++,s++,s++);
        NodeElement[][] out3 = (NodeElement[][])part2.get(0);
        NodeElement[][] in3 = (NodeElement[][])part2.get(1);
        NodeElement[][] out4 = (NodeElement[][])part2.get(2);
        NodeElement[][] in4 = (NodeElement[][])part2.get(3);
        
        this.connecXD(out1,in1, out3, in3);
        this.connecXD(out2, in2, out4, in4);
        
        ArrayList list = new ArrayList();
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
    
    private List connect6(){
        List part1 = this.connect5(0);
        NodeElement[][] out1 = (NodeElement[][])part1.get(0);
        NodeElement[][] in1 = (NodeElement[][])part1.get(1);
        NodeElement[][] out2 = (NodeElement[][])part1.get(2);
        NodeElement[][] in2 = (NodeElement[][])part1.get(3);
        NodeElement[][] out3 = (NodeElement[][])part1.get(4);
        NodeElement[][] in3 = (NodeElement[][])part1.get(5);
        NodeElement[][] out4 = (NodeElement[][])part1.get(6);
        NodeElement[][] in4 = (NodeElement[][])part1.get(7);
        
        List part2 = this.connect5(8);
        NodeElement[][] out5 = (NodeElement[][])part2.get(0);
        NodeElement[][] in5 = (NodeElement[][])part2.get(1);
        NodeElement[][] out6 = (NodeElement[][])part2.get(2);
        NodeElement[][] in6 = (NodeElement[][])part2.get(3);
        NodeElement[][] out7 = (NodeElement[][])part2.get(4);
        NodeElement[][] in7 = (NodeElement[][])part2.get(5);
        NodeElement[][] out8 = (NodeElement[][])part2.get(6);
        NodeElement[][] in8 = (NodeElement[][])part2.get(7);
        
        this.connecXD(out1,in1, out5, in5);
        this.connecXD(out2, in2, out6, in6);
        this.connecXD(out3,in3, out7, in7);
        this.connecXD(out4, in4, out8, in8);
        
        ArrayList list = new ArrayList();
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
    
    private void connect2D(NodeElement[][] pack){
        for(int i=0; i < pack.length; i++)
            for(int j=0; j < pack[i].length-1; j++){
                LinkElement link = this.nextLink();
                link.setSource(pack[i][j]);
                link.attachSource();
                link.setTarget(pack[i][j + 1]);
                link.attachTarget();
            }
        
        for(int i=0; i < pack.length-1; i++)
            for(int j=0; j < pack[i].length; j++){
                LinkElement link = this.nextLink();
                link.setSource(pack[i][j]);
                link.attachSource();
                link.setTarget(pack[i+1][j]);
                link.attachTarget();
            }  
    }
    
    private void connect3D(NodeElement[][] out, NodeElement[][] in){
        this.connect2D(out);
        this.connect2D(in);
        
        for(int i=0; i < out.length; i++)
            for(int j =0; j < out[i].length; j++){
                LinkElement link = this.nextLink();
                link.setSource(out[i][j]);
                link.attachSource();
                link.setTarget(in[i][j]);
                link.attachTarget();
            }
    }
    
    private void connecXD(NodeElement[][] out1, NodeElement[][] in1, NodeElement[][] out2, NodeElement[][] in2){
        for(int i=0; i < out1.length; i++)
            for(int j =0; j < out1[i].length; j++){
                LinkElement link = this.nextLink();
                link.setSource(out1[i][j]);
                link.attachSource();
                link.setTarget(out2[i][j]);
                link.attachTarget();
            }
        
        for(int i=0; i < in1.length; i++)
            for(int j =0; j < in1[i].length; j++){
                LinkElement link = this.nextLink();
                link.setSource(in1[i][j]);
                link.attachSource();
                link.setTarget(in2[i][j]);
                link.attachTarget();
            }
    }

}
