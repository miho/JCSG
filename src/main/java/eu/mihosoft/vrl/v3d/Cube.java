/* 
 * Cube.java
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
import java.util.List;

/**
 * An axis-aligned solid cuboid defined by {@code center} and
 * {@code dimensions}.
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class Cube implements Primitive {

    /**
     * Center of this cube.
     */
    private Vector3d center;
    /**
     * Cube dimensions.
     */
    private Vector3d dimensions;
    
    private final PropertyStorage properties = new PropertyStorage();

    /**
     * Constructor. Creates a new cube with center {@code [0,0,0]} and
     * dimensions {@code [1,1,1]}.
     */
    public Cube() {
        center = new Vector3d(0, 0, 0);
        dimensions = new Vector3d(1, 1, 1);
    }
    
    /**
     * Constructor. Creates a new cube with center {@code [0,0,0]} and
     * dimensions {@code [size,size,size]}.
     * 
     * @param size size
     */
    public Cube(double size) {
        center = new Vector3d(0, 0, 0);
        dimensions = new Vector3d(size, size, size);
    }

    /**
     * Constructor.  Creates a new cuboid with the specified center and 
     * dimensions.
     * 
     * @param center center of the cuboid
     * @param dimensions cube dimensions
     */
    public Cube(Vector3d center, Vector3d dimensions) {
        this.center = center;
        this.dimensions = dimensions;
    }

    @Override
    public List<Polygon> toPolygons() {

        int[][][] a = {
            // position     // normal
            {{0, 4, 6, 2}, {-1, 0, 0}},
            {{1, 3, 7, 5}, {+1, 0, 0}},
            {{0, 1, 5, 4}, {0, -1, 0}},
            {{2, 6, 7, 3}, {0, +1, 0}},
            {{0, 2, 3, 1}, {0, 0, -1}},
            {{4, 5, 7, 6}, {0, 0, +1}}
        };
        List<Polygon> polygons = new ArrayList<>();
        for (int[][] info : a) {
            List<Vertex> vertices = new ArrayList<>();
            for (int i : info[0]) {
                Vector3d pos = new Vector3d(
                        center.x + dimensions.x * (1 * Math.min(1, i & 1) - 0.5),
                        center.y + dimensions.y * (1 * Math.min(1, i & 2) - 0.5),
                        center.z + dimensions.z * (1 * Math.min(1, i & 4) - 0.5)
                );
                vertices.add(new Vertex(pos, new Vector3d(
                        (double) info[1][0],
                        (double) info[1][1],
                        (double) info[1][2]
                )));
            }
            polygons.add(new Polygon(vertices,properties));
        }
        return polygons;
    }

    /**
     * @return the center
     */
    public Vector3d getCenter() {
        return center;
    }

    /**
     * @param center the center to set
     */
    public void setCenter(Vector3d center) {
        this.center = center;
    }

    /**
     * @return the dimensions
     */
    public Vector3d getDimensions() {
        return dimensions;
    }

    /**
     * @param dimensions the dimensions to set
     */
    public void setDimensions(Vector3d dimensions) {
        this.dimensions = dimensions;
    }

    @Override
    public PropertyStorage getProperties() {
        return properties;
    }
}
