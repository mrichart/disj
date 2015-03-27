package distributed.plugin.ui.models.topologies;

public class SpatialTriangle {

	public SpatialPoint p1, p2, p3;

	public SpatialTriangle() {
		p1 = null;
		p2 = null;
		p3 = null;
	}

	public SpatialTriangle(SpatialPoint p1, SpatialPoint p2, SpatialPoint p3) {
		this.p1 = p1;
		this.p2 = p2;
		this.p3 = p3;
	}

	public boolean sharesVertex(SpatialTriangle other) {
		return p1 == other.p1 || p1 == other.p2 || p1 == other.p3
				|| p2 == other.p1 || p2 == other.p2 || p2 == other.p3
				|| p3 == other.p1 || p3 == other.p2 || p3 == other.p3;
	}

}
