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
import eu.mihosoft.vrl.v3d.Transform;
import eu.mihosoft.vrl.v3d.Vector3d;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class PlaneWithHoles {

    public CSG toCSG() {
        CSG result = new Cube(Vector3d.ZERO, new Vector3d(30, 30, 1)).toCSG();

//        CSG result = null;
//        try {
//            result = STL.file(Paths.get("box_refined-01.stl")).transformed(Transform.unity().scale(30, 30, 0.5)).optimization(CSG.OptType.POLYGON_BOUND);
//        } catch (IOException ex) {
//            Logger.getLogger(PlaneWithHoles.class.getName()).log(Level.SEVERE, null, ex);
//        }

        CSG spheres = null;

        for (int y = 0; y < 11; y++) {

            System.out.println("line: " + y);

            for (int x = 0; x < 11; x++) {

                double radius = 1.2;
                double spacing = 0.25;

//                CSG sphere = new Cylinder(radius, 1, 24).toCSG().transformed(
//                        Transform.unity().translate((x - 5) * (radius * 1.7 + spacing), (y - 5) * (radius * 1.7 + spacing), -0.5)).optimization(CSG.OptType.CSG_BOUND);

                CSG sphere = new Sphere(radius).toCSG().transformed(
                        Transform.unity().translate((x - 5) * (radius * 2 + spacing), (y - 5) * (radius * 2 + spacing), -0.0)).optimization(CSG.OptType.POLYGON_BOUND);

                
//                result = result.difference(sphere);
                if (spheres == null) {
                    spheres = sphere;
                } else {
                    spheres = spheres.union(sphere);
                }

            }
        }
        
        try {
            FileUtil.write(Paths.get("cyl.stl"), spheres.toStlString());
        } catch (IOException ex) {
            Logger.getLogger(PlaneWithHoles.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println(">> final diff");

        result = result.difference(spheres);

        return result;
    }

    public static void main(String[] args) throws IOException {
        
        CSG.setDefaultOptType(CSG.OptType.CSG_BOUND);

        PlaneWithHoles planeWithHoles = new PlaneWithHoles();

        // save union as stl
//        FileUtil.write(Paths.get("sample.stl"), new ServoHead().servoHeadFemale().transformed(Transform.unity().scale(1.0)).toStlString());
        FileUtil.write(Paths.get("sample.stl"), planeWithHoles.toCSG().toStlString());

    }
}
