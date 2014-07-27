/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d.samples;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.FileUtil;
import eu.mihosoft.vrl.v3d.Sphere;
import eu.mihosoft.vrl.v3d.Transform;
import eu.mihosoft.vrl.v3d.XModifier;
import eu.mihosoft.vrl.v3d.YModifier;
import eu.mihosoft.vrl.v3d.ZModifier;
import java.io.IOException;
import java.nio.file.Paths;


/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class ModifiedRoundedCube {

   
    public CSG toCSG() {

        CSG result = new Sphere(3,64,32).toCSG();
        
//        CSG result = new Cube(3).toCSG();

        return result.weighted(new YModifier(true)).
                transformed(Transform.unity().scale(0.1)).weighted(new XModifier(true)).
                transformed(Transform.unity().scale(0.1)).weighted(new ZModifier(true)).
                transformed(Transform.unity().scale(0.1));
    }

    public static void main(String[] args) throws IOException {

        FileUtil.write(Paths.get("rounded-cube-mod.stl"), new ModifiedRoundedCube().toCSG().toStlString());

        new ModifiedRoundedCube().toCSG().toObj().toFiles(Paths.get("rounded-cube-mod.obj"));

    }

}
