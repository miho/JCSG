/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d.ext.org.poly2tri;

import eu.mihosoft.vrl.v3d.Plane;
import eu.mihosoft.vrl.v3d.Vector3d;
import eu.mihosoft.vrl.v3d.Vertex;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class PolygonUtil {

    private PolygonUtil() {
        throw new AssertionError("Don't instantiate me!", null);
    }

    public static List<eu.mihosoft.vrl.v3d.Polygon> concaveToConvex(eu.mihosoft.vrl.v3d.Polygon concave) {

        List<eu.mihosoft.vrl.v3d.Polygon> result = new ArrayList<>();

        Vector3d normal = concave.vertices.get(0).normal.clone();

        Plane plane = concave.plane.clone();

        boolean cw = plane.dist > 0;

        List< PolygonPoint> points = new ArrayList<>();

        for (Vertex v : concave.vertices) {
            PolygonPoint vp = new PolygonPoint(v.pos.x, v.pos.y, v.pos.z);
            points.add(vp);
        }

        eu.mihosoft.vrl.v3d.ext.org.poly2tri.Polygon p
                = new eu.mihosoft.vrl.v3d.ext.org.poly2tri.Polygon(points);
        eu.mihosoft.vrl.v3d.ext.org.poly2tri.Poly2Tri.triangulate(p);

        List<DelaunayTriangle> triangles = p.getTriangles();

        List<Vertex> triPoints = new ArrayList<>();

        for (DelaunayTriangle t : triangles) {

            int counter = 0;
            for (TriangulationPoint tp : t.points) {

                triPoints.add(new Vertex(
                        new Vector3d(tp.getX(), tp.getY(), tp.getZ()),
                        normal));

                if (counter == 2) {
                    if (!cw) {
                        Collections.reverse(triPoints);
                    }
                    eu.mihosoft.vrl.v3d.Polygon poly = new eu.mihosoft.vrl.v3d.Polygon(triPoints);
                    result.add(poly);
                    counter = 0;
                    triPoints = new ArrayList<>();
                    System.out.println(poly.toStlString());

                } else {
                    counter++;
                }
            }
        }

        return result;
    }
}
