package distributed.plugin.ui.actions;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.actions.RetargetAction;

import distributed.plugin.ui.Activator;

public class SaveRecordRegargetAction extends RetargetAction {

	public SaveRecordRegargetAction(String actionID, String text) {
        super(actionID, text);
    	try {
			final URL installUrl = Activator.getDefault().getBundle().getEntry("/");
			//TODO : change the icon
			final URL imageUrl = new URL(installUrl, "icons/saveRec.png");
			setImageDescriptor(ImageDescriptor.createFromURL(imageUrl));
			
		} catch (MalformedURLException e) {					
		}
        setToolTipText(text);
	}

}
