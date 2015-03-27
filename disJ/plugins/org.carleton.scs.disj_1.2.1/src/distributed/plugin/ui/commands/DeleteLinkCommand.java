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

import org.eclipse.gef.commands.Command;

import distributed.plugin.ui.IGraphEditorConstants;
import distributed.plugin.ui.models.LinkElement;

/**
 * @author Me
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class DeleteLinkCommand extends Command {

    private LinkElement linkElement;

    /**
     * Constructor
     */
    public DeleteLinkCommand() {
        super(IGraphEditorConstants.DELETE_EDGE_COMD);
    }
    
    public String getLabel(){
        return IGraphEditorConstants.DELETE_EDGE_COMD;
    }

    public void setLinkElement(LinkElement element) {
        this.linkElement = element;
    }

    public void execute() {
        this.deleteConnections();
    }

    public void redo() {
        this.execute();
    }

    public void undo() {
        this.restoreConnections();

    }

    private void deleteConnections() {
        this.linkElement.detachSource();
        this.linkElement.detachTarget();
    }

    private void restoreConnections() {
        this.linkElement.attachSource();
        this.linkElement.attachTarget();
    }
}
