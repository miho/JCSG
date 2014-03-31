/**
 * CSG.java
 *
 * Copyright 2014-2014 Michael Hoffer <info@michaelhoffer.de>. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info@michaelhoffer.de> "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info@michaelhoffer.de> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of Michael Hoffer
 * <info@michaelhoffer.de>.
 */
package eu.mihosoft.vrl.v3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.shape.TriangleMesh;

/**
 * Constructive Solid Geometry (CSG).
 *
 * This implementation is a Java port of
 * <a
 * href="https://github.com/evanw/csg.js/">https://github.com/evanw/csg.js/</a>
 * with some additional features like polygon extrude, transformations etc.
 * Thanks to the author for creating the CSG.js library.<br><br>
 *
 * <b>Implementation Details</b>
 *
 * All CSG operations are implemented in terms of two functions,
 * {@link Node#clipTo(eu.mihosoft.vrl.v3d.Node)} and {@link Node#invert()},
 * which remove parts of a BSP tree inside another BSP tree and swap solid and
 * empty space, respectively. To find the union of {@code a} and {@code b}, we
 * want to remove everything in {@code a} inside {@code b} and everything in
 * {@code b} inside {@code a}, then combine polygons from {@code a} and
 * {@code b} into one solid:
 *
 * <blockquote><pre>
 *     a.clipTo(b);
 *     b.clipTo(a);
 *     a.build(b.allPolygons());
 * </pre></blockquote>
 *
 * The only tricky part is handling overlapping coplanar polygons in both trees.
 * The code above keeps both copies, but we need to keep them in one tree and
 * remove them in the other tree. To remove them from {@code b} we can clip the
 * inverse of {@code b} against {@code a}. The code for union now looks like
 * this:
 *
 * <blockquote><pre>
 *     a.clipTo(b);
 *     b.clipTo(a);
 *     b.invert();
 *     b.clipTo(a);
 *     b.invert();
 *     a.build(b.allPolygons());
 * </pre></blockquote>
 *
 * Subtraction and intersection naturally follow from set operations. If union
 * is {@code A | B}, differenceion is {@code A - B = ~(~A | B)} and intersection
 * is {@code A & B =
 * ~(~A | ~B)} where {@code ~} is the complement operator.
 */
public class CSG {

    private List<Polygon> polygons;

    private CSG() {
    }

    /**
     * Constructs a CSG from a list of {@link Polygon} instances.
     *
     * @param polygons polygons
     * @return a CSG instance
     */
    public static CSG fromPolygons(List<Polygon> polygons) {
        CSG csg = new CSG();
        csg.polygons = polygons;
        return csg;
    }

    /**
     * Constructs a CSG from the specified {@link Polygon} instances.
     *
     * @param polygons polygons
     * @return a CSG instance
     */
    public static CSG fromPolygons(Polygon... polygons) {
        return fromPolygons(Arrays.asList(polygons));
    }

    @Override
    public CSG clone() {
        CSG csg = new CSG();
        csg.polygons = new ArrayList<>();
        polygons.stream().forEach((polygon) -> {
            csg.polygons.add(polygon.clone());
        });
        return csg;
    }

    /**
     *
     * @return the polygons of this CSG
     */
    public List<Polygon> getPolygons() {
        return polygons;
    }

    /**
     * Return a new CSG solid representing the union of this csg and the
     * specified csg.
     *
     * <b>Note:</b> Neither this csg nor the specified csg are modified.
     *
     * <blockquote><pre>
     *    A.union(B)
     *
     *    +-------+            +-------+
     *    |       |            |       |
     *    |   A   |            |       |
     *    |    +--+----+   =   |       +----+
     *    +----+--+    |       +----+       |
     *         |   B   |            |       |
     *         |       |            |       |
     *         +-------+            +-------+
     * </pre></blockquote>
     *
     *
     * @param csg other csg
     *
     * @return union of this csg and the specified csg
     */
    public CSG union(CSG csg) {
        Node a = new Node(this.clone().polygons);
        Node b = new Node(csg.clone().polygons);
        a.clipTo(b);
        b.clipTo(a);
        b.invert();
        b.clipTo(a);
        b.invert();
        a.build(b.allPolygons());
        return CSG.fromPolygons(a.allPolygons());
    }

    /**
     * Return a new CSG solid representing the difference of this csg and the
     * specified csg.
     *
     * <b>Note:</b> Neither this csg nor the specified csg are modified.
     *
     * <blockquote><pre>
     * A.difference(B)
     *
     * +-------+            +-------+
     * |       |            |       |
     * |   A   |            |       |
     * |    +--+----+   =   |    +--+
     * +----+--+    |       +----+
     *      |   B   |
     *      |       |
     *      +-------+
     * </pre></blockquote>
     *
     * @param csg other csg
     * @return difference of this csg and the specified csg
     */
    public CSG difference(CSG csg) {
        Node a = new Node(this.clone().polygons);
        Node b = new Node(csg.clone().polygons);
        a.invert();
        a.clipTo(b);
        b.clipTo(a);
        b.invert();
        b.clipTo(a);
        b.invert();
        a.build(b.allPolygons());
        a.invert();
        return CSG.fromPolygons(a.allPolygons());
    }

