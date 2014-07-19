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
import eu.mihosoft.vrl.v3d.Transform;
import eu.mihosoft.vrl.v3d.Vector3d;
import java.io.IOException;
import java.nio.file.Paths;

/**
 *
 * @author miho
 */
public class ArduinoMount {

    private double bottomWidth = 68.6;
    private double bottomHeight = 53.3;
    private double bottomThickness = 2;

    private double pinHeight = 12;
    private double pinHoleHeight = 4.8;
    private double pinRadius = 2;
    private double boardThickness = 2.0;
    
    private double servoConnectThickness = 7;

    private CSG board() {
        return new Cube(Vector3d.ZERO, new Vector3d(bottomWidth, bottomHeight, bottomThickness)).toCSG();
    }

    private CSG pins() {
        CSG prototype = new Cylinder(pinRadius, pinHeight, 16).toCSG();

        CSG first = prototype.clone().transformed(Transform.unity().translate(bottomWidth / 2.0, bottomHeight / 2.0, 0));
        CSG second = prototype.clone().transformed(Transform.unity().translate(bottomWidth / 2.0, -bottomHeight / 2.0, 0));
        CSG third = prototype.clone().transformed(Transform.unity().translate(-bottomWidth / 2.0, 0, 0));

        CSG pins = first.union(second).union(third);

        CSG board = board().transformed(Transform.unity().translateZ(pinHoleHeight*2));

        return pins.difference(board);
    }

    private CSG pinConnections() {

        CSG first = new Cube(Vector3d.ZERO, new Vector3d(bottomWidth / 2, 3, bottomThickness)).
                toCSG().transformed(Transform.unity().translate(-bottomWidth / 4, 0, bottomThickness / 2));
        CSG second = new Cube(Vector3d.ZERO, new Vector3d(bottomWidth / 2 + 10, 3, bottomThickness)).
                toCSG().transformed(Transform.unity().rotZ(37.8).translate(bottomWidth / 4 + 5, 0, bottomThickness / 2));
        CSG third = new Cube(Vector3d.ZERO, new Vector3d(bottomWidth / 2 + 10, 3, bottomThickness)).
                toCSG().transformed(Transform.unity().rotZ(-37.8).translate(bottomWidth / 4 + 5, 0, bottomThickness / 2));

        return first.union(second).union(third);
    }
    
    private CSG servoConnect() {

        CSG firstA = new Cube(Vector3d.ZERO, new Vector3d(bottomWidth, servoConnectThickness, bottomThickness)).
                toCSG().transformed(Transform.unity().translate(0, -bottomHeight/2, bottomThickness / 2));
       
        CSG firstB = new Cube(Vector3d.ZERO, new Vector3d(3, bottomHeight/2+servoConnectThickness/2, bottomThickness)).
                toCSG().transformed(Transform.unity().translate(-bottomWidth/2, -bottomHeight/4 - servoConnectThickness/4, bottomThickness / 2));

        CSG first = firstA.union(firstB);
        
        CSG second = first.transformed(Transform.unity().rotX(180).translateZ(-bottomThickness));
        
        return first.union(second);
    }

    public CSG toCSG() {
        return pins().union(pinConnections()).union(servoConnect());
    }

    public static void main(String[] args) throws IOException {

        ArduinoMount aMount = new ArduinoMount();

        // save union as stl
//        FileUtil.write(Paths.get("sample.stl"), new ServoHead().servoHeadFemale().transformed(Transform.unity().scale(1.0)).toStlString());
        FileUtil.write(Paths.get("sample.stl"), aMount.toCSG().toStlString());

    }

}
