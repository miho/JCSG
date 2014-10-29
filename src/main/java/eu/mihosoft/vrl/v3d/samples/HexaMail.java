/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d.samples;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.FileUtil;
import static eu.mihosoft.vrl.v3d.Transform.unity;
import eu.mihosoft.vrl.v3d.Vector3d;
import java.io.IOException;
import java.nio.file.Paths;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class HexaMail {

    public CSG toCSG(int numEdges, int numX, int numY) {

        PolyMailTile tile = new PolyMailTile().setNumEdges(numEdges).setHingeHoleScale(1.2);

        double hingeHoleScale = tile.getHingeHoleScale();

        CSG malePart = tile.setCombined().toCSG();
        CSG femalePart = tile.setCombined().toCSG();

        CSG result = null;

        for (int y = 0; y < numY; y++) {

            for (int x = 0; x < numX; x++) {

                double pinOffset = tile.getPinLength()
                        - (tile.getJointRadius() * hingeHoleScale
                        - tile.getJointRadius());

                double xOffset = 0;
                double yOffset = pinOffset*0.9;

                if (y % 2 == 0) {
                    xOffset = tile.getApothem() + pinOffset*0.5;
                }

                double translateX
                        = (-tile.getApothem() * 2 - pinOffset) * x + xOffset;
                double translateY
                        = (-tile.getRadius() * 0.5 - tile.getRadius()) * y - yOffset*y;

                CSG part2;

                if (x % 2 == 0) {
                    part2 = femalePart.clone();
                } else {
                    part2 = malePart.clone();
                }

                if (numEdges % 2 != 0) {
                    part2 = part2.transformed(
                            unity().rotZ(360.0 / numEdges * 0.5));
                }

                part2 = part2.transformed(
                        unity().translate(translateX, translateY, 0));

                if (result == null) {
                    result = part2.clone();
                }

                result = result.dumbUnion(part2);
            }
        }

        return result;
    }

    public static void main(String[] args) throws IOException {
        FileUtil.write(Paths.get("hexamail.stl"), new HexaMail().toCSG(6, 3, 3).toStlString());
    }
}
