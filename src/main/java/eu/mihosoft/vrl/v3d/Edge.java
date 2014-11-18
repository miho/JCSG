/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 * @author miho
 */
public class Edge {

    private Vertex p1;
    private Vertex p2;

    public Edge(Vertex p1, Vertex p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    /**
     * @return the p1
     */
    public Vertex getP1() {
        return p1;
    }

    /**
     * @param p1 the p1 to set
     */
    public void setP1(Vertex p1) {
        this.p1 = p1;
    }

    /**
     * @return the p2
     */
    public Vertex getP2() {
        return p2;
    }

    /**
     * @param p2 the p2 to set
     */
    public void setP2(Vertex p2) {
        this.p2 = p2;
    }

    public static List<Edge> fromPolygon(Polygon poly) {
        List<Edge> result = new ArrayList<>();

        for (int i = 0; i < poly.vertices.size(); i++) {
            Edge e = new Edge(poly.vertices.get(i), poly.vertices.get((i + 1) % poly.vertices.size()));

            result.add(e);
        }

        return result;
    }

    public static List<Vertex> toVertices(List<Edge> edges) {
        return edges.stream().map(e -> e.p1).collect(Collectors.toList());
    }

    public static List<Vector3d> toPoints(List<Edge> edges) {
        return edges.stream().map(e -> e.p1.pos).collect(Collectors.toList());
    }

    private static Polygon toPolygon(List<Vector3d> points, Plane plane) {

//        List<Vector3d> points = edges.stream().().map(e -> e.p1.pos).
//                collect(Collectors.toList());
        Polygon p = Polygon.fromPoints(points);

        p.vertices.stream().forEachOrdered((vertex) -> {
            vertex.normal = plane.normal.clone();
        });

        // we try to detect wrong orientation by comparing normals
        if (p.plane.normal.angle(plane.normal) > 0.1) {
            p.flip();
        }

        return p;
    }

    public static List<Polygon> toPolygons(List<Edge> boundaryEdges, Plane plane) {

        List<Vector3d> boundaryPath = new ArrayList<>();

        boolean[] used = new boolean[boundaryEdges.size()];
        Edge edge = boundaryEdges.get(0);
        used[0] = true;
        while (true) {
            Edge finalEdge = edge;

            boundaryPath.add(finalEdge.p1.pos);

            int nextEdgeIndex = boundaryEdges.indexOf(boundaryEdges.stream().
                    filter(e -> finalEdge.p2.equals(e.p1)).findFirst().get());

            if (used[nextEdgeIndex]) {
//                System.out.println("nexIndex: " + nextEdgeIndex);
                break;
            }
//            System.out.print("edge: " + edge.p2.pos);
            edge = boundaryEdges.get(nextEdgeIndex);
//            System.out.println("-> edge: " + edge.p1.pos);
            used[nextEdgeIndex] = true;
        }

        List<Polygon> result = new ArrayList<>();

        System.out.println("#bnd-path-length: " + boundaryPath.size());

        result.add(toPolygon(boundaryPath, plane));

        return result;
    }

    /**
     * Determines whether the specified point lies on tthis edge.
     *
     * @param p point to check
     * @param TOL tolerance
     * @return <code>true</code> if the specified point lies on this line
     * segment; <code>false</code> otherwise
     */
    public boolean contains(Vector3d p, double TOL) {

        double x = p.x;
        double x1 = this.p1.pos.x;
        double x2 = this.p2.pos.x;

        double y = p.y;
        double y1 = this.p1.pos.y;
        double y2 = this.p2.pos.y;

        double z = p.z;
        double z1 = this.p1.pos.z;
        double z2 = this.p2.pos.z;

        double AB = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1) + (z2 - z1) * (z2 - z1));
        double AP = Math.sqrt((x - x1) * (x - x1) + (y - y1) * (y - y1) + (z - z1) * (z - z1));
        double PB = Math.sqrt((x2 - x) * (x2 - x) + (y2 - y) * (y2 - y) + (z2 - z) * (z2 - z));

        return Math.abs(AB - (AP + PB)) < TOL;
    }

    /**
     * Determines whether the specified point lies on tthis edge.
     *
     * @param p point to check
     * @return <code>true</code> if the specified point lies on this line
     * segment; <code>false</code> otherwise
     */
    public boolean contains(Vector3d p) {
        return contains(p, Plane.EPSILON);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.p1);
        hash = 71 * hash + Objects.hashCode(this.p2);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Edge other = (Edge) obj;
        if (!(Objects.equals(this.p1, other.p1) || Objects.equals(this.p2, other.p1))) {
            return false;
        }
        if (!(Objects.equals(this.p2, other.p2) || Objects.equals(this.p1, other.p2))) {
            return false;
        }
        return true;
    }

}
