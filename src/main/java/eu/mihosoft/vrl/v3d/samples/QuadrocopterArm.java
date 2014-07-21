/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d.samples;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Cylinder;
import eu.mihosoft.vrl.v3d.Extrude;
import eu.mihosoft.vrl.v3d.FileUtil;
import eu.mihosoft.vrl.v3d.Transform;
import eu.mihosoft.vrl.v3d.Vector3d;
import java.io.IOException;
import java.nio.file.Paths;

import static eu.mihosoft.vrl.v3d.Transform.*;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class QuadrocopterArm {

    public CSG toCSG() {

        double outerRadius = 15;
        double length = 150;
        double wallThickness = 0.4;

        double structureRadius = 0.4;
        double maxXRot = 180;
        double maxYRot = 180;
        double maxZRot = 180;

        double maxXYOffset = outerRadius;

        double innerRadius = 5;
        double innerWallThickness = 0.4;

        double numPlates = 0;
        double plateThickness = 1;
        
        double shrinkFactorX = 0.5;
        
        // optimization seems to cause problems
        CSG.setDefaultOptType(CSG.OptType.NONE);

        CSG innerStructure = null;

        for (int i = 0; i < 50; i++) {
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
                    Math.random() * length));

            if (innerStructure == null) {
                innerStructure = cyl;
            } else {
                innerStructure = innerStructure.union(cyl);
            }

        }

        innerStructure = innerStructure.intersect(
                new Cylinder(outerRadius, length, 16).toCSG().
                transformed(unity().scaleX(0.5)));

        CSG outerCyl = outerCyl(outerRadius, length, wallThickness,
                shrinkFactorX, shrinkFactorX*0.95).union(innerStructure);

        CSG innerCyl = new Cylinder(innerRadius, length, 16).toCSG();

        CSG finalGeometry = outerCyl.union(innerCyl);

        CSG plate = new Cylinder(outerRadius, plateThickness, 16).toCSG().
                transformed(unity().scaleX(shrinkFactorX));

        CSG plates = null;

        if (numPlates > 0) {
            double dt = length / numPlates;
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
        finalGeometry = finalGeometry.difference(
                new Cylinder(innerRadius - innerWallThickness,
                        length, 16).toCSG());

        return finalGeometry;
    }

    private CSG outerCyl(double outerRadius, double length, double wallThickness, double scaleOuter, double scaleInner) {
        CSG outerCyl = new Cylinder(outerRadius, length, 32).toCSG().transformed(unity().scaleX(scaleOuter));
        CSG outerCylInner = new Cylinder(outerRadius - wallThickness/scaleOuter, length, 32).toCSG().transformed(unity().scaleX(scaleInner));
        outerCyl = outerCyl.difference(outerCylInner);
        return outerCyl;
    }

    public static void main(String[] args) throws IOException {

        FileUtil.write(Paths.get("quadrocopter-arm.stl"), new QuadrocopterArm().toCSG().toStlString());

        new QuadrocopterArm().toCSG().toObj().toFiles(Paths.get("quadrocopter-arm.obj"));

    }

}
