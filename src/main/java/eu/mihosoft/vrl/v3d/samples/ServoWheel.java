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
import static eu.mihosoft.vrl.v3d.Transform.unity;
import eu.mihosoft.vrl.v3d.Vector3d;
import java.io.IOException;
import java.nio.file.Paths;

// TODO: Auto-generated Javadoc
/**
 * The Class ServoWheel.
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class ServoWheel {

    /** The tooth length. */
    private double toothLength = 0.7;
    
    /** The tooth width. */
    private double toothWidth = 0.1;
    
    /** The tooth height. */
    private double toothHeight = 0.3;
    
    /** The tooth count. */
    private int toothCount = 25;
    
    /** The head height. */
    private double headHeight = 4;
    
    /** The head diameter. */
    private double headDiameter = 5.92;
    
    /** The head screw diameter. */
    private double headScrewDiameter = 2.5;
    
    /** The head thickness. */
    private double headThickness = 1.1;

    /** The servo head. */
    private ServoHead servoHead = new ServoHead(toothLength, toothWidth, toothHeight, toothCount, headHeight, headDiameter, headScrewDiameter, headThickness);

    /** The number of arms. */
    private int numberOfArms = 7;
    
    /** The inner width. */
    double innerWidth = 7;
    
    /** The outer width. */
    double outerWidth = 3.5;
    
    /** The thickness. */
    double thickness = 2;
    
    /** The radius. */
    double radius = 27.5;
    
    /** The ring thickness. */
    double ringThickness = 3;
    
    /** The wheel thickness. */
    double wheelThickness = 5;

    /** The minor arm length. */
    double minorArmLength = radius * 0.75;
    
    /** The minor arm height. */
    double minorArmHeight = headHeight;
    
    /** The minor arm thickness. */
    double minorArmThickness = 2.5;
    
    /** The outer ring thickness. */
    double outerRingThickness = wheelThickness/3.0*2;
    
    /** The outer ring depth. */
    double outerRingDepth = 0.5;

    /**
     * To csg.
     *
     * @return the csg
     */
    public CSG toCSG() {

        double dt = 360.0 / numberOfArms;

        CSG arms = null;

        for (int i = 0; i < numberOfArms; i++) {

            CSG arm = servoArm(innerWidth, outerWidth, thickness, radius, ringThickness, minorArmThickness, minorArmLength, minorArmHeight).transformed(unity().rotZ(dt * i));

            if (arms == null) {
                arms = arm;
            } else {
                arms = arms.union(arm);
            }
        }

        CSG sHead = servoHead.servoHeadFemale();

        CSG screwHole = new Cylinder(headScrewDiameter / 2.0, ringThickness * 2, 16).toCSG();

        if (arms != null) {
            sHead = sHead.union(arms);
        }

        sHead = sHead.difference(screwHole);

        CSG outerWheelCylinder = new Cylinder(radius, wheelThickness, 64).toCSG();
        CSG innerWheelCylinder = new Cylinder(radius - ringThickness, wheelThickness, 64).toCSG();

        CSG ring = outerWheelCylinder.difference(innerWheelCylinder);

        CSG wheel = ring.union(sHead);
        
       
        
        CSG outerRingOutCylinder = new Cylinder(radius, outerRingThickness, 64).toCSG();
        CSG outerRingInnerCylinder = new Cylinder(radius-outerRingDepth, outerRingThickness, 64).toCSG();
        
        CSG outerRing = outerRingOutCylinder.difference(outerRingInnerCylinder).
                transformed(unity().translateZ(wheelThickness*0.5-outerRingThickness*0.5));
        
        wheel = wheel.difference(outerRing);
        
        return wheel;
    }

    /**
     * Servo arm.
     *
     * @param innerWidth the inner width
     * @param outerWidth the outer width
     * @param thickness the thickness
     * @param radius the radius
     * @param wheelThickness the wheel thickness
     * @param minorArmThickness the minor arm thickness
     * @param minorArmLegth the minor arm legth
     * @param minorArmHeight the minor arm height
     * @return the csg
     */
    public CSG servoArm(
            double innerWidth, double outerWidth, double thickness, double radius, double wheelThickness, double minorArmThickness, double minorArmLegth, double minorArmHeight) {
        CSG mainArm = Extrude.points(Vector3d.z(thickness),
                new Vector3d(-innerWidth * 0.5, 0),
                new Vector3d(innerWidth * 0.5, 0),
                new Vector3d(outerWidth * 0.5, radius - wheelThickness),
                new Vector3d(-outerWidth * 0.5, radius - wheelThickness)
        );

        CSG minorArm = Extrude.points(Vector3d.z(minorArmThickness),
                new Vector3d(headDiameter * 0.5 + headThickness * 0.5, thickness),
                new Vector3d(minorArmLegth - headDiameter * 0.5 - headThickness * 0.5, thickness),
                new Vector3d(headDiameter * 0.5 + headThickness * 0.5, minorArmHeight + thickness * 0.5)).transformed(unity().rot(-90, 0, 0).translateZ(-minorArmThickness * 0.5));

        minorArm = minorArm.transformed(unity().rotZ(-90));

        return mainArm.union(minorArm);

    }

    /**
     * The main method.
     *
     * @param args the arguments
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void main(String[] args) throws IOException {

        System.out.println("RUNNING");

        FileUtil.write(Paths.get("servo-wheel.stl"), new ServoWheel().toCSG().toStlString());

    }
}
