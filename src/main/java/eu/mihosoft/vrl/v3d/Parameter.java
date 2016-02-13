package eu.mihosoft.vrl.v3d;

public abstract class Parameter {
	
	private final String name;
	public Parameter(String name){
		this.name = name;
	}
	public long getParameterInFixedPointScaledValue() {
		return CSGDatabase.getMicrons(name);
	}
	public void setParameterInFixedPointScaledValue(long parameterInFixedPointScaledValue) {
		CSGDatabase.setFixedPointScaledValue(name, parameterInFixedPointScaledValue);
	}
	
	public double getParameterInFlotingPointEngineeringUnits() {
		return ((double)getParameterInFixedPointScaledValue())/1000.0;
	}
	public void setParameterInFlotingPointEngineeringUnits(double FlotingPointEngineeringUnits) {
		setParameterInFixedPointScaledValue((long) (FlotingPointEngineeringUnits*1000.0));
	}
	public String getName() {
		return name;
	}
	

}
