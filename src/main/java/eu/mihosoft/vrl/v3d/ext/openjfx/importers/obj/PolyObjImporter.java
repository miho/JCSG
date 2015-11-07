/*
 * Copyright (c) 2008, 2014, Oracle and/or its affiliates.
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
package eu.mihosoft.vrl.v3d.ext.openjfx.importers.obj;



import eu.mihosoft.vrl.v3d.ext.openjfx.importers.SmoothingGroups;
import eu.mihosoft.vrl.v3d.ext.openjfx.shape3d.PolygonMesh;
import eu.mihosoft.vrl.v3d.ext.openjfx.shape3d.PolygonMeshView;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;


import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


// TODO: Auto-generated Javadoc
/**
 * OBJ object loader.
 */
public class PolyObjImporter {

    /**
     * Vertex index.
     *
     * @param vertexIndex the vertex index
     * @return the int
     */
    private int vertexIndex(int vertexIndex) {
        if (vertexIndex < 0) {
            return vertexIndex + vertexes.size() / 3;
        } else {
            return vertexIndex - 1;
        }
    }

    /**
     * Uv index.
     *
     * @param uvIndex the uv index
     * @return the int
     */
    private int uvIndex(int uvIndex) {
        if (uvIndex < 0) {
            return uvIndex + uvs.size() / 2;
        } else {
            return uvIndex - 1;
        }
    }
    
    /**
     * Normal index.
     *
     * @param normalIndex the normal index
     * @return the int
     */
    private int normalIndex(int normalIndex) {
        if (normalIndex < 0) {
            return normalIndex + normals.size() / 3;
        } else {
            return normalIndex - 1;
        }
    }

    /** The debug. */
    private static boolean debug = false;
    
    /** The scale. */
    private static float scale = 1;
    
    /** The flat xz. */
    private static boolean flatXZ = false;

    /**
     * Log.
     *
     * @param string the string
     */
    static void log(String string) {
        if (debug) {
            System.out.println(string);
        }
    }

    /**
     * Gets the meshes.
     *
     * @return the meshes
     */
    public Set<String> getMeshes() {
        return meshes.keySet();
    }

    /** The meshes. */
    private Map<String, PolygonMesh> meshes = new HashMap<>();
    
    /** The materials. */
    private Map<String, Material> materials = new HashMap<>();
    
    /** The material library. */
    private List<Map<String, Material>> materialLibrary = new ArrayList<>();
    
    /** The obj filename. */
    private String objFilename;

    /**
     * Instantiates a new poly obj importer.
     *
     * @param filename the filename
     * @throws FileNotFoundException the file not found exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public PolyObjImporter(String filename) throws FileNotFoundException, IOException {
        this.objFilename = filename;
        log("Reading filename = " + filename);;
        read(new URL(filename).openStream());
    }

    /**
     * Instantiates a new poly obj importer.
     *
     * @param inputStream the input stream
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public PolyObjImporter(InputStream inputStream) throws IOException {
        read(inputStream);
    }
    
    /**
     * Gets the mesh.
     *
     * @return the mesh
     */
    public PolygonMesh getMesh() {
        return meshes.values().iterator().next();
    }
    
    /**
     * Gets the material.
     *
     * @return the material
     */
    public Material getMaterial() {
        return materials.values().iterator().next();
    }
    
    /**
     * Gets the mesh.
     *
     * @param key the key
     * @return the mesh
     */
    public PolygonMesh getMesh(String key) {
        return meshes.get(key);
    }

    /**
     * Gets the material.
     *
     * @param key the key
     * @return the material
     */
    public Material getMaterial(String key) {
        return materials.get(key);
    }
    
    /**
     * Builds the polygon mesh view.
     *
     * @param key the key
     * @return the polygon mesh view
     */
    public PolygonMeshView buildPolygonMeshView(String key) {
        PolygonMeshView polygonMeshView = new PolygonMeshView();
        polygonMeshView.setId(key);
        polygonMeshView.setMaterial(materials.get(key));
        polygonMeshView.setMesh(meshes.get(key));
//        polygonMeshView.setCullFace(CullFace.NONE); TODO
        return polygonMeshView;
    }
    
    /**
     * Sets the debug.
     *
     * @param debug the new debug
     */
    public static void setDebug(boolean debug) {
        PolyObjImporter.debug = debug;
    }

    /**
     * Sets the scale.
     *
     * @param scale the new scale
     */
    public static void setScale(float scale) {
        PolyObjImporter.scale = scale;
    }

    /** The vertexes. */
    private FloatArrayList vertexes = new FloatArrayList();
    
    /** The uvs. */
    private FloatArrayList uvs = new FloatArrayList();
    
    /** The faces. */
    private List<int[]> faces = new ArrayList<>();
    
    /** The smoothing groups. */
    private IntegerArrayList smoothingGroups = new IntegerArrayList();
    
