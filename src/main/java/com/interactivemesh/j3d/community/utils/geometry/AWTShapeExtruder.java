/*
 * $RCSfile: Font3D.java,v $
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
 * AWTShapeExtruder is derived from "javax.media.j3d.Font3D".
 * 
 * Redistribution and use are permitted according to the license notice 
 * above mentioned. A copy is attached as LICENSE_GPL_CLASSPATH.txt.
 *
 * Author: August Lammersdorf, www.InteractiveMesh.com/org
 * Version: 1.7
 * Date: 2008/12/19 
 *
 */

package com.interactivemesh.j3d.community.utils.geometry;

import eu.mihosoft.ext.j3d.com.sun.j3d.utils.geometry.GeometryInfo;
import eu.mihosoft.ext.j3d.com.sun.j3d.utils.geometry.NormalGenerator;
import eu.mihosoft.ext.j3d.javax.media.j3d.BoundingBox;
import eu.mihosoft.ext.j3d.javax.media.j3d.GeometryArray;
import eu.mihosoft.ext.j3d.javax.media.j3d.LineStripArray;
import eu.mihosoft.ext.j3d.javax.media.j3d.Transform3D;
import eu.mihosoft.ext.j3d.javax.media.j3d.TriangleArray;
import eu.mihosoft.ext.j3d.javax.vecmath.Point2f;
import eu.mihosoft.ext.j3d.javax.vecmath.Point3d;
import eu.mihosoft.ext.j3d.javax.vecmath.Point3f;
import eu.mihosoft.ext.j3d.javax.vecmath.SingularMatrixException;
import eu.mihosoft.ext.j3d.javax.vecmath.Tuple3f;
import eu.mihosoft.ext.j3d.javax.vecmath.Vector3f;
import java.awt.Font;
import java.awt.Shape;

import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;

import java.util.ArrayList;



/**
 * AWTShapeExtruder converts a Java 2D <code>Shape</code>, called here AWT Shape,
 * to a 3D geometry in a factory manner. 
 * <p>
 * Each AWTShapeExtruder object has the following parameters:<P>
 * <UL>
 * <LI>Tessellation tolerance - precision value used in tessellating the AWT Shape.</LI><P>
 * <LI>Extrusion path - creates depth by describing how the edge of the AWT Shape varies in the Z axis.</LI><P>
 * <LI>Crease angle - determines the smooth shading of the 3D geometry.</LI><P>
 * <LI>Geometry transform - transformation applied to the generated coordinates and normals.</LI></UL><P>
 * 
 * According to the current state of these parameters a AWTShapeExtruder object 
 * creates for a given AWT Shape or for given characters a <code>javax.media.j3d.GeometryArray</code> 
 * 
 * <UL>
 * <LI>{@link #getGeometry(Shape, AffineTransform)}</LI>
 * <LI>{@link #getGeometry(char[], Font, boolean)}</LI>
 * <LI>{@link #getOutlineGeometry(Shape, AffineTransform)}</LI>
 * </UL><br/>
 * 
 * AWTShapeExtruder is a generalized version of <code>javax.media.j3d.Font3D</code>.
 * </p>
 * 
 * @version 1.7
 * @see AWTShapeExtrusion
 * @see String3D
 */
public class AWTShapeExtruder {

	// 0.767944871 radians = 44 degrees
	private double				creaseAngle				=	0.767944871; 
    private double              tessellationTolerance 	=   0.01;
    
    private AWTShapeExtrusion	shapeExtrusion          =   null;
    
    // V 1.7
    // Geometry transformation
    // true : no transformation will be applied
    private boolean             isIdentityTransform     =   true;
    // true : allows to transform BoundingBox
    // false : calc BB from coordinates (e.g. if rotation)
    private boolean             isScaleTranslation      =   false;
    private Transform3D         coordTransform          =   new Transform3D();
    // Inverse transpose transform
    private Transform3D         normalTransform         =   new Transform3D();
    
    // Used by triangulateGlyphs method to split contour data into islands.
    private final static float 	EPS = 0.000001f;

    /**
     * Constructs an AWTShapeExtruder object using the default values for the 
     * extrusion path, the tessellation tolerance, the crease angle, 
     * and the geometry transform.  
     * <p>
     * The default values are as follows:
     * <ul>
     * extrusion path : null (no extrusion)<br>
     * tessellation tolerance : 0.01<br>
     * crease angle : 0.767944871 radians (44 degrees)<br>
     * geometry transform : identity matrix<br>
     * </ul>
     * </p>
     */
    public AWTShapeExtruder() {
        this(0.01, null);
    }
    
    /**
     * Constructs an AWTShapeExtruder object from the specified AWTShapeExtrusion
     * object, using the default values for the tessellation tolerance, 
     * the crease angle, and the geometry transform.  
     * <p>
     * The default values are as follows:
     * <ul>
     * tessellation tolerance : 0.01<br>
     * crease angle : 0.767944871 radians (44 degrees)<br>
     * geometry transform : identity matrix<br>
     * </ul>
     * </p>
     * <P>
     * The AWTShapeExtrusion object contains the extrusion path to use on
     * the AWT Shape object. Passing null for
     * the AWTShapeExtrusion parameter results in no extrusion being done.
     *
     * @param extrudePath the extrusion path used to describe how
     * the edge of the AWT Shape varies along the Z axis
     */
    public AWTShapeExtruder(AWTShapeExtrusion extrudePath) {
    	this(0.01, extrudePath);
    }

    /**
     * Constructs an AWTShapeExtruder object from the specified AWTShapeExtrusion
     * object, using the specified tessellation tolerance and the default values for 
     * the crease angle and the geometry transform.
     * <p>
     * The default values are as follows:
     * <ul>
     * crease angle : 0.767944871 radians (44 degrees)<br>
     * geometry transform : identity matrix<br>
     * </ul>
     * </p>
     * <P>
     * The AWTShapeExtrusion object contains the extrusion path to use on
     * the AWT Shape. Passing null for the AWTShapeExtrusion parameter 
     * results in no extrusion being done.
     *
     * @param tessellationTolerance the tessellation tolerance value
     * used in tessellating the AWT Shape.
     * This corresponds to the <code>flatness</code> parameter in
     * the <code>java.awt.Shape.getPathIterator</code> method.
     * @param extrudePath the extrusion path used to describe how
     * the edge of the AWT Shape varies along the Z axis.
     *
     */
    public AWTShapeExtruder(double tessellationTolerance, AWTShapeExtrusion extrudePath) {
    	this.tessellationTolerance = tessellationTolerance;
    	this.shapeExtrusion = extrudePath;
    }
    /**
     * Constructs an AWTShapeExtruder object from the specified AWTShapeExtrusion
     * object, using the specified tessellation tolerance and crease angle 
     * and the default value for the geometry transform.
     * <P>
     * The default value is as follows:
     * <ul>
     * geometry transform : identity matrix<br>
     * </ul></P>
     * <P>
     * The AWTShapeExtrusion object contains the extrusion path to use on
     * the AWT Shape. Passing null for the AWTShapeExtrusion parameter 
     * results in no extrusion being done.
     * @param tessellationTolerance the tessellation tolerance value
     * used in tessellating the AWT Shape.
     * This corresponds to the <code>flatness</code> parameter in
     * the <code>java.awt.Shape.getPathIterator</code> method.
     * @param extrudePath the extrusion path used to describe how
     * the edge of the AWT Shape varies along the Z axis.
     * @param creaseAngle an angle between surface normals in radians to determin if 
     * adjacent triangles are shaded smoothly across the edge, clamped to [0, Math.PI]
     */
    public AWTShapeExtruder(double tessellationTolerance, AWTShapeExtrusion extrudePath, double creaseAngle) {
    	this.tessellationTolerance = tessellationTolerance;
    	this.shapeExtrusion = extrudePath;
    	setCreaseAngle(creaseAngle);
    }

    /**
     * Returns the AWTShapeExtrusion object used to describe how
     * the edge of the AWT Shape varies along the Z axis.<P>
     *
     * The default extrusion path is <code>null</code>.
     *  
     * @return the extrusion path of this AWTShapeExtruder object
     */
    public AWTShapeExtrusion getShapeExtrusion() {
    	return shapeExtrusion;
    }
    
    /**
     * Sets the AWTShapeExtrusion object used to describe how
     * the edge of the AWT Shape varies along the Z axis. 
     * Passing null results in no extrusion being done.<P>
     *
     * The default extrusion path is <code>null</code>.
     * 
     * @param extrudePath the extrusion path used to describe how
     * the edge of the AWT Shape varies along the Z axis.
     */
    public void setShapeExtrusion(AWTShapeExtrusion extrudePath) {
        this.shapeExtrusion = extrudePath;
    }
    
    /**
     * Returns the tessellation tolerance with which the geometry of the AWT Shape 
     * will be created.<P>
     * 
     * The default tolerance value is 0.01.
     * 
     * @return the tessellation tolerance used by this AWTShapeExtruder
     */
    public double getTessellationTolerance() {
    	return tessellationTolerance;
    }
    /**
     * Sets the tessellation tolerance with which the geometry of the AWT Shape 
     * will be created.<P>
     * 
     * The default tolerance value is 0.01.
     * 
     * @param tessellationTolerance the tessellation tolerance value
     * used in tessellating the AWT Shape
     */
    public void setTessellationTolerance(double tessellationTolerance) {
    	this.tessellationTolerance = tessellationTolerance;
    }

    /**
     * Returns the crease angle.<P> 
     * 
     * The default value is 0.767944871 radians (44 degrees)
     * 
     * @return the crease angle in radians
     */
    public double getCreaseAngle() {
    	return creaseAngle;
    }
    
    /**
     * Sets the crease angle in radians. 
     * The specified value will be clamped to [0, Math.PI]. <P>
     * 
     * The default value is 0.767944871 radians (44 degrees)
     * 
     * @param creaseAngle an angle between surface normals to determin if 
     * adjacent triangles are shaded smoothly across the edge
     */
    public void setCreaseAngle(double creaseAngle) {
    	this.creaseAngle = Math.max(0, Math.min(Math.PI, creaseAngle));
    }
    
