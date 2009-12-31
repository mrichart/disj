/*******************************************************************************
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     DisJ Development Group
 *******************************************************************************/

package distributed.plugin.ui.parts.policies;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;

import distributed.plugin.ui.IGraphEditorConstants;
import distributed.plugin.ui.commands.ConnectionCommand;
import distributed.plugin.ui.models.BiLinkElement;
import distributed.plugin.ui.models.GraphElement;
import distributed.plugin.ui.models.LinkElement;
import distributed.plugin.ui.models.NodeElement;
import distributed.plugin.ui.models.UniLinkElement;
import distributed.plugin.ui.parts.NodePart;

/**
 * A Graphical Node Role for <code>NodePart</code>
 * 
 * @version 0.1
 */
public class NodeGraphicalEditPolicy extends GraphicalNodeEditPolicy {

    /**
     * Constructor
     */
    public NodeGraphicalEditPolicy() {
        super();
    }

    /**
     * TODO setup all necessary data for execution
     * 
     * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#getConnectionCompleteCommand(org.eclipse.gef.requests.CreateConnectionRequest)
     */
    protected Command getConnectionCompleteCommand(
            CreateConnectionRequest request) {

        ConnectionCommand command = (ConnectionCommand) request
                .getStartCommand();
        command.setTarget(this.getNodeElement());

        return command;
    }

    /**
     * TODO setup all necessary data for execution
     * 
     * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#getConnectionCreateCommand(org.eclipse.gef.requests.CreateConnectionRequest)
     */
    protected Command getConnectionCreateCommand(CreateConnectionRequest request) {

        LinkElement link;
        Object type = request.getNewObjectType();
        
        if (type.equals(IGraphEditorConstants.TEMPLATE_UNI_LINK))
            link = (UniLinkElement) request.getNewObject();
        else if (type.equals(IGraphEditorConstants.TEMPLATE_BI_LINK))
            link = (BiLinkElement) request.getNewObject();
        else {
            System.out
                    .println("--- unknown link type @NodeGraphicalEditPolicy.getConnectionCreatedCommand()");
            link = null;
        }

        ConnectionCommand command = new ConnectionCommand();
        command.setParent(this.getGraphElement());
        command.setLinkElement(link);
        command.setSource(this.getNodeElement());
        request.setStartCommand(command);
        return command;
    }

    /**
     * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#getReconnectTargetCommand(org.eclipse.gef.requests.ReconnectRequest)
     */
    protected Command getReconnectTargetCommand(ReconnectRequest request) {

        ConnectionCommand cmd = new ConnectionCommand();

        cmd.setParent(this.getGraphElement());
        cmd.setLinkElement((LinkElement) request.getConnectionEditPart()
                .getModel());
        cmd.setTarget(this.getNodeElement());

//        System.out
//                .println("[NodeGraphicalEditPolicy] getReconnectTargetCommand() with targetNoe = "
//                        + this.getNodeElement());

        return cmd;
    }

    /**
     * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#getReconnectSourceCommand(org.eclipse.gef.requests.ReconnectRequest)
     */
    protected Command getReconnectSourceCommand(ReconnectRequest request) {

        ConnectionCommand cmd = new ConnectionCommand();

        cmd.setParent(this.getGraphElement());
        cmd.setLinkElement((LinkElement) request.getConnectionEditPart()
                .getModel());
        cmd.setSource(this.getNodeElement());

//        System.out
//                .println("[NodeGraphicalEditPolicy] getReconnectSourceCommand() with sourceNode="
//                        + this.getNodeElement());

        return cmd;
    }

    /**
     * Get a corresponding editPart to this policy
     * 
     * @return
     */
    protected NodePart getGraphNodeEditPart() {
        return (NodePart) this.getHost();
    }

    /**
     * Get a corresponding model element to this policy
     * 
     * @return
     */
    public NodeElement getNodeElement() {
        return (NodeElement) this.getHost().getModel();
    }

    public GraphElement getGraphElement() {
        return (GraphElement) this.getHost().getParent().getModel();
    }

}
