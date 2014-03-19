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
import java.util.List;
import java.util.Optional;

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
        return extrude(dir, Polygon.fromPoints(points));
    }

    private static CSG extrude(Vector3d dir, Polygon polygon1) {
        List<Polygon> newPolygons = new ArrayList<>();
        
        double direction = polygon1.plane.normal.dot(dir);
       
        if (direction > 0) {
            System.out.println("Extrude: CW -> CCW");
            polygon1 = polygon1.flipped();
        }

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

        return CSG.fromPolygons(newPolygons);

    }
}
