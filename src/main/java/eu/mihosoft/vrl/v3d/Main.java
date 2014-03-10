/* 
 * Main.java
 *
 * Copyright (c) 2009–2014 Steinbeis Forschungszentrum (STZ Ölbronn),
 * Copyright (c) 2006–2014 by Michael Hoffer
 * 
 * This file is part of Visual Reflection Library (VRL).
 *
 * VRL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3
 * as published by the Free Software Foundation.
 * 
 * see: http://opensource.org/licenses/LGPL-3.0
 *      file://path/to/VRL/src/eu/mihosoft/vrl/resources/license/lgplv3.txt
 *
 * VRL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * This version of VRL includes copyright notice and attribution requirements.
 * According to the LGPL this information must be displayed even if you modify
 * the source code of VRL. Neither the VRL Canvas attribution icon nor any
 * copyright statement/attribution may be removed.
 *
 * Attribution Requirements:
 *
 * If you create derived work you must do three things regarding copyright
 * notice and author attribution.
 *
 * First, the following text must be displayed on the Canvas or an equivalent location:
 * "based on VRL source code".
 * 
 * Second, the copyright notice must remain. It must be reproduced in any
 * program that uses VRL.
 *
 * Third, add an additional notice, stating that you modified VRL. In addition
 * you must cite the publications listed below. A suitable notice might read
 * "VRL source code modified by YourName 2012".
 * 
 * Note, that these requirements are in full accordance with the LGPL v3
 * (see 7. Additional Terms, b).
 *
 * Publications:
 *
 * M. Hoffer, C.Poliwoda, G.Wittum. Visual Reflection Library -
 * A Framework for Declarative GUI Programming on the Java Platform.
 * Computing and Visualization in Science, in press.
 */
package eu.mihosoft.vrl.v3d;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class Main {

    public static void main(String[] args) {
        
        CSG extruded = Extrude.points(
                new Vector3d(0, 0, 1),
                new Vector3d(0, 0),
                new Vector3d(1, 0),
                new Vector3d(1, 1),
                new Vector3d(0.5, 1),
                new Vector3d(0.5, 0.5),
                new Vector3d(0, 0.5)
        );

        Transform transform = Transform.unity().
                rotZ(25).
                rotY(15).
                rotX(25).
                translate(0, 0, -1).
                scale(0.5, 1.5, 1.5);

        CSG cube = new Cube(2).toCSG();
        CSG sphere = new Sphere(1.25).toCSG();
        CSG union = cube.union(sphere);

//                .difference(
////                        new Cube(new Vector3d(0, 0, 0), new Vector3d(2, 2, 2)).toCSG()
//                        p.extrude(new Vector3d(0, 0, 3)).
//                                transformed(transform)
//                )
//                ;
//                .intersect(
//                        new Cylinder().toCSG().
//                        transformed(
//                                Transform.unity().
//                                translate(new Vector3d(0, 0, 0)).
//                                scale(new Vector3d(1, 3, 1))
//                        )
//                );
        try {
            FileUtil.write(
                    Paths.get("obj.stl"),
                    RaspberryPiMount.boardAndPegs().toStlString());
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


}
