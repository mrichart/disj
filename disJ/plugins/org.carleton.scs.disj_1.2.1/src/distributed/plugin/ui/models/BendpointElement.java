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

import org.eclipse.draw2d.Bendpoint;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;

/**
 * @author Me
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class BendpointElement implements Bendpoint {

    private float weight = 0.5f;

    private Dimension d1, d2;

    /**
     * Constructor
     */
    public BendpointElement() {
        super();
    }

    /**
     * @see org.eclipse.draw2d.Bendpoint#getLocation()
     */
    public Point getLocation() {
        return null;
    }

    public Dimension getFirstRelativeDimension() {
        return d1;
    }

    public Dimension getSecondRelativeDimension() {
        return d2;
    }

    public float getWeight() {
        return weight;
    }

    public void setRelativeDimensions(Dimension dim1, Dimension dim2) {
        d1 = dim1;
        d2 = dim2;
    }

    public void setWeight(float w) {
        weight = w;
    }

}
