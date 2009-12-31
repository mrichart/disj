/*******************************************************************************
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     DisJ Development Group
 *******************************************************************************/

package distributed.plugin.ui.models.topologies;

import java.util.List;

import org.eclipse.draw2d.geometry.Point;

/**
 * @author Me
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public interface ITopology {

    /**
     * Get a name of this topology
     * 
     * @return String a name of this lopology
     */
    public String getName();

    /**
     * Create nodes and links
     */
    public void createTopology();

    /**
     * Get all nodes in this topology.
     * 
     * @return
     */
    public List getAllNodes();
    
    /**
     * Get all links in this topology
     * @return
     */
    public List getAllLinks();

    /**
     * Get a connection type in this topology. Note: All link in the topology
     * has a same type.
     * 
     * @return String a type of link <code>LinkElement.UNI</code> or
     *         <code>LinkElement.BI</code>
     */
    public String getConnectionType();

    /**
     * Apply an actual location into every node in created topology.
     * 
     * @param point
     *            a location that user want to place this topology.
     */
    public void applyLocation(Point point);
    
    /**
     * Set the connections between nodes and links according to this topology
     */
    public void setConnections();

}
