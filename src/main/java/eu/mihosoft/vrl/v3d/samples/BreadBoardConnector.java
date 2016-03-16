/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d.samples;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Extrude;
import eu.mihosoft.vrl.v3d.FileUtil;
import eu.mihosoft.vrl.v3d.Plane;
import eu.mihosoft.vrl.v3d.Transform;
import eu.mihosoft.vrl.v3d.Vector3d;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author miho
 */
public class BreadBoardConnector {

    private double boardMountingThickness = 2.0;

    private double breadBoardThickness = 9;

    private double connectorDepth = 30;

    private double pegHeight = 1;
    private double pegToothHeight = 0.3;
    private double pegOverlap = 0.6;

    private double boardMountingWidth = 8.1;

    private double breadBoardToPiMountDistance = 26;

//    private double breadBoardMountLength = 20;
    public CSG toCSG() {

        double th = 2;
        double smh = boardMountingWidth;
        double bmth = boardMountingThickness;
        double bbpbd = breadBoardToPiMountDistance ;
        double bbth = breadBoardThickness - th;

        double pth = pegToothHeight;
        double ph = pegHeight;
        double po = pegOverlap;

        return Extrude.points(new Vector3d(0, 0, connectorDepth),
                new Vector3d(-th, -th),
                new Vector3d(smh + pth + ph, -th),
                new Vector3d(smh + pth + Math.max(ph / 3, 0.4), 0 + po),
                new Vector3d(smh + pth, 0 + po),
                new Vector3d(smh, 0),
                new Vector3d(0, 0),
                new Vector3d(0, bmth),
                new Vector3d(smh, bmth),
                new Vector3d(smh, bmth + th),
                new Vector3d(0, bmth + th),
                new Vector3d(0, bmth +bbpbd-th),//1
                new Vector3d(smh, bmth +bbpbd-th), // 2
                new Vector3d(smh, bmth + th + bbpbd - th), // 3
                new Vector3d(0, bmth + th + bbpbd - th), // 4
                new Vector3d(0, bmth + th + bbpbd + bbth), // 5
                new Vector3d(smh, bmth + th +bbpbd + bbth), // 6
                new Vector3d(smh, bmth + th +bbpbd + bbth + th), // 7
                new Vector3d(0, bmth + th +bbpbd + bbth + th), // 8
                new Vector3d(-th, bmth + th +bbpbd + bbth + th) // 9
        );

    }

    public static void main(String[] args) throws IOException {

        BreadBoardConnector arConnect = new BreadBoardConnector();

        // save union as stl
        FileUtil.write(Paths.get("bread-board-connector-tmp.stl"), arConnect.toCSG().transformed(Transform.unity().mirror(Plane.XY_PLANE).rotY(180)).toStlString());

    }
}
