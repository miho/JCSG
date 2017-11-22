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
			CSG sliceICSG = incoming.transformed(slicePlane).toolOffset(normalInsetDistance);
			Node a = new Node(sliceICSG.getPolygons());
			Node b = new Node(planeCSG.getPolygons());
			a.invert();
			b.clipTo(a);
			b.invert();
			a.clipTo(b);
			b.clipTo(a);
			List<Polygon> polygons = b.allPolygons();
			for (int i = 0; i < polygons.size(); i++) {
				Polygon p = polygons.get(i);
				if (isPolygonAtZero(p)) {
					rawPolygons.add(p);
				}
			}

			// return Edge.boundaryPolygonsOfPlaneGroup(rawPolygons)
			ArrayList<Vertex> uniquePoints = new ArrayList<>();
			ArrayList<ArrayList<Edge>> edges = new ArrayList<>();
			for (int j = 0; j < rawPolygons.size(); j++) {
				Polygon it = rawPolygons.get(j);
				ArrayList<Edge> newList = new ArrayList<>();
				edges.add(newList);
				List<Vertex> vertices = it.vertices;
				for (int i = 0; i < vertices.size() - 1; i++) {
					try {
						Edge e = new Edge(getUnique(vertices.get(i), uniquePoints),
								getUnique(vertices.get(i + 1), uniquePoints));
						if (e.getP1() != e.getP2())
							newList.add(e);
					} catch (Exception ex) {
						// println "Point Pruned "
					}
				}
				try {
					Edge e = new Edge(getUnique(vertices.get(vertices.size() - 1), uniquePoints),
							getUnique(vertices.get(0), uniquePoints));
					if (e.getP1() != e.getP2())
						newList.add(e);
				} catch (Exception ex) {
					// println "Point Pruned "
				}
			}

			for (int k = 0; k < edges.size(); k++) {
				ArrayList<Edge> it = edges.get(k);
				for (int l = 0; l < it.size(); l++) {
					Edge myEdge = it.get(l);
					for (int i = 0; i < edges.size(); i++) {// for each edge we
															// cheack every
															// other edge
						ArrayList<Edge> testerList = edges.get(i);
						for (int j = 0; j < testerList.size(); j++) {
							Edge tester = testerList.get(j);
							boolean sharedEndPoints = myEdge.getP1().pos.equals(tester.getP1().pos)
									|| myEdge.getP1().pos.equals(tester.getP2().pos)
									|| myEdge.getP2().pos.equals(tester.getP1().pos)
									|| myEdge.getP2().pos.equals(tester.getP2().pos);

							if (!sharedEndPoints) {

								if (tester.contains(myEdge.getP1().pos)) {
									testerList.remove(tester);
									testerList.add(j, new Edge(tester.getP1(), myEdge.getP1()));
									testerList.add(j + 1, new Edge(myEdge.getP1(), tester.getP2()));

								}
								if (tester.contains(myEdge.getP2().pos)) {
									testerList.remove(tester);
									testerList.add(j, new Edge(tester.getP1(), myEdge.getP2()));
									testerList.add(j + 1, new Edge(myEdge.getP2(), tester.getP2()));

								}
							}
						}
					}
				}
			}
			List<Polygon> fixed = new ArrayList<>();

			for (int i = 0; i < edges.size(); i++) {
				ArrayList<Edge> it = edges.get(i);
				fixed.add(Edge.toPolygon(Edge.toPoints(it), Plane.XY_PLANE));
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
