/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d.samples;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Cube;
import eu.mihosoft.vrl.v3d.FileUtil;
import eu.mihosoft.vrl.v3d.Sphere;
import static eu.mihosoft.vrl.v3d.Transform.unity;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Average Chicken Egg.
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class Egg {

    public CSG toCSG() {
        double radius = 22;
        double stretch = 1.50;
        int resolution = 64;

        // cube that cuts the spheres
        CSG cube = new Cube(2*stretch*radius).toCSG();
        cube = cube.transformed(unity().translateZ(stretch*radius));

        // stretched sphere
        CSG upperHalf = new Sphere(radius, resolution, resolution/2).toCSG().
                transformed(unity().scaleZ(stretch));
        
        // upper half
        upperHalf = upperHalf.intersect(cube);
        
        CSG lowerHalf = new Sphere(radius, resolution, resolution/2).toCSG();
        lowerHalf = lowerHalf.difference(cube);
        
         // stretch lower half
        lowerHalf = lowerHalf.transformed(unity().scaleZ(stretch*0.72));
        
        CSG egg = upperHalf.union(lowerHalf);
        
        return egg;
    }

    public static void main(String[] args) throws IOException {
        FileUtil.write(Paths.get("egg.stl"), new Egg().toCSG().toStlString());
    }
}
