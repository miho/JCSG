package eu.mihosoft.vrl.v3d;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.vecmath.Matrix4d;

import eu.mihosoft.vrl.v3d.ext.org.poly2tri.DelaunayTriangle;
import eu.mihosoft.vrl.v3d.ext.org.poly2tri.PolygonUtil;

public class Slice {
	private static ISlice sliceEngine = new ISlice() {

		double length(Edge e) {

			return Math.sqrt(
					Math.pow(e.getP1().getX() - e.getP2().getX(), 2) + Math.pow(e.getP1().getY() - e.getP2().getY(), 2)
							+ Math.pow(e.getP1().getZ() - e.getP2().getZ(), 2));
		}

		boolean touching(Vertex p1, Vertex p2) {
			double COINCIDENCE_TOLERANCE = 0.001;
			if (Math.abs(p1.getX() - p2.getX()) > COINCIDENCE_TOLERANCE) {
				return false;
			}
			if (Math.abs(p1.getY() - p2.getY()) > COINCIDENCE_TOLERANCE) {
				return false;
			}
			if (Math.abs(p1.getZ() - p2.getZ()) > COINCIDENCE_TOLERANCE) {
				return false;
			}
			return true;
		}

		Vertex getUnique(Vertex desired, ArrayList<Vertex> uniquePoints) {
			if (Math.abs(desired.getZ()) > 0.0001) {
				// println "Bad point! "+desired
				throw new RuntimeException("Bad point!");
			}
			for (Vertex existing : uniquePoints)
				if (touching(desired, existing)) {
					return existing;
				}
			uniquePoints.add(desired);
			return desired;
		}

		/**
		 * An interface for slicking CSG objects into lists of points that can
		 * be extruded back out
		 * 
		 * @param incoming
		 *            Incoming CSG to be sliced
		 * @param slicePlane
		 *            Z coordinate of incoming CSG to slice at
		 * @param normalInsetDistance
		 *            Inset for sliced output
		 * @return A set of polygons defining the sliced shape
		 */
		public List<Polygon> slice(CSG incoming, Transform slicePlane, double normalInsetDistance) {
			// println "Groovy Slicing engine"

			List<Polygon> rawPolygons = new ArrayList<>();

			// Actual slice plane
			CSG planeCSG = incoming.getBoundingBox().toZMin();
			// Loop over each polygon in the slice of the incoming CSG
			// Add the polygon to the final slice if it lies entirely in the z
			// plane
			for (Polygon p : incoming.transformed(slicePlane).intersect(planeCSG).toolOffset(normalInsetDistance)
					.getPolygons()) {
				if (isPolygonAtZero(p)) {
					rawPolygons.add(p);
				}
			}
			CSG flat = CSG.fromPolygons(rawPolygons);
			flat = flat.union(flat);

			// return Edge.boundaryPolygonsOfPlaneGroup(rawPolygons)
			ArrayList<Vertex> uniquePoints = new ArrayList<>();
			ArrayList<ArrayList<Edge>> edges = new ArrayList<>();
			for (int j = 0; j < rawPolygons.size(); j++) {
				// Polygon it = rawPolygons.get(j);
				ArrayList<Edge> newList = new ArrayList<>();
				edges.add(newList);
				// List<Vertex> vertices = it.vertices;
				for (int i = 0; i < rawPolygons.get(j).vertices.size() - 1; i++) {
					try {
						newList.add(new Edge(getUnique(rawPolygons.get(j).vertices.get(i), uniquePoints),
								getUnique(rawPolygons.get(j).vertices.get(i + 1), uniquePoints)));
					} catch (Exception ex) {
						// println "Point Pruned "
					}
				}
				try {
					boolean add = newList.add(
							new Edge(
									getUnique(rawPolygons.get(j).vertices.get(rawPolygons.get(j).vertices.size() - 1),
											uniquePoints),
									getUnique(rawPolygons.get(j).vertices.get(0), uniquePoints)));
				} catch (Exception ex) {
					// println "Point Pruned "
				}
			}

			for (int l = 0; l < edges.size(); l++) {
				// ArrayList<Edge> it = edges.get(l);
				for (int k = 0; k < edges.get(l).size(); k++) {
					// Edge myEdge = it.get(k);
					for (int i = 0; i < edges.size(); i++) {// for each edge we
															// cheack every
															// other edge
						// ArrayList<Edge> testerList = edges.get(i);
						for (int j = 0; j < edges.get(i).size(); j++) {
							// Edge tester=edges.get(i).get(j);
							if (edges.get(i).get(j) == edges.get(l).get(k)) {
								continue;// skip comparing to itself
							}
							if (Edge.falseBoundaryEdgeSharedWithOtherEdge(edges.get(i).get(j), edges.get(l).get(k))

							) {

								edges.get(i).remove(edges.get(i).get(j));
								double lenghtFirstToFirst = length(
										new Edge(edges
													.get(i).get(j)
													.getP1(), 
												edges
													.get(l).get(k)
													.getP1()));
								double lenghtFirstToSecond = length(
										new Edge(
												edges
													.get(i).get(j)
													.getP1(), 
												edges
													.get(l).get(k)
													.getP2()));
								if (lenghtFirstToFirst < lenghtFirstToSecond) {
									edges.get(i).add(j,
											new Edge(edges.get(i).get(j).getP1(), edges.get(l).get(k).getP1()));
									edges.get(i).add(j + 1,
											new Edge(edges.get(l).get(k).getP1(), edges.get(i).get(j).getP2()));
								} else {
									edges.get(i).add(j,
											new Edge(edges.get(i).get(j).getP1(), edges.get(l).get(k).getP2()));
									edges.get(i).add(j + 1,
											new Edge(edges.get(l).get(k).getP2(), edges.get(i).get(j).getP2()));
								}

								// println "Line touching but not the same!
								// "+length(myEdge)+" other "+length(tester)

							}
						}
					}
				}
			}
			List<Polygon> fixed = new ArrayList<>();

			for (int i = 0; i < edges.size(); i++) {
				//ArrayList<Edge> it = edges.get(i);
				fixed.add(Edge.toPolygon(Edge.toPoints(edges.get(i)), Plane.XY_PLANE));
			}

			// return fixed
			List<Polygon> triangles = new ArrayList<>();
			for (int i = 0; i < fixed.size(); i++) {
				eu.mihosoft.vrl.v3d.ext.org.poly2tri.Polygon p = PolygonUtil.fromCSGPolygon(fixed.get(i));
				eu.mihosoft.vrl.v3d.ext.org.poly2tri.Poly2Tri.triangulate(p);
				List<DelaunayTriangle> t = p.getTriangles();
				for (int j = 0; j < t.size(); j++)
					triangles.add(t.get(j).toPolygon());
			}
			// return triangles
			return Edge.boundaryPathsWithHoles(Edge.boundaryPaths(Edge.boundaryEdgesOfPlaneGroup(triangles)));
		}

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
