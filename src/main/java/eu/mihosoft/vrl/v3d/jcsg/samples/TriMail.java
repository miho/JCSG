/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d.jcsg.samples;

import eu.mihosoft.vrl.v3d.jcsg.CSG;
import eu.mihosoft.vrl.v3d.jcsg.FileUtil;
import static eu.mihosoft.vrl.v3d.jcsg.Transform.unity;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class TriMail {

    public CSG toCSG(int numX, int numY) {

        final int numEdges = 3;

        PolyMailTile tile = new PolyMailTile().setNumEdges(numEdges).
                setPinThickness(2.1).
                setHingeHoleScale(1.2).setConeLength(1.8);

        double hingeHoleScale = tile.getHingeHoleScale();
        double radius = tile.getRadius();
        double side = tile.getSideLength();

        double pinOffset = tile.getPinLength()
                - (tile.getJointRadius() * hingeHoleScale
                - tile.getJointRadius());

        CSG malePart = tile.setMale().toCSG().transformed(
                unity().rotZ(360.0 / numEdges * 0.75));
        CSG femalePart = tile.setFemale().toCSG().transformed(
                unity().rotZ(360.0 / numEdges * 0.25));

        femalePart = femalePart.transformed(unity().translate(0, radius / 2, 0));

        double xOffset = Math.acos(Math.toRadians(30)) * pinOffset;
        double yOffset = Math.asin(Math.toRadians(30)) * pinOffset;

        femalePart = femalePart.transformed(unity().translate(0, yOffset, 0));

        double xStep = side*0.5 + xOffset;
        double yStep = femalePart.getBounds().getBounds().y + yOffset + pinOffset;

        CSG result = null;

        for (int y = 0; y < numY; y++) {
            for (int x = 0; x < numX*2; x++) {
                
                CSG part;
                
                if (x%2==0) {
                    part = malePart;
                } else {
                    part = femalePart;
                }
                
                if (result == null) {
                    result = malePart;
                } else {
                    double xRowOffset = side*0.5 + xOffset;
                    
                    result = result.dumbUnion(part.transformed(unity().
                            translate(x*xStep+y*xRowOffset, y*yStep,0)));
                }
            }
        }

        return result;
    }

    public static void main(String[] args) throws IOException {
        FileUtil.write(new File("trimail.stl"), new TriMail().
                toCSG(8, 8).toStlString());
    }
}

//
///**
// *
// * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
// */
//public class TriMail {
//
//    public CSG toCSG(int numX, int numY) {
//        
//        final int numEdges = 3;
//
//        PolyMailTile tile = new PolyMailTile().setNumEdges(numEdges).
//                setPinThickness(2.1).
//                setHingeHoleScale(1.2).setConeLength(1.8);
//
//        double hingeHoleScale = tile.getHingeHoleScale();
//
//        CSG malePart = tile.setMale().toCSG().transformed(
//                            unity().rotZ(360.0 / numEdges*0.75));
//        CSG femalePart = tile.setFemale().toCSG().transformed(
//                            unity().rotZ(360.0 / numEdges*0.25));
//
//        CSG result = null;
//
//        for (int y = 0; y < numY; y++) {
//
//            for (int x = 0; x < numX; x++) {
//
//                double pinOffset = tile.getPinLength()
//                        - (tile.getJointRadius() * hingeHoleScale
//                        - tile.getJointRadius());
//
//                double xOffset = 0;
//                double yOffset = 0;
//
//                if (y % 2 == 0) {
//                    xOffset = tile.getSideLength() * 0.5+ pinOffset*0.5;
//                    
//                    if ((y/2) % 2 == 0) {
//                        xOffset-= tile.getSideLength()*0.5 + pinOffset*0.5;
//                    }
//                    
//                    yOffset = +tile.getPinLength()*3;
//                }
//                
//                double translateX
//                        = (-tile.getSideLength() - pinOffset) * x + xOffset;
//                double translateY = tile.getRadius()*y - yOffset;
//
//                CSG part2;
//
//                if (y % 2 == 0) {
//                    part2 = femalePart.clone();
//                } else {
//                    part2 = malePart.clone();
//                }
//
//                part2 = part2.transformed(
//                        unity().translate(translateX, translateY, 0));
//
//                if (result == null) {
//                    result = part2.clone();
//                }
//
//                result = result.dumbUnion(part2);
//            }
//        }
//
//        return result;
//    }
//
//    public static void main(String[] args) throws IOException {
//        FileUtil.write(new File("trimail.stl"), new TriMail().
//                toCSG(3, 3).toStlString());
//    }
//}
