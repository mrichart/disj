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

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;

import distributed.plugin.ui.models.GraphElement;
import distributed.plugin.ui.models.NodeElement;

/**
 * @author daanish
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class GraphEditPartFactory implements EditPartFactory {

	public EditPart createEditPart(EditPart context, Object model) {
		EditPart part = null;
		NodeElement nodeElement = null;
		String NodeId;
		if (model instanceof GraphElement)
			part = new GraphPart();
		else if (model instanceof NodeElement) {
			nodeElement = (NodeElement) model;

			NodeId = nodeElement.getName();
			boolean isInit = nodeElement.getNode().isInitializer();
			boolean isAlive = nodeElement.getNode().isAlive();
			part = new NodePart(NodeId, isInit, isAlive);
		}
		if (part != null)
			part.setModel(model);

		return part;
	}
}
