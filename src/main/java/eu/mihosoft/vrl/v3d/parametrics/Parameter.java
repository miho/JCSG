package eu.mihosoft.vrl.v3d.parametrics;

import java.util.ArrayList;

public abstract class Parameter {
	
	private final String name;
	private final ArrayList<Object> options=new ArrayList<Object>();
	private Object value;
	
	public Parameter(String key,Object defaultValue,ArrayList<Object> options){
		this.name = key;
		if(CSGDatabase.get(name)==null)
			this.value = defaultValue;
		else{
			this.value = CSGDatabase.get(name).getValue();
		}
		CSGDatabase.addParameterListener(name, new IParameterChanged() {
			@Override
			public void parameterChanged(String name, Parameter p) {
				value = p.getValue();// if another instance of parameter with this key changes value
			}
		});
//		if(String.class.isInstance(defaultValue)){
//			if(CSGDatabase.get(key)==null)
//				CSGDatabase.set(key, (String)defaultValue);
//		}
//		if(Integer.class.isInstance(defaultValue)){
//			if(CSGDatabase.get(key)==null)
//				setParameterInFixedPointScaledValue((Integer)defaultValue);
//		}
//		if(Double.class.isInstance(defaultValue)){
//			if(CSGDatabase.get(key)==null)
//				setParameterInFlotingPointEngineeringUnits((Double)defaultValue);
//		}
//		
	}

	public String getName() {
		return name;
	}
	
	public void setValue(Object newVal){
		value=newVal;
		ArrayList<IParameterChanged> listeners = CSGDatabase.getParamListeners(name);
		for(IParameterChanged l:listeners){
			l.parameterChanged(name, this);
		}
	}
	
	public Object getValue() {
		return value;
	}
	public ArrayList<Object> getOptions() {
		return options;
	}
	

}
