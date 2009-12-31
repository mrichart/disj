/*******************************************************************************
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     DisJ Development Group
 *******************************************************************************/

package distributed.plugin.ui.validators;

import org.eclipse.jface.viewers.ICellEditorValidator;

import distributed.plugin.ui.IGraphEditorConstants;

/**
 * @author Me
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class EmptyCellEditorValidator implements ICellEditorValidator {

	private static EmptyCellEditorValidator instance;
	
	// Singleton
	private EmptyCellEditorValidator(){}

	public static EmptyCellEditorValidator instance() {
		if (instance == null) 
			instance = new EmptyCellEditorValidator();
		return instance;
	}

	/**
	 * @see org.eclipse.jface.viewers.ICellEditorValidator#isValid(java.lang.Object)
	 */
	public String isValid(Object value) {
		if(((String)value).trim().equals(""))
			return IGraphEditorConstants.ERROR_EMPTY_TEXT;
		else
			return null;
	}

}
