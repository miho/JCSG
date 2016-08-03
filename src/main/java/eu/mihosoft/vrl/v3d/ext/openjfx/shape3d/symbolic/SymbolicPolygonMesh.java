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

import eu.mihosoft.vrl.v3d.ext.openjfx.shape3d.PolygonMesh;


// TODO: Auto-generated Javadoc
/**
 * Polygon mesh where the points are symbolic. That is, the values of the 
 * points depend on other variables and they can be updated appropriately.
 */
public class SymbolicPolygonMesh {
    
    /** The points. */
    public SymbolicPointArray points;
    
    /** The tex coords. */
    public float[] texCoords;
    
    /** The faces. */
    public int[][] faces;
    
    /** The face smoothing groups. */
    public int[] faceSmoothingGroups;
    
    /** The num edges in faces. */
    private int numEdgesInFaces = -1;

    /**
     * Instantiates a new symbolic polygon mesh.
     *
     * @param points the points
     * @param texCoords the tex coords
     * @param faces the faces
     * @param faceSmoothingGroups the face smoothing groups
     */
    public SymbolicPolygonMesh(SymbolicPointArray points, float[] texCoords, int[][] faces, int[] faceSmoothingGroups) {
        this.points = points;
        this.texCoords = texCoords;
        this.faces = faces;
        this.faceSmoothingGroups = faceSmoothingGroups;
    }
    
    /**
     * Instantiates a new symbolic polygon mesh.
     *
     * @param mesh the mesh
     */
    public SymbolicPolygonMesh(PolygonMesh mesh) {
        this.points = new OriginalPointArray(mesh);
        this.texCoords = mesh.getTexCoords().toArray(this.texCoords);
        this.faces = mesh.faces;
        this.faceSmoothingGroups = mesh.getFaceSmoothingGroups().toArray(null);
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
}
