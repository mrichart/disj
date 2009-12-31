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
public class Tree implements ITopology {

    private static final int GAP = IGraphEditorConstants.NODE_SIZE + 10;

    private static final int HIGHT = IGraphEditorConstants.NODE_SIZE * 4;

    private int count;

    private int nextL;

    private int numOfNode;

    private int diameter;

    private boolean rootedTree;

    private String linkType;

    private GraphElementFactory factory;

    private Shell shell;

    private List nodes;

    private List links;

    private int[] hops;

    private InternalNode tree[];

    private Random random;

    class InternalNode {

        private int level;

        final private NodeElement element;

        private List children;

        InternalNode(NodeElement root) {
            this.level = 1;
            this.element = root;
            this.children = new ArrayList();
        }

        NodeElement getElement() {
            return this.element;
        }

        void addChild(InternalNode node) {
            this.children.add(node);
        }

        List getChildren() {
            return this.children;
        }

        int getNumOfChildren() {
            return this.children.size();
        }

        InternalNode getChild(int i) {
            return (InternalNode) this.children.get(i);
        }

        int getLevel() {
            return this.level;
        }

        void setLevel(int level) {
            this.level = level;
        }
    }

    /**
     * Constructor
     */
    public Tree(GraphElementFactory factory, Shell shell) {
        this.count = -1;
        this.nextL = -1;
        this.factory = factory;
        this.shell = shell;
        TreeDialog dialog = new TreeDialog(this.shell);
        dialog.open();
        this.numOfNode = dialog.getNumNode();
        this.diameter = dialog.getDiamerLength();
        this.linkType = dialog.getLinkType();
        this.rootedTree = dialog.isRooted();
        this.nodes = new ArrayList();
        this.links = new ArrayList();
        this.hops = new int[this.diameter + 1];
        if (this.rootedTree)
            this.tree = new InternalNode[this.numOfNode];
        else
            this.tree = new InternalNode[this.diameter + 1];
        this.random = new Random(System.currentTimeMillis());
    }

    /**
     * @see distributed.plugin.ui.models.topologies.ITopology#getName()
     */
    public String getName() {
        return IGraphEditorConstants.CREATE_TREE_COMD;
    }

    private NodeElement nextNode() {
        return (NodeElement) this.nodes.get(++count);
    }

    private LinkElement nextLink() {
        return (LinkElement) this.links.get(++nextL);
    }

    /**
     * @see distributed.plugin.ui.models.topologies.ITopology#createTopology()
     */
    public void createTopology() {
        // Tree: the number of link == number of node - 1

        // create links
        if (this.linkType.equals(IGraphEditorConstants.UNI))
            for (int i = 0; i < this.numOfNode - 1; i++) {
                this.links.add(this.factory.createUniLinkElement());
            }
        else
            for (int i = 0; i < this.numOfNode - 1; i++) {
                this.links.add(this.factory.createBiLinkElement());
            }

        // create nodes
        for (int i = 0; i < this.numOfNode; i++) {
            NodeElement node = this.factory.createNodeElement();
            node.setSize(new Dimension(IGraphEditorConstants.NODE_SIZE,
                    IGraphEditorConstants.NODE_SIZE));
            this.nodes.add(node);
        }

        if (this.rootedTree) {
            // add node into a list
            for (int i = 0; i < this.tree.length; i++) {
                this.tree[i] = new InternalNode(this.nextNode());
            }
            // a random rooted tree;
            // r.h.s index of "i" is parent of "i",
            // therefore, the last index is root
            for (int i = 0; i < this.tree.length - 1; i++) {
                int par = this.random.nextInt(this.tree.length - 1 - i) + 1;
                this.tree[par + i].addChild(this.tree[i]);
            }
        } else {
            // assign number of nodes for each hop along a diameter line
            int maxChild = this.numOfNode - this.diameter - 1;
            this.assignChildren(maxChild);
            this.constructSubTree();
        }
    }

