/*
 * $RCSfile: Text3D.java,v $
 *
 * Copyright 1996-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
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
 * $Date: 2008/02/28 20:17:31 $
 * $State: Exp $
 * 
 * 
 * String3D is derived from "javax.media.j3d.Text3D".
 * 
 * Redistribution and use are permitted according to the license notice 
 * above mentioned. A copy is attached as LICENSE_GPL_CLASSPATH.txt.
 *
 * Author: August Lammersdorf, www.InteractiveMesh.com/org
 * Version: 1.2
 * Date: 2008/12/19 
 *
 */

package com.interactivemesh.j3d.community.utils.geometry;

import eu.mihosoft.ext.j3d.javax.media.j3d.BoundingBox;
import eu.mihosoft.ext.j3d.javax.media.j3d.GeometryArray;
import eu.mihosoft.ext.j3d.javax.media.j3d.LineStripArray;
import eu.mihosoft.ext.j3d.javax.media.j3d.TriangleArray;
import eu.mihosoft.ext.j3d.javax.vecmath.Point3d;
import eu.mihosoft.ext.j3d.javax.vecmath.Point3f;
import eu.mihosoft.ext.j3d.javax.vecmath.Vector3f;
import java.awt.Font;
import java.awt.Shape;

import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;

import java.util.ArrayList;


/**
 * String3D converts a text string to a 3D geometry in a factory manner.  <P>
 * Each String3D object has the following parameters:<P>
 * <UL>
 * <LI>Font object - describes the font of the text string,
 * such as the font family (Helvetica, Courier, etc.), style (Italic,
 * bold, etc.), and point size. The size of the resulting characters will 
 * be equal to the point size. For example, a 12 point font will result in 
 * a geometry with characters 12 meters tall.  </LI><P>
 * 
 * <LI>Position - determines the initial placement of the text string 
 * in three-space.</LI><P>
 * 
 * <LI>Alignment - specifies how the string is placed in relation to 
 * the position parameter.</LI><P>
 * 
 * <LI>Path - specifies how succeeding characters in the string are placed 
 * in relation to the previous character and are aligned in relation 
 * to the position parameter.</LI><P>
 * 
 * <LI>Character spacing - the space between characters. This spacing is
 * in addition to the regular spacing between characters as defined in the
 * Font object.</LI><P>
 *
 * <LI>AWTShapeExtruder object - extrudes the shape of the characters 
 * to a 3D geometry. The specified geometry transformation will be 
 * applied to the generated coordinates and normals.</LI></UL><P>
 * 
 * The size and original position of a text string can be ascertained without 
 * generating geometry data by
 * <UL>
 * <LI>{@link #getGeometryBounds(String, BoundingBox)}</LI>
 * </UL><br>
 * According to the current state of these parameters a String3D object 
 * creates for a given string a single <code>javax.media.j3d.GeometryArray</code> 
 * for the entire text 
 * <UL>
 * <LI>{@link #getStringGeometry(String)}</LI>
 * <LI>{@link #getStringGeometry(String, BoundingBox)}</LI><br/>
 * <LI>{@link #getStringOutlineGeometry(String)}</LI>
 * <LI>{@link #getStringOutlineGeometry(String, BoundingBox)}</LI>
 * </UL>
 * or a single <code>javax.media.j3d.GeometryArray</code> 
 * for each character:<P>
 * <UL>
 * <LI>{@link #getCharacterGeometry(String)}</LI>
 * <LI>{@link #getCharacterGeometry(String, BoundingBox)}</LI><br/>
 * <LI>{@link #getCharacterOutlineGeometry(String)}</LI>
 * <LI>{@link #getCharacterOutlineGeometry(String, BoundingBox)}</LI>
 * </UL>
 * <P>
 * A returned GeometryArray includes triangle resp. line primitives and normals.
 * </P>
 * 
 * <P>Sample strings:
 * <pre>
 * String string0 = new String("String3D");
 * String string1 = new String("Two Words !");
 * String string2 = new String("  9        8       7      6     5    4   3  2 1  ");
 * 
 * int[] codePoints = new int[] {9992, 9786}; // Airplane, Smiley (e.g. Lucida Sans)
 * String string3 = new String(codePoints, 0, 2);        
 * </pre>
 * </P>
 * String3D is a generalized version of <code>javax.media.j3d.Text3D</code>.
 * 
 * @version 1.2
 * 
 */
public class String3D {   
      
    /**
     * Specifies how the string is placed in relation to the position parameter.
     */
    public enum Alignment {
        /**
         * The center of the string is placed on the <code>position</code> point.
         */
        CENTER,
        /**
         * The first character of the string is placed on the <code>position</code> point.
         */
        FIRST,
        /**
         * The last character of the string is placed on the <code>position</code> point.
         */
        LAST;
    }
    
    /**
     * Specifies how succeeding characters in the string are placed in relation 
     * to the previous characters and are aligned in relation to the position parameter.
     */
    public enum Path {
        /**
         * Succeeding characters are placed to the left of the current character.
         */
        LEFT,
        /**
         * Succeeding characters are placed to the right of the current character.
         */
        RIGHT,
        /**
         * Succeeding characters are placed below the current character
         * and are centered on the <code>position</code> point.
         */
        DOWN_CENTER,
        /**
         * Succeeding characters are placed below the current character
         * and are aligned left in relation to the <code>position</code> point.
         */
        DOWN_LEFT,
        /**
         * Succeeding characters are placed below the current character
         * and are aligned right in relation to the <code>position</code> point.
         */
        DOWN_RIGHT,
        /**
         * Succeeding characters are placed above the current character
         * and are centered on the <code>position</code> point.
         */
        UP_CENTER,
        /**
         * Succeeding characters are placed above the current character
         * and are aligned left in relation to the <code>position</code> point.
         */
        UP_LEFT,
        /**
         * Succeeding characters are placed above the current character
         * and are aligned right in relation to the <code>position</code> point.
         */
        UP_RIGHT;       
    }
    
    
    private AWTShapeExtruder    stringExtruder  =   null;
    private Font                stringFont      =   null;
    private Point3f             stringPosition  =   new Point3f();
    private float               stringSpacing   =   0.0f;
    
    private Alignment           stringAlignment =   Alignment.FIRST;
    private Path                stringPath      =   Path.RIGHT;
    
    /**
     * Constructs a String3D object with default parameters.
     * The default values are as follows:
     * <ul>
     * font : null<br>
     * extruder : null<br>
     * position : (0, 0, 0)<br>
     * alignment : Alignment.FIRST<br>
     * path : Path.RIGHT<br>
     * character spacing : 0.0<br>
     * </ul>
     */
    public String3D() {
    }

