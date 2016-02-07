package eu.mihosoft.vrl.v3d.ext.org.poly2tri;

public class DistanceFieldsContext extends TriangulationContext<DistanceFieldsDebugContext> {

	@Override
	public TriangulationAlgorithm algorithm() {
		return TriangulationAlgorithm.DistanceFields;
	}

	@Override
	public TriangulationConstraint newConstraint(TriangulationPoint a, TriangulationPoint b) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void isDebugEnabled(boolean b) {
		// TODO Auto-generated method stub
	}

}
