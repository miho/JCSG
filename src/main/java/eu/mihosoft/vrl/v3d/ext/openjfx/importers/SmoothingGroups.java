/*
 * Copyright (c) 2010, 2014, Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package eu.mihosoft.vrl.v3d.ext.openjfx.importers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import com.sun.javafx.geom.Vec3f;
import javafx.scene.shape.TriangleMesh;

// TODO: Auto-generated Javadoc
/**
 *  Util for converting Normals to Smoothing Groups.
 */
public class SmoothingGroups {
    
    /** The not visited. */
    private BitSet visited, notVisited;
    
    /** The q. */
    private Queue<Integer> q;

    /** The faces. */
    private int[][] faces;
    
    /** The face normals. */
    private int[][] faceNormals;
    
    /** The normals. */
    private float[] normals;
    
    /** The face edges. */
    private Edge[][] faceEdges;

    /**
     * Instantiates a new smoothing groups.
     *
     * @param faces the faces
     * @param faceNormals the face normals
     * @param normals the normals
     */
    public SmoothingGroups(int faces[][], int[][] faceNormals, float[] normals) {
        this.faces = faces;
        this.faceNormals = faceNormals;
        this.normals = normals;
        visited = new BitSet(faces.length);
        notVisited = new BitSet(faces.length);
        notVisited.set(0, faces.length, true);
        q = new LinkedList<Integer>();
    }

    /**
     * Gets the next connected component.
     *
     * @param adjacentFaces the adjacent faces
     * @return the next connected component
     */
    // edge -> [faces]
    private List<Integer> getNextConnectedComponent(Map<Edge, List<Integer>> adjacentFaces) {
        int index = notVisited.previousSetBit(faces.length - 1);
        q.add(index);
        visited.set(index);
        notVisited.set(index, false);
        List<Integer> res = new ArrayList<Integer>();
        while (!q.isEmpty()) {
            Integer faceIndex = q.remove();
            res.add(faceIndex);
            for (Edge edge : faceEdges[faceIndex]) {
                List<Integer> adjFaces = adjacentFaces.get(edge);
                if (adjFaces == null) {
                    continue;
                }
                Integer adjFaceIndex = adjFaces.get(adjFaces.get(0).equals(faceIndex) ? 1 : 0);
                if (!visited.get(adjFaceIndex)) {
                    q.add(adjFaceIndex);
                    visited.set(adjFaceIndex);
                    notVisited.set(adjFaceIndex, false);
                }
            }
        }
        return res;
    }

    /**
     * Checks for next connected component.
     *
     * @return true, if successful
     */
    private boolean hasNextConnectedComponent() {
        return !notVisited.isEmpty();
    }

    /**
     * Compute face edges.
     */
    private void computeFaceEdges() {
        faceEdges = new Edge[faces.length][];
        for (int f = 0; f < faces.length; f++) {
            int[] face = faces[f];
            int[] faceNormal = faceNormals[f];
            int n = face.length/2;
            faceEdges[f] = new Edge[n];
            int from = face[(n-1) * 2];
            int fromNormal = faceNormal[n-1];
            for (int i = 0; i < n; i++) {
                int to = face[i * 2];
                int toNormal = faceNormal[i];
                Edge edge = new Edge(from, to, fromNormal, toNormal);
                faceEdges[f][i] = edge;
                from = to;
                fromNormal = toNormal;
            }
        }
    }
    
    /**
     * Gets the adjacent faces.
     *
     * @return the adjacent faces
     */
    private Map<Edge, List<Integer>> getAdjacentFaces() {
        Map<Edge, List<Integer>> adjacentFaces = new HashMap<Edge, List<Integer>>();
        for (int f = 0; f < faceEdges.length; f++) {
            for (Edge edge : faceEdges[f]) {
                if (!adjacentFaces.containsKey(edge)) {
                    adjacentFaces.put(edge, new ArrayList<Integer>());
                }
                adjacentFaces.get(edge).add(f);
            }
        }
        for (Iterator<Map.Entry<Edge, List<Integer>>> it = adjacentFaces.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Edge, List<Integer>> e = it.next();
            if (e.getValue().size() != 2) {
                // just skip them
                it.remove();
            }
        }
        return adjacentFaces;
    }

