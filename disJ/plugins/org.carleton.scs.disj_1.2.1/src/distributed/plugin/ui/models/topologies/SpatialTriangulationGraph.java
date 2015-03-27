package distributed.plugin.ui.models.topologies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.widgets.Shell;

import distributed.plugin.ui.IGraphEditorConstants;
import distributed.plugin.ui.dialogs.SpatialDialog;
import distributed.plugin.ui.models.GraphElementFactory;
import distributed.plugin.ui.models.LinkElement;
import distributed.plugin.ui.models.NodeElement;

public class SpatialTriangulationGraph implements ITopology {

	private static double EPSILON = 0.000001;

	private int numberOfNode;

	private String linkType;

	private List<NodeElement> nodes;

	private List<LinkElement> links;

	private Shell shell;

	private GraphElementFactory factory;

	private Random random;
	
	public SpatialTriangulationGraph(GraphElementFactory factory, Shell shell) {
		this.factory = factory;
		this.shell = shell;
		this.numberOfNode = 0;
		this.linkType = IGraphEditorConstants.BI;
		this.random = new Random(System.currentTimeMillis());
		this.nodes = new ArrayList<NodeElement>();
		this.links = new ArrayList<LinkElement>();
	}

	private static class XComparator implements Comparator<SpatialPoint> {
		public int compare(SpatialPoint p1, SpatialPoint p2) {
			if (p1.x < p2.x) {
				return -1;
			} else if (p1.x > p2.x) {
				return 1;
			} else {
				return 0;
			}
		}
	}

	/*
	 * Return TRUE if a point (xp,yp) is inside the circumcircle made up of the
	 * points (x1,y1), (x2,y2), (x3,y3) The circumcircle centre is returned in
	 * (xc,yc) and the radius r NOTE: A point on the edge is inside the
	 * circumcircle
	 */
	private static boolean circumCircle(SpatialPoint p, SpatialTriangle t,
			SpatialPoint circle) {

		double m1, m2, mx1, mx2, my1, my2;
		double dx, dy, rsqr, drsqr;

		/* Check for coincident points */
		if (Math.abs(t.p1.y - t.p2.y) < EPSILON
				&& Math.abs(t.p2.y - t.p3.y) < EPSILON) {
			System.err.println("CircumCircle: Points are coincident.");
			return false;
		}

		if (Math.abs(t.p2.y - t.p1.y) < EPSILON) {
			m2 = -(t.p3.x - t.p2.x) / (t.p3.y - t.p2.y);
			mx2 = (t.p2.x + t.p3.x) / 2.0f;
			my2 = (t.p2.y + t.p3.y) / 2.0f;
			circle.x = (t.p2.x + t.p1.x) / 2.0f;
			circle.y = m2 * (circle.x - mx2) + my2;
		} else if (Math.abs(t.p3.y - t.p2.y) < EPSILON) {
			m1 = -(t.p2.x - t.p1.x) / (t.p2.y - t.p1.y);
			mx1 = (t.p1.x + t.p2.x) / 2.0f;
			my1 = (t.p1.y + t.p2.y) / 2.0f;
			circle.x = (t.p3.x + t.p2.x) / 2.0f;
			circle.y = m1 * (circle.x - mx1) + my1;
		} else {
			m1 = -(t.p2.x - t.p1.x) / (t.p2.y - t.p1.y);
			m2 = -(t.p3.x - t.p2.x) / (t.p3.y - t.p2.y);
			mx1 = (t.p1.x + t.p2.x) / 2.0f;
			mx2 = (t.p2.x + t.p3.x) / 2.0f;
			my1 = (t.p1.y + t.p2.y) / 2.0f;
			my2 = (t.p2.y + t.p3.y) / 2.0f;
			circle.x = (m1 * mx1 - m2 * mx2 + my2 - my1) / (m1 - m2);
			circle.y = m1 * (circle.x - mx1) + my1;
		}

		dx = t.p2.x - circle.x;
		dy = t.p2.y - circle.y;
		rsqr = dx * dx + dy * dy;
		circle.z = (float) Math.sqrt(rsqr);

		dx = p.x - circle.x;
		dy = p.y - circle.y;
		drsqr = dx * dx + dy * dy;

		return drsqr <= rsqr;
	}

