/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.mihosoft.vrl.v3d;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
final class Modifier {
    private final WeightFunction function;

    public Modifier(WeightFunction function) {
       this.function = function;
    }
    
    void modify(CSG csg) {
        for(Polygon p : csg.getPolygons()) {
            for(Vertex v : p.vertices) {
                v.setWeight(function.eval(v.pos, csg));
            }
        }
    }
    
    CSG modified(CSG csg) {
        CSG result = csg.clone();
        modify(result);
        return result;
    }
}
