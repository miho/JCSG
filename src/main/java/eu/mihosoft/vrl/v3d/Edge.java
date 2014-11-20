/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author miho
 */
public class Edge {

    private final Vertex p1;
    private final Vertex p2;
    private final Vector3d direction;
    

    public Edge(Vertex p1, Vertex p2) {
        this.p1 = p1;
        this.p2 = p2;
        
        direction = p2.pos.minus(p1.pos).normalized();
    }

    /**
     * @return the p1
     */
    public Vertex getP1() {
        return p1;
    }

//    /**
//     * @param p1 the p1 to set
//     */
//    public void setP1(Vertex p1) {
//        this.p1 = p1;
//    }

    /**
     * @return the p2
     */
    public Vertex getP2() {
        return p2;
    }

//    /**
//     * @param p2 the p2 to set
//     */
//    public void setP2(Vertex p2) {
//        this.p2 = p2;
//    }

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

//        // we try to detect wrong orientation by comparing normals
//        if (p.plane.normal.angle(plane.normal) > 0.1) {
//            p.flip();
//        }
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

    private static class Node<T> {

        private Node parent;
        private final List<Node> children = new ArrayList<>();
        private final int index;
        private final T value;
        private boolean isHole;

        public Node(int index, T value) {
            this.index = index;
            this.value = value;
        }

        public void addChild(int index, T value) {
            children.add(new Node(index, value));
        }

        public List<Node> getChildren() {
            return this.children;
        }

        /**
         * @return the parent
         */
        public Node getParent() {
            return parent;
        }

        /**
         * @return the index
         */
        public int getIndex() {
            return index;
        }

        /**
         * @return the value
         */
        public T getValue() {
            return value;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 67 * hash + this.index;
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
            final Node<?> other = (Node<?>) obj;
            if (this.index != other.index) {
                return false;
            }
            return true;
        }

        public int distanceToRoot() {
            int dist = 0;

            Node pNode = getParent();

            while (pNode != null) {
                dist++;
                pNode = getParent();
            }

            return dist;
        }

        /**
         * @return the isHole
         */
        public boolean isIsHole() {
            return isHole;
        }

        /**
         * @param isHole the isHole to set
         */
        public void setIsHole(boolean isHole) {
            this.isHole = isHole;
        }

    }

    private static final String KEY_POLYGON_HOLES = "jcsg:edge:polygon-holes";

    private static List<Polygon> boundaryPathsWithHoles(List<Polygon> boundaryPaths) {

        List<Polygon> result = boundaryPaths.stream().
                map(p -> p.clone()).collect(Collectors.toList());

        List<List<Integer>> parents = new ArrayList<>();
        boolean[] isHole = new boolean[result.size()];

        for (int i = 0; i < result.size(); i++) {
            Polygon p1 = result.get(i);
            List<Integer> parentsOfI = new ArrayList<>();
            parents.add(parentsOfI);
            for (int j = 0; j < result.size(); j++) {
                Polygon p2 = result.get(j);
                if (i != j) {
                    if (p2.contains(p1)) {
                        parentsOfI.add(j);
                    }
                }
            }
            isHole[i] = parentsOfI.size() % 2 != 0;
        }

        int[] parent = new int[result.size()];

        for (int i = 0; i < parent.length; i++) {
            parent[i] = -1;
        }

        for (int i = 0; i < parents.size(); i++) {
            List<Integer> par = parents.get(i);

            int max = 0;
            int maxIndex = 0;
            for (int pIndex : par) {

                int pSize = parents.get(pIndex).size();

                if (max < pSize) {
                    max = pSize;
                    maxIndex = pIndex;
                }
            }

            parent[i] = maxIndex;

            if (!isHole[maxIndex] && isHole[i]) {

                List<Polygon> holes;

                Optional<List<Polygon>> holesOpt = result.get(maxIndex).
                        getStorage().getValue(KEY_POLYGON_HOLES);

                if (holesOpt.isPresent()) {
                    holes = holesOpt.get();
                } else {
                    holes = new ArrayList<>();
                    result.get(maxIndex).getStorage().
                            set(KEY_POLYGON_HOLES, holes);
                }

                holes.add(result.get(i));
            }
        }

        return result;
    }

