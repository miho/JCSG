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
package eu.mihosoft.vrl.v3d.ext.openjfx.shape3d.symbolic;

import eu.mihosoft.vrl.v3d.ext.openjfx.shape3d.SubdivisionMesh.BoundaryMode;
import eu.mihosoft.vrl.v3d.ext.openjfx.shape3d.SubdivisionMesh.MapBorderMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.geometry.Point2D;


// TODO: Auto-generated Javadoc
/**
 * Data structure builder for Catmull Clark subdivision surface.
 */
public class SymbolicSubdivisionBuilder {
    
    /** The old mesh. */
    private SymbolicPolygonMesh oldMesh;
    
    /** The edge infos. */
    private Map<Edge, EdgeInfo> edgeInfos;
    
    /** The face infos. */
    private FaceInfo[] faceInfos;
    
    /** The point infos. */
    private PointInfo[] pointInfos;
    
    /** The points. */
    private SubdividedPointArray points;
    
    /** The tex coords. */
    private float[] texCoords;
    
    /** The reindex. */
    private int[] reindex;
    
    /** The new tex coord index. */
    private int newTexCoordIndex;
    
    /** The boundary mode. */
    private BoundaryMode boundaryMode;
    
    /** The map border mode. */
    private MapBorderMode mapBorderMode;

    /**
     * Instantiates a new symbolic subdivision builder.
     *
     * @param oldMesh the old mesh
     * @param boundaryMode the boundary mode
     * @param mapBorderMode the map border mode
     */
    public SymbolicSubdivisionBuilder(SymbolicPolygonMesh oldMesh, BoundaryMode boundaryMode, MapBorderMode mapBorderMode) {
        this.oldMesh = oldMesh;
        this.boundaryMode = boundaryMode;
        this.mapBorderMode = mapBorderMode;
    }
    
    /**
     * Subdivide.
     *
     * @return the symbolic polygon mesh
     */
    public SymbolicPolygonMesh subdivide() {
        collectInfo();
        
        texCoords = new float[(oldMesh.getNumEdgesInFaces() * 3 + oldMesh.faces.length) * 2];
        int[][] faces = new int[oldMesh.getNumEdgesInFaces()][8];
        int[] faceSmoothingGroups = new int[oldMesh.getNumEdgesInFaces()];
        newTexCoordIndex = 0;
        reindex = new int[oldMesh.points.numPoints]; // indexes incremented by 1, 0 reserved for empty
        
        // face points first
        int newFacesInd = 0;
        for (int f = 0; f < oldMesh.faces.length; f++) {
            FaceInfo faceInfo = faceInfos[f];
            int[] oldFaces = oldMesh.faces[f];
            for (int p = 0; p < oldFaces.length; p += 2) {
                faces[newFacesInd][4] = getPointNewIndex(faceInfo);
                faces[newFacesInd][5] = getTexCoordNewIndex(faceInfo);
                faceSmoothingGroups[newFacesInd] = oldMesh.faceSmoothingGroups[f];
                newFacesInd++;
            }
        }
        // then, add edge points
        newFacesInd = 0;
        for (int f = 0; f < oldMesh.faces.length; f++) {
            FaceInfo faceInfo = faceInfos[f];
            int[] oldFaces = oldMesh.faces[f];
            for (int p = 0; p < oldFaces.length; p += 2) {
                faces[newFacesInd][2] = getPointNewIndex(faceInfo, (p / 2 + 1) % faceInfo.edges.length);
                faces[newFacesInd][3] = getTexCoordNewIndex(faceInfo, (p / 2 + 1) % faceInfo.edges.length);
                faces[newFacesInd][6] = getPointNewIndex(faceInfo, p / 2);
                faces[newFacesInd][7] = getTexCoordNewIndex(faceInfo, p / 2);
                newFacesInd++;
            }
        }
        // finally, add control points
        newFacesInd = 0;
        for (int f = 0; f < oldMesh.faces.length; f++) {
            FaceInfo faceInfo = faceInfos[f];
            int[] oldFaces = oldMesh.faces[f];
            for (int p = 0; p < oldFaces.length; p += 2) {
                faces[newFacesInd][0] = getPointNewIndex(oldFaces[p]);
                faces[newFacesInd][1] = getTexCoordNewIndex(faceInfo, oldFaces[p], oldFaces[p+1]);
                newFacesInd++;
            }
        }
        
        SymbolicPolygonMesh newMesh = new SymbolicPolygonMesh(points, texCoords, faces, faceSmoothingGroups);
        return newMesh;
    }
    