    /**
     * Constructs a String3D object given a font and an extruder.
     *
     * @see AWTShapeExtruder
     */
    public String3D(Font font, AWTShapeExtruder extruder) {
        stringFont = font;
        stringExtruder = extruder;
    }

    /**
     * Constructs a String3D object given a font, an extruder, and a position. 
     *
     * @see AWTShapeExtruder
     */
    public String3D(Font font, AWTShapeExtruder extruder, Point3f position) {
        stringFont = font;
        stringExtruder = extruder;
        if (position != null)
            stringPosition.set(position);
    }

    /**
     * Constructs a String3D object given a font, an extruder, a position, 
     * an alignment, and a path. 
     *
     * @see AWTShapeExtruder
     */
    public String3D(Font font, AWTShapeExtruder extruder,  
                    Point3f position, Alignment alignment, Path path) {
        stringFont = font;
        stringExtruder = extruder;
        if (position != null)
            stringPosition.set(position);        
        this.setAlignment(alignment);
        this.setPath(path);
    }
    
    /**
     * Returns the Font object used by this String3D object.<P>
     * 
     * The default font is <code>null</code>.
     *
     * @return the Font object of this String3D object - 
     * null if no Font has been associated with this object.
     *
     */
    public Font getFont() {
        return stringFont;
    }
    
    /**
     * Sets the Font object used by this String3D object.<P>
     *     
     * The default font is <code>null</code>.
     *
     * @param font the Font object to associate with this String3D.
     *
     */
    public void setFont(Font font) {
        stringFont = font;
    }
    
    /**
     * Returns the AWTShapeExtruder object used by this String3D object.<P>
     * 
     * The default extruder is <code>null</code>.
     *
     * @return the AWTShapeExtruder object of this String3D object - 
     * null if no AWTShapeExtruder has been associated with this object.
     *
     */
    public AWTShapeExtruder getExtruder() {
        return stringExtruder;
    }

    /**
     * Sets the AWTShapeExtruder object used by this String3D object.<P>
     * 
     * The default extruder is <code>null</code>.
     *
     * @param extruder the AWTShapeExtruder object to associate with this String3D.
     *
     */
    public void setExtruder(AWTShapeExtruder extruder) {
        stringExtruder = extruder;
    }

    /**
     * Copies the <code>position</code> field into the supplied
     * parameter.  The <code>position</code> is used to determine the
     * initial placement of the String3D string.  The position, combined with
     * the path and alignment control how the string is displayed.<P>
     *
     * The default position is (0, 0, 0).

     * @param position the point to position the string.
     * 
     * @exception NullPointerException if position is <code>null</code>.
     *
     * @see #getAlignment
     * @see #getPath
     */
    public void getPosition(Point3f position) {
        if (position == null)
            throw new NullPointerException("position is null");
        position.set(stringPosition);
    }

    /**
     * Sets the <code>position</code> field to the supplied
     * parameter.  The <code>position</code> is used to determine the
     * initial placement of the String3D string.  The position, combined with
     * the path and alignment control how the string is displayed.<P>
     * 
     * The default position is (0, 0, 0).
     *
     * @param position the point to position the string.
     * 
     * @exception NullPointerException if position is <code>null</code>.
     * 
     * @see #getAlignment
     * @see #getPath
     */
    public void setPosition(Point3f position) {
        if (position == null)
            throw new NullPointerException("position is null");
        stringPosition.set(position);
    }
    
    /**
     * Returns the string alignment policy for this String3D 
     * object. The <code>alignment</code> is used to specify how
     * characters in the string are placed in relation to the
     * <code>position</code> field. <P>
     * 
     * The default value is <code>String3D.Alignment.FIRST</code>.
     *
     * @return the current alignment policy for this object.
     *
     * @see #getPosition
     */
    public Alignment getAlignment() {
        return stringAlignment;
    }

    /**
     * Sets the string alignment policy for this String3D 
     * object. The <code>alignment</code> is used to specify how
     * characters in the string are placed in relation to the
     * <code>position</code> field.  <P>
     * 
     * The default value is <code>String3D.Alignment.FIRST</code>.
     *
     * @param alignment the new alignment policy for this object
     *
     * @see #getPosition
     */
    public void setAlignment(Alignment alignment) {
        stringAlignment = alignment;
    }
    
    /**
     * Returns the characters path policy.  This field is used to specify how 
     * succeeding characters in the string are placed in relation to the 
     * previous characters and are aligned in relation to the position parameter.<P>
     * 
     * The default value is <code>String3D.Path.RIGHT</code>.
     *
     * @return the current characters path policy for this object.
     */
    public Path getPath() {
        return stringPath;
    }

    /**
     * Sets the characters path policy.  This field is used to specify how 
     * succeeding characters in the string are placed in relation to the 
     * previous characters and are aligned in relation to the position parameter.<P>
     * 
     * The default value is <code>String3D.Path.RIGHT</code>.
     *
     * @param path the new characters path policy for this object.
     */
    public void setPath(Path path) {
        stringPath = path;
    }
    
    /**
     * Returns the character spacing used when creating the string's geometry.
     * This spacing is in addition to the regular spacing between characters as
     * defined in the Font object. The value is measured in meters.<P>
     * 
     * The default value is 0.0.
     *
     * @return the current character spacing value
     */
    public float getCharacterSpacing() {
        return stringSpacing;
    }

    /**
     * Sets the character spacing used when creating the string's geometry.
     * This spacing is in addition to the regular spacing between characters as
     * defined in the Font object. The value is measured in meters.<P>
     * 
     * The default value is 0.0.
     *
     * @param characterSpacing the new character spacing value
     */
    public void setCharacterSpacing(float characterSpacing) {
        stringSpacing = characterSpacing;
    }
    
    //
    // Factory
    //
    
