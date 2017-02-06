/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.mihosoft.jcsg.samples;

import eu.mihosoft.jcsg.FileUtil;
import eu.mihosoft.jcsg.Sphere;
import eu.mihosoft.vvecmath.Transform;
import eu.mihosoft.jcsg.CSG;
import eu.mihosoft.vvecmath.Vector3d;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class EggCup {
     public static void main(String[] args) throws IOException {
        FileUtil.write(Paths.get("eggcup.stl"), new EggCup().toCSG().toStlString());
    }

    private CSG toCSG() {
        
        CSG.setDefaultOptType(CSG.OptType.POLYGON_BOUND);
        CSG egg = new Egg().toCSG();
        
        Vector3d eggBounds = egg.getBounds().getBounds();
        
        CSG upperNegativeEgg = egg.transformed(Transform.unity().translateZ(eggBounds.z()*0.175).scale(0.88,0.88,1));
        CSG lowerNegativeEgg = egg.transformed(Transform.unity().translateZ(-eggBounds.z()*0.50));
        
        System.out.println("egg-size: " + upperNegativeEgg.getBounds());
        
         try {
             FileUtil.write(Paths.get("eggcup-neg.stl"), upperNegativeEgg.toStlString());
         } catch (IOException ex) {
             Logger.getLogger(EggCup.class.getName()).log(Level.SEVERE, null, ex);
         }
        
        int resolution = 64;
        double wallThickness = 5.0;
        double radius = eggBounds.x()/2.0+wallThickness;
        
        
        CSG.setDefaultOptType(CSG.OptType.NONE);
        
        CSG feet = new MoebiusStairs().resolution(90).twists(2).toCSG();
        
        feet = feet.
                transformed(Transform.unity().translateZ(-radius).scale(1.2,1.2,1.3));
        
        CSG.setDefaultOptType(CSG.OptType.POLYGON_BOUND);
        
        CSG shellOuter = new Sphere(radius, resolution, resolution/2).toCSG().
                transformed(Transform.unity().scaleZ(1.25));

        CSG shell = shellOuter.difference(lowerNegativeEgg);
        
         double shellHeight = shell.getBounds().getBounds().z();
        
        double shrinkTransformZ = 0.8;
         
        shell = shell.transformed(Transform.unity().scaleZ(shrinkTransformZ));

        
        double lowerIntersectionHeight = shellOuter.getBounds().getBounds().z()-shellHeight;
        
        lowerIntersectionHeight = lowerIntersectionHeight*shrinkTransformZ;
        
        
        Transform shellTransform = Transform.unity().translateZ(-lowerIntersectionHeight);
        
        shell = shell.transformed(shellTransform);
        
        shell = shell.union(feet);
        
        shell = shell.difference(upperNegativeEgg.transformed(shellTransform));
        
        return shell;
        
    }
}
