/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg.samples;

import eu.mihosoft.jcsg.Cube;
import eu.mihosoft.jcsg.CSG;
import eu.mihosoft.jcsg.Extrude;
import eu.mihosoft.jcsg.FileUtil;
import eu.mihosoft.vvecmath.Transform;
import eu.mihosoft.vvecmath.Vector3d;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author miho
 */
public class ServoMountPixy {

    // mini servo
//    private double servoWidth = 22.9;
//    private double servoThickness = 12.0;
//    
    //standard servo
    private double servoWidth = 40.5;
    private double servoThickness = 19.0;
    private double borderThickness = 3.0;
    private double overlap = 3;
    private double servoMountHeight = 20;

    private double boardMountingThickness = 3;
    private double boardHolder1Length = 12;
    private double boardHolder2Length = 16;

    private double boardMountingWidth = 8.1;

    private double pegHeight = 1;
    private double pegToothHeight = 0.6;
    private double pegOverlap = 0.5;

    public CSG toCSGSimple() {

        return Extrude.points(Vector3d.xyz(0, 0, servoMountHeight),
                Vector3d.xy(0, servoThickness),
                Vector3d.xy(overlap, servoThickness),
                Vector3d.xy(-borderThickness, servoThickness + borderThickness),
                Vector3d.xy(-borderThickness, -borderThickness),
                Vector3d.xy(servoWidth + borderThickness, -borderThickness),
                Vector3d.xy(servoWidth + borderThickness, servoThickness + borderThickness),
                Vector3d.xy(servoWidth - overlap, servoThickness),
                Vector3d.xy(servoWidth, servoThickness),
                Vector3d.xy(servoWidth, 0),
                Vector3d.xy(0, 0)
        );
    }

    public CSG toCSG() {
        Transform bm1Transform = Transform.unity().rotY(90).rotZ(90).translate(borderThickness, borderThickness, -boardHolder1Length + borderThickness);
        CSG bm1 = boardMount1().transformed(bm1Transform);
        Transform bm2Transform = Transform.unity().translateX(servoWidth - boardHolder1Length + borderThickness * 2);
        CSG bm2 = boardMountWithPixy().transformed(bm1Transform).transformed(bm2Transform);
        CSG sm = toCSGSimple();

        return sm.union(bm1).union(bm2);//.transformed(Transform.unity().scale(0.08));
    }

    private CSG boardMount1() {
        return boardMount(boardHolder1Length);
    }

    private CSG boardMount2() {
        return boardMount(boardHolder2Length);
    }

    private CSG boardMountWithPixy() {
        return boardMount2().union(pixyMount());
    }

    private CSG pixyMount() {

        double pixyBoardThickness = 2;

        double camHeight = 60;
        double camHolderHeight = 20;
//        
//        double outerThickness = boardHolder2Length*0.5-boardHolder2Length*0.5;
//        double innerThickness = pixyBoardThickness;
//        return pixyMountBase(
//                outerThickness).
//                union(pixyMountBase(innerThickness).transformed(Transform.unity().translateZ(outerThickness)));

        CSG pixyBoard = new Cube(50, 50, pixyBoardThickness).toCSG();

        pixyBoard = pixyBoard.transformed(
                Transform.unity().translate(
                        camHeight + camHolderHeight + boardMountingThickness + borderThickness * 4,
                        pixyBoard.getBounds().getBounds().y() * 0.5, boardHolder2Length * 0.5));

        return pixyMountBase().difference(pixyBoard);
    }

    private CSG pixyMountBase() {

        double h = boardMountingWidth;

        double camHeight = 60;
        double camHolderHeight = 20;
        double camWidth = 53;
        double outerPiMountWidth = 60;
        double camOverlap = 10;
        double upperCamOverlap = 3;
        double camHolderWidth = 10;

        double breadBoardHeight = 26;
        double breadBoardThickness = 9;
        
        double breadBoardOverlap = 14.5;
        
        double bottomThickness = 3;

        List<Vector3d> points = Arrays.asList(
                Vector3d.xy(boardMountingThickness + borderThickness, -borderThickness),
                Vector3d.xy(boardMountingThickness + borderThickness + camHeight + camHolderHeight, -borderThickness),
                Vector3d.xy(boardMountingThickness + borderThickness + camHeight + camHolderHeight, 0 + upperCamOverlap),
                Vector3d.xy(boardMountingThickness + borderThickness + camHeight, (outerPiMountWidth - camWidth) + camOverlap),
                Vector3d.xy(boardMountingThickness + borderThickness + camHeight - borderThickness, (outerPiMountWidth - camWidth) + camOverlap),
                Vector3d.xy(boardMountingThickness + borderThickness + camHeight - borderThickness - camHolderWidth, 0),
                // -> (breadboard)
                Vector3d.xy(boardMountingThickness + borderThickness + breadBoardHeight + breadBoardThickness + borderThickness + bottomThickness, 0),
                Vector3d.xy(boardMountingThickness + borderThickness + breadBoardHeight + breadBoardThickness + borderThickness, breadBoardOverlap),
                Vector3d.xy(boardMountingThickness + borderThickness + breadBoardHeight + breadBoardThickness, breadBoardOverlap),
                Vector3d.xy(boardMountingThickness + borderThickness + breadBoardHeight + breadBoardThickness, 0),
                Vector3d.xy(boardMountingThickness + borderThickness + breadBoardHeight , 0),
                Vector3d.xy(boardMountingThickness + borderThickness + breadBoardHeight , breadBoardOverlap),
                Vector3d.xy(boardMountingThickness + borderThickness + breadBoardHeight - borderThickness, breadBoardOverlap),
                Vector3d.xy(boardMountingThickness + borderThickness + breadBoardHeight - borderThickness - bottomThickness, 0),
                // <-
                Vector3d.xy(boardMountingThickness - pegOverlap + borderThickness, 0),
                Vector3d.xy(boardMountingThickness, h),
                Vector3d.xy(boardMountingThickness, 0)
        );

        Collections.reverse(points);

        return Extrude.points(Vector3d.xyz(0, 0, boardHolder2Length),
                points
        );
    }

    private CSG boardMount(double boardHolderLength) {
        
        double bottomThickness = 3;

        double h = boardMountingWidth;

        List<Vector3d> points = Arrays.asList(
                Vector3d.ZERO,
                Vector3d.xy(0, -borderThickness),
                Vector3d.xy(boardMountingThickness + borderThickness + bottomThickness, -borderThickness),
                Vector3d.xy(boardMountingThickness + borderThickness, h + pegToothHeight + pegHeight),
                Vector3d.xy(boardMountingThickness - pegOverlap, h + pegToothHeight+pegHeight*0.25),
                Vector3d.xy(boardMountingThickness - pegOverlap, h + pegToothHeight),
                Vector3d.xy(boardMountingThickness, h),
                Vector3d.xy(boardMountingThickness, 0)
        );

        Collections.reverse(points);

        return Extrude.points(Vector3d.xyz(0, 0, boardHolderLength),
                points
        );
    }

    public static void main(String[] args) throws IOException {

        ServoMountPixy sMount = new ServoMountPixy();

        // save union as stl
//        FileUtil.write(Paths.get("sample.stl"), new ServoHead().servoHeadFemale().transformed(Transform.unity().scale(1.0)).toStlString());
        FileUtil.write(Paths.get("servo-mount-pixy.stl"), sMount.toCSG().toStlString());

    }
}
