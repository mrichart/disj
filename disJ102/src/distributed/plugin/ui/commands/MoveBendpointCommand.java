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

import org.eclipse.draw2d.Bendpoint;

import distributed.plugin.ui.IGraphEditorConstants;
import distributed.plugin.ui.models.BendpointElement;

/**
 * @author Me
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class MoveBendpointCommand extends BendpointCommand {

    private Bendpoint oldBendpoint;

    /**
     * 
     */
    public MoveBendpointCommand() {
        super();
        setLabel(IGraphEditorConstants.ADJUST_LINK_COMD);
    }
    
    public String getLabel(){
        return IGraphEditorConstants.ADJUST_LINK_COMD;
    }

    public void execute() {
        BendpointElement bp = new BendpointElement();
        bp.setRelativeDimensions(getFirstRelativeDimension(),
                getSecondRelativeDimension());
        setOldBendpoint((Bendpoint) getConnectionModel().getBendpoints().get(
                getIndex()));
        getConnectionModel().setBendpoint(getIndex(), bp);
        super.execute();
    }

    protected Bendpoint getOldBendpoint() {
        return oldBendpoint;
    }

    public void setOldBendpoint(Bendpoint bp) {
        oldBendpoint = bp;
    }

    public void undo() {
        super.undo();
        getConnectionModel().setBendpoint(getIndex(), getOldBendpoint());
    }

}
