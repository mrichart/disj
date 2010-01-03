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
public class GenericGraph implements ITopology {

	private int count;

	private int numberOfNode;

	private String linkType;

	private List nodes;

	private List links;

	private Shell shell;

	private GraphElementFactory factory;

	private Random random;

	private String defaultType = IGraphEditorConstants.GENERIC;

	private String type = defaultType;

	private int numberOfLink;

	private int numberOfInitiators;

	/**
     * 
     */
	public GenericGraph(GraphElementFactory factory, Shell shell) {
		this.count = -1;
		this.factory = factory;
		this.shell = shell;
		GenericGraphDialog dialog = new GenericGraphDialog(this.shell);
		dialog.open();
		this.numberOfNode = dialog.getNumNode();
		this.linkType = dialog.getLinkType();
		init();
	}

	public GenericGraph(GraphElementFactory factory, Shell shell, String type) {
		this.type = type;
		this.count = -1;
		this.factory = factory;
		this.shell = shell;
		if (type == IGraphEditorConstants.GENERIC_C){
		GenericGraphCDialog dialog = new GenericGraphCDialog(this.shell);
		dialog.open();
		this.numberOfNode = dialog.getNumNode();
		this.numberOfLink = dialog.getNumLink();
		this.linkType = dialog.getLinkType();
		this.numberOfInitiators = dialog.getNumInitiators();
		
		}

		init();
	}

	private void init() {
		this.random = new Random(System.currentTimeMillis());
		this.nodes = new ArrayList();
		this.links = new ArrayList();
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
		for (int i = 0; i < this.numberOfNode; i++) {
			NodeElement n = this.factory.createNodeElement();
			n.setSize(new Dimension(IGraphEditorConstants.NODE_SIZE,
					IGraphEditorConstants.NODE_SIZE));			
			this.nodes.add(n);
		}
		if (type == IGraphEditorConstants.GENERIC_C){
			Vector<Integer> inits=new Vector<Integer>();
			int rn;
			for (int i=0;i<this.numberOfInitiators;i++){
				rn=random.nextInt(this.numberOfNode);
				while(inits.contains(rn)){
					rn=random.nextInt(this.numberOfNode);
				}
				inits.add(rn);
				((NodeElement)(nodes.get(rn))).setPropertyValue(NodeElement.PROPERTY_IS_INIT, 1);
			}
		}
	}

	/**
	 * @see distributed.plugin.ui.models.topologies.ITopology#getAllNodes()
	 */
	public List getAllNodes() {
		return this.nodes;
	}

	public NodeElement nextNode() {
		return (NodeElement) this.nodes.get(++this.count);
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
		int range = this.numberOfNode * 20;
		int up = this.numberOfNode / 2;
		int down = this.numberOfNode - up;

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
			for (int i = 0; i < this.numberOfNode; i++) {
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
			int up = this.numberOfNode / 2;
			int down = this.numberOfNode - up;
			int size = (up < down) ? up : down;
			for (int i = 0; i < size; i++) {
				NodeElement source = (NodeElement) this.nodes.get(i);
				int p = this.random.nextInt(2);
				if (p == 1) {
					int target = i + size;
					if (target >= this.numberOfNode)
						target = this.numberOfNode - 1;
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
		int numberOfNode=this.numberOfNode;
		int numberOfLinks=this.numberOfLink;
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
		List targets = target.getTargetConnections();
		for (int i = 0; i < targets.size(); i++) {
			LinkElement tar = (LinkElement) targets.get(i);
			if (tar.getTarget().getNodeId().equals(source.getNodeId()))
				return;
		}
		targets = target.getSourceConnections();
		for (int i = 0; i < targets.size(); i++) {
			LinkElement tar = (LinkElement) targets.get(i);
			if (tar.getTarget().getNodeId().equals(source.getNodeId()))
				return;
		}

		// remove repeated connection from a same source
		List sources = source.getTargetConnections();
		for (int i = 0; i < sources.size(); i++) {
			LinkElement tar = (LinkElement) sources.get(i);
			if (tar.getTarget().getNodeId().equals(target.getNodeId()))
				return;
		}
		sources = source.getSourceConnections();
		for (int i = 0; i < sources.size(); i++) {
			LinkElement tar = (LinkElement) sources.get(i);
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
