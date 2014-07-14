/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d;

import java.io.IOException;
import java.nio.file.Paths;

import static eu.mihosoft.vrl.v3d.Transform.*;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class LeapMotionCase {

    private double w = 80.5;
    private double h = 30.1;
    private double d = 11;

    private double arc = 7.25;
    private int arcRes = 32;

    private double caseThickness = 1;

    private double pegHeight = 1.0;
    private double pegWidth = 10;
    private double pegOffset = 0.6;
    private double pegTopHeight = 2;
    private double pegToCaseOffset = 2.5;

    private CSG outline(double w, double h, double d, double arc, double thickness, int arcRes) {

        arc = arc + thickness;

        CSG arcCyl1 = new Cylinder(arc, d, arcRes).toCSG().transformed(unity().translateX(arc - thickness).translateY(arc - thickness));
        CSG arcCyl2 = arcCyl1.transformed(unity().translateX(w - arc * 2 + thickness));
        CSG arcCyl3 = arcCyl2.transformed(unity().translateY(h - arc * 2 + thickness * 2));
        CSG arcCyl4 = arcCyl1.transformed(unity().translateY(h - arc * 2 + thickness * 2));
        CSG arcCyls = arcCyl1.union(arcCyl2, arcCyl3, arcCyl4);

        return arcCyls.hull();
    }

    private CSG deviceOutline() {
        return outline(w, h, d, arc, 0, arcRes);
    }

    private CSG caseOutline() {

        CSG outline = outline(w + caseThickness, h, d, arc, caseThickness, arcRes);

        CSG cyl = new Cylinder(pegWidth / 2.0 + pegToCaseOffset, h + caseThickness * 2, arcRes).toCSG().
                transformed(unity().rotX(90).translate(outline.getBounds().getBounds().x / 2.0, -d, -caseThickness));

        return outline.
                difference(deviceOutline().transformed(unity().translateZ(caseThickness))).difference(cyl);
    }

    private CSG peg() {

        double fullPegHeight = pegHeight + d + pegTopHeight;

        return Extrude.points(Vector3d.z(pegWidth),
                new Vector3d(0, 0),
                new Vector3d(caseThickness, 0),
                new Vector3d(caseThickness, fullPegHeight),
                new Vector3d(0, fullPegHeight),
                new Vector3d(-pegOffset, fullPegHeight - pegTopHeight),
                new Vector3d(0, fullPegHeight - pegTopHeight)
        );
    }

    private CSG pegToFront() {
        return peg().transformed(unity().rotX(-90).rotY(-90)).transformed(unity().translateX(-pegWidth / 2.0));
    }

    private CSG pegToBack() {
        return peg().transformed(unity().rotX(-90).rotY(90)).transformed(unity().translateX(-pegWidth / 2.0));
    }

    private CSG fullCase() {

        CSG caseOutline = caseOutline();

        CSG peg1 = pegToFront().transformed(unity().translate(caseOutline.getBounds().getBounds().x / 2, h, 0));
        CSG peg2 = pegToBack().transformed(unity().translate(caseOutline.getBounds().getBounds().x / 2 + pegWidth, 0, 0));

        return caseOutline.union(peg1, peg2);
    }

    public CSG toCSG() {

        return fullCase();
    }

    public static void main(String[] args) throws IOException {

        FileUtil.write(Paths.get("leapmotion.stl"), new LeapMotionCase().toCSG().toStlString());

        new LeapMotionCase().toCSG().toObj().toFiles(Paths.get("leapmotion.obj"));

    }

}