    /** The normals. */
    private FloatArrayList normals = new FloatArrayList();
    
    /** The face normals. */
    private List<int[]> faceNormals = new ArrayList<>();
    
    /** The material. */
    private Material material = new PhongMaterial(Color.WHITE);
    
    /** The faces start. */
    private int facesStart = 0;
    
    /** The faces normal start. */
    private int facesNormalStart = 0;
    
    /** The smoothing groups start. */
    private int smoothingGroupsStart = 0;
    
    /**
     * Read.
     *
     * @param inputStream the input stream
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void read(InputStream inputStream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        int currentSmoothGroup = 0;
        String key = "default";
        while ((line = br.readLine()) != null) {
            try {
                if (line.startsWith("g ") || line.equals("g")) {
                    addMesh(key);
                    key = line.length() > 2 ? line.substring(2) : "default";
                    log("key = " + key);
                } else if (line.startsWith("v ")) {
                    String[] split = line.substring(2).trim().split(" +");
                    float x = Float.parseFloat(split[0]) * scale;
                    float y = Float.parseFloat(split[1]) * scale;
                    float z = Float.parseFloat(split[2]) * scale;

    //                log("x = " + x + ", y = " + y + ", z = " + z);

                    vertexes.add(x);
                    vertexes.add(y);
                    vertexes.add(z);

                    if (flatXZ) {
                        uvs.add(x);
                        uvs.add(z);
                    }
                } else if (line.startsWith("vt ")) {
                    String[] split = line.substring(3).trim().split(" +");
                    float u = split[0].trim().equalsIgnoreCase("nan") ? Float.NaN : Float.parseFloat(split[0]);
                    float v = split[1].trim().equalsIgnoreCase("nan") ? Float.NaN : Float.parseFloat(split[1]);

    //                log("u = " + u + ", v = " + v);

                    uvs.add(u);
                    uvs.add(1 - v);
                } else if (line.startsWith("f ")) {
                    String[] split = line.substring(2).trim().split(" +");
                    int[] faceIndexes = new int[split.length*2];
                    int[] faceNormalIndexes = new int[split.length];
                    for (int i = 0; i < split.length; i++) {
                        String[] split2 = split[i].split("/");
                        faceIndexes[i*2] = vertexIndex(Integer.parseInt(split2[0]));
                        faceIndexes[(i*2)+1] = (split2.length > 1 && split2[1].length()>0) ? uvIndex(Integer.parseInt(split2[1])) : -1;
                        faceNormalIndexes[i] = (split2.length > 2 && split2[2].length()>0) ? normalIndex(Integer.parseInt(split2[2])) : -1;
                    }
                    faces.add(faceIndexes);
                    faceNormals.add(faceNormalIndexes);
                    smoothingGroups.add(currentSmoothGroup);
                } else if (line.startsWith("s ")) {
                    if (line.substring(2).equals("off")) {
                        currentSmoothGroup = 0;
                    } else {
                        currentSmoothGroup = Integer.parseInt(line.substring(2));
                    }
                } else if (line.startsWith("mtllib ")) {
                    // setting materials lib
                    String[] split = line.substring("mtllib ".length()).trim().split(" +");
                    for (String filename : split) {
                        MtlReader mtlReader = new MtlReader(filename, objFilename);
                        materialLibrary.add(mtlReader.getMaterials());
                    }
                } else if (line.startsWith("usemtl ")) {
                    addMesh(key);
                    // setting new material for next mesh
                    String materialName = line.substring("usemtl ".length());
                    for (Map<String, Material> mm : materialLibrary) {
                        Material m = mm.get(materialName);
                        if (m != null) {
                            material = m;
                            break;
                        }
                    }
                } else if (line.isEmpty() || line.startsWith("#")) {
                    // comments and empty lines are ignored
                } else if (line.startsWith("vn ")) {
                    String[] split = line.substring(2).trim().split(" +");
                    float x = Float.parseFloat(split[0]);
                    float y = Float.parseFloat(split[1]);
                    float z = Float.parseFloat(split[2]);
                    normals.add(x);
                    normals.add(y);
                    normals.add(z);
                } else {
                    log("line skipped: " + line);
                }
            } catch (Exception ex) {
                Logger.getLogger(MtlReader.class.getName()).log(Level.SEVERE, "Failed to parse line:" + line, ex);
            }
        }
        addMesh(key);
        
        log("Totally loaded " + (vertexes.size() / 3.) + " vertexes, " 
                + (uvs.size() / 2.) + " uvs, " 
                + (faces.size() / 6.) + " faces, " 
                + smoothingGroups.size() + " smoothing groups.");
    }
    
    /**
     * Adds the mesh.
     *
     * @param key the key
     */
    private void addMesh(String key) {
        if (facesStart >= faces.size()) {
            // we're only interested in faces
            smoothingGroupsStart = smoothingGroups.size();
            return;
        }
        Map<Integer, Integer> vertexMap = new HashMap<>(vertexes.size() / 2);
        Map<Integer, Integer> uvMap = new HashMap<>(uvs.size() / 2);
        Map<Integer, Integer> normalMap = new HashMap<>(normals.size() / 2);
        FloatArrayList newVertexes = new FloatArrayList(vertexes.size() / 2);
        FloatArrayList newUVs = new FloatArrayList(uvs.size() / 2);
        FloatArrayList newNormals = new FloatArrayList(normals.size() / 2);
        boolean useNormals = true;

        int[][] faceArrays = new int[faces.size()-facesStart][];
        int[][] faceNormalArrays = new int[faceNormals.size()-facesNormalStart][];
        
        for (int i = facesStart; i < faces.size();i++) {
            int[] faceIndexes = faces.get(i);
            int[] faceNormalIndexes = faceNormals.get(i);
            for (int j=0;j<faceIndexes.length;j+=2){
                int vi = faceIndexes[j];
                Integer nvi = vertexMap.get(vi);
                if (nvi == null) {
                    nvi = newVertexes.size() / 3;
                    vertexMap.put(vi, nvi);
                    newVertexes.add(vertexes.get(vi * 3));
                    newVertexes.add(vertexes.get(vi * 3 + 1));
                    newVertexes.add(vertexes.get(vi * 3 + 2));
                }
                faceIndexes[j] = nvi;
//                faces.set(i, nvi);
                int uvi = faceIndexes[j+1];
                Integer nuvi = uvMap.get(uvi);
                if (nuvi == null) {
                    nuvi = newUVs.size() / 2;
                    uvMap.put(uvi, nuvi);
                    if (uvi >= 0) {
                        newUVs.add(uvs.get(uvi * 2));
                        newUVs.add(uvs.get(uvi * 2 + 1));
                    } else {
                        newUVs.add(0f);
                        newUVs.add(0f);
                    }
                }
                faceIndexes[j+1] = nuvi;
//                faces.set(i + 1, nuvi);
                
                int ni = faceNormalIndexes[j/2];
                Integer nni = normalMap.get(ni);
                if (nni == null) {
                    nni = newNormals.size() / 3;
                    normalMap.put(ni, nni);
                    if (ni >= 0 && normals.size() >= (ni+1)*3) {
                        newNormals.add(normals.get(ni * 3));
                        newNormals.add(normals.get(ni * 3 + 1));
                        newNormals.add(normals.get(ni * 3 + 2));
                    } else {
                        useNormals = false;
                        newNormals.add(0f);
                        newNormals.add(0f);
                        newNormals.add(0f);
                    }
                }
                faceNormalIndexes[j/2] = nni;
            }
            faceArrays[i-facesStart] = faceIndexes;
            faceNormalArrays[i-facesNormalStart] = faceNormalIndexes;
        }

        PolygonMesh mesh = new PolygonMesh(
                newVertexes.toFloatArray(),
                newUVs.toFloatArray(),
                faceArrays
        );
        
        // Use normals if they are provided
        if (useNormals) {
            int[] smGroups = SmoothingGroups.calcSmoothGroups(faceArrays, faceNormalArrays, newNormals.toFloatArray());
            mesh.getFaceSmoothingGroups().setAll(smGroups);
        } else {
            mesh.getFaceSmoothingGroups().setAll(((IntegerArrayList) smoothingGroups.subList(smoothingGroupsStart, smoothingGroups.size())).toIntArray());
        }
        
        if (debug) {
            System.out.println("mesh.points = " + mesh.getPoints());
            System.out.println("mesh.texCoords = " + mesh.getTexCoords());
            System.out.println("mesh.faces: ");
            for (int[] face: mesh.faces) {
                System.out.println("    face:: "+Arrays.toString(face));
            }
        }

        int keyIndex = 2;
        String keyBase = key;
        while (meshes.get(key) != null) {
            key = keyBase + " (" + keyIndex++ + ")";
        }
        meshes.put(key, mesh);
        materials.put(key, material);
        
        log("Added mesh '" + key + "' of " + (mesh.getPoints().size()/3) + " vertexes, "
                + (mesh.getTexCoords().size()/2) + " uvs, "
                + mesh.faces.length + " faces, "
                + 0 + " smoothing groups.");
        log("material diffuse color = " + ((PhongMaterial) material).getDiffuseColor());
        log("material diffuse map = " + ((PhongMaterial) material).getDiffuseMap());
        
        facesStart = faces.size();
        facesNormalStart = faceNormals.size();
        smoothingGroupsStart = smoothingGroups.size();
    }

    /**
     * Sets the flat xz.
     *
     * @param flatXZ the new flat xz
     */
    public static void setFlatXZ(boolean flatXZ) {
        PolyObjImporter.flatXZ = flatXZ;
    }
}