    // V 1.7
    /**
     * Copies the transform component of this AWTShapeExtruder object 
     * into the passed Transform3D object.<P> 
     * 
     * The default transform is the identity matrix.
     * 
     * @param transform the Transform3D object to be copied into 
     * @exception NullPointerException if transform is <code>null</code>
     */
    public void getGeometryTransform(Transform3D transform) {
        if (transform == null)
            throw new NullPointerException("transform is null");
        transform.set(coordTransform);
    }
    // V 1.7
    /**
     * Sets the transform component of this AWTShapeExtruder object 
     * to the value of the passed transform.<P>
     * 
     * The specified transform will be applied to the generated coordinates and normals
     * in addition to a shape's affine transformation resp. a string's position value.<P>
     * 
     * The font size of a text string and the tesselation tolerance determine 
     * the precision and resolution of the tesselated geometry. These values 
     * should be set according to the desired quality.
     * 
     * Finally, this transform allows to scale the geometry to achieve 
     * a scene conform character size. 
     * Of course, rotation and translation components can be set as well.<P>
     * 
     * See also the sample code on this javadoc's overview page.<P>
     * 
     * The default transform is the identity matrix, 
     * so that the generated geometry remains unchanged.
     * 
     * @param transform the Transform3D object to be copied
     * @exception NullPointerException if transform is <code>null</code>
     * @exception IllegalArgumentException if transform is not invertable
     */
    public void setGeometryTransform(Transform3D transform) {
        
        if (transform == null)
            throw new NullPointerException("transform is null");
        
        if ((isIdentityTransform = ((transform.getType() & Transform3D.IDENTITY) != 0))) {
            coordTransform.setIdentity();
            normalTransform.setIdentity();
            return;
        }
                
        Transform3D invertTransposeT3D = new Transform3D(transform);
        
        // TODO ORTHOGONAL | RIGID | CONGRUENT
        if ((transform.getType() & Transform3D.ORTHOGONAL) == 0) { 
            try {
                invertTransposeT3D.invert();
                invertTransposeT3D.transpose();
            }
            catch (SingularMatrixException e) {
                throw new IllegalArgumentException("transform is not invertable", e);
            }
        }
        
        // Ease bounds transform
        float[] mat = new float[16];
        transform.get(mat);
        isScaleTranslation = 
               (mat[1] == 0.0f && mat[2] == 0.0f && 
                mat[4] == 0.0f && mat[6] == 0.0f && 
                mat[8] == 0.0f && mat[9] == 0.0f && 
                mat[12] == 0.0f && mat[13] == 0.0f && mat[14] == 0.0f && mat[15] == 1.0f);
        
        coordTransform.set(transform);
        normalTransform.set(invertTransposeT3D);
/*        
System.out.println("isScaleTranslation = " + isScaleTranslation);
        
System.out.println(coordTransform.toString());
System.out.println("-----------------------------------------");
System.out.println(normalTransform.toString());
*/
    }

    /**
     * Returns a GeometryArray for the given AWT Shape object 
     * according to the specified extrusion path, tessellation tolerance,
     * crease angle, and geometry transform of this AWTShapeExtruder object. 
     * 
     * If an optional AffineTransform is specified, the coordinates returned
     * by the shape's path iterator are transformed accordingly.
     * <p>
     * The returned GeometryArray includes triangle primitives and normals 
     * which are determined by the current crease angle.
     *
     * @param shape AWT Shape from which to generate a tessellated extruded geometry
     * @param trans an optional AffineTransform to be applied to the coordinates 
     * of the shape's path iterater, or <code>null</code> if untransformed coordinates are desired
     * @return a GeometryArray or <code>null</code> if no GeometryArray could be created
     */
    public GeometryArray getGeometry(Shape shape, AffineTransform trans) {		
	    
        PathIterator pIt = shape.getPathIterator(trans, tessellationTolerance);
        
        GeometryArray geomArray = createGeometry(pIt, false, true); // by copy, Geometry transform
        
        return geomArray;
    }
    
    // V 1.4 
    /**
     * Returns a GeometryArray representing the outline of the given AWT Shape object
     * according to the specified tessellation tolerance and geometry transform 
     * of this AWTShapeExtruder object.
     * 
     * Neither the extrusion path nor the crease angle attributes are applied.
     * 
     * If an optional AffineTransform is specified, the coordinates returned
     * by the shape's path iterator are transformed accordingly.
     * <p>
     * The returned GeometryArray includes line primitives and normals 
     * which are directed along the positive Z axis (0, 0, 1).
     * 
     * @param shape AWT Shape from which to generate an outline geometry
     * @param trans an optional AffineTransform to be applied to the coordinates 
     * of the shape's path iterater, or <code>null</code> if untransformed coordinates are desired
     * @return a GeometryArray or <code>null</code> if no GeometryArray could be created
     */
    public GeometryArray getOutlineGeometry(Shape shape, AffineTransform trans) {      
        
        PathIterator pIt = shape.getPathIterator(trans, tessellationTolerance);
        // incl normals, by copy, geometry transform
        GeometryArray geomArray = createLineStripGeometry(pIt, true, false, true);
        
        return geomArray;
    }
    
    /** 
     * Returns a GeometryArray for the given characters
     * according to the specified extrusion path, tessellation tolerance,
     * crease angle, and geometry transform of this AWTShapeExtruder object 
     * 
     * and according to the specified Font object which describes the font name 
     * (Helvetica, Courier, etc.), the font style (bold, italic, etc.), and point size. 
     * The flip value allows to reverse the Font's Y-axis downwards orientation. 
     * <p>
     * The returned GeometryArray includes triangle primitives and normals 
     * which are determined by the current crease angle.
     * <p>
     * Sample characters:
     * <pre>
     * char[] chars0 = {'A'};
     * 
     * char[] chars1 = {'Y', 'e', 's', ' ', '?'};
     * 
     * char[] chars2 = new String("Text").toCharArray();
     * 
     * // Unicode code point
     * int codePoint = 64; 
     * char[] chars3 = Character.toChars(codePoint);
     * </pre>
     * </p>
     * @param chars characters from which to generate tessellated glyphs
     * @param font the Java 2D Font used to create the glyphs
     * @param flip if <code>true</code> the Font's Y-axis downwards orientaton is reversed
     * @return a GeometryArray or <code>null</code> if no GeometryArray could be created
     * 
     * @see String3D
     */
    public GeometryArray getGeometry(char[] chars, Font font, boolean flip) {
    		    
	    Shape shape = createAWTShape(chars, font, flip);
	    	    
        PathIterator pIt = shape.getPathIterator(null, tessellationTolerance);
        
        GeometryArray geomArray = createGeometry(pIt, false, true); // by copy, Geometry transform
        
        return geomArray;
    }
    /**
     * Returns an AWT Shape of the specified characters according to 
     * the specified Font object which describes the font name (Helvetica, Courier, etc.), 
     * the font style (bold, italic, etc.), and point size. 
     * The flip value allows to reverse the Font's Y-axis downwards orientation. 
     * <p>
     * Sample characters:
     * <pre>
     * char[] chars0 = {'A'};
     * 
     * char[] chars1 = {'Y', 'e', 's', ' ', '?'};
     * 
     * char[] chars2 = new String("Text").toCharArray();
     * 
     * // Unicode code point
     * int codePoint = 64; 
     * char[] chars3 = Character.toChars(codePoint);
     * </pre>
     * </p>
     * 
     * @param chars characters from which to generate an AWT Shape
     * @param font the Java 2D Font used to create the glyphs
     * @param flip if <code>true</code> the Font's Y-axis downwards orientation is reversed
     * @return an AWT Shape
     */
    public static Shape createAWTShape(char[] chars, Font font, boolean flip) {
    	
    	// isAntiAliased, usesFractionalMetrics
		FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);

        GlyphVector gv = font.createGlyphVector(frc, chars);

	    Shape shape = gv.getOutline();

	    if (!flip) 
	    	return shape;
    
		// Font Y-axis is downwards, so send affine transform to flip it.
	    AffineTransform trans = new AffineTransform();	    
	    /*
	    Rectangle2D bnd = gv.getVisualBounds();
	    double tx = bnd.getX() + 0.5 * bnd.getWidth();
	    double ty = bnd.getY() + 0.5 * bnd.getHeight();
	    trans.setToTranslation(-tx, -ty);
	    trans.scale(1.0, -1.0);
	    trans.translate(tx, -ty);
	    */	    
	    // V 1.3
	    trans.setToScale(1.0, -1.0); // Mirrored at y = 0, no center transform needed !
	    
