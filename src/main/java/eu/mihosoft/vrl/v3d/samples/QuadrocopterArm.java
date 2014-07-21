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
import eu.mihosoft.vrl.v3d.Sphere;
import eu.mihosoft.vrl.v3d.Transform;
import java.io.IOException;
import java.nio.file.Paths;

import static eu.mihosoft.vrl.v3d.Transform.*;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class QuadrocopterArm {

    public CSG mainArm(int numInnerStructures, double length, double armThickness, double innerTubeOffset) {

        double outerRadius = armThickness/2.0;
        double wallThickness = 0.4;

        double structureRadius = 0.4;
        double maxXRot = 180;
        double maxYRot = 180;
        double maxZRot = 180;

        double maxXYOffset = outerRadius;

        double innerRadius = 3.0;
        double innerWallThickness = 0.5;

        double numPlates = 0;
        double plateThickness = 0.5;

        double shrinkFactorX = 0.5;

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
                    innerTubeOffset +Math.random() * (length - innerTubeOffset)));

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

        CSG innerCyl = new Cylinder(innerRadius, length-innerTubeOffset, 16).toCSG().transformed(unity().translateZ(innerTubeOffset));

        CSG finalGeometry = outerCyl.union(innerCyl);

        CSG plate = new Cylinder(outerRadius, plateThickness, 16).toCSG().
                transformed(unity().scaleX(shrinkFactorX));
        
        CSG endPlate = plate.transformed(unity().translateZ(innerTubeOffset));
        
        finalGeometry = finalGeometry.union(endPlate);

        CSG plates = null;

        if (numPlates > 0) {
            double dt = (length-innerTubeOffset) / numPlates;
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
        
        CSG cube = new Cube(outerRadius*2, outerRadius*2, outerRadius/2.0).
                toCSG().difference(innerCyl).
                transformed(unity().translateZ(length-outerRadius/4.0));
        
        finalGeometry = finalGeometry.union(cube);
        
        finalGeometry = finalGeometry.difference(
                new Cylinder(innerRadius - innerWallThickness,
                        length, 16).toCSG());

        return finalGeometry;
    }

    private CSG outerCyl(double outerRadius, double length,
            double wallThickness, double scaleOuter, double scaleInner) {
        CSG outerCyl = new Cylinder(outerRadius, length, 32).toCSG().
                transformed(unity().scaleX(scaleOuter));
        CSG outerCylInner = new Cylinder(outerRadius - wallThickness / scaleOuter,
                length, 32).toCSG().transformed(unity().scaleX(scaleInner));
        outerCyl = outerCyl.difference(outerCylInner);
        return outerCyl;
    }

    public CSG toCSG() {
        
        // optimization seems to cause problems
        CSG.setDefaultOptType(CSG.OptType.NONE);
        
        double engineRadius = 13;
        double screwDistance = 12.5;
        double screwRadius = 1.25;
        double enginePlatformThickness = 2.0;
        double mainHoleRadius = 3.6;
        
        double armLength = 120;
        int numInnerStructures = 32;
        double armThickness = 18;
        
        double innerTubeOffset = engineRadius*2+5;
        
        CSG mainArm = mainArm(numInnerStructures, armLength, armThickness, innerTubeOffset).transformed(unity().rotX(90).rotY(90));
        
        CSG enginePlatformSphere = new Sphere(engineRadius*1.1,64,32).toCSG().transformed(unity().scaleX(2).translateZ(armThickness/2.0));
        
        Transform engineTransform = unity().translateX(-mainHoleRadius).translateZ(-armThickness*0.28);
        
        CSG mainHole = new Cylinder(mainHoleRadius, enginePlatformThickness, 16).toCSG().transformed(engineTransform);
        CSG enginePlatform = enginePlatform(engineRadius, enginePlatformThickness, mainHoleRadius, screwRadius, screwDistance).transformed(engineTransform);
        
        return mainArm.difference(enginePlatformSphere).union(enginePlatform).difference(mainHole);
    }

    private CSG enginePlatform(double engineRadius, double enginePlatformThickness, double mainHoleRadius, double screwRadius, double screwDistance) {
        CSG enginePlatform = new Cylinder(engineRadius, enginePlatformThickness, 32).toCSG();
        
        CSG secondCyl = new Cylinder(engineRadius*0.3, enginePlatformThickness, 3).toCSG().transformed(unity().translateX(-engineRadius*2));
        
        enginePlatform = enginePlatform.union(secondCyl).hull();
        
        CSG mainHole = new Cylinder(mainHoleRadius, enginePlatformThickness, 16).toCSG();
        
        CSG screwHolePrototype = new Cylinder(screwRadius, enginePlatformThickness, 16).toCSG();
        
        double screwDistFromOrigin = screwDistance/2.0;
        
        CSG screwHole1 = screwHolePrototype.transformed(unity().translate(-screwDistFromOrigin,-screwDistFromOrigin,0));
        CSG screwHole2 = screwHolePrototype.transformed(unity().translate(screwDistFromOrigin,-screwDistFromOrigin,0));
        CSG screwHole3 = screwHolePrototype.transformed(unity().translate(screwDistFromOrigin,screwDistFromOrigin,0));
        CSG screwHole4 = screwHolePrototype.transformed(unity().translate(-screwDistFromOrigin,screwDistFromOrigin,0));
        
        
        enginePlatform = enginePlatform.difference(mainHole, screwHole1, screwHole2, screwHole3, screwHole4);
        
        
        return enginePlatform;
    }

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
