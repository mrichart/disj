/*******************************************************************************
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     DisJ Development Group
 *******************************************************************************/

package distributed.plugin.ui.commands;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;

import distributed.plugin.ui.IGraphEditorConstants;
import distributed.plugin.ui.models.NodeElement;

/**
 * @author Me
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SetConstraintCommand extends Command {

//    private Rectangle newBound;
//	private Rectangle oldBound;

    private Point newPos;
    private Dimension newSize;
    private Point oldPos;
    private Dimension oldSize;
    private NodeElement model;
    
    /**
     * 
     */
    public SetConstraintCommand() {
        super(IGraphEditorConstants.MOVE_NODE_COMD);
    }
    
    public String getLabel(){
        return IGraphEditorConstants.MOVE_NODE_COMD;
    }
    
    public void setNodeElement(NodeElement element){
        this.model = element;
    }
    
//    public void setBound(Rectangle bound){
//        this.newBound = bound;
//    }
    

	public void execute() {
		//this.oldBound = this.model.getBound();
	    //this.redo();
		oldSize = this.model.getSize();
		oldPos  = this.model.getLocation();
		this.model.setLocation(newPos);
		//this.model.setSize(newSize);
		
	}

	public void redo() {
		//model.setBound(this.newBound);
		//this.model.setSize(newSize);
		this.model.setLocation(newPos);
	}

	public void undo() {
		//this.model.setBound(this.oldBound);

		//this.model.setSize(oldSize);
		this.model.setLocation(oldPos);
	}
	
	public void setSize(Dimension p) {
		newSize = p;
	  }
	
	public void setLocation(Rectangle r){
//	    System.out.println("[SetConstraintCommand].setLocation() with r="+r);

		setLocation(r.getLocation());
		setSize(r.getSize());
	  }

	  public void setLocation(Point p) {
//	    System.out.println("[SetConstraintCommand].setLocation() with p="+p);

		newPos = p;
	  }
	
}
