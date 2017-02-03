/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg.samples;

import eu.mihosoft.jcsg.CSG;
import eu.mihosoft.jcsg.Cylinder;
import eu.mihosoft.jcsg.FileUtil;
import eu.mihosoft.vvecmath.Transform;

import java.io.IOException;
import java.nio.file.Paths;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class QuadrocopterBottom {

    public static void main(String[] args) throws IOException {

        CSG.setDefaultOptType(CSG.OptType.POLYGON_BOUND);

        CSG result = new QuadrocopterBottom().toCSG();

        FileUtil.write(Paths.get("quadrocopter-bottom.stl"), result.toStlString());
        result.toObj().toFiles(Paths.get("quadrocopter-bottom.obj"));
    }

    public void print3d(CSG csg, int n) {
        try {
            FileUtil.write(Paths.get("quadrocopter-bottom-" + n + ".stl"), csg.toStlString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CSG toCSG() {
        double outerRadius = 93;
        double bottomThickness = 3;
        int numHoneyCombs = 21;
        double honeyCombWallThickness = 2;
        double platformBorderThickness = 5;

        CSG base = basePlatform(outerRadius, numHoneyCombs, bottomThickness,platformBorderThickness,honeyCombWallThickness);

        return base;

    }

    private CSG basePlatform(double platformRadius, int numHoneycombs, double platformThickness, double platformBorderThickness, double honeycombWallThickness) {
        double honeycombRadius = platformRadius / numHoneycombs;

        CSG platform = new Cylinder(platformRadius, platformThickness, 64).toCSG();

        CSG innerPlatform = new Cylinder(platformRadius - platformBorderThickness, platformThickness, 64).toCSG();

        CSG platformShell = platform.difference(innerPlatform);

        CSG honeycombPrototype = new Cylinder(honeycombRadius, platformThickness, 6).toCSG();

        int numHoneycomb = (int) ((platformRadius * 2) / (honeycombRadius * 2));

        CSG hexagons = null;

        double inradiusOfHexagon = honeycombRadius * Math.cos((180.0 / 6.0) * Math.PI / 180);
        double sideLength = honeycombRadius * 2 * Math.sin((180.0 / 6.0) * Math.PI / 180);

        // TODO: change that!
        // inradius makes previus calculation obsolete
        // to be sure we use numHoneyCombs*1.3

        numHoneycomb*=1.4;

        double centerOffset = 0;//+honeycombRadius-inradiusOfHexagon;

        for (int y = 0; y < numHoneycomb; y++) {
            for (int x = 0; x < numHoneycomb; x++) {

                double offset = inradiusOfHexagon * (x % 2);

                double dx = -platformRadius + x * sideLength * 1.5;
                double dy = -platformRadius + y * inradiusOfHexagon * 2.0 + offset - honeycombWallThickness/4.0;

                dx += honeycombWallThickness*x +centerOffset - honeycombWallThickness/6.0;
                dy += honeycombWallThickness*y  + honeycombWallThickness * (x % 2)/2 + centerOffset*1.75 - inradiusOfHexagon*0.5 +honeycombWallThickness/2.0;

                CSG h = honeycombPrototype.transformed(Transform.unity().translate(
                        dx, dy, 0));

                if (hexagons == null) {
                    hexagons = h;
                } else {
                    hexagons = hexagons.union(h);
                }
            }
        }

        double centerHoleRadius = 15;
        double holeBorderThickness = platformBorderThickness;
        double cylHeight = platformThickness;

        CSG centerHoleOuter = new Cylinder(centerHoleRadius+holeBorderThickness,cylHeight,16).toCSG();
        CSG centerHoleInner = new Cylinder(centerHoleRadius, cylHeight, 16).toCSG();

        CSG centerHoleShell = centerHoleOuter.difference(centerHoleInner);

        if (hexagons!=null) {
            platform = platform.difference(hexagons);
        }

        return platform.union(platformShell,centerHoleShell).difference(centerHoleInner);
    }
}