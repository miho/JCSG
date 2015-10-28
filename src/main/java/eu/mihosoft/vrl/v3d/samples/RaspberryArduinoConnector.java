/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.mihosoft.vrl.v3d.samples;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Extrude;
import eu.mihosoft.vrl.v3d.FileUtil;
import eu.mihosoft.vrl.v3d.Vector3d;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class RaspberryArduinoConnector.
 *
 * @author miho
 */
public class RaspberryArduinoConnector {
    
    /** The arduino mounting thickness. */
    private double arduinoMountingThickness = 2.0;
    
    /** The rspberry mounting thickness. */
    private double rspberryMountingThickness = 2.0;
    
    /** The board to board spacing. */
    private double boardToBoardSpacing = 30.0;
    
    /** The connector depth. */
    private double connectorDepth = 8;
    
    /** The peg height. */
    private double pegHeight= 1;
    
    /** The peg tooth height. */
    private double pegToothHeight = 0.3;
    
    /** The peg overlap. */
    private double pegOverlap = 0.6;
    
    /** The board mounting width. */
    private double boardMountingWidth = 8;
    
    /**
     * To csg.
     *
     * @return the csg
     */
    public CSG toCSG() {
        
        double th = 2;
        double smh = boardMountingWidth;
        double ath = arduinoMountingThickness;
        double rth = rspberryMountingThickness;
        double b2bs = boardToBoardSpacing;
        
        double pth = pegToothHeight;
        double ph = pegHeight;
        double po = pegOverlap;
        
        return Extrude.points(new Vector3d(0,0,connectorDepth),
                new Vector3d(-th,-th),
                new Vector3d(smh + pth+ph,-th),
                new Vector3d(smh + pth+Math.max(ph/3,0.4),0 + po),
                new Vector3d(smh + pth,0 + po),
                new Vector3d(smh,0),
                new Vector3d(0,0),
                new Vector3d(0,ath),
                new Vector3d(smh,ath),
                new Vector3d(smh,ath+th),
                new Vector3d(0,ath+th),
                new Vector3d(0,ath+th+b2bs),
                new Vector3d(smh,ath+th+b2bs),
                new Vector3d(smh,ath+th+b2bs+th),
                new Vector3d(0,ath+th+b2bs+th),
                new Vector3d(0,ath+th+b2bs+th+rth),
                new Vector3d(smh,ath+th+b2bs+th+rth),
                new Vector3d(smh + pth,ath+th+b2bs+th+rth - po),
                new Vector3d(smh + pth+Math.max(ph/3,0.4), ath + th + b2bs + th + rth - po),
                new Vector3d(smh + pth+ph,ath+th+b2bs+th+rth+th),
                new Vector3d(-th,ath+th+b2bs+th+rth+th)
        );
    }
    
        /**
         * The main method.
         *
         * @param args the arguments
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public static void main(String[] args) throws IOException {

        RaspberryArduinoConnector arConnect = new RaspberryArduinoConnector();

        // save union as stl
//        FileUtil.write(Paths.get("sample.stl"), new ServoHead().servoHeadFemale().transformed(Transform.unity().scale(1.0)).toStlString());
        FileUtil.write(Paths.get("pi-arduino-connector.stl"), arConnect.toCSG().toStlString());

    }
}
