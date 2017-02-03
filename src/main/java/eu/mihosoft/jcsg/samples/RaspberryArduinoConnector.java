/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.mihosoft.jcsg.samples;

import eu.mihosoft.jcsg.CSG;
import eu.mihosoft.jcsg.Extrude;
import eu.mihosoft.jcsg.FileUtil;
import eu.mihosoft.vvecmath.Vector3d;

import java.io.IOException;
import java.nio.file.Paths;

/**
 *
 * @author miho
 */
public class RaspberryArduinoConnector {
    private double arduinoMountingThickness = 2.0;
    private double rspberryMountingThickness = 2.0;
    
    private double boardToBoardSpacing = 30.0;
    
    private double connectorDepth = 8;
    
    private double pegHeight= 1;
    private double pegToothHeight = 0.3;
    private double pegOverlap = 0.6;
    
    private double boardMountingWidth = 8;
    
    public CSG toCSG() {
        
        double th = 2;
        double smh = boardMountingWidth;
        double ath = arduinoMountingThickness;
        double rth = rspberryMountingThickness;
        double b2bs = boardToBoardSpacing;
        
        double pth = pegToothHeight;
        double ph = pegHeight;
        double po = pegOverlap;
        
        return Extrude.points(Vector3d.xyz(0,0,connectorDepth),
                Vector3d.xy(-th,-th),
                Vector3d.xy(smh + pth+ph,-th),
                Vector3d.xy(smh + pth+Math.max(ph/3,0.4),0 + po),
                Vector3d.xy(smh + pth,0 + po),
                Vector3d.xy(smh,0),
                Vector3d.xy(0,0),
                Vector3d.xy(0,ath),
                Vector3d.xy(smh,ath),
                Vector3d.xy(smh,ath+th),
                Vector3d.xy(0,ath+th),
                Vector3d.xy(0,ath+th+b2bs),
                Vector3d.xy(smh,ath+th+b2bs),
                Vector3d.xy(smh,ath+th+b2bs+th),
                Vector3d.xy(0,ath+th+b2bs+th),
                Vector3d.xy(0,ath+th+b2bs+th+rth),
                Vector3d.xy(smh,ath+th+b2bs+th+rth),
                Vector3d.xy(smh + pth,ath+th+b2bs+th+rth - po),
                Vector3d.xy(smh + pth+Math.max(ph/3,0.4), ath + th + b2bs + th + rth - po),
                Vector3d.xy(smh + pth+ph,ath+th+b2bs+th+rth+th),
                Vector3d.xy(-th,ath+th+b2bs+th+rth+th)
        );
    }
    
        public static void main(String[] args) throws IOException {

        RaspberryArduinoConnector arConnect = new RaspberryArduinoConnector();

        // save union as stl
//        FileUtil.write(Paths.get("sample.stl"), new ServoHead().servoHeadFemale().transformed(Transform.unity().scale(1.0)).toStlString());
        FileUtil.write(Paths.get("pi-arduino-connector.stl"), arConnect.toCSG().toStlString());

    }
}
