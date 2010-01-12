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
import java.util.Random;
import java.util.Vector;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.widgets.Shell;

import distributed.plugin.ui.IGraphEditorConstants;
import distributed.plugin.ui.dialogs.GenericGraphCDialog;
import distributed.plugin.ui.dialogs.GenericGraphDialog;
import distributed.plugin.ui.models.GraphElementFactory;
import distributed.plugin.ui.models.LinkElement;
import distributed.plugin.ui.models.NodeElement;

/**
 * @author Me
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class GenericGraph extends AbstractGraph {

	private int count;

	private String linkType;

	private String defaultType = IGraphEditorConstants.GENERIC;

	private String type = defaultType;

	/**
     * 
     */
	public GenericGraph(GraphElementFactory factory, Shell shell, String type) {
		super(factory, shell);
		this.count = -1;
		this.type = type;		
		this.linkType = IGraphEditorConstants.BI;
	}

	/**
	 * @see distributed.plugin.ui.models.topologies.ITopology#getName()
	 */
	public String getName() {
		return IGraphEditorConstants.CREATE_GENERIC_COMD;
	}

	/**
	 * @see distributed.plugin.ui.models.topologies.ITopology#createTopology()
	 */
	public void createTopology() {
		
		if(this.type.equals(IGraphEditorConstants.GENERIC)){
			GenericGraphDialog dialog = new GenericGraphDialog(this.shell);
			dialog.open();
			if(!dialog.isCancel()){
				this.numNode = dialog.getNumNode();
				this.linkType = dialog.getLinkType();
				this.numInit = dialog.getNumInit();
			}
		}else{
			GenericGraphCDialog dialog = new GenericGraphCDialog(this.shell);
			dialog.open();
			if(!dialog.isCancel()){
				this.numNode = dialog.getNumNode();
				this.numLink = dialog.getNumLink();
				this.linkType = dialog.getLinkType();
				this.numInit = dialog.getNumInit();
			}
		}
		
		for (int i = 0; i < this.numNode; i++) {
			NodeElement n = this.factory.createNodeElement();
			n.setSize(new Dimension(IGraphEditorConstants.NODE_SIZE,
					IGraphEditorConstants.NODE_SIZE));
			this.nodes.add(n);
		}
		
		// set init nodes		
		this.setInitNodes();
	}

	public NodeElement nextNode() {
		return (NodeElement) this.nodes.get(++this.count);
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
		if(this.numNode < 1){
			return;
		}
		
		int range = this.numNode * 20;
		int up = this.numNode / 2;
		int down = this.numNode - up;

		int x = point.x;
		int y = point.y;
		int x1, y1;
		x1 = x;
		y1 = y;
		for (int i = 0; i < up; i++) {
			if (i % 2 == 0) {
				x1 = this.random.nextInt(80) + 50 + x1;
				y1 = this.random.nextInt(30);
			} else {
				x1 = this.random.nextInt(80) + x1;
				y1 = this.random.nextInt(100) + 30;
			}
			if (y1 > y)
				y1 = y;
			if (Math.abs(y - y1) > range)
				y1 = y;

			NodeElement node = this.nextNode();
			Point p = new Point(x1, y1);
			node.setLocation(p);
		}

		NodeElement node = this.nextNode();
		Point p = new Point(x, y);
		node.setLocation(p);
		x1 = x;
		y1 = y;
		for (int i = 0; i < down - 1; i++) {
			if (i % 2 == 0) {
				x1 = this.random.nextInt(80) + 50 + x1;
				y1 = this.random.nextInt(30) + y;
			} else {
				x1 = this.random.nextInt(80) + x1;
				y1 = this.random.nextInt(80) + 30 + y;
			}
			node = this.nextNode();
			p = new Point(x1, y1);
			node.setLocation(p);
		}
	}

	/**
	 * @see distributed.plugin.ui.models.topologies.ITopology#setConnections()
	 */
	public void setConnections() {
		if (this.type == IGraphEditorConstants.GENERIC) {
			for (int i = 0; i < this.numNode; i++) {
				NodeElement source = (NodeElement) this.nodes.get(i);
				int numNeighbour = this.random.nextInt(4) + 1;
				for (int j = 1; j < numNeighbour; j++) {
					int p = this.random.nextInt(3);
					if (p == 1) {
						// conect to left neighbours
						int a = i - j;
						if (a > -1) {
							this.connectTarget(source, a);
						}
					}
					p = this.random.nextInt(2);
					if (p == 1) {
						// connect to right neighbour
						int a = i + j;
						if (a < this.nodes.size()) {
							this.connectTarget(source, a);
						}
					}
				}
			}
			// inter conect
			int up = this.numNode / 2;
			int down = this.numNode - up;
			int size = (up < down) ? up : down;
			for (int i = 0; i < size; i++) {
				NodeElement source = (NodeElement) this.nodes.get(i);
				int p = this.random.nextInt(2);
				if (p == 1) {
					int target = i + size;
					if (target >= this.numNode)
						target = this.numNode - 1;
					this.connectTarget(source, target);
				}
			}
		} else if (this.type == IGraphEditorConstants.GENERIC_C) {
			generateConnectedGenericGraph();

		}

	}
	/**
	 * added by Russell Nov,2008
	 */

	private void generateConnectedGenericGraph() {
		
		Vector<Integer> connectedNodes = new Vector<Integer>();
		Vector<Integer> unconnectedNodes = new Vector<Integer>();
		Vector<String> links = new Vector<String>();
		Random random = new Random(System.currentTimeMillis());
		int numberOfNode=this.numNode;
		int numberOfLinks=this.numLink;
		connectedNodes.add(0);
		for (int i = 1; i < numberOfNode; i++) {
			unconnectedNodes.add(i);
		}
		for (int j = 0; j <numberOfLinks; j++) {
			int p = 0;
			int q = 0;
			while (p == q ||links.contains(p+":"+q)||links.contains(q+":"+p)|| 
					(links.size()<numberOfNode-1&&
							!(connectedNodes.contains(p)&&unconnectedNodes.contains(q))))
					{
				p = random.nextInt(numberOfNode);
				q = random.nextInt(numberOfNode);
			}
			links.add(p+":"+q);
			connectedNodes.add(q);
			unconnectedNodes.remove(new Integer(q));
			//-----------------------------------------------------------
			NodeElement source = (NodeElement) this.nodes.get(p);
			NodeElement target = (NodeElement) this.nodes.get(q);
			LinkElement link;
			if (this.linkType.equals(IGraphEditorConstants.BI))
				link = this.factory.createBiLinkElement();
			else
				link = this.factory.createUniLinkElement();

			link.setSource(source);
			link.attachSource();

			link.setTarget(target);
			link.attachTarget();

			this.links.add(link);
		}
		
	}

	private void connectTarget(NodeElement source, int t) {

		// remove repeated connection from a same pair
		NodeElement target = (NodeElement) this.nodes.get(t);
		List<LinkElement> targets = target.getTargetConnections();
		for (int i = 0; i < targets.size(); i++) {
			LinkElement tar = targets.get(i);
			if (tar.getTarget().getNodeId().equals(source.getNodeId()))
				return;
		}
		targets = target.getSourceConnections();
		for (int i = 0; i < targets.size(); i++) {
			LinkElement tar =  targets.get(i);
			if (tar.getTarget().getNodeId().equals(source.getNodeId()))
				return;
		}

		// remove repeated connection from a same source
		List<LinkElement> sources = source.getTargetConnections();
		for (int i = 0; i < sources.size(); i++) {
			LinkElement tar =  sources.get(i);
			if (tar.getTarget().getNodeId().equals(target.getNodeId()))
				return;
		}
		sources = source.getSourceConnections();
		for (int i = 0; i < sources.size(); i++) {
			LinkElement tar = sources.get(i);
			if (tar.getTarget().getNodeId().equals(target.getNodeId()))
				return;
		}

		LinkElement link;
		if (this.linkType.equals(IGraphEditorConstants.BI))
			link = this.factory.createBiLinkElement();
		else
			link = this.factory.createUniLinkElement();

		link.setSource(source);
		link.attachSource();

		link.setTarget(target);
		link.attachTarget();

		this.links.add(link);
	}

}
