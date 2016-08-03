package eu.mihosoft.vrl.v3d.parametrics;

import eu.mihosoft.vrl.v3d.CSG;

public interface IRegenerate {
	/**
	 * This is an interface for regenerating a CSG from the outside
	 * @param previous the predious CSG that was used as the source for this call
	 * @return The new CSG configured and ready for re-rendering
	 */
	public CSG regenerate(CSG previous);
}