    /*
     * generate a number of children for each hops along a diameter
     */
    private void assignChildren(int maxChild) {

        // add a node along a diameter line
        for (int i = 0; i < this.hops.length; i++) {
            this.hops[i]++;
        }

        // add children along a diameter line except 2 ends
        for (int i = 1; i < this.hops.length - 1 && maxChild > 0; i++) {
            int deduct = this.random.nextInt(maxChild);
            this.hops[i] += deduct;
            maxChild -= deduct;
        }

        int i = 0;
        int size = this.hops.length - 2;
        while (maxChild > 0) {
            this.hops[(i % size) + 1]++;
            maxChild--;
            i++;
        }
    }

    /*
     * Construct a virtual tree
     */
    private void constructSubTree() {

        // create a set of nodes along the diameter path
        for (int i = 0; i < this.hops.length; i++) {
            this.tree[i] = new InternalNode(this.nextNode());
        }

        // construct a new sub-trees along the diameter path
        int maxChild = 0;
        int half = this.tree.length / 2;

        // first half of diameter
        for (int i = 0; i < half; i++) {
            // then.. max level <= i
            maxChild = this.subTree(i, this.hops[i] - 1, this.tree[i]);
            // if there is a leftover, add to root of each hop
            if (maxChild > 0) {
                for (int j = 0; j < maxChild; j++) {
                    InternalNode child = new InternalNode(this.nextNode());
                    tree[i].addChild(child);
                }
            }
        }

        // second half of diameter
        for (int i = this.tree.length; i > half; i--) {
            // then.. max level <= tree.lenght - i
            maxChild = this.subTree(tree.length - i, this.hops[i - 1] - 1,
                    this.tree[i - 1]);
            // if there is a leftover, add to root of each hop
            if (maxChild > 0) {
                for (int j = 0; j < maxChild; j++) {
                    InternalNode child = new InternalNode(this.nextNode());
                    tree[i - 1].addChild(child);
                }
            }
        }

    }

    private int subTree(int depth, int maxChild, InternalNode par) {

        // we dont wana do anymore
        if (maxChild == 0)
            return maxChild;

        if (depth == 0)
            return maxChild;

        int num = random.nextInt(maxChild) + 1;
        InternalNode child;
        for (int j = 0; j < num; j++) {
            maxChild--;
            child = new InternalNode(this.nextNode());
            par.addChild(child);
        }
        // toss the coin wheather we need to go
        // deeper til the max depth we can go

        int toss = random.nextInt(depth);
        if (toss > 0) {
            // still want more depth
            for (int j = 0; j < par.getNumOfChildren(); j++) {
                maxChild = this.subTree(depth--, maxChild, par.getChild(j));
            }
        }
        return maxChild;
    }

    /**
     * @see distributed.plugin.ui.models.topologies.ITopology#getAllNodes()
     */
    public List getAllNodes() {
        return this.nodes;
    }

    /**
     * @see distributed.plugin.ui.models.topologies.ITopology#getAllLinks()
     */
    public List getAllLinks() {
        return this.links;
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

        if (this.rootedTree)
            this.drawRootedTree(point, this.tree[this.tree.length - 1], 2);
        else
            // draw a random tree
            this.drawTree(point);

    }

    private void drawRootedTree(Point point, InternalNode par, int varies) {
        par.getElement().setLocation(point);
        if (par.getNumOfChildren() > 0) {
            List children = par.getChildren();
            int half = children.size() / 2;
            for (int i = 0; i < children.size(); i++) {
                Point p;
                // divide children into 2 halves
                if (i < half)
                    p = new Point(point.x - ((GAP + varies) * (half - i)),
                            point.y + HIGHT);
                else if (i == half)
                    p = new Point(point.x, point.y + HIGHT);
                else
                    p = new Point(point.x + ((GAP + varies) * (i - half)),
                            point.y + HIGHT);

                this.drawRootedTree(p, (InternalNode) children.get(i),
                        (int) (varies * 2));
            }
        }
    }

