package eu.mihosoft.vrl.v3d.parametrics;

import java.util.HashMap;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.ItoCSG;

public class CSGCache {
	private HashMap<String , CSG> chache = new HashMap<>();
	
	CSG get(String key, ItoCSG builder){
		if(chache.get(key) == null){
			chache.put(key, builder.toCSG());
		}
		return chache.get(key);
	}
	

}
