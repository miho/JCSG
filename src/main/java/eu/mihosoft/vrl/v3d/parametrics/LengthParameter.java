package eu.mihosoft.vrl.v3d.parametrics;

import java.util.ArrayList;

public class LengthParameter extends Parameter {

	public LengthParameter(String key, Double defaultValue, ArrayList<Double> options) {
		 ArrayList<String> opts=new ArrayList<String>();
		 for(Object d:options)
			 opts.add(d.toString());
		setup(key, new Long((long) (defaultValue*1000.0)), opts);
	}


	
}
