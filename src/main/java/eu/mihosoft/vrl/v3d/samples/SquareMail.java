/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d.samples;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Cube;
import eu.mihosoft.vrl.v3d.FileUtil;
import static eu.mihosoft.vrl.v3d.Transform.unity;
import eu.mihosoft.vrl.v3d.Vector3d;
import java.io.IOException;
import java.nio.file.Paths;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class SquareMail {

    private CSG toCSG(int numX, int numY) {

//        CSG.setDefaultOptType(CSG.OptType.POLYGON_BOUND);
        double w = 10;
        
        double d = 2.2;

        double jointRadius = 1.1;
        double coneLength = 1.8;
        double hingeHoleScale = 1.15;
        
        double h = w;
        double pinLength = 0.8;
        double pinThickness = 1.2;
        
        CSG mainCube = new Cube(w, h, d).toCSG();

        Hinge hingePrototype = new Hinge().setJointRadius(jointRadius).
                setJointLength(pinThickness).setConeLength(coneLength);
        hingePrototype.setJointConnectionThickness(
                hingePrototype.getJointRadius() * 2);

        CSG hinge1 = hingePrototype.toCSG();

        Vector3d hingeBounds = hinge1.getBounds().getBounds();

        hinge1 = hinge1.intersect(new Cube(hingeBounds.x,
                Math.min(hingeBounds.y, d), hingeBounds.z).toCSG());

        hinge1 = hinge1.transformed(unity().rotX(90));

        CSG pin = new Cube(pinLength + hingePrototype.getJointRadius(),
                pinThickness, d).toCSG().transformed(unity().
                        translateX(-(jointRadius + pinLength) * 0.5));

        hinge1 = hinge1.union(pin);

        hinge1 = hinge1.transformed(unity().
                translateX(w * 0.5 + hingePrototype.getJointRadius()
                        + pinLength));

        CSG hinge2 = hinge1.transformed(unity().rotZ(90));

        CSG hingeHole1 = hinge1.transformed(unity().translateX(
                -w * 0.5 - hingePrototype.getJointRadius()
                - pinLength));

        hingeHole1 = hingeHole1.transformed(unity().scale(hingeHoleScale));

        hingeHole1 = hingeHole1.transformed(unity().translateX(
                -w * 0.5 + jointRadius*hingeHoleScale));
        
        CSG hingeHole2 = hingeHole1.transformed(unity().rotZ(90));

        CSG part = mainCube.union(hinge1, hinge2).difference(hingeHole1, hingeHole2);

        Vector3d partBounds = part.getBounds().getBounds();

        CSG result = null;

        for (int y = 0; y < numY; y++) {

            for (int x = 0; x < numX; x++) {

                double translateX
                        = (-partBounds.x + jointRadius + jointRadius * hingeHoleScale) * x;
                double translateY
                        = (-partBounds.y + jointRadius + jointRadius * hingeHoleScale) * y;

                CSG part2 = part.transformed(unity().translate(translateX, translateY, 0));

                if (result == null) {
                    result = part2.clone();
                }

                result = result.dumbUnion(part2);
            }
        }

        return result;

    }

    public static void main(String[] args) throws IOException {
        FileUtil.write(Paths.get("squaremail-test.stl"), new SquareMail().toCSG(12,4).toStlString());
    }
}
