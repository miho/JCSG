/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d;

// TODO: Auto-generated Javadoc
/**
 * Modifies along x axis.
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class UnityModifier implements WeightFunction {

    /**
     * Constructor.
     */
    public UnityModifier() {
    }

    /* (non-Javadoc)
     * @see eu.mihosoft.vrl.v3d.WeightFunction#eval(eu.mihosoft.vrl.v3d.Vector3d, eu.mihosoft.vrl.v3d.CSG)
     */
    @Override
    public double eval(Vector3d pos, CSG csg) {
        return 1.0;
    }

}
