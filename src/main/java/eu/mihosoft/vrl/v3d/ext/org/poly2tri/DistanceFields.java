package eu.mihosoft.vrl.v3d.ext.org.poly2tri;

import java.util.ArrayList;
import java.util.List;

public class DistanceFields {
	private double smallestEdge = 0.1;
	public static void triangulate(DistanceFieldsContext tcx) {

		List<TriangulationPoint> points;
		ArrayList<Edge> edges =new ArrayList<Edge>();
		ArrayList<Edge> allEdges =new ArrayList<Edge>();
		points = tcx.getPoints();
		Triangulatable polygon = tcx.getTriangulatable();
		
		System.out.println("Have "+points.size()+" points in polygon");
		TriangulationPoint startingPoint = points.get(0);
		TriangulationPoint lastPoint=startingPoint;
		// Load up all of the exterior edges
		for (int i = 1; i < points.size(); i++) {
			Edge e =new Edge(lastPoint, points.get(i));
			edges.add(e);
			allEdges.add(e);
			lastPoint=points.get(i);
		}
		edges.add(new Edge(lastPoint, startingPoint));// stiched back together
		for(Edge e:edges){
			// Iterate over each edge and find its mating point
			
			TriangulationPoint finalPont = null;
			for(TriangulationPoint t:points){
				if((!t.equals(e.p)) && (!t.equals(e.q))){
					Edge alpha =new Edge(e.p, t);
					Edge beta =new Edge(e.q, t);
					boolean cross=false;
					for(Edge ch:allEdges){
						if(ch.checkForCrossing(alpha) || ch.checkForCrossing(beta)){
							cross=true;
							break;
						}
					}
					if(!cross){
						finalPont=t;
						allEdges.add(alpha);
						allEdges.add(beta);
					}
				}
				if(finalPont!=null)
					break;// final point for edge found
			}
			
			
			DelaunayTriangle edgeTri = new DelaunayTriangle(e.p, e.q, finalPont);
			
			polygon.addTriangle(edgeTri);
		}
		
	}

}
