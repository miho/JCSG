/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.mihosoft.vrl.v3d;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author miho
 */
public class RaspberryArduinoConnector {
    private double arduinoMountingThickness = 2.0;
    private double rspberryMountingThickness = 2.0;
    
    private double boardToBoardSpacing = 30.0;
    
    private double connectorDepth = 8;
    
    public CSG toCSG() {
        
        double th = 2;
        double smh = 7;
        double ath = arduinoMountingThickness;
        double rth = rspberryMountingThickness;
        double b2bs = boardToBoardSpacing;
        
        return Extrude.points(new Vector3d(0,0,connectorDepth),
                new Vector3d(-th,-th),
                new Vector3d(smh,-th),
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
                new Vector3d(smh,ath+th+b2bs+th+rth+th),
                new Vector3d(-th,ath+th+b2bs+th+rth+th)
        );
    }
    
        public static void main(String[] args) throws IOException {

        RaspberryArduinoConnector arConnect = new RaspberryArduinoConnector();

        // save union as stl
//        FileUtil.write(Paths.get("sample.stl"), new ServoHead().servoHeadFemale().transformed(Transform.unity().scale(1.0)).toStlString());
        FileUtil.write(Paths.get("sample.stl"), arConnect.toCSG().toStlString());

    }
}
