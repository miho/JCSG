/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d.samples;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Cube;
import eu.mihosoft.vrl.v3d.FileUtil;
import eu.mihosoft.vrl.v3d.Transform;
import static eu.mihosoft.vrl.v3d.Transform.unity;
import eu.mihosoft.vrl.v3d.UnityModifier;
import eu.mihosoft.vrl.v3d.Vector3d;
import eu.mihosoft.vrl.v3d.WeightFunction;
import eu.mihosoft.vrl.v3d.YModifier;
import java.io.IOException;
import java.nio.file.Paths;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class QuadrocopterLadingGears {

    public static void main(String[] args) throws IOException {

        QuadrocopterLadingGears moebiusStairs = new QuadrocopterLadingGears();
        CSG csg = moebiusStairs.toCSG();

        FileUtil.write(Paths.get("quadcopter-landing-gear.stl"), csg.toStlString());
        csg.toObj().toFiles(Paths.get("quadcopter-landing-gear.obj"));

    }

    private CSG toCSG() {

        double armThickness = 18;
        double armShrinkFactor = 0.640;

        double gearHeadHeight = 18;
        double gearWidth = 16;
        double gearDepth = 20;

        double armInset = 6;

        CSG arm = QuadrocopterArm.outerCyl(armThickness / 2.0, gearDepth, 0, armShrinkFactor, 0, true);
        arm = arm.transformed(unity().translateY(armInset));

        CSG landingGearHead = new Cube(gearWidth, gearHeadHeight, gearDepth).toCSG();
        Transform lgOrigin = unity().translate(0, gearHeadHeight / 2.0, gearDepth / 2.0);
        landingGearHead = landingGearHead.transformed(lgOrigin);

        landingGearHead = landingGearHead.difference(arm);

        double gearLegHeight = 120;
        int legResolution = 10;

        CSG legPrototype = new Cube(gearDepth, gearLegHeight / legResolution, gearWidth).noCenter().toCSG().transformed(unity().translate(0, gearHeadHeight, -gearWidth / 2.0));

        CSG leg = legPrototype.clone();

        double dH = gearLegHeight / legResolution;

        for (int i = 1; i < legResolution; i++) {
            leg = leg.union(legPrototype.transformed(unity().translateY(i * dH)));
        }

        WeightFunction translateWeight = (v, csg) -> {
            if (v.y < 2*dH) {
                return 0;
            } else {
                double val =  0.9+v.y * v.y/ ((gearLegHeight * gearLegHeight) + gearLegHeight*10);
                
//                System.out.println("val: " + val + ", " + Math.min(1,val));
                
                return val;
            }
        };

        leg = leg.weighted(translateWeight).transformed(unity().scale(0.6, 1, 0.6)).
//                weighted(translateWeight).transformed(unity().translateX(-50)).
                weighted(new UnityModifier()).transformed(unity().rotY(90));

        return leg.union(landingGearHead);
    }
}
