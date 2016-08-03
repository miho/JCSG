package eu.mihosoft.vrl.v3d;

public interface PrepForManufacturing {
	/**
	 * THis interface is for objects that will 
	 * convert the modeling orientation of a part 
	 * into whatever CSG orientation and added 
	 * support may be need for manufacturing. 
	 * @param incoming the Incoming CSG
	 * @return the transformed CSG
	 */
	public CSG prep(CSG incoming);
}
