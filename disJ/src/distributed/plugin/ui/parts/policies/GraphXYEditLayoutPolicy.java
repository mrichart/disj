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

/**
 * @author daanish
 * @version 0.1
 */

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.CreateRequest;

import distributed.plugin.ui.commands.CreateNodeCommand;
import distributed.plugin.ui.commands.CreateTopologyCommand;
import distributed.plugin.ui.commands.SetConstraintCommand;
import distributed.plugin.ui.models.GraphElement;
import distributed.plugin.ui.models.NodeElement;
import distributed.plugin.ui.models.topologies.ITopology;
import distributed.plugin.ui.parts.NodePart;

public class GraphXYEditLayoutPolicy extends XYLayoutEditPolicy {

    protected Command createAddCommand(EditPart child, Object constraint) {
        // not support
        return null;
    }

    protected Command createChangeConstraintCommand(EditPart child,
            Object constraint) {
        SetConstraintCommand setConstraint = new SetConstraintCommand();
        setConstraint.setNodeElement(((NodePart) child).getNodeElement());
        setConstraint.setLocation((Rectangle) constraint);
        return setConstraint;
    }

    /**
     * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#createChildEditPolicy(EditPart)
     */
    protected EditPolicy createChildEditPolicy(EditPart child) {
        return new ResizableEditPolicy();
    }

    /**
     * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#getCreateCommand(org.eclipse.gef.requests.CreateRequest)
     */
    protected Command getCreateCommand(CreateRequest request) {
        Object obj = request.getNewObject();
        if (obj instanceof NodeElement) {
            NodeElement element = (NodeElement) obj;
            CreateNodeCommand create = this.createNodeElement(request, element);
            return create;
        } else if (obj instanceof ITopology) {
            ITopology topology = (ITopology) obj;
            CreateTopologyCommand create = this.createTopology(request,
                    topology);
            return create;
        } else {
            return null;
        }
    }

    protected Command getDeleteDependantCommand(Request request) {
        // not support
        return null;
    }

    /*
     * @param request @param element @return
     */
    private CreateNodeCommand createNodeElement(CreateRequest request,
            NodeElement element) {
        CreateNodeCommand create = new CreateNodeCommand();
        GraphElement parent = (GraphElement) getHost().getModel();
        create.setParent(parent);
        create.setChild(element);
        create.setLocation(request.getLocation());
        return create;
    }

    private CreateTopologyCommand createTopology(CreateRequest request,
            ITopology topology) {
        CreateTopologyCommand create = new CreateTopologyCommand(topology);
        GraphElement parent = (GraphElement) getHost().getModel();
        create.setParent(parent);
        create.setLocation(request.getLocation());
        return create;
    }

}