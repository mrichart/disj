/*******************************************************************************
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     DisJ Development Group
 *******************************************************************************/

package distributed.plugin.ui.parts;

import java.beans.PropertyChangeEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;

/**
 * @author Me
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class UniLinkPart extends LinkPart {

    /**
     * Constructor
     */
    public UniLinkPart() {
        super();
//        System.err.println("[UniLinkPart] created");
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
     */
    protected IFigure createFigure() {
        IFigure connectionFigure = super.createFigure();

//        System.err.println("[UniLinkPart] createFigure " + connectionFigure);
        
        ((PolylineConnection) connectionFigure)
                .setTargetDecoration(new PolygonDecoration());

        return connectionFigure;
    }
    
    /**
     * TODO
     * 
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event) {
        super.propertyChange(event);
//        System.err.println("[UniLinkPart] propertyChange: "
//                + event.getPropertyName());
    }

    /**
     * TODO Refreshes the visual aspects of this, based upon the model (Wire).
     * It changes the wire color depending on the state of Wire.
     * 
     */
    protected void refreshVisuals() {
        super.refreshVisuals();
    }

    // public AccessibleEditPart getAccessibleEditPart() {
    // if (acc == null)
    // acc = new AccessibleGraphicalEditPart() {
    // public void getName(AccessibleEvent e) {
    // e.result = IGraphEditorConstants.UNI;
    // }
    // };
    // return acc;
    // }

}
