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
import eu.mihosoft.vrl.v3d.RoundedCube;
import eu.mihosoft.vrl.v3d.Transform;
import static eu.mihosoft.vrl.v3d.Transform.unity;
import java.io.IOException;
import java.nio.file.Paths;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class QuadrocopterCross {

    public static void main(String[] args) throws IOException {

        CSG result = new QuadrocopterCross().toCSG2();

        FileUtil.write(Paths.get("quadrocopter-cross.stl"), result.toStlString());
        result.toObj().toFiles(Paths.get("quadrocopter-cross.obj"));

    }

    public CSG toCSG(double armHeight, double armScaleFactor, double armCubeWidth, double armCubeThickness, double holderPlatformRadius, double holderPlatformThickness) {

        double widthTol = 2;
        double thicknessTol = 0.1;
        double holderWallThickness = 6;
        double armOverlap = 30;

        armCubeThickness = armCubeThickness + thicknessTol;

        double holderCubeDepth = armOverlap + armCubeThickness + holderWallThickness;

        double armWidth = armHeight * armScaleFactor;
        
        Transform xTransform = unity().translateX(-holderWallThickness *2);
        Transform yTransform = unity().translateY(-armCubeThickness / 2.0 - armOverlap / 2.0 + holderWallThickness);

        CSG armCube = new Cube(armCubeWidth + widthTol, armCubeThickness, armHeight).
                toCSG().transformed(yTransform);
        CSG arm = new Cube(armWidth, holderCubeDepth, armHeight).toCSG().
                transformed(unity().translateZ(armHeight / 2.0));
        arm = new Cylinder(armHeight / 2.0, holderCubeDepth, 32).
                toCSG().transformed(unity().rotX(90).
                        translate(0, 0, -holderCubeDepth / 2.0).scaleX(armScaleFactor)).union(arm);
        CSG holder = armCube.union(arm).transformed(unity().rotZ(90));
        
                        
        double sideArmHight = 150 / 2.0;
        double sideArmGroundDist = 25;
        double sideArmRadius = armHeight / 6.0;
        double sideArmShrinkFactor = 0.6;
        
//        CSG sideArms = QuadrocopterArm.sideArms(sideArmGroundDist, sideArmHight, sideArmRadius, sideArmShrinkFactor, armCubeThickness,armWidth).transformed(xTransform);
        
//        return holder.union(sideArms);

        return holder;

    }

    public CSG toCSG2() {

        double platformRadius = 80;
        double innerHoleRadius = 50;
        double platformThickness = 3; // deprecated

        double armHeight = 18;
        double armScaleFactor = 0.65;
        double armCubeWidth = armHeight;
        double armCubeThickness = 4;
        double holderPlatformRadius = 20;

        double distToInnerHole = 5;

        CSG armHolderPrototype = toCSG(armHeight, armScaleFactor, armCubeWidth,
                armCubeThickness, holderPlatformRadius, platformThickness).transformed(unity().translateX(68).translateZ(14));

        CSG armHolders = armHolderPrototype.clone();

        CSG quarterPrototype = new RoundedCube(platformRadius).cornerRadius(10)
                .resolution(16).toCSG()
                .transformed(unity().rotZ(45)).transformed(unity().scaleY(3))
                .transformed(unity().translate(
                                innerHoleRadius + distToInnerHole, 0, -armHeight / 2.0))
                .transformed(unity().rotZ(-45));

        CSG quarters = quarterPrototype.clone();

        for (int i = 1; i < 4; i++) {

            Transform rotTransform = unity().rotZ(i * 90);

            armHolders = armHolders.union(armHolderPrototype.transformed(rotTransform));
            quarters = quarters.union(quarterPrototype.transformed(rotTransform));
        }

        CSG platform = new Cylinder(platformRadius, armHeight, 64).toCSG();
        CSG innerHole = new Cylinder(innerHoleRadius, armHeight, 64).toCSG();

        platform = platform.difference(armHolders, innerHole,quarters);

        return platform;

    }

}
