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
public class TriMail {

    public CSG toCSG(int numEdges, int numX, int numY) {

        PolyMailTile tile = new PolyMailTile().setNumEdges(numEdges).
                setPinThickness(2.1).
                setHingeHoleScale(1.2).setConeLength(1.8);

        double hingeHoleScale = tile.getHingeHoleScale();

        CSG malePart = tile.setMale().toCSG().transformed(
                            unity().rotZ(360.0 / numEdges*0.75));
        CSG femalePart = tile.setFemale().toCSG().transformed(
                            unity().rotZ(360.0 / numEdges*0.25));

        CSG result = null;

        for (int y = 0; y < numY; y++) {

            for (int x = 0; x < numX; x++) {

                double pinOffset = tile.getPinLength()
                        - (tile.getJointRadius() * hingeHoleScale
                        - tile.getJointRadius());

                double xOffset = 0;
                double yOffset =0;

                if (y % 2 == 0) {
                    xOffset = tile.getSideLength() * 0.5+ pinOffset*0.5;
                    
                    if ((y/2) % 2 == 0) {
                        xOffset-= tile.getSideLength()*0.5 + pinOffset*0.5;
                    }
                    
                     yOffset = +tile.getPinLength()*3;
                }
                
                
                double translateX
                        = (-tile.getSideLength() - pinOffset) * x + xOffset;
                double translateY = tile.getRadius()*y - yOffset;

                CSG part2;

                if (y % 2 == 0) {
                    part2 = femalePart.clone();
                } else {
                    part2 = malePart.clone();
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
        FileUtil.write(Paths.get("trimail-test.stl"), new TriMail().toCSG(3, 3, 3).toStlString());
    }
}
