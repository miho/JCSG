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

// TODO: Auto-generated Javadoc
/**
 * The Class BreadBoardMount.
 *
 * @author miho
 */
public class BreadBoardMount {
    
    /** The bread board width. */
    private double breadBoardWidth = 84;
    
    /** The breadboard length. */
    private double breadboardLength = 54;
    
    /** The bread board height. */
    private double breadBoardHeight = 8.5;
    
    /** The pin radius. */
    private double pinRadius = 5;
    
    /** The pin height. */
    private double pinHeight = 14;
    
    /** The pin hole height. */
    private double pinHoleHeight = 10;
    
    /** The bottom thickness. */
    private double bottomThickness = 2;
    
    /** The servo connect thickness. */
    private double servoConnectThickness = 7;
    
    /**
     * Board.
     *
     * @return the csg
     */
    private CSG board() {
        return new Cube(Vector3d.ZERO, new Vector3d(breadBoardWidth, breadboardLength, breadBoardHeight)).toCSG();
    }

    /**
     * Pins.
     *
     * @return the csg
     */
    private CSG pins() {
        CSG prototype = new Cylinder(pinRadius, pinHeight, 16).toCSG();

        CSG first = prototype.clone().transformed(Transform.unity().translate(breadBoardWidth / 2.0, breadBoardHeight / 2.0, 0));
        CSG second = prototype.clone().transformed(Transform.unity().translate(breadBoardWidth / 2.0, -breadBoardHeight / 2.0, 0));
        CSG third = prototype.clone().transformed(Transform.unity().translate(-breadBoardWidth / 2.0-1, 0, 0));

        CSG pins = first.union(second).union(third);

        CSG board = board().transformed(Transform.unity().translateZ(pinHoleHeight*2));

        return pins.difference(board);
    }

    /**
     * Pin connections.
     *
     * @return the csg
     */
    private CSG pinConnections() {

        CSG first = new Cube(Vector3d.ZERO, new Vector3d(breadBoardWidth / 2, 3, bottomThickness)).
                toCSG().transformed(Transform.unity().translate(-breadBoardWidth / 4, 0, bottomThickness / 2));
        CSG second = new Cube(Vector3d.ZERO, new Vector3d(breadBoardWidth / 2 + 10, 3, bottomThickness)).
                toCSG().transformed(Transform.unity().rotZ(37.8).translate(breadBoardWidth / 4 + 5, 0, bottomThickness / 2));
        CSG third = new Cube(Vector3d.ZERO, new Vector3d(breadBoardWidth / 2 + 10, 3, bottomThickness)).
                toCSG().transformed(Transform.unity().rotZ(-37.8).translate(breadBoardWidth / 4 + 5, 0, bottomThickness / 2));

        return first.union(second).union(third);
    }
    
    /**
     * Servo connect.
     *
     * @return the csg
     */
    private CSG servoConnect() {

        CSG firstA = new Cube(Vector3d.ZERO, new Vector3d(breadBoardWidth, servoConnectThickness, bottomThickness)).
                toCSG().transformed(Transform.unity().translate(0, -breadBoardHeight/2, bottomThickness / 2));
       
        CSG firstB = new Cube(Vector3d.ZERO, new Vector3d(3, breadBoardHeight/2+servoConnectThickness/2, bottomThickness)).
                toCSG().transformed(Transform.unity().translate(-breadBoardWidth/2, -breadBoardHeight/4 - servoConnectThickness/4, bottomThickness / 2));

        CSG first = firstA.union(firstB);
        
        CSG second = first.transformed(Transform.unity().rotX(180).translateZ(-bottomThickness));
        
        return first.union(second);
    }

    /**
     * To csg.
     *
     * @return the csg
     */
    public CSG toCSG() {
        return pins().union(pinConnections()).union(servoConnect());
    }

    /**
     * The main method.
     *
     * @param args the arguments
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void main(String[] args) throws IOException {

        ArduinoMount aMount = new ArduinoMount();

        // save union as stl
//        FileUtil.write(Paths.get("sample.stl"), new ServoHead().servoHeadFemale().transformed(Transform.unity().scale(1.0)).toStlString());
        FileUtil.write(Paths.get("bread-board-mount.stl"), aMount.toCSG().toStlString());

    }
}
