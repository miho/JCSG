/**
 * Cube.java
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
import eu.mihosoft.vrl.v3d.parametrics.Parameter;

// TODO: Auto-generated Javadoc
/**
 * An axis-aligned solid cuboid defined by {@code center} and
 * {@code dimensions}.
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class Cube extends Primitive {

    /**
     * Center of this cube.
     */
    private Vector3d center;
    /**
     * Cube dimensions.
     */
    private Vector3d dimensions;

    /** The centered. */
    private boolean centered = true;

    /** The properties. */
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
     * Constructor. Creates a new cuboid with the specified center and
     * dimensions.
     *
     * @param center center of the cuboid
     * @param dimensions cube dimensions
     */
    public Cube(Vector3d center, Vector3d dimensions) {
        this.center = center;
        this.dimensions = dimensions;
    }

    /**
     * Constructor. Creates a new cuboid with center {@code [0,0,0]} and with
     * the specified dimensions.
     *
     * @param w width
     * @param h height
     * @param d depth
     */
    public Cube(double w, double h, double d) {
        this(Vector3d.ZERO, new Vector3d(w, h, d));
    }
    public Cube(LengthParameter w, LengthParameter h, LengthParameter d) {
        this(Vector3d.ZERO, new Vector3d(w.getMM(), h.getMM(), d.getMM()));
        parametrics.add(w);
        parametrics.add(h);
        parametrics.add(d);
    }
    public Cube(LengthParameter size) {
        this(size,size,size);
    }
    /* (non-Javadoc)
     * @see eu.mihosoft.vrl.v3d.Primitive#toPolygons()
     */
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
            polygons.add(new Polygon(vertices, properties));
        }

        if (!centered) {

            Transform centerTransform = Transform.unity().translate(dimensions.x / 2.0, dimensions.y / 2.0, dimensions.z / 2.0);

            for (Polygon p : polygons) {
                p.transform(centerTransform);
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
    public Cube setCenter(Vector3d center) {
        this.center = center;
        return this;
    }

    /**
     * Gets the dimensions.
     *
     * @return the dimensions
     */
    public Vector3d getDimensions() {
        return dimensions;
    }

    /**
     * Sets the dimensions.
     *
     * @param dimensions the dimensions to set
     */
    public Cube setDimensions(Vector3d dimensions) {
        this.dimensions = dimensions;
        return this;
    }

    /* (non-Javadoc)
     * @see eu.mihosoft.vrl.v3d.Primitive#getProperties()
     */
    @Override
    public PropertyStorage getProperties() {
        return properties;
    }

    /**
     * Defines that this cube will not be centered.
     * @return this cube
     */
    public Cube noCenter() {
        centered = false;
        return this;
    }

}
