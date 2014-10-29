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
import static eu.mihosoft.vrl.v3d.Transform.unity;
import eu.mihosoft.vrl.v3d.Vector3d;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class PolyMailTile {

    private boolean male;

    double radius = 10;

    double d = 2.2;

    double jointRadius = 1.1;
    double coneLength = 1.8;
    double hingeHoleScale = 1.15;

    double pinLength = 0.8;
    double pinThickness = 1.2;

    private CSG toCSG(int numEdges) {

//        CSG.setDefaultOptType(CSG.OptType.POLYGON_BOUND);
        double step = 360.0 / numEdges;
        double initialRot = step * 0.5;

        CSG mainPrism = new Cylinder(radius, d, numEdges).toCSG().
                transformed(unity().translateZ(-d * 0.5).rotZ(initialRot));

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

        double apothem = radius * Math.cos(Math.toRadians(180.0 / numEdges));

        hinge1 = hinge1.transformed(unity().
                translateX(apothem + hingePrototype.getJointRadius()
                        + pinLength));

        List<CSG> hinges = new ArrayList<>();

        hinges.add(hinge1);

        for (int i = 1; i < numEdges; i++) {
            CSG hinge = hinge1.transformed(unity().rotZ(i * step));
            hinges.add(hinge);
        }

        CSG hingeHole1 = hinge1.transformed(unity().translateX(
                -apothem - hingePrototype.getJointRadius()
                - pinLength));

        hingeHole1 = hingeHole1.transformed(unity().scale(hingeHoleScale));

        hingeHole1 = hingeHole1.transformed(unity().translateX(
                -apothem + jointRadius * hingeHoleScale));

        hingeHole1 = hingeHole1.transformed(unity().rotZ(initialRot));

        List<CSG> hingeHoles = new ArrayList<>();

        hingeHoles.add(hingeHole1);

        for (int i = 1; i < numEdges; i++) {
            CSG hole = hingeHole1.transformed(unity().rotZ(i * step));
            hingeHoles.add(hole);
        }

        CSG malePart = mainPrism.union(hinges);
        CSG femalePart = mainPrism.difference(hingeHoles);

        Vector3d malePartBounds = malePart.getBounds().getBounds();

        return malePart;

    }

    public static void main(String[] args) throws IOException {
        FileUtil.write(Paths.get("triangularmail.stl"), new PolyMailTile().toCSG(4).toStlString());
    }
}
