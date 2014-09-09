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

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class ServoHead {

    private double toothLength = 0.7;
    private double toothWidth = 0.1;
    private double toothHeight = 0.3;
    private int toothCount = 25;
    private double headHeight = 4;
    private double headDiameter = 5.92;
    private double headScrewDiameter = 2.5;
    private double headThickness = 1.1;

    public ServoHead(
            double toothLength,
            double toothWidth,
            double toothHeight,
            int toothCount,
            double headHeight,
            double headDiameter,
            double headScrewDiameter,
            double headThickness) {

        this.toothLength = toothLength;
        this.toothWidth = toothWidth;
        this.toothHeight = toothHeight;
        this.toothCount = toothCount;
        this.headHeight = headHeight;
        this.headDiameter = headDiameter;
        this.headScrewDiameter = headScrewDiameter;
        this.headThickness = headThickness;
    }

    public ServoHead() {
    }

    public CSG servoTooth() {

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

    public CSG servoHeadMale() {

        double clear = 0.3;

        CSG cylinder = new Cylinder(
                new Vector3d(0, 0, 0), new Vector3d(0, 0, headHeight),
                headDiameter / 2 - toothHeight + clear + 0.03, toothCount * 2).toCSG();

        CSG result = null;

        for (int i = 0; i < toothCount; i++) {

            CSG tooth = servoTooth();

            Transform translate = Transform.unity().translateY(headDiameter / 2 - toothHeight + clear);
            Transform rot = Transform.unity().rotZ(i * (360.0 / toothCount));

            tooth = tooth.transformed(rot.apply(translate));

            if (result == null) {
                result = tooth;
            } else {
                result = result.union(tooth);
            }
        }

        if (result != null) {
            result = result.union(cylinder);
        }

        return result;
    }

    public CSG servoHeadFemale() {

        CSG cyl1 = new Cylinder(headDiameter / 2 + headThickness, headHeight + 1, 16).toCSG();
//        cyl1 = cyl1.transformed(Transform.unity().translateZ(0.1));

        CSG cyl2 = new Cylinder(headScrewDiameter / 2, 10, 16).toCSG();

        CSG head = servoHeadMale();

        CSG headFinal = cyl1.difference(cyl2).difference(head);

        return headFinal.transformed(unity().rotX(180).translateZ(-headHeight-headThickness));
    }
    
    
        public static void main(String[] args) throws IOException {
        
        System.out.println("RUNNING");

        FileUtil.write(Paths.get("servo-head-female.stl"), new ServoHead().servoHeadFemale().toStlString());
         FileUtil.write(Paths.get("servo-head-male.stl"), new ServoHead().servoHeadMale().toStlString());

    }
}
