/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d;

import java.util.List;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class Bounds {

    private final Vector3d center;
    private final Vector3d bounds;
    private final Vector3d min;
    private final Vector3d max;
    private final CSG cube;

    public Bounds(Vector3d min, Vector3d max) {
        this.center = new Vector3d(
                (max.x - min.x) / 2,
                (max.y - min.y) / 2,
                (max.z - min.z) / 2);
        
        this.bounds = new Vector3d(
                max.x - min.x,
                max.y - min.y,
                max.z - min.z);
        
        this.min = min;
        this.max = max;

        cube = new Cube(center, bounds).toCSG();
    }

    /**
     * @return the center
     */
    public Vector3d getCenter() {
        return center;
    }

    /**
     * @return the bounds
     */
    public Vector3d getBounds() {
        return bounds;
    }

    public CSG toCSG() {
        return cube;
    }

    public boolean contains(Vertex v) {
        boolean inX = min.x <= v.pos.x && v.pos.x <= max.x;
        boolean inY = min.y <= v.pos.y && v.pos.y <= max.y;
        boolean inZ = min.z <= v.pos.z && v.pos.z <= max.z;

        return inX && inY && inZ;
    }

    public boolean contains(Polygon p) {
        return p.vertices.stream().anyMatch((v) -> (contains(v)));
    }

    /**
     * @return the min
     */
    public Vector3d getMin() {
        return min;
    }

    /**
     * @return the max
     */
    public Vector3d getMax() {
        return max;
    }

}
