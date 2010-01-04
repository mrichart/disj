package distributed.plugin.ui.models.topologies;

import org.eclipse.draw2d.geometry.Point;

public class SpatialPoint {
	
	public int index;
	public double x, y, z;

	public SpatialPoint (){
		this(-1, 0, 0, 0);
	}
	public SpatialPoint (int index, Point location){
		this(index, location.x, location.y, 0);
	}
	public SpatialPoint(int index, double x, double y, double z) {
		this.index = index;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public String toString() {
		return this.index + "(" + x + "," + y + ")";
	}
}
