/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d;

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

//              tooth = tooth.transformed(rot);
            
            if (i == 0) {
                result = tooth;
            } else {
                result = result.union(tooth);
            }
        }
        
        result = result.union(cylinder);

        return result;

    }
}
