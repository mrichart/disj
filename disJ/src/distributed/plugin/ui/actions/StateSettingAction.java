/*******************************************************************************
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     DisJ Development Group
 *******************************************************************************/

package distributed.plugin.ui.actions;

import java.util.Map;

import org.eclipse.gef.ui.actions.WorkbenchPartAction;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Shell;

import distributed.plugin.ui.IGraphEditorConstants;
import distributed.plugin.ui.dialogs.AddStatesDialog;
import distributed.plugin.ui.dialogs.RemoveStatesDialog;
import distributed.plugin.ui.editor.GraphEditor;
import distributed.plugin.ui.models.GraphElement;

/**
 * @author Me
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class StateSettingAction extends WorkbenchPartAction {

    private String execType;

    /**
     * @param part
     */
    public StateSettingAction(GraphEditor part, String type) {
        super(part);
        this.execType = type;
        setId(type);
        
    }

    private Shell getShell() {
        return getWorkbenchPart().getSite().getShell();
    }

    /**
     * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#calculateEnabled()
     */
    protected boolean calculateEnabled() {
        return this.canPerformAction();
    }

    /*
     * Validate the execution
     */
    private boolean canPerformAction() {
        if (getWorkbenchPart() instanceof GraphEditor) {
            GraphEditor editor = (GraphEditor) getWorkbenchPart();
            if (editor.getGraphElement() != null
                    && !editor.getEngine().isStarted())
                return true;
        }
        return false;
    }

    public void run() {     
        if (this.execType.equals(IGraphEditorConstants.ADD_STATE_ID)) {
            this.executeAddState();
            
        } else if (this.execType.equals(IGraphEditorConstants.REMOVE_STATE_ID)) {
            this.executeRemoveStates();
            
        }else {
            return;
        }
    }

    private void executeAddState() {
    	GraphEditor editor = (GraphEditor) getWorkbenchPart();
    	GraphElement ge = editor.getGraphElement();
    	Map<Short, RGB> stateColr = ge.getStateColors();
    	
    	 // display existing colors
        AddStatesDialog dialog = new AddStatesDialog(this.getShell(), stateColr);
              
        // accept new updated
        stateColr = dialog.open();

        // cancel selected
        if (stateColr == null)
            return;
        
        // ok selected
        if (stateColr != null){
	        // update data
	        ge.addStateColor(stateColr);
	        editor.makeDirty();
        }
    }

    private void executeRemoveStates() {
    	GraphEditor editor = (GraphEditor) getWorkbenchPart();
    	GraphElement ge = editor.getGraphElement();
    	Map<Short, RGB> stateColr = ge.getStateColors();
    	
   	 	// display existing colors
        RemoveStatesDialog dialog = new RemoveStatesDialog(this.getShell(),
                stateColr);
        
        // accept new updated
        stateColr = dialog.open();
        
        // ok selected
        if (stateColr != null){
	        // update data
        	ge.removeAllStateColor();
	        ge.addStateColor(stateColr);
	        editor.makeDirty();
        }

    }

}
