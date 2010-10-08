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

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.actions.RetargetAction;

import distributed.plugin.ui.GraphEditorPlugin;

/**
 * @author Me
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class LoadRetargetAction extends RetargetAction {

    /**
     * @param actionID
     * @param text
     */
    public LoadRetargetAction(String actionID, String text) {
        super(actionID, text);     
//        	setImageDescriptor(
//        	ImageDescriptor.createFromFile(GraphEditor.class,"icons/load_en.gif"));
        
        	try {
				final URL installUrl = GraphEditorPlugin.getDefault().getBundle().getEntry("/");
				final URL imageUrl = new URL(installUrl, "icons/load.png");
				setImageDescriptor(ImageDescriptor.createFromURL(imageUrl));
				
			} catch (MalformedURLException e) {					
			}
			setToolTipText(text);
			setText(text);
			setDescription(text);
    }

}
