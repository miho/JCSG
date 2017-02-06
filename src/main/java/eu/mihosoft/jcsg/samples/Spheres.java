/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.mihosoft.jcsg.samples;

import eu.mihosoft.jcsg.CSG;
import eu.mihosoft.jcsg.FileUtil;
import eu.mihosoft.jcsg.Sphere;
import eu.mihosoft.vvecmath.Transform;

import java.io.IOException;
import java.nio.file.Paths;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class Spheres {

    public CSG toCSG() {

        double maxR = 10;

        double w = 30;
        double h = 30;
        double d = 30;

        // optimization reduces runtime dramatically
        CSG.setDefaultOptType(CSG.OptType.POLYGON_BOUND);

        CSG spheres = null;

        for(int i = 0;i<70;i++) {
            CSG s = new Sphere(Math.random()*maxR).toCSG().
                    transformed(
                            Transform.unity().
                                    translate(
                                            Math.random()*w,
                                            Math.random()*h,
                                            Math.random()*d));
            if (spheres == null) {
                spheres = s;
            } else {
                spheres = spheres.union(s);
            }
        }

        return spheres;
    }

    public static void main(String[] args) throws IOException {
        FileUtil.write(Paths.get("spheres.stl"), new Spheres().toCSG().toStlString());
    }
}