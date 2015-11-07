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

// TODO: Auto-generated Javadoc
/**
 * The Class ServoToServoConnector.
 *
 * @author miho
 */
public class ServoToServoConnector {

    /** The servo width. */
    //standard servo
    private double servoWidth = 40.0;
    
    /** The servo thickness. */
    private double servoThickness = 19.0;
    
    /** The border thickness. */
    private double borderThickness = 2;
    
    /** The connector thickness. */
    private double connectorThickness = 4;
    
    /** The servo mount height. */
    private double servoMountHeight = 10;
    
    /** The servo distance. */
    private double servoDistance = 17;
    
    /** The height. */
    private double height=12;
    

    /**
     * To csg.
     *
     * @return the csg
     */
    public CSG toCSG() {
        
        double sth = servoThickness;
        double sd = servoDistance;
        double th = borderThickness;
        double th2 = connectorThickness;
        
        double h = height;
        
        CSG fork = Extrude.points(new Vector3d(0, 0,servoMountHeight),
                new Vector3d(0,0),
                new Vector3d(sth,0),
                new Vector3d(sth,h),
                new Vector3d(sth+th,h),
                new Vector3d(sth+th,-th),
                new Vector3d(sth/2+th2/2,-th),
                new Vector3d(sth/2+th2/4,-th-sd/2),
                new Vector3d(sth/2-th2/4,-th-sd/2),
                new Vector3d(sth/2-th2/2,-th),
                new Vector3d(-th,-th),
                new Vector3d(-th,h),
                new Vector3d(0,h)
        );
        
        CSG fork2 = fork.transformed(Transform.unity().rotZ(180).translateX(-sth).translateY(sd+th*2));
        
        return fork.union(fork2);
    }

    /**
     * The main method.
     *
     * @param args the arguments
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void main(String[] args) throws IOException {

        ServoToServoConnector servo2ServoConnector = new ServoToServoConnector();

        // save union as stl
//        FileUtil.write(Paths.get("sample.stl"), new ServoHead().servoHeadFemale().transformed(Transform.unity().scale(1.0)).toStlString());
        FileUtil.write(Paths.get("sample.stl"), servo2ServoConnector.toCSG().toStlString());

    }
}
