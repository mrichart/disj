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

/**
 * @author Me
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class DeleteBendpointCommand extends BendpointCommand {

    private Bendpoint bendpoint;

    /**
     * Constructor
     */
    public DeleteBendpointCommand() {
        super();
        setLabel(IGraphEditorConstants.ADJUST_LINK_COMD);
    }
    
    public String getLabel(){
        return IGraphEditorConstants.ADJUST_LINK_COMD;
    }

    public void execute() {
        bendpoint = (Bendpoint) getConnectionModel().getBendpoints().get(
                getIndex());
        getConnectionModel().removeBendpoint(getIndex());
        super.execute();
    }

    public void undo() {
        super.undo();
        getConnectionModel().insertBendpoint(getIndex(), bendpoint);
    }

}
