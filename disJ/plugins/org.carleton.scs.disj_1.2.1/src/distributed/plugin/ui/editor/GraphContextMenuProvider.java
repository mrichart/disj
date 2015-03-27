/*******************************************************************************
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     DisJ Development Group
 *******************************************************************************/

package distributed.plugin.ui.editor;

import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.actions.ActionFactory;

/**
 * @author Me
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class GraphContextMenuProvider extends ContextMenuProvider {

    private ActionRegistry actionRegistry;

    /**
     * Constructor
     * 
     * @param viewer
     */
    public GraphContextMenuProvider(EditPartViewer viewer,
            ActionRegistry registry) {
        super(viewer);
        this.actionRegistry = registry;
    }

    /**
     * @see org.eclipse.gef.ContextMenuProvider#buildContextMenu(org.eclipse.jface.action.IMenuManager)
     */
    public void buildContextMenu(IMenuManager menu) {
        
        GEFActionConstants.addStandardActionGroups(menu);

        IAction action;

        action = this.actionRegistry.getAction(ActionFactory.UNDO.getId());
        menu.appendToGroup(GEFActionConstants.GROUP_UNDO, action);

        action = this.actionRegistry.getAction(ActionFactory.REDO.getId());
        menu.appendToGroup(GEFActionConstants.GROUP_UNDO, action);

        action = this.actionRegistry.getAction(ActionFactory.DELETE.getId());
        if (action.isEnabled())
            menu.appendToGroup(GEFActionConstants.GROUP_EDIT, action);

        action = this.actionRegistry.getAction(ActionFactory.SAVE.getId());
        menu.appendToGroup(GEFActionConstants.GROUP_SAVE, action);

    }

}
