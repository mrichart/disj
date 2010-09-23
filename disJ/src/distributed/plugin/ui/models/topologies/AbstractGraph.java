/**
 * 
 */
package distributed.plugin.ui.models.topologies;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.widgets.Shell;

import distributed.plugin.core.Node;
import distributed.plugin.ui.models.GraphElementFactory;
import distributed.plugin.ui.models.LinkElement;
import distributed.plugin.ui.models.NodeElement;

/**
 * @author rpiyasin
 *
 */
public abstract class AbstractGraph implements ITopology {

	int numNode;
	int numLink;
	int numInit;
	int maxX;
	int maxY;
	
	List<NodeElement> nodes;
	List<LinkElement> links;

	Shell shell;
	Random random;
	GraphElementFactory factory;
	
	AbstractGraph(GraphElementFactory factory, Shell shell) {
		this.numNode = 0;
		this.numLink = 0;
		this.numInit = 0;
		this.maxX = 0;
		this.maxY = 0;
		
		this.factory = factory;
		this.shell = shell;
		this.random = new Random(System.currentTimeMillis());
		
		this.nodes = new ArrayList<NodeElement>();
		this.links = new ArrayList<LinkElement>();
	}
	
	public abstract void applyLocation(Point point);

	public abstract void createTopology();
	
	/*
	 * FIXME Randomly init the initiator to be true	
	 */
	void setInitNodes(){
			
		double min = this.numNode * 0.6;
		if(this.numInit >= this.numNode){
			for (NodeElement node : this.nodes) {
				node.getNode().setInit(true);
			}
		}else if( this.numInit >= min){
			for (NodeElement node : this.nodes) {
				node.getNode().setInit(true);
			}
			
			Node n;
			List<Integer> temp = new ArrayList<Integer>();
			int nId;
			int size = this.numNode - this.numInit;
			for (int i = 0; i < size; i++) {
				nId = random.nextInt(this.numNode);
				while (temp.contains(nId)) {
					nId = random.nextInt(this.numNode);
				}
				temp.add(nId);
				n = this.nodes.get(nId).getNode();
				n.setInit(false);
			}
		}else{
			Node n;
			List<Integer> temp = new ArrayList<Integer>();			
			int nId;
			int size = this.numInit;
			for (int i = 0; i < size; i++) {
				nId = random.nextInt(this.numNode);
				while (temp.contains(nId)) {
					nId = random.nextInt(this.numNode);
				}
				temp.add(nId);
				n = this.nodes.get(nId).getNode();
				n.setInit(true);
			}
		}
	}

	public List<LinkElement> getAllLinks(){
		return this.links;
	}

	public List<NodeElement> getAllNodes(){
		return this.nodes;
	}

	public void setMaxX(int x){
		this.maxX = x;
	}
	
	public void setMaxY(int y){
		this.maxY = y;
	}
	
	public int getMaxX() {
		return maxX;
	}

	public int getMaxY() {
		return maxY;
	}

	public abstract String getConnectionType();

	public abstract String getName() ;

	public abstract void setConnections() ;

}