    /**
     * Subdivide.
     *
     * @param oldMesh the old mesh
     * @param boundaryMode the boundary mode
     * @param mapBorderMode the map border mode
     * @return the symbolic polygon mesh
     */
    public static SymbolicPolygonMesh subdivide(SymbolicPolygonMesh oldMesh, BoundaryMode boundaryMode, MapBorderMode mapBorderMode) {
        SymbolicSubdivisionBuilder subdivision = new SymbolicSubdivisionBuilder(oldMesh, boundaryMode, mapBorderMode);
        return subdivision.subdivide();
    }

    /**
     * Adds the edge.
     *
     * @param edge the edge
     * @param faceInfo the face info
     */
    private void addEdge(Edge edge, FaceInfo faceInfo) {
        EdgeInfo edgeInfo = edgeInfos.get(edge);
        if (edgeInfo == null) {
            edgeInfo = new EdgeInfo();
            edgeInfo.edge = edge;
            edgeInfos.put(edge, edgeInfo);
        }
        edgeInfo.faces.add(faceInfo);
    }

    /**
     * Adds the point.
     *
     * @param point the point
     * @param faceInfo the face info
     * @param edge the edge
     */
    private void addPoint(int point, FaceInfo faceInfo, Edge edge) {
        PointInfo pointInfo = pointInfos[point];
        if (pointInfo == null) {
            pointInfo = new PointInfo();
            pointInfos[point] = pointInfo;
        }
        pointInfo.edges.add(edge);
        pointInfo.faces.add(faceInfo);
    }
    
    /**
     * Adds the point.
     *
     * @param point the point
     * @param edge the edge
     */
    private void addPoint(int point, Edge edge) {
        PointInfo pointInfo = pointInfos[point];
        if (pointInfo == null) {
            pointInfo = new PointInfo();
            pointInfos[point] = pointInfo;
        }
        pointInfo.edges.add(edge);
    }

    /**
     * Collect info.
     */
    private void collectInfo() {
        edgeInfos = new HashMap<>(oldMesh.faces.length * 2);
        faceInfos = new FaceInfo[oldMesh.faces.length];
        pointInfos = new PointInfo[oldMesh.points.numPoints];
        
        for (int f = 0; f < oldMesh.faces.length; f++) {
            int[] face = oldMesh.faces[f];
            int n = face.length / 2;
            FaceInfo faceInfo = new FaceInfo(n);
            faceInfos[f] = faceInfo;
            if (n < 3) {
                continue;
            }
            int from = face[(n-1) * 2];
            int texFrom = face[(n-1) * 2 + 1];
            double fu, fv;
            double tu, tv;
            double u = 0, v = 0;
            fu = oldMesh.texCoords[texFrom * 2];
            fv = oldMesh.texCoords[texFrom * 2 + 1];
            for (int i = 0; i < n; i++) {
                int to = face[i * 2];
                int texTo = face[i * 2 + 1];
                tu = oldMesh.texCoords[texTo * 2];
                tv = oldMesh.texCoords[texTo * 2 + 1];
                Point2D midTexCoord = new Point2D((fu + tu) / 2, (fv + tv) / 2);
                Edge edge = new Edge(from, to);
                faceInfo.edges[i] = edge;
                faceInfo.edgeTexCoords[i] = midTexCoord;
                addEdge(edge, faceInfo);
                addPoint(to, faceInfo, edge);
                addPoint(from, edge);
                fu = tu; fv = tv;
                u += tu / n; v += tv / n;
                from = to;
                texFrom = texTo;
            }
            faceInfo.texCoord = new Point2D(u, v);
        }
        
        points = new SubdividedPointArray(oldMesh.points, oldMesh.points.numPoints + faceInfos.length + edgeInfos.size(), boundaryMode);
        
        for (int f = 0; f < oldMesh.faces.length; f++) {
            int[] face = oldMesh.faces[f];
            int n = face.length / 2;
            int[] faceVertices = new int[n];
            for (int i = 0; i < n; i++) {
                faceVertices[i] = face[i * 2];
            }
            faceInfos[f].facePoint = points.addFacePoint(faceVertices);
        }
        
        for(EdgeInfo edgeInfo : edgeInfos.values()) {
            int[] edgeFacePoints = new int[edgeInfo.faces.size()];
            for (int f = 0; f < edgeInfo.faces.size(); f++) {
                edgeFacePoints[f] = edgeInfo.faces.get(f).facePoint;
            }
            edgeInfo.edgePoint = points.addEdgePoint(edgeFacePoints, edgeInfo.edge.from, edgeInfo.edge.to, edgeInfo.isBoundary());
        }
    }

