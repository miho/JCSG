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
 * The Class BatteryHolder.
 *
 * @author miho
 */
public class BatteryHolder {

    /** The mounting thickness. */
    private double mountingThickness = 2.0;

    /** The board to board spacing. */
    private double boardToBoardSpacing = 30.0;

    /** The connector depth. */
    private double connectorDepth = 15;

    /** The peg height. */
    private double pegHeight = 1;
    
    /** The peg tooth height. */
    private double pegToothHeight = 0.3;
    
    /** The peg overlap. */
    private double pegOverlap = 0.6;

    /** The board mounting width. */
    private double boardMountingWidth = 8.11;

    /** The battery height. */
    private double batteryHeight = 22;
    
    /** The battery length. */
    private double batteryLength = 54;

    /** The foot height. */
    private double footHeight = 25;
    
    /** The foot size. */
    private double footSize = 10;


    /**
     * To csg.
     *
     * @return the csg
     */
    public CSG toCSG() {

        double th = 2;
        double smh = boardMountingWidth;
        double mth = mountingThickness;

        double pth = pegToothHeight;
        double ph = pegHeight;
        double po = pegOverlap;
        
        double o = 13;

        return Extrude.points(new Vector3d(0, 0, connectorDepth),
                new Vector3d(-th, -th),
                new Vector3d(smh + pth + ph+o, -th),
                new Vector3d(smh + pth + Math.max(ph / 3, 0.4)+o, 0 + po),
                new Vector3d(smh + pth+o, 0 + po),
                new Vector3d(smh+o, 0),
                new Vector3d(0+o, 0),
                new Vector3d(0+o, mth),
                new Vector3d(smh+o, mth),
                new Vector3d(smh+o, mth + th),
                new Vector3d(0, mth + th),
                new Vector3d(0, mth + th + batteryHeight),
                new Vector3d(batteryLength, mth + th + batteryHeight),
                new Vector3d(batteryLength, mth + th + batteryHeight * 0.3),
                new Vector3d(batteryLength + th, mth + th + batteryHeight * 0.3),
                new Vector3d(batteryLength + th, mth + th + batteryHeight + th),
                new Vector3d(0, mth + th + batteryHeight + th),
                new Vector3d(0, mth + th + batteryHeight + th + footHeight - th * 2),
                new Vector3d(footSize, mth + th + batteryHeight + th + footHeight - th),
                new Vector3d(footSize, mth + th + batteryHeight + th + footHeight),
                new Vector3d(-th, mth + th + batteryHeight + th + footHeight)
        );
    }

    /**
     * The main method.
     *
     * @param args the arguments
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void main(String[] args) throws IOException {

        BatteryHolder arConnect = new BatteryHolder();

        // save union as stl
//        FileUtil.write(Paths.get("sample.stl"), new ServoHead().servoHeadFemale().transformed(Transform.unity().scale(1.0)).toStlString());
        FileUtil.write(Paths.get("battery-holder.stl"), arConnect.toCSG().toStlString());

    }
}
