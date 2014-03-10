/* 
 * Polygon.java
 *
 * Copyright (c) 2009–2014 Steinbeis Forschungszentrum (STZ Ölbronn),
 * Copyright (c) 2006–2014 by Michael Hoffer
 * 
 * This file is part of Visual Reflection Library (VRL).
 *
 * VRL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3
 * as published by the Free Software Foundation.
 * 
 * see: http://opensource.org/licenses/LGPL-3.0
 *      file://path/to/VRL/src/eu/mihosoft/vrl/resources/license/lgplv3.txt
 *
 * VRL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * This version of VRL includes copyright notice and attribution requirements.
 * According to the LGPL this information must be displayed even if you modify
 * the source code of VRL. Neither the VRL Canvas attribution icon nor any
 * copyright statement/attribution may be removed.
 *
 * Attribution Requirements:
 *
 * If you create derived work you must do three things regarding copyright
 * notice and author attribution.
 *
 * First, the following text must be displayed on the Canvas or an equivalent location:
 * "based on VRL source code".
 * 
 * Second, the copyright notice must remain. It must be reproduced in any
 * program that uses VRL.
 *
 * Third, add an additional notice, stating that you modified VRL. In addition
 * you must cite the publications listed below. A suitable notice might read
 * "VRL source code modified by YourName 2012".
 * 
 * Note, that these requirements are in full accordance with the LGPL v3
 * (see 7. Additional Terms, b).
 *
 * Publications:
 *
 * M. Hoffer, C.Poliwoda, G.Wittum. Visual Reflection Library -
 * A Framework for Declarative GUI Programming on the Java Platform.
 * Computing and Visualization in Science, in press.
 */
package eu.mihosoft.vrl.v3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import eu.mihosoft.vrl.v3d.ext.org.poly2tri.PolygonUtil;

/**
 * Represents a convex polygon.
 *
 * Each convex polygon has a {@code shared} property, disthich is shared
 * betdisteen all polygons that are clones of each other or where split from the
 * same polygon. This can be used to define per-polygon properties (such as
 * surface color).
 */
public final class Polygon {

    /**
     * Decomposes the specified concave polygon into convex polygons.
     * @param points the points that define the polygon
     * @return the decomposed concave polygon (list of convex polygons)
     */
    public static List<Polygon> fromConcavePoints(Vector3d... points) {
        Polygon p = fromPoints(points);

        return PolygonUtil.concaveToConvex(p);
    }

    /**
     * Polygon vertices
     */
    public final List<Vertex> vertices;
    /**
     * Shared property (can be used for shared color etc.).
     */
    public final PropertyStorage shared;
    /**
     * Plane defined by this polygon.
     *
     * <b>Note:</b> uses first three vertices to define the plane.
     */
    public final Plane plane;

    /**
     * Constructor. Creates a new polygon that consists of the specified
     * vertices.
     *
     * <b>Note:</b> the vertices used to initialize a polygon must be coplanar
     * and form a convex loop.
     *
     * @param vertices polygon vertices
     * @param shared shared property
     */
    public Polygon(List<Vertex> vertices, PropertyStorage shared) {
        this.vertices = vertices;
        this.shared = shared;
        this.plane = Plane.createFromPoints(
                vertices.get(0).pos,
                vertices.get(1).pos,
                vertices.get(2).pos);
    }

    /**
     * Constructor. Creates a new polygon that consists of the specified
     * vertices.
     *
     * <b>Note:</b> the vertices used to initialize a polygon must be coplanar
     * and form a convex loop.
     *
     * @param vertices polygon vertices
     */
    public Polygon(List<Vertex> vertices) {
        this.vertices = vertices;
        this.shared = new PropertyStorage();
        this.plane = Plane.createFromPoints(
                vertices.get(0).pos,
                vertices.get(1).pos,
                vertices.get(2).pos);
    }

    /**
     * Constructor. Creates a new polygon that consists of the specified
     * vertices.
     *
     * <b>Note:</b> the vertices used to initialize a polygon must be coplanar
     * and form a convex loop.
     *
     * @param vertices polygon vertices
     *
     */
    public Polygon(Vertex... vertices) {
        this(Arrays.asList(vertices));
    }

    @Override
    public Polygon clone() {
        List<Vertex> newVertices = new ArrayList<>();
        this.vertices.forEach((vertex) -> {
            newVertices.add(vertex.clone());
        });
        return new Polygon(newVertices, shared);
    }