    /**
     * Returns a list of all boundary paths.
     *
     * @param boundaryEdges boundary edges (all paths must be closed)
     * @return
     */
    private static List<Polygon> boundaryPaths(List<Edge> boundaryEdges) {
        List<Polygon> result = new ArrayList<>();

        boolean[] used = new boolean[boundaryEdges.size()];
        int startIndex = 0;
        Edge edge = boundaryEdges.get(startIndex);
        used[startIndex] = true;

        while (startIndex > 0) {
            List<Vector3d> boundaryPath = new ArrayList<>();
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
            result.add(Polygon.fromPoints(boundaryPath));

            startIndex = nextUnused(used);
            edge = boundaryEdges.get(startIndex);
            used[startIndex] = true;
        }

        return result;
    }

    /**
     * Returns the next unused index as specified in the given boolean array.
     *
     * @param usage the usage array
     * @return the next unused index or a value &lt; 0 if all indices are used
     */
    private static int nextUnused(boolean[] usage) {
        for (int i = 0; i < usage.length; i++) {
            if (usage[i] == false) {
                return i;
            }
        }

        return -1;
    }

    public static List<Polygon> _toPolygons(List<Edge> boundaryEdges, Plane plane) {

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

    public Vector3d getDirection() {
        return direction;
    }

    /**
     * Returns the the point of this edge that is closest to the specified edge. 
     * 
     * <b>NOTE:</b> returns an empty optional if the edges are parallel
     * 
     * @param e the edge to check
     * @return the the point of this edge that is closest to the specified edge
     */
    public Optional<Vector3d> getClosestPoint(Edge e) {
        
        // algorithm from:
        // org.apache.commons.math3.geometry.euclidean.threed/Line.java.html

        Vector3d ourDir = getDirection();

        double cos = ourDir.dot(e.getDirection());
        double n = 1 - cos * cos;
        
        if (n < Plane.EPSILON) {
            // the lines are parallel
            return Optional.empty();
        }
        
        final Vector3d delta = p2.pos.minus(p1.pos);
        final double norm2 = delta.magnitudeSq();

        // line points above the origin
        Vector3d thisZero = p1.pos.plus(delta.times(-p1.pos.dot(delta)/ norm2));
        Vector3d eZero = e.p1.pos.plus(delta.times(-e.p1.pos.dot(delta)/ norm2));
        
        final Vector3d delta0 = eZero.minus(thisZero);
        final double a = delta0.dot(direction);
        final double b = delta0.dot(e.direction);
        
        Vector3d closestP = thisZero.plus(direction.times(a-b * cos));
        
        if (!contains(closestP)) {
            if (closestP.minus(p1.pos).magnitudeSq()
                    < closestP.minus(p2.pos).magnitudeSq()) {
                Optional.of(closestP);
            }
        }

        return Optional.of(closestP);
    }
    
    
    /**
     * Returns the intersection point between this edge and the specified edge.
     * 
     *  <b>NOTE:</b> returns an empty optional if the edges are parallel or if
     * the intersection point is not inside the specified edge segment
     * 
     * @param e edge to intersect
     * @return the intersection point between this edge and the specified edge
     */
    public Optional<Vector3d> getIntersection(Edge e) {
        Optional<Vector3d> closestPOpt = getClosestPoint(e);
        
        if (!closestPOpt.isPresent()) {
            // edges are parallel
            return Optional.empty();
        }
        
        Vector3d closestP = closestPOpt.get();
        
        if (e.contains(closestP)) {
            return closestPOpt;
        } else {
            // intersection point outside of segment
            return Optional.empty();
        }
    }

}
