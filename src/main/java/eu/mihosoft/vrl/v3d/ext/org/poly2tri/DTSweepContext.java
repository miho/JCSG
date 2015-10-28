/**
 * DTSweepContext.java
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


import java.util.ArrayDeque;
import java.util.Collections;
// TODO: Auto-generated Javadoc
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;



/**
 * The Class DTSweepContext.
 *
 * @author Thomas ??? (thahlen@gmail.com)
 */
class DTSweepContext extends TriangulationContext<DTSweepDebugContext>
{
//    private final static Logger //logger = LoggerFactory.getLogger( DTSweepContext.class );

    // Inital triangle factor, seed triangle will extend 30% of 
    /** The alpha. */
// PointSet width to both left and right.
    private final float ALPHA = 0.3f;

    /**  Advancing front *. */
    protected AdvancingFront aFront;
    
    /**  head point used with advancing front. */
    private TriangulationPoint _head;
    
    /**  tail point used with advancing front. */
    private TriangulationPoint _tail;
    
    /** The basin. */
    protected Basin basin = new Basin();
    
    /** The edge event. */
    protected EdgeEvent edgeEvent = new EdgeEvent();
    
    /** The _comparator. */
    private DTSweepPointComparator _comparator = new DTSweepPointComparator();
    
    /**
     * Instantiates a new DT sweep context.
     */
    public DTSweepContext()
    {
        clear();
    }
        
    /* (non-Javadoc)
     * @see eu.mihosoft.vrl.v3d.ext.org.poly2tri.TriangulationContext#isDebugEnabled(boolean)
     */
    public void isDebugEnabled( boolean b )
    {
        if( b )
        {
            if( _debug == null )
            {
                _debug = new DTSweepDebugContext(this);
            }
        }
        _debugEnabled  = b;
    }

    /**
     * Removes the from list.
     *
     * @param triangle the triangle
     */
    public void removeFromList( DelaunayTriangle triangle )
    {
        _triList.remove( triangle );
        // TODO: remove all neighbor pointers to this triangle
//        for( int i=0; i<3; i++ )
//        {
//            if( triangle.neighbors[i] != null )
//            {
//                triangle.neighbors[i].clearNeighbor( triangle );
//            }
//        }
//        triangle.clearNeighbors();
    }

    /**
     * Mesh clean.
     *
     * @param triangle the triangle
     */
    protected void meshClean(DelaunayTriangle triangle)
    {
    	DelaunayTriangle t1,t2;
        if( triangle != null )
        {	
	        ArrayDeque<DelaunayTriangle> deque = new ArrayDeque<DelaunayTriangle>();
	        deque.addFirst(triangle);
	        triangle.isInterior(true);
	
	        while( !deque.isEmpty() )
	        {
	            t1 = deque.removeFirst();
	            _triUnit.addTriangle( t1 );
	            for( int i=0; i<3; ++i )
	            {
	                if( !t1.cEdge[i] ) 
	                {
	                    t2 = t1.neighbors[i];
	                    if( t2 != null && !t2.isInterior() ) 
	                    {
	                        t2.isInterior(true);
	                        deque.addLast(t2);
	                    }
	                }
	            }
	        }
        }
    }
    
    /* (non-Javadoc)
     * @see eu.mihosoft.vrl.v3d.ext.org.poly2tri.TriangulationContext#clear()
     */
    public void clear()
    {
        super.clear();
        _triList.clear();
    }

    /**
     * Gets the advancing front.
     *
     * @return the advancing front
     */
    public AdvancingFront getAdvancingFront()
    {
        return aFront;
    }

    /**
     * Sets the head.
     *
     * @param p1 the new head
     */
    public void setHead( TriangulationPoint p1 ) { _head = p1; }
    
    /**
     * Gets the head.
     *
     * @return the head
     */
    public TriangulationPoint getHead() { return _head; }

    /**
     * Sets the tail.
     *
     * @param p1 the new tail
     */
    public void setTail( TriangulationPoint p1 ) { _tail = p1; }
    
    /**
     * Gets the tail.
     *
     * @return the tail
     */
    public TriangulationPoint getTail() { return _tail; }

    /**
     * Adds the node.
     *
     * @param node the node
     */
    public void addNode( AdvancingFrontNode node )
    {
//        System.out.println( "add:" + node.key + ":" + System.identityHashCode(node.key));
//        m_nodeTree.put( node.getKey(), node );
        aFront.addNode( node );
    }

    /**
     * Removes the node.
     *
     * @param node the node
     */
    public void removeNode( AdvancingFrontNode node )
    {
//        System.out.println( "remove:" + node.key + ":" + System.identityHashCode(node.key));
//        m_nodeTree.delete( node.getKey() );
        aFront.removeNode( node );
    }