    // V 1.2
    /**
     * Returns the bounding box for the given string
     * according to the specified font, extrusion depth, spacing, alignment, 
     * and path of this String3D object without generating geometry data.<P>
     * 
     * Neither the position nor the geometry transform attributes are applied.
     * The edge contour of a specified extrusion shape is ignored.<P>
     * 
     * The returned lower and upper corners of the BoundingBox object allow 
     * to ascertain the width, height, and depth as well as the center of the
     * tessellated and extruded geometry.
     * 
     * @param string a String from which to generate the geometry's bounding box
     * @param bounds a BoundingBox to receive the geometry's lower and upper corners
     * 
     * @throws NullPointerException if string is <code>null</code> or of length < 1
     * @throws NullPointerException if bounds is <code>null</code>
     * @throws IllegalStateException if no extruder or font object is set
     * @throws IllegalArgumentException if string consists of whitespaces only
     * 
     */
    public void getGeometryBounds(String string, BoundingBox bounds) {
          
        checkStringAndState(string);
        
        if (bounds == null)
            throw new NullPointerException("String3D : bounds is null !");

        boolean isReverse = (stringPath == Path.LEFT || 
                             stringPath == Path.DOWN_CENTER || 
                             stringPath == Path.DOWN_LEFT || 
                             stringPath == Path.DOWN_RIGHT);
            
        // GlyphVector -> CharTranslation
        
        GlyphVector[] gvs = createGlyphVectors(string, isReverse);        
        
        if (gvs == null)
            throw new IllegalArgumentException("String3D : string is whitespace !");
        
        createCharTranslations(gvs, bounds, new Point3f());     

        // upper.z; lower.z is done    
        if (stringExtruder.getShapeExtrusion() != null) {
            Point3d p3d = new Point3d();
            bounds.getUpper(p3d);
            p3d.z += stringExtruder.getShapeExtrusion().getDepth();
            bounds.setUpper(p3d);
        } 
    }
        
    /**
     * Generates a GeometryArray object for the given string 
     * according to the specified font, extruder, spacing, position, alignment, 
     * and path of this String3D object.<P>
     * 
     * All whitespaces within the string influence the characters' position, 
     * alignment, and path. But a non-straight extrusion shape remains unconsidered. <P>
     * 
     * The returned GeometryArray object comprises the geometry of 
     * all non-whitespace characters and includes triangle primitives as well as normals.
     * 
     * @param string a String from which to generate a tessellated extruded geometry
     * 
     * @return a GeometryArray or <code>null</code> if no GeometryArray could be created
     * 
     * @throws NullPointerException if string is <code>null</code> or of length < 1
     * @throws IllegalStateException if no extruder or font object is set
     * @throws IllegalArgumentException if string consists of whitespaces only
     * 
     */
    public GeometryArray getStringGeometry(String string) {       
        return createStringGeometry(string, null);
    }
    
    /**
     * Generates a GeometryArray object for the given string 
     * according to the specified font, extruder, spacing, position, alignment, 
     * and path of this String3D object 
     * and returns the geometry's bounding box to the given BoundingBox object.<P>
     * 
     * All whitespaces within the string influence the characters' position, 
     * alignment and path. But a non-straight extrusion shape remains unconsidered.<P>
     * 
     * The returned GeometryArray object comprises the geometry of 
     * all non-whitespace characters and includes triangle primitives as well as normals.<P>
     * 
     * The specified BoundingBox object, if not <code>null</code>, receives
     * the bounding box of the returned GeometryArray object.   
     * 
     * @param string a String from which to generate a tessellated extruded geometry
     * @param bounds a BoundingBox to receive the geometry's bounding box
     * 
     * @return a GeometryArray or <code>null</code> if no GeometryArray could be created
     * 
     * @throws NullPointerException if string is <code>null</code> or of length < 1
     * @throws IllegalStateException if no extruder or font object is set
     * @throws IllegalArgumentException if string consists of whitespaces only
     * 
     */
    public GeometryArray getStringGeometry(String string, BoundingBox bounds) {        
        return createStringGeometry(string, bounds);
    }
        
    private GeometryArray createStringGeometry(String string, BoundingBox bounds) {
        
        checkStringAndState(string);

        boolean isReverse = (stringPath == Path.LEFT || 
                             stringPath == Path.DOWN_CENTER || 
                             stringPath == Path.DOWN_LEFT || 
                             stringPath == Path.DOWN_RIGHT);
        
        boolean isExtrusionShape = ( (stringExtruder.getShapeExtrusion() != null) &&
                                     (stringExtruder.getShapeExtrusion().getExtrusionShape() != null));
        
        // GlyphVector -> CharTranslation -> PathIterator -> Geometry -> Geometry transform
        
        GlyphVector[] gvs = createGlyphVectors(string, isReverse);        
        
        if (gvs == null)
            throw new IllegalArgumentException("String3D : string is whitespace !");
        
        Vector3f[] charTranss = createCharTranslations(gvs, bounds, stringPosition);     
        
        int numChars = gvs.length;      
        int totalVertexCount = 0;
        
        GeometryArray[] geomList = new GeometryArray[numChars];
        
        for (int i=0; i < numChars; i++) {
        
            PathIterator pathIt = createPathIterator(gvs[i]);
    
            // byReference, no Geometry transform !
            GeometryArray geomInterim = stringExtruder.createGeometry(pathIt, true, false);
        
            if (geomInterim == null)
                continue;
            
            totalVertexCount += geomInterim.getVertexCount();
            
            geomList[i] = geomInterim;
        }
        
        if (totalVertexCount < 1)
            return null;
        
        // Bounds
        
        // isExtrusionShape
        Point3d boundsLower = new Point3d();
        Point3d boundsUpper = new Point3d();
        // upper.z; lower.z is done 
        if (bounds != null) {
            if (stringExtruder.getShapeExtrusion() != null) {
                Point3d p3d = new Point3d();
                bounds.getUpper(p3d);
                p3d.z += stringExtruder.getShapeExtrusion().getDepth();
                bounds.setUpper(p3d);
            }
            
            if (isExtrusionShape) {
                bounds.getLower(boundsLower);
                bounds.getUpper(boundsUpper);
            }
        }
        
        
        float[] coordsAll = new float[totalVertexCount * 3];
        float[] normalsAll = new float[totalVertexCount * 3];
        
        int k = 0; 
        
        for (int i=0; i < numChars; i++) {
            
            GeometryArray geomInterim = geomList[i];
            
            if (geomInterim == null)
                continue;
            
            float[] coords = geomInterim.getCoordRefFloat();
            float[] normals = geomInterim.getNormalRefFloat();
            
            Vector3f charTrans = charTranss[i];
            
            if (bounds != null && isExtrusionShape) {
                for (int j=0,z=coords.length; j < z; j+=3) {               
                    coordsAll[k] = coords[j] + charTrans.x;
                    if (boundsLower.x > coordsAll[k])
                        boundsLower.x = coordsAll[k];
                    else if (boundsUpper.x < coordsAll[k])
                        boundsUpper.x = coordsAll[k];
                    
                    normalsAll[k++] = normals[j];
                    
                    coordsAll[k] = coords[j+1] + charTrans.y;
                    if (boundsLower.y > coordsAll[k])
                        boundsLower.y = coordsAll[k];
                    else if (boundsUpper.y < coordsAll[k])
                        boundsUpper.y = coordsAll[k];
                    
                    normalsAll[k++] = normals[j+1];
                    
                    coordsAll[k] = coords[j+2] + charTrans.z;
                    normalsAll[k++] = normals[j+2];
                }                                         
            }
            else {                
                for (int j=0,z=coords.length; j < z; j+=3) {               
                    coordsAll[k] = coords[j] + charTrans.x;
                    normalsAll[k++] = normals[j];
                    
                    coordsAll[k] = coords[j+1] + charTrans.y;
                    normalsAll[k++] = normals[j+1];
                    
                    coordsAll[k] = coords[j+2] + charTrans.z;
                    normalsAll[k++] = normals[j+2];
                }         
            }
            
            geomInterim.setCoordRefFloat(null);
            geomInterim.setNormalRefFloat(null);
        }
        
        // isExtrusionShape
        if (bounds != null && isExtrusionShape) {
            bounds.setLower(boundsLower);
            bounds.setUpper(boundsUpper);
        }
        
        TriangleArray triaArray = new TriangleArray(totalVertexCount, 
                                                    GeometryArray.COORDINATES | 
                                                    GeometryArray.NORMALS);
        // Geometry transform  V 1.2
        stringExtruder.transformCoords(coordsAll);
        stringExtruder.transformNormals(normalsAll);
        if (bounds != null)
            stringExtruder.transformBounds(bounds, coordsAll);

        triaArray.setCoordinates(0, coordsAll);
        triaArray.setNormals(0, normalsAll);
              
        return triaArray;
    }

