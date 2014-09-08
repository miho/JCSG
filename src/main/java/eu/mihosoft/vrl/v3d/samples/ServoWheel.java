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

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class ServoWheel {
    
                private double toothLength = 0.7;
    private double toothWidth = 0.1;
    private double toothHeight = 0.3;
    private int toothCount = 25;
    private double headHeight = 4;
    private double headDiameter = 5.92;
    private double headScrewDiameter = 2.5;
    private double headThickness = 1.1;

    

    public CSG toCSG() {
        
        ServoHead servoHead = new ServoHead(toothLength, toothWidth, toothHeight, toothCount, headHeight, headDiameter, headScrewDiameter, headThickness);
        
        return servoHead.servoHeadFemale();
    }

  
    
    public CSG servoArm(
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
    
    public CSG arm(double toothLength, double toothWidth, double headHeight, double headHeight1, double headHeight2, int holeCount) {
        return null;
    }
    
    
    public static void main(String[] args) throws IOException {
        
        System.out.println("RUNNING");

        FileUtil.write(Paths.get("servo-wheel.stl"), new ServoWheel().toCSG().toStlString());

    }
}
