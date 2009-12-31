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

import org.eclipse.gef.ui.actions.WorkbenchPartAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.EditorPart;

import distributed.plugin.ui.IGraphEditorConstants;
import distributed.plugin.ui.dialogs.AddStatesDialog;
import distributed.plugin.ui.dialogs.RemoveStatesDialog;
import distributed.plugin.ui.editor.GraphEditor;

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
    public StateSettingAction(EditorPart part, String type) {
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
        AddStatesDialog dialog = new AddStatesDialog(this.getShell());
        Object[] values = dialog.open();

        // cancel selected
        if (values.length < 2)
            return;

        // invalid input
        if (!this.validateStateInput(values))
            return;

        GraphEditor editor = (GraphEditor) getWorkbenchPart();
        Short state = new Short(Short.parseShort((String) values[0]));
        editor.getGraphElement().addStateColor(state, (RGB) values[1]);
        editor.makeDirty();

    }

    private boolean validateStateInput(Object[] param) {
        try {
            short s = Short.parseShort((String) param[0]);
        } catch (ClassCastException e) {
            MessageDialog.openError(this.getShell(), "Assign State Color",
                    param[0] + " is not a number");
        } catch (NumberFormatException e) {
            MessageDialog.openError(this.getShell(), "Assign State Color",
                    param[0] + " is not a number");
        }

        if (param[1] != null)
            return true;

        return false;
    }

    private void executeRemoveStates() {
        GraphEditor editor = (GraphEditor) getWorkbenchPart();
        //System.out.println("Size of states colors: " + 
        //        editor.getGraphElement().getGraph().getNumberOfState());
        
        RemoveStatesDialog dialog = new RemoveStatesDialog(this.getShell(),
                editor);
        dialog.open();

    }

}
