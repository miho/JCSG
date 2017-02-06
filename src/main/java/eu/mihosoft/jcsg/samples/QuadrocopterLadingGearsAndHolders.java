/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg.samples;

import eu.mihosoft.vvecmath.Vector3d;
import eu.mihosoft.vvecmath.Transform;
import eu.mihosoft.jcsg.*;

import java.io.IOException;
import java.nio.file.Paths;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class QuadrocopterLadingGearsAndHolders {

    public static void main(String[] args) throws IOException {

        QuadrocopterLadingGearsAndHolders ladingGearsAndHolders = new QuadrocopterLadingGearsAndHolders();
        CSG csg = ladingGearsAndHolders.toCSG();

        FileUtil.write(Paths.get("quadcopter-landing-gear-and-holder.stl"), csg.toStlString());
        csg.toObj().toFiles(Paths.get("quadcopter-landing-gear-and-holder.obj"));

    }

    private CSG toCSG() {

        double armThickness = 18;
        double armShrinkFactor = 0.640;

        double gearHeadHeight = 18;
        double gearWidth = 16;
        double gearDepth = 30;

        double armInset = 6;

        double platformInsertionDepth = 4;
        double platformThickness = 3;

        double platformSpaceTop = 50;
        double platformSpaceInner = 50;

        CSG platformReplacement = bottomReplacementShape(platformThickness,gearWidth, platformInsertionDepth);


        CSG arm = QuadrocopterArm.outerCyl(armThickness / 2.0, gearDepth, 0, armShrinkFactor, 0, true);
        arm = arm.transformed(Transform.unity().translateY(armInset));

        CSG landingGearHead = new Cube(gearWidth, gearHeadHeight, gearDepth).toCSG();
        Transform lgOrigin = Transform.unity().translate(0, gearHeadHeight / 2.0, gearDepth / 2.0);
        landingGearHead = landingGearHead.transformed(lgOrigin);

        landingGearHead = landingGearHead.difference(arm);

        double gearLegHeight = 150;
        int legResolution = 16;

        CSG legPrototype = new Cube(gearDepth, gearLegHeight / legResolution, gearWidth).noCenter().toCSG().
                transformed(Transform.unity().translate(0, gearHeadHeight, -gearWidth / 2.0));

        CSG leg = legPrototype.clone();

        double dH = gearLegHeight / legResolution;

        for (int i = 1; i < legResolution; i++) {
            leg = leg.union(legPrototype.transformed(Transform.unity().translateY(i * dH)));
        }

        WeightFunction translateWeight = (v, csg) -> {
            if (v.y() < 2*dH) {
                return 0;
            } else {
                double val =  0.82+v.y() * v.y()/ ((gearLegHeight * gearLegHeight));

//                System.out.println("val: " + val + ", " + Math.min(1,val));
                return val;
            }
        };

        leg = leg.weighted(translateWeight).transformed(Transform.unity().scale(0.6, 1, 0.6)).
//                weighted(translateWeight).transformed(unity().translateX(-50)).
                weighted(new UnityModifier()).transformed(Transform.unity().rotY(90));

        return leg.union(landingGearHead).difference(
                platformReplacement.transformed(Transform.unity().translateY(gearHeadHeight+platformSpaceTop)),
                platformReplacement.transformed(Transform.unity().translateY(gearHeadHeight+platformSpaceTop+platformSpaceInner)));
    }

    private CSG bottomReplacementShape(double bottomThickness, double landingGearWidth, double depth) {
        Vector3d height = Vector3d.z(landingGearWidth);

        double th = bottomThickness;
        double d = depth;
        double t = th;

        return Extrude.points(height,
                Vector3d.ZERO,
                Vector3d.xy(0,th),
                Vector3d.xy(-d,th),
                Vector3d.xy(-d-t,th*0.5),
                Vector3d.xy(-d,0)
                ).transformed(Transform.unity().rotY(270)).transformed(Transform.unity().translateX(-landingGearWidth*0.5));
    }
}