    /**
     * Generates a GeometryArray object representing the outline for the given string 
     * according to the specified font, spacing, position, alignment, 
     * and path of this String3D object. 
     * Neither the extrusion path nor the crease angle are applied.<P>
     * 
     * All whitespaces within the string influence the characters' position, 
     * alignment, and path.<P>
     * 
     * The returned GeometryArray object comprises the geometry of 
     * all non-whitespace characters and includes line primitives as well as normals
     * which are directed along the positive Z axis (0, 0, 1).
     * 
     * @param string a String from which to generate an outline geometry
     * 
     * @return a GeometryArray or <code>null</code> if no GeometryArray could be created
     * 
     * @throws NullPointerException if string is <code>null</code> or of length < 1
     * @throws IllegalStateException if no extruder or font object is set
     * @throws IllegalArgumentException if string consists of whitespaces only
     * 
     */
    public GeometryArray getStringOutlineGeometry(String string) {
        return createStringOutlineGeometry(string, null);
    }
    /**
     * Generates a GeometryArray object representing the outline for the given string 
     * according to the specified font, spacing, position, alignment, 
     * and path of this String3D object 
     * and returns the geometry's bounding box to the given BoundingBox object. 
     * Neither the extrusion path nor the crease angle are applied.<P>
     * 
     * All whitespaces within the string influence the characters' position, 
     * alignment, and path.<P>
     * 
     * The returned GeometryArray object comprises the geometry of 
     * all non-whitespace characters and includes line primitives as well as normals
     * which are directed along the positive Z axis (0, 0, 1).<P>
     * 
     * The specified BoundingBox object, if not <code>null</code>, receives
     * the bounding box of the returned GeometryArray object.   
     * 
     * @param string a String from which to generate an outline geometry
     * @param bounds a BoundingBox to receive the geometry's bounding box
     * 
     * @return a GeometryArray or <code>null</code> if no GeometryArray could be created
     * 
     * @throws NullPointerException if string is <code>null</code> or of length < 1
     * @throws IllegalStateException if no extruder or font object is set
     * @throws IllegalArgumentException if string consists of whitespaces only
     *
     */
    public GeometryArray getStringOutlineGeometry(String string, BoundingBox bounds) {
        return createStringOutlineGeometry(string, bounds);
    }
    
    // V 1.1
    private GeometryArray createStringOutlineGeometry(String string, BoundingBox bounds) {
        
        checkStringAndState(string);

        boolean isReverse = (stringPath == Path.LEFT || 
                             stringPath == Path.DOWN_CENTER || 
                             stringPath == Path.DOWN_LEFT || 
                             stringPath == Path.DOWN_RIGHT);
        
        // isExtrusionShape ? -> only flat line geometry yet 
        
        // GlyphVector -> CharTranslation -> PathIterator -> Geometry -> Geometry transform
        
        GlyphVector[] gvs = createGlyphVectors(string, isReverse);        
        
        if (gvs == null)
            throw new IllegalArgumentException("String3D : string is whitespace !");
        
        Vector3f[] charTranss = createCharTranslations(gvs, bounds, stringPosition); 
        
        int numChars = gvs.length;      
        int totalVertexCount = 0;
        int totalStripCount = 0;
        int numStrips = 0;
        int maxNumStrips = 0;
                
        LineStripArray[] geomList = new LineStripArray[numChars];
        
        for (int i=0; i < numChars; i++) {
        
            PathIterator pathIt = createPathIterator(gvs[i]);
    
            // Container for coordinates and strips; no normals, byReference, no geom transform !
            LineStripArray geomInterim = stringExtruder.createLineStripGeometry(pathIt, false, true, false);
        
            if (geomInterim == null)
                continue;
            
            totalVertexCount += geomInterim.getVertexCount();
            
            numStrips = geomInterim.getNumStrips();
            totalStripCount += numStrips;
            if (maxNumStrips < numStrips)
                maxNumStrips = numStrips;
            
            geomList[i] = geomInterim;
        }
        
        if (totalVertexCount < 1)
            return null;

        // Bounds       
        // no extrusion shape: upper.z = lower.z ( = stringPosition.z) done

        float[] coordsAll         = new float[totalVertexCount * 3];
        int[]   stripVertexCtAll  = new int[totalStripCount];
        int[]   stripVertCtBuffer = new int[maxNumStrips];
        
        int k = 0; 
        int l = 0;
        
        for (int i=0; i < numChars; i++) {
            
            LineStripArray geomInterim = geomList[i];
            
            if (geomInterim == null)
                continue;
            
            float[] coords = geomInterim.getCoordRefFloat();
            geomInterim.getStripVertexCounts(stripVertCtBuffer);
            
            for (int j=0,z=coords.length; j < z; j+=3) {               
                coordsAll[k++] = coords[j]   + charTranss[i].x;                    
                coordsAll[k++] = coords[j+1] + charTranss[i].y;                   
                coordsAll[k++] = coords[j+2] + charTranss[i].z;
            }         
            
            for (int j=0,num=geomInterim.getNumStrips(); j < num; j++)
                stripVertexCtAll[l++] = stripVertCtBuffer[j];
            
            geomInterim.setCoordRefFloat(null);
        }
        
        
        LineStripArray lineStripArray = new LineStripArray(
                            totalVertexCount,
                            LineStripArray.COORDINATES | LineStripArray.NORMALS, 
                            stripVertexCtAll);

        // Geometry transform  V 1.2
        stringExtruder.transformCoords(coordsAll);
        float[] normalArr = {0, 0, 1};
        stringExtruder.transformNormals(normalArr);
        if (bounds != null)
            stringExtruder.transformBounds(bounds, coordsAll);
               
        lineStripArray.setCoordinates(0, coordsAll);
        
        for (int i=0; i < totalVertexCount; i++)
            lineStripArray.setNormal(i, normalArr);

        
        return lineStripArray;
    }
        
