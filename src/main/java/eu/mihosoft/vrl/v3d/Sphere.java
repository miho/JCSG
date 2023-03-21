/**
 * Sphere.java
 *
 * Copyright 2014-2014 Michael Hoffer info@michaelhoffer.de. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer info@michaelhoffer.de "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer info@michaelhoffer.de OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of Michael Hoffer
 * info@michaelhoffer.de.
 */
package eu.mihosoft.vrl.v3d;

import java.util.ArrayList;
import java.util.List;

import eu.mihosoft.vrl.v3d.parametrics.LengthParameter;

// TODO: Auto-generated Javadoc
/**
 * A solid sphere.
 *
 * Tthe tessellation along the longitude and latitude directions can be
 * controlled via the {@link #numSlices} and {@link #numStacks} parameters.
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class Sphere extends Primitive {

    private static final int NUM_SLICES = 16;

	private static final int NUM_STACKS = 8;

	/** The center. */
    private Vector3d center;
    
    /** The radius. */
    private double radius;
    
    /** The num slices. */
    private int numSlices;
    
    /** The num stacks. */
    private int numStacks;

    /** The properties. */
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
//    public Cube(LengthParameter w, LengthParameter h, LengthParameter d) {
//        this(Vector3d.ZERO, new Vector3d(w.getMM(), h.getMM(), d.getMM()));
//
//    }
    public Sphere(LengthParameter size) {
    	this(size.getMM());
    	parametrics.add(size);
    }
    public Sphere(LengthParameter size, int numSlices, int numStacks) {
    	this(size.getMM(),  numSlices,  numStacks);
    	parametrics.add(size);
    }
    /**
     * Constructor. Creates a sphere with the specified radius, number of slices
     * and stacks.
     *
     * @param radius sphare radius
     * @param numSlices number of slices
     * @param numStacks number of stacks
     */
    public Sphere(double radius, int numSlices, int numStacks) {
        init();
        this.radius = radius;
        this.setNumSlices(numSlices);
        this.setNumStacks(numStacks);
    }

    /**
     * Constructor. Creates a sphere with the specified center, radius, number
     * of slices and stacks.
     *
     * @param center center of the sphere
     * @param radius sphere radius
     * @param numSlices number of slices
     * @param numStacks number of stacks
     */
    public Sphere(Vector3d center, double radius, int numSlices, int numStacks) {
        this.center = center;
        this.radius = radius;
        this.setNumSlices(numSlices);
        this.setNumStacks(numStacks);
    }

    /**
     * Inits the.
     */
    private void init() {
        center = new Vector3d(0, 0, 0);
        radius = 1;
        setNumSlices(NUM_SLICES);
        setNumStacks(NUM_STACKS);
    }

    /**
     * Sphere vertex.
     *
     * @param c the c
     * @param r the r
     * @param theta the theta
     * @param phi the phi
     * @return the vertex
     */
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

    /* (non-Javadoc)
     * @see eu.mihosoft.vrl.v3d.Primitive#toPolygons()
     */
    @Override
    public List<Polygon> toPolygons() {
  	  if(radius<=0)
	  		throw new NumberFormatException("radius can not be negative");
        List<Polygon> polygons = new ArrayList<>();

        for (int i = 0; i < getNumSlices(); i++) {
            for (int j = 0; j < getNumStacks(); j++) {
                final List<Vertex> vertices = new ArrayList<>();

                vertices.add(
                        sphereVertex(center, radius, i / (double) getNumSlices(),
                                j / (double) getNumStacks())
                );
                if (j > 0) {
                    vertices.add(
                            sphereVertex(center, radius, (i + 1) / (double) getNumSlices(),
                                    j / (double) getNumStacks())
                    );
                }
                if (j < getNumStacks() - 1) {
                    vertices.add(
                            sphereVertex(center, radius, (i + 1) / (double) getNumSlices(),
                                    (j + 1) / (double) getNumStacks())
                    );
                }
                vertices.add(
                        sphereVertex(center, radius, i / (double) getNumSlices(),
                                (j + 1) / (double) getNumStacks())
                );
                polygons.add(new Polygon(vertices, getProperties()));
            }
        }
        return polygons;
    }

    /**
     * Gets the center.
     *
     * @return the center
     */
    public Vector3d getCenter() {
        return center;
    }

    /**
     * Sets the center.
     *
     * @param center the center to set
     */
    public Sphere setCenter(Vector3d center) {
        this.center = center;
        return this;
    }

    /**
     * Gets the radius.
     *
     * @return the radius
     */
    public double getRadius() {
        return radius;
    }

    /**
     * Sets the radius.
     *
     * @param radius the radius to set
     */
    public Sphere setRadius(double radius) {
        this.radius = radius;return this;
    }

    /**
     * Gets the num slices.
     *
     * @return the numSlices
     */
    public int getNumSlices() {
        return numSlices;
    }

    /**
     * Sets the num slices.
     *
     * @param numSlices the numSlices to set
     */
    public Sphere setNumSlices(int numSlices) {
    	if(numSlices>(NUM_SLICES*4))
    		numSlices=(NUM_SLICES*4);
        this.numSlices = numSlices;return this;
    }

    /**
     * Gets the num stacks.
     *
     * @return the numStacks
     */
    public int getNumStacks() {
        return numStacks;
    }

    /**
     * Sets the num stacks.
     *
     * @param numStacks the numStacks to set
     */
    public Sphere setNumStacks(int numStacks) {
    	if(numStacks>(NUM_STACKS*4))
    		numStacks=(NUM_STACKS*4);
        this.numStacks = numStacks;return this;
    }

    /* (non-Javadoc)
     * @see eu.mihosoft.vrl.v3d.Primitive#getProperties()
     */
    @Override
    public PropertyStorage getProperties() {
        return properties;
    }

}
