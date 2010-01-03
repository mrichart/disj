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
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.RelativeBendpoint;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import distributed.plugin.core.IConstants;
import distributed.plugin.ui.models.BendpointElement;
import distributed.plugin.ui.models.LinkElement;
import distributed.plugin.ui.parts.policies.LinkBendpointEditPolicy;
import distributed.plugin.ui.parts.policies.LinkEditPolicy;

/**
 * An EditPart of <code>Edge</code> type
 */
public abstract class LinkPart extends AbstractConnectionEditPart implements
        PropertyChangeListener {

    public static final Color ACTIVE = new Color(Display.getDefault(), 168, 74,
            0), DEAD = new Color(Display.getDefault(), 0, 0, 0);

    /**
     * Constructor
     */
    public LinkPart() {
        super();
    }

    /**
     * 
     */
    public void activate() {
        // System.err.println("[LinkPart] activate");
        super.activate();
        this.getLinkElement().addPropertyChangeListener(this);
    }

    public void activateFigure() {
        super.activateFigure();
        /*
         * Once the figure has been added to the ConnectionLayer, start
         * listening for its router to change.
         */
        getFigure().addPropertyChangeListener(
                Connection.PROPERTY_CONNECTION_ROUTER, this);
    }

    /**
     * 
     */
    public void deactivate() {
        this.getLinkElement().removePropertyChangeListener(this);
        super.deactivate();
    }

    public void deactivateFigure() {
        getFigure().removePropertyChangeListener(
                Connection.PROPERTY_CONNECTION_ROUTER, this);
        super.deactivateFigure();
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
     */
    protected void createEditPolicies() {

        // Required to move connections end poinys
        installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE,
                new ConnectionEndpointEditPolicy());

        // Require for bend points
        this.refreshBendpointEditPolicy();

        // for deleting link
        installEditPolicy(EditPolicy.CONNECTION_ROLE, new LinkEditPolicy());
    }

    /**
     * Returns the model of this represented as a LinkElement.
     * 
     * @return Model of this as <code>LinkElement</code>
     */
    protected LinkElement getLinkElement() {
        return (LinkElement) getModel();
    }

    /**
     * This method is called when a property is changed in the
     * HelloConnectionModel we listen to.
     */
    public void propertyChange(PropertyChangeEvent iEvent) {

        String property = iEvent.getPropertyName();
        Object value = iEvent.getNewValue();
        //System.err.println("[LinkPart] propertyChange " + property);
        if (Connection.PROPERTY_CONNECTION_ROUTER.equals(property)) {
            refreshBendpoints();
            refreshBendpointEditPolicy();
        }
        if (IConstants.PROPERTY_CHANGE_BENDPOINT.equals(property))
            this.refreshBendpoints();
		if (IConstants.PROPERTY_CHANGE__LINK_INVISIBLE.equals(property)) {
			
			this.makeInvisible((Boolean)value);
		}


    }

    private void makeInvisible(final boolean value) {
		// TODO Auto-generated method stub
		final IFigure figure = this.getFigure();
		Display display = Display.getCurrent();
		Runnable ui = null;
        if (display == null) {
            ui = new Runnable() {
                public void run() {
                
                	setVisualProperty(value);
                }
            };
        } else {
        	setVisualProperty(value);
        }
        if (ui != null) {
            display = Display.getDefault();
            display.asyncExec(ui);
        }		
	}
    
	protected void setVisualProperty(boolean value){
    	figure.setVisible(value);
    	super.refreshVisuals();
	}

	/** Updates bendpoints. */
    private void refreshBendpoints() {

        List modelConstraint = this.getLinkElement().getBendpoints();
        List figureConstraint = new ArrayList();
        for (int i = 0; i < modelConstraint.size(); i++) {
            BendpointElement wbp = (BendpointElement) modelConstraint.get(i);
            RelativeBendpoint rbp = new RelativeBendpoint(getConnectionFigure());
            rbp.setRelativeDimensions(wbp.getFirstRelativeDimension(), wbp
                    .getSecondRelativeDimension());
            rbp.setWeight((i + 1) / ((float) modelConstraint.size() + 1));
            figureConstraint.add(rbp);
        }
        getConnectionFigure().setRoutingConstraint(figureConstraint);
    }

    private void refreshBendpointEditPolicy() {
        installEditPolicy(EditPolicy.CONNECTION_BENDPOINTS_ROLE,
                new LinkBendpointEditPolicy());
    }

    /**
     * Refreshes the visual aspects of this, based upon the model (Edge). It
     * changes the wire color depending on the state of Wire.
     * 
     */
    protected void refreshVisuals() {
        this.refreshBendpoints();
        super.refreshVisuals();
        // if (getWire().getValue())
        // getWireFigure().setForegroundColor(alive);
        // else
        // getWireFigure().setForegroundColor(dead);
    }

}
