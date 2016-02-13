package eu.mihosoft.vrl.v3d;

public interface IParameterChanged {
	/**
	 * This is a listener for a parameter changing
	 * @param name
	 * @param p
	 */
	public void parameterChanged(String name, Parameter p);
}
