/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg.samples;

import eu.mihosoft.jcsg.Extrude;
import eu.mihosoft.jcsg.CSG;
import eu.mihosoft.jcsg.FileUtil;
import eu.mihosoft.vvecmath.Transform;
import eu.mihosoft.vvecmath.Vector3d;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author miho
 */
public class ServoMount {

    // mini servo
//    private double servoWidth = 22.9;
//    private double servoThickness = 12.0;
//    
    //standard servo
    private double servoWidth = 40.0;
    private double servoThickness = 19.0;
    private double borderThickness = 2;
    private double overlap = 3;
    private double servoMountHeight = 20;

    private double boardMountingThickness = 2;
    private double boardHolderLength = 12;
    
    private double boardMountingWidth = 8.1;
    
    private double pegHeight= 1;
    private double pegToothHeight = 0.6;
    private double pegOverlap = 0.5;

    public CSG toCSGSimple() {

        return Extrude.points(Vector3d.xyz(0, 0, servoMountHeight),
                Vector3d.xy(0, servoThickness),
                Vector3d.xy(overlap, servoThickness),
                Vector3d.xy(-borderThickness, servoThickness + borderThickness),
                Vector3d.xy(-borderThickness, -borderThickness),
                Vector3d.xy(servoWidth + borderThickness, -borderThickness),
                Vector3d.xy(servoWidth + borderThickness, servoThickness + borderThickness),
                Vector3d.xy(servoWidth - overlap, servoThickness),
                Vector3d.xy(servoWidth, servoThickness),
                Vector3d.xy(servoWidth, 0),
                Vector3d.xy(0, 0)
        );
    }

    public CSG toCSG() {
        CSG bm1 = boardMount().transformed(Transform.unity().rotY(90).rotZ(90).translate(borderThickness, borderThickness, -boardHolderLength + borderThickness));
        CSG bm2 = bm1.transformed(Transform.unity().translateX(servoWidth -boardHolderLength + borderThickness*2));
        CSG sm = toCSGSimple();

        return sm.union(bm1).union(bm2);//.transformed(Transform.unity().scale(0.08));
    }

    private CSG boardMount() {

        double h = boardMountingWidth;
        
        List<Vector3d> points = Arrays.asList(Vector3d.ZERO,
                Vector3d.xy(0, -borderThickness),
                Vector3d.xy(boardMountingThickness + borderThickness, -borderThickness),
                Vector3d.xy(boardMountingThickness + borderThickness, h + pegToothHeight+pegHeight),
                Vector3d.xy(boardMountingThickness - pegOverlap, h + pegToothHeight),
                 Vector3d.xy(boardMountingThickness, h),
                Vector3d.xy(boardMountingThickness, 0)
        );
        
        Collections.reverse(points);

        return Extrude.points(Vector3d.xyz(0,0,boardHolderLength),
                points
        );
    }
    
        public static void main(String[] args) throws IOException {

        ServoMount sMount = new ServoMount();

        // save union as stl
//        FileUtil.write(Paths.get("sample.stl"), new ServoHead().servoHeadFemale().transformed(Transform.unity().scale(1.0)).toStlString());
        FileUtil.write(Paths.get("servo-mount.stl"), sMount.toCSG().toStlString());

    }
}
