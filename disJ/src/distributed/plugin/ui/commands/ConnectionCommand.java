/*******************************************************************************
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     DisJ Development Group
 *******************************************************************************/

package distributed.plugin.ui.commands;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.gef.commands.Command;

import distributed.plugin.core.Edge;
import distributed.plugin.core.IConstants;
import distributed.plugin.ui.IGraphEditorConstants;
import distributed.plugin.ui.models.GraphElement;
import distributed.plugin.ui.models.LinkElement;
import distributed.plugin.ui.models.NodeElement;

/**
 * @author Me
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class ConnectionCommand extends Command {

    private GraphElement graph;

    private NodeElement oldSource;

    private NodeElement oldTarget;

    private NodeElement source;

    private NodeElement target;

    private LinkElement link;

    /**
     * Constructor
     */
    public ConnectionCommand() {
        super(IGraphEditorConstants.CONNECTION_COMD);
    }
    
    public String getLabel() {
        return IGraphEditorConstants.CONNECTION_COMD;
    }

    /**
     * TODO not sure its correct
     */
    public boolean canExecute() {
        
        if (target == null && source == null)
            return false;

        if (target != null && source != null) {
            // no self connection
            if (target.getNode() == source.getNode())
                return false;

            try {
                // no repeated link
                Object org = source.getNodeId();
                Object dist = target.getNodeId();
                Map links = source.getNode().getEdges();
                Iterator edges = links.keySet().iterator();
                for (Object key = null; edges.hasNext();) {
                    key = edges.next();
                    Edge e = (Edge) links.get(key);
                    Object s = e.getStart().getNodeId();
                    Object d = e.getEnd().getNodeId();
                    if (e.getDirection() == IConstants.UNI_DIRECTION) {
                        if (org.equals(s) && dist.equals(d))
                            return false;
                    } else if (e.getDirection() == IConstants.BI_DIRECTION) {
                        if (org.equals(s) && dist.equals(d))
                            return false;
                        if (org.equals(d) && dist.equals(s))
                            return false;
                    }
                }
            } catch (NullPointerException ne) {
                System.err
                        .println("***This is bug [ConnectionCommand]canExecute"
                                + ne);
            }
        }
        return true;
    }

    public void execute() {
        //System.out.println("[ConnectionCommand].execute()");
        this.redo();
        
    }

    public void redo() {
        //System.out.println("[ConnectionCommand].redo()");
        this.link.setSource(this.source);
        this.link.attachSource();

        this.link.setTarget(this.target);
        this.link.attachTarget();
        
        this.graph.addEdge(this.link.getEdgeId(), this.link);
    }

    public void undo() {
        //System.out.println("[ConnectionCommand].undo()");
        this.link.detachSource();
        this.link.detachTarget();
        this.graph.removeEdge(this.link.getEdgeId(), this.link);

    }

    /**
     * @return Returns the link.
     */
    public LinkElement getLinkElement() {
        return this.link;
    }

    /**
     * @return Returns the source.
     */
    public NodeElement getSource() {
        return source;
    }

    /**
     * @return Returns the target.
     */
    public NodeElement getTarget() {
        return target;
    }

    public void setLinkElement(LinkElement link) {
        this.link = link;
        oldSource = link.getSource();
        oldTarget = link.getTarget();
    }

    public void setSource(NodeElement newSource) {
        this.source = newSource;
    }

    public void setTarget(NodeElement newTarget) {
        this.target = newTarget;
    }

    public void setParent(final GraphElement parent){
        this.graph = parent;
    }
}
