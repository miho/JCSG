package eu.mihosoft.vrl.v3d;

import java.util.List;

public interface ISlice {
	/**
	 * An interface for slicking CSG objects into lists of points that can be extruded back out
	 * @param incoming			  Incoming CSG to be sliced
	 * @param slicePlane		  Z coordinate of incoming CSG to slice at
	 * @param normalInsetDistance Inset for sliced output
	 * @return					  A set of points defining the sliced shape
	 */
	List<Vector3d> slice(CSG incoming, double slicePlane, double normalInsetDistance);
}
