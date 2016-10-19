/*
 * $RCSfile: FontExtrusion.java,v $
 *
 * Copyright 1997-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE_GPL_CLASSPATH file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE_GPL_CLASSPATH
 * file that accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
 * $Revision: 1.6 $
 * $Date: 2008/02/28 20:17:21 $
 * $State: Exp $
 * 
 * 
 * AWTShapeExtrusion is derived from "javax.media.j3d.FontExtrusion".
 * 
 * Redistribution and use are permitted according to the license notice 
 * above mentioned. A copy is attached as LICENSE_GPL_CLASSPATH.txt.
 *
 * Author: August Lammersdorf, www.InteractiveMesh.com/org
 * Version: 1.4
 * Date: 2008/12/11 
 *
 */

package com.interactivemesh.j3d.community.utils.geometry;

import eu.mihosoft.ext.j3d.javax.vecmath.Point2f;
import java.awt.Shape;
import java.awt.geom.PathIterator;

import java.util.ArrayList;


/**
 * An AWTShapeExtrusion object is used to describe the extrusion path
 * for a AWTShapeExtruder object. The extrusion path defines the edge contour
 * of its AWT Shape object. This contour is perpendicular to the face of the 
 * AWT Shape and must be monotonic in x.
 * <P>
 * The shape of the extrusion path is, by default, a straight line
 * from 0.0 to 0.2 (known as a straight bevel). The shape may be
 * modified via the extrusionShape parameter, also a <code>java.awt.Shape</code>
 * object that describes the 3D contour of the AWT Shape object.
 * </p>
 * <P>
 * User is responsible for data sanity and must make sure that 
 * extrusionShape does not cause intersections. 
 * Else undefined output may be generated.
 * </p>
 * <p>
 * AWTShapeExtrusion is a generalized version of <code>javax.media.j3d.FontExtrusion</code>.
 * </p>
 *
 * @version 1.4
 * @see AWTShapeExtruder
 */
public class AWTShapeExtrusion {

    private float       length 	=   0.2f;
    private Shape       shape	=   null;
    private Point2f[]   pnts	=   null;
    
    private boolean     isT3DExtrusion = true; // v 1.3 / 1.4

    private double      tessellationTolerance = 0.01;

    /**
     * Constructs an AWTShapeExtrusion object with default parameters. The
     * default parameters are as follows:
     * <P>
     * <ul>
     * extrusion shape : null<br>
     * tessellation tolerance : 0.01<br>
     * Text3D-style extrusion : false<br>
     * </ul>
     * <P>
     *
     * A null extrusion shape specifies that a straight line from 0.0
     * to 0.2 (straight bevel) is used.
     *
     * @see AWTShapeExtruder
     */
    public AWTShapeExtrusion() {  
    }
    
    /**
     * Constructs an AWTShapeExtrusion object with a straight bevel extrusion shape,
     * a straight line from 0.0 to depth. 
     * 
     * @param depth length of extrusion
     * @throws IllegalArgumentException if depth is less than 0.0
     * @see AWTShapeExtruder
    */
    public AWTShapeExtrusion(float depth) {
    	if (depth < 0.0f)
    		throw new IllegalArgumentException("Length of extrusion is less than 0.0; depth = " + depth + " !");
    	this.length = depth;
    }
    
    /**
     * Constructs an AWTShapeExtrusion object with the specified extrusion shape, 
     * using the default tessellation tolerance. The specified shape is used to 
     * construct the edge contour. Each shape begins with an implicit
     * point at 0.0. Contour must be monotonic in x.
     *
     * @param extrusionShape the shape object to use to generate the
     * extrusion path.
     * A null shape specifies that a straight line from 0.0 to 0.2
     * (straight bevel) is used.
     *
     * @throws IllegalArgumentException if multiple contours in 
     * extrusionShape, or contour is not monotonic or least x-value
     * of a contour point is not 0.0f
     *
     * @see AWTShapeExtruder
     */
    public AWTShapeExtrusion(Shape extrusionShape) {
    	setExtrusionShape(extrusionShape);
    }

