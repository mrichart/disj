package distributed.plugin.ui.actions;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.actions.RetargetAction;

import distributed.plugin.ui.GraphEditorPlugin;

public class ReplayRegargetAction extends RetargetAction {

    public ReplayRegargetAction(String actionID, String text) {
        super(actionID, text);
    	try {
			final URL installUrl = GraphEditorPlugin.getDefault().getBundle().getEntry("/");
			//TODO : change the icon
			final URL imageUrl = new URL(installUrl, "icons/replay.png");
			setImageDescriptor(ImageDescriptor.createFromURL(imageUrl));
			
		} catch (MalformedURLException e) {					
		}
        setToolTipText(text);
    }

}
