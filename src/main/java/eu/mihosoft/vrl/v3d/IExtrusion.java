package eu.mihosoft.vrl.v3d;

import java.util.List;

public interface IExtrusion {
	/**
	 * Extrusion interface for writing extrusion strategy scripts
	 * @param dir
	 * @param points
	 * @return
	 */
	CSG extrude(Vector3d dir, List<Vector3d> points) ;
}