    /**
     * Gets the normal.
     *
     * @param index the index
     * @return the normal
     */
    Vec3f getNormal(int index) {
        return new Vec3f(normals[index * 3], normals[index * 3 + 1], normals[index * 3 + 2]);
    }
    
    /** The Constant normalAngle. */
    private static final float normalAngle = 0.9994f; // cos(2)

    /**
     * Checks if is normals equal.
     *
     * @param n1 the n1
     * @param n2 the n2
     * @return true, if is normals equal
     */
    private static boolean isNormalsEqual(Vec3f n1, Vec3f n2) {
        if (n1.x == 1.0e20f || n1.y == 1.0e20f || n1.z == 1.0e20f
                || n2.x == 1.0e20f || n2.y == 1.0e20f || n2.z == 1.0e20f) {
            //System.out.println("unlocked normal found, skipping");
            return false;
        }
        Vec3f myN1 = new Vec3f(n1);
        myN1.normalize();
        Vec3f myN2 = new Vec3f(n2);
        myN2.normalize();
        return myN1.dot(myN2) >= normalAngle;
    }

    /**
     * Gets the smooth edges.
     *
     * @param adjacentFaces the adjacent faces
     * @return the smooth edges
     */
    private Map<Edge, List<Integer>> getSmoothEdges(Map<Edge, List<Integer>> adjacentFaces) {
        Map<Edge, List<Integer>> smoothEdges = new HashMap<Edge, List<Integer>>();

        for (int face = 0; face < faceEdges.length; face++) {
            for (Edge edge : faceEdges[face]) {
                List<Integer> adjFaces = adjacentFaces.get(edge);
                if (adjFaces == null || adjFaces.size() != 2) {
                    // could happen when we skip edges!
                    continue;
                }
                int adjFace = adjFaces.get(adjFaces.get(0) == face ? 1 : 0);
                Edge[] adjFaceEdges = faceEdges[adjFace];
                int adjEdgeInd = Arrays.asList(adjFaceEdges).indexOf(edge);
                if (adjEdgeInd == -1) {
                    System.out.println("Can't find edge " + edge + " in face " + adjFace);
                    System.out.println(Arrays.asList(adjFaceEdges));
                    continue;
                }
                Edge adjEdge = adjFaceEdges[adjEdgeInd];

                if (edge.isSmooth(adjEdge)) {
                    if (!smoothEdges.containsKey(edge)) {
                        smoothEdges.put(edge, adjFaces);
                    }
                }
            }
        }
        return smoothEdges;
    }

    /**
     * Calc conn components.
     *
     * @param smoothEdges the smooth edges
     * @return the list
     */
    private List<List<Integer>> calcConnComponents(Map<Edge, List<Integer>> smoothEdges) {
        //System.out.println("smoothEdges = " + smoothEdges);
        List<List<Integer>> groups = new ArrayList<List<Integer>>();
        while (hasNextConnectedComponent()) {
            List<Integer> smoothGroup = getNextConnectedComponent(smoothEdges);
            groups.add(smoothGroup);
        }
        return groups;
    }

    /**
     * Generate sm groups.
     *
     * @param groups the groups
     * @return the int[]
     */
    private int[] generateSmGroups(List<List<Integer>> groups) {
        int[] smGroups = new int[faceNormals.length];
        int curGroup = 0;
        for (int i = 0; i < groups.size(); i++) {
            List<Integer> list = groups.get(i);
            if (list.size() == 1) {
                smGroups[list.get(0)] = 0;
            } else {
                for (int j = 0; j < list.size(); j++) {
                    Integer faceIndex = list.get(j);
                    smGroups[faceIndex] = 1 << curGroup;
                }
                if (curGroup++ == 31) {
                    curGroup = 0;
                }
            }
        }
        return smGroups;
    }

