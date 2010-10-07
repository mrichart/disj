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

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import distributed.plugin.core.IConstants;
import distributed.plugin.ui.IGraphEditorConstants;
import distributed.plugin.ui.figures.NodeFigure;
import distributed.plugin.ui.models.GraphElement;
import distributed.plugin.ui.models.LinkElement;
import distributed.plugin.ui.models.NodeElement;
import distributed.plugin.ui.parts.policies.ElementComponentEditPolicy;
import distributed.plugin.ui.parts.policies.NodeGraphicalEditPolicy;

/**
 * @author Me
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class NodePart extends AbstractGraphicalEditPart implements
		NodeEditPart, PropertyChangeListener {

	String NodeId;

	/** The figure's anchors. */
	private ChopboxAnchor anchor;

	/**
	 * Constructor
	 */
	public NodePart() {
		super();
	}

	public NodePart(String NodeId) {
		super();
		this.NodeId = NodeId;
	}

	public void activate() {
		// System.out.println("[NodePart] activate");
		if (isActive())
			return;
		super.activate();
		this.getNodeElement().addPropertyChangeListener(this);
	}

	/**
	 * Makes the EditPart insensible to changes in the model by removing itself
	 * from the model's list of listeners.
	 */
	public void deactivate() {
		if (!isActive())
			return;
		super.deactivate();
		this.getNodeElement().removePropertyChangeListener(this);
	}

	/**
	 * Returns the model associated with this editPart.
	 * 
	 * @return The model of this as a NodeElement.
	 */
	public NodeElement getNodeElement() {
		return (NodeElement) getModel();
	}

	/**
	 * TODO create node figure here
	 * 
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	protected IFigure createFigure() {
		Figure fig = new NodeFigure(NodeId);

		this.anchor = new ChopboxAnchor(fig);
		return fig;
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
	 */
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE,
				new NodeGraphicalEditPolicy());
		installEditPolicy(EditPolicy.COMPONENT_ROLE,
				new ElementComponentEditPolicy());
		// installEditPolicy(EditPolicy.LAYOUT_ROLE, new
		// GraphXYEditLayoutPolicy());
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractEditPart#createConnection(Object)
	 */
	protected ConnectionEditPart createConnection(Object iModel) {
		// System.out.println("[NodePart] createConnection() with model="
		// + getModel());

		// The line below is based on Logical Diagram Editor's
		// LogicEditPart.createConnection()
		// It looks like an EditPart cache mechanism for EditPart connections,
		// but I need to make sure of that...
		LinkElement link = (LinkElement) iModel;
		LinkPart connectPart = (LinkPart) getRoot().getViewer()
				.getEditPartRegistry().get(link);

		if (connectPart == null) {
			if (link.getType().equals(IGraphEditorConstants.BI))
				connectPart = new BiLinkPart();
			else
				connectPart = new UniLinkPart();

			connectPart.setModel(link);
		}

		// System.out.println("[NodePart] createConnection() returns "
		// + connectPart);

		return connectPart;
	}

	/**
	 * Returns the Figure of this as a NodeFigure by calling createFigure()
	 * 
	 * @return a NodeFigure
	 */
	protected NodeFigure getNodeFigure() {
		return (NodeFigure) this.getFigure();
	}

	public GraphicalEditPart getGraphicalParent() {
		return (GraphicalEditPart) getParent();
	}

	/**
	 * Returns a list of connections for which this is the source.
	 * 
	 * @return List of connections.
	 */
	protected List getModelSourceConnections() {
		List conns = this.getNodeElement().getSourceConnections();
		// System.out.println("[NodePart] getModelSourceConnections() returns "
		// + conns);
		return conns;
	}

	/**
	 * Returns a list of connections for which this is the target.
	 * 
	 * @return List of connections.
	 */
	protected List getModelTargetConnections() {
		List conns = this.getNodeElement().getTargetConnections();
		// System.out.println("[NodePart] getModelTargetConnections() returns "
		// + conns);
		return conns;
	}

	/**
	 * Returns the connection anchor for the given ConnectionEditPart's source.
	 * 
	 * @see org.eclipse.gef.NodeEditPart#getSourceConnectionAnchor(org.eclipse.gef.ConnectionEditPart)
	 * @return ConnectionAnchor.
	 */
	public ConnectionAnchor getSourceConnectionAnchor(
			ConnectionEditPart connection) {
		return this.anchor;

	}

	/**
	 * @see org.eclipse.gef.NodeEditPart#getTargetConnectionAnchor(org.eclipse.gef.ConnectionEditPart)
	 */
	public ConnectionAnchor getTargetConnectionAnchor(
			ConnectionEditPart connection) {
		return this.anchor;
	}

	/**
	 * @see org.eclipse.gef.NodeEditPart#getSourceConnectionAnchor(org.eclipse.gef.Request)
	 */
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		return this.anchor;
	}

	/**
	 * @see org.eclipse.gef.NodeEditPart#getTargetConnectionAnchor(org.eclipse.gef.Request)
	 */
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		return this.anchor;
	}

	/**
	 * TODO add more stuffs look at LogicEditPart
	 * 
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		try {
			String prop = evt.getPropertyName();
			Display display = Display.getCurrent();
			Runnable ui = null;

			if (prop.equals(IConstants.PROPERTY_CHANGE_SIZE)) {
				if (display == null) {
					ui = new Runnable() {
						public void run() {
							refreshVisuals();
						}
					};
				} else {
					this.refreshVisuals();
				}

			} else if (prop.equals(IConstants.PROPERTY_CHANGE_LOCATION)) {
				if (display == null) {
					ui = new Runnable() {
						public void run() {
							refreshVisuals();
						}
					};
				} else {
					this.refreshVisuals();
				}
			} else if (prop.equals(IConstants.PROPERTY_CHANGE_INPUT)) {
				if (display == null) {
					ui = new Runnable() {
						public void run() {
							refreshTargetConnections();
						}
					};
				} else {
					refreshTargetConnections();
				}
			} else if (prop.equals(IConstants.PROPERTY_CHANGE_OUTPUT)) {
				if (display == null) {
					ui = new Runnable() {
						public void run() {
							refreshSourceConnections();
						}
					};
				} else {
					refreshSourceConnections();
				}
			} else if (prop.equals(IConstants.PROPERTY_CHANGE_NODE_STATE)) {
				final Integer value = (Integer) evt.getNewValue();
				if (display == null) {
					ui = new Runnable() {
						public void run() {
							refreshStateColor(value);
						}
					};
				} else {
					this.refreshStateColor(value);
				}
			} else if (prop.equals(IConstants.PROPERTY_CHANGE_NAME)) {
				final String newName = (String) evt.getNewValue();
				this.getNodeFigure().getLabel().setText(newName);
				if (display == null) {
					ui = new Runnable() {
						public void run() {
							refreshVisuals();
						}
					};
				} else {
					this.refreshVisuals();
				}
			}else {
				return;
			}
			if (ui != null) {
				display = Display.getDefault();
				display.asyncExec(ui);
			}
		} catch (RuntimeException e) {
			// TODO ignore this
			e.printStackTrace();
		}
	}

	private void refreshStateColor(Integer newState) {
		Short state = new Short(newState.shortValue());
		GraphElement g = (GraphElement) this.getGraphicalParent().getModel();
		Color color = g.getColor(state);		
		this.getNodeFigure().setBackgroundColor(color);
		this.refreshVisuals();
	}

	/**
	 * TODO update visual of node here i.e. node's color
	 * 
	 * @see org.eclipse.gef.editparts.AbstractEditPart#refreshVisuals()
	 */
	protected void refreshVisuals() {
		// super.refreshVisuals();
		Figure fig = this.getNodeFigure();
		Point loc = this.getNodeElement().getLocation();
		Dimension size = this.getNodeElement().getSize();
		Rectangle r = new Rectangle(loc, size);
		// System.out.println("[NodePart] refreshVisuals");
		getGraphicalParent().setLayoutConstraint(this, fig, r);

	}

}
