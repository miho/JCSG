/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d.samples;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Cylinder;
import eu.mihosoft.vrl.v3d.FileUtil;
import eu.mihosoft.vrl.v3d.Transform;
import static eu.mihosoft.vrl.v3d.Transform.unity;
import java.io.IOException;
import java.nio.file.Paths;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class QuadrocopterPlatform {
    
    public static void main(String[] args) throws IOException {

        CSG result = new QuadrocopterPlatform().toCSG();

        FileUtil.write(Paths.get("quadrocopter-platform.stl"), result.toStlString());
        result.toObj().toFiles(Paths.get("quadrocopter-platform.obj"));

//        CSG resultNoStructure = new QuadrocopterArm().toCSG();
//
//        FileUtil.write(Paths.get("quadrocopter-arm-no-structure.stl"), resultNoStructure.toStlString());
//        resultNoStructure.toObj().toFiles(Paths.get("quadrocopter-arm-no-structure.obj"));
    }
    
    
    private CSG toCSG() {

        double platformRadius = 150;
        double platformThickness = 3;
        double platformBorderThickness = 4;

        int numHoneycombs = 14;
        double honeycombWallThickness = 2.5;

        CSG platform =  basePlatform(platformRadius, numHoneycombs, platformThickness, platformBorderThickness, honeycombWallThickness);
        
        double armHeight = 18;
        double armScaleFactor = 0.5;
        double armCubeWidth = armHeight;
        double armCubeThickness = 4;
        double holderPlatformRadius = 36;
        
        CSG armHolderPrototype = new QuadrocopterArmHolder().toCSG(armHeight, armScaleFactor, armCubeWidth, armCubeThickness, holderPlatformRadius, platformThickness).transformed(unity().translateX(-platformRadius));
        
        CSG armHolders = armHolderPrototype.clone();
        
        for( int i = 1; i < 4; i++) {
            armHolders = armHolders.union(armHolderPrototype.transformed(unity().rotZ(i*90)));
        }
        
        platform = platform.union(armHolders);
        
        return platform;
    }

    

    private CSG basePlatform(double platformRadius, int numHoneycombs, double platformThickness, double platformBorderThickness, double honeycombWallThickness) {
        double honeycombRadius = platformRadius / numHoneycombs;
        
        CSG platform = new Cylinder(platformRadius, platformThickness, 64).toCSG();
        
        CSG innerPlatform = new Cylinder(platformRadius - platformBorderThickness, platformThickness, 64).toCSG();
        
        CSG platformShell = platform.difference(innerPlatform);

        CSG honeycombPrototype = new Cylinder(honeycombRadius, platformThickness, 6).toCSG();

        int numHoneycomb = (int) ((platformRadius * 2) / (honeycombRadius * 2));

        CSG hexagons = null;

        double inradiusOfHexagon = honeycombRadius * Math.cos((180.0 / 6.0) * Math.PI / 180);
        double sideLength = honeycombRadius * 2 * Math.sin((180.0 / 6.0) * Math.PI / 180);
        
        // TODO: change that!
        // inradius makes previus calculation obsolete
        // to be sure we use numHoneyCombs*1.3
        
        numHoneycomb*=1.4;

        for (int y = 0; y < numHoneycomb; y++) {
            for (int x = 0; x < numHoneycomb; x++) {

                double offset = inradiusOfHexagon * (x % 2);

                double dx = -platformRadius + x * sideLength * 1.5;
                double dy = -platformRadius + y * inradiusOfHexagon * 2.0 + offset;

                dx += honeycombWallThickness*x;
                dy += honeycombWallThickness*y  + honeycombWallThickness * (x % 2)/2;

                CSG h = honeycombPrototype.transformed(unity().translate(
                        dx, dy, 0));

                if (hexagons == null) {
                    hexagons = h;
                } else {
                    hexagons = hexagons.union(h);
                }
            }
        }
        
        if (hexagons!=null) {
            platform = platform.difference(hexagons);
        }

        return platform.union(platformShell);
    }
}