    /**
     * Calc control point.
     *
     * @param srcPointIndex the src point index
     * @return the int
     */
    private int calcControlPoint(int srcPointIndex) {
        PointInfo pointInfo = pointInfos[srcPointIndex];
        int origPoint = srcPointIndex;
        
        int[] facePoints = new int[pointInfo.faces.size()];
        for (int f = 0; f < facePoints.length; f++) {
            facePoints[f] = pointInfo.faces.get(f).facePoint;
        }
        int[] edgePoints = new int[pointInfo.edges.size()];
        boolean[] isEdgeBoundary = new boolean[pointInfo.edges.size()];
        int[] fromEdgePoints = new int[pointInfo.edges.size()];
        int[] toEdgePoints = new int[pointInfo.edges.size()];
        int i = 0;
        for (Edge edge : pointInfo.edges) {
            EdgeInfo edgeInfo = edgeInfos.get(edge);
            edgePoints[i] = edgeInfo.edgePoint;
            isEdgeBoundary[i] = edgeInfo.isBoundary();
            fromEdgePoints[i] = edgeInfo.edge.from;
            toEdgePoints[i] = edgeInfo.edge.to;
            i++;
        }
        int destPointIndex = points.addControlPoint(facePoints, edgePoints, fromEdgePoints, toEdgePoints, isEdgeBoundary, origPoint, pointInfo.isBoundary(), pointInfo.hasInternalEdge());
        return destPointIndex;
    }

    /**
     * Calc control tex coord.
     *
     * @param faceInfo the face info
     * @param srcPointIndex the src point index
     * @param srcTexCoordIndex the src tex coord index
     * @param destTexCoordIndex the dest tex coord index
     */
    private void calcControlTexCoord(FaceInfo faceInfo, int srcPointIndex, int srcTexCoordIndex, int destTexCoordIndex){
        PointInfo pointInfo = pointInfos[srcPointIndex];
        boolean pointBelongsToCrease = oldMesh.points instanceof OriginalPointArray;
        if ((mapBorderMode == MapBorderMode.SMOOTH_ALL && (pointInfo.isBoundary() || pointBelongsToCrease)) || 
                (mapBorderMode == MapBorderMode.SMOOTH_INTERNAL && !pointInfo.hasInternalEdge())) {
            double u = oldMesh.texCoords[srcTexCoordIndex * 2] / 2;
            double v = oldMesh.texCoords[srcTexCoordIndex * 2 + 1] / 2;
            for (int i = 0; i < faceInfo.edges.length; i++) {
                if ((faceInfo.edges[i].to == srcPointIndex) || (faceInfo.edges[i].from == srcPointIndex)) {
                    u += faceInfo.edgeTexCoords[i].getX() / 4;
                    v += faceInfo.edgeTexCoords[i].getY() / 4;
                }
            }
            texCoords[destTexCoordIndex * 2] = (float) u;
            texCoords[destTexCoordIndex * 2 + 1] = (float) v;
        } else {
            texCoords[destTexCoordIndex * 2] = oldMesh.texCoords[srcTexCoordIndex * 2];
            texCoords[destTexCoordIndex * 2 + 1] = oldMesh.texCoords[srcTexCoordIndex * 2 + 1];
        }
    }

    /**
     * Gets the point new index.
     *
     * @param srcPointIndex the src point index
     * @return the point new index
     */
    private int getPointNewIndex(int srcPointIndex) {
        int destPointIndex = reindex[srcPointIndex] - 1;
        if (destPointIndex == -1) {
            destPointIndex = calcControlPoint(srcPointIndex);
            reindex[srcPointIndex] = destPointIndex + 1;
        }
        return destPointIndex;
    }
    
    /**
     * Gets the point new index.
     *
     * @param faceInfo the face info
     * @param edgeInd the edge ind
     * @return the point new index
     */
    private int getPointNewIndex(FaceInfo faceInfo, int edgeInd) {
        Edge edge = faceInfo.edges[edgeInd];
        EdgeInfo edgeInfo = edgeInfos.get(edge);
        return edgeInfo.edgePoint;
    }

