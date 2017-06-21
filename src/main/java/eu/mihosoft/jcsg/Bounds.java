/**
 * Bounds.java
 *
 * Copyright 2014-2017 Michael Hoffer <info@michaelhoffer.de>. All rights
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
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info@michaelhoffer.de> "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info@michaelhoffer.de> OR
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
 * <info@michaelhoffer.de>.
 */
package eu.mihosoft.jcsg;

import eu.mihosoft.vvecmath.Vector3d;

/**
 * Bounding box for CSGs.
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class Bounds {

    private final Vector3d center;
    private final Vector3d bounds;
    private final Vector3d min;
    private final Vector3d max;
    private CSG csg;
    private final Cube cube;

    /**
     * Constructor.
     *
     * @param min min x,y,z values
     * @param max max x,y,z values
     */
    public Bounds(Vector3d min, Vector3d max) {
        this.center = Vector3d.xyz(
                (max.x() + min.x()) / 2,
                (max.y() + min.y()) / 2,
                (max.z() + min.z()) / 2);

        this.bounds = Vector3d.xyz(
                Math.abs(max.x() - min.x()),
                Math.abs(max.y() - min.y()),
                Math.abs(max.z() - min.z()));

        this.min = min.clone();
        this.max = max.clone();

        cube = new Cube(center, bounds);

    }

    @Override
    public Bounds clone() {
        return new Bounds(min.clone(), max.clone());
    }

    /**
     * Returns the position of the center.
     *
     * @return the center position
     */
    public Vector3d getCenter() {
        return center;
    }

    /**
     * Returns the bounds (width,height,depth).
     *
     * @return the bounds (width,height,depth)
     */
    public Vector3d getBounds() {
        return bounds;
    }

    /**
     * Returns this bounding box as csg.
     *
     * @return this bounding box as csg
     */
    public CSG toCSG() {

        if (csg == null) {
            csg = cube.toCSG();
        }

        return csg;
    }

    /**
     * Returns this bounding box as cube.
     *
     * @return this bounding box as cube
     */
    public Cube toCube() {
        return cube;
    }

    /**
     * Indicates whether the specified vertex is contained within this bounding
     * box (check includes box boundary).
     *
     * @param v vertex to check
     * @return {@code true} if the vertex is contained within this bounding box;
     * {@code false} otherwise
     */
    public boolean contains(Vertex v) {
        return contains(v.pos);
    }

    /**
     * Indicates whether the specified point is contained within this bounding
     * box (check includes box boundary).
     *
     * @param v vertex to check
     * @return {@code true} if the point is contained within this bounding box;
     * {@code false} otherwise
     */
    public boolean contains(Vector3d v) {
        boolean inX = min.x() <= v.x() && v.x() <= max.x();
        boolean inY = min.y() <= v.y() && v.y() <= max.y();
        boolean inZ = min.z() <= v.z() && v.z() <= max.z();

        return inX && inY && inZ;
    }

    /**
     * Indicates whether the specified polygon is contained within this bounding
     * box (check includes box boundary).
     *
     * @param p polygon to check
     * @return {@code true} if the polygon is contained within this bounding
     * box; {@code false} otherwise
     */
    public boolean contains(Polygon p) {
        return p.vertices.stream().allMatch(v -> contains(v));
    }

    /**
     * Indicates whether the specified polygon intersects with this bounding box
     * (check includes box boundary).
     *
     * @param p polygon to check
     * @return {@code true} if the polygon intersects this bounding box;
     * {@code false} otherwise
     * @deprecated not implemented yet
     */
    @Deprecated
    public boolean intersects(Polygon p) {
        throw new UnsupportedOperationException("Implementation missing!");
    }

    /**
     * Indicates whether the specified bounding box intersects with this
     * bounding box (check includes box boundary).
     *
     * @param b box to check
     * @return {@code true} if the bounding box intersects this bounding box;
     * {@code false} otherwise
     */
    public boolean intersects(Bounds b) {

        if (b.getMin().x() > this.getMax().x() || b.getMax().x() < this.getMin().x()) {
            return false;
        }
        if (b.getMin().y() > this.getMax().y() || b.getMax().y() < this.getMin().y()) {
            return false;
        }
        if (b.getMin().z() > this.getMax().z() || b.getMax().z() < this.getMin().z()) {
            return false;
        }

        return true;

    }

    /**
     * @return the min x,y,z values
     */
    public Vector3d getMin() {
        return min;
    }

    /**
     * @return the max x,y,z values
     */
    public Vector3d getMax() {
        return max;
    }

    @Override
    public String toString() {
        return "[center: " + center + ", bounds: " + bounds + "]";
    }

}
