/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg.samples;

import eu.mihosoft.jcsg.Cube;
import eu.mihosoft.jcsg.Cylinder;
import eu.mihosoft.jcsg.FileUtil;
import eu.mihosoft.vvecmath.Transform;
import eu.mihosoft.jcsg.CSG;

import java.io.IOException;
import java.nio.file.Paths;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class QuadrocopterArmHolder {

    public CSG toCSG(double armHeight, double armScaleFactor, double armCubeWidth, double armCubeThickness, double holderPlatformRadius, double holderPlatformThickness) {

        double widthTol = 2;
        double thicknessTol = 0.1;
        double holderWallThickness = 3;
        double armOverlap = 15;
        
        double holderTopRailWidth = 2;
        double holderTopRailSpacing = 2;
        
        armCubeThickness = armCubeThickness+thicknessTol;

        double holderCubeWidth = widthTol + holderWallThickness * 2 + armCubeWidth;
        double holderCubeHeight = armHeight + holderWallThickness;
        double holderCubeDepth = armOverlap + armCubeThickness + holderWallThickness;

        CSG holderCube = new Cube(holderCubeWidth, holderCubeDepth, holderCubeHeight).toCSG();

        double armWidth = armHeight * armScaleFactor;

        CSG armCube = new Cube(armCubeWidth + widthTol, armCubeThickness, armHeight).
                toCSG().transformed(Transform.unity().translateY(-armCubeThickness/2.0-armOverlap/2.0+holderWallThickness));
        CSG arm = new Cube(armWidth, holderCubeDepth, armHeight).toCSG().
                transformed(Transform.unity().translateZ(armHeight / 2.0));
        arm = new Cylinder(armHeight / 2.0, holderCubeDepth, 32).
                toCSG().transformed(Transform.unity().rotX(90).
                        translate(0, 0, -holderCubeDepth/2.0).scaleX(armScaleFactor)).union(arm);
        CSG holder = holderCube.difference(armCube.union(arm).
                transformed(Transform.unity().translate(0, 0, 0.5*holderWallThickness))).transformed(Transform.unity().translateY(-holderCubeDepth/2.0));
        
        CSG holderTopRail = new Cylinder(holderTopRailWidth/2.0, holderCubeDepth, 6).toCSG().
                transformed(Transform.unity().translate(-holderCubeWidth/2.0,-holderCubeDepth, -holderTopRailWidth/2.0 + holderCubeHeight/2.0 - holderTopRailSpacing).rotX(90).rotZ(30));
        
        holderTopRail = holderTopRail.union(holderTopRail.transformed(Transform.unity().translateX(holderCubeWidth)));
        
        holder = holder.difference(holderTopRail);

//        return holder;

        CSG holderPlatform = new Cylinder(holderPlatformRadius, holderPlatformThickness, 64).toCSG().transformed(Transform.unity().scaleY(1.15).translateY(-holderPlatformRadius*0.75));
      
        return holderPlatform.union(holder.transformed(Transform.unity().translateZ(4*holderWallThickness))).transformed(Transform.unity().rotZ(-90));
    }

    public static void main(String[] args) throws IOException {
        CSG result = new QuadrocopterArmHolder().toCSG(18, 0.5, 18, 4, 20, 3);

        FileUtil.write(Paths.get("quadrocopter-arm-holder.stl"), result.toStlString());
        result.toObj().toFiles(Paths.get("quadrocopter-arm-holder.obj"));
    }
}
