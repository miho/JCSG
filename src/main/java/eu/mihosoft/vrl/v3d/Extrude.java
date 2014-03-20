/**
 * Extrude.java
 *
 * Copyright 2014-2014 Michael Hoffer <info@michaelhoffer.de>. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info@michaelhoffer.de> "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info@michaelhoffer.de> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of Michael Hoffer
 * <info@michaelhoffer.de>.
 */
package eu.mihosoft.vrl.v3d;

import eu.mihosoft.vrl.v3d.ext.org.poly2tri.PolygonUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Extrudes concave and convex polygons.
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class Extrude {

    private Extrude() {
        throw new AssertionError("Don't instantiate me!", null);
    }

    /**
     * Extrudes the specified path (convex or concave polygon without holes or
     * intersections, specified in CCW) into the specified direction.
     *
     * @param dir direction
     * @param points path (convex or concave polygon without holes or
     * intersections)
     *
     * @return a CSG object that consists of the extruded polygon
     */
    public static CSG points(Vector3d dir, Vector3d... points) {

        return extrude(dir, Polygon.fromPoints(toCCW(Arrays.asList(points))));
    }

    /**
     * Extrudes the specified path (convex or concave polygon without holes or
     * intersections, specified in CCW) into the specified direction.
     *
     * @param dir direction
     * @param points path (convex or concave polygon without holes or
     * intersections)
     *
     * @return a CSG object that consists of the extruded polygon
     */
    public static CSG points(Vector3d dir, List<Vector3d> points) {

        List<Vector3d> newList = new ArrayList<>(points);

        return extrude(dir, Polygon.fromPoints(toCCW(newList)));
    }

    private static CSG extrude(Vector3d dir, Polygon polygon1) {
        List<Polygon> newPolygons = new ArrayList<>();

//        double direction = polygon1.plane.normal.dot(dir);
//
//        if (direction > 0) {
//            System.out.println("Extrude: CW -> CCW");
//            polygon1 = polygon1.flipped();
//        }
        newPolygons.addAll(PolygonUtil.concaveToConvex(polygon1));

        Polygon polygon2 = polygon1.translated(dir);

        int numvertices = polygon1.vertices.size();
        for (int i = 0; i < numvertices; i++) {
            List<Vector3d> sidefacepoints = new ArrayList<>();
            int nexti = (i < (numvertices - 1)) ? i + 1 : 0;
            sidefacepoints.add(polygon1.vertices.get(i).pos);
            sidefacepoints.add(polygon2.vertices.get(i).pos);
            sidefacepoints.add(polygon2.vertices.get(nexti).pos);
            sidefacepoints.add(polygon1.vertices.get(nexti).pos);
            Polygon sidefacepolygon = Polygon.fromPoints(
                    (sidefacepoints), polygon1.shared);
            newPolygons.add(sidefacepolygon);
        }

//        polygon2 = polygon2.flipped();
        List<Polygon> topPolygons = PolygonUtil.concaveToConvex(polygon2);

        for (Polygon polygon : topPolygons) {
            polygon.flip();
        }

        newPolygons.addAll(topPolygons);

        return CSG.fromPolygons(newPolygons);

    }

    private static CSG extrudeWithSubPoly(Vector3d dir, Polygon polygon1) {
        List<Polygon> newPolygons = new ArrayList<>();

//        double direction = polygon1.plane.normal.dot(dir);
//
//        if (direction > 0) {
//            System.out.println("Extrude: CW -> CCW");
//            polygon1 = polygon1.flipped();
//        }
        
        List<Polygon> bottomPolygons = PolygonUtil.concaveToConvex(polygon1);

        for (Polygon bottom : bottomPolygons) {
            
            newPolygons.add(bottom);
            
            Polygon top = bottom.translated(dir);

            int numvertices = bottom.vertices.size();
            for (int i = 0; i < numvertices; i++) {
                List<Vector3d> sidefacepoints = new ArrayList<>();
                int nexti = (i < (numvertices - 1)) ? i + 1 : 0;
                sidefacepoints.add(bottom.vertices.get(i).pos);
                sidefacepoints.add(top.vertices.get(i).pos);
                sidefacepoints.add(top.vertices.get(nexti).pos);
                sidefacepoints.add(bottom.vertices.get(nexti).pos);
                Polygon sidefacepolygon = Polygon.fromPoints(
                        sidefacepoints, bottom.shared);
                newPolygons.add(sidefacepolygon);
            }
            
            newPolygons.add(top.flipped());

        }

////        polygon2 = polygon2.flipped();
//        List<Polygon> topPolygons = PolygonUtil.concaveToConvex(polygon2);
//
//        for (Polygon polygon : topPolygons) {
//            polygon.flip();
//        }

//        newPolygons.addAll(topPolygons);

        return CSG.fromPolygons(newPolygons);

    }

    public static List<Vector3d> toCCW(List<Vector3d> points) {
        
        List<Vector3d> result = new ArrayList<>(points);
        
        if (!isCCW(Polygon.fromPoints(result))) {
            Collections.reverse(result);
        }

        return result;
    }
    
    public static List<Vector3d> toCW(List<Vector3d> points) {
        
        List<Vector3d> result = new ArrayList<>(points);
        
        if (isCCW(Polygon.fromPoints(result))) {
            Collections.reverse(result);
        }

        return result;
    }

    public static boolean isCCW(Polygon polygon) {

        if (polygon.vertices.size() < 3) {
            throw new IllegalArgumentException("Only polygons with at least 3 vertices are supported!");
        }

        // search highest left vertex
        int highestLeftVertexIndex = 0;
        Vertex highestLeftVertex = polygon.vertices.get(0);
        for (int i = 0; i < polygon.vertices.size(); i++) {
            Vertex v = polygon.vertices.get(i);

            if (v.pos.y > highestLeftVertex.pos.y) {
                highestLeftVertex = v;
                highestLeftVertexIndex = i;
            } else if (v.pos.y == highestLeftVertex.pos.y && v.pos.x < highestLeftVertex.pos.x) {
                highestLeftVertex = v;
                highestLeftVertexIndex = i;
            }
        }

        // determine next and previous vertex indices
        int nextVertexIndex = (highestLeftVertexIndex + 1) % polygon.vertices.size();
        int prevVertexIndex = highestLeftVertexIndex - 1;
        if (prevVertexIndex < 0) {
            prevVertexIndex = polygon.vertices.size() - 1;
        }
        Vertex nextVertex = polygon.vertices.get(nextVertexIndex);
        Vertex prevVertex = polygon.vertices.get(prevVertexIndex);

        // edge 1
        double a1 = normalizedX(highestLeftVertex.pos, nextVertex.pos);

        // edge 2
        double a2 = normalizedX(highestLeftVertex.pos, prevVertex.pos);

        // select vertex with lowest x value
        int selectedVIndex;

        if (a2 > a1) {
            selectedVIndex = nextVertexIndex;
        } else {
            selectedVIndex = prevVertexIndex;
        }

        // indicates whether edge points towards the highestLeftVertexIndex (ccw/cw)
        return selectedVIndex < highestLeftVertexIndex;
    }

    private static double normalizedX(Vector3d v1, Vector3d v2) {
        Vector3d v2MinusV1 = v2.minus(v1);

        return v2MinusV1.dividedBy(v2MinusV1.magnitude()).times(Vector3d.X_ONE).x;
    }
}
