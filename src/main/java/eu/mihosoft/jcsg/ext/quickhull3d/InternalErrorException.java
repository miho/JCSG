package eu.mihosoft.jcsg.ext.quickhull3d;

/**
 * Exception thrown when QuickHull3D encounters an internal error.
 */
class InternalErrorException extends RuntimeException
{
	public InternalErrorException (String msg)
	 { super (msg);
	 }
}
