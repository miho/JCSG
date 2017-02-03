/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg.samples;

import eu.mihosoft.vvecmath.Transform;
import eu.mihosoft.jcsg.*;
import eu.mihosoft.jcsg.CSG;
import eu.mihosoft.jcsg.Cube;
import eu.mihosoft.jcsg.FileUtil;
import eu.mihosoft.jcsg.Sphere;

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
        cube = cube.transformed(Transform.unity().translateZ(stretch*radius));

        // stretched sphere
        CSG upperHalf = new Sphere(radius, resolution, resolution/2).toCSG().
                transformed(Transform.unity().scaleZ(stretch));
        
        // upper half
        upperHalf = upperHalf.intersect(cube);
        
        CSG lowerHalf = new Sphere(radius, resolution, resolution/2).toCSG();
        lowerHalf = lowerHalf.difference(cube);
        
         // stretch lower half
        lowerHalf = lowerHalf.transformed(Transform.unity().scaleZ(stretch*0.72));
        
        CSG egg = upperHalf.union(lowerHalf);
        
        return egg;
    }

    public static void main(String[] args) throws IOException {
        FileUtil.write(Paths.get("egg.stl"), new Egg().toCSG().toStlString());
    }
}
