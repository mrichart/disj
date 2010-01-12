/*******************************************************************************
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     DisJ Development Group
 *******************************************************************************/

package distributed.plugin.ui.models;

import distributed.plugin.core.IConstants;
import distributed.plugin.ui.IGraphEditorConstants;

/**
 * @author Me
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class UniLinkElement extends LinkElement {

    static final long serialVersionUID = IConstants.SERIALIZE_VERSION;

    /**
     * Constructor
     */
    protected UniLinkElement(String graphId, String id) {
        super(graphId, id, IConstants.DIRECTION_UNI);
        this.getEdge().setDirection(IConstants.DIRECTION_UNI);
    }

    public String getType() {
        return IGraphEditorConstants.UNI;
    }

    public void attachSource() {
        if (this.getSource() == null)
            return;
        this.getSource().connectOutLink(this);
    }
   
    public void attachTarget() {
        if (this.getTarget() == null)
            return;
        this.getTarget().connectInLink(this);
    }

}
