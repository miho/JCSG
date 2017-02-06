/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg.samples;

import eu.mihosoft.jcsg.CSG;
import eu.mihosoft.jcsg.Cylinder;
import eu.mihosoft.jcsg.FileUtil;
import eu.mihosoft.jcsg.Sphere;
import eu.mihosoft.vvecmath.Transform;
import eu.mihosoft.vvecmath.Vector3d;
import java.io.IOException;
import java.nio.file.Paths;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class FractalStructureBeam2D {

    public static void main(String[] args) throws IOException {

        CSG result = new FractalStructureBeam2D().toCSG();
        
        result = result.union(new Sphere(Vector3d.ZERO, 1, 4, 4).toCSG());

        FileUtil.write(Paths.get("fractal-structure-beam-2d.stl"), result.toStlString());

        result.toObj().toFiles(Paths.get("fractal-structure-beam-2d.stl"));

    }

    private CSG toCSG() {
        return createBeam(5, Vector3d.xy(0, 0), Vector3d.xy(10, 0), 2);
    }

    private CSG createBeam(double b, Vector3d start, Vector3d stop, int i) {
        if (i == 0) {
            return createBeamTerminal(b, start, stop);
        }

        double l = stop.minus(start).magnitude();
        double a = stop.y() - start.y();
        double c = l;

        double alpha = Math.asin((a / c)) * 180.0 / Math.PI;

        System.out.println("level: "+ i+" alpha: " + alpha + " : " + start + " : " + stop + " : l(c) = " + l + " : a = " + a);

        Transform localToGlobalTransform = Transform.unity().rotZ(-alpha).translate(start);

        double nextB = b / 5.0;

        Vector3d innerStart = Vector3d.ZERO;
        Vector3d innerStop = Vector3d.xy(l, 0);

        Vector3d incVec = Vector3d.xy(0, b / 2.0 - nextB / 2.0);

        Vector3d mainBeamStartUpper = innerStart.plus(incVec);
        Vector3d mainBeamStartLower = innerStart.minus(incVec);
        Vector3d mainBeamStopUpper = innerStop.plus(incVec);
        Vector3d mainBeamStopLower = innerStop.minus(incVec);

        CSG upperMainBeam = createBeam(nextB, mainBeamStartUpper, mainBeamStopUpper, i - 1);
        CSG lowerMainBeam = createBeam(nextB, mainBeamStartLower, mainBeamStopLower, i - 1);

        CSG mainBeams = upperMainBeam.union(lowerMainBeam);

        boolean switchDir = false;

        CSG innerBeams = null;

        Vector3d startMinorBeam = mainBeamStartLower;//Vector3d.xy(0, -b / 2.0);
        Vector3d stopMinorBeam = Vector3d.xy(b, 0).plus(incVec);//Vector3d.xy(b, b / 2.0);
        
        
        CSG startMinor = new Sphere(startMinorBeam, 0.5, 4, 4).toCSG();
        CSG  stopMinor = new Sphere( stopMinorBeam, 0.5, 4, 4).toCSG();

        innerBeams = startMinor.union(stopMinor);
        
        int counter = 0;

        /*while (stopMinorBeam.x < innerStop.x) {
        
        stopMinorBeam = Vector3d.xy((counter + 1) * b, !switchDir ? b / 2.0 : -b / 2.0);
        
        counter++;
        switchDir = !switchDir;
        
        //if (i == 2) {
        //   System.out.println("level: " + i + " counter: " + counter + " : " + startMinorBeam + " : " + stopMinorBeam);
        //}
        
        CSG innerB = createBeam(nextB, startMinorBeam, stopMinorBeam, i - 1);
        
        if (innerBeams == null) {
        innerBeams = innerB;
        } else {
        innerBeams = innerBeams.union(innerB);
        }
        
        startMinorBeam = stopMinorBeam.clone();
        }*/

//        if (innerBeams != null) {
        return mainBeams.union(innerBeams).transformed(localToGlobalTransform);
//        } else {
//            return mainBeams.transformed(localToGlobalTransform);
//        }
    }

    private CSG createBeamTerminal(double b, Vector3d start, Vector3d stop) {
        return new Cylinder(start, stop, b / 2.0, 4).toCSG();
    }
}