    /**
     * Generates GeometryArray objects for the given string's characters  
     * according to the specified font, extruder, spacing, position, alignment, 
     * and path of this String3D object.<P>
     * 
     * All whitespaces within the string influence the characters' position, 
     * alignment, and path. But a non-straight extrusion shape remains unconsidered. <P>
     * 
     * The returned GeometryArray objects comprise the geometry of 
     * all non-whitespace characters and include triangle primitives as well as normals.
     * 
     * @param string a String from which to generate for each character a tessellated extruded geometry
     * 
     * @return GeometryArray objects or <code>null</code> if no GeometryArray could be created
     * 
     * @throws NullPointerException if string is <code>null</code> or of length < 1
     * @throws IllegalStateException if no extruder or font object is set
     * @throws IllegalArgumentException if string consists of whitespaces only
     * 
     */
    public GeometryArray[] getCharacterGeometry(String string) {
        
        return createCharacterGeometry(string, null);
    }
    /**
     * Generates GeometryArray objects for the given string's characters  
     * according to the specified font, extruder, spacing, position, alignment, 
     * and path of this String3D object
     * and returns the geometries' bounding box to the given BoundingBox object.<P>
     * 
     * All whitespaces within the string influence the characters' position, 
     * alignment, and path. But a non-straight extrusion shape remains unconsidered. <P>
     * 
     * The returned GeometryArray objects comprise the geometry of 
     * all non-whitespace characters and include triangle primitives as well as normals.<P>
     * 
     * The specified BoundingBox object, if not <code>null</code>, receives
     * the combined bounding box of all returned GeometryArray objects.   
     * 
     * @param string a String from which to generate for each character a tessellated extruded geometry
     * @param bounds a BoundingBox to receive the geometries' bounding box
     * 
     * @return GeometryArray objects or <code>null</code> if no GeometryArray could be created
     * 
     * @throws NullPointerException if string is <code>null</code> or of length < 1
     * @throws IllegalStateException if no extruder or font object is set
     * @throws IllegalArgumentException if string consists of whitespaces only
     * 
     */
    public GeometryArray[] getCharacterGeometry(String string, BoundingBox bounds) {
        return createCharacterGeometry(string, bounds);
    }
    
    private GeometryArray[] createCharacterGeometry(String string, BoundingBox bounds) {

        checkStringAndState(string);
        
        boolean isReverse = (stringPath == Path.LEFT || 
                             stringPath == Path.DOWN_CENTER || 
                             stringPath == Path.DOWN_LEFT || 
                             stringPath == Path.DOWN_RIGHT);

        boolean isExtrusionShape = ( (stringExtruder.getShapeExtrusion() != null) &&
                                     (stringExtruder.getShapeExtrusion().getExtrusionShape() != null));

        // GlyphVector -> CharTranslation -> PathIterator -> Geometry

        GlyphVector[] gvs = createGlyphVectors(string, isReverse);      
        
        if (gvs == null)
            throw new IllegalArgumentException("String3D : string is whitespace !");

        Vector3f[] charTranss = createCharTranslations(gvs, bounds, stringPosition);

        // Bounds 
        
        // isExtrusionShape
        Point3d boundsLower = new Point3d();
        Point3d boundsUpper = new Point3d();
        // upper.z; lower.z is done 
        if (bounds != null) {
            if (stringExtruder.getShapeExtrusion() != null) {
                Point3d p3d = new Point3d();
                bounds.getUpper(p3d);
                p3d.z += stringExtruder.getShapeExtrusion().getDepth();
                bounds.setUpper(p3d);
            }
            
            if (isExtrusionShape) {
                bounds.getLower(boundsLower);
                bounds.getUpper(boundsUpper);
            }
        }
        

        int numChars = gvs.length;
        
        ArrayList<GeometryArray> geomList = new ArrayList<GeometryArray>(numChars);
        
        ArrayList<float[]> coordsList = null;
        if (bounds != null)
            coordsList = new ArrayList<float[]>(numChars);
        
        for (int i=0; i < numChars; i++) {
        
            PathIterator pathIt = createPathIterator(gvs[i]);
           
            // byReference, no Geometry transform !
            GeometryArray geomInterim = stringExtruder.createGeometry(pathIt, true, false);
        
            if (geomInterim == null)
                continue;
            
            float[] coords = geomInterim.getCoordRefFloat();
            float[] normals = geomInterim.getNormalRefFloat();
            
            Vector3f charTrans = charTranss[i];
            
            if (bounds != null && isExtrusionShape) {
                for (int j=0,z=coords.length; j < z; j+=3) {               
                    coords[j]   += charTrans.x;
                    if (boundsLower.x > coords[j])
                        boundsLower.x = coords[j];
                    else if (boundsUpper.x < coords[j])
                        boundsUpper.x = coords[j];
                                    
                    coords[j+1] += charTrans.y;
                    if (boundsLower.y > coords[j+1])
                        boundsLower.y = coords[j+1];
                    else if (boundsUpper.y < coords[j+1])
                        boundsUpper.y = coords[j+1];
                    
                    coords[j+2] += charTrans.z;
                }                  
            }
            else {
                for (int j=0,z=coords.length; j < z; j+=3) {               
                    coords[j]   += charTrans.x;
                    coords[j+1] += charTrans.y;
                    coords[j+2] += charTrans.z;
                }  
            }
            
            TriangleArray triaArray = new TriangleArray(geomInterim.getVertexCount(), 
                                                        GeometryArray.COORDINATES | 
                                                        GeometryArray.NORMALS);
            // Geometry transform  V 1.2
            stringExtruder.transformCoords(coords);
            stringExtruder.transformNormals(normals);
            if (bounds != null)
                coordsList.add(coords);

            triaArray.setCoordinates(0, coords);
            triaArray.setNormals(0, normals);
            
            geomList.add(triaArray);
            
            geomInterim.setCoordRefFloat(null);
            geomInterim.setNormalRefFloat(null);
        }
              
        if (geomList.isEmpty())
            return null;
        
        if (bounds != null) {
            // isExtrusionShape
            if (isExtrusionShape) {
                bounds.setLower(boundsLower);
                bounds.setUpper(boundsUpper);
            }
            // Transform bounds
            stringExtruder.transformBounds(bounds, coordsList);       
            coordsList.clear();
        }

        GeometryArray[] geomArrays = new GeometryArray[geomList.size()];
        geomList.toArray(geomArrays);

        return geomArrays;
    }
    