	/*
	 * Triangulation subroutine Takes as input vertices (Point3fs) in Vector
	 * pxyz Returned is a list of triangular faces in the Vector v These
	 * triangles are arranged in a consistent clockwise order.
	 */
	private Vector<SpatialTriangle> triangulate(Vector<SpatialPoint> nodes) {

		// sort vertex array in increasing x values
		Collections.sort(nodes, new XComparator());

		/*
		 * Find the maximum and minimum vertex bounds. This is to allow
		 * calculation of the bounding triangle
		 */
		double xmin = ((SpatialPoint) nodes.elementAt(0)).x;
		double ymin = ((SpatialPoint) nodes.elementAt(0)).y;
		double xmax = xmin;
		double ymax = ymin;

		Iterator<SpatialPoint> pIter = nodes.iterator();
		while (pIter.hasNext()) {
			SpatialPoint p = pIter.next();
			if (p.x < xmin)
				xmin = p.x;
			if (p.x > xmax)
				xmax = p.x;
			if (p.y < ymin)
				ymin = p.y;
			if (p.y > ymax)
				ymax = p.y;
		}

		double dx = xmax - xmin;
		double dy = ymax - ymin;
		double dmax = (dx > dy) ? dx : dy;
		double xmid = (xmax + xmin) / 2.0f;
		double ymid = (ymax + ymin) / 2.0f;

		Vector<SpatialTriangle> triList = new Vector<SpatialTriangle>(); // for the Triangles
		HashSet<SpatialTriangle> complete = new HashSet<SpatialTriangle>(); // for complete Triangles

		// create supertriangle
		SpatialTriangle superTriangle = new SpatialTriangle();
		superTriangle.p1 = new SpatialPoint(-9, xmid - 2.0f * dmax,
				ymid - dmax, 0.0f);
		superTriangle.p2 = new SpatialPoint(-99, xmid, ymid + 2.0f * dmax, 0.0f);
		superTriangle.p3 = new SpatialPoint(-999, xmid + 2.0f * dmax, ymid
				- dmax, 0.0f);
//		System.out.println("SuperTri1: " + superTriangle.p1);
//		System.out.println("SuperTri2: " + superTriangle.p2);
//		System.out.println("SuperTri3: " + superTriangle.p3);
		triList.addElement(superTriangle);

		
		// add one point at a time into the mesh 
		Vector<SpatialLink> edges = new Vector<SpatialLink>();
		pIter = nodes.iterator();
		while (pIter.hasNext()) {
			SpatialPoint p = pIter.next();
			edges.clear();

			/*
			 * If the point P(x,y) lies inside the circumcircle
			 * then the three edges of new triangle are added  
			 * and the outer triangle is removed.
			 */
			SpatialPoint circle = new SpatialPoint();

			for (int j = triList.size() - 1; j >= 0; j--) {

				SpatialTriangle t = triList.elementAt(j);
				if (complete.contains(t)) {
					continue;
				}

				boolean inside = circumCircle(p, t, circle);

				if (circle.x + circle.z < p.x) {
					complete.add(t);
				}
				if (inside) {
					edges.addElement(new SpatialLink(t.p1, t.p2));
					edges.addElement(new SpatialLink(t.p2, t.p3));
					edges.addElement(new SpatialLink(t.p3, t.p1));
					triList.remove(t);
				}

			}

			/*
			 * Tag multiple edges Note: if all triangles are specified
			 * anticlockwise then all interior edges are opposite pointing in
			 * direction.
			 */
			for (int j = 0; j < edges.size() - 1; j++) {
				SpatialLink e1 = edges.elementAt(j);
				for (int k = j + 1; k < edges.size(); k++) {
					SpatialLink e2 = edges.elementAt(k);
					if (e1.p1 == e2.p2 && e1.p2 == e2.p1) {
						e1.p1 = null;
						e1.p2 = null;
						e2.p1 = null;
						e2.p2 = null;
					}
					/* Shouldn't need the following, see note above */
					if (e1.p1 == e2.p1 && e1.p2 == e2.p2) {
						e1.p1 = null;
						e1.p2 = null;
						e2.p1 = null;
						e2.p2 = null;
					}
				}
			}

			 // Add connections of an triangle in clockwise order.			
			for (int j = 0; j < edges.size(); j++) {
				SpatialLink e = edges.elementAt(j);
				if (e.p1 == null || e.p2 == null) {
					continue;
				}
				triList.add(new SpatialTriangle(e.p1, e.p2, p));
			}

		}

		
		// Remove every triangles that contains supertriangle node
		for (int i = triList.size() - 1; i >= 0; i--) {
			SpatialTriangle t = triList.elementAt(i);
			if (t.sharesVertex(superTriangle)) {
				triList.remove(t);
			}
		}

		return triList;
	}

