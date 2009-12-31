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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.widgets.Shell;

import distributed.plugin.ui.IGraphEditorConstants;
import distributed.plugin.ui.dialogs.TorusDialog;
import distributed.plugin.ui.models.GraphElementFactory;
import distributed.plugin.ui.models.LinkElement;
import distributed.plugin.ui.models.NodeElement;

/**
 * @author Me
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Torus implements ITopology {

    private static final int GAP = IGraphEditorConstants.NODE_SIZE * 3;

    private int cols;

    private int rows;

    private String type;
    
    private String linkType;

    private GraphElementFactory factory;

    private Shell shell;

    private NodeElement[][] nodes;

    private List links;

    /**
     * Construtor
     */
    public Torus(GraphElementFactory factory, Shell shell, String type) {
        this.factory = factory;
        this.shell = shell;
        this.type = type;
        TorusDialog dialog = new TorusDialog(this.shell);
        dialog.open();
        this.rows = dialog.getNumRows();
        this.cols = dialog.getNumCols();
        this.linkType = dialog.getLinkType();
        this.nodes = new NodeElement[this.rows][this.cols];
        this.links = new ArrayList();
    }

    /**
     * @see distributed.plugin.ui.models.topologies.ITopology#getName()
     */
    public String getName() {
        return IGraphEditorConstants.CREATE_TORUS_COMD;
    }

    /**
     * @see distributed.plugin.ui.models.topologies.ITopology#createTopology()
     */
    public void createTopology() {
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                this.nodes[i][j] = this.factory.createNodeElement();
            }
        }

        // links for horizental directions
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                LinkElement link;
                if (this.linkType.equals(IGraphEditorConstants.UNI))
                    link = this.factory.createUniLinkElement();
                else
                    link = this.factory.createBiLinkElement();
                this.links.add(link);
            }
        }

        // links for vertical directions
        for (int i = 0; i < this.cols; i++) {
            for (int j = 0; j < this.rows; j++) {
                LinkElement link;
                if (this.linkType.equals(IGraphEditorConstants.UNI))
                    link = this.factory.createUniLinkElement();
                else
                    link = this.factory.createBiLinkElement();
                this.links.add(link);
            }
        }
        
        // Torus2: a bottom end will have a repeated link for both
        // horizental and vertical connection
        if(this.type.equals(IGraphEditorConstants.TORUS_2))
            this.links.remove(links.size()-1);

    }

    /**
     * @see distributed.plugin.ui.models.topologies.ITopology#getAllNodes()
     */
    public List getAllNodes() {
        List tmp = new ArrayList();
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                tmp.add(this.nodes[i][j]);
            }
        }
        return tmp;
    }

    /**
     * @see distributed.plugin.ui.models.topologies.ITopology#getAllLinks()
     */
    public List getAllLinks() {
        return this.links;
    }

    /**
     * @see distributed.plugin.ui.models.topologies.ITopology#getConnectionType()
     */
    public String getConnectionType() {
        return this.linkType;
    }

    /**
     * @see distributed.plugin.ui.models.topologies.ITopology#applyLocation(org.eclipse.draw2d.geometry.Point)
     */
    public void applyLocation(Point point) {
        int x = point.x;
        int y = point.y;
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                Point p = new Point(x + (j * GAP), y + (i * GAP));
                this.nodes[i][j].setLocation(p);
                this.nodes[i][j].setSize(new Dimension(
                        IGraphEditorConstants.NODE_SIZE,
                        IGraphEditorConstants.NODE_SIZE));
            }
        }
    }

    /**
     * @see distributed.plugin.ui.models.topologies.ITopology#setConnections()
     */
    public void setConnections() {
        if(this.type.equals(IGraphEditorConstants.TORUS_1))
            this.connectType1();
        else
            this.connectType2();
    }
    
    private void connectType1(){
        int count = -1;
        // horizental connections
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                LinkElement link = (LinkElement) this.links.get(++count);
                link.setSource(this.nodes[i][j]);
                link.attachSource();
                link.setTarget(this.nodes[i][(j + 1)%this.cols]);
                link.attachTarget();
            }
        }

        // vertical connections
        for (int i = 0; i < this.cols; i++) {
            for (int j = 0; j < this.rows; j++) {
                LinkElement link = (LinkElement) this.links.get(++count);
                link.setSource(this.nodes[j][i]);
                link.attachSource();
                link.setTarget(this.nodes[(j + 1)%this.rows][i]);
                link.attachTarget();
            }
        }
    }

    private void connectType2(){
        int count = -1;
        // horizental connections
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                LinkElement link = (LinkElement) this.links.get(++count);
                link.setSource(this.nodes[i][j]);
                link.attachSource();
                if(j == this.cols-1)
                    link.setTarget(this.nodes[(i+1)%this.rows][0]);
                else
                    link.setTarget(this.nodes[i][j + 1]);
                link.attachTarget();
            }
        }

        // vertical connections
        for (int i = 0; i < this.cols; i++) {
            for (int j = 0; j < this.rows; j++) {
                // ignore a repeated link at a bottom end 
                if(i == this.cols-1 && j == this.rows -1)
                    break;
                
                LinkElement link = (LinkElement) this.links.get(++count);
                link.setSource(this.nodes[j][i]);
                link.attachSource();
                if(j == this.rows -1)
                    link.setTarget(this.nodes[0][(i+1)%this.cols]);
                else
                    link.setTarget(this.nodes[(j + 1)%this.rows][i]);
                link.attachTarget();
            }
        }
    }
    
}
