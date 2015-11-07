/**
 * PointSet.java
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
import java.util.ArrayList;
import java.util.List;


// TODO: Auto-generated Javadoc
/**
 * The Class PointSet.
 */
class PointSet implements Triangulatable
{
    
    /** The _points. */
    List<TriangulationPoint> _points;
    
    /** The _triangles. */
    List<DelaunayTriangle> _triangles;
    
    /**
     * Instantiates a new point set.
     *
     * @param points the points
     */
    public PointSet( List<TriangulationPoint> points )
    {
        _points = new ArrayList<>();
        _points.addAll( points );
    }
    
    /* (non-Javadoc)
     * @see eu.mihosoft.vrl.v3d.ext.org.poly2tri.Triangulatable#getTriangulationMode()
     */
    public TriangulationMode getTriangulationMode()
    {
        return TriangulationMode.UNCONSTRAINED;
    }

    /* (non-Javadoc)
     * @see eu.mihosoft.vrl.v3d.ext.org.poly2tri.Triangulatable#getPoints()
     */
    public List<TriangulationPoint> getPoints()
    {
        return _points;
    }
    
    /* (non-Javadoc)
     * @see eu.mihosoft.vrl.v3d.ext.org.poly2tri.Triangulatable#getTriangles()
     */
    public List<DelaunayTriangle> getTriangles()
    {
        return _triangles;
    }
    
    /* (non-Javadoc)
     * @see eu.mihosoft.vrl.v3d.ext.org.poly2tri.Triangulatable#addTriangle(eu.mihosoft.vrl.v3d.ext.org.poly2tri.DelaunayTriangle)
     */
    public void addTriangle( DelaunayTriangle t )
    {
        _triangles.add( t );
    }

    /* (non-Javadoc)
     * @see eu.mihosoft.vrl.v3d.ext.org.poly2tri.Triangulatable#addTriangles(java.util.List)
     */
    public void addTriangles( List<DelaunayTriangle> list )
    {
        _triangles.addAll( list );
    }

    /* (non-Javadoc)
     * @see eu.mihosoft.vrl.v3d.ext.org.poly2tri.Triangulatable#clearTriangulation()
     */
    public void clearTriangulation()
    {
        _triangles.clear();            
    }

    /* (non-Javadoc)
     * @see eu.mihosoft.vrl.v3d.ext.org.poly2tri.Triangulatable#prepareTriangulation(eu.mihosoft.vrl.v3d.ext.org.poly2tri.TriangulationContext)
     */
    public void prepareTriangulation( TriangulationContext<?> tcx )
    {
        if( _triangles == null )
        {
            _triangles = new ArrayList<DelaunayTriangle>( _points.size() );            
        }
        else
        {
            _triangles.clear();                        
        }
        tcx.addPoints( _points );
    }
}
