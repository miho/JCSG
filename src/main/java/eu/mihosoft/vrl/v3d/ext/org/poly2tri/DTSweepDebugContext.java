/**
 * DTSweepDebugContext.java
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

class DTSweepDebugContext extends TriangulationDebugContext
{
    /*
     * Fields used for visual representation of current triangulation
     */
    protected DelaunayTriangle _primaryTriangle;
    protected DelaunayTriangle _secondaryTriangle;
    protected TriangulationPoint _activePoint;
    protected AdvancingFrontNode _activeNode;
    protected DTSweepConstraint _activeConstraint;   
        
    public DTSweepDebugContext( DTSweepContext tcx )
    {
        super( tcx );
    }
    
    public boolean isDebugContext()
    {
        return true;
    }

    //  private Tuple2<TPoint,Double> m_circumCircle = new Tuple2<TPoint,Double>( new TPoint(), new Double(0) );
//  public Tuple2<TPoint,Double> getCircumCircle() { return m_circumCircle; }
    public DelaunayTriangle getPrimaryTriangle()
    {
        return _primaryTriangle;
    }

    public DelaunayTriangle getSecondaryTriangle()
    {
        return _secondaryTriangle;
    }
    
    public AdvancingFrontNode getActiveNode()
    {
        return _activeNode;
    }

    public DTSweepConstraint getActiveConstraint()
    {
        return _activeConstraint;
    }

    public TriangulationPoint getActivePoint()
    {
        return _activePoint;
    }

    public void setPrimaryTriangle( DelaunayTriangle triangle )
    {
        _primaryTriangle = triangle;        
        _tcx.update("setPrimaryTriangle");
    }

    public void setSecondaryTriangle( DelaunayTriangle triangle )
    {
        _secondaryTriangle = triangle;        
        _tcx.update("setSecondaryTriangle");
    }
    
    public void setActivePoint( TriangulationPoint point )
    {
        _activePoint = point;        
    }

    public void setActiveConstraint( DTSweepConstraint e )
    {
        _activeConstraint = e;
        _tcx.update("setWorkingSegment");
    }

    public void setActiveNode( AdvancingFrontNode node )
    {
        _activeNode = node;        
        _tcx.update("setWorkingNode");
    }

    @Override
    public void clear()
    {
        _primaryTriangle = null;
        _secondaryTriangle = null;
        _activePoint = null;
        _activeNode = null;
        _activeConstraint = null;   
    }
        
//  public void setWorkingCircumCircle( TPoint point, TPoint point2, TPoint point3 )
//  {
//          double dx,dy;
//          
//          CircleXY.circumCenter( point, point2, point3, m_circumCircle.a );
//          dx = m_circumCircle.a.getX()-point.getX();
//          dy = m_circumCircle.a.getY()-point.getY();
//          m_circumCircle.b = Double.valueOf( Math.sqrt( dx*dx + dy*dy ) );
//          
//  }
}