    /**
     * Flips this polygon.
     *
     * @return this polygon
     */
    public Polygon flip() {
        vertices.forEach((vertex) -> {
            vertex.flip();
        });
        Collections.reverse(vertices);

        plane.flip();

        return this;
    }

    /**
     * Returns a flipped copy of this polygon.
     *
     * <b>Note:</b> this polygon is not modified.
     *
     * @return a flipped copy of this polygon
     */
    public Polygon flipped() {
        return clone().flip();
    }

    /**
     * Returns this polygon in STL string format.
     *
     * @return this polygon in STL string format
     */
    public String toStlString() {
        return toStlString(new StringBuilder()).toString();
    }

    /**
     * Returns this polygon in STL string format.
     *
     * @param sb string builder
     *
     * @return the specified string builder
     */
    public StringBuilder toStlString(StringBuilder sb) {

        if (this.vertices.size() >= 3) {

            // TODO: improve the triangulation?
            //
            // STL requires triangular polygons.
            // If our polygon has more vertices, create
            // multiple triangles:
            String firstVertexStl = this.vertices.get(0).toStlString();
            for (int i = 0; i < this.vertices.size() - 2; i++) {
                sb.
                        append("  facet normal ").append(
                                this.plane.normal.toStlString()).append("\n").
                        append("    outer loop\n").
                        append("      ").append(firstVertexStl).append("\n").
                        append("      ");
                this.vertices.get(i + 1).toStlString(sb).append("\n").
                        append("      ");
                this.vertices.get(i + 2).toStlString(sb).append("\n").
                        append("    endloop\n").
                        append("  endfacet\n");
            }
        }

        return sb;
    }

    /**
     * Translates this polygon.
     *
     * @param v the vector that defines the translation
     * @return this polygon
     */
    public Polygon translate(Vector3d v) {
        vertices.forEach((vertex) -> {
            vertex.pos = vertex.pos.plus(v);
        });
        return this;
    }

    /**
     * Returns a translated copy of this polygon.
     *
     * <b>Note:</b> this polygon is not modified
     *
     * @param v the vector that defines the translation
     *
     * @return a translated copy of this polygon
     */
    public Polygon translated(Vector3d v) {
        return clone().translate(v);
    }

    /**
     * Applies the specified transformation to this polygon.
     *
     * <b>Note:</b> if the applied transformation performs a mirror operation
     * the vertex order of this polygon is reversed.
     *
     * @param transform the transformation to apply
     *
     * @return this polygon
     */
    public Polygon transform(Transform transform) {

        this.vertices.stream().forEach(
                (v) -> {
                    v.transform(transform);
                });

        if (transform.getScale() < 0) {
            // the transformation includes mirroring. We need to reverse the 
            // vertex order in order to preserve the inside/outside orientation:
            Collections.reverse(vertices);
        }
        return this;
    }

    /**
     * Returns a transformed copy of this polygon.
     *
     * <b>Note:</b> if the applied transformation performs a mirror operation
     * the vertex order of this polygon is reversed.
     *
     * <b>Note:</b> this polygon is not modified
     *
     * @param transform the transformation to apply
     * @return a transformed copy of this polygon
     */
    public Polygon transformed(Transform transform) {
        return clone().transform(transform);
    }

    /**
     * Creates a polygon from the specified point list.
     *
     * @param points the points that define the polygon
     * @param shared shared property storage
     * @return a polygon defined by the specified point list
     */
    public static Polygon fromPoints(List<Vector3d> points,
            PropertyStorage shared) {
        return fromPoints(points, shared, null);
    }

    /**
     * Creates a polygon from the specified point list.
     *
     * @param points the points that define the polygon
     * @return a polygon defined by the specified point list
     */
    public static Polygon fromPoints(List<Vector3d> points) {
        return fromPoints(points, new PropertyStorage(), null);
    }

    /**
     * Creates a polygon from the specified points.
     *
     * @param points the points that define the polygon
     * @return a polygon defined by the specified point list
     */
    public static Polygon fromPoints(Vector3d... points) {
        return fromPoints(Arrays.asList(points), new PropertyStorage(), null);
    }