    /**
     * Locate node.
     *
     * @param point the point
     * @return the advancing front node
     */
    public AdvancingFrontNode locateNode( TriangulationPoint point )
    {
        return aFront.locateNode( point );
    }

    /**
     * Creates the advancing front.
     */
    public void createAdvancingFront()
    {
        AdvancingFrontNode head,tail,middle;
        // Initial triangle
        DelaunayTriangle iTriangle = new DelaunayTriangle( _points.get(0), 
                                                           getTail(), 
                                                           getHead() );
        addToList( iTriangle );
        
        head = new AdvancingFrontNode( iTriangle.points[1] );
        head.triangle = iTriangle;
        middle = new AdvancingFrontNode( iTriangle.points[0] );
        middle.triangle = iTriangle;
        tail = new AdvancingFrontNode( iTriangle.points[2] );

        aFront = new AdvancingFront( head, tail ); 
        aFront.addNode( middle );
        
        // TODO: I think it would be more intuitive if head is middles next and not previous
        //       so swap head and tail
        aFront.head.next = middle;
        middle.next = aFront.tail;
        middle.prev = aFront.head;
        aFront.tail.prev = middle;
    }
    
    /**
     * The Class Basin.
     */
    class Basin
    {
        
        /** The left node. */
        AdvancingFrontNode leftNode;
        
        /** The bottom node. */
        AdvancingFrontNode bottomNode;
        
        /** The right node. */
        AdvancingFrontNode rightNode;
        
        /** The width. */
        public double width;
        
        /** The left highest. */
        public boolean leftHighest;        
    }
    
    /**
     * The Class EdgeEvent.
     */
    class EdgeEvent
    {
        
        /** The constrained edge. */
        DTSweepConstraint constrainedEdge;
        
        /** The right. */
        public boolean right;
    }

    /**
     * Try to map a node to all sides of this triangle that don't have 
     * a neighbor.
     *
     * @param t the t
     */
    public void mapTriangleToNodes( DelaunayTriangle t )
    {
        AdvancingFrontNode n;
        for( int i=0; i<3; i++ )
        {
            if( t.neighbors[i] == null )
            {
                n = aFront.locatePoint( t.pointCW( t.points[i] ) );
                if( n != null )
                {
                    n.triangle = t;
                }
            }            
        }        
    }

    /* (non-Javadoc)
     * @see eu.mihosoft.vrl.v3d.ext.org.poly2tri.TriangulationContext#prepareTriangulation(eu.mihosoft.vrl.v3d.ext.org.poly2tri.Triangulatable)
     */
    @Override
    public void prepareTriangulation( Triangulatable t )
    {
        super.prepareTriangulation( t );

        double xmax, xmin;
        double ymax, ymin;

        xmax = xmin = _points.get(0).getX();
        ymax = ymin = _points.get(0).getY();
        // Calculate bounds. Should be combined with the sorting
        for( TriangulationPoint p : _points )
        {
            if( p.getX() > xmax )
                xmax = p.getX();
            if( p.getX() < xmin )
                xmin = p.getX();
            if( p.getY() > ymax )
                ymax = p.getY();
            if( p.getY() < ymin )
                ymin = p.getY();
        }

        double deltaX = ALPHA * ( xmax - xmin );
        double deltaY = ALPHA * ( ymax - ymin );
        TPoint p1 = new TPoint( xmax + deltaX, ymin - deltaY );
        TPoint p2 = new TPoint( xmin - deltaX, ymin - deltaY );

        setHead( p1 );
        setTail( p2 );

//        long time = System.nanoTime();
        // Sort the points along y-axis
        Collections.sort( _points, _comparator );
//        //logger.info( "Triangulation setup [{}ms]", ( System.nanoTime() - time ) / 1e6 );
    }


    /**
     * Finalize triangulation.
     */
    public void finalizeTriangulation()
    {
        _triUnit.addTriangles( _triList );
        _triList.clear();
    }

    /* (non-Javadoc)
     * @see eu.mihosoft.vrl.v3d.ext.org.poly2tri.TriangulationContext#newConstraint(eu.mihosoft.vrl.v3d.ext.org.poly2tri.TriangulationPoint, eu.mihosoft.vrl.v3d.ext.org.poly2tri.TriangulationPoint)
     */
    @Override
    public TriangulationConstraint newConstraint( TriangulationPoint a, TriangulationPoint b )
    {
        return new DTSweepConstraint( a, b );        
    }

    /* (non-Javadoc)
     * @see eu.mihosoft.vrl.v3d.ext.org.poly2tri.TriangulationContext#algorithm()
     */
    @Override
    public TriangulationAlgorithm algorithm()
    {
        return TriangulationAlgorithm.DTSweep;
    }
}
