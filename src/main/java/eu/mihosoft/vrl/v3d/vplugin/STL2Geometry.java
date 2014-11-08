/* 
 * STL2Geometry.java
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
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
 * First, the following text must be displayed on the Canvas:
 * "based on VRL source code". In this case the VRL canvas icon must be removed.
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
 * Computing and Visualization in Science, 2011, in press.
 */
package eu.mihosoft.vrl.v3d.vplugin;

import eu.mihosoft.vrl.annotation.ParamInfo;
import eu.mihosoft.vrl.io.IOUtil;
import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Node;
import eu.mihosoft.vrl.v3d.Nodes;
import eu.mihosoft.vrl.v3d.Polygon;
import eu.mihosoft.vrl.v3d.PropertyStorage;
import eu.mihosoft.vrl.v3d.Triangle;
import eu.mihosoft.vrl.v3d.VTriangleArray;
import eu.mihosoft.vrl.v3d.Vector3d;
import eu.mihosoft.vrl.v3d.ext.imagej.STLLoader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.media.j3d.Geometry;
import javax.vecmath.Point3f;

/**
 * <p>
 * Converts simple obj files to java 3d geometries.</p>
 * <p>
 * The format is relatively simple:
 * </p>
 * <code>
 * <pre>
 * v node_x node_y node_z
 * .
 * .
 * .
 * f node_index_1 node_index_2 node_index_3
 * </pre>
 * </code>
 * <p>
 * Example (tetrahedron):
 * </p>
 * <code>
 * <pre>
 * v  1  1 -1
 * v -1 -1 -1
 * v -1  1  1
 * v  1 -1  1
 * f 1 2 3
 * f 1 2 4
 * f 1 3 4
 * f 2 3 4
 * </pre>
 * </code>
 * <p>
 * All other information in the obj file is ignored. Currently only triangles
 * are supported.
 * <p>
 * @author Michael Hoffer <info@michaelhoffer.de>
 * @see VTriangleArray
 */
public class STL2Geometry {

//    /**
//     * Loads text file to a geometry array.
//     *
//     * @param in the input stream
//     * @return the geometry
//     * @throws java.io.IOException
//     */
//    public Geometry loadObj(
//            @ParamInfo(name = "Input File:",
//                    style = "default") InputStream in) throws IOException {
//        return loadAsVTriangleArray(in).getTriangleArray();
//    }

    /**
     * Loads text file to a geometry array.
     *
     * @param file the file
     * @return the geometry
     * @throws java.io.IOException
     */
    public Geometry loadSTL(
            @ParamInfo(name = "Input File:",
                    style = "load-dialog") File file) throws IOException {
        return loadAsVTriangleArray(file).getTriangleArray();
    }

    /**
     * Loads text file to a VTriangleArray.
     *
     * @param file the file
     * @return the VTriangleArray
     * @throws java.io.IOException
     */
    public VTriangleArray loadAsVTriangleArray(
            @ParamInfo(name = "Input File:",
                    style = "load-dialog") File file) throws IOException {

        VTriangleArray triangleArray = new VTriangleArray();

        STLLoader loader = new STLLoader();

        List<Point3f> vertices = new ArrayList<Point3f>();
        for (Point3f p : loader.parse(file)) {
            vertices.add(p);
            if (vertices.size() == 3) {
                triangleArray.addTriangle(
                        new Triangle(
                                new Node(vertices.get(0)),
                                new Node(vertices.get(1)),
                                new Node(vertices.get(2))));
                vertices = new ArrayList<Point3f>();
            }
        }

        return triangleArray;

    }

    /**
     * Loads text file to a VTriangleArray.
     *
     * @param in the stream
     * @return the VTriangleArray
     * @throws java.io.IOException
     */
//    public VTriangleArray loadAsVTriangleArray(
//            @ParamInfo(name = "Input File:",
//            style = "default") InputStream in) throws IOException {
//
//        VTriangleArray triangleArray = new VTriangleArray();
//
//        STLLoader loader = new STLLoader();
//        
//        List<Polygon> polygons = new ArrayList<Polygon>();
//        List<Vector3d> vertices = new ArrayList<Vector3d>();
//        for(Point3f p :loader.parse(f) {
//            vertices.add(new Vector3d(p.x, p.y, p.z));
//            if (vertices.size()==3) {
//                polygons.add(Polygon.fromPoints(vertices));
//                vertices = new ArrayList<Vector3d>();
//            }
//        }
//
//
//        return triangleArray;
//    }
}
