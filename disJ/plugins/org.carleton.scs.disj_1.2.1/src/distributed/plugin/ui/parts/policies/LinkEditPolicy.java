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
import org.eclipse.gef.editpolicies.ConnectionEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

import distributed.plugin.ui.commands.DeleteLinkCommand;
import distributed.plugin.ui.models.LinkElement;

/**
 * @author Me
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class LinkEditPolicy extends ConnectionEditPolicy {

    /**
     * Constructor
     */
    public LinkEditPolicy() {
        super();
    }

    /**
     * @see org.eclipse.gef.editpolicies.ConnectionEditPolicy#getDeleteCommand(org.eclipse.gef.requests.GroupRequest)
     */
    protected Command getDeleteCommand(GroupRequest request) {
//        ConnectionCommand c = new ConnectionCommand();
//        c.setLinkElement((LinkElement) getHost().getModel());
//        return c; if (element instanceof LinkElement) {

        Object element = getHost().getModel();
        if (element instanceof LinkElement) {
            //System.out.println("deleteLink cmd " + element);
            DeleteLinkCommand cmd = new DeleteLinkCommand();
            cmd.setLinkElement((LinkElement) element);
            return cmd;
        }else {
            System.out.println("unknow element " + element);
            return null;
        }
    }

}
