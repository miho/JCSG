/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg;

import eu.mihosoft.vvecmath.Vector3d;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class HoleDetectionTest {

    @Test
    public void holeDetectionTest() {
        
        // one polygon with one hole
        Polygon p1 = Polygon.fromPoints(
                Vector3d.xy(1, 1),
                Vector3d.xy(2, 3),
                Vector3d.xy(4, 3),
                Vector3d.xy(5, 2),
                Vector3d.xy(4, 1),
                Vector3d.xy(3, 0),
                Vector3d.xy(2, 2)
        );
        Polygon p1Hole = Polygon.fromPoints(
                Vector3d.xy(3, 1),
                Vector3d.xy(3, 2),
                Vector3d.xy(4, 2)
        );

        createNumHolesTest(Arrays.asList(p1, p1Hole), 1, 0);
        
        // one polygon with two holes
        Polygon p2 = Polygon.fromPoints(
                Vector3d.xy(1, 1),
                Vector3d.xy(2, 2),
                Vector3d.xy(1, 5),
                Vector3d.xy(2, 6),
                Vector3d.xy(6, 6),
                Vector3d.xy(3, 5),
                Vector3d.xy(6, 5),
                Vector3d.xy(6, 1),
                Vector3d.xy(3, 0)
        );
        Polygon p2Hole1 = Polygon.fromPoints(
                Vector3d.xy(3, 2),
                Vector3d.xy(3, 3),
                Vector3d.xy(4, 2),
                Vector3d.xy(4, 1)
        );
        Polygon p2Hole2 = Polygon.fromPoints(
                Vector3d.xy(2, 3),
                Vector3d.xy(2, 4),
                Vector3d.xy(3, 4)
        );

        createNumHolesTest(Arrays.asList(p2, p2Hole1, p2Hole2), 2, 0, 0);
        
        // one polygon with two holes, one of the holes contains another
        // polygon with one hole
        Polygon p3 = Polygon.fromPoints(
                Vector3d.xy(1, 1),
                Vector3d.xy(2, 2),
                Vector3d.xy(1, 5),
                Vector3d.xy(2, 6),
                Vector3d.xy(6, 6),
                Vector3d.xy(3, 5),
                Vector3d.xy(6, 5),
                Vector3d.xy(6, 1),
                Vector3d.xy(3, 0)
        );
        Polygon p3Hole1 = Polygon.fromPoints(
                Vector3d.xy(3, 2),
                Vector3d.xy(3, 3),
                Vector3d.xy(4, 4),
                Vector3d.xy(5, 3),
                Vector3d.xy(5, 2),
                Vector3d.xy(4, 1)
        );
        
        Polygon p3p1 = Polygon.fromPoints(
                Vector3d.xy(4, 2),
                Vector3d.xy(3.5, 2.5),
                Vector3d.xy(4, 3),
                Vector3d.xy(4.5, 2.5)
        );
        
        Polygon p3p1Hole = Polygon.fromPoints(
                Vector3d.xy(4, 2.25),
                Vector3d.xy(3.75, 2.5),
                Vector3d.xy(4, 2.75),
                Vector3d.xy(4.25, 2.5)
        );
        
        Polygon p3Hole2 = Polygon.fromPoints(
                Vector3d.xy(2, 3),
                Vector3d.xy(2, 4),
                Vector3d.xy(3, 4)
        );

        createNumHolesTest(
                Arrays.asList(p3, p3Hole1, p3Hole2, p3p1, p3p1Hole),
                2, 0, 0, 1, 0);
    }

    private static void createNumHolesTest(
            List<Polygon> polygons, int... numHoles) {

        if (polygons.size() != numHoles.length) {
            throw new IllegalArgumentException(
                    "Number of polygons and number of entries in numHoles-array"
                    + " are not equal!");
        }

        polygons = Edge.boundaryPathsWithHoles(polygons);

        for (int i = 0; i < polygons.size(); i++) {

            Optional<List<Polygon>> holesOfPresult
                    = polygons.get(i).
                    getStorage().getValue(Edge.KEY_POLYGON_HOLES);

            int numHolesOfP;

            if (!holesOfPresult.isPresent()) {
                numHolesOfP = 0;
            } else {
                List<Polygon> holesOfP = holesOfPresult.get();
                numHolesOfP = holesOfP.size();
            }

            assertTrue("Polygon " + i + ": Expected " + numHoles[i]
                    + " holes, got "
                    + numHolesOfP, numHolesOfP == numHoles[i]);
        }
    }
}