	    return trans.createTransformedShape(shape);
    }
    
    //
    // Geometry transform  V 1.7
    //
    
    void transformCoords(float[] coords) {
        if (isIdentityTransform == false)
            this.transformGeometry(coordTransform, coords, false);
    }    
    
    void transformNormals(float[] coords) {
        if (isIdentityTransform == false)
            this.transformGeometry(normalTransform, coords, true);
    }
    
    void transformBounds(BoundingBox bounds, float[] coords) {
        if (isIdentityTransform)
            return;
            
        if (isScaleTranslation) {
            transformAxisAligned(bounds);
        }
        else {
            ArrayList<float[]> coordsList = new ArrayList<float[]>(1);
            coordsList.add(coords);
            this.transformBounds(bounds, coordsList);
        }
    }
    
    // coordsList : !empty, elements != null; coords.length%3 == 0  !!
    void transformBounds(BoundingBox bounds, ArrayList<float[]> coordsList) {
        if (isIdentityTransform)
            return;
            
        if (isScaleTranslation) {
            transformAxisAligned(bounds);
        }
        // Bounds generated from coordinates
        else {
            
            float[] coords0 = coordsList.get(0);
            
            float lx = coords0[0]; 
            float ly = coords0[1]; 
            float lz = coords0[2];
            float ux = lx;
            float uy = ly; 
            float uz = lz; 
            
            float x, y, z;
            
            for (float[] coords : coordsList) {
                if (coords == null)
                    continue;
                
                for (int i=0,l=coords.length; i < l; i+=3) {
                    x = coords[i];
                    if      (x < lx)    lx = x;
                    else if (x > ux)    ux = x;
                    
                    y = coords[i+1];
                    if      (y < ly)    ly = y;
                    else if (y > uy)    uy = y;
                    
                    z = coords[i+2];
                    if      (z < lz)    lz = z;
                    else if (z > uz)    uz = z;
                }
            }
            
            bounds.setLower(new Point3d(lx, ly, lz));
            bounds.setUpper(new Point3d(ux, uy, uz));
        }
    }
    
    private void transformAxisAligned(BoundingBox bounds) {
        Point3d p3d = new Point3d();            
        
        bounds.getLower(p3d);
        coordTransform.transform(p3d);
        bounds.setLower(p3d);
        
        bounds.getUpper(p3d);            
        coordTransform.transform(p3d);           
        bounds.setUpper(p3d);
    }
    
    // Transform coordinates or normals
    private void transformGeometry(Transform3D transform, float[] triple, boolean isNormal) {
        
        int length = triple.length;        
        if (length < 3 || length%3 != 0)
            throw new IllegalArgumentException("length < 3 || length%3 != 0");       
        int tripleCt = length/3;
        
        float[] mat = new float[16];
        transform.get(mat);
        
        float x = 0;
        float y = 0;
        float z = 0;
        int   k = 0;
        
        if (isNormal) {
            
            float xt = 0;
            float yt = 0;
            float zt = 0;

            for (int i=0; i < tripleCt; i++) {
                
                x = triple[k++];
                y = triple[k++];
                z = triple[k++];
                
                // Transform
                xt = mat[0]*x + mat[1]*y + mat[2]*z;
                yt = mat[4]*x + mat[5]*y + mat[6]*z;
                zt = mat[8]*x + mat[9]*y + mat[10]*z;
                
                // Normalize
                float nLength = (float)Math.sqrt(xt*xt + yt*yt + zt*zt);
                if (nLength != 0) 
                    nLength = 1.0f/nLength;
                else
                    nLength = 1; // Exception ??
                
                k -= 3;               
                triple[k++] = xt*nLength;
                triple[k++] = yt*nLength;
                triple[k++] = zt*nLength;
            }        
        }
        else {
            for (int i=0; i < tripleCt; i++) {
                
                x = triple[k++];
                y = triple[k++];
                z = triple[k++];
                
                k -= 3;
                
                triple[k++] = mat[0]*x + mat[1]*y + mat[2]*z + mat[3];
                triple[k++] = mat[4]*x + mat[5]*y + mat[6]*z + mat[7];
                triple[k++] = mat[8]*x + mat[9]*y + mat[10]*z + mat[11];
            }        
        }
    }
    
    // Transform coordinates
    private void transformCoords(Transform3D transform, ArrayList<? extends Tuple3f> triple) {
        
        float[] mat = new float[16];
        transform.get(mat);
        
        float x = 0;
        float y = 0;
        float z = 0;
        
        Tuple3f t3f = null;
        
        for (int i=0,ct=triple.size(); i < ct; i++) {
            
            t3f = triple.get(i);
            
            x = t3f.x;
            y = t3f.y;
            z = t3f.z;
            
            t3f.x = mat[0]*x + mat[1]*y + mat[2]*z  + mat[3];
            t3f.y = mat[4]*x + mat[5]*y + mat[6]*z  + mat[7];
            t3f.z = mat[8]*x + mat[9]*y + mat[10]*z + mat[11];
        }        
    }
    
    //
    // Outline geometry  V 1.4
    //
    LineStripArray createLineStripGeometry(PathIterator pIt, 
                                           boolean inclNormals, 
                                           boolean byReference,
                                           boolean geomTransform) {
        
        int flag= -1, numPoints = 0, num=0;
        
        ArrayList<Point3f> coordsList = new ArrayList<Point3f>(50);
        float tmpCoords[] = new float[6];
        
        float lastX= .0f, lastY= .0f;
        float firstPntx = Float.MAX_VALUE, firstPnty = Float.MAX_VALUE;

        // Collects num vertices of each countour
        ArrayList<Integer> contours = new ArrayList<Integer>(10);
        
        Point3f vertex = null;
        
        while (!pIt.isDone()) {
            
            vertex = new Point3f();
            flag = pIt.currentSegment(tmpCoords);
            
            // The segment type constant that specifies that 
            // the preceding subpath should be closed by appending a line segment 
            // back to the point corresponding to the most recent SEG_MOVETO.
            if (flag == PathIterator.SEG_CLOSE){
                
                // Single Contour done
                if (num > 0) {
                    if (num > 2) {
                        // first vertex == last vertex needed !!
                        int lastIndVert = coordsList.size()-1;
                        Point3f lastVertex = coordsList.get(lastIndVert);
                        //
                        if (firstPntx != lastVertex.x || firstPnty != lastVertex.y) {
                            coordsList.add(new Point3f(firstPntx, firstPnty, 0));
                            num++;
                            numPoints++;    
                        }   
                    }
                    
                    if (num > 0) {        
                        // Min length of contour 3 !! 
                        if (num < 3) {
                            int lastIndex = coordsList.size()-1;
                            coordsList.remove(lastIndex);
                            if (num == 2)
                                coordsList.remove(lastIndex-1);
                            numPoints -= num;                           
                        }
                        else {
                            contours.add(num);
                        }
                            
                        num = 0;
                    }
                }
            } 
            // The segment type constant for a point that specifies
            // the starting location for a new subpath.
            else if (flag == PathIterator.SEG_MOVETO){
                 vertex.x = tmpCoords[0];
                 vertex.y = tmpCoords[1];
                 lastX = vertex.x;
                 lastY = vertex.y;
        
                 if ((lastX == firstPntx) && (lastY == firstPnty)) {
                     pIt.next();
                     continue;
                 }
                 
                 // No SEG_CLOSE TODO
                 // Single Contour done
                 if (num > 0){
                     if (num > 2) {
                         // first vertex == last vertex needed !!
                         int lastIndVert = coordsList.size()-1;
                         Point3f lastVertex = coordsList.get(lastIndVert);
                         //
                         if (firstPntx != lastVertex.x || firstPnty != lastVertex.y) {
                             coordsList.add(new Point3f(firstPntx, firstPnty, 0));
                             num++;
                             numPoints++;    
                         }
                     }
                    
                     if (num > 0) {
                         // Min length of contour 3 !! 
                         if (num < 3) {
                             int lastIndex = coordsList.size()-1;
                             coordsList.remove(lastIndex);
                             if (num == 2)
                                 coordsList.remove(lastIndex-1);
                             numPoints -= num;                           
                         }
                         else {
                             contours.add(num);
                         }
                         
                         num = 0;
                     }
                 }
                 
                 firstPntx = lastX;
                 firstPnty = lastY;
                 coordsList.add(vertex);
                 num++;
                 numPoints++;
            } 
            // The segment type constant for a point that specifies
            // the end point of a line to be drawn from the most recently specified point.
            else if (flag == PathIterator.SEG_LINETO){
                 vertex.x = tmpCoords[0];
                 vertex.y = tmpCoords[1];
                 
                 // Check here for duplicate points
                 if ((vertex.x == lastX) && (vertex.y == lastY)) {
                     pIt.next();
                     continue;
                 }
                 
                 lastX = vertex.x;
                 lastY = vertex.y;
                 
                 coordsList.add(vertex);
                 num++;
                 numPoints++;
            } 
                
            pIt.next();
        }
        
        // No data(e.g space, control characters)
        // Two point can't form a valid contour
        if (numPoints == 0){
            return null;
        }
        
        // contour = strip !?
        int stripCt = contours.size();
        int vertexCt = 0;
        
        int[] stripVertexCounts = new int[stripCt];
        
        for (int i=0; i < stripCt; i++) {
            stripVertexCounts[i] = contours.get(i);    
            vertexCt += stripVertexCounts[i]; 
        }
                
        int vertexFormat = LineStripArray.COORDINATES;
        if (inclNormals)
            vertexFormat |= LineStripArray.NORMALS;
        if (byReference)
            vertexFormat |= LineStripArray.BY_REFERENCE;
        
        LineStripArray lineStripArray = new LineStripArray(vertexCt, vertexFormat, 
                                                           stripVertexCounts);
        if (byReference) {
            int k = 0;
            
            float[] coords = new float[vertexCt * 3];
        
            for (int i=0; i < vertexCt; i++) {
                vertex = coordsList.get(i);
                coords[k++] = vertex.x;
                coords[k++] = vertex.y;  
                k++; // z = 0
            }
            // transform coords
            if (geomTransform && !isIdentityTransform)
                this.transformGeometry(coordTransform, coords, false);            
            
            lineStripArray.setCoordRefFloat(coords);
            
            if (inclNormals) {
                k = 0;
                
                float[] normals = new float[vertexCt * 3];
    
                // transform normals
                if (geomTransform && !isIdentityTransform) {
                    float[] normalArr = {0, 0, 1};
                    this.transformGeometry(normalTransform, normalArr, true);
                    
                    for (int i=0; i < vertexCt; i++) {
                        normals[k++] = normalArr[0];
                        normals[k++] = normalArr[1];
                        normals[k++] = normalArr[2]; 
                    }
                }
                else {
                    for (int i=0; i < vertexCt; i++) {
                        k += 2; // x = y = 0
                        normals[k++] = 1; 
                    }
                }
                               
                lineStripArray.setNormalRefFloat(normals);
            }
        }
        // byCopy
        else {
            if (inclNormals) {
                
                Vector3f normal = new Vector3f(0, 0, 1);
                
                if (geomTransform && !isIdentityTransform) {
                    // transform coords
                    this.transformCoords(coordTransform, coordsList);
                    // transform normal
                    float[] normalArr = {0, 0, 1};
                    this.transformGeometry(normalTransform, normalArr, true);
                    normal.set(normalArr);                   
                }               
                
                for (int i=0; i < vertexCt; i++) {              
                    lineStripArray.setCoordinate(i, coordsList.get(i));
                    lineStripArray.setNormal(i, normal);
                }
            }
            else {
                // transform coords
                if (geomTransform && !isIdentityTransform)
                    this.transformCoords(coordTransform, coordsList);
                
                for (int i=0; i < vertexCt; i++)              
                    lineStripArray.setCoordinate(i, coordsList.get(i));
            }
        }
               
        coordsList = null;   
        contours = null;
        
        return lineStripArray;
    }
    
    // 
    // Extruded geometry
    //
    GeometryArray createGeometry(PathIterator pIt, boolean byReference,
                                                   boolean geomTransform) {		

        int flag= -1, numPoints = 0, i, j, k, num=0, vertCnt;
        
        ArrayList<Point3f> coordsList = new ArrayList<Point3f>();
        float tmpCoords[] = new float[6];
        
        float lastX= .0f, lastY= .0f;
        float firstPntx = Float.MAX_VALUE, firstPnty = Float.MAX_VALUE;

        GeometryInfo gi = null;

        // Collects num vertices of each countour
        ArrayList<Integer> contours = new ArrayList<Integer>(10);
        
        float maxY = -Float.MAX_VALUE;
        int maxYIndex = 0, beginIdx = 0, endIdx = 0, start = 0;
        int[] lastMaxYIndices = new int[3];

        boolean setMaxY = false;
        
        Point3f vertex = null;
        
        while (!pIt.isDone()) {
            
            vertex = new Point3f();
            
            flag = pIt.currentSegment(tmpCoords);
            
	    	// The segment type constant that specifies that 
	    	// the preceding subpath should be closed by appending a line segment 
	    	// back to the point corresponding to the most recent SEG_MOVETO.
            if (flag == PathIterator.SEG_CLOSE){
                
            	// Single Contour done
            	if (num > 0) {
//System.out.println("AWTShapeExtruder SEG_CLOSE num = " + num);
            	    if (num > 2) {
                    	// V 1.1
    					// first vertex != last vertex needed !!
                        int lastIndVert = coordsList.size()-1;
                        Point3f lastVertex = coordsList.get(lastIndVert);
    					//
    	                if (firstPntx == lastVertex.x && firstPnty == lastVertex.y) {
    					    coordsList.remove(lastIndVert);
    					    num--;
    					    numPoints--;	
    					    
    					    /* V 1.6 'lastIndVert == maxYIndex' can't happen, 
    					     *       because in this case  'first index = maxYIndex'
    					    // V 1.2 Check if maxYIndex is removed
    					    if (lastIndVert == maxYIndex) {
    					        int lastIndMax = maxYIndices.size()-1;
    					        
    					        maxYIndex = maxYIndices.get(lastIndMax-1);
    					        maxY = coordsList.get(maxYIndex).y;
    					        
    					        maxYIndices.remove(lastIndMax);
    					    }
    					    */
/*				    
System.out.println("AWTShapeExtruder first Pnt = " + firstPntx + " / " + firstPnty);
System.out.println("AWTShapeExtruder lastVertex = " + lastVertex);
System.out.println("AWTShapeExtruder SEG_CLOSE first vertex != last vertex needed numPoints = " + numPoints);
	*/
					    }   
            	    }
					
	            	if (num > 0) {
						// V 1.1
						// Min length of contour: 3 !! 
		            	if (num < 3) {
		            		int lastIndex = coordsList.size()-1;
		            		coordsList.remove(lastIndex);
		            		if (num == 2)
		            			coordsList.remove(lastIndex-1);
						    numPoints -= num;						    
                            num = 0;
                            // v 1.6 Reset maxYIndices
                            if (setMaxY) {
                                maxYIndex = lastMaxYIndices[0];
                                beginIdx = lastMaxYIndices[1];
                                endIdx = lastMaxYIndices[2];
                            }
		            	}
		            	else {
		                    if (setMaxY) {
		                        // Get Previous point
		                        beginIdx = start;
		                        endIdx = numPoints-1;
		                        
                                lastMaxYIndices[0] = maxYIndex;
                                lastMaxYIndices[1] = beginIdx;
                                lastMaxYIndices[2] = endIdx;
		                    }
		                    
		                    contours.add(num);
		                    num = 0;
		                }
	            	}
            	}
            } 
			// The segment type constant for a point that specifies
			// the starting location for a new subpath.
            else if (flag == PathIterator.SEG_MOVETO){
                 vertex.x = tmpCoords[0];
                 vertex.y = tmpCoords[1];
                 lastX = vertex.x;
                 lastY = vertex.y;

                 if ((lastX == firstPntx) && (lastY == firstPnty)) {
                     pIt.next();
                     continue;
                 }
                 
                 // 'num > 0' -> no SEG_CLOSE -> finish the contour here  TODO or throw an exception
                 // Single Contour done
                 if (num > 0){
					// V 1.1
					// first vertex != last vertex needed !!
					if (firstPntx == tmpCoords[0] && firstPnty == tmpCoords[1]) {
					    coordsList.remove(coordsList.get(coordsList.size()-1));
					    num--;
					    numPoints--;						    
					}   
					
					if (num > 0) {
                        // Min length of contour: 3 !! 
						if (num < 3) {
							int lastIndex = coordsList.size()-1;
							coordsList.remove(lastIndex);
							if (num == 2)
								coordsList.remove(lastIndex-1);
						    num = 0;
						    numPoints -= num;						    
                            // v 1.6 Reset maxYIndices
                            if (setMaxY) {
                                maxYIndex = lastMaxYIndices[0];
                                beginIdx = lastMaxYIndices[1];
                                endIdx = lastMaxYIndices[2];
                            }
						}
						else {
					        if (setMaxY) {
					            // Get Previous point
					            beginIdx = start;
					            endIdx = numPoints-1;
					            
					            lastMaxYIndices[0] = maxYIndex;
					            lastMaxYIndices[1] = beginIdx;
					            lastMaxYIndices[2] = endIdx;
					        }
        
					        contours.add(num);
					        num = 0;
						}
					}
                 }
                 
                 firstPntx = lastX;
                 firstPnty = lastY;
                 
                 // begin index of this new contour
                 start = numPoints; // v 1.6 
                 setMaxY = false;
                 if (vertex.y > maxY) {
                     maxY = vertex.y;
                     maxYIndex = numPoints;
                     setMaxY = true;                     
                 }
             
                 coordsList.add(vertex);
                 num++;
                 numPoints++;
            } 
			// The segment type constant for a point that specifies
			// the end point of a line to be drawn from the most recently specified point.
            else if (flag == PathIterator.SEG_LINETO){
                 vertex.x = tmpCoords[0];
                 vertex.y = tmpCoords[1];
                 
                 //Check here for duplicate points. 
                 //Code later in this function can not handle duplicate points.
                 if ((vertex.x == lastX) && (vertex.y == lastY)) {
                     pIt.next();
                     continue;
                 }
                 
                 if (vertex.y > maxY) {
                     maxY = vertex.y;
                     maxYIndex = numPoints;
                     setMaxY = true;                     
                 }
                 
                 lastX = vertex.x;
                 lastY = vertex.y;
                 
                 coordsList.add(vertex);
                 num++;
                 numPoints++;
            } 
            else {
                // TODO throw new IllegalStateException(""); ??
                System.out.println("AWTShapeExtruder flag = pIt.currentSegment = " + flag);
            }
            	
            pIt.next();
        }
        
        // No data(e.g space, control characters)
        // Two point can't form a valid contour
        if (numPoints == 0){
        	return null;
        }

        // Determine font winding order use for side triangles
        Point3f p1 = new Point3f(), p2 = new Point3f(), p3 = new Point3f();
        boolean flip_side_orient = true;

        Point3f[] vertices = new Point3f[coordsList.size()]; 
        coordsList.toArray(vertices);
/*      
Point3f p3f = null;        
for (int n=0; n < vertices.length; n++) {
    p3f = vertices[n];
    System.out.println(" vertices = " + p3f.x + " / " + p3f.y + " / " + p3f.z);
}

System.out.println("AWTShapeExtruder maxYIndex = " + maxYIndex);
System.out.println("AWTShapeExtruder beginIdx  = " + beginIdx);
System.out.println("AWTShapeExtruder endIdx    = " + endIdx);
*/ 
        if (endIdx - beginIdx > 0) {
            // must be true unless it is a single line
            // define as "MoveTo p1 LineTo p2 Close" which is
            // not a valid font definition.

            // P1 : at maxYIndex - 1
            if (maxYIndex == beginIdx) {
                p1.set(vertices[endIdx]);		    
            } 
            else {
                p1.set(vertices[maxYIndex-1]);		    
            }
            
            // P2 : at maxYIndex
            p2.set(vertices[maxYIndex]);	
            
            // P3 : at maxYIndex + 1
            if (maxYIndex == endIdx) {
                p3.set(vertices[beginIdx]);
            } 
            else {
                p3.set(vertices[maxYIndex+1]);
            }
/*            
System.out.println("AWTShapeExtruder p1    = " + p1.x + " / " + p1.y + " / " + p1.z);
System.out.println("AWTShapeExtruder p2    = " + p2.x + " / " + p2.y + " / " + p2.z);
System.out.println("AWTShapeExtruder p3    = " + p3.x + " / " + p3.y + " / " + p3.z);
*/
            if (p3.x != p2.x) {
                if (p1.x != p2.x) {
                    // Use the one with smallest slope
                    if (Math.abs((p2.y - p1.y)/(p2.x - p1.x)) >
                        Math.abs((p3.y - p2.y)/(p3.x - p2.x))) {
                        flip_side_orient = (p3.x > p2.x);
                    } 
                    else {
                        flip_side_orient = (p2.x > p1.x);
                    }
                } 
                else {
                    flip_side_orient = (p3.x > p2.x);			
                }
            } 
            else {
                // p1.x != p2.x, otherwise all three
                // point form a straight vertical line with
                // the middle point the highest. This is not a
                // valid font definition.
                flip_side_orient = (p2.x > p1.x);
            }
        }
        
//System.out.println("AWTShapeExtruder flip_side_orient = " + flip_side_orient);
//System.out.println("AWTShapeExtruder contours.size = " + contours.size());

        // Build a Tree of Islands
        int  startIdx = 0;
        IslandsNode islandsTree = new IslandsNode(-1, -1);

        for (i= 0;i < contours.size(); i++) {
            endIdx = startIdx + contours.get(i);
            islandsTree.insert(new IslandsNode(startIdx, endIdx), vertices);
            startIdx = endIdx;
        }      

        coordsList = null;   // Free memory 
        contours = null;

        // Compute islandCounts[][] and outVerts[][]
        ArrayList<IslandsNode> islandsList = new ArrayList<IslandsNode>();
        islandsTree.collectOddLevelNode(islandsList, 0);

        IslandsNode[] nodes = new IslandsNode[islandsList.size()];
        islandsList.toArray(nodes);
        
        int numIslands = islandsList.size();
               
        int[][]     islandCounts = new int[numIslands][];
        Point3f[][] outVerts     = new Point3f[numIslands][];
        
        int nchild, sum;
        IslandsNode node;	    

        for (i=0; i < numIslands; i++) {
            
            node = nodes[i];
            nchild = node.numChild();
            
 			islandCounts[i] = new int[nchild + 1];
 			
            islandCounts[i][0] = node.numVertices();
            sum = 0;
            sum += islandCounts[i][0];
            for (j=0; j < nchild; j++) {
                islandCounts[i][j+1] = node.getChild(j).numVertices();
                sum += islandCounts[i][j+1];
            } 
            
            outVerts[i] = new Point3f[sum];
            startIdx = 0;
            for (k=node.startIdx; k < node.endIdx; k++) {
                outVerts[i][startIdx++] = vertices[k];
            }

            for (j=0; j < nchild; j++) {
                endIdx = node.getChild(j).endIdx;
                for (k=node.getChild(j).startIdx; k < endIdx; k++) {
                    outVerts[i][startIdx++] = vertices[k];
                }
            }            
        }

        islandsTree = null; // Free memory 
        islandsList = null;
        vertices = null;

        int[] contourCounts = new int[1];
        int currCoordIndex = 0, vertOffset = 0;

        ArrayList<Point3f[]> triaData = new ArrayList<Point3f[]>();

        numPoints = 0;
        //Now loop thru each island, calling triangulator once per island.
        //Combine triangle data for all islands together in one object.
        for (i=0; i < numIslands; i++) {
        	
            contourCounts[0] = islandCounts[i].length;
            numPoints += outVerts[i].length; 
            
            gi = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
            gi.setCoordinates(outVerts[i]);
            gi.setStripCounts(islandCounts[i]);
            gi.setContourCounts(contourCounts);
            // no normal generation

            GeometryArray ga = gi.getGeometryArray(false, false, false);

            // V 1.1
			Point3f[] triaPoints = removeDegeneratedTrias(ga);
			vertOffset += triaPoints.length;
			
			triaData.add(triaPoints);

        }
        
        // v 1.3
        boolean isFrontBackAndFlat = (shapeExtrusion != null && shapeExtrusion.getDepth() == 0.0f);
        
        // Single face
        if (shapeExtrusion == null) {
            vertCnt = vertOffset;
        }
        // Multiply by 2 since we create 2 faces of the font
        // Second term is for side-faces along depth of the font
        else {
            if (isFrontBackAndFlat) // v 1.3
                vertCnt = vertOffset * 2;
            else if (shapeExtrusion.getExtrusionShape() == null)
                vertCnt = vertOffset * 2 + numPoints * 6;
            else {
                vertCnt = vertOffset * 2 + numPoints * 6 * (shapeExtrusion.getExtrusion().length -1);
            }
        }
        
        // To create triangles for side faces, every vertex is duplicated currently.
        
        int vertexFormat = GeometryArray.COORDINATES | GeometryArray.NORMALS;
        if (byReference)
            vertexFormat |= GeometryArray.BY_REFERENCE;
        
		GeometryArray triAry = new TriangleArray(vertCnt, vertexFormat);
		
        float[] coords = new float[vertCnt * 3];
        float[] normals = new float[vertCnt * 3];		
		
        boolean[] flip_orient = new boolean[numIslands];
        
        Point3f q1 = new Point3f(), q2 = new Point3f(), q3 = new Point3f();

	    Vector3f nF = new Vector3f(0, 0, 1); 	// Normal for +z direction
	    Vector3f nB = new Vector3f(0, 0, -1);	// Normal for -z direction
        // Transform normals V 1.7
	    if (geomTransform && !isIdentityTransform) {
	        // Front face
	        float[] normalArrF = {0, 0, 1};
	        this.transformNormals(normalArrF);
	        nF.set(normalArrF);
	        // Back face : inverse of front face
            nB.set(-nF.x, -nF.y, -nF.z);
	    }
	    
	    Vector3f nz = new Vector3f(); // temp normal

        for (j=0; j < numIslands; j++) {
        	
        	Point3f[] triaPoints = (Point3f[])triaData.get(j);
            vertOffset = triaPoints.length;

            boolean findOrient = false;

            //Create the triangle array
            for (i=0; i < vertOffset; i+=3, currCoordIndex+=3) {
                //Get 3 points. Since triangle is known to be flat, normal
                // must be same for all 3 points.
			    p1.set(triaPoints[i]);
			    p2.set(triaPoints[i+1]);
			    p3.set(triaPoints[i+2]);
			    
                if (!findOrient) {
                    //Check here if triangles are wound incorrectly and need
                    //to be flipped.
                    if (!getNormal(p1, p2, p3, nz)) {
                    	// this creates a degenerated triangle: should never happen
//System.out.println("findOrient 0 = " + j + " / " + p1 + " / " + p2 + " / " +p3);
                        continue;
                    }

                    if (nz.z >= EPS) {// 0.000001f; +Z
                        flip_orient[j] = false;
                    } 
                    else if (nz.z <= -EPS) { // -Z
                        flip_orient[j] = true;
//System.out.println("findOrient j / i / flip_orient[j] = " +j +" / "+ i + " / true");
                    } 
                    else {
                    	// this creates a degenerated triangle: should never happen
//System.out.println("findOrient 1 = "+ nz.z + " / " + p1 + " / " + p2 + " / " +p3);
                        continue;
                    }                  
                    
                    findOrient = true;
                }
                
                if (flip_orient[j]){
                    //New Triangulator preserves contour orientation. If contour
                    //input is wound incorrectly, swap 2nd and 3rd points to
                    //sure all triangles are wound correctly for j3d.
                    q1.x = p2.x; q1.y = p2.y; q1.z = p2.z;
                    p2.x = p3.x; p2.y = p3.y; p2.z = p3.z;
                    p3.x = q1.x; p3.y = q1.y; p3.z = q1.z;
                }

                int index = currCoordIndex*3;
                
                if (shapeExtrusion != null) { // incl. isFrontBackAndFlat (depth == 0.0)
                    
                    // back shape triangle (p1, p3, p2)

                    coords[index] = p1.x;    
                    normals[index++] = nB.x;   
                    
                    coords[index] = p1.y;    
                    normals[index++] = nB.y;  
                    
                    coords[index] = p1.z;    
                    normals[index++] = nB.z;   
                    
                    
                    coords[index] = p3.x;    
                    normals[index++] = nB.x;   
                    
                    coords[index] = p3.y;    
                    normals[index++] = nB.y;  
                    
                    coords[index] = p3.z;    
                    normals[index++] = nB.z;   
                    
                    
                    coords[index] = p2.x;    
                    normals[index++] = nB.x;   
                    
                    coords[index] = p2.y;    
                    normals[index++] = nB.y;  
                    
                    coords[index] = p2.z;    
                    normals[index++] = nB.z;   
                    
                    q1.x = p1.x; q1.y = p1.y; q1.z = p1.z + shapeExtrusion.getDepth();
                    q2.x = p2.x; q2.y = p2.y; q2.z = p2.z + shapeExtrusion.getDepth();
                    q3.x = p3.x; q3.y = p3.y; q3.z = p3.z + shapeExtrusion.getDepth();
                    
                    // front shape triangle (q1, q2, q3)
                    
                    index = (currCoordIndex+vertOffset)*3;
                    
                    coords[index] = q1.x;    
                    normals[index++] = nF.x;   
                    
                    coords[index] = q1.y;    
                    normals[index++] = nF.y;  
                    
                    coords[index] = q1.z;    
                    normals[index++] = nF.z;   
                    
                    
                    coords[index] = q2.x;    
                    normals[index++] = nF.x;   
                    
                    coords[index] = q2.y;    
                    normals[index++] = nF.y;  
                    
                    coords[index] = q2.z;    
                    normals[index++] = nF.z;   
                    
                    
                    coords[index] = q3.x;    
                    normals[index++] = nF.x;   
                    
                    coords[index] = q3.y;    
                    normals[index++] = nF.y;  
                    
                    coords[index] = q3.z;    
                    normals[index++] = nF.z;        
                } 
                else { // No extrusion
                    
                    // front shape triangle (p1, p2, p3)

                    coords[index] = p1.x;    
                    normals[index++] = nF.x;   
                    
                    coords[index] = p1.y;    
                    normals[index++] = nF.y;  
                    
                    coords[index] = p1.z;    
                    normals[index++] = nF.z;   
                    
                    
                    coords[index] = p2.x;    
                    normals[index++] = nF.x;   
                    
                    coords[index] = p2.y;    
                    normals[index++] = nF.y;  
                    
                    coords[index] = p2.z;    
                    normals[index++] = nF.z;                       
                    
                    
                    coords[index] = p3.x;    
                    normals[index++] = nF.x;   
                    
                    coords[index] = p3.y;    
                    normals[index++] = nF.y;  
                    
                    coords[index] = p3.z;    
                    normals[index++] = nF.z;   
                }
            }
            
            if (shapeExtrusion != null) {		
                currCoordIndex += vertOffset;
            }
        }

        // Now add side triangles in both cases.

        // Since we duplicated triangles with different Z, make sure
        // currCoordIndex points to correct location.
        if (shapeExtrusion != null && !isFrontBackAndFlat){ // v 1.3
            
            // last known non-degenerate normal
            Vector3f goodNormal = new Vector3f();

            int index = currCoordIndex*3;
            
            // straight bevel extrusion shape, a straight line from 0.0 to depth
            if (shapeExtrusion.getExtrusionShape() == null) {
                
                Vector3f n1 = new Vector3f();
                Vector3f n2 = new Vector3f();
                           
                boolean smooth;
                // we'll put a crease if the angle between the normals is
                // greater than creaseAngle
                float cosine;
                // need the previous normals to check for smoothing
                Vector3f pn1 = null, pn2 = null;
                // need the next normals to check for smoothing
                Vector3f n3 = new Vector3f(), n4 = new Vector3f();
                //  store the normals for each point because they are
                // the same for both triangles
                Vector3f p1Normal = new Vector3f();
                Vector3f p2Normal = new Vector3f();
                Vector3f q1Normal = new Vector3f();
                Vector3f q2Normal = new Vector3f();

                for (i=0; i < numIslands; i++) {
                	
                    for (j=0, k=0, num =0; j < islandCounts[i].length; j++) {
                    	
                        num += islandCounts[i][j];
                        p1.x = outVerts[i][num - 1].x; 
                        p1.y = outVerts[i][num - 1].y; 
                        p1.z = 0.0f;
                        q1.x = p1.x; q1.y = p1.y; q1.z = p1.z+shapeExtrusion.getDepth();
                        p2.z = 0.0f;
                        q2.z = p2.z+shapeExtrusion.getDepth();
                        
                        for (int m=0; m < num;m++) {	      
                            p2.x = outVerts[i][m].x;
                            p2.y = outVerts[i][m].y;
                            q2.x = p2.x; 
                            q2.y = p2.y; 
                            if (getNormal(p1, q1, p2, n1)) {

                                if (!flip_side_orient) {
                                    n1.negate();
                                }
                                goodNormal.set(n1);
                                break;
                            }
                        }

                        for (; k < num; k++) { 
                            p2.x = outVerts[i][k].x;p2.y = outVerts[i][k].y;p2.z = 0.0f;
                            q2.x = p2.x; q2.y = p2.y; q2.z = p2.z+shapeExtrusion.getDepth();

                            if (!getNormal(p1, q1, p2, n1)) {
                                n1.set(goodNormal);
                            } 
                            else {
                                if (!flip_side_orient) {
                                    n1.negate();
                                }
                                goodNormal.set(n1);
                            }

                            if (!getNormal(p2, q1, q2, n2)) {
                                n2.set(goodNormal);
                            } 
                            else {
                                if (!flip_side_orient) {
                                    n2.negate();
                                }
                                goodNormal.set(n2);
                            }
                            // if there is a previous normal, see if we need to smooth
                            // this normal or make a crease

                            if (pn1 != null) {
                                cosine = n1.dot(pn2);
                                smooth = cosine > creaseAngle;
                                if (smooth) {
                                    p1Normal.x = (pn1.x + pn2.x + n1.x);
                                    p1Normal.y = (pn1.y + pn2.y + n1.y);
                                    p1Normal.z = (pn1.z + pn2.z + n1.z);
                                    normalize(p1Normal);

                                    q1Normal.x = (pn2.x + n1.x + n2.x);
                                    q1Normal.y = (pn2.y + n1.y + n2.y);
                                    q1Normal.z = (pn2.z + n1.z + n2.z);
                                    normalize(q1Normal);
                                } 
                                else {
                                    p1Normal.x = n1.x; p1Normal.y = n1.y; p1Normal.z = n1.z;
                                    q1Normal.x = n1.x+n2.x; 
                                    q1Normal.y = n1.y+n2.y;
                                    q1Normal.z = n1.z+ n2.z; 
                                    normalize(q1Normal);
                                } 
                            } 
                            else { // if pn1 == null
                                pn1 = new Vector3f();
                                pn2 = new Vector3f();
                                p1Normal.x = n1.x;
                                p1Normal.y = n1.y;
                                p1Normal.z = n1.z;

                                q1Normal.x = (n1.x + n2.x);
                                q1Normal.y = (n1.y + n2.y);
                                q1Normal.z = (n1.z + n2.z);
                                normalize(q1Normal);
                            } 

                            // if there is a next, check if we should smooth normal

                            if (k+1 < num) {
                                p3.x = outVerts[i][k+1].x; p3.y = outVerts[i][k+1].y; 
                                p3.z = 0.0f;
                                q3.x = p3.x; q3.y = p3.y; q3.z = p3.z + shapeExtrusion.getDepth();

                                if (!getNormal(p2, q2, p3, n3)) {
                                    n3.set(goodNormal);
                                } 
                                else {
                                    if (!flip_side_orient) {
                                        n3.negate();
                                    }
                                    goodNormal.set(n3);
                                }

                                if (!getNormal(p3, q2, q3, n4)) {
                                    n4.set(goodNormal);
                                } 
                                else {
                                    if (!flip_side_orient) {
                                        n4.negate();
                                    }
                                    goodNormal.set(n4);
                                }

                                cosine = n2.dot(n3);
                                smooth = cosine > creaseAngle;

                                if (smooth) {
                                    p2Normal.x = (n1.x + n2.x + n3.x);
                                    p2Normal.y = (n1.y + n2.y + n3.y);
                                    p2Normal.z = (n1.z + n2.z + n3.z);
                                    normalize(p2Normal);

                                    q2Normal.x = (n2.x + n3.x + n4.x);
                                    q2Normal.y = (n2.y + n3.y + n4.y);
                                    q2Normal.z = (n2.z + n3.z + n4.z);
                                    normalize(q2Normal);
                                } 
                                else { 
                                    p2Normal.x = n1.x + n2.x;
                                    p2Normal.y = n1.y + n2.y;
                                    p2Normal.z = n1.z + n2.z;
                                    normalize(p2Normal);
                                    q2Normal.x = n2.x; q2Normal.y = n2.y; q2Normal.z = n2.z;
                                } 
                            } 
                            else { // if k+1 >= num
                                p2Normal.x = (n1.x + n2.x);
                                p2Normal.y = (n1.y + n2.y);
                                p2Normal.z = (n1.z + n2.z);
                                normalize(p2Normal);

                                q2Normal.x = n2.x;
                                q2Normal.y = n2.y;
                                q2Normal.z = n2.z;
                            } 

                            // add pts for the 2 tris
                            // p1, q1, p2 and p2, q1, q2

                            if (flip_side_orient) {
                                
                                // (p1, q1, p2) (p2, q1, q2)

                                // 0
                                coords[index] = p1.x;    
                                normals[index++] = p1Normal.x;   
                                
                                coords[index] = p1.y;    
                                normals[index++] = p1Normal.y;  
                                
                                coords[index] = p1.z;    
                                normals[index++] = p1Normal.z;                                   
                                // 1
                                coords[index] = q1.x;    
                                normals[index++] = q1Normal.x;   
                                
                                coords[index] = q1.y;    
                                normals[index++] = q1Normal.y;  
                                
                                coords[index] = q1.z;    
                                normals[index++] = q1Normal.z;                                  
                                // 2
                                coords[index] = p2.x;    
                                normals[index++] = p2Normal.x;   
                                
                                coords[index] = p2.y;    
                                normals[index++] = p2Normal.y;  
                                
                                coords[index] = p2.z;    
                                normals[index++] = p2Normal.z;   
                                
                                
                                // 3
                                coords[index] = p2.x;    
                                normals[index++] = p2Normal.x;   
                                
                                coords[index] = p2.y;    
                                normals[index++] = p2Normal.y;  
                                
                                coords[index] = p2.z;    
                                normals[index++] = p2Normal.z;                                   
                                // 4
                                coords[index] = q1.x;    
                                normals[index++] = q1Normal.x;   
                                
                                coords[index] = q1.y;    
                                normals[index++] = q1Normal.y;  
                                
                                coords[index] = q1.z;    
                                normals[index++] = q1Normal.z;   
                                
                            } 
                            else {
                                
                                // (q1, p1, p2) (q1, p2, q2)
                                
                                // 0
                                coords[index] = q1.x;    
                                normals[index++] = q1Normal.x;   
                                
                                coords[index] = q1.y;    
                                normals[index++] = q1Normal.y;  
                                
                                coords[index] = q1.z;    
                                normals[index++] = q1Normal.z;                                   
                                // 1
                                coords[index] = p1.x;    
                                normals[index++] = p1Normal.x;   
                                
                                coords[index] = p1.y;    
                                normals[index++] = p1Normal.y;  
                                
                                coords[index] = p1.z;    
                                normals[index++] = p1Normal.z;                                   
                                // 2
                                coords[index] = p2.x;    
                                normals[index++] = p2Normal.x;   
                                
                                coords[index] = p2.y;    
                                normals[index++] = p2Normal.y;  
                                
                                coords[index] = p2.z;    
                                normals[index++] = p2Normal.z;   
                                

                                // 3
                                coords[index] = q1.x;    
                                normals[index++] = q1Normal.x;   
                                
                                coords[index] = q1.y;    
                                normals[index++] = q1Normal.y;  
                                
                                coords[index] = q1.z;    
                                normals[index++] = q1Normal.z;                                   
                                // 4
                                coords[index] = p2.x;    
                                normals[index++] = p2Normal.x;   
                                
                                coords[index] = p2.y;    
                                normals[index++] = p2Normal.y;  
                                
                                coords[index] = p2.z;    
                                normals[index++] = p2Normal.z;  
 
                            }

                            // 5
                            coords[index] = q2.x;    
                            normals[index++] = q2Normal.x;   
                            
                            coords[index] = q2.y;    
                            normals[index++] = q2Normal.y;  
                            
                            coords[index] = q2.z;    
                            normals[index++] = q2Normal.z;   
                            
                            
                            pn1.x = n1.x; pn1.y = n1.y; pn1.z = n1.z;
                            pn2.x = n2.x; pn2.y = n2.y; pn2.z = n2.z;
                            p1.x = p2.x; p1.y = p2.y; p1.z = p2.z;
                            q1.x = q2.x; q1.y = q2.y; q1.z = q2.z;

                        }// for k

                        // set the previous normals to null when we are done
                        pn1 = null;
                        pn2 = null;
                    }// for j
                }//for i
                
                // Geometry transform  V 1.7
                if (geomTransform && !isIdentityTransform) {
                    this.transformCoords(coords);
                    this.transformNormals(normals);                   
                }

                // V 1.2 set arrays
                if (byReference) {
                    triAry.setCoordRefFloat(coords);
                    triAry.setNormalRefFloat(normals);
                }
                else {
                    triAry.setCoordinates(0, coords);
                    triAry.setNormals(0, normals);
                }

            }            
            else { // if shape
                int m, offset=0;
                
                Point3f P2 = new Point3f(), Q2 = new Point3f(), P1 = new Point3f();
                
                Vector3f nn1 = new Vector3f(), nn2= new Vector3f(), nn3 = new Vector3f();
                Vector3f nna = new Vector3f(), nnb= new Vector3f();
                
                Point2f[] extrPnts = shapeExtrusion.getExtrusion();
                int extrPntsLength = extrPnts.length;
                
//                Vector3f n = new Vector3f();  v 1.5

                // fontExtrusion.shape is specified, and is NOT straight line
                for (i=0; i < numIslands; i++) {
                	
                    // For each contour
                    for (j=0, k=0, offset=num=0; j < islandCounts[i].length; j++) {
                    	
                    	// V 1.1
						boolean isFirst = true;
						boolean isLast = false;
					    
					    float cosFirst = 0;
					    float cosa = 0;
					    float cosb = 0;
                    	
                        num += islandCounts[i][j];

                        // Start at last vertex
                        p1.x = outVerts[i][num - 1].x;
                        p1.y = outVerts[i][num - 1].y;
                        p1.z = 0.0f;
                        
                        q1.x = p1.x; q1.y = p1.y; q1.z = p1.z+shapeExtrusion.getDepth();
                        
                        // One before last
                        p3.z = 0.0f;                        
                        for (m=num-2; m >= 0; m--) {
                            p3.x = outVerts[i][m].x;
                            p3.y = outVerts[i][m].y;
                            // nn1 : normal of last segment
                            if (getNormal(p3, q1, p1, nn1)) {
                                if (!flip_side_orient) {
                                    nn1.negate();
                                }
                                goodNormal.set(nn1);
                                break;
                            }
                        }
                        
                        // For each contour's vertex
                        for (; k < num; k++){
                        	
                        	isLast = (k == num-1);
                        	
                            p2.x = outVerts[i][k].x;  p2.y = outVerts[i][k].y; p2.z = 0.0f;
                            q2.x = p2.x; q2.y = p2.y; q2.z = p2.z+shapeExtrusion.getDepth();
                            // nn2 : normal on current segment
                            getNormal(p1, q1, p2, nn2);
                            // v 1.5
                            if (!flip_side_orient) {
                                nn2.negate();
                            }

                            p3.x = outVerts[i][(k+1)==num ? offset : (k+1)].x;
                            p3.y = outVerts[i][(k+1)==num ? offset : (k+1)].y;
                            p3.z = 0.0f;
                            // nn3 : normal on next segment
                            if (!getNormal(p3,p2,q2, nn3)) {
                                nn3.set(goodNormal);
                            } 
                            else {
                                if (!flip_side_orient) {
                                    nn3.negate();
                                }
                                goodNormal.set(nn3);
                            }

                            // Calculate normals at the point by averaging normals
                            // of two faces on each side of the point.
                            nna.x = (nn1.x+nn2.x);
                            nna.y = (nn1.y+nn2.y);
                            nna.z = (nn1.z+nn2.z);
                            normalize(nna);

                            nnb.x = (nn3.x+nn2.x);
                            nnb.y = (nn3.y+nn2.y);
                            nnb.z = (nn3.z+nn2.z);
                            normalize(nnb);

                            P1.x = p1.x; P1.y = p1.y; P1.z = p1.z;
                            P2.x = p2.x; P2.y = p2.y; P2.z = p2.z;
                            Q2.x = q2.x; Q2.y = q2.y; Q2.z = q2.z;
                            
                            // V 1.1
							if (isFirst) {
								cosa = nna.dot(nn1); 																
/*	
System.out.println("Extruder: -----------------------------------------------------------");                              
System.out.println("Extruder: cosa  = " + cosa + " / " +Math.toDegrees(Math.acos(cosa)));	
double angle = nna.angle(nn1);
System.out.println("Extruder: angle = " + angle + " / " + Math.toDegrees(angle));     
*/
							    cosFirst = cosa;
								isFirst = false;
							}
							else {
								cosa = cosb;
							}

							if (isLast) {
								cosb = cosFirst;
							}
							else {
								cosb = nnb.dot(nn3);
							}
/*							
System.out.println("Extruder: cosb  = " + cosb + " / " +Math.toDegrees(Math.acos(cosb)));   
double angle = nnb.angle(nn3);
System.out.println("Extruder: angle = " + angle + " / " + Math.toDegrees(angle));                              
System.out.println("Extruder: -----------------------------------------------------------");                              
*/							
							// Run along the extrusion border from back face to front face
                            for (m=1; m < extrPntsLength; m++){
                                // v 1.5                                
                                if (shapeExtrusion.isT3DStyleExtrusion()) {
                                    q1.x = P1.x + nna.x * extrPnts[m].y; // a la Text3D
                                    q1.y = P1.y + nna.y * extrPnts[m].y;
                                    q2.x = P2.x + nnb.x * extrPnts[m].y;
                                    q2.y = P2.y + nnb.y * extrPnts[m].y;
                                }
                                else {                                    
                                    q1.x = P1.x + nna.x * extrPnts[m].y / cosa; // V 1.1
                                    q1.y = P1.y + nna.y * extrPnts[m].y / cosa;
                                    q2.x = P2.x + nnb.x * extrPnts[m].y / cosb;
                                    q2.y = P2.y + nnb.y * extrPnts[m].y / cosb;
                                }
                                
                                q1.z = q2.z = extrPnts[m].x;

                                /* ?? v 1.5
                                if (!getNormal(p1, q1, p2, n)) {
                                    n.set(goodNormal);
                                } 
                                else {
                                    if (!flip_side_orient) {
                                        n.negate();
                                    }
                                    goodNormal.set(n);
                                }
                                */
                                
                                // V 1.2
                                // Normal calculation will be done in NormalGenerator 
                                // for smooth shading according to crease angle
                                
                                if (flip_side_orient) {
                                    
                                    // (p1, q1, p2)
                                    
                                    coords[index++] = p1.x;    
                                    coords[index++] = p1.y;    
                                    coords[index++] = p1.z;   
                                    
                                    coords[index++] = q1.x;    
                                    coords[index++] = q1.y;    
                                    coords[index++] = q1.z;    
                                } 
                                else  {
                                    
                                    // (q1, p1, p2)
                                    
                                    coords[index++] = q1.x;    
                                    coords[index++] = q1.y;    
                                    coords[index++] = q1.z;   
                                    
                                    coords[index++] = p1.x;    
                                    coords[index++] = p1.y;    
                                    coords[index++] = p1.z;    
                                }
                                
                                coords[index++] = p2.x;    
                                coords[index++] = p2.y;    
                                coords[index++] = p2.z;  
                                
                                /* ?? v 1.5
                                if (!getNormal(p2, q1, q2, n)) {
                                    n.set(goodNormal);
                                } 
                                else {
                                    if (!flip_side_orient) {
                                        n.negate();
                                    }
                                    goodNormal.set(n);
                                }
                                */
                                
                                if (flip_side_orient) {

                                    // (p2, q1, q2)
                                    
                                    coords[index++] = p2.x;    
                                    coords[index++] = p2.y;    
                                    coords[index++] = p2.z;   
                                    
                                    coords[index++] = q1.x;    
                                    coords[index++] = q1.y;    
                                    coords[index++] = q1.z;    
                                } 
                                else {

                                    // (q1, p2, q2)
                                    
                                    coords[index++] = q1.x;    
                                    coords[index++] = q1.y;    
                                    coords[index++] = q1.z;   
                                    
                                    coords[index++] = p2.x;    
                                    coords[index++] = p2.y;    
                                    coords[index++] = p2.z;    
                                }
                                
                                coords[index++] = q2.x;    
                                coords[index++] = q2.y;    
                                coords[index++] = q2.z;   

                                p1.x = q1.x;p1.y = q1.y;p1.z = q1.z;
                                p2.x = q2.x;p2.y = q2.y;p2.z = q2.z;
                            }// for m
                            
                            p1.x = P2.x; p1.y = P2.y; p1.z = P2.z;
                            q1.x = Q2.x; q1.y = Q2.y; q1.z = Q2.z;
                            nn1.x = nn2.x; nn1.y = nn2.y; nn1.z = nn2.z;
                        }// for k
                        
                        offset = num;
                    }// for j
                }//for i
                
                // Geometry transform  V 1.7
                // Transform coords only, normals will be done by NormalGenerator
                if (geomTransform && !isIdentityTransform) {
                    this.transformCoords(coords);
                }
                
                // V 1.1
                GeometryInfo giShape = new GeometryInfo(GeometryInfo.TRIANGLE_ARRAY);
                giShape.setCoordinates(coords);
                // generate normals
                NormalGenerator ngShape = new NormalGenerator(creaseAngle);
                ngShape.generateNormals(giShape);
                
                // V 1.2
                if (byReference)
                    triAry = giShape.getGeometryArray(true, false, false);
                else
                    triAry = giShape.getGeometryArray();
                
            }// if shape
        }// if fontExtrusion
        // No extrusion
        else {
            // Geometry transform  V 1.7
            // Transform coords only, normal are done
            if (geomTransform && !isIdentityTransform) {
                this.transformCoords(coords);
            }
            
            // V 1.2 set arrays
            if (byReference) {
                triAry.setCoordRefFloat(coords);
                triAry.setNormalRefFloat(normals);
            }
            else {
                triAry.setCoordinates(0, coords);
                triAry.setNormals(0, normals);
            }
        }

        return triAry;
    }

    private boolean getNormal(Point3f p1, Point3f p2, Point3f p3, Vector3f normal) {
        Vector3f v1 = new Vector3f();
        Vector3f v2 = new Vector3f();

        /* 
        v1.sub(p2, p1);
        v2.sub(p2, p3);
        normal.cross(v1, v2);
        normal.negate();
        */
        // V 1.7
        v1.sub(p2, p1);
        v2.sub(p3, p1);
        normal.cross(v1, v2);

        float length = normal.length();

        if (length > 0) {
            length = 1.0f/length;
            normal.x *= length;
            normal.y *= length;
            normal.z *= length;
            return true;
        }
//System.out.println("getNormal false length = " +length +" / " + p1 + " / " + p2 + " / " +p3);
        return false;
    }    
    
    private boolean normalize(Vector3f v) {
        float len = v.length();	
        if (len > 0) {
            len = 1.0f/len;
            v.x *= len;
            v.y *= len;
            v.z *= len;
            return true;
        } 
        return false;
    }    

    // V 1.1 / 1.7
	// remove degenerated triangles; workaround
	private Point3f[] removeDegeneratedTrias(GeometryArray ga) {
		
        Vector3f v1 = new Vector3f();
        Vector3f v2 = new Vector3f();
        Vector3f n  = new Vector3f();

		int length = ga.getVertexCount();
		
		Point3f[] points = new Point3f[length];
		
		Point3f p1 = new Point3f();
		Point3f p2 = new Point3f();
		Point3f p3 = new Point3f();
		
		int k = 0;
		
		for (int i=0; i < length; i+=3) {
		    
			ga.getCoordinate(i, p1);
			ga.getCoordinate(i+1, p2);
			ga.getCoordinate(i+2, p3);			
/*			
			if (p1.equals(p2) || p1.equals(p3) || p2.equals(p3)) {
System.out.println("removeDegeneratedTrias = " + p1 + " / " + p2 + " / " +p3);
				continue;
			}
			if ( (p2.y - p1.y)/(p2.x - p1.x) == (p3.y - p2.y)/(p3.x - p2.x)) {
System.out.println("removeDegeneratedTrias slope  = "+n.length()+" / " + p1 + " / " + p2 + " / " +p3);
			    continue;
			}
*/			
			// V 1.7
			v1.sub(p2, p1);
			v2.sub(p3, p1);
			n.cross(v1, v2);

			if (n.length() > 0.0f) {
			    points[k++] = new Point3f(p1);
			    points[k++] = new Point3f(p2);
			    points[k++] = new Point3f(p3);
			}
			else {
//			    System.out.println("removeDegeneratedTrias length = "+n.length()+" / " + p1 + " / " + p2 + " / " +p3);
//			    System.out.println("removeDegeneratedTrias normal = "+n);
			}   
		}
		
		if (k == length) {
		    return points;
		}
		else {
//System.out.println("removeDegeneratedTrias : (k != length) = " + k + " / " + length);
    		Point3f[] pointArray = new Point3f[k];
    		for (int i=0; i < k; i++)
    			pointArray[i] = points[i];
    		
    		return pointArray;
		}
	}

    // A Tree of islands form based on contour, each parent's contour 
    // enclosed all the child. We built this since Triangular fail to
    // handle the case of multiple concentrated contours. i.e. if
    // 4 contours A > B > C > D. Triangular will fail recongized
    // two island, one form by A & B and the other by C & D.
    // Using this tree we can separate out every 2 levels and pass
    // in to triangular to workaround its limitation.
    private final class IslandsNode {

        private ArrayList<IslandsNode> islandsList = null;
        private int startIdx, endIdx;

        IslandsNode(int startIdx, int endIdx) {
            this.startIdx = startIdx;
            this.endIdx = endIdx;
            islandsList = null;
        }

        void addChild(IslandsNode node) {

            if (islandsList == null) {
                islandsList = new ArrayList<IslandsNode>(5);
            }
            islandsList.add(node);
        }

        void removeChild(IslandsNode node) {
            islandsList.remove(islandsList.indexOf(node));
        }

        IslandsNode getChild(int idx) {
            return islandsList.get(idx);
        }

        int numChild() {
            return (islandsList == null ? 0 : islandsList.size());
        }

        int numVertices() {
            return endIdx - startIdx;
        }

        void insert(IslandsNode newNode, Point3f[] vertices) {
            boolean createNewLevel = false;

            if (islandsList != null) {
                IslandsNode childNode;
                int status;

                for (int i=numChild()-1; i>=0; i--) {
                    childNode = getChild(i);
                    status = check2Contours(newNode.startIdx, newNode.endIdx,
                                            childNode.startIdx, childNode.endIdx,
                                            vertices);
                    switch (status) {
                        case 2: // newNode inside childNode, go down recursively
                            childNode.insert(newNode, vertices);
                            return;
                        case 3:// childNode inside newNode, 
                            // continue to search other childNode also
                            // inside this one and group them together.
                            newNode.addChild(childNode);
                            createNewLevel = true;
                            break;
                        default: // intersecting or disjoint						
                    }		
                }
            }

            if (createNewLevel) {
                // Remove child in newNode from this
                for (int i=newNode.numChild()-1; i>=0; i--) {
                    removeChild(newNode.getChild(i));
                }
                // Add the newNode to parent 
            } 
            addChild(newNode);
        }

        // Return a list of node with odd number of level
        private void collectOddLevelNode(ArrayList<IslandsNode> list, int level) {
            if ((level % 2) == 1) {
                list.add(this);
            }
            if (islandsList != null) {
                level++;
                for (int i=numChild()-1; i>=0; i--) {
                        getChild(i).collectOddLevelNode(list, level);
                }
            }
        }
        // check if 2 contours are inside/outside/intersect one another
        // INPUT:
        // vertCnt1, vertCnt2  - number of vertices in 2 contours
        // begin1, begin2      - starting indices into vertices for 2 contours
        // vertices            - actual vertex data
        // OUTPUT:
        // status == 1   - intersecting contours
        //           2   - first contour inside the second 
        //           3   - second contour inside the first
        //           0   - disjoint contours(2 islands) 
        private int check2Contours(int begin1, int end1, int begin2, int end2, Point3f[] vertices) {
            int i;
            boolean inside2, inside1;

            inside2 = pointInPolygon2D(vertices[begin1].x, vertices[begin1].y, 
                                       begin2, end2, vertices);

            for (i=begin1+1; i < end1;i++) {
                if (pointInPolygon2D(vertices[i].x, vertices[i].y, 
                                     begin2, end2, vertices) != inside2) {
                    return 1;	  //intersecting contours
                }
            }

            // Since we are using point in polygon test and not 
            // line in polygon test. There are cases we miss the interesting
            // if we are not checking the reverse for all points. This happen
            // when two points form a line pass through a polygon but the two
            // points are outside of it.

            inside1 = pointInPolygon2D(vertices[begin2].x, vertices[begin2].y, 
                                       begin1, end1, vertices);

            for (i=begin2+1; i < end2;i++) {
                if (pointInPolygon2D(vertices[i].x, vertices[i].y, 
                                     begin1, end1, vertices) != inside1) { 
                    return 1; //intersecting contours
                }
            }

            if (!inside2) {
                if (!inside1) {  	
                    return 0;   // disjoint countours
                } 
                // inside2 = false and inside1 = true
                return 3;  // second contour inside first
            }

            // must be inside2 = true and inside1 = false
            // Note that it is not possible inside2 = inside1 = true
            // unless two contour overlap to each others.
            //
            return 2;  // first contour inside second
        }
	    
        // Test if 2D point (x,y) lies inside polygon represented by verts.
        // z-value of polygon vertices is ignored. Sent only to avoid data-copy.
        // Uses ray-shooting algorithm to compute intersections along +X axis.
        // This algorithm works for all polygons(concave, self-intersecting) and
        // is best solution here due to large number of polygon vertices.
        // Point is INSIDE if number of intersections is odd, OUTSIDE if number
        // of intersections is even.
        private boolean pointInPolygon2D(float x, float y, int begIdx, int endIdx, Point3f[] verts){

            int i, num_intersections = 0;
            float xi;

            for (i=begIdx;i < endIdx-1;i++) {
                if ((verts[i].y >= y && verts[i+1].y >= y) ||
                    (verts[i].y <  y && verts[i+1].y <  y))
                    continue;

                xi = verts[i].x + (verts[i].x - verts[i+1].x)*(y - verts[i].y)/
                    (verts[i].y - verts[i+1].y);

                if (x < xi) num_intersections++;
            }

            // Check for segment from last vertex to first vertex.

            if (!((verts[i].y >= y && verts[begIdx].y >= y) ||
                  (verts[i].y <  y && verts[begIdx].y <  y))) {
                    xi = verts[i].x + (verts[i].x - verts[begIdx].x)*(y - verts[i].y)/
                        (verts[i].y - verts[begIdx].y);

                    if (x < xi) num_intersections++;
                }

            return ((num_intersections % 2) != 0);
        }
    }
}
