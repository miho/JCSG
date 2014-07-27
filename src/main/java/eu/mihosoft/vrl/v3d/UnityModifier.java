/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d;

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

    @Override
    public double eval(Vector3d pos, CSG csg) {
        return 1.0;
    }

}
