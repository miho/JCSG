/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d.samples;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Cube;
import eu.mihosoft.vrl.v3d.Cylinder;
import eu.mihosoft.vrl.v3d.FileUtil;
import eu.mihosoft.vrl.v3d.Sphere;
import eu.mihosoft.vrl.v3d.Transform;
import eu.mihosoft.vrl.v3d.Vector3d;
import java.io.IOException;
import java.nio.file.Paths;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class BraceletGenerator {

    public static CSG toCSG() {

        double sphereRadius = 10;

        CSG sphere = new Sphere(Vector3d.ZERO, sphereRadius, 64, 64).toCSG();

        double spaceRadius = 1;

        CSG spaceProt = new Cylinder(spaceRadius * 1, sphereRadius * 2, 16).toCSG().
                transformed(Transform.unity().translate(0, 0, -sphereRadius).
                        rotZ(45));

        CSG spaces = null;

        double step = 360.0 / 20;
        
        for (double i = 0; i < 360; i += step) {
            CSG sp = spaceProt.transformed(Transform.unity().
                    translate(sphereRadius - spaceRadius *0.98, 0, 0)).
                    transformed(Transform.unity().rotZ(i));

            if (spaces == null) {
                spaces = sp;
            } else {
                spaces = spaces.union(sp);
            }
        }
        
        
        double braceletHeight = 2;
        
        CSG top = new Cube(sphereRadius*2).toCSG().
                transformed(Transform.unity().
                        translateZ(sphereRadius+braceletHeight/2.0));
        CSG bottom = new Cube(sphereRadius*2).toCSG().
                transformed(Transform.unity().
                        translateZ(-sphereRadius-braceletHeight/2.0));
        
        sphere = sphere.transformed(Transform.unity().scaleZ(0.5));
        
        return sphere.difference(top).difference(bottom).difference(spaces).
                transformed(Transform.unity().scale(3.5));

    }

    public static void main(String[] args) throws IOException {
        
        FileUtil.write(Paths.get("sample.stl"), toCSG().toStlString());
    }
}
