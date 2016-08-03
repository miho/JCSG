/**
 * TriangulationProcess.java
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
import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.List;
// TODO: Auto-generated Javadoc
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;




/**
 * The Class TriangulationProcess.
 *
 * @author Thomas ???, thahlen@gmail.com
 */
class TriangulationProcess implements Runnable
{
//    private final static Logger //logger = LoggerFactory.getLogger( TriangulationProcess.class );

    /** The _algorithm. */
private final TriangulationAlgorithm _algorithm;
    
    /** The _tcx. */
    private TriangulationContext<?> _tcx;
    
    /** The _thread. */
    private Thread                  _thread;
    
    /** The _is terminated. */
    private boolean                 _isTerminated = false;
    
    /** The _point count. */
    private int                     _pointCount = 0;
    
    /** The _timestamp. */
    private long                    _timestamp = 0;
    
    /** The _triangulation time. */
    private double                  _triangulationTime = 0;

    /** The _awaiting termination. */
    private boolean                 _awaitingTermination;
    
    /** The _restart. */
    private boolean                 _restart = false;
    
    /** The _triangulations. */
    private ArrayList<Triangulatable> _triangulations = new ArrayList<Triangulatable>();
    
    /** The _listeners. */
    private ArrayList<TriangulationProcessListener> _listeners = new ArrayList<TriangulationProcessListener>();
    
    /**
     * Adds the listener.
     *
     * @param listener the listener
     */
    public void addListener( TriangulationProcessListener listener )
    {
        _listeners.add( listener );
    }
    
    /**
     * Removes the listener.
     *
     * @param listener the listener
     */
    public void removeListener( TriangulationProcessListener listener )
    {
        _listeners.remove( listener );
    }
    
    /**
     * Clear listeners.
     */
    public void clearListeners()
    {
        _listeners.clear();
    }

    /**
     * Notify all listeners of this new event.
     *
     * @param event the event
     */
    private void sendEvent( TriangulationProcessEvent event )
    {
        for( TriangulationProcessListener l : _listeners )
        {
            l.triangulationEvent( event, _tcx.getTriangulatable() );
        }
    }

    /**
     * Gets the step count.
     *
     * @return the step count
     */
    public int getStepCount()
    {
        return _tcx.getStepCount();
    }

    /**
     * Gets the timestamp.
     *
     * @return the timestamp
     */
    public long getTimestamp()
    {
        return _timestamp;
    }
    
    /**
     * Gets the triangulation time.
     *
     * @return the triangulation time
     */
    public double getTriangulationTime() 
    {
        return _triangulationTime;
    }
    
    /**
     * Uses SweepLine algorithm by default.
     */
    public TriangulationProcess()
    {
        this( TriangulationAlgorithm.DTSweep );
    }

    /**
     * Instantiates a new triangulation process.
     *
     * @param algorithm the algorithm
     */
    public TriangulationProcess( TriangulationAlgorithm algorithm )
    {
        _algorithm = algorithm;
        _tcx = Poly2Tri.createContext( algorithm );
    }
    
    /**
     * This retriangulates same set as previous triangulation
     * useful if you want to do consecutive triangulations with 
     * same data. Like when you when you want to do performance 
     * tests.
     *
     * @param ps the ps
     */
//    public void triangulate()
//    {
//        start();
//    }
    
    /**
     * Triangulate a PointSet with eventual constraints 
     * 
     * @param ps the set of points to triangulate
     */
    public void triangulate( PointSet ps )
    {
        _triangulations.clear();
        _triangulations.add( ps );        
        start();
    }

    /**
     * Triangulate a PointSet with eventual constraints .
     *
     * @param cps the cps
     */
    public void triangulate( ConstrainedPointSet cps )
    {
        _triangulations.clear();
        _triangulations.add( cps );        
        start();
    }
    
    /**
     * Triangulate a PolygonSet.
     *
     * @param ps the ps
     */
    public void triangulate( PolygonSet ps )
    {
        _triangulations.clear();
        _triangulations.addAll( ps.getPolygons() );
        start();
    }

