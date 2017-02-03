/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg;

import eu.mihosoft.vvecmath.Vector3d;
import java.util.Optional;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class EdgeIntersectionTest {

    @Test
    public void closestPointTest() {

        // closest point is e1p2
        createClosestPointTest(
                Vector3d.xyz(1, 2, 3), /*e1p2*/ Vector3d.xyz(4, 5, 6),
                Vector3d.xyz(4, 5, 7), Vector3d.xyz(0, 1, 7),
                Vector3d.xyz(4, 5, 6));

        // parallel edges (result=null)
        createClosestPointTest(
                Vector3d.xyz(1, 1, -1), Vector3d.xyz(1, 1, 1),
                Vector3d.xyz(2, 2, -3), Vector3d.xyz(2, 2, 4),
                null);
        createClosestPointTest(
                Vector3d.xyz(1, 3, -1), Vector3d.xyz(1, 4, 2),
                Vector3d.xyz(1 + 10, 3, -1), Vector3d.xyz(1 + 10, 4, 2),
                null);
        createClosestPointTest(
                Vector3d.xyz(3, 6, -1), Vector3d.xyz(10, 7, 1),
                Vector3d.xyz(3, 6, -1 + 3), Vector3d.xyz(10, 7, 1 + 3),
                null);

        // result is exactly in the middle of e1 and e2
        createClosestPointTest(
                Vector3d.xyz(5, 4, 2), /*e1p2*/ Vector3d.xyz(3, 2, 11),
                Vector3d.xyz(5, 2, 11), /*e1p2*/ Vector3d.xyz(3, 4, 2),
                Vector3d.xyz(4, 3, 6.5));
    }

    @Test
    public void intersectionTest() {
        // closest point is e1p2 which does not exist on e2. thus, the expected
        // result is null
        createIntersectionTest(
                Vector3d.xyz(1, 2, 3), /*e1p2*/ Vector3d.xyz(4, 5, 6),
                Vector3d.xyz(4, 5, 7), Vector3d.xyz(0, 1, 7),
                null);

        // parallel edges (result=null)
        createIntersectionTest(
                Vector3d.xyz(1, 1, -1), Vector3d.xyz(1, 1, 1),
                Vector3d.xyz(2, 2, -3), Vector3d.xyz(2, 2, 4),
                null);
        createIntersectionTest(
                Vector3d.xyz(1, 3, -1), Vector3d.xyz(1, 4, 2),
                Vector3d.xyz(1 + 10, 3, -1), Vector3d.xyz(1 + 10, 4, 2),
                null);
        createIntersectionTest(
                Vector3d.xyz(3, 6, -1), Vector3d.xyz(10, 7, 1),
                Vector3d.xyz(3, 6, -1 + 3), Vector3d.xyz(10, 7, 1 + 3),
                null);

        // result is exactly in the middle of e1 and e2
        createIntersectionTest(
                Vector3d.xyz(5, 4, 2), /*e1p2*/ Vector3d.xyz(3, 2, 11),
                Vector3d.xyz(5, 2, 11), /*e1p2*/ Vector3d.xyz(3, 4, 2),
                Vector3d.xyz(4, 3, 6.5));
    }

    private static void createIntersectionTest(
            Vector3d e1p1, Vector3d e1p2,
            Vector3d e2p1, Vector3d e2p2,
            Vector3d expectedPoint) {
        Edge e1 = new Edge(
                new Vertex(
                        e1p1, Vector3d.Z_ONE),
                new Vertex(
                        e1p2, Vector3d.Z_ONE));

        Edge e2 = new Edge(
                new Vertex(
                        e2p1, Vector3d.Z_ONE),
                new Vertex(
                        e2p2, Vector3d.Z_ONE));

        Optional<Vector3d> closestPointResult = e1.getIntersection(e2);

        if (expectedPoint != null) {
            assertTrue("Intersection point must exist",
                    closestPointResult.isPresent());

            Vector3d closestPoint = closestPointResult.get();

            assertTrue("Intersection point " + expectedPoint + ", got "
                    + closestPoint, expectedPoint.equals(closestPoint));
        } else {
            assertFalse("Intersection point must not exist : "
                    + closestPointResult, closestPointResult.isPresent());
        }
    }

    private static void createClosestPointTest(
            Vector3d e1p1, Vector3d e1p2,
            Vector3d e2p1, Vector3d e2p2,
            Vector3d expectedPoint) {
        Edge e1 = new Edge(
                new Vertex(
                        e1p1, Vector3d.Z_ONE),
                new Vertex(
                        e1p2, Vector3d.Z_ONE));

        Edge e2 = new Edge(
                new Vertex(
                        e2p1, Vector3d.Z_ONE),
                new Vertex(
                        e2p2, Vector3d.Z_ONE));

        Optional<Vector3d> closestPointResult = e1.getClosestPoint(e2);

        if (expectedPoint != null) {
            assertTrue("Closest point must exist",
                    closestPointResult.isPresent());

            Vector3d closestPoint = closestPointResult.get();

            assertTrue("Expected point " + expectedPoint + ", got "
                    + closestPoint, expectedPoint.equals(closestPoint));
        } else {
            assertFalse("Closest point must not exist : "
                    + closestPointResult, closestPointResult.isPresent());
        }
    }

}
