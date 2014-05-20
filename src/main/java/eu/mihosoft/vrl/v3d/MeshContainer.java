/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d;

import javafx.scene.shape.Mesh;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class MeshContainer {

    private final Mesh mesh;
    private final double width;
    private final double height;
    private final double depth;
    private final Bounds bounds;

    MeshContainer(Mesh mesh, Vector3d min, Vector3d max) {
        this.mesh = mesh;
        this.bounds = new Bounds(min, max);
        this.width = bounds.getBounds().x;
        this.height = bounds.getBounds().y;
        this.depth = bounds.getBounds().z;
    }

    /**
     * @return the width
     */
    public double getWidth() {
        return width;
    }

    /**
     * @return the height
     */
    public double getHeight() {
        return height;
    }

    /**
     * @return the depth
     */
    public double getDepth() {
        return depth;
    }

    /**
     * @return the mesh
     */
    public Mesh getMesh() {
        return mesh;
    }

    @Override
    public String toString() {
        return bounds.toString();
    }

    /**
     * @return the bounds
     */
    public Bounds getBounds() {
        return bounds;
    }

}
