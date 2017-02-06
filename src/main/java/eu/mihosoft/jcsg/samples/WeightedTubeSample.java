/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg.samples;

import eu.mihosoft.jcsg.CSG;
import eu.mihosoft.jcsg.Cylinder;
import eu.mihosoft.jcsg.FileUtil;
import eu.mihosoft.vvecmath.Transform;
import eu.mihosoft.jcsg.UnityModifier;
import eu.mihosoft.jcsg.WeightFunction;

import java.io.IOException;
import java.nio.file.Paths;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class WeightedTubeSample {

    public CSG toCSG() {
        
        

        WeightFunction weight = (v, csg) -> {
            double w = Math.max(1,((0.1)+Math.random())/(v.z()*0.1+0.1));

            return w;

        };
        
//        CSG.setDefaultOptType(CSG.OptType.POLYGON_BOUND);

        CSG protoOuter = new Cylinder(1, 1, 16).toCSG();
        CSG protoInner = new Cylinder(0.5, 1, 16).toCSG();

        CSG outer = protoOuter;
        CSG inner = protoInner;

        for (int i = 0; i < 50; i++) {
            outer = outer.union(protoOuter.transformed(Transform.unity().translateZ(i / 5.0)));
            inner = inner.union(protoInner.transformed(Transform.unity().translateZ(i / 5.0)));
        }
        
        Transform scale = Transform.unity().scale(2, 2, 1);
        Transform scaleInner = Transform.unity().scale(1.5, 1.5, 1);

        inner = inner.weighted(weight).transformed(scaleInner).weighted(new UnityModifier());

        return outer.weighted(weight).
                transformed(scale).difference(inner);
    }

    public static void main(String[] args) throws IOException {

        FileUtil.write(Paths.get("weighted-tube.stl"), new WeightedTubeSample().toCSG().toStlString());

//        new WeightedTubeSample().toCSG().toObj().toFiles(Paths.get("weighted-tube.obj"));

    }

}
