/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d.samples;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Cube;
import eu.mihosoft.vrl.v3d.Cylinder;
import eu.mihosoft.vrl.v3d.FileUtil;
import eu.mihosoft.vrl.v3d.Transform;
import static eu.mihosoft.vrl.v3d.Transform.*;
import java.io.IOException;
import java.nio.file.Paths;

// TODO: Auto-generated Javadoc
/**
 * The Class MoebiusStairs.
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class MoebiusStairs {

    /** The n. */
    private double n = 45;
    
    /** The twists. */
    private double twists = 1;
    
    /** The tilt. */
    private double tilt = 0;

    /**
     * Resolution.
     *
     * @param n the n
     * @return the moebius stairs
     */
    public MoebiusStairs resolution(double n) {
        this.n = n;
        return this;
    }

    /**
     * Twists.
     *
     * @param twists the twists
     * @return the moebius stairs
     */
    public MoebiusStairs twists(double twists) {
        this.twists = twists;
        return this;
    }

    /**
     * To csg.
     *
     * @return the csg
     */
    public CSG toCSG() {

//        CSG.setDefaultOptType(CSG.OptType.POLYGON_BOUND);
        CSG result = null;

        CSG firstCube = null;
        CSG prevCube = null;

        for (int i = 1; i <= n * 2; i++) {

            double deg = i * 360.0 / n;

            Transform rot1 = unity().rotZ(deg);

            Transform translate1 = unity().translate(
                    -20 + 5 * sin(i * 360.0 * (twists + .5) / n),
                    0,
                    8 * cos(i * 360 * (twists + .5) / n));

            Transform rot2 = unity().rotX(90 - tilt);

            Transform finalTransform = rot1.apply(translate1).apply(rot2);

            CSG cube = new Cube(
                    3 + abs(8.0 * cos(30 + (twists + 0.5) * deg)),
                    4,
                    4).toCSG();

//            CSG cube = new Cylinder(
//                    4,
//                    3 + abs(8.0 * cos(30 + i * 360 * (twists + 0.5) / n)),
//                    3).toCSG().transformed(unity().rotY(90).rotZ(60+i*60.0/n));
            cube = cube.transformed(finalTransform);

            if (i == 1) {
                firstCube = cube;
            }

            if (result == null) {
                result = cube;
            }
//
            if (prevCube != null) {
                CSG union = cube.hull(prevCube);
                result = result.union(union);
            }

            if (i == n * 2) {
                CSG union = firstCube.hull(prevCube);
                result = result.union(union);
            }

            prevCube = cube;
        } // end for

        return result.transformed(unity().translateZ(8 + 4 / 2));
    }

    /**
     * Sin.
     *
     * @param deg the deg
     * @return the double
     */
    private static double sin(double deg) {
        return Math.sin(Math.toRadians(deg));
    }

    /**
     * Cos.
     *
     * @param deg the deg
     * @return the double
     */
    private static double cos(double deg) {
        return Math.cos(Math.toRadians(deg));
    }

    /**
     * Abs.
     *
     * @param value the value
     * @return the double
     */
    private double abs(double value) {
        return Math.abs(value);
    }

    /**
     * The main method.
     *
     * @param args the arguments
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void main(String[] args) throws IOException {

        MoebiusStairs moebiusStairs = new MoebiusStairs();
        CSG csg = moebiusStairs.toCSG();

        FileUtil.write(Paths.get("moebius-stairs.stl"), csg.toStlString());
        csg.toObj().toFiles(Paths.get("moebius-stairs.obj"));

    }
}