    /**
     * Constructs an AWTShapeExtrusion object with the specified extrusion shape, 
     * using the specified tessellation tolerance. The specified shape is used 
     * to construct the edge contour. Each shape begins with an implicit
     * point at 0.0. Contour must be monotonic in x.
     *
     * @param extrusionShape the shape object to use to generate the extrusion path.
     * A null shape specifies that a straight line from 0.0 to 0.2
     * (straight bevel) is used.
     * @param tessellationTolerance the tessellation tolerance value
     * used in tessellating the extrusion shape.
     * This corresponds to the <code>flatness</code> parameter in
     * the <code>java.awt.Shape.getPathIterator</code> method.
     *
     * @throws IllegalArgumentException if multiple contours in 
     * extrusionShape, or contour is not monotonic or least x-value
     * of a contour point is not 0.0f
     *
     * @see AWTShapeExtruder
     *
     */
    public AWTShapeExtrusion(Shape extrusionShape, double tessellationTolerance) {
    	this.tessellationTolerance = tessellationTolerance;
    	setExtrusionShape(extrusionShape);
    }

    /**
     * Returns the AWTShapeExtrusion's shape parameter.  This
     * parameter is used to construct the edge contour.
     *
     * @return extrusionShape the shape object used to generate the
     *  extrusion path, or <code>null</code> if a straight bevel extrusion shape is set
     *
     */
    public Shape getExtrusionShape() {
        return shape;
    }

    /**
     * Sets the AWTShapeExtrusion's shape parameter.  This
     * parameter is used to construct the edge contour.
     *
     * @param extrusionShape the shape object to use to generate the
     * extrusion path.
     * A null shape specifies that a straight line from 0.0 to 0.2
     * (straight bevel) is used.
     *
     * @throws IllegalArgumentException if multiple contours in 
     * extrusionShape, or contour is not monotonic or least x-value
     * of a contour point is not 0.0f
     */
    public void setExtrusionShape(Shape extrusionShape) {
        
    	shape = extrusionShape;
    	
    	// Set defaults
    	if (shape == null) {
    	    length = 0.2f;
    	    pnts   = null;
            return;
    	}

    	// V 1.2
    	updateExtrusionShape();
    }
    
    /**
     * Sets the AWTShapeExtrusion's shape to a straight bevel extrusion shape,
     * a straight line from 0.0 to depth. 
     * 
     * @param depth length of extrusion
     * @throws IllegalArgumentException if depth is less than 0.0
     */
    public void setStraightBevelExtrusionShape(float depth) {
        if (depth < 0.0f)
            throw new IllegalArgumentException("Length of extrusion is less than 0.0; depth = " + depth + " !");
        length  =   depth;
        shape   =   null;
        pnts    =   null;
    }
    
