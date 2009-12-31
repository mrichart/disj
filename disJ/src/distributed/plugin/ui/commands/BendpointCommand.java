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
import org.eclipse.gef.commands.Command;

import distributed.plugin.ui.IGraphEditorConstants;
import distributed.plugin.ui.models.LinkElement;

/**
 * @author Me
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class BendpointCommand extends Command {

    protected int index;

    protected Point location;

    protected LinkElement linkElement;

    private Dimension d1, d2;

    /**
     * 
     */
    public BendpointCommand() {
        super();
        setLabel(IGraphEditorConstants.ADJUST_LINK_COMD);
    }
    
    public String getLabel(){
        return IGraphEditorConstants.ADJUST_LINK_COMD;
    }

    protected Dimension getFirstRelativeDimension() {
        return d1;
    }

    protected Dimension getSecondRelativeDimension() {
        return d2;
    }

    protected int getIndex() {
        return index;
    }

    protected Point getLocation() {
        return location;
    }

    protected LinkElement getConnectionModel() {
        return linkElement;
    }

    public void redo() {
        execute();
    }

    public void setRelativeDimensions(Dimension dim1, Dimension dim2) {
        d1 = dim1;
        d2 = dim2;
    }

    public void setIndex(int i) {
        index = i;
    }

    public void setLocation(Point p) {
        location = p;
    }

    public void setConnectionModel(LinkElement connectionModel) {
        linkElement = connectionModel;
    }

}
