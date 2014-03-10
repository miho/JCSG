/**
 * Polygon.java
 *
 * Copyright 2014-2014 Michael Hoffer <info@michaelhoffer.de>. All rights reserved.
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
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info@michaelhoffer.de> "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info@michaelhoffer.de> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of Michael Hoffer <info@michaelhoffer.de>.
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
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Polygon implements Triangulatable
{
    private final static Logger logger = LoggerFactory.getLogger( Polygon.class );

    protected ArrayList<TriangulationPoint> _points = new ArrayList<TriangulationPoint>();
    protected ArrayList<TriangulationPoint> _steinerPoints;
    protected ArrayList<Polygon> _holes;

    protected List<DelaunayTriangle> m_triangles;

    protected PolygonPoint _last;

    /**
     * To create a polygon we need atleast 3 separate points
     * 
     * @param p1
     * @param p2
     * @param p3
     */
    public Polygon( PolygonPoint p1, PolygonPoint p2, PolygonPoint p3 )
    {
        p1._next = p2;
        p2._next = p3;
        p3._next = p1;
        p1._previous = p3;
        p2._previous = p1;
        p3._previous = p2;
        _points.add( p1 );
        _points.add( p2 );
        _points.add( p3 );
    }

    /**
     * Requires atleast 3 points
     * @param points - ordered list of points forming the polygon. 
     *                 No duplicates are allowed
     */
    public Polygon( List<PolygonPoint> points )
    {
        // Lets do one sanity check that first and last point hasn't got same position
        // Its something that often happen when importing polygon data from other formats
        if( points.get(0).equals( points.get(points.size()-1) ) )
        {
            logger.warn( "Removed duplicate point");
            points.remove( points.size()-1 );
        }
        _points.addAll( points );
    }

    /**
     * Requires atleast 3 points
     *
     * @param points
     */
    public Polygon( PolygonPoint[] points )
    {        
        this( Arrays.asList( points ) );
    }

    public TriangulationMode getTriangulationMode()
    {
        return TriangulationMode.POLYGON;
    }

    public int pointCount()
    {
        int count = _points.size();
        if( _steinerPoints != null )
        {
            count += _steinerPoints.size();
        }
        return count;
    }

    public void addSteinerPoint( TriangulationPoint point )
    {
        if( _steinerPoints == null )
        {
            _steinerPoints = new ArrayList<TriangulationPoint>();
        }
        _steinerPoints.add( point );        
    }
    
    public void addSteinerPoints( List<TriangulationPoint> points )
    {
        if( _steinerPoints == null )
        {
            _steinerPoints = new ArrayList<TriangulationPoint>();
        }
        _steinerPoints.addAll( points );        
    }

    public void clearSteinerPoints()
    {
        if( _steinerPoints != null )
        {
            _steinerPoints.clear();
        }
    }

    /**
     * Assumes: that given polygon is fully inside the current polygon 
     * @param poly - a subtraction polygon
     */
    public void addHole( Polygon poly )
    {
        if( _holes == null )
        {
            _holes = new ArrayList<Polygon>();
        }
        _holes.add( poly );
        // XXX: tests could be made here to be sure it is fully inside
//        addSubtraction( poly.getPoints() );
    }

    /**
     * Will insert a point in the polygon after given point 
     * 
     * @param a
     * @param b
     * @param p
     */
    public void insertPointAfter( PolygonPoint a, PolygonPoint newPoint )
    {
        // Validate that 
        int index = _points.indexOf( a );
        if( index != -1 )
        {
            newPoint.setNext( a.getNext() );
            newPoint.setPrevious( a );
            a.getNext().setPrevious( newPoint );
            a.setNext( newPoint );
            _points.add( index+1, newPoint );
        }
        else
        {
            throw new RuntimeException( "Tried to insert a point into a Polygon after a point not belonging to the Polygon" );
        }
    }

    public void addPoints( List<PolygonPoint> list )    
    {
        PolygonPoint first;
        for( PolygonPoint p : list )
        {
            p.setPrevious( _last );
            if( _last != null )
            {
                p.setNext( _last.getNext() );
                _last.setNext( p );
            }
            _last = p;
            _points.add( p );
        }
        first = (PolygonPoint)_points.get(0);
        _last.setNext( first );
        first.setPrevious( _last );
    }
    
    /**
     * Will add a point after the last point added
     * 
     * @param p
     */
    public void addPoint(PolygonPoint p )    
    {
        p.setPrevious( _last );
        p.setNext( _last.getNext() );
        _last.setNext( p );
        _points.add( p );
    }
    
    public void removePoint( PolygonPoint p )
    {
        PolygonPoint next, prev;
        
        next = p.getNext();
        prev = p.getPrevious();
        prev.setNext( next );
        next.setPrevious( prev );
        _points.remove( p );
    }

    public PolygonPoint getPoint()
    {
        return _last;
    }
    
    public List<TriangulationPoint> getPoints()
    {
        return _points;
    }

    public List<DelaunayTriangle> getTriangles()
    {
        return m_triangles;
    }
    
    public void addTriangle( DelaunayTriangle t )
    {
        m_triangles.add( t );
    }

    public void addTriangles( List<DelaunayTriangle> list )
    {
        m_triangles.addAll( list );
    }

    public void clearTriangulation()
    {
        if( m_triangles != null )
        {
            m_triangles.clear();
        }
    }

    /**
     * Creates constraints and populates the context with points
     */
    public void prepareTriangulation( TriangulationContext<?> tcx )
    {
        if( m_triangles == null )
        {
            m_triangles = new ArrayList<DelaunayTriangle>( _points.size() );
        }
        else
        {
            m_triangles.clear();
        }

        // Outer constraints
        for( int i = 0; i < _points.size()-1 ; i++ )
        {
            tcx.newConstraint( _points.get( i ), _points.get( i+1 ) );
        }
        tcx.newConstraint( _points.get( 0 ), _points.get( _points.size()-1 ) );
        tcx.addPoints( _points );

        // Hole constraints
        if( _holes != null )
        {
            for( Polygon p : _holes )
            {
                for( int i = 0; i < p._points.size()-1 ; i++ )
                {
                    tcx.newConstraint( p._points.get( i ), p._points.get( i+1 ) );
                }
                tcx.newConstraint( p._points.get( 0 ), p._points.get( p._points.size()-1 ) );            
                tcx.addPoints( p._points );
            }
        }

        if( _steinerPoints != null )
        {
            tcx.addPoints( _steinerPoints );
        }
    }

}
