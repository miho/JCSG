/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d;

import eu.mihosoft.vrl.v3d.ext.org.poly2tri.PolygonUtil;
import java.util.ArrayList;
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
     * intersections) into the specified direction.
     *
     * @param dir direction
     * @param points path (convex or concave polygon without holes or
     * intersections)
     *
     * @return a CSG object that consists of the extruded polygon
     */
    public static CSG points(Vector3d dir, Vector3d... points) {
        return extrude(dir, Polygon.fromPoints(points));
    }

    private static CSG extrude(Vector3d dir, Polygon polygon1) {
        List<Polygon> newPolygons = new ArrayList<>();

        double direction = polygon1.plane.normal.dot(dir);

        if (direction > 0) {
            polygon1 = polygon1.flipped();
        }

//        newPolygons.add(polygon1);
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
                    sidefacepoints, polygon1.shared);
            newPolygons.add(sidefacepolygon);
        }

        polygon2 = polygon2.flipped();
        newPolygons.addAll(PolygonUtil.concaveToConvex(polygon2));

//        newPolygons.add(polygon2);
        return CSG.fromPolygons(newPolygons);

    }
}
