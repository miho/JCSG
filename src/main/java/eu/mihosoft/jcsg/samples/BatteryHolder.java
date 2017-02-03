/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg.samples;

import eu.mihosoft.jcsg.Extrude;
import eu.mihosoft.jcsg.FileUtil;
import eu.mihosoft.jcsg.CSG;
import eu.mihosoft.vvecmath.Vector3d;
import java.io.IOException;
import java.nio.file.Paths;

/**
 *
 * @author miho
 */
public class BatteryHolder {

    private double mountingThickness = 3.0;

    private double boardToBoardSpacing = 30.0;

    private double connectorDepth = 25;

    private double pegHeight = 1;
    private double pegToothHeight = 0.3;
    private double pegOverlap = 0.6;

    private double boardMountingWidth = 8.11;

    private double batteryHeight = 22;
    private double batteryLength = 54;

    private double footHeight = 25;
    private double footSize = 10;


    public CSG toCSG() {

        double th = 3;
        double smh = boardMountingWidth;
        double mth = mountingThickness;

        double pth = pegToothHeight;
        double ph = pegHeight;
        double po = pegOverlap;
        
        double o = 13;

        return Extrude.points(Vector3d.xyz(0, 0, connectorDepth),
                Vector3d.xy(-th, -th),
                Vector3d.xy(smh + pth + ph+o, -th),
                Vector3d.xy(smh + pth + Math.max(ph / 3, 0.4)+o, 0 + po),
                Vector3d.xy(smh + pth+o, 0 + po),
                Vector3d.xy(smh+o, 0),
                Vector3d.xy(0+o, 0),
                Vector3d.xy(0+o, mth),
                Vector3d.xy(smh+o, mth),
                Vector3d.xy(smh+o, mth + th),
                Vector3d.xy(0, mth + th),
                Vector3d.xy(0, mth + th + batteryHeight),
                Vector3d.xy(batteryLength, mth + th + batteryHeight),
                Vector3d.xy(batteryLength, mth + th + batteryHeight * 0.3),
                Vector3d.xy(batteryLength + th, mth + th + batteryHeight * 0.3),
                Vector3d.xy(batteryLength + th, mth + th + batteryHeight + th),
                Vector3d.xy(0, mth + th + batteryHeight + th),
                Vector3d.xy(0, mth + th + batteryHeight + th + footHeight - th * 2),
                Vector3d.xy(footSize, mth + th + batteryHeight + th + footHeight - th),
                Vector3d.xy(footSize, mth + th + batteryHeight + th + footHeight),
                Vector3d.xy(-th, mth + th + batteryHeight + th + footHeight)
        );
    }

    public static void main(String[] args) throws IOException {

        BatteryHolder arConnect = new BatteryHolder();

        // save union as stl
//        FileUtil.write(Paths.get("sample.stl"), new ServoHead().servoHeadFemale().transformed(Transform.unity().scale(1.0)).toStlString());
        FileUtil.write(Paths.get("battery-holder.stl"), arConnect.toCSG().toStlString());

    }
}
