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
import distributed.plugin.runtime.Graph;
import distributed.plugin.ui.IGraphEditorConstants;

/**
 * @author Me
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class BiLinkElement extends LinkElement {

    static final long serialVersionUID = 1;

    private String type;

    /**
     * Constructor
     */
    protected BiLinkElement(String graphId, String id) {
        super(graphId, id, IConstants.BI_DIRECTION);
        this.type = IGraphEditorConstants.BI;
        this.getEdge().setDirection(IConstants.BI_DIRECTION);
    }

    public String getType() {
        return this.type;
    }

    
    public void attachSource() {
        if (this.getSource() == null)
            return;
        this.getSource().connectInLink(this);
    }

 
    public void attachTarget() {
        if (this.getTarget() == null)
            return;
        this.getTarget().connectOutLink(this);
    }

}
