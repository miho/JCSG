/**
 * ConstrainedPointSet.java
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
import java.util.Iterator;
import java.util.List;



// TODO: Auto-generated Javadoc
/**
 * Exteet by adding some Constraints on how it will be triangulated<br>
 * A constraint defines an edge between two points in the set, these edges can not
 * be crossed. They will be enforced triangle edges after a triangulation.
 *  
 * 
 * 
 * @author Thomas ???, thahlen@gmail.com
 */
class ConstrainedPointSet extends PointSet
{
    
    /** The _index. */
    int[] _index;
    
    /** The _constrained point list. */
    List<TriangulationPoint> _constrainedPointList = null;

    /**
     * Instantiates a new constrained point set.
     *
     * @param points the points
     * @param index the index
     */
    public ConstrainedPointSet( List<TriangulationPoint> points, int[] index )
    {
        super( points );
        _index = index;  
    }

    /**
     * Instantiates a new constrained point set.
     *
     * @param points - A list of all points in PointSet
     * @param constraints - Pairs of two points defining a constraint, all points  must  be part of given PointSet!
     */
    public ConstrainedPointSet( List<TriangulationPoint> points, List<TriangulationPoint> constraints )
    {
        super( points );
        _constrainedPointList = new ArrayList<TriangulationPoint>();
        _constrainedPointList.addAll(constraints);  
    }

    /* (non-Javadoc)
     * @see eu.mihosoft.vrl.v3d.ext.org.poly2tri.PointSet#getTriangulationMode()
     */
    @Override
    public TriangulationMode getTriangulationMode()
    {
        return TriangulationMode.CONSTRAINED;
    }

    /**
     * Gets the edge index.
     *
     * @return the edge index
     */
    public int[] getEdgeIndex()
    {
        return _index;
    }

    /* (non-Javadoc)
     * @see eu.mihosoft.vrl.v3d.ext.org.poly2tri.PointSet#prepareTriangulation(eu.mihosoft.vrl.v3d.ext.org.poly2tri.TriangulationContext)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void prepareTriangulation( TriangulationContext tcx )
    {
        super.prepareTriangulation( tcx );
        if( _constrainedPointList != null )
        {
        	TriangulationPoint p1,p2;
        	Iterator iterator = _constrainedPointList.iterator();
    		while(iterator.hasNext())
    		{
    			p1 = (TriangulationPoint)iterator.next();
    			p2 = (TriangulationPoint)iterator.next();
    			tcx.newConstraint(p1,p2);
    		}
        }
        else
        {
	        for( int i = 0; i < _index.length; i+=2 )
	        {
	            // XXX: must change!!
	            tcx.newConstraint( _points.get( _index[i] ), _points.get( _index[i+1] ) );
	        }
        }
    }

    /**
     * TODO: TO BE IMPLEMENTED!
     * Peforms a validation on given input<br>
     * 1. Check's if there any constraint edges are crossing or collinear<br>
     * 2. 
     *
     * @return true, if is valid
     */
    public boolean isValid()
    {
        return true;
    }
}
