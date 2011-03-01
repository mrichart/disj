/*******************************************************************************
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     DisJ Development Group
 *******************************************************************************/

package distributed.plugin.ui;

import org.eclipse.swt.graphics.Color;


/**
 * @author Me
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public interface IGraphEditorConstants {

	// menu constants
	public String GraphicalEditor_FILE_DELETED_TITLE_UI = "File Deleted"; //$NON-NLS-1$

	public String GraphicalEditor_FILE_DELETED_WITHOUT_SAVE_INFO = "The file has been deleted from the file system. " +
			"Do you want to save your changes or close the editor without saving?";//$NON-NLS-1$

	public String GraphicalEditor_SAVE_BUTTON_UI = "Save"; //$NON-NLS-1$

	public String GraphicalEditor_CLOSE_BUTTON_UI = "Close"; //$NON-NLS-1$

	// Default node background color
	public static Color DEFAULT_NODE_COLOR = new Color(null, 100, 150, 50);



	// drawing tool constants
	public static final String SEPARATOR_ONE = "GraphEditor.separator.one";
	
	public static final String CONTROL_GROUP_LABEL = "Control Group";
	public static final String BREAK_POINT = "Breakpoint";
	public static final String BREAK_POINT_DESC = "Put a breakpoint at a specific node";
	
	public static final String DRAW_COMPONENTS = "Drawing";	
	public static final String DRAW_NODE = "Node";
	public static final String DRAW_NODE_DESC = "Draw a node";	
	public static final String DRAW_UNI_LINK ="Uni-link";	
	public static final String DRAW_UNI_LINK_DESC = "Draw a uni-directional link";	
	public static final String DRAW_BI_LINK = "Bi-link";	
	public static final String DRAW_BI_LINK_DESC = "Draw a bi-directional link";	
	
	public static final String TOPOLOGY_TYPES ="Topology";
	public static final String RING = "Ring";
	public static final String RING_DESC = "Draw a ring network";
	public static final String TREE = "Tree";
	public static final String TREE_DESC = "Draw a tree network";
	public static final String COMPLETE = "Complete";
	public static final String COMPLETE_DESC = "Draw a complete network";	
	public static final String HYPER_CUBE = "Hyper Cube";
	public static final String HYPER_CUBE_DESC = "Draw a hyper cube network";
	public static final String MESH = "Mesh";
	public static final String MESH_DESC = "Draw a mesh network";
	public static final String TORUS_STACK = "Torus";
	public static final String TORUS_STACK_DESC = "Draw Torus network";
	public static final String TORUS_1 = "Torus_1";
	public static final String TORUS_1_DESC = "Draw a Torus network";
	public static final String TORUS_2 = "Torus_2";
	public static final String TORUS_2_DESC = "Draw a Torus network";
	public static final String RANDOM_STACK = "Random Generic";
	public static final String RANDOM_STACK_DESC = "Draw random generic graph network";
	public static final String FOREST = "Forest";
	public static final String FORREST_DESC = "Draw a random forest network";
	public static final String CONNECTED = "Connected";
	public static final String CONNECTED_DESC = "Draw a random connected network";
	public static final String SPATIAL = "Spatial";
	public static final String SPATIAL_DESC = "Draw a random spatial triangulation polygon";
	
	// action bar constants
	public static final String EXECUTE_MENU = "Simulator";
	public static final String SUB_MENU = "Debug Menu";
	public static final String RESUME = "Run/Resume";
	public static final String SUSPEND = "Suspend";
	public static final String STOP = "Stop";
	public static final String LOAD_PROTOCOL = "Load Distributed Protocol";
	public static final String LOAD_RANDOM = "Load Random Generator";
	public static final String LOAD_ADVERSARY = "Load Adversary";
	public static final String NEXT = "Step Next";
	public static final String ADD_STATE = "Add States";
	public static final String REMOVE_STATE = "Remove/View States";
	public static final String SPEED = "Process Speed";
	public static final String REPLAY = "Replay Record";
	public static final String SAVE_RECORD = "Save the last run";
	
	public static final String EXECUTE_MENU_ID = "IGraphEditorConstants.Simulator";
	public static final String SUB_MENU_ID = "IGraphEditorConstants.Debug_Menu";
	public static final String ACTION_RESUME = "IGraphEditorConstants.Resume";
	public static final String ACTION_SUSPEND = "IGraphEditorConstants.Suspend";
	public static final String ACTION_STOP = "IGraphEditorConstants.Stop";
	public static final String ACTION_LOAD = "IGraphEditorConstants.Load_Java_Class";
	public static final String ACTION_LOAD_RANDOM = "IGraphEditorConstants.Load_Random";
	public static final String ACTION_LOAD_ADVERSARY = "IGraphEditorConstants.Load_Adversary";
	public static final String ACTION_STEP_NEXT = "IGraphEditorConstants.Step_Next";
	public static final String ADD_STATE_ID = "IGraphEditorConstants.Add_State";
	public static final String REMOVE_STATE_ID = "IGraphEditorConstants.Remove_State";
	public static final String ACTION_SET_SPEED = "IGraphEditorConstants.Process_Speed";
	public static final String ACTION_REPLAY_RECORD ="IGraphEditorConstants.Replay_Record";
	public static final String ACTION_SAVE_RECORD="IGraphEditorConstants.Save_Recording";
	
	// command template
	public static final String TEMPLATE_NODE = "0";
	public static final String TEMPLATE_UNI_LINK = "1";
	public static final String TEMPLATE_BI_LINK = "2";
	public static final String TEMPLATE_BREAKPOINT = "3";
	public static final String TEMPLATE_RING = "4";
	public static final String TEMPLATE_TREE = "5";
	public static final String TEMPLATE_MESH = "6";
	public static final String TEMPLATE_COMPLETE = "7";
	public static final String TEMPLATE_HYPER_CUBE = "8";
	public static final String TEMPLATE_TORUS_1 = "9";
	public static final String TEMPLATE_TORUS_2 = "10";
	public static final String TEMPLATE_GENERIC = "11";
	public static final String TEMPLATE_CONNECTED = "12";
	public static final String TEMPLATE_SPATIAL = "13";
	
	
	// command label
	public static final String CREATE_NODE_COMD = "Create Node";
	public static final String CONNECTION_COMD = "Connect Link";	
	public static final String DELETE_NODE_COMD = "Delete Node";
	public static final String DELETE_EDGE_COMD = "Delete Link";
	public static final String MOVE_NODE_COMD = "Move";
	public static final String ADJUST_LINK_COMD = "Adjust Link";
	public static final String CREATE_RING_COMD = "Create Ring";
	public static final String CREATE_TREE_COMD = "Create Tree";
	public static final String CREATE_COMPLET_COMD = "Create Complete Graph";
	public static final String CREATE_MESH_COMD = "Create Mesh";
	public static final String CREATE_TORUS_COMD ="Create Torus";
	public static final String CREATE_HYPECUBE_COMD = "Create HyperCube";
	public static final String CREATE_GENERIC_COMD = "Create Generic Graph";
	public static final String CREATE_SPATIAL_COMD = "Create Spatial Graph";
	
	// general constants
	public static final int NODE_SIZE = 30;
	public static final String CONTEXT_MENU = "distributed.plugin.ui.actions";
	public static final String ERROR_NAN = "It has to be an integer number";
	public static final String ERROR_NEGATIVE = "It has to be positive integer number";
	public static final String ERROR_EMPTY_TEXT = "It cannot be an empty string";
	public static final String ERROR_OUTOF_RAND = "The range must be between 0 - 100";
	public static final String DISJ_CONSOLE = "DisJ Console";
	
    // message direction types
    public static final String UNI = "Uni-Dirctional";

    public static final String BI = "Bi-Directional";
    
    // Record File constant
    public static final String STATE_CHANGE=" changes_its_state_to ";
	public static final String LINK_VISIBILITY_CHANGE = " changes_its_visibility_to ";
	
	
	

}