    /**
     * Generates GeometryArray objects representing the outline for the given string's characters  
     * according to the specified font, spacing, position, alignment, 
     * and path of this String3D object.
     * Neither the extrusion path nor the crease angle are applied<P>
     * 
     * All whitespaces within the string influence the characters' position, 
     * alignment, and path.<P>
     * 
     * The returned GeometryArray objects comprise the geometry of 
     * all non-whitespace characters and include line primitives as well as normals
     * which are directed along the positive Z axis (0, 0, 1).
     * 
     * @param string a String from which to generate for each character an outline geometry
     * 
     * @return GeometryArray objects or <code>null</code> if no GeometryArray could be created
     * 
     * @throws NullPointerException if string is <code>null</code> or of length < 1
     * @throws IllegalStateException if no extruder or font object is set
     * @throws IllegalArgumentException if string consists of whitespaces only
     * 
     */
    public GeometryArray[] getCharacterOutlineGeometry(String string) {
        return createCharacterOutlineGeometry(string, null);
    }
    /**
     * Generates GeometryArray objects representing the outline for the given string's characters  
     * according to the specified font, spacing, position, alignment, 
     * and path of this String3D object
     * and returns the geometries' bounding box to the given BoundingBox object.
     * Neither the extrusion path nor the crease angle are applied<P>
     * 
     * All whitespaces within the string influence the characters' position, 
     * alignment, and path.<P>
     * 
     * The returned GeometryArray objects comprise the geometry of 
     * all non-whitespace characters and include line primitives as well as normals
     * which are directed along the positive Z axis (0, 0, 1).<P>
     * 
     * The specified BoundingBox object, if not <code>null</code>, receives
     * the combined bounding box of all returned GeometryArray objects.   
     *  
     * @param string a String from which to generate for each character an outline geometry
     * @param bounds a BoundingBox to receive the geometries' bounding box
     * 
     * @return GeometryArray objects or <code>null</code> if no GeometryArray could be created
     * 
     * @throws NullPointerException if string is <code>null</code> or of length < 1
     * @throws IllegalStateException if no extruder or font object is set
     * @throws IllegalArgumentException if string consists of whitespaces only
     * 
     */
    public GeometryArray[] getCharacterOutlineGeometry(String string, BoundingBox bounds) {
        return createCharacterOutlineGeometry(string, bounds);
    }

    // V 1.1
    private GeometryArray[] createCharacterOutlineGeometry(String string, BoundingBox bounds) {

        checkStringAndState(string);

        boolean isReverse = (stringPath == Path.LEFT || 
                             stringPath == Path.DOWN_CENTER || 
                             stringPath == Path.DOWN_LEFT || 
                             stringPath == Path.DOWN_RIGHT);
        
        // isExtrusionShape ? -> only flat line geometry yet 
        
        // GlyphVector -> CharTranslation -> PathIterator -> Geometry -> Geometry transform
        
        GlyphVector[] gvs = createGlyphVectors(string, isReverse);        
        
        if (gvs == null)
            throw new IllegalArgumentException("String3D : string is whitespace !");
        
        Vector3f[] charTranss = createCharTranslations(gvs, bounds, stringPosition); 

        // Bounds       
        // no extrusion shape: upper.z = lower.z ( = stringPosition.z) done

        // Transform normal  V 1.2
        float[] normalArr = {0, 0, 1};
        stringExtruder.transformNormals(normalArr);
        
        int numChars = gvs.length;      
                
        ArrayList<LineStripArray> geomList = new ArrayList<LineStripArray>(numChars);
        
        ArrayList<float[]> coordsList = null;
        if (bounds != null)
            coordsList = new ArrayList<float[]>(numChars);
                       
        for (int i=0; i < numChars; i++) {
        
            PathIterator pathIt = createPathIterator(gvs[i]);
    
            // Container for coordinates and strips; no normals, byReference, no geom transform !
            LineStripArray geomInterim = stringExtruder.createLineStripGeometry(pathIt, false, true, false);
        
            if (geomInterim == null)
                continue;
            
            float[] coords = geomInterim.getCoordRefFloat();
        
            for (int j=0,z=coords.length; j < z; j+=3) {               
                coords[j]   += charTranss[i].x;
                coords[j+1] += charTranss[i].y;
                coords[j+2] += charTranss[i].z;
            }  
            
            int vertexCt = geomInterim.getVertexCount();
            
            int[] stripVertexCt = new int[geomInterim.getNumStrips()];
            geomInterim.getStripVertexCounts(stripVertexCt);
            
            LineStripArray lineStripArray = new LineStripArray(
                            vertexCt,
                            LineStripArray.COORDINATES | LineStripArray.NORMALS, 
                            stripVertexCt);

            // Transform coords  V 1.2
            stringExtruder.transformCoords(coords);
            if (bounds != null)
                coordsList.add(coords);

            lineStripArray.setCoordinates(0, coords);

            for (int j=0; j < vertexCt; j++)
                lineStripArray.setNormal(j, normalArr);
                
            geomList.add(lineStripArray);
            
            geomInterim.setCoordRefFloat(null);
        }
        
        if (geomList.isEmpty())
            return null;
        
        // Transform bounds
        if (bounds != null) {
            stringExtruder.transformBounds(bounds, coordsList);
            coordsList.clear();
        }
        
        LineStripArray[] geomArrays = new LineStripArray[geomList.size()];
        geomList.toArray(geomArrays);
        
        return geomArrays;
    }
        
    private void checkStringAndState(String string) {
        
        if (string == null || string.length() < 1)
            throw new NullPointerException("String3D : string is null or of length < 1 !");
        
        if (stringExtruder == null)
            throw new IllegalStateException("String3D : extruder isn't set !");
        if (stringFont == null)
            throw new IllegalStateException("String3D : font isn't set !");        
    }
    
