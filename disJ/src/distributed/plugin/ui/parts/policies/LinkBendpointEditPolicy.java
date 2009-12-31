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

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.BendpointEditPolicy;
import org.eclipse.gef.requests.BendpointRequest;

import distributed.plugin.ui.commands.BendpointCommand;
import distributed.plugin.ui.commands.CreateBendpointCommand;
import distributed.plugin.ui.commands.DeleteBendpointCommand;
import distributed.plugin.ui.commands.MoveBendpointCommand;
import distributed.plugin.ui.models.LinkElement;

/**
 * @author Me
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class LinkBendpointEditPolicy extends BendpointEditPolicy {

    /**
     * Constructor
     */
    public LinkBendpointEditPolicy() {
        super();
    }

    /**
     * This edit policy's method is called when a bendpoint creation is caused
     * by user interactions.
     * 
     * @see org.eclipse.gef.editpolicies.BendpointEditPolicy#getCreateBendpointCommand(org.eclipse.gef.requests.BendpointRequest)
     */
    protected Command getCreateBendpointCommand(BendpointRequest request) {
//        System.out
//                .println("[LinkBendpointEditPolicy].getCreateBendpointCommand() with req="
//                        + request);

        CreateBendpointCommand com = new CreateBendpointCommand();
        Point p = request.getLocation();
        Connection conn = getConnection();

        conn.translateToRelative(p);

        com.setLocation(p);
        Point ref1 = getConnection().getSourceAnchor().getReferencePoint();
        Point ref2 = getConnection().getTargetAnchor().getReferencePoint();

        conn.translateToRelative(ref1);
        conn.translateToRelative(ref2);

        com.setRelativeDimensions(p.getDifference(ref1), p.getDifference(ref2));
        com.setConnectionModel((LinkElement) request.getSource().getModel());
        com.setIndex(request.getIndex());
        return com;
    }

    /**
     * This edit policy's method is called when a bendpoint deletion is caused
     * by user interactions.
     * 
     * @see org.eclipse.gef.editpolicies.BendpointEditPolicy#getDeleteBendpointCommand(org.eclipse.gef.requests.BendpointRequest)
     */
    protected Command getDeleteBendpointCommand(BendpointRequest request) {
//        System.out
//                .println("[LinnkBendpointEditPolicy].getDeleteBendpointCommand() with req="
//                        + request);

        BendpointCommand com = new DeleteBendpointCommand();
        Point p = request.getLocation();
        com.setLocation(p);
        com.setConnectionModel((LinkElement) request.getSource().getModel());
        com.setIndex(request.getIndex());
        return com;
    }

    /**
     * This edit policy's method is called when a bendpoint displacement is
     * caused by user interactions.
     * 
     * @see org.eclipse.gef.editpolicies.BendpointEditPolicy#getMoveBendpointCommand(org.eclipse.gef.requests.BendpointRequest)
     */
    protected Command getMoveBendpointCommand(BendpointRequest request) {
//        System.out
//                .println("[LinnkBendpointEditPolicy].getMoveBendpointCommand() with req="
//                        + request);

        MoveBendpointCommand com = new MoveBendpointCommand();
        Point p = request.getLocation();
        Connection conn = getConnection();

        conn.translateToRelative(p);

        com.setLocation(p);

        Point ref1 = getConnection().getSourceAnchor().getReferencePoint();
        Point ref2 = getConnection().getTargetAnchor().getReferencePoint();

        conn.translateToRelative(ref1);
        conn.translateToRelative(ref2);

        com.setRelativeDimensions(p.getDifference(ref1), p.getDifference(ref2));
        com.setConnectionModel((LinkElement) request.getSource().getModel());
        com.setIndex(request.getIndex());
        return com;
    }

}
