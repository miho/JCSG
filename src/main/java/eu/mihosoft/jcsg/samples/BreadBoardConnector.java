/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg.samples;

import eu.mihosoft.jcsg.CSG;
import eu.mihosoft.jcsg.Extrude;
import eu.mihosoft.jcsg.FileUtil;
import eu.mihosoft.vvecmath.Plane;
import eu.mihosoft.vvecmath.Transform;
import eu.mihosoft.vvecmath.Vector3d;
import java.io.IOException;
import java.nio.file.Paths;

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

        return Extrude.points(Vector3d.xyz(0, 0, connectorDepth),
                Vector3d.xy(-th, -th),
                Vector3d.xy(smh + pth + ph, -th),
                Vector3d.xy(smh + pth + Math.max(ph / 3, 0.4), 0 + po),
                Vector3d.xy(smh + pth, 0 + po),
                Vector3d.xy(smh, 0),
                Vector3d.xy(0, 0),
                Vector3d.xy(0, bmth),
                Vector3d.xy(smh, bmth),
                Vector3d.xy(smh, bmth + th),
                Vector3d.xy(0, bmth + th),
                Vector3d.xy(0, bmth +bbpbd-th),//1
                Vector3d.xy(smh, bmth +bbpbd-th), // 2
                Vector3d.xy(smh, bmth + th + bbpbd - th), // 3
                Vector3d.xy(0, bmth + th + bbpbd - th), // 4
                Vector3d.xy(0, bmth + th + bbpbd + bbth), // 5
                Vector3d.xy(smh, bmth + th +bbpbd + bbth), // 6
                Vector3d.xy(smh, bmth + th +bbpbd + bbth + th), // 7
                Vector3d.xy(0, bmth + th +bbpbd + bbth + th), // 8
                Vector3d.xy(-th, bmth + th +bbpbd + bbth + th) // 9
        );

    }

    public static void main(String[] args) throws IOException {

        BreadBoardConnector arConnect = new BreadBoardConnector();

        // save union as stl
        FileUtil.write(Paths.get("bread-board-connector-tmp.stl"), arConnect.toCSG().transformed(Transform.unity().mirror(Plane.XY_PLANE).rotY(180)).toStlString());

    }
}
