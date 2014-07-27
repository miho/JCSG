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
public class XModifier implements WeightFunction {

    private Bounds bounds;
    private double min = 0;
    private double max = 1.0;

    private double sPerUnit;
    private boolean centered;


    /**
     * Constructor.
     */
    public XModifier() {
    }

    /**
     * Constructor.
     * 
     * @param centered defines whether to center origin at the csg location
     */
    public XModifier(boolean centered) {
        this.centered = centered;
    }

    @Override
    public double eval(Vector3d pos, CSG csg) {

        if (bounds == null) {
            this.bounds = csg.getBounds();
            sPerUnit = (max - min) / (bounds.getMax().x - bounds.getMin().x);
        }

        double s = sPerUnit * (pos.x - bounds.getMin().x);

        if (centered) {
            s = s - (max - min)/2.0;
            
            s = Math.abs(s)*2;
        }
        
        return s;
    }

}
