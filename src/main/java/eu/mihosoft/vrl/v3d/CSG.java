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

import eu.mihosoft.vrl.v3d.ext.openjfx.importers.obj.ObjImporter;
import eu.mihosoft.vrl.v3d.ext.quickhull3d.HullUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.scene.paint.Color;
import javafx.scene.shape.Mesh;
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
    private static OptType defaultOptType = OptType.NONE;
    private OptType optType = null;
    private PropertyStorage storage;

    private CSG() {
        storage = new PropertyStorage();
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

    /**
     * Constructs a CSG from a list of {@link Polygon} instances.
     *
     * @param storage shared storage
     * @param polygons polygons
     * @return a CSG instance
     */
    public static CSG fromPolygons(PropertyStorage storage, List<Polygon> polygons) {

        CSG csg = new CSG();
        csg.polygons = polygons;

        csg.storage = storage;

        for (Polygon polygon : polygons) {
            polygon.setStorage(storage);
        }

        return csg;
    }

    /**
     * Constructs a CSG from the specified {@link Polygon} instances.
     *
     * @param storage shared storage
     * @param polygons polygons
     * @return a CSG instance
     */
    public static CSG fromPolygons(PropertyStorage storage, Polygon... polygons) {
        return fromPolygons(storage, Arrays.asList(polygons));
    }

    @Override
    public CSG clone() {
        CSG csg = new CSG();

        csg.setOptType(this.getOptType());

        // sequential code
//        csg.polygons = new ArrayList<>();
//        polygons.forEach((polygon) -> {
//            csg.polygons.add(polygon.clone());
//        });
        Stream<Polygon> polygonStream;

        if (polygons.size() > 200) {
            polygonStream = polygons.parallelStream();
        } else {
            polygonStream = polygons.stream();
        }

        csg.polygons = polygonStream.
                map((Polygon p) -> p.clone()).collect(Collectors.toList());

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
     * Defines the CSg optimization type.
     *
     * @param type optimization type
     * @return this CSG
     */
    public CSG optimization(OptType type) {
        this.setOptType(type);
        return this;
    }

    /**
     * Return a new CSG solid representing the union of this csg and the
     * specified csg.
     *
     * <b>Note:</b> Neither this csg nor the specified csg are weighted.
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

        switch (getOptType()) {
            case CSG_BOUND:
                return _unionCSGBoundsOpt(csg);
            case POLYGON_BOUND:
                return _unionPolygonBoundsOpt(csg);
            default:
//                return _unionIntersectOpt(csg);
                return _unionNoOpt(csg);
        }
    }
    
    /**
     * Returns a csg consisting of the polygons of this csg and the specified csg.
     * 
     * The purpose of this method is to allow fast union operations for objects
     * that do not intersect. 
     * 
     * <p><b>WARNING:</b> this method does not apply the csg algorithms. Therefore,
     * please ensure that this csg and the specified csg do not intersect.
     * 
     * @param csg csg
     * 
     * @return a csg consisting of the polygons of this csg and the specified csg
     */
    public CSG dumbUnion(CSG csg) {
        
        CSG result = this.clone();
        CSG other = csg.clone();
        
        result.polygons.addAll(other.polygons);
        
        return result;
    }

    /**
     * Return a new CSG solid representing the union of this csg and the
     * specified csgs.
     *
     * <b>Note:</b> Neither this csg nor the specified csg are weighted.
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
     * @param csgs other csgs
     *
     * @return union of this csg and the specified csgs
     */
    public CSG union(List<CSG> csgs) {

        CSG result = this;

        for (CSG csg : csgs) {
            result = result.union(csg);
        }

        return result;
    }

    /**
     * Return a new CSG solid representing the union of this csg and the
     * specified csgs.
     *
     * <b>Note:</b> Neither this csg nor the specified csg are weighted.
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
     * @param csgs other csgs
     *
     * @return union of this csg and the specified csgs
     */
    public CSG union(CSG... csgs) {
        return union(Arrays.asList(csgs));
    }

    /**
     * Returns the convex hull of this csg.
     *
     * @return the convex hull of this csg
     */
    public CSG hull() {

        return HullUtil.hull(this, storage);
    }

    /**
     * Returns the convex hull of this csg and the union of the specified csgs.
     *
     * @param csgs csgs
     * @return the convex hull of this csg and the specified csgs
     */
    public CSG hull(List<CSG> csgs) {

        CSG csgsUnion = new CSG();
        csgsUnion.storage = storage;
        csgsUnion.optType = optType;
        csgsUnion.polygons = this.clone().polygons;

        csgs.stream().forEach((csg) -> {
            csgsUnion.polygons.addAll(csg.clone().polygons);
        });

        csgsUnion.polygons.forEach(p -> p.setStorage(storage));
        return csgsUnion.hull();

//        CSG csgsUnion = this;
//
//        for (CSG csg : csgs) {
//            csgsUnion = csgsUnion.union(csg);
//        }
//
//        return csgsUnion.hull();
    }

    /**
     * Returns the convex hull of this csg and the union of the specified csgs.
     *
     * @param csgs csgs
     * @return the convex hull of this csg and the specified csgs
     */
    public CSG hull(CSG... csgs) {

        return hull(Arrays.asList(csgs));
    }

    private CSG _unionCSGBoundsOpt(CSG csg) {
        System.err.println("WARNING: using " + CSG.OptType.NONE
                + " since other optimization types missing for union operation.");
        return _unionIntersectOpt(csg);
    }

    private CSG _unionPolygonBoundsOpt(CSG csg) {
        List<Polygon> inner = new ArrayList<>();
        List<Polygon> outer = new ArrayList<>();

        Bounds bounds = csg.getBounds();

        this.polygons.stream().forEach((p) -> {
            if (bounds.intersects(p.getBounds())) {
                inner.add(p);
            } else {
                outer.add(p);
            }
        });

        List<Polygon> allPolygons = new ArrayList<>();

        if (!inner.isEmpty()) {
            CSG innerCSG = CSG.fromPolygons(inner);

            allPolygons.addAll(outer);
            allPolygons.addAll(innerCSG._unionNoOpt(csg).polygons);
        } else {
            allPolygons.addAll(this.polygons);
            allPolygons.addAll(csg.polygons);
        }

        return CSG.fromPolygons(allPolygons).optimization(getOptType());
    }

    /**
     * Optimizes for intersection. If csgs do not intersect create a new csg
     * that consists of the polygon lists of this csg and the specified csg. In
     * this case no further space partitioning is performed.
     *
     * @param csg csg
     * @return the union of this csg and the specified csg
     */
    private CSG _unionIntersectOpt(CSG csg) {
        boolean intersects = false;

        Bounds bounds = csg.getBounds();

        for (Polygon p : polygons) {
            if (bounds.intersects(p.getBounds())) {
                intersects = true;
                break;
            }
        }

        List<Polygon> allPolygons = new ArrayList<>();

        if (intersects) {
            return _unionNoOpt(csg);
        } else {
            allPolygons.addAll(this.polygons);
            allPolygons.addAll(csg.polygons);
        }

        return CSG.fromPolygons(allPolygons).optimization(getOptType());
    }

    private CSG _unionNoOpt(CSG csg) {
        Node a = new Node(this.clone().polygons);
        Node b = new Node(csg.clone().polygons);
        a.clipTo(b);
        b.clipTo(a);
        b.invert();
        b.clipTo(a);
        b.invert();
        a.build(b.allPolygons());
        return CSG.fromPolygons(a.allPolygons()).optimization(getOptType());
    }

    /**
     * Return a new CSG solid representing the difference of this csg and the
     * specified csgs.
     *
     * <b>Note:</b> Neither this csg nor the specified csgs are weighted.
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
     * @param csgs other csgs
     * @return difference of this csg and the specified csgs
     */
    public CSG difference(List<CSG> csgs) {

        if (csgs.isEmpty()) {
            return this.clone();
        }

        CSG csgsUnion = csgs.get(0);

        for (int i = 1; i < csgs.size(); i++) {
            csgsUnion = csgsUnion.union(csgs.get(i));
        }

        return difference(csgsUnion);
    }

    /**
     * Return a new CSG solid representing the difference of this csg and the
     * specified csgs.
     *
     * <b>Note:</b> Neither this csg nor the specified csgs are weighted.
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
     * @param csgs other csgs
     * @return difference of this csg and the specified csgs
     */
    public CSG difference(CSG... csgs) {

        return difference(Arrays.asList(csgs));
    }

    /**
     * Return a new CSG solid representing the difference of this csg and the
     * specified csg.
     *
     * <b>Note:</b> Neither this csg nor the specified csg are weighted.
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

        switch (getOptType()) {
            case CSG_BOUND:
                return _differenceCSGBoundsOpt(csg);
            case POLYGON_BOUND:
                return _differencePolygonBoundsOpt(csg);
            default:
                return _differenceNoOpt(csg);
        }
    }

    private CSG _differenceCSGBoundsOpt(CSG csg) {
        CSG b = csg;

        CSG a1 = this._differenceNoOpt(csg.getBounds().toCSG());
        CSG a2 = this.intersect(csg.getBounds().toCSG());

        return a2._differenceNoOpt(b)._unionIntersectOpt(a1).optimization(getOptType());
    }

    private CSG _differencePolygonBoundsOpt(CSG csg) {
        List<Polygon> inner = new ArrayList<>();
        List<Polygon> outer = new ArrayList<>();

        Bounds bounds = csg.getBounds();

        this.polygons.stream().forEach((p) -> {
            if (bounds.intersects(p.getBounds())) {
                inner.add(p);
            } else {
                outer.add(p);
            }
        });

        CSG innerCSG = CSG.fromPolygons(inner);

        List<Polygon> allPolygons = new ArrayList<>();
        allPolygons.addAll(outer);
        allPolygons.addAll(innerCSG._differenceNoOpt(csg).polygons);

        return CSG.fromPolygons(allPolygons).optimization(getOptType());
    }

    private CSG _differenceNoOpt(CSG csg) {

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

        CSG csgA = CSG.fromPolygons(a.allPolygons()).optimization(getOptType());
        return csgA;
    }

    /**
     * Return a new CSG solid representing the intersection of this csg and the
     * specified csg.
     *
     * <b>Note:</b> Neither this csg nor the specified csg are weighted.
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
        return CSG.fromPolygons(a.allPolygons()).optimization(getOptType());
    }

    /**
     * Return a new CSG solid representing the intersection of this csg and the
     * specified csgs.
     *
     * <b>Note:</b> Neither this csg nor the specified csgs are weighted.
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
     * @param csgs other csgs
     * @return intersection of this csg and the specified csgs
     */
    public CSG intersect(List<CSG> csgs) {

        if (csgs.isEmpty()) {
            return this.clone();
        }

        CSG csgsUnion = csgs.get(0);

        for (int i = 1; i < csgs.size(); i++) {
            csgsUnion = csgsUnion.union(csgs.get(i));
        }

        return intersect(csgsUnion);
    }

    /**
     * Return a new CSG solid representing the intersection of this csg and the
     * specified csgs.
     *
     * <b>Note:</b> Neither this csg nor the specified csgs are weighted.
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
     * @param csgs other csgs
     * @return intersection of this csg and the specified csgs
     */
    public CSG intersect(CSG... csgs) {

        return intersect(Arrays.asList(csgs));
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

    public CSG color(Color c) {

        CSG result = this.clone();

        storage.set("material:color",
                "" + c.getRed()
                + " " + c.getGreen()
                + " " + c.getBlue());

        return result;
    }

    public ObjFile toObj() {

        StringBuilder objSb = new StringBuilder();

        objSb.append("mtllib " + ObjFile.MTL_NAME);

        objSb.append("# Group").append("\n");
        objSb.append("g v3d.csg\n");

        class PolygonStruct {

            PropertyStorage storage;
            List<Integer> indices;
            String materialName;

            public PolygonStruct(PropertyStorage storage, List<Integer> indices, String materialName) {
                this.storage = storage;
                this.indices = indices;
                this.materialName = materialName;
            }
        }

        List<Vertex> vertices = new ArrayList<>();
        List<PolygonStruct> indices = new ArrayList<>();

        objSb.append("\n# Vertices\n");

        Map<PropertyStorage, Integer> materialNames = new HashMap<>();

        int materialIndex = 0;

        for (Polygon p : polygons) {
            List<Integer> polyIndices = new ArrayList<>();

            p.vertices.stream().forEach((v) -> {
                if (!vertices.contains(v)) {
                    vertices.add(v);
                    v.toObjString(objSb);
                    polyIndices.add(vertices.size());
                } else {
                    polyIndices.add(vertices.indexOf(v) + 1);
                }
            });

            if (!materialNames.containsKey(p.getStorage())) {
                materialIndex++;
                materialNames.put(p.getStorage(), materialIndex);
                p.getStorage().set("material:name", materialIndex);
            }

            indices.add(new PolygonStruct(
                    p.getStorage(), polyIndices,
                    "material-" + materialNames.get(p.getStorage())));
        }

        objSb.append("\n# Faces").append("\n");

        for (PolygonStruct ps : indices) {

            // add mtl info
            ps.storage.getValue("material:color").ifPresent(
                    (v) -> objSb.append("usemtl ").append(ps.materialName).append("\n"));

            // we triangulate the polygon to ensure 
            // compatibility with 3d printer software
            List<Integer> pVerts = ps.indices;
            int index1 = pVerts.get(0);
            for (int i = 0; i < pVerts.size() - 2; i++) {
                int index2 = pVerts.get(i + 1);
                int index3 = pVerts.get(i + 2);

                objSb.append("f ").
                        append(index1).append(" ").
                        append(index2).append(" ").
                        append(index3).append("\n");
            }
        }

        objSb.append("\n# End Group v3d.csg").append("\n");

        StringBuilder mtlSb = new StringBuilder();

        materialNames.keySet().forEach(s -> {
            if (s.contains("material:color")) {
                mtlSb.append("newmtl material-").append(s.getValue("material:name").get()).append("\n");
                mtlSb.append("Kd ").append(s.getValue("material:color").get()).append("\n");
            }
        });

        return new ObjFile(objSb.toString(), mtlSb.toString());
    }

    /**
     * Returns this csg in OBJ string format.
     *
     * @param sb string builder
     * @return the specified string builder
     */
    public StringBuilder toObjString(StringBuilder sb) {
        sb.append("# Group").append("\n");
        sb.append("g v3d.csg\n");

        class PolygonStruct {

            PropertyStorage storage;
            List<Integer> indices;
            String materialName;

            public PolygonStruct(PropertyStorage storage, List<Integer> indices, String materialName) {
                this.storage = storage;
                this.indices = indices;
                this.materialName = materialName;
            }
        }

        List<Vertex> vertices = new ArrayList<>();
        List<PolygonStruct> indices = new ArrayList<>();

        sb.append("\n# Vertices\n");

        for (Polygon p : polygons) {
            List<Integer> polyIndices = new ArrayList<>();

            p.vertices.stream().forEach((v) -> {
                if (!vertices.contains(v)) {
                    vertices.add(v);
                    v.toObjString(sb);
                    polyIndices.add(vertices.size());
                } else {
                    polyIndices.add(vertices.indexOf(v) + 1);
                }
            });

        }

        sb.append("\n# Faces").append("\n");

        for (PolygonStruct ps : indices) {
            // we triangulate the polygon to ensure 
            // compatibility with 3d printer software
            List<Integer> pVerts = ps.indices;
            int index1 = pVerts.get(0);
            for (int i = 0; i < pVerts.size() - 2; i++) {
                int index2 = pVerts.get(i + 1);
                int index3 = pVerts.get(i + 2);

                sb.append("f ").
                        append(index1).append(" ").
                        append(index2).append(" ").
                        append(index3).append("\n");
            }
        }

        sb.append("\n# End Group v3d.csg").append("\n");

        return sb;
    }

    /**
     * Returns this csg in OBJ string format.
     *
     * @return this csg in OBJ string format
     */
    public String toObjString() {
        StringBuilder sb = new StringBuilder();
        return toObjString(sb).toString();
    }

    public CSG weighted(WeightFunction f) {
        return new Modifier(f).modified(this);
    }

    /**
     * Returns a transformed copy of this CSG.
     *
     * @param transform the transform to apply
     *
     * @return a transformed copy of this CSG
     */
    public CSG transformed(Transform transform) {

        if (polygons.isEmpty()) {
            return clone();
        }

        List<Polygon> newpolygons = this.polygons.stream().map(
                p -> p.transformed(transform)
        ).collect(Collectors.toList());

        CSG result = CSG.fromPolygons(newpolygons).optimization(getOptType());

        result.storage = storage;

        return result;
    }
 

    // TODO finish experiment (20.7.2014)
    public MeshContainer toJavaFXMesh() {

        return toJavaFXMeshSimple();

// TODO test obj approach with multiple materials
//        try {
//            ObjImporter importer = new ObjImporter(toObj());
//
//            List<Mesh> meshes = new ArrayList<>(importer.getMeshCollection());
//            return new MeshContainer(getBounds().getMin(), getBounds().getMax(),
//                    meshes, new ArrayList<>(importer.getMaterialCollection()));
//        } catch (IOException ex) {
//            Logger.getLogger(CSG.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        // we have no backup strategy for broken streams :(
//        return null;
    }

    /**
     * Returns the CSG as JavaFX triangle mesh.
     *
     * @return the CSG as JavaFX triangle mesh
     */
    public MeshContainer toJavaFXMeshSimple() {

        TriangleMesh mesh = new TriangleMesh();

        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;

        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;

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
            } // end if #verts >= 3

        } // end for polygon

        return new MeshContainer(new Vector3d(minX, minY, minZ), new Vector3d(maxX, maxY, maxZ), mesh);
    }

    /**
     * Returns the bounds of this csg.
     *
     * @return bouds of this csg
     */
    public Bounds getBounds() {

        if (polygons.isEmpty()) {
            return new Bounds(Vector3d.ZERO, Vector3d.ZERO);
        }

        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;

        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;

        for (Polygon p : getPolygons()) {

            for (int i = 0; i < p.vertices.size(); i++) {

                Vertex vert = p.vertices.get(i);

                if (vert.pos.x < minX) {
                    minX = vert.pos.x;
                }
                if (vert.pos.y < minY) {
                    minY = vert.pos.y;
                }
                if (vert.pos.z < minZ) {
                    minZ = vert.pos.z;
                }

                if (vert.pos.x > maxX) {
                    maxX = vert.pos.x;
                }
                if (vert.pos.y > maxY) {
                    maxY = vert.pos.y;
                }
                if (vert.pos.z > maxZ) {
                    maxZ = vert.pos.z;
                }

            } // end for vertices

        } // end for polygon

        return new Bounds(
                new Vector3d(minX, minY, minZ),
                new Vector3d(maxX, maxY, maxZ));
    }

    /**
     * @return the optType
     */
    private OptType getOptType() {
        return optType != null ? optType : defaultOptType;
    }

    /**
     * @param optType the optType to set
     */
    public static void setDefaultOptType(OptType optType) {
        defaultOptType = optType;
    }

    /**
     * @param optType the optType to set
     */
    public void setOptType(OptType optType) {
        this.optType = optType;
    }

    public static enum OptType {

        CSG_BOUND,
        POLYGON_BOUND,
        NONE
    }

}
