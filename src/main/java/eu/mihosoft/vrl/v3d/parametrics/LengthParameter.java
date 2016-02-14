package eu.mihosoft.vrl.v3d.parametrics;

import java.util.ArrayList;

public class LengthParameter extends Parameter {

	public LengthParameter(String key, Double defaultValue, ArrayList<Double> options) {
		super(key, new Long((long) (defaultValue*1000.0)), new ArrayList<Object>(options));
	}
	
	public void setMM(double newVal){
		setValue(new Long((long)(newVal*1000.0)));
	}
	public void setMicrons(long newVal){
		setValue(new Long(newVal));
	}
	
	public double getMM(){
		return ((double)((Long)getValue()))/1000.0;
	}
	public double getMicrons(){
		return (Long)getValue();
	}
	
}
