/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d.samples;

import eu.mihosoft.vrl.v3d.Bounds;
import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.FileUtil;
import eu.mihosoft.vrl.v3d.STL;
import eu.mihosoft.vrl.v3d.Transform;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author miho
 */
public class DualMaterialRobot {

    private static CSG robot;

    public static CSG middleToCSG() throws IOException {

        if (robot == null) {
            robot = STL.file(java.nio.file.Paths.get("/home/miho/CuraExamples/UltimakerRobot_support.stl"));
        }

//        return robot.getBounds().toCSG().transformed(Transform.unity().scale(1.1,0.5,1.1).translateY(10).translateZ(-1.5)).difference(robot);
        Bounds robotBounds = robot.getBounds();

        CSG middle = robotBounds.toCSG().transformed(Transform.unity().scaleZ(1 / 2.0).translateZ(robotBounds.getBounds().z/3.0));

        return robot.intersect(middle);
    }

    public static CSG topBottomCSG() throws IOException {

        if (robot == null) {
            robot = STL.file(java.nio.file.Paths.get("/home/miho/CuraExamples/UltimakerRobot_support.stl"));
        }

        Bounds robotBounds = robot.getBounds();

        CSG middle = robotBounds.toCSG().transformed(Transform.unity().scaleZ(1 / 2.0).translateZ(robotBounds.getBounds().z/3.0));

        return robot.difference(middle);

    }

    public static void main(String[] args) throws IOException {

//        FileUtil.write(Paths.get("robot-color-1.stl"), DualMaterialRobot.topBottomCSG().toStlString());
//        FileUtil.write(Paths.get("robot-color-2.stl"), DualMaterialRobot.middleToCSG().toStlString());
        
        robot = STL.file(java.nio.file.Paths.get("/home/miho/CuraExamples/UltimakerRobot_support.stl"));
        
        
        FileUtil.write(Paths.get("robot-ascii.stl"), robot.toStlString());

    }
}
