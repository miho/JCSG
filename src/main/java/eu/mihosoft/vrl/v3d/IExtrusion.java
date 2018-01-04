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
    /**
     * Extrude.
     *
     * @param dir
     *            the dir
     * @param polygon1
     *            the polygon1
     * @return the csg
     */
	CSG extrude(Vector3d dir, Polygon polygon1);
}
