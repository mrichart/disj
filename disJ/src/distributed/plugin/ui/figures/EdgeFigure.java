package distributed.plugin.ui.figures;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import distributed.plugin.ui.Activator;

public class EdgeFigure extends PolylineConnection {

	private static Image IMG_AGENT;
	
	private static ImageDescriptor IDSC_AGENT;
	
	private static Image IMG_AGENTS;
	
	private static ImageDescriptor IDSC_AGENTS;

	private Label label;
	
	private int numPacket;
	  
	static {
		try {
			URL installUrl = Activator.getDefault().getBundle().getEntry("/");
			URL imageUrl = new URL(installUrl, "icons/agent.png");
			IDSC_AGENT = ImageDescriptor.createFromURL(imageUrl);
			IMG_AGENT = IDSC_AGENT.createImage();
			
			imageUrl = new URL(installUrl, "icons/agents.png");
			IDSC_AGENTS = ImageDescriptor.createFromURL(imageUrl);
			IMG_AGENTS = IDSC_AGENTS.createImage();
			
		}catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}   
	
	public EdgeFigure(){
		super();
		this.label = new Label("");
		this.add(this.label);
	}
	
	/**
	 * @see Shape#outlineShape(Graphics)
	 */
    public void outlineShape(Graphics graphics) {
       	super.outlineShape(graphics); 
       	
    	if(numPacket == 1){  
    		PointList points = this.getPoints();
    		Point p = points.getMidpoint();
    		graphics.drawImage(IMG_AGENT, p);
    		
    	} else if (numPacket > 1){
    		PointList points = this.getPoints();
    		Point p = points.getMidpoint();
    		graphics.drawImage(IMG_AGENTS, p);
    		
    	} else {
    		
    	}
    }
    /**
     * Set current number of packets under transmission
     * @param num
     */
    public void setNumPacket(int num){
		this.numPacket = num;
    	
	}
}
