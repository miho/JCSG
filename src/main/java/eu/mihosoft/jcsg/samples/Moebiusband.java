/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg.samples;

import eu.mihosoft.jcsg.CSG;
import eu.mihosoft.jcsg.Extrude;
import eu.mihosoft.jcsg.FileUtil;
import eu.mihosoft.vvecmath.Transform;
import eu.mihosoft.vvecmath.Vector3d;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class Moebiusband {

    public CSG toCSG() {

        double width = 10;
        double height = 20;

        List<Vector3d> points = Arrays.asList(
                Vector3d.xy(-width / 2, -height / 2),
                Vector3d.xy(width / 2, -height / 2),
                Vector3d.xy(width / 2, height / 2),
                Vector3d.xy(-width / 2, height / 2));

        List<CSG> originalFacets = new ArrayList<>();

        List<CSG> facets = new ArrayList<>();

        CSG prev = null;

        for (int i = 0; i < 10; i++) {

            Transform t = Transform.unity().translateZ(2).rotZ(i);

            CSG facet = Extrude.points(Vector3d.xyz(0, 0, 1), points);

            if (prev != null) {
                facets.add(facet.union(prev).hull());
            }

            originalFacets.add(facet);

            points.stream().forEach((p) -> t.transform(p));

            prev = facet;
        }

        CSG result = facets.get(0);

        for (int i = 1; i < facets.size(); i++) {
            result = result.union(facets.get(i));
        }

        CSG originalResult = originalFacets.get(0);

        for (int i = 1; i < facets.size(); i++) {
            originalResult = originalResult.union(originalFacets.get(i));
        }

        return result.union(originalResult.transformed(Transform.unity().translateX(width * 2)));
    }

    public static void main(String[] args) throws IOException {

        System.out.println("RUNNING");

        FileUtil.write(Paths.get("möbiusband.stl"), new Moebiusband().toCSG().toStlString());

    }
}
