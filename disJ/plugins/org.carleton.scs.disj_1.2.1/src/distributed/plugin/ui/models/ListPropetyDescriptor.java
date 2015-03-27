/*******************************************************************************
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     DisJ Development Group
 *******************************************************************************/

package distributed.plugin.ui.models;

import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import distributed.plugin.ui.dialogs.ListDialogCellEditor;

/**
 * @author Me
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ListPropetyDescriptor extends PropertyDescriptor {

	private List data;
	/**
	 * @param id
	 * @param displayName
	 * @param data
	 */
	public ListPropetyDescriptor(Object id, String displayName, List data) {
		super(id, displayName);
		this.data = data;
	}
	
	/**
	 * @see org.eclipse.ui.views.properties.IPropertyDescriptor#createPropertyEditor(Composite)
	 */
	public CellEditor createPropertyEditor(Composite parent) {
		CellEditor editor = new ListDialogCellEditor(this.data, parent);
		if (getValidator() != null)
			editor.setValidator(getValidator());
		return editor;
	}


}