    private void drawTree(Point point) {
        // draw a subtree along the diameter
        for (int i = 0; i < tree.length; i++) {
            this.drawRootedTree(point, tree[i], 2);
            point = new Point(point.x + HIGHT, point.y);
        }
    }

    /*
     * Find a location for directed child of this node @return a locatoin for a
     * sibling
     */
    // private Point applyOneLevel(Point center, Point p1, List children) {
    //
    // int overlap = IGraphEditorConstants.NODE_SIZE + 10;
    // int numNode = children.size() + 2; // extra for sibling and parent
    // int radius = IGraphEditorConstants.NODE_SIZE * 4;
    // NodeElement node;
    // double dTheta = 360.0 / (double) numNode;
    // double thetaDeg, thetaRad, nextTheta, x, y;
    //
    // int next = 0;
    // // keep finding the place until got all of it
    // for (int i = 0; next < children.size(); i++) {
    // thetaDeg = 360.0 - (double) i * dTheta;
    // thetaRad = Math.toRadians(thetaDeg);
    // x = radius * Math.cos(thetaRad) + center.x;
    // y = radius * Math.sin(thetaRad) + center.y;
    //
    // if (Math.abs(x - p1.x) < overlap || Math.abs(y - p1.y) < overlap)
    // continue;
    //
    // if (next < children.size()) {
    // InternalNode par = (InternalNode) children.get(next);
    // next++;
    // node = par.getElement();
    // Point point = new Point(x, y);
    // node.setLocation(point);
    // node.setSize(new Dimension(IGraphEditorConstants.NODE_SIZE,
    // IGraphEditorConstants.NODE_SIZE));
    //
    // if (par.getNumOfChildren() > 0) {
    // Point p = this.applyOneLevel(point, center, par
    // .getChildren());
    // }
    // }
    // }
    //
    // thetaDeg = 360.0 - (double) children.size() * dTheta;
    // thetaRad = Math.toRadians(thetaDeg);
    // x = radius * Math.cos(thetaRad) + center.x;
    // y = radius * Math.sin(thetaRad) + center.y;
    // return new Point(x, y);
    // }
    /**
     * @see distributed.plugin.ui.models.topologies.ITopology#setConnections()
     */
    public void setConnections() {
        if (this.rootedTree)
            this.connectRootedTree();
        else
            this.connectTree();

    }

    private void connectRootedTree() {
        for (int i = this.tree.length - 1; i >= 0; i--) {
            NodeElement source = this.tree[i].getElement();
            for (int j = 0; j < this.tree[i].getNumOfChildren(); j++) {
                LinkElement link = this.nextLink();

                link.setSource(source);
                link.attachSource();

                NodeElement target = ((InternalNode) this.tree[i].getChild(j))
                        .getElement();
                link.setTarget(target);
                link.attachTarget();
            }
        }
    }

    private void connectTree() {
        // connect nodes along diameter
        for (int i = 0; i < this.tree.length - 1; i++) {
            NodeElement source = this.tree[i].getElement();
            LinkElement link = this.nextLink();
            link.setSource(source);
            link.attachSource();

            NodeElement target = this.tree[i + 1].getElement();
            link.setTarget(target);
            link.attachTarget();
        }
        
        // connect node for each subtree
        for(int i = 0; i < this.tree.length; i++){
            this.connectSubTree(this.tree[i]);
        }
    }
    
    private void connectSubTree(InternalNode par){
        NodeElement source = par.getElement();
        for(int j =0; j < par.getNumOfChildren(); j++){
            InternalNode target = par.getChild(j);
            this.connectSubTree(target);  
            this.connectNodes(source, target.getElement());
        }        
    }
    
    private void connectNodes(NodeElement par, NodeElement child){
        LinkElement link = this.nextLink();
        link.setSource(par);
        link.attachSource();

        link.setTarget(child);
        link.attachTarget();
    }
}
