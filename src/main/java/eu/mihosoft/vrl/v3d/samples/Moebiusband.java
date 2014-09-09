/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d.samples;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Extrude;
import eu.mihosoft.vrl.v3d.FileUtil;
import eu.mihosoft.vrl.v3d.Transform;
import eu.mihosoft.vrl.v3d.Vector3d;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class Moebiusband {

    public CSG toCSG() {

        double width = 10;
        double height = 20;

        List<Vector3d> points = Arrays.asList(
                new Vector3d(-width / 2, -height / 2),
                new Vector3d(width / 2, -height / 2),
                new Vector3d(width / 2, height / 2),
                new Vector3d(-width / 2, height / 2));

        List<CSG> originalFacets = new ArrayList<>();

        List<CSG> facets = new ArrayList<>();

        CSG prev = null;

        for (int i = 0; i < 10; i++) {

            Transform t = Transform.unity().translateZ(2).rotZ(i);

            CSG facet = Extrude.points(new Vector3d(0, 0, 1), points);

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
