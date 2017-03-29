/**
 * Icosahedron.java
 */
package eu.mihosoft.vrl.v3d;

import java.util.ArrayList;
import java.util.List;

import eu.mihosoft.vrl.v3d.ext.quickhull3d.HullUtil;
import eu.mihosoft.vrl.v3d.parametrics.LengthParameter;
import eu.mihosoft.vrl.v3d.parametrics.Parameter;

public class Icosahedron extends Primitive {

    /**
     * Center of this icosahedron.
     */
    private Vector3d center;
    /**
     * Icosahedron circumscribed radius.
     */
    private double radius;

    /** The centered. */
    private boolean centered = true;

    /** The properties. */
    private final PropertyStorage properties = new PropertyStorage();
    /**
     * Constructor. Creates a new icosahedron with center {@code [0,0,0]} and
     * dimensions {@code [1,1,1]}.
     */
    public Icosahedron() {
        center = new Vector3d(0, 0, 0);
        radius = 1;
    }

    /**
     * Constructor. Creates a new icosahedron with center {@code [0,0,0]} and
     * radius {@code size}.
     *
     * @param size size
     */
    public Icosahedron(double size) {
        center = new Vector3d(0, 0, 0);
        radius = size;
    }

    /**
     * Constructor. Creates a new icosahedron with the specified center and
     * radius.
     *
     * @param center center of the icosahedron
     * @param circumradius of the icosahedron
     */
    public Icosahedron(Vector3d center, double size) {
        this.center = center;
        this.radius = size;
    }
    
    /* (non-Javadoc)
     * @see eu.mihosoft.vrl.v3d.Primitive#toPolygons()
     */
    @Override
    public List<Polygon> toPolygons() {
    	
    	double phi = (Math.sqrt(5)+1)/2;
    	
    	List<Vector3d> points = new ArrayList<>();
    		points.add(new Vector3d(0,1,phi));
			points.add(new Vector3d(0,-1,phi));
			points.add(new Vector3d(phi,0,1));
			points.add(new Vector3d(1,phi,0));
			points.add(new Vector3d(-1,phi,0));
			points.add(new Vector3d(-phi,0,1));
			points.add(new Vector3d(1,-phi,0));
			points.add(new Vector3d(phi,0,-1));
			points.add(new Vector3d(0,1,-phi));
			points.add(new Vector3d(-phi,0,1));
			points.add(new Vector3d(-1,-phi,0));
			points.add(new Vector3d(0,-1,phi));
    	
		List<Polygon> polygons = HullUtil.hull(points).scale(1/(Math.sqrt(1+Math.pow(phi, 2)))).getPolygons();

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
    public Icosahedron setCenter(Vector3d center) {
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
    public void setRadius(double radius) {
        this.radius = radius;
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
    public Icosahedron noCenter() {
        centered = false;
        return this;
    }

}
