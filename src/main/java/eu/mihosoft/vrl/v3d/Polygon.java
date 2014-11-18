/**
 * Polygon.java
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
import java.util.Collections;
import java.util.List;
import eu.mihosoft.vrl.v3d.ext.org.poly2tri.PolygonUtil;

/**
 * Represents a convex polygon.
 *
 * Each convex polygon has a {@code shared} property, which is shared between
 * all polygons that are clones of each other or where split from the same
 * polygon. This can be used to define per-polygon properties (such as surface
 * color).
 */
public final class Polygon {

    /**
     * Polygon vertices
     */
    public final List<Vertex> vertices;
    /**
     * Shared property (can be used for shared color etc.).
     */
    private PropertyStorage shared;
    /**
     * Plane defined by this polygon.
     *
     * <b>Note:</b> uses first three vertices to define the plane.
     */
    public final Plane plane;

    void setStorage(PropertyStorage storage) {
        this.shared = storage;
    }

    /**
     * Decomposes the specified concave polygon into convex polygons.
     *
     * @param points the points that define the polygon
     * @return the decomposed concave polygon (list of convex polygons)
     */
    public static List<Polygon> fromConcavePoints(Vector3d... points) {
        Polygon p = fromPoints(points);

        return PolygonUtil.concaveToConvex(p);
    }

    /**
     * Decomposes the specified concave polygon into convex polygons.
     *
     * @param points the points that define the polygon
     * @return the decomposed concave polygon (list of convex polygons)
     */
    public static List<Polygon> fromConcavePoints(List<Vector3d> points) {
        Polygon p = fromPoints(points);

        return PolygonUtil.concaveToConvex(p);
    }

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
        return new Polygon(newVertices, getStorage());
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

        Vector3d a = this.vertices.get(0).pos;
        Vector3d b = this.vertices.get(1).pos;
        Vector3d c = this.vertices.get(2).pos;

        this.plane.normal = b.minus(a).cross(c.minus(a));

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
                }
        );

        Vector3d a = this.vertices.get(0).pos;
        Vector3d b = this.vertices.get(1).pos;
        Vector3d c = this.vertices.get(2).pos;

        this.plane.normal = b.minus(a).cross(c.minus(a)).normalized();
        this.plane.dist = this.plane.normal.dot(a);

        if (transform.isMirror()) {
            // the transformation includes mirroring. flip polygon
            flip();

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

    /**
     * Returns the bounds of this polygon.
     *
     * @return bouds of this polygon
     */
    public Bounds getBounds() {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;

        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < vertices.size(); i++) {

            Vertex vert = vertices.get(i);

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

        return new Bounds(
                new Vector3d(minX, minY, minZ),
                new Vector3d(maxX, maxY, maxZ));
    }

    public boolean contains(Vector3d p) {
        // taken from http://www.java-gaming.org/index.php?topic=26013.0
        // and http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
        double px = p.x;
        double py = p.y;
        boolean oddNodes = false;
        double x2 = vertices.get(vertices.size() - 1).pos.x;
        double y2 = vertices.get(vertices.size() - 1).pos.y;
        double x1, y1;
        for (int i = 0; i < vertices.size(); x2 = x1, y2 = y1, ++i) {
            x1 = vertices.get(i).pos.x;
            y1 = vertices.get(i).pos.y;
            if (((y1 < py) && (y2 >= py))
                    || (y1 >= py) && (y2 < py)) {
                if ((py - y1) / (y2 - y1)
                        * (x2 - x1) < (px - x1)) {
                    oddNodes = !oddNodes;
                }
            }
        }
        return oddNodes;
    }
    
    public boolean contains(Polygon p) {
        
        for (Vertex v : p.vertices) {
            if (!contains(v.pos)) {
                return false;
            }
        }
        
        return true;
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
    /**
     * @return the shared
     */
    public PropertyStorage getStorage() {

        if (shared == null) {
            shared = new PropertyStorage();
        }

        return shared;
    }
}
