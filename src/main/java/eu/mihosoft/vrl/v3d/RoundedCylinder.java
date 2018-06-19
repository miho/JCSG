/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d;

import java.util.ArrayList;
import java.util.List;

import eu.mihosoft.vrl.v3d.ext.quickhull3d.HullUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class RoundedCube.
 * 
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class RoundedCylinder extends Primitive {

    /** The properties. */
    private final PropertyStorage properties = new PropertyStorage();

    /** The corner radius. */
    private double cornerRadius = 2;
    
    /** The resolution. */
    private int resolution = 30;

	private double startRadius;

	private double endRadius;

	private double height;
	
	private int numCornerSlices;

    /**
     * Constructor. Creates a cylinder ranging from {@code [0,0,0]} to
     * {@code [0,0,height]} with the specified {@code radius} and
     * {@code height}. The resolution of the tessellation can be controlled with
     * {@code numSlices}.
     *
     * @param startRadius cylinder start radius
     * @param endRadius cylinder end radius
     * @param height cylinder height
     * @param numSlices number of slices (used for tessellation)
     */
    public RoundedCylinder(double startRadius, double endRadius, double height, int numSlices) {
		this.startRadius = startRadius;
		this.endRadius = endRadius;
		this.height = height;
		this.resolution = numSlices;
		setNumCornerSlices(6);
    }

    /**
     * Constructor. Creates a cylinder ranging from {@code [0,0,0]} to
     * {@code [0,0,height]} with the specified {@code radius} and
     * {@code height}. The resolution of the tessellation can be controlled with
     * {@code numSlices}.
     *
     * @param startRadius cylinder start radius
     * @param height cylinder height
     */
    public RoundedCylinder(double startRadius,  double height) {
		this.startRadius = startRadius;
		this.endRadius = startRadius;
		this.height = height;
		this.resolution = 30;
		setNumCornerSlices(6);
    }

    
    /* (non-Javadoc)
     * @see eu.mihosoft.vrl.v3d.Primitive#toPolygons()
     */
    @Override
    public List<Polygon> toPolygons() {
    	double minHeight = height-cornerRadius*2;
		ArrayList<CSG> cylParts =new ArrayList<>();

		for(int i=0;i<numCornerSlices;i++){
			double radIncs = startRadius-cornerRadius+Math.sin(Math.PI/2*((double)i/(double)numCornerSlices))*cornerRadius;
			double radInce = endRadius-cornerRadius+Math.sin(Math.PI/2*((double)i/(double)numCornerSlices))*cornerRadius;

			double heightInc = (Math.cos(Math.PI/2*((double)i/(double)(numCornerSlices)))*cornerRadius);
			cylParts.add(
				new Cylinder(radIncs, // Radius at the bottom
							radInce, // Radius at the top
                      		heightInc*2+minHeight, // Height
                      		(int)resolution //resolution
                      		)
							.toCSG()
                      		.movez(-heightInc)
			);
		}
		return HullUtil.hull(cylParts).toZMin().getPolygons();
    }

    /* (non-Javadoc)
     * @see eu.mihosoft.vrl.v3d.Primitive#getProperties()
     */
    @Override
    public PropertyStorage getProperties() {
        return properties;
    }


    /**
     * Gets the resolution.
     *
     * @return the resolution
     */
    public int getResolution() {
        return resolution;
    }
    
     /**
      * Resolution.
      *
      * @param resolution the resolution to set
      * @return this cube
      */
    public RoundedCylinder resolution(int resolution) {
        this.resolution = resolution;
        return this;
    }

    /**
     * Gets the corner radius.
     *
     * @return the corner radius
     */
    public double getCornerRadius() {
        return cornerRadius;
    }
    
    /**
     * Corner radius.
     *
     * @param cornerRadius the corner radius to set
     * @return this cube
     */
    public RoundedCylinder cornerRadius(double cornerRadius) {
        this.cornerRadius = cornerRadius;
        return this;
    }



	public int getNumCornerSlices() {
		return numCornerSlices;
	}



	public RoundedCylinder setNumCornerSlices(int numCornerSlices) {
		this.numCornerSlices = numCornerSlices;
        return this;
	}

}