    private GlyphVector[] createGlyphVectors(String string3d, boolean reverse) {
        
        // isAntiAliased, usesFractionalMetrics
        FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
        
        char[] chars = new char[1];
        int length = string3d.length();
        GlyphVector[] gvs = new GlyphVector[length];
        
        int whitespaceCt = 0;
        
        if (reverse) {
            int k = 0;
            for (int i=length-1; i >= 0; i--) {
                chars[0] = string3d.charAt(i);
                gvs[k++] = stringFont.createGlyphVector(frc, chars);
                
                if (Character.isWhitespace(chars[0]))
                    whitespaceCt++;
            }
        }
        else {
            for (int i=0; i < length; i++) {
                chars[0] = string3d.charAt(i);
                gvs[i] = stringFont.createGlyphVector(frc, chars);
                
                if (Character.isWhitespace(chars[0]))
                    whitespaceCt++;
            }
        }
        
        if (whitespaceCt == length)
            return null;
        else
            return gvs;
    }   
    
    private PathIterator createPathIterator(GlyphVector glyphVector) {
                       
        // Font Y-axis is downwards, so send affine transform to flip it.
        AffineTransform glyphTrans = new AffineTransform();
//        Rectangle2D bnd = glyphVector.getVisualBounds();
           
//        double tx = bnd.getX() + 0.5 * bnd.getWidth();
//        double ty = bnd.getY() + 0.5 * bnd.getHeight();
//        glyphTrans.setToTranslation(-tx, -ty);
//        glyphTrans.scale(1.0, -1.0);
//        glyphTrans.translate(tx, -ty);
        
        glyphTrans.setToScale(1.0, -1.0); // Mirrored at y = 0, no center transform needed !

        Shape shape = glyphVector.getOutline();

        PathIterator pathIt = shape.getPathIterator(glyphTrans, stringExtruder.getTessellationTolerance());
        
        return pathIt;
    }
    
