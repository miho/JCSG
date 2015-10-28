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
package eu.mihosoft.vrl.v3d.ext.openjfx.shape3d;

import javafx.collections.FXCollections;
import javafx.collections.ObservableFloatArray;
import javafx.collections.ObservableIntegerArray;

// TODO: Auto-generated Javadoc
/**
 * A Mesh where each face can be a Polygon
 * 
 * can convert to using ObservableIntegerArray.
 */
public class PolygonMesh {
    
    /** The points. */
    private final ObservableFloatArray points = FXCollections.observableFloatArray();
    
    /** The tex coords. */
    private final ObservableFloatArray texCoords = FXCollections.observableFloatArray();
    
    /** The faces. */
    public int[][] faces = new int[0][0];
    
    /** The face smoothing groups. */
    private final ObservableIntegerArray faceSmoothingGroups = FXCollections.observableIntegerArray();
    
    /** The num edges in faces. */
    protected int numEdgesInFaces = -1; // TODO invalidate automatically by listening to faces (whenever it is an observable)

    /**
     * Instantiates a new polygon mesh.
     */
    public PolygonMesh() {}

    /**
     * Instantiates a new polygon mesh.
     *
     * @param points the points
     * @param texCoords the tex coords
     * @param faces the faces
     */
    public PolygonMesh(float[] points, float[] texCoords, int[][] faces) {
        this.points.addAll(points);
        this.texCoords.addAll(texCoords);
        this.faces = faces;
    }

    /**
     * Gets the points.
     *
     * @return the points
     */
    public ObservableFloatArray getPoints() {
        return points;
    }
    
    /**
     * Gets the tex coords.
     *
     * @return the tex coords
     */
    public ObservableFloatArray getTexCoords() {
        return texCoords;
    }
    
    /**
     * Gets the face smoothing groups.
     *
     * @return the face smoothing groups
     */
    public ObservableIntegerArray getFaceSmoothingGroups() {
        return faceSmoothingGroups;
    }
     
    /**
     * Gets the num edges in faces.
     *
     * @return the num edges in faces
     */
    public int getNumEdgesInFaces() {
        if (numEdgesInFaces == -1) {
            numEdgesInFaces = 0;
            for(int[] face : faces) {
                numEdgesInFaces += face.length;
            }
           numEdgesInFaces /= 2;
        }
        return numEdgesInFaces;
    }
    
    /** The Constant NUM_COMPONENTS_PER_POINT. */
    // TODO: Hardcode to constants for FX 8 (only one vertex format)
    private static final int NUM_COMPONENTS_PER_POINT = 3;
    
    /** The Constant NUM_COMPONENTS_PER_TEXCOORD. */
    private static final int NUM_COMPONENTS_PER_TEXCOORD = 2;
    
    /** The Constant NUM_COMPONENTS_PER_FACE. */
    private static final int NUM_COMPONENTS_PER_FACE = 6;

    /**
     * Gets the point element size.
     *
     * @return the point element size
     */
    public int getPointElementSize() {
        return NUM_COMPONENTS_PER_POINT;
    }

    /**
     * Gets the tex coord element size.
     *
     * @return the tex coord element size
     */
    public int getTexCoordElementSize() {
        return NUM_COMPONENTS_PER_TEXCOORD;
    }

    /**
     * Gets the face element size.
     *
     * @return the face element size
     */
    public int getFaceElementSize() {
        return NUM_COMPONENTS_PER_FACE;
    }
}
