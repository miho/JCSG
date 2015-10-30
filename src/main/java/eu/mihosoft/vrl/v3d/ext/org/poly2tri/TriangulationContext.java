/**
 * TriangulationContext.java
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
 * The Class TriangulationContext.
 *
 * @param <A> the generic type
 */
abstract class TriangulationContext<A extends TriangulationDebugContext>
{
    
    /** The _debug. */
    protected A _debug;
    
    /** The _debug enabled. */
    protected boolean _debugEnabled = false;
    
    /** The _tri list. */
    protected ArrayList<DelaunayTriangle> _triList = new ArrayList<DelaunayTriangle>();

    /** The _points. */
    protected ArrayList<TriangulationPoint> _points = new ArrayList<TriangulationPoint>(200);
    
    /** The _triangulation mode. */
    protected TriangulationMode _triangulationMode;
    
    /** The _tri unit. */
    protected Triangulatable _triUnit;

    /** The _terminated. */
    private boolean _terminated = false;
    
    /** The _wait until notified. */
    private boolean _waitUntilNotified;

    /** The _step time. */
    private int _stepTime = -1;
    
    /** The _step count. */
    private int _stepCount = 0;
    
    /**
     * Gets the step count.
     *
     * @return the step count
     */
    public int getStepCount() { return _stepCount; }

    /**
     * Done.
     */
    public void done()
    {
        _stepCount++;
    }

    /**
     * Algorithm.
     *
     * @return the triangulation algorithm
     */
    public abstract TriangulationAlgorithm algorithm();
    
    /**
     * Prepare triangulation.
     *
     * @param t the t
     */
    public void prepareTriangulation( Triangulatable t )
    {
        _triUnit = t;
        _triangulationMode = t.getTriangulationMode();
        t.prepareTriangulation( this );
    }
    
    /**
     * New constraint.
     *
     * @param a the a
     * @param b the b
     * @return the triangulation constraint
     */
    public abstract TriangulationConstraint newConstraint( TriangulationPoint a, TriangulationPoint b );
    
    /**
     * Adds the to list.
     *
     * @param triangle the triangle
     */
    public void addToList( DelaunayTriangle triangle )
    {
        _triList.add( triangle );
    }
        
    /**
     * Gets the triangles.
     *
     * @return the triangles
     */
    public List<DelaunayTriangle> getTriangles()
    {
        return _triList;
    }

    /**
     * Gets the triangulatable.
     *
     * @return the triangulatable
     */
    public Triangulatable getTriangulatable()
    {
        return _triUnit;
    }
    
    /**
     * Gets the points.
     *
     * @return the points
     */
    public List<TriangulationPoint> getPoints()
    {
        return _points;
    }

    /**
     * Update.
     *
     * @param message the message
     */
    public synchronized void update(String message)
    {
        if( _debugEnabled )
        {
            try
            {
                synchronized( this )
                {
                    _stepCount++;
                    if( _stepTime > 0 )
                    {
                        wait( (int)_stepTime );
                        /** Can we resume execution or are we expected to wait? */ 
                        if( _waitUntilNotified )
                        {
                            wait();
                        }
                    }
                    else
                    {
                        wait();
                    }
                    // We have been notified
                    _waitUntilNotified = false;
                }
            }
            catch( InterruptedException e )
            {
                update("Triangulation was interrupted");
            }
        }
        if( _terminated )
        {
            throw new RuntimeException( "Triangulation process terminated before completion");
        }
    }
    
    /**
     * Clear.
     */
    public void clear()
    {
        _points.clear();
        _terminated = false;
        if( _debug != null )
        {
            _debug.clear();
        }
        _stepCount=0;
    }

    /**
     * Gets the triangulation mode.
     *
     * @return the triangulation mode
     */
    public TriangulationMode getTriangulationMode()
    {
        return _triangulationMode;
    }
    
    /**
     * Wait until notified.
     *
     * @param b the b
     */
    public synchronized void waitUntilNotified(boolean b)
    {
        _waitUntilNotified = b;
    }

    /**
     * Terminate triangulation.
     */
    public void terminateTriangulation()
    {
        _terminated=true;
    }

    /**
     * Checks if is debug enabled.
     *
     * @return true, if is debug enabled
     */
    public boolean isDebugEnabled()
    {
        return _debugEnabled;
    }
    
    /**
     * Checks if is debug enabled.
     *
     * @param b the b
     */
    public abstract void isDebugEnabled( boolean b );

    /**
     * Gets the debug context.
     *
     * @return the debug context
     */
    public A getDebugContext()
    {
        return _debug;
    }
    
    /**
     * Adds the points.
     *
     * @param points the points
     */
    public void addPoints( List<TriangulationPoint> points )
    {
        _points.addAll( points );
    }
}
