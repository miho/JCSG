package eu.mihosoft.vrl.v3d;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class Slice
{
	private static final CSG planeCSG = new Cube(10000, 10000, 1).noCenter().toCSG().movex(-5000).movey(-5000);

	private static ISlice sliceEngine = (incoming, slicePlane, normalInsetDistance) -> {
        List<Vector3d> slicedPoints = new ArrayList<>();

		for (Polygon polygon : incoming.movez(-slicePlane).intersect(planeCSG).getPolygons())
		{
			slicedPoints.addAll(polygon.vertices.stream().filter(v -> v.getZ() == 0).map(v -> new Vector3d(v.getX(), v.getY(), v.getZ())).collect(Collectors.toList()));
		}

        return slicedPoints;
    };

	public static  List<Vector3d> slice(CSG incoming, double slicePlane, double normalInsetDistance) {
		return getSliceEngine().slice(incoming, slicePlane, normalInsetDistance);
	}
	
	
	public static ISlice getSliceEngine() {
		return sliceEngine;
	}
	public static void setSliceEngine(ISlice sliceEngine) {
		Slice.sliceEngine = sliceEngine;
	}

}
