/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.mihosoft.vrl.v3d;

// TODO: Auto-generated Javadoc
/**
 * The Class Modifier.
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
final class Modifier {
    
    /** The function. */
    private final WeightFunction function;

    /**
     * Instantiates a new modifier.
     *
     * @param function the function
     */
    public Modifier(WeightFunction function) {
       this.function = function;
    }
    
    /**
     * Modify.
     *
     * @param csg the csg
     */
    void modify(CSG csg) {
        for(Polygon p : csg.getPolygons()) {
            for(Vertex v : p.vertices) {
                v.setWeight(function.eval(v.pos, csg));
            }
        }
    }
    
    /**
     * Modified.
     *
     * @param csg the csg
     * @return the csg
     */
    CSG modified(CSG csg) {
        CSG result = csg.clone();
        modify(result);
        return result;
    }
}