	public void createTopology() {		
		SpatialDialog dialog = new SpatialDialog(this.shell);
		dialog.open();
		
		if(!dialog.isCancel()){
			this.numberOfNode = dialog.getNumNode();
			this.linkType = dialog.getLinkType();
			
			for (int i = 0; i < this.numberOfNode; i++) {
				NodeElement n = this.factory.createNodeElement();
				n.setSize(new Dimension(IGraphEditorConstants.NODE_SIZE,
						IGraphEditorConstants.NODE_SIZE));
				this.nodes.add(n);
			}
		}
	}

	public List<LinkElement> getAllLinks() {
		return this.links;
	}

	public List<NodeElement> getAllNodes() {
		return this.nodes;
	}

	public String getConnectionType() {
		return this.linkType;
	}

	public String getName() {
		return IGraphEditorConstants.CREATE_SPATIAL_COMD;
	}

	public void applyLocation(Point point) {
		//System.out.println("+@applyLocation()");
		
		if(this.numberOfNode > 0){
			List<Integer> xList = new ArrayList<Integer>();
			List<Integer> yList = new ArrayList<Integer>();
	
			for (int i = 0; i < this.numberOfNode; i++) {
				xList.add(i * (IGraphEditorConstants.NODE_SIZE));
				yList.add(this.random.nextInt(this.numberOfNode)*IGraphEditorConstants.NODE_SIZE);
			}
		//	System.out.println("@applyLocation() xList: " + xList);
		//	System.out.println("@applyLocation() yList: " + yList);
	
			List<Point> pList = new ArrayList<Point>();
			Point p = null;
			for (int i = 0; i < this.numberOfNode; i++) {
				p = new Point(xList.get(i), yList.get(i));
				pList.add(p);
			}
	
			// assign point to each node
			NodeElement node;
			for (int i = 0; i < this.numberOfNode; i++) {
				node = this.nodes.get(i);
				point = pList.get(i);
				node.setLocation(point);
			}
		}
		//System.out.println("-@applyLocation()");
	}

	public void setConnections() {

		//System.out.println("+@setConnections()");

		if(this.numberOfNode > 0){
			// create triangulation
			Vector<SpatialPoint> points = new Vector<SpatialPoint>();
			for (int i = 0; i < this.nodes.size(); i++) {
				points.addElement(new SpatialPoint(i, this.nodes.get(i)
						.getLocation()));
			}
			Vector<SpatialTriangle> triList = this.triangulate(points);
	
			//System.out.println("@setConnections() triList: " + triList);
	
			// connecting the nodes
			SpatialTriangle tri;
			NodeElement n1, n2, n3;
			Map<String, List<String>> conMap = new HashMap<String, List<String>>();
			for (int i = 0; i < triList.size(); i++) {
				tri = triList.elementAt(i);
				n1 = this.nodes.get(tri.p1.index);
				n2 = this.nodes.get(tri.p2.index);
				n3 = this.nodes.get(tri.p3.index);
	
				conMap = this.connectNodes(conMap, n1, n2);
				conMap = this.connectNodes(conMap, n2, n3);
				conMap = this.connectNodes(conMap, n3, n1);
				
				//System.out.println(n1.getName() + "-" + n2.getName() + "-"+ n3.getName());
				
			}
		}
		//System.out.println("-@setConnections()");
	}

	private List<String> getNeighborList(String name, Map<String, 
			List<String>> conMap) {
		
		List<String> nb = conMap.get(name);
		if (nb == null) {
			nb = new ArrayList<String>();
		}
		return nb;
	}

	private Map<String, List<String>> connectNodes(
			Map<String, List<String>> conMap, NodeElement n1, NodeElement n2) {

		LinkElement link;
		List<String> list;
		list = this.getNeighborList(n1.getName(), conMap);
		if (!list.contains(n2.getName())) {
			if(this.linkType.equals(IGraphEditorConstants.BI)){
				link = this.factory.createBiLinkElement();
			}else{
				link = this.factory.createUniLinkElement();
			}
			link.setSource(n1);
			link.attachSource();
			link.setTarget(n2);
			link.attachTarget();
			this.links.add(link);

			// update record 2 ways
			list.add(n2.getName());
			conMap.put(n1.getName(), list);

			list = this.getNeighborList(n2.getName(), conMap);
			if (!list.contains(n1.getName())) {
				list.add(n1.getName());
				conMap.put(n2.getName(), list);
			}
		}
		return conMap;
	}
}
