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

import org.eclipse.gef.ui.actions.ActionBarContributor;
import org.eclipse.gef.ui.actions.DeleteRetargetAction;
import org.eclipse.gef.ui.actions.RedoRetargetAction;
import org.eclipse.gef.ui.actions.UndoRetargetAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionFactory;

import distributed.plugin.ui.IGraphEditorConstants;

/**
 * @author Me
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class GraphEditorActionBarContributor extends ActionBarContributor {
	
	/**
	 * Constructor
	 */
	public GraphEditorActionBarContributor() {
	    super();
	}

	/**
	 * @see org.eclipse.gef.ui.actions.ActionBarContributor#buildActions()
	 */
	protected void buildActions() {
		addRetargetAction(new UndoRetargetAction());
		addRetargetAction(new RedoRetargetAction());
		addRetargetAction(new DeleteRetargetAction());
		
		addRetargetAction(new LoadRetargetAction(IGraphEditorConstants.ACTION_LOAD, IGraphEditorConstants.LOAD));
		addRetargetAction(new LoadRandomRetargetAction(IGraphEditorConstants.ACTION_LOAD_RANDOM, IGraphEditorConstants.LOAD_RANDOM));
		addRetargetAction(new StepNextRetargetAction(IGraphEditorConstants.ACTION_STEP_NEXT, IGraphEditorConstants.NEXT));
		addRetargetAction(new ResumeRetargetAction(IGraphEditorConstants.ACTION_RESUME, IGraphEditorConstants.RESUME));
		addRetargetAction(new StopRetargetAction(IGraphEditorConstants.ACTION_STOP, IGraphEditorConstants.STOP));
		addRetargetAction(new SuspendRetargetAction(IGraphEditorConstants.ACTION_SUSPEND, IGraphEditorConstants.SUSPEND));
		addRetargetAction(new AddStateRetargetAction(IGraphEditorConstants.ADD_STATE_ID, IGraphEditorConstants.ADD_STATE));
		addRetargetAction(new RemoveStateRetargetAction(IGraphEditorConstants.REMOVE_STATE_ID, IGraphEditorConstants.REMOVE_STATE));
		addRetargetAction(new SpeedRetargetAction(IGraphEditorConstants.ACTION_SET_SPEED, IGraphEditorConstants.SPEED));
		addRetargetAction(new ReplayRegargetAction(IGraphEditorConstants.ACTION_REPLAY_RECORD,IGraphEditorConstants.REPLAY));
		//addRetargetAction(new SaveRecordRegargetAction(IGraphEditorConstants.ACTION_SAVE_RECORD,IGraphEditorConstants.SAVE_RECORD));
		
	}

	/**
	 * @see org.eclipse.gef.ui.actions.ActionBarContributor#declareGlobalActionKeys()
	 */
	protected void declareGlobalActionKeys() {
		addGlobalActionKey(ActionFactory.PRINT.getId());
		addGlobalActionKey(ActionFactory.SELECT_ALL.getId());
	}

	/**
	 * @see org.eclipse.ui.part.EditorActionBarContributor#contributeToToolBar(IToolBarManager)
	 */
	public void contributeToToolBar(IToolBarManager tbm) {

		tbm.add(getAction(ActionFactory.UNDO.getId()));
		tbm.add(getAction(ActionFactory.REDO.getId()));
		tbm.add(new Separator());
		tbm.add(getAction(IGraphEditorConstants.ADD_STATE_ID));
		tbm.add(getAction(IGraphEditorConstants.REMOVE_STATE_ID));
		tbm.add(new Separator());
		tbm.add(getAction(IGraphEditorConstants.ACTION_LOAD));
		tbm.add(getAction(IGraphEditorConstants.ACTION_LOAD_RANDOM));
		tbm.add(new Separator());
		tbm.add(getAction(IGraphEditorConstants.ACTION_RESUME));
		tbm.add(getAction(IGraphEditorConstants.ACTION_SUSPEND));
		tbm.add(getAction(IGraphEditorConstants.ACTION_STOP));		
		tbm.add(getAction(IGraphEditorConstants.ACTION_STEP_NEXT));
		tbm.add(new Separator());
		tbm.add(getAction(IGraphEditorConstants.ACTION_SET_SPEED));
		tbm.add(new Separator());
		tbm.add(getAction(IGraphEditorConstants.ACTION_REPLAY_RECORD));
		//tbm.add(getAction(IGraphEditorConstants.ACTION_SAVE_RECORD));
		
	}
	
	/**
	 * @see org.eclipse.ui.part.EditorActionBarContributor#contributeToMenu(IMenuManager)
	 */
	public void contributeToMenu(IMenuManager menubar) {
		super.contributeToMenu(menubar);
		MenuManager viewMenu = new MenuManager(IGraphEditorConstants.EXECUTE_MENU, IGraphEditorConstants.EXECUTE_MENU_ID);	
		viewMenu.add(getAction(IGraphEditorConstants.ADD_STATE_ID));
		viewMenu.add(getAction(IGraphEditorConstants.REMOVE_STATE_ID));
		viewMenu.add(getAction(IGraphEditorConstants.ACTION_LOAD));
		viewMenu.add(getAction(IGraphEditorConstants.ACTION_LOAD_RANDOM));
		
		viewMenu.add(new Separator("execute group"));		
		viewMenu.add(getAction(IGraphEditorConstants.ACTION_RESUME));
		viewMenu.add(getAction(IGraphEditorConstants.ACTION_SUSPEND));
		viewMenu.add(getAction(IGraphEditorConstants.ACTION_STOP));
		
		viewMenu.add(new Separator("extra grpup"));
		viewMenu.add(getAction(IGraphEditorConstants.ACTION_STEP_NEXT));
		viewMenu.add(getAction(IGraphEditorConstants.ACTION_SET_SPEED));

		menubar.insertAfter(IWorkbenchActionConstants.M_EDIT, viewMenu);
	}
	
}
