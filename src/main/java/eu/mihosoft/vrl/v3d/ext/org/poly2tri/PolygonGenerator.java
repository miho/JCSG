/**
 * PolygonGenerator.java
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
// TODO: Auto-generated Javadoc
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

/**
 * The Class PolygonGenerator.
 */
class PolygonGenerator
{
    
    /** The Constant PI_2. */
    private static final double PI_2 = 2.0*Math.PI;
    
    /**
     * Random circle sweep.
     *
     * @param scale the scale
     * @param vertexCount the vertex count
     * @return the polygon
     */
    public static Polygon RandomCircleSweep( double scale, int vertexCount )
    {
        PolygonPoint point;
        PolygonPoint[] points;
        double radius = scale/4;

        points = new PolygonPoint[vertexCount];
        for(int i=0; i<vertexCount; i++)
        {
            do
            {
                if( i%250 == 0 )
                {
                    radius += scale/2*(0.5 - Math.random());
                }
                else if( i%50 == 0 )
                {
                    radius += scale/5*(0.5 - Math.random());
                }
                else
                {
                    radius += 25*scale/vertexCount*(0.5 - Math.random());                                        
                }
                radius = radius > scale/2 ? scale/2 : radius;
                radius = radius < scale/10 ? scale/10 : radius;
            } while( radius < scale/10 || radius > scale/2 ); 
            point = new PolygonPoint( radius*Math.cos( (PI_2*i)/vertexCount ), 
                                      radius*Math.sin( (PI_2*i)/vertexCount ) );
            points[i] = point;
        }            
        return new Polygon( points );
    }

    /**
     * Random circle sweep2.
     *
     * @param scale the scale
     * @param vertexCount the vertex count
     * @return the polygon
     */
    public static Polygon RandomCircleSweep2( double scale, int vertexCount )
    {
        PolygonPoint point;
        PolygonPoint[] points;
        double radius = scale/4;

        points = new PolygonPoint[vertexCount];
        for(int i=0; i<vertexCount; i++)
        {
            do
            {
                radius += scale/5*(0.5 - Math.random());                                        
                radius = radius > scale/2 ? scale/2 : radius;
                radius = radius < scale/10 ? scale/10 : radius;
            } while( radius < scale/10 || radius > scale/2 ); 
            point = new PolygonPoint( radius*Math.cos( (PI_2*i)/vertexCount ), 
                                      radius*Math.sin( (PI_2*i)/vertexCount ) );
            points[i] = point;
        }            
        return new Polygon( points );
    }
}
