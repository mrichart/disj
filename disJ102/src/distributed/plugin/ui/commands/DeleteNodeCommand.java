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

import org.eclipse.gef.commands.Command;

import distributed.plugin.ui.IGraphEditorConstants;
import distributed.plugin.ui.models.GraphElement;
import distributed.plugin.ui.models.LinkElement;
import distributed.plugin.ui.models.NodeElement;

/**
 * @author Me
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class DeleteNodeCommand extends Command {

    private GraphElement graphElement;

    private NodeElement nodeElement;

    private List sourceConnections;

    private List targetConnections;

    /**
     * Constructor
     */
    public DeleteNodeCommand() {
        super(IGraphEditorConstants.DELETE_NODE_COMD);
    }
    
    public String getLabel(){
        return IGraphEditorConstants.DELETE_NODE_COMD;
    }

    /**
     * Set a graph owner of the deleting node
     * @param element
     */
    public void setGraphElement(GraphElement element) {
        this.graphElement = element;
    }

    /**
     * Set a node that want to delete
     * @param element
     */
    public void setNodeElement(NodeElement element) {
        this.nodeElement = element;
        this.sourceConnections = this.nodeElement.getSourceConnections();
        this.targetConnections = this.nodeElement.getTargetConnections();
    }

    public void execute() {
        this.deleteConnections();
        this.deleteNode();
    }

    public void redo() {
        this.execute();
    }

    public void undo() {
        this.graphElement.addNode(this.nodeElement.getNodeId(),
                this.nodeElement);
        this.restoreConnections();

    }
    
    private void deleteConnections() {

        // due to the list also shrink while we are detaching
        for (int i = 0; !this.sourceConnections.isEmpty(); ) {
            LinkElement wire = (LinkElement) this.sourceConnections.get(i);
            wire.detachSource();
            wire.detachTarget();
            this.graphElement.removeEdge(wire.getEdgeId(), wire);
        }

        for (int i = 0; !this.targetConnections.isEmpty(); ) {
            LinkElement wire = (LinkElement) this.targetConnections.get(i);
            wire.detachSource();
            wire.detachTarget();
            this.graphElement.removeEdge(wire.getEdgeId(), wire);
        }
    }

    private void deleteNode() {
        this.graphElement.removeNode(nodeElement.getNodeId(), nodeElement);
    }

    private void restoreConnections() {
        for (int i = 0; i < this.sourceConnections.size(); i++) {
            LinkElement wire = (LinkElement) this.sourceConnections.get(i);
            wire.attachSource();
            wire.attachTarget();
        }
        this.sourceConnections.clear();
        for (int i = 0; i < this.targetConnections.size(); i++) {
            LinkElement wire = (LinkElement) this.targetConnections.get(i);
            wire.attachSource();
            wire.attachTarget();
        }
        this.targetConnections.clear();
    }

}
