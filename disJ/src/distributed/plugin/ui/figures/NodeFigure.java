/*******************************************************************************
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     DisJ Development Group
 *******************************************************************************/

package distributed.plugin.ui.figures;

import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.ToolbarLayout;

import distributed.plugin.ui.IGraphEditorConstants;

/**
 * @author Me
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class NodeFigure extends RoundedRectangle {    

    /** The figure's anchor. */
    // private ChopboxAnchor anchor;
    /**
     * Constructor
     */
    private Label label;
    
    public NodeFigure(String name) {
        super();
//        System.err.println("[NodeFigure] created");
        // this.anchor = new ChopboxAnchor(this);
        label = new Label(name);
        add(label);
        ToolbarLayout layout = new ToolbarLayout();
        setLayoutManager(layout);
        // setBorder(new RaisedBorder());
        setBackgroundColor(IGraphEditorConstants.DEFAULT_NODE_COLOR);
        setOpaque(true);
        setSize(IGraphEditorConstants.NODE_SIZE, IGraphEditorConstants.NODE_SIZE);
    }

	public Label getLabel() {
		return label;
	}
    
    


    // /**
    // * TODO need to have 2 type of anchors
    // * @param terminal
    // * @return
    // */
    // public ConnectionAnchor getConnectionAnchor(String terminal) {
    // return this.anchor;
    // }
    //    
    // /**
    // * TODO still dont understand
    // * @param p
    // * @return
    // */
    // public ConnectionAnchor getSourceConnectionAnchorAt(Point p) {
    // return this.anchor;
    // }

}
