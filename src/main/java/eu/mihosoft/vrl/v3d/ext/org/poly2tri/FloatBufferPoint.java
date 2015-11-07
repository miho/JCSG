/**
 * FloatBufferPoint.java
 *
 * Copyright 2014-2014 Michael Hoffer info@michaelhoffer.de. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer info@michaelhoffer.de "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer info@michaelhoffer.de OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of Michael Hoffer info@michaelhoffer.de.
 */ 

package eu.mihosoft.vrl.v3d.ext.org.poly2tri;
/* Poly2Tri
 * Copyright (c) 2009-2010, Poly2Tri Contributors
 * http://code.google.com/p/poly2tri/
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of Poly2Tri nor the names of its contributors may be
 *   used to endorse or promote products derived from this software without specific
 *   prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
import java.nio.FloatBuffer;



// TODO: Auto-generated Javadoc
/**
 * The Class FloatBufferPoint.
 */
class FloatBufferPoint extends TriangulationPoint
{
    
    /** The _fb. */
    private final FloatBuffer _fb;
    
    /** The _iz. */
    private final int _ix,_iy,_iz;
    
    /**
     * Instantiates a new float buffer point.
     *
     * @param fb the fb
     * @param index the index
     */
    public FloatBufferPoint( FloatBuffer fb, int index )
    {
        _fb = fb;
        _ix = index;
        _iy = index+1;
        _iz = index+2;
    }
    
    /* (non-Javadoc)
     * @see eu.mihosoft.vrl.v3d.ext.org.poly2tri.TriangulationPoint#getX()
     */
    public final double getX()
    {
        return _fb.get( _ix );
    }
    
    /* (non-Javadoc)
     * @see eu.mihosoft.vrl.v3d.ext.org.poly2tri.TriangulationPoint#getY()
     */
    public final double getY()
    {
        return _fb.get( _iy );
    }
    
    /* (non-Javadoc)
     * @see eu.mihosoft.vrl.v3d.ext.org.poly2tri.TriangulationPoint#getZ()
     */
    public final double getZ()
    {
        return _fb.get( _iz );
    }
    
    /* (non-Javadoc)
     * @see eu.mihosoft.vrl.v3d.ext.org.poly2tri.TriangulationPoint#getXf()
     */
    public final float getXf()
    {
        return _fb.get( _ix );
    }
    
    /* (non-Javadoc)
     * @see eu.mihosoft.vrl.v3d.ext.org.poly2tri.TriangulationPoint#getYf()
     */
    public final float getYf()
    {
        return _fb.get( _iy );
    }
    
    /* (non-Javadoc)
     * @see eu.mihosoft.vrl.v3d.ext.org.poly2tri.TriangulationPoint#getZf()
     */
    public final float getZf()
    {
        return _fb.get( _iz );
    }

    /* (non-Javadoc)
     * @see eu.mihosoft.vrl.v3d.ext.org.poly2tri.TriangulationPoint#set(double, double, double)
     */
    @Override
    public void set( double x, double y, double z )
    {
        _fb.put( _ix, (float)x );
        _fb.put( _iy, (float)y );
        _fb.put( _iz, (float)z );
    }
    
    /**
     * To points.
     *
     * @param fb the fb
     * @return the triangulation point[]
     */
    public static TriangulationPoint[] toPoints( FloatBuffer fb )
    {
        FloatBufferPoint[] points = new FloatBufferPoint[fb.limit()/3];
        for( int i=0,j=0; i<points.length; i++, j+=3 )
        {
            points[i] = new FloatBufferPoint(fb, j);
        }        
        return points;
    }
}