    /**
     * Creates a polygon from the specified point list.
     *
     * @param points the points that define the polygon
     * @param shared
     * @param plane may be null
     * @return a polygon defined by the specified point list
     */
    private static Polygon fromPoints(
            List<Vector3d> points, PropertyStorage shared, Plane plane) {
        Vector3d normal
                = (plane != null) ? plane.normal.clone() : new Vector3d(0, 0, 0);

        List<Vertex> vertices = new ArrayList<>();

        for (Vector3d p : points) {
            Vector3d vec = p.clone();
            Vertex vertex = new Vertex(vec, normal);
            vertices.add(vertex);
        }

        return new Polygon(vertices, shared);
    }

    

//    private static List<Polygon> concaveToConvex(Polygon concave) {
//        List<Polygon> result = new ArrayList<>();
//
//        Triangulation t = new Triangulation();
//        
//        double[] xv = new double[concave.vertices.size()];
//        double[] yv = new double[concave.vertices.size()];
//        
//        for(int i = 0; i < xv.length;i++) {
//            Vector3d pos = concave.vertices.get(i).pos;
//            xv[i] = pos.x;
//            yv[i] = pos.y;
//        }
//        
//        TriangleTri[] triangles = t.triangulatePolygon(xv, yv, xv.length);
//        
//        for(TriangleTri tr : triangles) {
//            double x1 = tr.x[0];
//            double x2 = tr.x[1];
//            double x3 = tr.x[2];
//            double y1 = tr.y[0];
//            double y2 = tr.y[1];
//            double y3 = tr.y[2];
//            
//            Vertex v1 = new Vertex(new Vector3d(x1, y1), new Vector3d(0, 0));
//            Vertex v2 = new Vertex(new Vector3d(x2, y2), new Vector3d(0, 0));
//            Vertex v3 = new Vertex(new Vector3d(x3, y3), new Vector3d(0, 0));
//            
//            result.add(new Polygon(v1,v2,v3));
//        }
//
//        return result;
//    }
//    private static List<Polygon> concaveToConvex(Polygon concave) {
//        List<Polygon> result = new ArrayList<>();
//
//        //convert polygon to convex polygons
//        EarClippingTriangulator clippingTriangulator = new EarClippingTriangulator();
//        double[] vertexArray = new double[concave.vertices.size() * 2];
//        for (int i = 0; i < vertexArray.length; i += 2) {
//            Vertex v = concave.vertices.get(i / 2);
//            vertexArray[i + 0] = v.pos.x;
//            vertexArray[i + 1] = v.pos.y;
//        }
// 
//        IntArray indices = clippingTriangulator.computeTriangles(vertexArray);
//        
//        System.out.println("indices: " + indices.size + ", vertices: " + vertexArray.length);
//        
//        for (double i : vertexArray) {
//            System.out.println("vertices: " + i);
//        }
//        
//        Vertex[] newPolygonVerts = new Vertex[3];
//
//        int count = 0;
//        for (int i = 0; i < indices.size; i+=2) {
//            double x = vertexArray[indices.items[i]+0];
//            double y = vertexArray[indices.items[i]+1];
//            
//            Vector3d pos = new Vector3d(x, y);
//            Vertex v = new Vertex(pos, new Vector3d(0, 0, 0));
//
//            System.out.println("writing vertex: " + (count));
//            newPolygonVerts[count] = v;
//
//            if (count == 2) {
//                result.add(new Polygon(newPolygonVerts));
//                count = 0;
//            } else {
//                count++;
//            }
//        }
//        
//        System.out.println("---");
//        
//        for (Polygon p : result) {
//            System.out.println(p.toStlString());
//        }
//
//        return result;
//        
////        Point3d[] points = new Point3d[concave.vertices.size()];
////        
////        for (int i = 0; i < points.length;i++) {
////            Vector3d pos = concave.vertices.get(i).pos;
////            points[i] = new Point3d(pos.x, pos.y, pos.z);
////        }
////        
////        QuickHull3D hull = new QuickHull3D();
////        hull.build(points);
////
////        System.out.println("Vertices:");
////        Point3d[] vertices = hull.getVertices();
////        for (int i = 0; i < vertices.length; i++) {
////            Point3d pnt = vertices[i];
////            System.out.println(pnt.x + " " + pnt.y + " " + pnt.z);
////        }
////
////        System.out.println("Faces:");
////        int[][] faceIndices = hull.getFaces();
////        for (int i = 0; i < faceIndices.length; i++) {
////            for (int k = 0; k < faceIndices[i].length; k++) {
////                System.out.print(faceIndices[i][k] + " ");
////            }
////            System.out.println("");
////        }
//
////        return result;
//    }
}
