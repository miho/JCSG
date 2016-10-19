/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d;

import java.util.List;
import javafx.scene.paint.Color;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public interface CSG {

    CSG clone();

    CSG color(Color c);
    
    public static RenderType getDefaultRenderType() {
        return RenderType.JAVA;
    }

    /**
     * @param optType the optType to set
     */
    static void setDefaultOptType(OptType optType) {
        //
    }

    /**
     * @param optType the optType to set
     */
    void setOptType(OptType optType);

    /**
     * Constructs a CSG from a list of {@link Polygon} instances.
     *
     * @param polygons polygons
     * @return a CSG instance
     */
    static CSG fromPolygons(RenderType type, List<Polygon> polygons) {
        if (type == RenderType.JAVA) {
            return CSGImpl.fromPolygons(polygons);
        } else if (type == RenderType.JS) {
            return CSGImpl.fromPolygons(polygons);
        }

        return null;
    }

    /**
     * Constructs a CSG from the specified {@link Polygon} instances.
     *
     * @param polygons polygons
     * @return a CSG instance
     */
    static CSG fromPolygons(RenderType type, Polygon... polygons) {
        if (type == RenderType.JAVA) {
            return CSGImpl.fromPolygons(polygons);
        } else if (type == RenderType.JS) {
            return CSGImpl.fromPolygons(polygons);
        }
        return null;
    }

    /**
     * Constructs a CSG from the specified {@link Polygon} instances.
     *
     * @param storage shared storage
     * @param polygons polygons
     * @return a CSG instance
     */
    static CSG fromPolygons(RenderType type, PropertyStorage storage, Polygon... polygons) {
        if (type == RenderType.JAVA) {
            return CSGImpl.fromPolygons(storage, polygons);
        } else if (type == RenderType.JS) {
            return CSGImpl.fromPolygons(storage, polygons);
        }
        return null;
    }

    public static CSG fromPolygons(RenderType type, PropertyStorage properties, List<Polygon> toPolygons) {
        if (type == RenderType.JAVA) {
            return CSGImpl.fromPolygons(properties, toPolygons);
        } else if (type == RenderType.JS) {
            return CSGImpl.fromPolygons(properties, toPolygons);
        }
        return null;
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
    CSG difference(List<CSG> csgs);

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
    CSG difference(CSG... csgs);

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
    CSG difference(CSG csg);

    /**
     * Returns the bounds of this csg.
     *
     * @return bouds of this csg
     */
    Bounds getBounds();

    /**
     *
     * @return the polygons of this CSG
     */
    List<Polygon> getPolygons();

    /**
     * Returns the convex hull of this csg.
     *
     * @return the convex hull of this csg
     */
    CSG hull();

    /**
     * Returns the convex hull of this csg and the union of the specified csgs.
     *
     * @param csgs csgs
     * @return the convex hull of this csg and the specified csgs
     */
    CSG hull(List<CSG> csgs);

    /**
     * Returns the convex hull of this csg and the union of the specified csgs.
     *
     * @param csgs csgs
     * @return the convex hull of this csg and the specified csgs
     */
    CSG hull(CSG... csgs);

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
    CSG intersect(CSG csg);

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
    CSG intersect(List<CSG> csgs);

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
    CSG intersect(CSG... csgs);

    /**
     * Defines the CSg optimization type.
     *
     * @param type optimization type
     * @return this CSG
     */
    CSG optimization(OptType type);

    // TODO finish experiment (20.7.2014)
    MeshContainer toJavaFXMesh();

    /**
     * Returns the CSG as JavaFX triangle mesh.
     *
     * @return the CSG as JavaFX triangle mesh
     */
    MeshContainer toJavaFXMeshSimple();

    ObjFile toObj();

    /**
     * Returns this csg in OBJ string format.
     *
     * @param sb string builder
     * @return the specified string builder
     */
    StringBuilder toObjString(StringBuilder sb);

    /**
     * Returns this csg in OBJ string format.
     *
     * @return this csg in OBJ string format
     */
    String toObjString();

    /**
     * Returns this csg in STL string format.
     *
     * @return this csg in STL string format
     */
    String toStlString();

    /**
     * Returns this csg in STL string format.
     *
     * @param sb string builder
     *
     * @return the specified string builder
     */
    StringBuilder toStlString(StringBuilder sb);

    /**
     * Returns a transformed copy of this CSG.
     *
     * @param transform the transform to apply
     *
     * @return a transformed copy of this CSG
     */
    CSG transformed(Transform transform);

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
    CSG union(CSG csg);

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
    CSG union(List<CSG> csgs);

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
    CSG union(CSG... csgs);

    CSG weighted(WeightFunction f);

    public RenderType getRenderType();

    public static enum OptType {

        CSG_BOUND,
        POLYGON_BOUND,
        NONE
    }

    public static enum RenderType {
        JAVA,
        JS,
    }
}
