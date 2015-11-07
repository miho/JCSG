/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d.samples;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Extrude;
import eu.mihosoft.vrl.v3d.FileUtil;
import eu.mihosoft.vrl.v3d.Transform;
import eu.mihosoft.vrl.v3d.Vector3d;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class ServoMount.
 *
 * @author miho
 */
public class ServoMount {

    // mini servo
//    private double servoWidth = 22.9;
//    private double servoThickness = 12.0;
//    
    /** The servo width. */
    //standard servo
    private double servoWidth = 40.0;
    
    /** The servo thickness. */
    private double servoThickness = 19.0;
    
    /** The border thickness. */
    private double borderThickness = 2;
    
    /** The overlap. */
    private double overlap = 3;
    
    /** The servo mount height. */
    private double servoMountHeight = 20;

    /** The board mounting thickness. */
    private double boardMountingThickness = 2;
    
    /** The board holder length. */
    private double boardHolderLength = 12;
    
    /** The board mounting width. */
    private double boardMountingWidth = 8.1;
    
    /** The peg height. */
    private double pegHeight= 1;
    
    /** The peg tooth height. */
    private double pegToothHeight = 0.6;
    
    /** The peg overlap. */
    private double pegOverlap = 0.5;

    /**
     * To csg simple.
     *
     * @return the csg
     */
    public CSG toCSGSimple() {

        return Extrude.points(new Vector3d(0, 0, servoMountHeight),
                new Vector3d(0, servoThickness),
                new Vector3d(overlap, servoThickness),
                new Vector3d(-borderThickness, servoThickness + borderThickness),
                new Vector3d(-borderThickness, -borderThickness),
                new Vector3d(servoWidth + borderThickness, -borderThickness),
                new Vector3d(servoWidth + borderThickness, servoThickness + borderThickness),
                new Vector3d(servoWidth - overlap, servoThickness),
                new Vector3d(servoWidth, servoThickness),
                new Vector3d(servoWidth, 0),
                new Vector3d(0, 0)
        );
    }

    /**
     * To csg.
     *
     * @return the csg
     */
    public CSG toCSG() {
        CSG bm1 = boardMount().transformed(Transform.unity().rotY(90).rotZ(90).translate(borderThickness, borderThickness, -boardHolderLength + borderThickness));
        CSG bm2 = bm1.transformed(Transform.unity().translateX(servoWidth -boardHolderLength + borderThickness*2));
        CSG sm = toCSGSimple();

        return sm.union(bm1).union(bm2);//.transformed(Transform.unity().scale(0.08));
    }

    /**
     * Board mount.
     *
     * @return the csg
     */
    private CSG boardMount() {

        double h = boardMountingWidth;
        
        List<Vector3d> points = Arrays.asList(Vector3d.ZERO,
                new Vector3d(0, -borderThickness),
                new Vector3d(boardMountingThickness + borderThickness, -borderThickness),
                new Vector3d(boardMountingThickness + borderThickness, h + pegToothHeight+pegHeight),
                new Vector3d(boardMountingThickness - pegOverlap, h + pegToothHeight),
                 new Vector3d(boardMountingThickness, h),
                new Vector3d(boardMountingThickness, 0)
        );
        
        Collections.reverse(points);

        return Extrude.points(new Vector3d(0,0,boardHolderLength),
                points
        );
    }
    
        /**
         * The main method.
         *
         * @param args the arguments
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public static void main(String[] args) throws IOException {

        ServoMount sMount = new ServoMount();

        // save union as stl
//        FileUtil.write(Paths.get("sample.stl"), new ServoHead().servoHeadFemale().transformed(Transform.unity().scale(1.0)).toStlString());
        FileUtil.write(Paths.get("servo-mount.stl"), sMount.toCSG().toStlString());

    }
}