    /**
     * Return a new CSG solid representing the intersection of this csg and the
     * specified csg.
     *
     * <b>Note:</b> Neither this csg nor the specified csg are modified.
     *
     * <blockquote><pre>
     *     A.intersect(B)
     *
     *     +-------+
     *     |       |
     *     |   A   |
     *     |    +--+----+   =   +--+
     *     +----+--+    |       +--+
     *          |   B   |
     *          |       |
     *          +-------+
     * }
     * </pre></blockquote>
     *
     * @param csg other csg
     * @return intersection of this csg and the specified csg
     */
    public CSG intersect(CSG csg) {
        Node a = new Node(this.clone().polygons);
        Node b = new Node(csg.clone().polygons);
        a.invert();
        b.clipTo(a);
        b.invert();
        a.clipTo(b);
        b.clipTo(a);
        a.build(b.allPolygons());
        a.invert();
        return CSG.fromPolygons(a.allPolygons());
    }

    /**
     * Returns this csg in STL string format.
     *
     * @return this csg in STL string format
     */
    public String toStlString() {
        StringBuilder sb = new StringBuilder();
        toStlString(sb);
        return sb.toString();
    }

    /**
     * Returns this csg in STL string format.
     *
     * @param sb string builder
     *
     * @return the specified string builder
     */
    public StringBuilder toStlString(StringBuilder sb) {
        sb.append("solid v3d.csg\n");
        this.polygons.stream().forEach(
                (Polygon p) -> {
                    p.toStlString(sb);
                });
        sb.append("endsolid v3d.csg\n");
        return sb;
    }

    /**
     * Returns a transformed copy of this CSG.
     *
     * @param transform the transform to apply
     *
     * @return a transformed copy of this CSG
     */
    public CSG transformed(Transform transform) {
        List<Polygon> newpolygons = this.polygons.stream().map(
                p -> p.transformed(transform)
        ).collect(Collectors.toList());

        CSG result = CSG.fromPolygons(newpolygons);

        return result;
    }

    /**
     * Returns the CSG as JavaFX triangle mesh.
     *
     * @return the CSG as JavaFX triangle mesh
     */
    public MeshContainer toJavaFXMesh() {

        TriangleMesh mesh = new TriangleMesh();

        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE;

        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;
        double maxZ = Double.MIN_VALUE;

        int counter = 0;
        for (Polygon p : getPolygons()) {
            if (p.vertices.size() >= 3) {

                // TODO: improve the triangulation?
                //
                // JavaOne requires triangular polygons.
                // If our polygon has more vertices, create
                // multiple triangles:
                Vertex firstVertex = p.vertices.get(0);
                for (int i = 0; i < p.vertices.size() - 2; i++) {

                    if (firstVertex.pos.x < minX) {
                        minX = firstVertex.pos.x;
                    }
                    if (firstVertex.pos.y < minY) {
                        minY = firstVertex.pos.y;
                    }
                    if (firstVertex.pos.z < minZ) {
                        minZ = firstVertex.pos.z;
                    }

                    if (firstVertex.pos.x > maxX) {
                        maxX = firstVertex.pos.x;
                    }
                    if (firstVertex.pos.y > maxY) {
                        maxY = firstVertex.pos.y;
                    }
                    if (firstVertex.pos.z > maxZ) {
                        maxZ = firstVertex.pos.z;
                    }

                    mesh.getPoints().addAll(
                            (float) firstVertex.pos.x,
                            (float) firstVertex.pos.y,
                            (float) firstVertex.pos.z);

                    mesh.getTexCoords().addAll(0); // texture (not covered)
                    mesh.getTexCoords().addAll(0);

                    Vertex secondVertex = p.vertices.get(i + 1);

                    if (secondVertex.pos.x < minX) {
                        minX = secondVertex.pos.x;
                    }
                    if (secondVertex.pos.y < minY) {
                        minY = secondVertex.pos.y;
                    }
                    if (secondVertex.pos.z < minZ) {
                        minZ = secondVertex.pos.z;
                    }

                    if (secondVertex.pos.x > maxX) {
                        maxX = firstVertex.pos.x;
                    }
                    if (secondVertex.pos.y > maxY) {
                        maxY = firstVertex.pos.y;
                    }
                    if (secondVertex.pos.z > maxZ) {
                        maxZ = firstVertex.pos.z;
                    }

                    mesh.getPoints().addAll(
                            (float) secondVertex.pos.x,
                            (float) secondVertex.pos.y,
                            (float) secondVertex.pos.z);

                    mesh.getTexCoords().addAll(0); // texture (not covered)
                    mesh.getTexCoords().addAll(0);

                    Vertex thirdVertex = p.vertices.get(i + 2);

                    mesh.getPoints().addAll(
                            (float) thirdVertex.pos.x,
                            (float) thirdVertex.pos.y,
                            (float) thirdVertex.pos.z);

                    if (thirdVertex.pos.x < minX) {
                        minX = thirdVertex.pos.x;
                    }
                    if (thirdVertex.pos.y < minY) {
                        minY = thirdVertex.pos.y;
                    }
                    if (thirdVertex.pos.z < minZ) {
                        minZ = thirdVertex.pos.z;
                    }

                    if (thirdVertex.pos.x > maxX) {
                        maxX = firstVertex.pos.x;
                    }
                    if (thirdVertex.pos.y > maxY) {
                        maxY = firstVertex.pos.y;
                    }
                    if (thirdVertex.pos.z > maxZ) {
                        maxZ = firstVertex.pos.z;
                    }

                    mesh.getTexCoords().addAll(0); // texture (not covered)
                    mesh.getTexCoords().addAll(0);

                    mesh.getFaces().addAll(
                            counter, // first vertex
                            0, // texture (not covered)
                            counter + 1, // second vertex
                            0, // texture (not covered)
                            counter + 2, // third vertex
                            0 // texture (not covered)
                    );
                    counter += 3;
                } // end for
            }

        } // end for polygon

        return new MeshContainer(mesh, maxX - minX, maxY - minY, maxZ - minZ);
    }

}
