/**
 * PolygonUtil.java
 *
 * Copyright 2014-2014 Michael Hoffer <info@michaelhoffer.de>. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info@michaelhoffer.de> "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info@michaelhoffer.de> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of Michael Hoffer <info@michaelhoffer.de>.
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
