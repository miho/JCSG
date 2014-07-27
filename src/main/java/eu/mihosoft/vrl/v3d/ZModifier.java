/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d;

/**
 * Modifies along z axis.
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class ZModifier implements WeightFunction {

    private Bounds bounds;
    private double min = 0;
    private double max = 1.0;

    private double sPerUnit;
    private boolean centered;

    /**
     * Constructor.
     */
    public ZModifier() {
    }

    /**
     * Constructor.
     *
     * @param centered defines whether to center origin at the csg location
     */
    public ZModifier(boolean centered) {
        this.centered = centered;
    }

    @Override
    public double eval(Vector3d pos, CSG csg) {

        if (bounds == null) {
            this.bounds = csg.getBounds();
            sPerUnit = (max - min) / (bounds.getMax().z - bounds.getMin().z);
        }

        double s = sPerUnit * (pos.z - bounds.getMin().z);

        if (centered) {
            s = s - (max - min) / 2.0;

            s = Math.abs(s) * 2;
        }

        return s;
    }

}
