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
import java.util.List;

import org.eclipse.draw2d.AutomaticRouter;
import org.eclipse.draw2d.BendpointConnectionRouter;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.FanRouter;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;
import org.eclipse.gef.tools.MarqueeDragTracker;
import org.eclipse.swt.widgets.Display;

import distributed.plugin.ui.models.GraphElement;
import distributed.plugin.ui.models.NodeElement;
import distributed.plugin.ui.parts.policies.GraphXYEditLayoutPolicy;

/**
 * 
 */
public class GraphPart extends AbstractGraphicalEditPart implements
        NodeEditPart, PropertyChangeListener {

    /** Singleton instance of MarqueeDragTracker. */
    static DragTracker dragTracker = null;

    /**
     * Constructor
     */
    public GraphPart() {
        super();
    }

    public void activate() {
        //System.err.println("[GraphPart] activate");
        if (isActive())
            return;
        super.activate();
        //System.err.println("[GraphPart] addPropChangeListener");
        this.getGraphElement().addPropertyChangeListener(this);
    }

    /**
     * Makes the EditPart insensible to changes in the model by removing itself
     * from the model's list of listeners.
     */
    public void deactivate() {
        if (!isActive())
            return;
        super.deactivate();
        this.getGraphElement().removePropertyChangeListener(this);
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
     */
    protected IFigure createFigure() {
        Figure f = new FreeformLayer();
        f.setLayoutManager(new FreeformLayout());
        // Don't know why, but if you don't setOpaque(true), you cannot move by
        // drag&drop!
        f.setOpaque(true);
        return f;
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
     */
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.NODE_ROLE, null);
        installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, null);
        installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, null);
        installEditPolicy(EditPolicy.COMPONENT_ROLE,
                new RootComponentEditPolicy());
        installEditPolicy(EditPolicy.LAYOUT_ROLE, new GraphXYEditLayoutPolicy());
    }

    /**
     * This method is not mandatory to implement, but if you do not implement
     * it, you will not have the ability to rectangle-selects several figures...
     */
    public DragTracker getDragTracker(Request req) {
//        System.out.println("[GraphPart].getDragTracker() with req="
//                + req);

        // Unlike in Logical Diagram Editor example, I use a singleton because
        // this
        // method is called several time, so I prefer to save memory ; and it
        // works!
        if (dragTracker == null) {
            dragTracker = new MarqueeDragTracker();
        }
        return dragTracker;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.NodeEditPart#getSourceConnectionAnchor(org.eclipse.gef.ConnectionEditPart)
     */
    public ConnectionAnchor getSourceConnectionAnchor(
            ConnectionEditPart connection) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.NodeEditPart#getTargetConnectionAnchor(org.eclipse.gef.ConnectionEditPart)
     */
    public ConnectionAnchor getTargetConnectionAnchor(
            ConnectionEditPart connection) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.NodeEditPart#getSourceConnectionAnchor(org.eclipse.gef.Request)
     */
    public ConnectionAnchor getSourceConnectionAnchor(Request request) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.NodeEditPart#getTargetConnectionAnchor(org.eclipse.gef.Request)
     */
    public ConnectionAnchor getTargetConnectionAnchor(Request request) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        //String prop = evt.getPropertyName();
        //System.out.println("[GraphPart] propertyChange: " + prop);
        Display display = Display.getCurrent();
        Runnable ui = null;

        if (display == null) {
			ui = new Runnable() {
				public void run() {
					refreshChildren();
					refreshVisuals();
				}
			};
		} else {
			this.refreshChildren();
			this.refreshVisuals();
		}
        if (ui != null) {
			display = Display.getDefault();
			display.asyncExec(ui);
		}        
    }

    /**
     * Get a corresponding element to this editPart
     * 
     * @return a GraphElement of this editPart
     */
    public GraphElement getGraphElement() {
        return (GraphElement) this.getModel();
    }

    /**
     * Return all sub elements of this part 
     * 
     * @see org.eclipse.gef.editparts.AbstractEditPart#getModelChildren()
     */
    protected List<NodeElement> getModelChildren() {
        List<NodeElement> children = this.getGraphElement().getNodeElements();
        //children.addAll(this.getGraphElement().getLinkElements());
        return children;
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractEditPart#refreshChildren()
     */
    protected void refreshChildren() {
        //System.err.println("[GraphPart] refreshChildren");

        super.refreshChildren();
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractEditPart#refreshVisuals()
     */
    protected void refreshVisuals() {
        //System.err.println("[GraphPart] refreshVisuals");
        super.refreshVisuals();

        ConnectionLayer cLayer = (ConnectionLayer) getLayer(LayerConstants.CONNECTION_LAYER);
        AutomaticRouter router = new FanRouter();
        router.setNextRouter(new BendpointConnectionRouter());
        cLayer.setConnectionRouter(router); 
    }

}
