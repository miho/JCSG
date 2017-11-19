package eu.mihosoft.vrl.v3d;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.vecmath.Matrix4d;

import eu.mihosoft.vrl.v3d.ext.org.poly2tri.DelaunayTriangle;
import eu.mihosoft.vrl.v3d.ext.org.poly2tri.PolygonUtil;

public class Slice {
	private static ISlice sliceEngine = new ISlice (){
		


		boolean touhing(Vertex point, Edge e){
			return e.contains(point.pos);
		}
		double length(Edge e){
			
			return Math.sqrt(Math.pow(e.getP1().getX()-e.getP2().getX(),2)+
			Math.pow(e.getP1().getY()-e.getP2().getY(),2)+
			Math.pow(e.getP1().getZ()-e.getP2().getZ(),2)
				);
		}
		
		boolean same(Edge point, Edge e){
			if(e.getP1()==point.getP1() && e.getP2()==point.getP2() )
				return true;
			if(e.getP1()==point.getP2() && e.getP2()==point.getP1() )
				return true	;
				
			return false;
		}
		boolean touching(Vertex p1, Vertex p2){
			double COINCIDENCE_TOLERANCE = 0.001;
			if(Math.abs( p1.getX()-p2.getX())>COINCIDENCE_TOLERANCE){
				return false;
			}
			if(Math.abs( p1.getY()-p2.getY())>COINCIDENCE_TOLERANCE){
				return false;
			}
			if(Math.abs( p1.getZ()-p2.getZ())>COINCIDENCE_TOLERANCE){
				return false;
			}
			return true;
		}
		Vertex getUnique(Vertex desired, ArrayList<Vertex> uniquePoints){
			if(Math.abs(desired.getZ())>0.0001){
				//println "Bad point! "+desired
				throw new RuntimeException("Bad point!");
			}
			for(Vertex existing:uniquePoints)
						if(	touching(desired,existing)){
							return 	existing;		
						}
			uniquePoints.add(desired);
			return desired;
		}

		
		/**
		 * An interface for slicking CSG objects into lists of points that can be extruded back out
		 * @param incoming			  Incoming CSG to be sliced
		 * @param slicePlane		  Z coordinate of incoming CSG to slice at
		 * @param normalInsetDistance Inset for sliced output
		 * @return					  A set of polygons defining the sliced shape
		 */
		public List<Polygon> slice(CSG incoming, Transform slicePlane, double normalInsetDistance){
			//println "Groovy Slicing engine"
			
			List<Polygon> rawPolygons = new ArrayList<>();

			// Actual slice plane
			CSG planeCSG = incoming.getBoundingBox()
					.toZMin();
			// Loop over each polygon in the slice of the incoming CSG
			// Add the polygon to the final slice if it lies entirely in the z plane
			for(Polygon p: incoming
					.intersect(planeCSG)						
					.getPolygons()){
				if(isPolygonAtZero(p)){
					rawPolygons.add(p);
				}
			}
			CSG flat = CSG.fromPolygons(rawPolygons);
			flat=flat.union(flat);
			
			//return Edge.boundaryPolygonsOfPlaneGroup(rawPolygons)		
			ArrayList<Vertex> uniquePoints = new ArrayList<>();
			ArrayList<ArrayList<Edge>> edges = new ArrayList<>();
			for(Polygon it: rawPolygons){
				ArrayList<Edge> newList = new ArrayList<>();
				edges.add(newList);
				List<Vertex> vertices = it.vertices;
				for(int i=0;i<vertices.size()-1;i++){
					try{
						newList.add(new Edge(getUnique(vertices.get(i),uniquePoints), getUnique(vertices.get(i+1),uniquePoints)));
					}catch(Exception ex){
						//println "Point Pruned "
					}
				}
				try{
					newList.add(new Edge(getUnique(vertices.get(vertices.size()-1),uniquePoints), getUnique(vertices.get(0),uniquePoints)));
				}catch(Exception ex){
					//println "Point Pruned "
				}
			}
			
			//edges.forEach{// search the list of all edges
			for(ArrayList<Edge> it: edges){
				for(Edge myEdge:it){// search through the edges in each list
					for(int i=0;i<edges.size();i++){// for each edge we cheack every other edge
						ArrayList<Edge> testerList = edges.get(i);
						for(int j=0;j<testerList.size();j++){
							Edge tester=testerList.get(j);
							if(tester==myEdge){
								continue;// skip comparing to itself
							}
							if(Edge.falseBoundaryEdgeSharedWithOtherEdge(tester,myEdge)
							
							){
								
								testerList.remove(tester);
								double lenghtFirstToFirst = length(new Edge(tester.getP1(),myEdge.getP1()));
								double lenghtFirstToSecond = length(new Edge(tester.getP1(),myEdge.getP2()));
								if(lenghtFirstToFirst<lenghtFirstToSecond){
									testerList.add(j,new Edge(tester.getP1(),myEdge.getP1()));
									testerList.add(j+1,new Edge(myEdge.getP1(),tester.getP2()));
								}else{
									testerList.add(j,new Edge(tester.getP1(),myEdge.getP2()));
									testerList.add(j+1,new Edge(myEdge.getP2(),tester.getP2()));
								}
								
								//println "Line touching but not the same! "+length(myEdge)+" other "+length(tester)
								
							}
						}
					}
				}
			}
			List<Polygon> fixed =  new ArrayList<>();
					

			for(ArrayList<Edge> it: edges){
				fixed.add( Edge.toPolygon(
						Edge.toPoints(it)
						,Plane.XY_PLANE));
			}

			//return fixed
			List<Polygon> triangles  = new ArrayList<>();
			for (int i = 0; i < fixed.size(); i++) {
				eu.mihosoft.vrl.v3d.ext.org.poly2tri.Polygon p = PolygonUtil.fromCSGPolygon(fixed.get(i));
				eu.mihosoft.vrl.v3d.ext.org.poly2tri.Poly2Tri.triangulate(p);
				List<DelaunayTriangle> t = p.getTriangles();
				for (int j = 0; j < t.size(); j++)
					triangles.add(t.get(j).toPolygon());
			}
			//return triangles
			return Edge.boundaryPathsWithHoles(
	                	Edge.boundaryPaths(
	                		Edge.boundaryEdgesOfPlaneGroup(triangles)));
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
