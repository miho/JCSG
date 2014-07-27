/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d;

import static eu.mihosoft.vrl.v3d.Transform.unity;
import java.util.List;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class RoundedCube implements Primitive {

    /**
     * Cube dimensions.
     */
    private Vector3d dimensions;
    private Vector3d center;
    private boolean centered;

    private final PropertyStorage properties = new PropertyStorage();

    private double cornerRadius = 0.1;
    private int resolution = 8;

    /**
     * Constructor. Creates a new rounded cube with center {@code [0,0,0]} and
     * dimensions {@code [1,1,1]}.
     */
    public RoundedCube() {
        center = new Vector3d(0, 0, 0);
        dimensions = new Vector3d(1, 1, 1);
    }

    /**
     * Constructor. Creates a new rounded cube with center {@code [0,0,0]} and
     * dimensions {@code [size,size,size]}.
     *
     * @param size size
     */
    public RoundedCube(double size) {
        center = new Vector3d(0, 0, 0);
        dimensions = new Vector3d(size, size, size);
    }

    /**
     * Constructor. Creates a new rounded cuboid with the specified center and
     * dimensions.
     *
     * @param center center of the cuboid
     * @param dimensions cube dimensions
     */
    public RoundedCube(Vector3d center, Vector3d dimensions) {
        this.center = center;
        this.dimensions = dimensions;
    }

    /**
     * Constructor. Creates a new rounded cuboid with center {@code [0,0,0]}
     * and with the specified dimensions.
     *
     * @param w width
     * @param h height
     * @param d depth
     */
    public RoundedCube(double w, double h, double d) {
        this(Vector3d.ZERO, new Vector3d(w, h, d));
    }


    @Override
    public List<Polygon> toPolygons() {
        CSG spherePrototype = 
                new Sphere(getCornerRadius(), getResolution()*2, getResolution()).toCSG();

        double x = dimensions.x / 2.0 - getCornerRadius();
        double y = dimensions.y / 2.0 - getCornerRadius();
        double z = dimensions.z / 2.0 - getCornerRadius();

        CSG sphere1 = spherePrototype.transformed(unity().translate(-x, -y, -z));
        CSG sphere2 = spherePrototype.transformed(unity().translate(x, -y, -z));
        CSG sphere3 = spherePrototype.transformed(unity().translate(x, y, -z));
        CSG sphere4 = spherePrototype.transformed(unity().translate(-x, y, -z));

        CSG sphere5 = spherePrototype.transformed(unity().translate(-x, -y, z));
        CSG sphere6 = spherePrototype.transformed(unity().translate(x, -y, z));
        CSG sphere7 = spherePrototype.transformed(unity().translate(x, y, z));
        CSG sphere8 = spherePrototype.transformed(unity().translate(-x, y, z));

        List<Polygon> result = sphere1.union(
                sphere2, sphere3, sphere4,
                sphere5, sphere6, sphere7, sphere8).hull().getPolygons();

        if (!centered) {

            Transform centerTransform = Transform.unity().translate(dimensions.x / 2.0, dimensions.y / 2.0, dimensions.z / 2.0);

            for (Polygon p : result) {
                p.transform(centerTransform);
            }
        }

        return result;
    }

    @Override
    public PropertyStorage getProperties() {
        return properties;
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

    /**
     * Defines that this cube will not be centered.
     * @return this cube
     */
    public RoundedCube noCenter() {
        centered = false;
        return this;
    }

    /**
     * @return the resolution
     */
    public int getResolution() {
        return resolution;
    }

    /**
     * @param resolution the resolution to set
     */
    public void setResolution(int resolution) {
        this.resolution = resolution;
    }
    
     /**
     * @param resolution the resolution to set
     * @return this cube
     */
    public RoundedCube resolution(int resolution) {
        this.resolution = resolution;
        return this;
    }

    /**
     * @return the corner radius
     */
    public double getCornerRadius() {
        return cornerRadius;
    }

    /**
     * @param cornerRadius the corner radius to set
     */
    public void setCornerRadius(double cornerRadius) {
        this.cornerRadius = cornerRadius;
    }
    
    /**
     * @param cornerRadius the corner radius to set
     * @return this cube
     */
    public RoundedCube cornerRadius(double cornerRadius) {
        this.cornerRadius = cornerRadius;
        return this;
    }

}
