package distributed.plugin.ui.actions;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.actions.RetargetAction;

import distributed.plugin.ui.Activator;

public class LoadRandomRetargetAction extends RetargetAction {

	public LoadRandomRetargetAction(String actionID, String text) {
		super(actionID, text);
		
		try {
			final URL installUrl = Activator.getDefault().getBundle().getEntry("/");
			final URL imageUrl = new URL(installUrl, "icons/load_ran.png");
			setImageDescriptor(ImageDescriptor.createFromURL(imageUrl));
			
		} catch (MalformedURLException e) {					
		}
		setToolTipText(text);
		setText(text);
		setDescription(text);
	}
//
//	public LoadRandomRetargetAction(String actionID, String text, int style) {
//		super(actionID, text, style);
//		// TODO Auto-generated constructor stub
//	}

}
