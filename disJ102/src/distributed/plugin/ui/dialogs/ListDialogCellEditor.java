/*******************************************************************************
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     DisJ Development Group
 *******************************************************************************/

package distributed.plugin.ui.dialogs;

import java.util.List;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author Me
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ListDialogCellEditor extends DialogCellEditor {

	private List data;
	
	/**
	 * 
	 */
	public ListDialogCellEditor(List data) {
		this.data = data;
	}

	/**
	 * @param parent
	 */
	public ListDialogCellEditor(List data, Composite parent) {
		super(parent);
		this.data = data;
	}

	/**
	 * @param parent
	 * @param style
	 */
	public ListDialogCellEditor(Composite parent, int style) {
		super(parent, style);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.DialogCellEditor#openDialogBox(org.eclipse.swt.widgets.Control)
	 */
	protected Object openDialogBox(Control cellEditorWindow) {
		// TODO add a list of data into the control
		cellEditorWindow.setData(this.data);
		return null;
	}

}
