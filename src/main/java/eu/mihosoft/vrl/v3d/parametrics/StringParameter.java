package eu.mihosoft.vrl.v3d.parametrics;

import java.util.ArrayList;
import java.util.List;

public class StringParameter extends Parameter {

	private List<String> options2;
	public StringParameter(String key, String defaultValue, List<String> options) {
		super(key, defaultValue, new ArrayList<Object>(options));
		options2 = options;
	}
	public void setString(String s){
		setValue(s);
	}
	public String getString(){
		return (String)getValue();
	}
	public List<String> getStringOptions(){
		return new ArrayList<String>(options2);
	}

}
