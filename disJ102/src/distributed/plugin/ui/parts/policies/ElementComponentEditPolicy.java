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
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

import distributed.plugin.ui.commands.DeleteNodeCommand;
import distributed.plugin.ui.models.GraphElement;
import distributed.plugin.ui.models.NodeElement;

/**
 * A Component Edit Role for <code>NodePart</code>
 */
public class ElementComponentEditPolicy extends ComponentEditPolicy {

    /**
     * Constructor
     */
    public ElementComponentEditPolicy() {
        super();
    }

    protected Command createDeleteCommand(GroupRequest request) {
        Object parent = getHost().getParent().getModel();
        Object element = getHost().getModel();

        if (element instanceof NodeElement) {
            //System.out.println("create deleteNode cmd " + element);
            DeleteNodeCommand cmd = new DeleteNodeCommand();
            cmd.setGraphElement((GraphElement) parent);
            cmd.setNodeElement((NodeElement) element);
            return cmd;
        } else {
            System.out.println("unknow element " + element);
            return null;
        }
    }

}
