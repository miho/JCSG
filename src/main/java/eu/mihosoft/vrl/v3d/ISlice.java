package eu.mihosoft.vrl.v3d;

import java.util.List;

public interface ISlice {
	/**
	 * An interface for slicking CSG objects into lists of points that can be extruded back out
	 * @param incoming Incoming CSG to be slices
	 * @param slicePlane the plane to slice, if null asumes the X/Y plane at z=0
	 * @param normalInsetDistance sets the inset to the interior of the shape. 
	 * @return a set of points corosponding to a slice given these parameters. 
	 */
	List<Vector3d> slice(CSG incoming, Plane slicePlane, double normalInsetDistance) ;
}
