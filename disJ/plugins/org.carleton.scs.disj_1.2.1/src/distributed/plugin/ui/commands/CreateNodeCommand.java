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

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.Command;

import distributed.plugin.ui.IGraphEditorConstants;
import distributed.plugin.ui.models.GraphElement;
import distributed.plugin.ui.models.NodeElement;

/**
 * Command for creating the node as 2D
 * 
 * @version 0.1
 */
public class CreateNodeCommand extends Command {

    private GraphElement graph;

    private NodeElement node;

    private Point location;

    /**
     * Constructor
     */
    public CreateNodeCommand() {
        super(IGraphEditorConstants.CREATE_NODE_COMD);
        this.location = new Point(IGraphEditorConstants.NODE_SIZE,
                IGraphEditorConstants.NODE_SIZE);
    }

    public String getLabel() {
        return IGraphEditorConstants.CREATE_NODE_COMD;
    }

    public void setParent(final GraphElement parent) {
        this.graph = parent;
    }

    public void setChild(final NodeElement child) {
        this.node = child;
    }

    public void setLocation(Point point) {
        this.location = point;
    }

    // public void setInitialBounds(final Rectangle this.constraints) {
    // this.this.constraint = this.constraints;
    // }

    /**
     * TODO disable about resize
     * 
     * @see org.eclipse.gef.commands.Command#execute()
     */
    public void execute() {

        // Insets expansion = new Insets();
        // if (!this.constraint.isEmpty()) {
        // this.constraint.expand(expansion);
        // } else {
        // this.constraint.x -= expansion.left;
        // this.constraint.y -= expansion.top;
        // }

        this.node.setLocation(this.location);
        this.node.setSize(new Dimension(IGraphEditorConstants.NODE_SIZE,
                IGraphEditorConstants.NODE_SIZE));
        this.redo();

    }

    /**
     * @see org.eclipse.gef.commands.Command#redo()
     */
    public void redo() {
        this.graph.addNode(this.node.getNodeId(), this.node);
    }

    /**
     * @see org.eclipse.gef.commands.Command#undo()
     */
    public void undo() {
        this.graph.removeNode(this.node.getNodeId(), this.node);
    }
}
