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
import eu.mihosoft.vrl.v3d.Vector3d;
import java.io.IOException;
import java.nio.file.Paths;

import static eu.mihosoft.vrl.v3d.Transform.*;

// TODO: Auto-generated Javadoc
/**
 * The Class LeapMotionCase.
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class LeapMotionCase {

    /** The w. */
    private double w = 80.5;
    
    /** The h. */
    private double h = 30.5;
    
    /** The d. */
    private double d = 12;

    /** The arc. */
    private double arc = 7.35;
    
    /** The arc res. */
    private int arcRes = 64;

    /** The case thickness. */
    private double caseThickness = 2.0;
    
    /** The device metal thickness. */
    private double deviceMetalThickness = 1.0;

    /** The peg thickness. */
    private double pegThickness = 0.8;
    
    /** The peg height. */
    private double pegHeight = -1;
    
    /** The peg width. */
    private double pegWidth = 16;
    
    /** The peg offset. */
    private double pegOffset = 0.6;
    
    /** The peg top height. */
    private double pegTopHeight = 2.4;
    
    /** The peg to case offset. */
    private double pegToCaseOffset = 2.5;
    
    /** The grab space. */
    private double grabSpace = 16;

    /**
     * Outline.
     *
     * @param w the w
     * @param h the h
     * @param d the d
     * @param arc the arc
     * @param thickness the thickness
     * @param arcRes the arc res
     * @return the csg
     */
    private CSG outline(double w, double h, double d, double arc, double thickness, int arcRes) {

        arc = arc + thickness;

        CSG arcCyl1 = new Cylinder(arc, d, arcRes).toCSG().transformed(
                        unity().translate(arc - thickness,arc - thickness,0));
        CSG arcCyl2 = new Cylinder(arc, d, arcRes).toCSG().transformed(
                        unity().translate(w - arc + thickness, arc - thickness,0));
        CSG arcCyl3 = new Cylinder(arc, d, arcRes).toCSG().transformed(
                        unity().translate(w - arc + thickness,h  - arc + thickness,0));
        CSG arcCyl4 = new Cylinder(arc, d, arcRes).toCSG().transformed(
                        unity().translate(arc - thickness,h  - arc + thickness,0));

        CSG arcCyls = arcCyl1.union(arcCyl2, arcCyl3, arcCyl4);

        return arcCyls.hull();
    }

    /**
     * Device outline.
     *
     * @return the csg
     */
    private CSG deviceOutline() {
        return outline(w, h, d, arc, 0, arcRes);
    }
    
    /**
     * Device inner outline.
     *
     * @return the csg
     */
    private CSG deviceInnerOutline() {
        return outline(w, h, d, arc, -deviceMetalThickness, arcRes);
    }

    /**
     * Case outline.
     *
     * @return the csg
     */
    private CSG caseOutline() {

        CSG outline = outline(w, h, d+caseThickness, arc, caseThickness, arcRes);

        CSG cyl = new Cylinder(grabSpace/2.0, h + caseThickness * 2, arcRes).toCSG().
                transformed(unity().rotX(90).translate(outline.getBounds().getBounds().x / 2.0 - caseThickness, -d, -caseThickness).scaleX(3.0));

        return outline.
                difference(deviceOutline().transformed(unity().translateZ(caseThickness))).difference(cyl);
    }

    /**
     * Peg.
     *
     * @return the csg
     */
    private CSG peg() {

        double fullPegHeight = pegHeight + d + caseThickness + pegTopHeight;

        return Extrude.points(Vector3d.z(pegWidth),
                new Vector3d(0, 0),
                new Vector3d(pegThickness, 0),
                new Vector3d(pegThickness, fullPegHeight),
                new Vector3d(0, fullPegHeight),
                new Vector3d(-pegOffset, fullPegHeight - pegTopHeight/2.0),
                new Vector3d(0, fullPegHeight - pegTopHeight)
        );
    }

    /**
     * Peg to front.
     *
     * @return the csg
     */
    private CSG pegToFront() {
        return peg().transformed(unity().rotX(-90).rotY(-90)).transformed(unity().translateX(-pegWidth / 2.0));
    }

    /**
     * Peg to back.
     *
     * @return the csg
     */
    private CSG pegToBack() {
        return peg().transformed(unity().rotX(-90).rotY(90)).transformed(unity().translateX(-pegWidth / 2.0));
    }

    /**
     * Full case.
     *
     * @return the csg
     */
    private CSG fullCase() {

        CSG caseOutline = caseOutline();
        
        // add protection space
        caseOutline = caseOutline.difference(deviceInnerOutline().transformed(unity().translate(0,0,caseThickness/2.0)));
        
        double outlineWidth =  caseOutline.getBounds().getBounds().x;
        
        caseOutline = addPegsToOutline(caseOutline, outlineWidth *0.25);
        caseOutline = addPegsToOutline(caseOutline, outlineWidth * 0.75);
        
        return caseOutline;
    }

    /**
     * Adds the pegs to outline.
     *
     * @param caseOutline the case outline
     * @param pos the pos
     * @return the csg
     */
    private CSG addPegsToOutline(CSG caseOutline, double pos) {
        
        pos = pos-caseThickness;
        
        CSG cyl1 = new Cylinder(pegWidth / 2.0 + pegToCaseOffset, h + caseThickness * 2, arcRes).toCSG().
                transformed(unity().rotX(90).translate(pos, -d, -caseThickness));
        
        caseOutline = caseOutline.difference(cyl1);
        
        CSG peg1 = pegToFront().transformed(unity().translate(pos, h, 0));
        CSG peg2 = pegToBack().transformed(unity().translate(pos + pegWidth, 0, 0));

        return caseOutline.union(peg1, peg2);
    }

    /**
     * To csg.
     *
     * @return the csg
     */
    public CSG toCSG() {

        return fullCase();
    }

    /**
     * The main method.
     *
     * @param args the arguments
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void main(String[] args) throws IOException {

        FileUtil.write(Paths.get("leapmotion.stl"), new LeapMotionCase().toCSG().toStlString());

        new LeapMotionCase().toCSG().toObj().toFiles(Paths.get("leapmotion.obj"));

    }

}
