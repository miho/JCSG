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
import eu.mihosoft.vrl.v3d.UnityModifier;
import eu.mihosoft.vrl.v3d.XModifier;
import eu.mihosoft.vrl.v3d.YModifier;
import eu.mihosoft.vrl.v3d.ZModifier;
import java.io.IOException;
import java.nio.file.Paths;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class WeightedSphere {

    public CSG toCSG() {

        CSG prototype = new Sphere(3, 128, 64).toCSG().optimization(CSG.OptType.POLYGON_BOUND);

//        CSG result = new Sphere(3, 128, 64).toCSG();
//        return result.
//                
//                weighted(new ZModifier(true)).transformed(Transform.unity().scale(0.1)).
//                weighted(new YModifier(true)).transformed(Transform.unity().scale(0.1)).
//                weighted(new XModifier(true)).transformed(Transform.unity().scale(0.1)).
//                transformed(Transform.unity().translateX(1)).weighted(new UnityModifier());
                

        CSG result = prototype;
        
        

//        double dt = 0.1;
//        for (int i = 0; i < 10; i++) {
//            final int index = i+1;
//            System.out.println("index: " + index + ", dt: " + (dt*index));
//            CSG morphed = prototype.weighted((v, csg) -> {
//                double w = (1 + Math.sin(v.z * 2)*Math.cos(v.z * 2)) / 2.0;
//
//                w = w * dt* index;
//
//                return w;
//
//            }).transformed(Transform.unity().scale(0.1)).
//                    weighted(new XModifier(true)).
////                    weighted(new UnityModifier()).
//                    transformed(Transform.unity().translateX(8+(i+1) * (8)));
//
//            result = result.union(morphed);
//        }
//        return result;
        
        

            CSG morphed = prototype.weighted((v, csg) -> {
                double w = (1 + Math.sin(v.z * 2)*Math.cos(v.z * 2)) / 2.0;

                return w;

            }).transformed(Transform.unity().scale(0.1)).
//                    weighted(new XModifier(true)).
//                    weighted(new UnityModifier()).
                    transformed(Transform.unity().translateX(1).rotZ(90));

            result = morphed;

        return result;
    }

    public static void main(String[] args) throws IOException {

        FileUtil.write(Paths.get("rounded-cube-mod.stl"), new WeightedSphere().toCSG().toStlString());

        new WeightedSphere().toCSG().toObj().toFiles(Paths.get("rounded-cube-mod.obj"));

    }

}
