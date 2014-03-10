/* 
 * Sphere.java
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
 * A solid sphere.
 *
 * Tthe tessellation along the longitude and latitude directions can be
 * controlled via the {@link #numSlices} and {@link #numStacks} parameters.
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class Sphere implements Primitive {

    private Vector3d center;
    private double radius;
    private int numSlices;
    private int numStacks;
    
    private final PropertyStorage properties = new PropertyStorage();

    /**
     * Constructor. Creates a sphere with radius 1, 16 slices and 8 stacks and
     * center [0,0,0].
     *
     */
    public Sphere() {
        init();
    }

    /**
     * Constructor. Creates a sphere with the specified radius, 16 slices and 8
     * stacks and center [0,0,0].
     *
     * @param radius sphare radius
     */
    public Sphere(double radius) {
        init();
        this.radius = radius;
    }

    /**
     * Constructor. Creates a sphere with the specified radius, number of slices
     * and stacks.
     *
     * @param center center of the sphere
     * @param radius sphere radius
     * @param numSlices number of slices
     * @param numStacks number of stacks
     */
    public Sphere(Vector3d center, double radius, int numSlices, int numStacks) {
        this.center = center;
        this.radius = radius;
        this.numSlices = numSlices;
        this.numStacks = numStacks;
    }

    private void init() {
        center = new Vector3d(0, 0, 0);
        radius = 1;
        numSlices = 16;
        numStacks = 8;
    }

    private Vertex sphereVertex(Vector3d c, double r, double theta, double phi) {
        theta *= Math.PI * 2;
        phi *= Math.PI;
        Vector3d dir = new Vector3d(
                Math.cos(theta) * Math.sin(phi),
                Math.cos(phi),
                Math.sin(theta) * Math.sin(phi)
        );
        return new Vertex(c.plus(dir.times(r)), dir);
    }

    @Override
    public List<Polygon> toPolygons() {
        List<Polygon> polygons = new ArrayList<>();

        for (int i = 0; i < numSlices; i++) {
            for (int j = 0; j < numStacks; j++) {
                final List<Vertex> vertices = new ArrayList<>();

                vertices.add(
                        sphereVertex(center, radius, i / (double) numSlices,
                                j / (double) numStacks)
                );
                if (j > 0) {
                    vertices.add(
                            sphereVertex(center, radius, (i + 1) / (double) numSlices,
                                    j / (double) numStacks)
                    );
                }
                if (j < numStacks - 1) {
                    vertices.add(
                            sphereVertex(center, radius, (i + 1) / (double) numSlices,
                                    (j + 1) / (double) numStacks)
                    );
                }
                vertices.add(
                        sphereVertex(center, radius, i / (double) numSlices,
                                (j + 1) / (double) numStacks)
                );
                polygons.add(new Polygon(vertices, getProperties()));
            }
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
     * @return the radius
     */
    public double getRadius() {
        return radius;
    }

    /**
     * @param radius the radius to set
     */
    public void setRadius(double radius) {
        this.radius = radius;
    }

    /**
     * @return the numSlices
     */
    public int getNumSlices() {
        return numSlices;
    }

    /**
     * @param numSlices the numSlices to set
     */
    public void setNumSlices(int numSlices) {
        this.numSlices = numSlices;
    }

    /**
     * @return the numStacks
     */
    public int getNumStacks() {
        return numStacks;
    }

    /**
     * @param numStacks the numStacks to set
     */
    public void setNumStacks(int numStacks) {
        this.numStacks = numStacks;
    }

    @Override
    public PropertyStorage getProperties() {
        return properties;
    }

}
