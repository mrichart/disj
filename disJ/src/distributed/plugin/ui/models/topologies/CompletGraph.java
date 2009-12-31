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
import distributed.plugin.ui.dialogs.CompleteGraphDialog;
import distributed.plugin.ui.models.GraphElementFactory;
import distributed.plugin.ui.models.LinkElement;
import distributed.plugin.ui.models.NodeElement;

/**
 * @author Me
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class CompletGraph implements ITopology {

    private static final int CONSTANT = 4;

    private static final int BASE = IGraphEditorConstants.NODE_SIZE * 4;

    private int radius;

    private int numberOfNode;

    private String linkType;

    private List nodes;

    private List links;

    private Shell shell;

    private GraphElementFactory factory;

    /**
     * Constructor;
     */
    public CompletGraph(GraphElementFactory factory, Shell shell) {
        this.factory = factory;
        this.shell = shell;
        CompleteGraphDialog dialog = new CompleteGraphDialog(this.shell);
        dialog.open();
        this.numberOfNode = dialog.getNumNode();
        this.linkType = dialog.getLinkType();
        this.radius = this.calculateRadius(this.numberOfNode);
        this.nodes = new ArrayList();
        this.links = new ArrayList();
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

        for (int i = 0; i < this.numberOfNode; i++) {
            this.nodes.add(this.factory.createNodeElement());
        }

        // Uni-Complete Graph: num of link == num of node * num of node-1
        if (this.linkType.equals(IGraphEditorConstants.UNI)) {
            for (int i = 0; i < this.numberOfNode; i++) {
                for (int j = 0; j < this.numberOfNode - 1; j++)
                    this.links.add(this.factory.createUniLinkElement());
            }
        }

        // Bi-CompleteGraph: sum of (n-1 + n-2 +...+ 2 + 1)
        if (this.linkType.equals(IGraphEditorConstants.BI)) {
            for (int i = 0; i < this.numberOfNode - 1; i++) {
                for (int j = 0; j < i + 1; j++)
                    this.links.add(this.factory.createBiLinkElement());
            }
        }
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
        NodeElement node;
        double dTheta = 360.0 / (double) this.numberOfNode;
        double thetaDeg, thetaRad, x, y;

        for (int i = 0; i < this.nodes.size(); i++) {
            thetaDeg = 360.0 - (double) i * dTheta;
            thetaRad = Math.toRadians(thetaDeg);
            x = radius * Math.cos(thetaRad) + point.x;
            y = radius * Math.sin(thetaRad) + point.y;

            node = (NodeElement) this.nodes.get(i);
            node.setLocation(new Point(x, y));
            node.setSize(new Dimension(IGraphEditorConstants.NODE_SIZE,
                    IGraphEditorConstants.NODE_SIZE));
        }
    }

    /**
     * @see distributed.plugin.ui.models.topologies.ITopology#getName()
     */
    public String getName() {
        return IGraphEditorConstants.CREATE_COMPLET_COMD;
    }

    /**
     * @see distributed.plugin.ui.models.topologies.ITopology#setConnections()
     */
    public void setConnections() {
        int count = -1;
        if (this.linkType.equals(IGraphEditorConstants.BI)) {
            for (int i = 0; i < this.nodes.size(); i++) {
                NodeElement source = (NodeElement) this.nodes.get(i);
                for (int j = i + 1; j < this.nodes.size(); j++) {
                    LinkElement link = (LinkElement) this.links.get(++count);
                    NodeElement target = (NodeElement) this.nodes.get(j);
                    link.setSource(source);
                    link.attachSource();
                    link.setTarget(target);
                    link.attachTarget();
                }
            }
        }

        if (this.linkType.equals(IGraphEditorConstants.UNI)) {
            if (this.linkType.equals(IGraphEditorConstants.UNI)) {
                for (int i = 0; i < this.numberOfNode; i++) {
                    NodeElement source = (NodeElement) this.nodes.get(i);
                    for (int j = 0; j < this.numberOfNode; j++)
                        if (i != j) {
                            LinkElement link = (LinkElement) this.links
                                    .get(++count);
                            NodeElement target = (NodeElement) this.nodes
                                    .get(j);
                            link.setSource(source);
                            link.attachSource();
                            link.setTarget(target);
                            link.attachTarget();
                        }
                }
            }
        }
    }

}
