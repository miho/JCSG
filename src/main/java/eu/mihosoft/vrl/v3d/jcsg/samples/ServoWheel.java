/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d.jcsg.samples;

import eu.mihosoft.vrl.v3d.jcsg.CSG;
import eu.mihosoft.vrl.v3d.jcsg.Cylinder;
import eu.mihosoft.vrl.v3d.jcsg.Extrude;
import eu.mihosoft.vrl.v3d.jcsg.FileUtil;
import eu.mihosoft.vrl.v3d.jcsg.Transform;
import eu.mihosoft.vrl.v3d.jcsg.Vector3d;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class ServoWheel {

    public static CSG servoTooth(double toothLength, double toothWidth, double toothHeight, double headHeight) {

         //
        //       |  tw  |
        //       --------    --
        //      /        \   th
        //     /          \  --
        //     
        //     |    tl    |
        //
        return Extrude.points(new Vector3d(0, 0, headHeight),
                new Vector3d(-toothLength / 2, 0),
                new Vector3d(-toothWidth / 2, toothHeight),
                new Vector3d(toothWidth / 2, toothHeight),
                new Vector3d(toothLength / 2, 0)
        );
    }

    public static CSG toCSG() {

//        return servoTooth(2, 1, 2, 6);
        return servoHead(5.92, 4, 25, 0.3, 0.7, 0.1);
    }

    public static CSG servoHead(
            double headDiameter,
            double headHeight,
            int toothCount,
            double toothHeight,
            double toothLength,
            double toothWidth) {

        double clear = 0.3;

        CSG cylinder = new Cylinder(
                new Vector3d(0, 0, 0), new Vector3d(0, 0, headHeight),
                headDiameter / 2 - toothHeight + clear + 0.03, toothCount*2).toCSG();

        CSG result = null;

        for (int i = 0; i < toothCount; i++) {
            
              CSG tooth = servoTooth(toothLength, toothWidth, toothHeight, headHeight);
              
              Transform translate = Transform.unity().translateY(headDiameter / 2 - toothHeight + clear);
              Transform rot = Transform.unity().rotZ(i * (360.0 / toothCount));  
              
              tooth = tooth.transformed(rot.apply(translate));
            
            if (i == 0) {
                result = tooth;
            } else {
                result = result.union(tooth);
            }
        }
        
        result = result.union(cylinder);

        return result;

    }
    
    public static CSG servoArm(
            double headDiameter,
            double headHeight,
            double headThickness,
            double headScrewDiameter,
            double toothLength, 
            double toothWidth,
            double armLength,
            int armCount) {
        return null;
    }
    
    public static CSG arm(double toothLength, double toothWidth, double headHeight, double headHeight1, double headHeight2, int holeCount) {
        return null;
    }
    
    private static CSG union(CSG first, CSG... csgs) {
        CSG result = first;
        
        for (CSG csg : csgs) {
            result = result.union(csg);
        }
        
        return first;
    }
    
    private static CSG difference(CSG first, CSG... csgs) {
        CSG result = first;
        
        for (CSG csg : csgs) {
            result = result.difference(csg);
        }
        
        return first;
    }
    
    private static CSG intersect(CSG first, CSG... csgs) {
        CSG result = first;
        
        for (CSG csg : csgs) {
            result = result.intersect(csg);
        }
        
        return first;
    }
    
    public static void main(String[] args) throws IOException {
        
        System.out.println("RUNNING");

        FileUtil.write(new File("servo-wheel.stl"), new ServoWheel().toCSG().toStlString());

    }
}