    /**
     * Calc smooth groups.
     *
     * @return the int[]
     */
    private int[] calcSmoothGroups() {
        computeFaceEdges();
        
        // edge -> [faces]
        Map<Edge, List<Integer>> adjacentFaces = getAdjacentFaces();

        // smooth edge -> [faces]
        Map<Edge, List<Integer>> smoothEdges = getSmoothEdges(adjacentFaces);

        //System.out.println("smoothEdges = " + smoothEdges);
        List<List<Integer>> groups = calcConnComponents(smoothEdges);

        return generateSmGroups(groups);
    }
    
    /**
     * The Class Edge.
     */
    private class Edge {
        
        /** The to. */
        int from, to;
        
        /** The to normal. */
        int fromNormal, toNormal;

        /**
         * Instantiates a new edge.
         *
         * @param from the from
         * @param to the to
         * @param fromNormal the from normal
         * @param toNormal the to normal
         */
        public Edge(int from, int to, int fromNormal, int toNormal) {
            this.from = Math.min(from, to);
            this.to = Math.max(from, to);
            this.fromNormal = Math.min(fromNormal, toNormal);
            this.toNormal = Math.max(fromNormal, toNormal);
        }
        
        /**
         * Checks if is smooth.
         *
         * @param edge the edge
         * @return true, if is smooth
         */
        public boolean isSmooth(Edge edge) {
            boolean smooth = (isNormalsEqual(getNormal(fromNormal), getNormal(edge.fromNormal)) && isNormalsEqual(getNormal(toNormal), getNormal(edge.toNormal))) ||
                    (isNormalsEqual(getNormal(fromNormal), getNormal(edge.toNormal)) && isNormalsEqual(getNormal(toNormal), getNormal(edge.fromNormal)));
            return smooth;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            int hash = 7;
            hash = 41 * hash + this.from;
            hash = 41 * hash + this.to;
            return hash;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Edge other = (Edge) obj;
            if (this.from != other.from) {
                return false;
            }
            if (this.to != other.to) {
                return false;
            }
            return true;
        }
    }

    /**
     * Calculates smoothing groups for data formatted in PolygonMesh style.
     *
     * @param faces An array of faces, where each face consists of an array of vertex and uv indices
     * @param faceNormals An array of face normals, where each face normal consists of an array of normal indices
     * @param normals The array of normals
     * @return An array of smooth groups, where the length of the array is the number of faces
     */
    public static int[] calcSmoothGroups(int[][] faces, int[][] faceNormals, float[] normals) {
        SmoothingGroups smoothGroups = new SmoothingGroups(faces, faceNormals, normals);
        return smoothGroups.calcSmoothGroups();
    }
    
    /**
     * Calculates smoothing groups for data formatted in TriangleMesh style.
     *
     * @param mesh the mesh
     * @param flatFaces An array of faces, where each triangle face is represented by 6 (vertex and uv) indices
     * @param flatFaceNormals An array of face normals, where each triangle face is represented by 3 normal indices
     * @param normals The array of normals
     * @return An array of smooth groups, where the length of the array is the number of faces
     */
    public static int[] calcSmoothGroups(TriangleMesh mesh, int[] flatFaces, int[] flatFaceNormals, float[] normals) {
        int faceElementSize = mesh.getFaceElementSize();
        int[][] faces = new int[flatFaces.length/faceElementSize][faceElementSize];
        for (int f = 0; f < faces.length; f++) {
            for (int e = 0; e < faceElementSize; e++) {
                faces[f][e] = flatFaces[f * faceElementSize + e];
            }
        }
        int pointElementSize = mesh.getPointElementSize();
        int[][] faceNormals = new int[flatFaceNormals.length/pointElementSize][pointElementSize];
        for (int f = 0; f < faceNormals.length; f++) {
            for (int e = 0; e < pointElementSize; e++) {
                faceNormals[f][e] = flatFaceNormals[f * pointElementSize + e];
            }
        }
        SmoothingGroups smoothGroups = new SmoothingGroups(faces, faceNormals, normals);
        return smoothGroups.calcSmoothGroups();
    }
}
