/*******************************************************************************
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     DisJ Development Group
 *******************************************************************************/

package distributed.plugin.ui.commands;

import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.Command;

import distributed.plugin.ui.models.GraphElement;
import distributed.plugin.ui.models.LinkElement;
import distributed.plugin.ui.models.NodeElement;
import distributed.plugin.ui.models.topologies.ITopology;

/**
 * @author Me
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class CreateTopologyCommand extends Command {

    private GraphElement graphElement;

    private ITopology topology;

    private Point location;

    /**
     * @param topology
     *            a topology that it's going to be created on UI
     */
    public CreateTopologyCommand(ITopology topology) {
        super(topology.getName());
        this.topology = topology;
        this.location = new Point(0, 0);
    }

    public void execute() {
        this.redo();
    }

    /**
     * @see org.eclipse.gef.commands.Command#redo()
     */
    public void redo() {
        this.topology.applyLocation(this.location);

        List nodes = this.topology.getAllNodes();
        for (int i = 0; i < nodes.size(); i++) {
            NodeElement node = (NodeElement) nodes.get(i);
            this.graphElement.addNode(node.getNodeId(), node);
        }

        this.topology.setConnections();

        List links = this.topology.getAllLinks();
        for (int i = 0; i < links.size(); i++) {
            LinkElement link = (LinkElement) links.get(i);
            this.graphElement.addEdge(link.getEdgeId(), link);
        }
    }

    /**
     * @see org.eclipse.gef.commands.Command#undo()
     */
    public void undo() {
        List nodes = this.topology.getAllNodes();
        for (int i = 0; i < nodes.size(); i++) {
            NodeElement node = (NodeElement) nodes.get(i);
            this.graphElement.removeNode(node.getNodeId(), node);
        }

        List links = this.topology.getAllLinks();
        for (int i = 0; i < links.size(); i++) {
            LinkElement link = (LinkElement) links.get(i);
            link.detachSource();
            link.detachTarget();
            this.graphElement.removeEdge(link.getEdgeId(), link);
        }

    }

    public void setParent(final GraphElement parent) {
        this.graphElement = parent;
    }

    public void setLocation(Point point) {
        this.location = point;
    }

}
