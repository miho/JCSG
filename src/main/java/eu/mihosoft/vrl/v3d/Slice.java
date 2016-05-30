package eu.mihosoft.vrl.v3d;

import javax.vecmath.Matrix4d;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class Slice
{
	private static final double SLICE_UPPER_BOUND = 0.001, SLICE_LOWER_BOUND = -0.001;

	private static ISlice sliceEngine = (incoming, slicePlane, normalInsetDistance) -> {
        List<Vector3d> slicedPoints = new ArrayList<>();

		//Invert the incoming transform
		Matrix4d inverse = slicePlane.scale(1.0D / slicePlane.getScale()).getInternalMatrix();
		inverse.invert();

		//Actual slice plane
		CSG planeCSG = new Cube(incoming.getMaxX() - incoming.getMinX(), incoming.getMaxY() - incoming.getMinY(), 1).noCenter().toCSG();
		planeCSG = planeCSG.movex((planeCSG.getMaxX() - planeCSG.getMinX()) / 2).movey((planeCSG.getMaxY() - planeCSG.getMinY()) / 2);

		//Loop over each polygon in the slice of the incoming CSG
		for (Polygon polygon : incoming.transformed(new Transform(inverse)).intersect(planeCSG).getPolygons())
		{
			//Add each vertex at z == 0 to a list as a vector3d
			slicedPoints.addAll(polygon.vertices.stream()
					.filter(v -> v.getZ() < SLICE_UPPER_BOUND && v.getZ() > SLICE_LOWER_BOUND)
					.map(v -> new Vector3d(v.getX(), v.getY(), v.getZ()))
					.collect(Collectors.toList()));
		}

        return slicedPoints;
    };

	public static  List<Vector3d> slice(CSG incoming, Transform slicePlane, double normalInsetDistance) {
		return getSliceEngine().slice(incoming, slicePlane, normalInsetDistance);
	}
	
	
	public static ISlice getSliceEngine() {
		return sliceEngine;
	}
	public static void setSliceEngine(ISlice sliceEngine) {
		Slice.sliceEngine = sliceEngine;
	}

}
