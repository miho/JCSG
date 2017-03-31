package eu.mihosoft.vrl.v3d;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.vecmath.Matrix4d;

import eu.mihosoft.vrl.v3d.ext.org.poly2tri.DelaunayTriangle;
import eu.mihosoft.vrl.v3d.ext.org.poly2tri.PolygonUtil;

public class Slice {
	private static ISlice sliceEngine = (incoming, slicePlane, normalInsetDistance) -> {
		double COINCIDENCE_TOLERANCE = 0.0001;
		List<Polygon> rawPolygons = new ArrayList<>();

		// Invert the incoming transform
		Matrix4d inverse = slicePlane.scale(1.0D / slicePlane.getScale()).getInternalMatrix();
		inverse.invert();

		// Actual slice plane
		CSG planeCSG = new Cube(incoming.getMaxX() - incoming.getMinX(), incoming.getMaxY() - incoming.getMinY(), 1)
				.noCenter().toCSG();
		planeCSG = planeCSG.movex((planeCSG.getMaxX() - planeCSG.getMinX()) / -2.0D)
				.movey((planeCSG.getMaxY() - planeCSG.getMinY()) / -2.0D);
		incoming.getPolygons();

		// Loop over each polygon in the slice of the incoming CSG
		// Add the polygon to the final slice if it lies entirely in the z plane
		rawPolygons.addAll(incoming.intersect(planeCSG).getPolygons().stream().filter(Slice::isPolygonAtZero)
				.collect(Collectors.toList()));

		/* Convert the list of polygons to a list of triangles */
		List<Polygon> triangles = new ArrayList<>();
		for (int i = 0; i < rawPolygons.size(); i++) {
			eu.mihosoft.vrl.v3d.ext.org.poly2tri.Polygon p = PolygonUtil.fromCSGPolygon(rawPolygons.get(i));
			eu.mihosoft.vrl.v3d.ext.org.poly2tri.Poly2Tri.triangulate(p);
			List<DelaunayTriangle> t = p.getTriangles();
			for (int j = 0; j < t.size(); j++)
				triangles.add(t.get(j).toPolygon());
		}

		/* List every edge */
		List<Edge> edges = new ArrayList<>();
		for (Polygon t : triangles) {
			edges.add(new Edge(t.vertices.get(0), t.vertices.get(1)));
			edges.add(new Edge(t.vertices.get(1), t.vertices.get(2)));
			edges.add(new Edge(t.vertices.get(2), t.vertices.get(0)));
		}

		/* Remove internal edges */
		for (int i = 0; i < edges.size(); i++) {
			boolean match = false;
			for (int j = 0; j < edges.size() && !match; j++) {
				if (edges.get(i).getP1().pos.minus(edges.get(j).getP2().pos).magnitude() <= COINCIDENCE_TOLERANCE
						&& edges.get(i).getP2().pos.minus(edges.get(j).getP1().pos)
								.magnitude() <= COINCIDENCE_TOLERANCE) {
					edges.remove(i);
					edges.remove(j);
					i--;
					match = false;
				}
			}
		}

		/* Generate polygons from edges */
		List<Polygon> polygons = new ArrayList<>();
		for (int edgeIndex = 0; edges.size() > 0;) {
			List<Vertex> vertices = new ArrayList<>();
			vertices.add(edges.get(0).getP1());
			for (; edges.get(edgeIndex).getP2().pos.minus(vertices.get(0).pos).magnitude() <= COINCIDENCE_TOLERANCE;) {
				vertices.add(edges.get(edgeIndex).getP2());
				edges.remove(edgeIndex);
				for (; edgeIndex < edges.size() && !(vertices.get(vertices.size() - 1).pos
						.minus(edges.get(edgeIndex).getP1().pos).magnitude() <= COINCIDENCE_TOLERANCE); edgeIndex++)
					;
			}
			edges.remove(edgeIndex);
			polygons.add(new Polygon(vertices));
		}

		return polygons;
	};

	/**
	 * Returns true if this polygon lies entirely in the z plane
	 *
	 * @param polygon
	 *            The polygon to check
	 * @return True if this polygon is entirely in the z plane
	 */
	private static boolean isPolygonAtZero(Polygon polygon) {
		// Return false if there is a vertex in this polygon which is not at
		// zero
		// Else, the polygon is at zero if every vertex in it is at zero
		for (Vertex v : polygon.vertices)
			if (!isVertexAtZero(v))
				return false;

		return true;
	}

	/**
	 * Returns true if this vertex is at z coordinate zero
	 *
	 * @param vertex
	 *            The vertex to check
	 * @return True if this vertex is at z coordinate zero
	 */
	private static boolean isVertexAtZero(Vertex vertex) {
		// The upper and lower bounds for checking the vertex z coordinate
		// against
		final double SLICE_UPPER_BOUND = 0.001, SLICE_LOWER_BOUND = -0.001;

		// The vertex is at zero if it is within tight bounds (to account for
		// floating point error)
		return vertex.getZ() < SLICE_UPPER_BOUND && vertex.getZ() > SLICE_LOWER_BOUND;
	}

	public static List<Polygon> slice(CSG incoming, Transform slicePlane, double normalInsetDistance) {
		return getSliceEngine().slice(incoming, slicePlane, normalInsetDistance);
	}

	public static ISlice getSliceEngine() {
		return sliceEngine;
	}

	public static void setSliceEngine(ISlice sliceEngine) {
		Slice.sliceEngine = sliceEngine;
	}
}
