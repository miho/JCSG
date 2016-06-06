package eu.mihosoft.vrl.v3d;

import javax.vecmath.Matrix4d;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Slice
{
	private static ISlice sliceEngine = (incoming, slicePlane, normalInsetDistance) -> {
        List<Polygon> finalSlice = new ArrayList<>();

		//Invert the incoming transform
		Matrix4d inverse = slicePlane.scale(1.0D / slicePlane.getScale()).getInternalMatrix();
		inverse.invert();

		//Actual slice plane
		CSG planeCSG = new Cube(incoming.getMaxX() - incoming.getMinX(), incoming.getMaxY() - incoming.getMinY(), 1).noCenter().toCSG();
		planeCSG = planeCSG.movex((planeCSG.getMaxX() - planeCSG.getMinX()) / -2.0D).movey((planeCSG.getMaxY() - planeCSG.getMinY()) / -2.0D);

		//Loop over each polygon in the slice of the incoming CSG
		//Add the polygon to the final slice if it lies entirely in the z plane
		finalSlice.addAll(incoming.intersect(planeCSG).getPolygons().stream()
					.filter(Slice::isPolygonAtZero)
					.collect(Collectors.toList()));

        return finalSlice;
    };

	/**
	 * Returns true if this polygon lies entirely in the z plane
	 *
	 * @param polygon The polygon to check
	 * @return		  True if this polygon is entirely in the z plane
     */
	private static boolean isPolygonAtZero(Polygon polygon)
	{
		//Return false if there is a vertex in this polygon which is not at zero
		//Else, the polygon is at zero if every vertex in it is at zero
		for (Vertex v : polygon.vertices)
			if (!isVertexAtZero(v))
				return false;

		return true;
	}

	/**
	 * Returns true if this vertex is at z coordinate zero
	 *
	 * @param vertex The vertex to check
	 * @return		 True if this vertex is at z coordinate zero
     */
	private static boolean isVertexAtZero(Vertex vertex)
	{
		//The upper and lower bounds for checking the vertex z coordinate against
		final double SLICE_UPPER_BOUND = 0.001, SLICE_LOWER_BOUND = -0.001;

		//The vertex is at zero if it is within tight bounds (to account for floating point error)
		return vertex.getZ() < SLICE_UPPER_BOUND && vertex.getZ() > SLICE_LOWER_BOUND;
	}

	public static  List<Polygon> slice(CSG incoming, Transform slicePlane, double normalInsetDistance) {
		return getSliceEngine().slice(incoming, slicePlane, normalInsetDistance);
	}
	
	
	public static ISlice getSliceEngine() {
		return sliceEngine;
	}
	public static void setSliceEngine(ISlice sliceEngine) {
		Slice.sliceEngine = sliceEngine;
	}

}
