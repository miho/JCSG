package eu.mihosoft.vrl.v3d.ext.quickhull3d;

// TODO: Auto-generated Javadoc
/**
 * Maintains a double-linked list of vertices for use by QuickHull3D.
 */
class VertexList
{
	
	/** The head. */
	private Vertex head;
	
	/** The tail. */
	private Vertex tail;

	/**
	 * Clears this list.
	 */
	public void clear()
	 {
	   head = tail = null; 
	 }

	/**
	 * Adds a vertex to the end of this list.
	 *
	 * @param vtx the vtx
	 */
	public void add (Vertex vtx)
	 { 
	   if (head == null)
	    { head = vtx;
	    }
	   else
	    { tail.next = vtx; 
	    }
	   vtx.prev = tail;
	   vtx.next = null;
	   tail = vtx;
	 }

	/**
	 * Adds a chain of vertices to the end of this list.
	 *
	 * @param vtx the vtx
	 */
	public void addAll (Vertex vtx)
	 { 
	   if (head == null)
	    { head = vtx;
	    }
	   else
	    { tail.next = vtx; 
	    }
	   vtx.prev = tail;
	   while (vtx.next != null)
	    { vtx = vtx.next;
	    }
	   tail = vtx;
	 }

	/**
	 * Deletes a vertex from this list.
	 *
	 * @param vtx the vtx
	 */
	public void delete (Vertex vtx)
	 {
	   if (vtx.prev == null)
	    { head = vtx.next;
	    }
	   else
	    { vtx.prev.next = vtx.next; 
	    }
	   if (vtx.next == null)
	    { tail = vtx.prev; 
	    }
	   else
	    { vtx.next.prev = vtx.prev; 
	    }
	 }

	/**
	 * Deletes a chain of vertices from this list.
	 *
	 * @param vtx1 the vtx1
	 * @param vtx2 the vtx2
	 */
	public void delete (Vertex vtx1, Vertex vtx2)
	 {
	   if (vtx1.prev == null)
	    { head = vtx2.next;
	    }
	   else
	    { vtx1.prev.next = vtx2.next; 
	    }
	   if (vtx2.next == null)
	    { tail = vtx1.prev; 
	    }
	   else
	    { vtx2.next.prev = vtx1.prev; 
	    }
	 }

	/**
	 * Inserts a vertex into this list before another
	 * specificed vertex.
	 *
	 * @param vtx the vtx
	 * @param next the next
	 */
	public void insertBefore (Vertex vtx, Vertex next)
	 {
	   vtx.prev = next.prev;
	   if (next.prev == null)
	    { head = vtx;
	    }
	   else
	    { next.prev.next = vtx; 
	    }
	   vtx.next = next;
	   next.prev = vtx;
	 }

	/**
	 * Returns the first element in this list.
	 *
	 * @return the vertex
	 */
	public Vertex first()
	 {
	   return head;
	 }

	/**
	 * Returns true if this list is empty.
	 *
	 * @return true, if is empty
	 */
	public boolean isEmpty()
	 {
	   return head == null;
	 }
}