    /**
     * Triangulate a Polygon.
     *
     * @param polygon the polygon
     */
    public void triangulate( Polygon polygon )
    {
        _triangulations.clear();
        _triangulations.add( polygon );
        start();
    }

    /**
     * Triangulate a List of Triangulatables.
     *
     * @param list the list
     */
    public void triangulate( List<Triangulatable> list )
    {
        _triangulations.clear();
        _triangulations.addAll( list );
        start();
    }

    /**
     * Start.
     */
    private void start()
    {
        if( _thread == null || _thread.getState() == State.TERMINATED )
        {
            _isTerminated = false;            
            _thread = new Thread( this, _algorithm.name() + "." + _tcx.getTriangulationMode() );
            _thread.start();
            sendEvent( TriangulationProcessEvent.Started );
        }
        else
        {
            // Triangulation already running. Terminate it so we can start a new
            shutdown();
            _restart = true;
        }
    }

    /**
     * Checks if is waiting.
     *
     * @return true, if is waiting
     */
    public boolean isWaiting()
    {
        if( _thread != null && _thread.getState() == State.WAITING )
        {
            return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
        _pointCount=0;
        try
        {
            long time = System.nanoTime();
            for( Triangulatable t : _triangulations )
            {
                _tcx.clear();
                _tcx.prepareTriangulation( t );
                _pointCount += _tcx._points.size();
                Poly2Tri.triangulate( _tcx );
            }
            _triangulationTime = ( System.nanoTime() - time ) / 1e6;
            //logger.info( "Triangulation of {} points [{}ms]", _pointCount, _triangulationTime );
            sendEvent( TriangulationProcessEvent.Done );
        }
        catch( RuntimeException e )
        {
            if( _awaitingTermination )
            {
                _awaitingTermination = false;
                //logger.info( "Thread[{}] : {}", _thread.getName(), e.getMessage() );
                sendEvent( TriangulationProcessEvent.Aborted );
            }
            else
            {
                e.printStackTrace();
                sendEvent( TriangulationProcessEvent.Failed );
            }
        }
        catch( Exception e )
        {
            e.printStackTrace();
            //logger.info( "Triangulation exception {}", e.getMessage() );
            sendEvent( TriangulationProcessEvent.Failed );
        }
        finally
        {
            _timestamp = System.currentTimeMillis();
            _isTerminated = true;
            _thread = null;
        }
        
        // Autostart a new triangulation?
        if( _restart )
        {
            _restart = false;
            start();
        }
    }

    /**
     * Resume.
     */
    public void resume()
    {
        if( _thread != null )
        {
            // Only force a resume when process is waiting for a notification
            if( _thread.getState() == State.WAITING )
            {
                synchronized( _tcx )
                {
                    _tcx.notify();
                }
            }
            else if( _thread.getState() == State.TIMED_WAITING )
            {
                _tcx.waitUntilNotified( false );
            }
        }
    }

    /**
     * Shutdown.
     */
    public void shutdown()
    {
        _awaitingTermination = true;
        _tcx.terminateTriangulation();
        resume();
    }

    /**
     * Gets the context.
     *
     * @return the context
     */
    public TriangulationContext<?> getContext()
    {
        return _tcx;
    }

    /**
     * Checks if is done.
     *
     * @return true, if is done
     */
    public boolean isDone()
    {
        return _isTerminated;
    }

    /**
     * Request read.
     */
    public void requestRead()
    {
        _tcx.waitUntilNotified( true );
    }

    /**
     * Checks if is readable.
     *
     * @return true, if is readable
     */
    public boolean isReadable()
    {
        if( _thread == null )
        {
            return true;
        }
        else
        {
            synchronized( _thread )
            {
                if( _thread.getState() == State.WAITING )
                {
                    return true;
                }
                else if( _thread.getState() == State.TIMED_WAITING )
                {
                    // Make sure that it stays readable
                    _tcx.waitUntilNotified( true );
                    return true;
                }
                return false;
            }
        }
    }

    /**
     * Gets the point count.
     *
     * @return the point count
     */
    public int getPointCount()
    {
        return _pointCount;
    }
}
