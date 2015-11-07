/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d;

// TODO: Auto-generated Javadoc
/**
 * Modifies along y axis.
 * 
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class YModifier implements WeightFunction {

    /** The bounds. */
    private Bounds bounds;
    
    /** The min. */
    private double min = 0;
    
    /** The max. */
    private double max = 1.0;

    /** The s per unit. */
    private double sPerUnit;
    
    /** The centered. */
    private boolean centered;

    /**
     * Constructor.
     */
    public YModifier() {
    }

    /**
     * Constructor.
     *
     * @param centered defines whether to center origin at the csg location
     */
    public YModifier(boolean centered) {
        this.centered = centered;
    }

    /* (non-Javadoc)
     * @see eu.mihosoft.vrl.v3d.WeightFunction#eval(eu.mihosoft.vrl.v3d.Vector3d, eu.mihosoft.vrl.v3d.CSG)
     */
    @Override
    public double eval(Vector3d pos, CSG csg) {

        if (bounds == null) {
            this.bounds = csg.getBounds();
            sPerUnit = (max - min) / (bounds.getMax().y - bounds.getMin().y);
        }

        double s = sPerUnit * (pos.y - bounds.getMin().y);

        if (centered) {
            s = s - (max - min) / 2.0;

            s = Math.abs(s) * 2;
        }

        return s;
    }

}
