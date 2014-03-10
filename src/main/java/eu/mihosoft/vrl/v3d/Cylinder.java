/* 
 * Cylinder.java
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
import java.util.Arrays;
import java.util.List;

/**
 * A solid cylinder.
 *
 * Tthe tessellation can be controlled via the {@link #numSlices} parameter.
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class Cylinder implements Primitive {

    private Vector3d start;
    private Vector3d end;
    private double radius;
    private int numSlices;

    private final PropertyStorage properties = new PropertyStorage();

    /**
     * Constructor. Creates a new cylinder with center {@code [0,0,0]} and
     * ranging from {@code [0,-0.5,0]} to {@code [0,0.5,0]}, i.e.
     * {@code size = 1}.
     */
    public Cylinder() {
        this.start = new Vector3d(0, -0.5, 0);
        this.end = new Vector3d(0, 0.5, 0);
        this.radius = 1;
        this.numSlices = 16;
    }

    /**
     * Constructor. Creates a cylinder ranging from {@code start} to {@code end}
     * with the specified {@code radius}. The resolution of the tessellation can
     * be controlled with {@code numSlices}.
     *
     * @param start cylinder start
     * @param end cylinder end
     * @param radius cylinder radius
     * @param numSlices number of slices (used for tessellation)
     */
    public Cylinder(Vector3d start, Vector3d end, double radius, int numSlices) {
        this.start = start;
        this.end = end;
        this.radius = radius;
        this.numSlices = numSlices;
    }

    @Override
    public List<Polygon> toPolygons() {
        final Vector3d s = getStart();
        Vector3d e = getEnd();
        final Vector3d ray = e.minus(s);
        final Vector3d axisZ = ray.unit();
        boolean isY = (Math.abs(axisZ.y) > 0.5);
        final Vector3d axisX = new Vector3d(isY ? 1 : 0, !isY ? 1 : 0, 0).
                cross(axisZ).unit();
        final Vector3d axisY = axisX.cross(axisZ).unit();
        Vertex startV = new Vertex(s, axisZ.negated());
        Vertex endV = new Vertex(e, axisZ.unit());
        List<Polygon> polygons = new ArrayList<>();

        for (int i = 0; i < numSlices; i++) {
            double t0 = i / (double) numSlices, t1 = (i + 1) / (double) numSlices;
            polygons.add(new Polygon(Arrays.asList(
                    startV,
                    cylPoint(axisX, axisY, axisZ, ray, s, radius, 0, t0, -1),
                    cylPoint(axisX, axisY, axisZ, ray, s, radius, 0, t1, -1)),
                    properties
            ));
            polygons.add(new Polygon(Arrays.asList(
                    cylPoint(axisX, axisY, axisZ, ray, s, radius, 0, t1, 0),
                    cylPoint(axisX, axisY, axisZ, ray, s, radius, 0, t0, 0),
                    cylPoint(axisX, axisY, axisZ, ray, s, radius, 1, t0, 0),
                    cylPoint(axisX, axisY, axisZ, ray, s, radius, 1, t1, 0)),
                    properties
            ));
            polygons.add(new Polygon(
                    Arrays.asList(
                            endV,
                            cylPoint(axisX, axisY, axisZ, ray, s, radius, 1, t1, 1),
                            cylPoint(axisX, axisY, axisZ, ray, s, radius, 1, t0, 1)),
                    properties
            )
            );
        }

        return polygons;
    }

    private Vertex cylPoint(
            Vector3d axisX, Vector3d axisY, Vector3d axisZ, Vector3d ray, Vector3d s,
            double r, double stack, double slice, double normalBlend) {
        double angle = slice * Math.PI * 2;
        Vector3d out = axisX.times(Math.cos(angle)).plus(axisY.times(Math.sin(angle)));
        Vector3d pos = s.plus(ray.times(stack)).plus(out.times(r));
        Vector3d normal = out.times(1.0 - Math.abs(normalBlend)).plus(axisZ.times(normalBlend));
        return new Vertex(pos, normal);
    }

    /**
     * @return the start
     */
    public Vector3d getStart() {
        return start;
    }

    /**
     * @param start the start to set
     */
    public void setStart(Vector3d start) {
        this.start = start;
    }

    /**
     * @return the end
     */
    public Vector3d getEnd() {
        return end;
    }

    /**
     * @param end the end to set
     */
    public void setEnd(Vector3d end) {
        this.end = end;
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
     * @return the number of slices
     */
    public int getNumSlices() {
        return numSlices;
    }

    /**
     * @param numSlices the number of slices to set
     */
    public void setNumSlices(int numSlices) {
        this.numSlices = numSlices;
    }

    @Override
    public PropertyStorage getProperties() {
        return properties;
    }

}
