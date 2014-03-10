/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.mihosoft.vrl.v3d;

import eu.mihosoft.vrl.v3d.ext.imagej.STLLoader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Point3f;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class STL {
    public static CSG file(Path path) throws IOException {
        STLLoader loader = new STLLoader();
        
        List<Polygon> polygons = new ArrayList<>();
        List<Vector3d> vertices = new ArrayList<>();
        for(Point3f p :loader.parse(path.toFile())) {
            vertices.add(new Vector3d(p.x, p.y, p.z));
            if (vertices.size()==3) {
                polygons.add(Polygon.fromPoints(vertices));
                vertices = new ArrayList<>();
            }
        }
        
        return CSG.fromPolygons(polygons);
    }
}
