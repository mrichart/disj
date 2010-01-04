package distributed.plugin.ui.models.topologies;

public class SpatialLink {

	public SpatialPoint p1, p2;

	public SpatialLink() {
		p1 = null;
		p2 = null;
	}

	public SpatialLink(SpatialPoint p1, SpatialPoint p2) {
		this.p1 = p1;
		this.p2 = p2;
	}
}
