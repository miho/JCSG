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
import eu.mihosoft.vrl.v3d.Plane;
import eu.mihosoft.vrl.v3d.RoundedCube;
import eu.mihosoft.vrl.v3d.Sphere;
import eu.mihosoft.vrl.v3d.Transform;
import java.io.IOException;
import java.nio.file.Paths;

import static eu.mihosoft.vrl.v3d.Transform.*;
import eu.mihosoft.vrl.v3d.UnityModifier;
import eu.mihosoft.vrl.v3d.Vector3d;
import eu.mihosoft.vrl.v3d.ZModifier;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class QuadrocopterArm {

    public CSG mainArm(int numInnerStructures, double length, double armThickness, double innerTubeOffset, double armCubeThickness) {

        double outerRadius = armThickness / 2.0;
        double wallThickness = 0.8;

        double structureRadius = 0.4;
        double maxXRot = 180;
        double maxYRot = 180;
        double maxZRot = 180;

        double maxXYOffset = outerRadius;

        double innerRadius = 5.1;
        double innerWallThickness = 0.8;

        double numPlates = 0;
        double plateThickness = 0.5;

        double shrinkFactorX = 0.65;

        double sideArmHight = length / 2.0;
        double sideArmGroundDist = 30;
        double sideArmRadius = armThickness / 6.0;
        double sideArmShrinkFactor = 0.6;

        CSG innerStructure = null;

        for (int i = 0; i < numInnerStructures; i++) {
            CSG cyl = new Cylinder(structureRadius, outerRadius * 10, 8).toCSG();
            cyl = cyl.transformed(Transform.unity().scale(
                    Math.max(0.5, Math.random() * 3),
                    Math.max(0.5, Math.random() * 3), 1));
            cyl = cyl.transformed(Transform.unity().
                    translateX(cyl.getBounds().getBounds().z / 2.0).
                    rotY(90));
            cyl = cyl.transformed(Transform.unity().rot(
                    Math.random() * maxXRot,
                    Math.random() * maxYRot,
                    Math.random() * maxZRot));

            cyl = cyl.transformed(Transform.unity().translate(
                    -maxXYOffset / 2.0 + Math.random() * maxXYOffset,
                    -maxXYOffset / 2.0 + Math.random() * maxXYOffset,
                    innerTubeOffset + Math.random() * (length - innerTubeOffset)));

            if (innerStructure == null) {
                innerStructure = cyl;
            } else {
                innerStructure = innerStructure.union(cyl);
            }
        }

        if (innerStructure != null) {
            innerStructure = innerStructure.intersect(
                    new Cylinder(outerRadius, length - innerTubeOffset, 16).toCSG().
                    transformed(unity().scaleX(0.5).translateZ(innerTubeOffset)));
        }

        CSG outerCyl = outerCyl(outerRadius, length, wallThickness,
                shrinkFactorX, shrinkFactorX * 0.95);

        if (innerStructure != null) {
            outerCyl = outerCyl.union(innerStructure);
        }

        CSG innerCyl = new Cylinder(innerRadius, length - innerTubeOffset, 16).toCSG().transformed(unity().translateZ(innerTubeOffset));

        CSG finalGeometry = outerCyl.union(innerCyl);

        CSG plate = new Cylinder(outerRadius, plateThickness, 16).toCSG().
                transformed(unity().scaleX(shrinkFactorX));

        CSG endPlate = plate.transformed(unity().translateZ(innerTubeOffset));

        finalGeometry = finalGeometry.union(endPlate);

        CSG plates = null;

        if (numPlates > 0) {
            double dt = (length - innerTubeOffset) / numPlates;
            for (int i = 0; i < numPlates; i++) {
                CSG pl = plate.transformed(unity().translateZ(dt * i));
                if (plates == null) {
                    plates = pl;
                } else {
                    plates = plates.union(pl);
                }
            }
            finalGeometry = finalGeometry.union(plates);
        }

        CSG cube = new Cube(outerRadius * 2, outerRadius * 2, armCubeThickness).
                toCSG().difference(innerCyl).
                transformed(unity().translateZ(length - armCubeThickness / 2.0));

        finalGeometry = finalGeometry.union(cube);

        CSG sideArms = sideArms(sideArmGroundDist, sideArmHight, sideArmRadius, sideArmShrinkFactor, length, armCubeThickness, outerRadius);

        //finalGeometry = finalGeometry.union(sideArms);
        finalGeometry = finalGeometry.difference(
                new Cylinder(innerRadius - innerWallThickness,
                        length, 16).toCSG());

        return finalGeometry;
    }

    public static CSG sideArms(double sideArmGroundDist, double sideArmHight, double sideArmRadius, double sideArmShrinkFactor, double length, double armCubeThickness, double outerRadius) {
        double sideArmLength = Math.sqrt(sideArmGroundDist * sideArmGroundDist + sideArmHight * sideArmHight);
        double alpha = Math.atan(sideArmGroundDist / sideArmHight) * 180 / Math.PI;
        CSG subCylinder = new Cylinder(sideArmRadius, sideArmLength + sideArmRadius, 16).toCSG().transformed(unity().rotY(90).scaleX(sideArmShrinkFactor)).transformed(unity().rotZ(alpha)).transformed(unity().translateX(-length + sideArmHight));
        subCylinder = subCylinder.difference(new Cube(new Vector3d(-length - sideArmRadius * 2, sideArmGroundDist), new Vector3d(sideArmRadius * 4, sideArmRadius * 4, sideArmRadius * 4)).toCSG());
        subCylinder = subCylinder.union(new Cube(new Vector3d(-length + armCubeThickness / 2.0, sideArmGroundDist, 0), new Vector3d(armCubeThickness, outerRadius * 2, outerRadius * 2)).toCSG());
        CSG sideArms = subCylinder.union(subCylinder.transformed(unity().mirror(Plane.XZ_PLANE))).transformed(unity().rotY(90).rotZ(180).rotX(90));
        return sideArms;
    }

    public static CSG sideArms(double sideArmGroundDist, double sideArmHight, double sideArmRadius, double sideArmShrinkFactor, double armCubeThickness, double outerRadius) {
        double sideArmLength = Math.sqrt(sideArmGroundDist * sideArmGroundDist + sideArmHight * sideArmHight);
        double alpha = Math.atan(sideArmGroundDist / sideArmHight) * 180 / Math.PI;
        CSG subCylinder = new Cylinder(sideArmRadius, sideArmLength + sideArmRadius, 16).toCSG().transformed(unity().rotY(90).scaleX(sideArmShrinkFactor)).transformed(unity().rotZ(alpha)).transformed(unity().translateX(sideArmHight));
        subCylinder = subCylinder.difference(new Cube(new Vector3d(0 - sideArmRadius * 2, sideArmGroundDist), new Vector3d(sideArmRadius * 4, sideArmRadius * 4, sideArmRadius * 4)).toCSG());
        subCylinder = subCylinder.union(new Cube(new Vector3d(0 + armCubeThickness / 2.0, sideArmGroundDist, 0), new Vector3d(armCubeThickness, outerRadius * 2, outerRadius * 2)).toCSG());
        CSG sideArms = subCylinder.union(subCylinder.transformed(unity().mirror(Plane.XZ_PLANE)));
        return sideArms;
    }

    public static CSG outerCyl(double outerRadius, double length,
            double wallThickness, double scaleOuter, double scaleInner) {
       

        return outerCyl(outerRadius, length, wallThickness, scaleOuter, scaleInner, false);
    }

    public static CSG outerCyl(double outerRadius, double length,
            double wallThickness, double scaleOuter, double scaleInner, boolean filled) {
        CSG outerCyl = new Cylinder(outerRadius, length, 32).toCSG().
                transformed(unity().scaleX(scaleOuter));

        if (!filled) {
            CSG outerCylInner = new Cylinder(outerRadius - wallThickness / scaleOuter,
                    length, 32).toCSG().transformed(unity().scaleX(scaleInner));
            outerCyl = outerCyl.difference(outerCylInner);
        }

        return outerCyl;
    }

//    private CSG outerCyl(double outerRadius, double length,
//            double wallThickness, double scaleOuter, double scaleInner) {
//
//        // refine
//        double l = length / 10;
//
//        CSG protoOuter = new Cylinder(outerRadius, l, 32).toCSG().
//                transformed(unity().scaleX(scaleOuter));
//        CSG protoInner = new Cylinder(outerRadius - wallThickness / scaleOuter,
//                l, 32).toCSG().transformed(unity().scaleX(scaleInner));
//
//        CSG outerCylProto = protoOuter.difference(protoInner);
//        
//        CSG outerCyl = outerCylProto;
//
//        for (int i = 1; i < 10; i++) {
//            outerCyl = outerCyl.union(protoOuter.transformed(Transform.unity().translateZ(i * l)));
//        }
//
//        outerCyl = outerCyl.weighted(new ZModifier(true)).transformed(Transform.unity().scale(0.8, 0.8, 1)).weighted(new UnityModifier());
//
//        return outerCyl;
//    }
    public CSG toCSG() {

        // optimization seems to cause problems
        CSG.setDefaultOptType(CSG.OptType.NONE);

        double engineRadius = 14;
        double screwDistanceBig = 9.5;
        double screwDistanceSmall = 8;
        double screwRadius = 1.6;
        double enginePlatformThickness = 2.0;
        double mainHoleRadius = 4;

        double washerWallThickness = 1;
        double washerHeight = 2;

        double armLength = 150;
        int numInnerStructures = 60;
        double armThickness = 18;
        double armCubeThickness = 4;

        double innerTubeOffset = engineRadius * 2 + 5;

        CSG mainArm = mainArm(numInnerStructures, armLength, armThickness, innerTubeOffset, armCubeThickness).transformed(unity().rotX(90).rotY(90));

        CSG enginePlatformSphere = new Sphere(engineRadius * 1.1, 64, 32).toCSG().transformed(unity().scaleX(2).translateZ(armThickness * 0.5));

        Transform engineTransform = unity().translateX(-mainHoleRadius).translateZ(-armThickness * 0.28).translateX(1.2);

        CSG mainHole = new Cylinder(mainHoleRadius, enginePlatformThickness, 16).toCSG().transformed(engineTransform);
        CSG enginePlatform = enginePlatform(engineRadius, enginePlatformThickness, mainHoleRadius, screwRadius, screwDistanceBig, screwDistanceSmall, washerWallThickness, washerHeight).transformed(engineTransform);

        mainArm = mainArm.difference(enginePlatformSphere).union(enginePlatform).difference(mainHole);

        //double armHeight = mainArm.getBounds().getBounds().x;
        return mainArm.transformed(unity().rotX(90).rotZ(90));
    }

    private CSG enginePlatform(double engineRadius, double enginePlatformThickness, double mainHoleRadius, double screwRadius, double screwDistanceBig, double screwDistanceSmall, double washerWallThickness, double washerHeight) {
        CSG enginePlatform = new Cylinder(engineRadius, enginePlatformThickness, 32).toCSG();

        CSG secondCyl = new Cylinder(engineRadius * 0.3, enginePlatformThickness, 3).toCSG().transformed(unity().translateX(-engineRadius * 2.7));

        enginePlatform = enginePlatform.union(secondCyl).hull();

        CSG mainHole = new Cylinder(mainHoleRadius, enginePlatformThickness * 5, 16).toCSG().transformed(Transform.unity().translateZ(-enginePlatformThickness));

        CSG screwHolePrototype = new Cylinder(screwRadius, enginePlatformThickness + washerHeight + 10, 16).toCSG().transformed(Transform.unity().translateZ(-5));

        CSG screwHole1 = screwHolePrototype.transformed(unity().translateX(screwDistanceBig)).transformed(unity().rotZ(-45));
        CSG screwHole2 = screwHolePrototype.transformed(unity().translateX(screwDistanceBig)).transformed(unity().rotZ(135));
        CSG screwHole3 = screwHolePrototype.transformed(unity().translateX(screwDistanceSmall)).transformed(unity().rotZ(45));
        CSG screwHole4 = screwHolePrototype.transformed(unity().translateX(screwDistanceSmall)).transformed(unity().rotZ(-135));

        CSG washerPrototype = new Cylinder(screwRadius + washerWallThickness, washerHeight, 16).toCSG();

        CSG washerHole = washerPrototype.clone();

        washerPrototype = washerPrototype.weighted(new ZModifier()).transformed(unity().scale(1.35, 1.35, 1)).weighted(new UnityModifier());

        washerPrototype = washerPrototype.
                difference(screwHolePrototype).transformed(unity().translateZ(-washerHeight));

        CSG washer1 = washerPrototype.transformed(unity().translateX(screwDistanceBig)).transformed(unity().rotZ(-45));
        CSG washer2 = washerPrototype.transformed(unity().translateX(screwDistanceBig)).transformed(unity().rotZ(135));
        CSG washer3 = washerPrototype.transformed(unity().translateX(screwDistanceSmall)).transformed(unity().rotZ(45));
        CSG washer4 = washerPrototype.transformed(unity().translateX(screwDistanceSmall)).transformed(unity().rotZ(-135));

        CSG washerHole1 = washerHole.transformed(unity().translateX(screwDistanceBig)).transformed(unity().rotZ(-45)).transformed(Transform.unity().translateZ(-washerHeight * 2));
        CSG washerHole2 = washerHole.transformed(unity().translateX(screwDistanceBig)).transformed(unity().rotZ(135)).transformed(Transform.unity().translateZ(-washerHeight * 2));
        CSG washerHole3 = washerHole.transformed(unity().translateX(screwDistanceSmall)).transformed(unity().rotZ(45)).transformed(Transform.unity().translateZ(-washerHeight * 2));
        CSG washerHole4 = washerHole.transformed(unity().translateX(screwDistanceSmall)).transformed(unity().rotZ(-135)).transformed(Transform.unity().translateZ(-washerHeight * 2));

//        CSG hullCube = new RoundedCube(20,5,3.8).cornerRadius(1).toCSG().transformed(Transform.unity().translate(-10,-2.5,-3.8/2.0-enginePlatformThickness));
        CSG hullCube = new Cylinder(3, 20, 16).toCSG().transformed(Transform.unity().rotY(90)).transformed(Transform.unity().translate(0, 0, -2));

        enginePlatform = enginePlatform.union(hullCube).hull().difference(washerHole1, washerHole2, washerHole3, washerHole4);

        enginePlatform = enginePlatform.difference(mainHole, screwHole1, screwHole2, screwHole3, screwHole4).union(washer1, washer2, washer3, washer4);

        return enginePlatform;
    }

//    private CSG enginePlatform(double engineRadius, double enginePlatformThickness, double mainHoleRadius, double screwRadius, double screwDistance) {
//        CSG enginePlatform = new Cylinder(engineRadius, enginePlatformThickness, 32).toCSG();
//
//        CSG secondCyl = new Cylinder(engineRadius * 0.3, enginePlatformThickness, 3).toCSG().transformed(unity().translateX(-engineRadius * 2));
//
//        enginePlatform = enginePlatform.union(secondCyl).hull();
//
//        CSG mainHole = new Cylinder(mainHoleRadius, enginePlatformThickness, 16).toCSG();
//
//        CSG screwHolePrototype = new Cylinder(screwRadius, enginePlatformThickness, 16).toCSG();
//
//        double screwDistFromOrigin = screwDistance / 2.0;
//        double upperScrewDistFromOrigin1 = screwDistFromOrigin* 0.9;
//        double upperScrewDistFromOrigin2 = screwDistFromOrigin* 0.95;
//
//        CSG screwHole1 = screwHolePrototype.transformed(unity().translate(-screwDistFromOrigin, -screwDistFromOrigin, 0));
//        CSG screwHole2 = screwHolePrototype.transformed(unity().translate(screwDistFromOrigin, -screwDistFromOrigin, 0));
//        CSG screwHole3 = screwHolePrototype.transformed(unity().translate(screwDistFromOrigin, screwDistFromOrigin, 0));
//        CSG screwHole4 = screwHolePrototype.transformed(unity().translate(-screwDistFromOrigin, screwDistFromOrigin, 0));
//        
//        screwHole1 = screwHole1.union(screwHolePrototype.transformed(unity().translate(-upperScrewDistFromOrigin1, -upperScrewDistFromOrigin2, 0))).hull();
//        screwHole4 = screwHole4.union(screwHolePrototype.transformed(unity().translate(-upperScrewDistFromOrigin1, upperScrewDistFromOrigin2, 0))).hull();
//        
//        screwHole2 = screwHole2.union(screwHolePrototype.transformed(unity().translate(upperScrewDistFromOrigin1, -upperScrewDistFromOrigin1, 0))).hull();
//        screwHole3 = screwHole3.union(screwHolePrototype.transformed(unity().translate(upperScrewDistFromOrigin1, upperScrewDistFromOrigin1, 0))).hull();
//
//        enginePlatform = enginePlatform.difference(mainHole, screwHole1, screwHole2, screwHole3, screwHole4);
//
//        return enginePlatform;
//    }
    public static void main(String[] args) throws IOException {

        CSG result = new QuadrocopterArm().toCSG();

        FileUtil.write(Paths.get("quadrocopter-arm.stl"), result.toStlString());
        result.toObj().toFiles(Paths.get("quadrocopter-arm.obj"));

//        CSG resultNoStructure = new QuadrocopterArm().toCSG();
//
//        FileUtil.write(Paths.get("quadrocopter-arm-no-structure.stl"), resultNoStructure.toStlString());
//        resultNoStructure.toObj().toFiles(Paths.get("quadrocopter-arm-no-structure.obj"));
    }

}