    // V 1.2
    private void updateExtrusionShape() {
        
        if (shape == null)
            throw new IllegalStateException("Extrusion shape is null !!");
        
        PathIterator pIt = shape.getPathIterator(null, tessellationTolerance);
        ArrayList<Point2f>  coords = new ArrayList<Point2f>();
        float tmpCoords[] = new float[6], prevX = 0.0f; 
        int flag, n = 0, inc = -1;

        // Extrusion shape is restricted to be single contour, monotonous
        // increasing, non-self-intersecting curve. Throw exception otherwise
        while (!pIt.isDone()) {
            Point2f vertex = new Point2f();
            flag = pIt.currentSegment(tmpCoords);
            if (flag == PathIterator.SEG_LINETO){
                vertex.x = tmpCoords[0];
                vertex.y = tmpCoords[1];
                if (inc == -1){
                    if (prevX < vertex.x) 
                        inc = 0; 
                    else if (prevX > vertex.x) 
                        inc = 1;
                }
                //Flag 'inc' indicates if curve is monotonic increasing or 
                // monotonic decreasing. It is set to -1 initially and remains
                // -1 if consecutive x values are same. Once 'inc' is set to 
                // 1 or 0, exception is thrown is curve changes direction.
                if (((inc == 0) && (prevX > vertex.x)) ||
                    ((inc == 1) && (prevX < vertex.x))   )
                    throw new IllegalArgumentException("AWTShapeExtrusion:invalid shape- non-monotonic");

                prevX = vertex.x;
                n++;
                coords.add(vertex);
            }
            else if (flag == PathIterator.SEG_MOVETO){
                if (n != 0)
                    throw new IllegalArgumentException("AWTShapeExtrusion:invalid shape- multiple contours");

                vertex.x = tmpCoords[0];
                vertex.y = tmpCoords[1];
                prevX = vertex.x;
                n++;
                coords.add(vertex);
            }
            pIt.next();
        }

        int i, num = coords.size();
        pnts = new Point2f[num];
        
        if (inc == 0){
            for (i=0;i < num;i++){
                pnts[i] = (Point2f)coords.get(i);
            }
        }
        else {
            for (i=0;i < num;i++) {
                pnts[i] = (Point2f)coords.get(num - i -1);
            }
        }

        // Force last y to be zero until Text3D face scaling is implemented
        pnts[num-1].y = 0.0f;
        if (pnts[0].x != 0.0f)
            throw new IllegalArgumentException("AWTShapeExtrusion: invalid shape - shape must start or end at x = 0.0f");

        // Compute straight line distance between first and last points.
        float dx = (pnts[0].x - pnts[num-1].x);
        float dy = (pnts[0].y - pnts[num-1].y);
        length = (float)Math.sqrt(dx*dx + dy*dy);
    }
    
    /**
     * Returns the tessellation tolerance with which the geometry of the 
     * extrusion shape will be created.
     * @return the tessellation tolerance used by this AWTShapeExtrusion
     *
     */
    public double getTessellationTolerance() {
    	return tessellationTolerance;
    }
    
    // V 1.2
    /**
     * Sets the tessellation tolerance with which the geometry of the 
     * extrusion shape will be created. <P>
     * This corresponds to the <code>flatness</code> parameter in
     * the <code>java.awt.Shape.getPathIterator</code> method.
     * @param tessellationTolerance the tessellation tolerance value
     * used in tessellating the extrusion shape.
     */
    public void setTessellationTolerance(double tessellationTolerance) {
        this.tessellationTolerance = tessellationTolerance;
        if (shape != null)
            updateExtrusionShape();
    }
    
    // V 1.3 / 1.4
    /**
     * Returns the state of the Text3D-style extrusion enable flag.
     *  
     * The default state is <code>false</code>.
     * 
     * @return true if Text3D-style extrusion is enabled, false if it is disabled 
     * @since AWT Shape Extruder 2.2
     */
    public boolean isT3DStyleExtrusion() {
        return isT3DExtrusion;
    }
    // V 1.3 / 1.4
    /**
     * Enables or disables Text3D-style extrusion. <P>
     * This flag allows to switch to Text3D-style extrusion 
     * combined with the advantages of smooth shading and solid geometry.
     * This provides an (interim) alternative for the not yet perfectly functioning
     * curved shape extrusion.
     * 
     * The default state is <code>false</code>.
     * 
     * @param enable true or false to enable or disable Text3D-style extrusion 
     * @since AWT Shape Extruder 2.2
     */
    public void setT3DStyleExtrusion(boolean enable) {
        isT3DExtrusion = enable;
    }
    
    /**
     * Returns the boundary of the specified extrusion shape.
     * 
     * @return outline of the specified extrusion shape, 
     * or <code>null</code> if a straight bevel extrusion shape is set
     */
    public Point2f[] getExtrusion() {
    	return pnts;  	   
    }
    /**
     * Returns the length of extrusion either of the straight bevel extrusion shape
     * or of the specified extrusion shape.
     * 
     * @return length of extrusion
     */
    public float getDepth() {
    	return length;
    }
}
