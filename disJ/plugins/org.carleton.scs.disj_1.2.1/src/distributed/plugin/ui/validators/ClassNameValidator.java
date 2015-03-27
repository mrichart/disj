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

import org.eclipse.jface.dialogs.IInputValidator;

/**
 * @author Me
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ClassNameValidator implements IInputValidator {

    /**
     * Constructor
     */
    public ClassNameValidator() {
        super();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IInputValidator#isValid(java.lang.String)
     */
    public String isValid(String newText) {
        if(newText == null)
            return "The input is null";
        
        if((newText.trim().equals("")))
            return "Class name cannot be blank";
        
        if(newText.trim().indexOf(" ") > -1)
            return "Class name cannot contains space";
        
        // check for special charector
        // return "It is invalid Class name in Java";
        return null;
    }

}
