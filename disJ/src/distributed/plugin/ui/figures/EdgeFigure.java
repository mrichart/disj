package distributed.plugin.ui.figures;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import distributed.plugin.ui.Activator;

public class EdgeFigure extends PolylineConnection {

	private static Image IMG_AGENT;
	
	private static ImageDescriptor IDSC_AGENT;

	private int numPacket;
	  
	static {
		try {
			URL installUrl = Activator.getDefault().getBundle().getEntry("/");
			URL imageUrl = new URL(installUrl, "icons/agent.png");
			IDSC_AGENT = ImageDescriptor.createFromURL(imageUrl);
			IMG_AGENT = IDSC_AGENT.createImage();
		}catch (MalformedURLException e) {
		}
	}   
	
	/**
	 * @see Shape#outlineShape(Graphics)
	 */
    public void outlineShape(Graphics graphics) {
    	super.outlineShape(graphics);
    	
    	if(numPacket == 1){
    		Rectangle f = Rectangle.SINGLETON;
    		Point p = f.getCenter();
    		graphics.drawString("A", p.x, p.y);
    		System.out.println("Num Packet: = 1");
    		
    	} else if (numPacket > 1){
    		System.out.println("Num Packet: = " + this.numPacket);
    	}
    }
    
    public void setNumPacket(int num){
		this.numPacket = num;
	}
}
