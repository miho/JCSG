/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class Möbiusband {

    public CSG toCSG() {

        double width = 10;
        double height = 20;

//        CSG result = null;

        List<Vector3d> points = Arrays.asList(
                new Vector3d(-width/2, -height/2),
                new Vector3d(width/2, -height/2),
                new Vector3d(width/2, height/2),
                new Vector3d(-width/2, height/2));
        
        List<CSG> facets = new ArrayList<>();

        for (int i = 0; i < 10; i++) {

            Transform t = Transform.unity().translateZ(1).rotZ(i);

            CSG facet = Extrude.points(new Vector3d(0, 0, 1), points);
            
            facets.add(facet);
            
            points.stream().forEach((p) -> t.transform(p));
        }
        
        CSG result = facets.get(0);
        
        for(int i = 1; i < 10; i++) {
            System.out.println("facet: " + i);
            
            System.out.println(facets.get(i).toStlString());

            Node n = new Node(facets.get(i).getPolygons());
            
            //if (true)return result;

            result = result.optimization(CSG.OptType.POLYGON_BOUND).union(facets.get(i).optimization(CSG.OptType.POLYGON_BOUND));
            try {
                FileUtil.write(Paths.get("möbiusband"+i+".stl"), facets.get(i).toStlString());
            } catch (IOException ex) {
                Logger.getLogger(Möbiusband.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return Hull.fromCSG(result);
    }

    public static void main(String[] args) throws IOException {
        
        System.out.println("RUNNING");

        FileUtil.write(Paths.get("möbiusband.stl"), new Möbiusband().toCSG().toStlString());

    }
}
