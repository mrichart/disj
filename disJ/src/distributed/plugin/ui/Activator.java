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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class Activator extends AbstractUIPlugin {
	
	public static final String PLUGIN_ID = "distributed.plugin.ui.editor.GraphEditor"; //$NON-NLS-1$
	
	//The shared instance.
	private static Activator plugin;
	
	//Resource bundle.
	//private ResourceBundle resourceBundle;
	
	/**
	 * The constructor.
	 */
	public Activator() {
		super();
		plugin = this;
//		try {
//			resourceBundle = ResourceBundle.getBundle("distributed.plugin.ui.ActivatorResources");
//		} catch (MissingResourceException x) {
//			resourceBundle = null;
//		}
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 *
	public static String getResourceString(String key) {
		ResourceBundle bundle = Activator.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}
*/
	/**
	 * Returns the plugin's resource bundle,
	 *
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}
	*/
	/** Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	 
}
