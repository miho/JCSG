package eu.mihosoft.vrl.v3d.parametrics;

import eu.mihosoft.vrl.v3d.CSG;

public interface IParametric {
	/**
	 * This is an interface for setting a parameter inside a CSG generation script. 
	 * NOTE this should run in less than 10ms to not cause hangs. This function should not regenerate cad if possible but just set values
	 * Use IRegenerate to perform cad regeneration.
	 * @param oldCSG The Old CSG that this function was called from
	 * @param parameterKey The String used as a HashMap key to this parameter
	 * @param newValue the new value of the parameter
	 * @return the CSG to be displayed, returning the same reference prevents excess render load
	 */
	public CSG change(CSG oldCSG,String parameterKey, Long newValue);
}