    /**
     * Gets the point new index.
     *
     * @param faceInfo the face info
     * @return the point new index
     */
    private int getPointNewIndex(FaceInfo faceInfo) {
        return faceInfo.facePoint;
    }

    /**
     * Gets the tex coord new index.
     *
     * @param faceInfo the face info
     * @param srcPointIndex the src point index
     * @param srcTexCoordIndex the src tex coord index
     * @return the tex coord new index
     */
    private int getTexCoordNewIndex(FaceInfo faceInfo, int srcPointIndex, int srcTexCoordIndex) {
        int destTexCoordIndex = newTexCoordIndex;
        newTexCoordIndex++;
        calcControlTexCoord(faceInfo, srcPointIndex, srcTexCoordIndex, destTexCoordIndex);
        return destTexCoordIndex;
    }
    
    /**
     * Gets the tex coord new index.
     *
     * @param faceInfo the face info
     * @param edgeInd the edge ind
     * @return the tex coord new index
     */
    private int getTexCoordNewIndex(FaceInfo faceInfo, int edgeInd) {
        int destTexCoordIndex = newTexCoordIndex;
        newTexCoordIndex++;
        texCoords[destTexCoordIndex * 2] = (float) faceInfo.edgeTexCoords[edgeInd].getX();
        texCoords[destTexCoordIndex * 2 + 1] = (float) faceInfo.edgeTexCoords[edgeInd].getY();
        return destTexCoordIndex;
    }
    
    /**
     * Gets the tex coord new index.
     *
     * @param faceInfo the face info
     * @return the tex coord new index
     */
    private int getTexCoordNewIndex(FaceInfo faceInfo) {
        int destTexCoordIndex = faceInfo.newTexCoordIndex - 1;
        if (destTexCoordIndex == -1) {
            destTexCoordIndex = newTexCoordIndex;
            faceInfo.newTexCoordIndex = destTexCoordIndex + 1;
            newTexCoordIndex++;
            texCoords[destTexCoordIndex * 2] = (float) faceInfo.texCoord.getX();
            texCoords[destTexCoordIndex * 2 + 1] = (float) faceInfo.texCoord.getY();
        }
        return destTexCoordIndex;
    }

    /**
     * The Class Edge.
     */
    private static class Edge {
        
        /** The to. */
        int from, to;

        /**
         * Instantiates a new edge.
         *
         * @param from the from
         * @param to the to
         */
        public Edge(int from, int to) {
            this.from = Math.min(from, to);
            this.to = Math.max(from, to);
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
     * The Class EdgeInfo.
     */
    private static class EdgeInfo {
        
        /** The edge. */
        Edge edge;
        
        /** The edge point. */
        int edgePoint;
        
        /** The faces. */
        List<FaceInfo> faces = new ArrayList<>(2);
        
        /**
         * an edge is in the boundary if it has only one adjacent face.
         *
         * @return true, if is boundary
         */
        public boolean isBoundary() {
            return faces.size() == 1;
        }
    }
    
    /**
     * The Class PointInfo.
     */
    private class PointInfo {
        
        /** The faces. */
        List<FaceInfo> faces = new ArrayList<>(4);
        
        /** The edges. */
        Set<Edge> edges = new HashSet<>(4);
        
        /**
         * A point is in the boundary if any of its adjacent edges is in the boundary.
         *
         * @return true, if is boundary
         */
        public boolean isBoundary() {
            for (Edge edge : edges) {
                EdgeInfo edgeInfo = edgeInfos.get(edge);
                if (edgeInfo.isBoundary())
                    return true;
            }
            return false;
        }
        
        /**
         * A point is internal if at least one of its adjacent edges is not in the boundary.
         *
         * @return true, if successful
         */
        public boolean hasInternalEdge() {
            for (Edge edge : edges) {
                EdgeInfo edgeInfo = edgeInfos.get(edge);
                if (!edgeInfo.isBoundary())
                    return true;
            }
            return false;
        }
    }
    
    /**
     * The Class FaceInfo.
     */
    private static class FaceInfo {
        
        /** The face point. */
        int facePoint;
        
        /** The tex coord. */
        Point2D texCoord;
        
        /** The new tex coord index. */
        int newTexCoordIndex;
        
        /** The edges. */
        Edge[] edges;
        
        /** The edge tex coords. */
        Point2D[] edgeTexCoords;

        /**
         * Instantiates a new face info.
         *
         * @param n the n
         */
        public FaceInfo(int n) {
            edges = new Edge[n];
            edgeTexCoords = new Point2D[n];
        }
    }
}