    // String.Character <-> GlyphVector ( >= 1 glyphs)
    private Vector3f[] createCharTranslations(GlyphVector[] glyphVectors, 
                                              BoundingBox bounds, 
                                              Point3f charPosition) {        
        Rectangle2D logicalBounds = null;
        Rectangle2D visualBounds = null;

        double logWidth = 0;
        double logHeight = 0;
        
        double visX = 0;
        double visY = 0;
        
        double visY_trans = 0;
        
        double visWidth = 0;
        double visHeight = 0;
        
        double visWidthMax = 0;         
        double[] visWidths = null;
        
        // Vertical whitespace : 1/4 of logHeight
        double vertWS = 0.25; // + vertS
        // Vertical space : 1/8 of logHeight
        double vertS = 0.125;
        double verticalSpace = 0;

        // Bounding box of all characters / string
        Point3d lower = new Point3d();
        Point3d upper = new Point3d();
        
        // 2D (z = stringPosition.z)
        // Visual bounding box of all characters 
        // from first character != whitespace to last character != whitespace
        Point3d visLower = new Point3d();
        Point3d visUpper = new Point3d();
        boolean isVisBoundsStarted = false;
        
        // Current character is whitespace
        boolean isCurrCharWhitespace = false;
        
        // Position ( = translation ) of current character
        Point3f position= new Point3f();
        
        boolean isPathUp = (stringPath == Path.UP_CENTER || stringPath == Path.UP_LEFT || stringPath == Path.UP_RIGHT);
        boolean isPathDown = (stringPath == Path.DOWN_CENTER || stringPath == Path.DOWN_LEFT || stringPath == Path.DOWN_RIGHT);

    
        int numChars = glyphVectors.length;
        
        Vector3f[] charTranslations = new Vector3f[numChars];       
        for (int i=0; i < numChars; i++) {
            charTranslations[i] = new Vector3f();
        }

        //
        // First character
        //
        
        logicalBounds = glyphVectors[0].getLogicalBounds();
        logWidth = logicalBounds.getWidth();
        logHeight = logicalBounds.getHeight();
        
        visualBounds = glyphVectors[0].getVisualBounds();
        visX = visualBounds.getX();                
        visY = visualBounds.getY();
        visWidth = visualBounds.getWidth();
        visHeight = visualBounds.getHeight(); 
        
        // TODO whitespace: alternative check !?
        if (visWidth < 0.000001 || visHeight < 0.000001) {
            isCurrCharWhitespace = true;
        }
        
        position.set(charPosition);
        
        // Start bounding box
        lower.set(position);
        upper.set(position);
        
        visLower.z = charPosition.z;
        visUpper.z = charPosition.z;
                
        if (stringPath == Path.RIGHT || stringPath == Path.LEFT) {
        
            // Left alignment
            position.x += (float)visX * (-1); 
            
            if (numChars == 1) // !isCurrCharWhitespace
                upper.x += visWidth;
            else
                upper.x += logWidth - visX; 

            // Visual bounds
            if (!isCurrCharWhitespace) { 
                isVisBoundsStarted = true;
                visLower.x = charPosition.x;
                visLower.y = charPosition.y + (-1) * (visY + visHeight);
                visUpper.x = visLower.x + visWidth;
                visUpper.y = visLower.y + visHeight;                
            }
        }
        else if (isPathUp || isPathDown) {
            
            // Collect width for DOWN_/UP_CENTER, DOWN_/UP_RIGHT
            visWidths = new double[numChars];
            visWidths[0] = visWidth;
            visWidthMax = visWidth;
            
            // Vertical space and spacing; 'logHeight' is constant for given font and size
            verticalSpace = logHeight * vertS + stringSpacing;

            // Whitespace
            if (isCurrCharWhitespace) {
                visHeight = logHeight * vertWS;
            }
            
            // DOWN_/UP_LEFT : default
            position.x += (float)visX * (-1); 
            
            // see below
            visY_trans = (visualBounds.getY() + visHeight); 
            
            position.y = (float)(upper.y + visY_trans); 

            upper.y += visHeight;
                        
            // Visual bounds
            if (!isCurrCharWhitespace) { 
                isVisBoundsStarted = true;
                visLower.x = charPosition.x;
                visLower.y = charPosition.y;
                visUpper.x = visLower.x + visWidth;
                visUpper.y = visLower.y + visHeight;                
            }
        }
        
        charTranslations[0].set(position);       

        //
        // Remaining characters
        //
        for (int i=1; i < numChars; i++) {
                       
            logicalBounds = glyphVectors[i].getLogicalBounds();
            logWidth = logicalBounds.getWidth();
            logHeight = logicalBounds.getHeight();
            
            visualBounds = glyphVectors[i].getVisualBounds();
            visX = visualBounds.getX();
            visY = visualBounds.getY();
            visWidth = visualBounds.getWidth();
            visHeight = visualBounds.getHeight(); 
            
            // TODO whitespace: alternative check !?
            isCurrCharWhitespace = (visWidth < 0.000001 || visHeight < 0.000001);
            
            // for both: reversed and non-reversed string
            
            if (stringPath == Path.RIGHT || stringPath == Path.LEFT) {
                
                // Horizontal whitespace has logWidth > 0
                
                upper.x     += stringSpacing;
                
                position.x  = (float)upper.x;
                                
                if (i == numChars-1) {
                    if (isCurrCharWhitespace)
                        upper.x += logWidth;
                    else
                        upper.x += (visX + visWidth);
                }
                else {
                    upper.x += logWidth;    
                }
                
                // Visual bounds
                if (!isCurrCharWhitespace) {
                    if (isVisBoundsStarted) {
                     // visLower.x   done
                        visLower.y = Math.min(visLower.y, charPosition.y + (-1) * (visY + visHeight));
                        visUpper.x = position.x + (visX + visWidth);
                        visUpper.y = Math.max(visUpper.y, charPosition.y + (-1) * visY); 
                    }
                    else {
                        isVisBoundsStarted = true;
                        visLower.x = position.x + visX;
                        visLower.y = charPosition.y + (-1) * (visY + visHeight);
                        visUpper.x = visLower.x + visWidth;
                        visUpper.y = visLower.y + visHeight; 
                    }
                }
            }
            else if (isPathUp || isPathDown) {     
                
                // Vertical whitespace : define height !
                if (isCurrCharWhitespace) {
                    visHeight = logHeight * vertWS;
                }
                
                // local coordinate system of glyph
                
                // Visual bounds samples, font size 100
                // glyphVector.getVisualBounds x=6.59375 y=-53.03125 w=45.015625 h=72.90625   p, q
                // glyphVector.getVisualBounds x=3.90625 y=-53.03125 w=45.171875 h=54.203125
                // glyphVector.getVisualBounds x=-4.5937 y=-71.5781  w=19.921875 h=92.625      j
                
                // x, y  coordinates of the upper-left corner = lower-left for a glyph of a font (upside-down)
                
                // (y + h) = the bottom of the upside-down glyph
                
                // => -(y + h) translates the bottom to (y = 0)
                
                // later this glyph will be flipped at (y = 0) 
                
                // => (y + h) translates the bottom of the flipped glyph to (y = 0)
                
                // so adding visHeight+space provides next 3D y-position
                
                visY_trans = (visualBounds.getY() + visHeight); // if whitespace: 0 + 0
                
                // (DOWN_/UP_) LEFT : default                 
                position.x = charPosition.x + (float)visX * (-1);             
                
                upper.y    += verticalSpace;
                               
                position.y = (float)(upper.y + visY_trans); 
                
                upper.y    += visHeight; 
                
                // Visual bounds
                if (!isCurrCharWhitespace) {
                    if (isVisBoundsStarted) {
                     // visLower.x   done
                     // visLower.y   done    
                        visUpper.x = Math.max(visUpper.x, visLower.x + visWidth);
                        visUpper.y = upper.y; 
                    }
                    else {
                        isVisBoundsStarted = true;
                        visLower.x = charPosition.x;
                        visLower.y = charPosition.y + upper.y - visHeight;
                        visUpper.x = visLower.x + visWidth;
                        visUpper.y = visLower.y + visHeight;                
                    }
                }
                                                
                // Collect visual width for center/right alignment
                visWidths[i] = visWidth;
                if (visWidthMax < visWidth) {
                    visWidthMax = visWidth;
                }
            }
            
            charTranslations[i].set(position);
        }

        //
        // Handle alignments. 
        //
        
        // UP/DOWN : alignment _CENTER, _RIGHT;  default: _LEFT
        if (isPathUp || isPathDown) {           
                
            double visdx = (visUpper.x - visLower.x); // visual x length
            
            if (stringPath == Path.UP_CENTER || stringPath == Path.DOWN_CENTER) {
                for (int i=0; i < numChars; i++) {
                    charTranslations[i].x -= visWidths[i] / 2;
                }
                visLower.x -= visdx / 2;
                visUpper.x -= visdx / 2;
            }
            else if (stringPath == Path.UP_RIGHT || stringPath == Path.DOWN_RIGHT) {
                for (int i=0; i < numChars; i++) {
                    charTranslations[i].x -= visWidths[i];
                }
                visLower.x -= visdx;
                visUpper.x -= visdx;
            }           
        }
        
        // String alignment
        
        // String lengths
        double dx = (upper.x - lower.x); // x length : Path.LEFT/RIGHT
        double dy = (upper.y - lower.y); // y length : isPathDown/isPathUp
        
        if (stringAlignment == Alignment.FIRST) {
            if (stringPath == Path.LEFT) {
                for (int i=0; i < numChars; i++) {
                    charTranslations[i].x -= dx; 
                }
                visLower.x -= dx;
                visUpper.x -= dx;
            }
            else if (isPathDown) {
                for (int i=0; i < numChars; i++) {
                    charTranslations[i].y -= dy; 
                }
                visLower.y -= dy;
                visUpper.y -= dy;
            }
        }
        else if (stringAlignment == Alignment.LAST) {
            if (stringPath == Path.RIGHT) {
                for (int i=0; i < numChars; i++) {
                    charTranslations[i].x -= dx; 
                }
                visLower.x -= dx;
                visUpper.x -= dx;
            }
            else if (isPathUp) {
                for (int i=0; i < numChars; i++) {
                    charTranslations[i].y -= dy; 
                }
                visLower.y -= dy;
                visUpper.y -= dy;
            }
        }
        else if (stringAlignment == Alignment.CENTER) {
                      
            // 1/2 x length, 1/2 y length
            dx *= 0.5;
            dy *= 0.5;
            
            if (stringPath == Path.RIGHT || stringPath == Path.LEFT) {
                for (int i=0; i < numChars; i++) {
                    charTranslations[i].x -= dx; 
                }
                visLower.x -= dx;
                visUpper.x -= dx;
            }
            else if (isPathUp || isPathDown) {
                for (int i=0; i < numChars; i++) {
                    charTranslations[i].y -= dy; 
                }
                visLower.y -= dy;
                visUpper.y -= dy;
            }
        }
        
        // Return visual bounding box
        if (bounds != null) {
            bounds.setLower(visLower);
            bounds.setUpper(visUpper);
        }

        // Return character translations        
        return charTranslations;
    }
}
