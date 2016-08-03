/**
 * AdvancingFrontNode.java
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
import eu.mihosoft.vrl.v3d.ext.org.poly2tri.TriangulationPoint;
import eu.mihosoft.vrl.v3d.ext.org.poly2tri.DelaunayTriangle;



// TODO: Auto-generated Javadoc
/**
 * The Class AdvancingFrontNode.
 */
class AdvancingFrontNode
{
    
    /** The next. */
    protected AdvancingFrontNode next = null;
    
    /** The prev. */
    protected AdvancingFrontNode prev = null;

    /** The key. */
    protected final Double key; // XXX: BST
    
    /** The value. */
    protected final double value;
    
    /** The point. */
    protected final TriangulationPoint point;
    
    /** The triangle. */
    protected DelaunayTriangle triangle;
    
    /**
     * Instantiates a new advancing front node.
     *
     * @param point the point
     */
    public AdvancingFrontNode( TriangulationPoint point )
    {
        this.point = point;
        value = point.getX();
        key = Double.valueOf( value ); // XXX: BST
    }    

    /**
     * Gets the next.
     *
     * @return the next
     */
    public AdvancingFrontNode getNext()
    {
        return next;
    }

    /**
     * Gets the previous.
     *
     * @return the previous
     */
    public AdvancingFrontNode getPrevious()
    {
        return prev;
    }

    /**
     * Gets the point.
     *
     * @return the point
     */
    public TriangulationPoint getPoint()
    {
        return point;
    }
    
    /**
     * Gets the triangle.
     *
     * @return the triangle
     */
    public DelaunayTriangle getTriangle()
    {
        return triangle;
    }

    /**
     * Checks for next.
     *
     * @return true, if successful
     */
    public boolean hasNext()
    {
        return next != null;
    }

    /**
     * Checks for previous.
     *
     * @return true, if successful
     */
    public boolean hasPrevious()
    {
        return prev != null;
    }    
}
