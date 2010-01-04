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
 * Validate an input in propertySheet
 */
public class NumberCellEditorValidator implements ICellEditorValidator {
	
	private static NumberCellEditorValidator instance;
	
	// Singleton
	private NumberCellEditorValidator(){}

	public static NumberCellEditorValidator instance() {
		if (instance == null) 
			instance = new NumberCellEditorValidator();
		return instance;
	}

	/**
	 * @see org.eclipse.jface.viewers.ICellEditorValidator#isValid(java.lang.Object)
	 */
	public String isValid(Object value) {
		try {
			new Integer((String)value);
			return null;
		} catch (Exception e) {
			return IGraphEditorConstants.ERROR_NAN;
		}
	}
}
