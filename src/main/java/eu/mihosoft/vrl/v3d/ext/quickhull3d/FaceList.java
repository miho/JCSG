package eu.mihosoft.vrl.v3d.ext.quickhull3d;

// TODO: Auto-generated Javadoc
/**
 * Maintains a single-linked list of faces for use by QuickHull3D.
 */
class FaceList
{
	
	/** The head. */
	private Face head;
	
	/** The tail. */
	private Face tail;

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
	public void add (Face vtx)
	 { 
	   if (head == null)
	    { head = vtx;
	    }
	   else
	    { tail.next = vtx; 
	    }
	   vtx.next = null;
	   tail = vtx;
	 }

	/**
	 * First.
	 *
	 * @return the face
	 */
	public Face first()
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
