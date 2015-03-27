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

import distributed.plugin.ui.IGraphEditorConstants;
import distributed.plugin.ui.models.BendpointElement;

/**
 * @author Me
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class CreateBendpointCommand extends BendpointCommand {

    /**
     * Constructor
     */
    public CreateBendpointCommand() {
        super();
        setLabel(IGraphEditorConstants.ADJUST_LINK_COMD);
    }
    
    public String getLabel(){
        return IGraphEditorConstants.ADJUST_LINK_COMD;
    }

    public void execute() {
        BendpointElement wbp = new BendpointElement();
        wbp.setRelativeDimensions(getFirstRelativeDimension(),
                getSecondRelativeDimension());
        getConnectionModel().insertBendpoint(getIndex(), wbp);
        super.execute();
    }

    public void undo() {
        super.undo();
        getConnectionModel().removeBendpoint(getIndex());
    }

}
