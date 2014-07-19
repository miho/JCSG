/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.mihosoft.vrl.v3d.samples;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Extrude;
import eu.mihosoft.vrl.v3d.FileUtil;
import eu.mihosoft.vrl.v3d.Transform;
import eu.mihosoft.vrl.v3d.Vector3d;
import java.io.IOException;
import java.nio.file.Paths;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class SurfacePro2PenHolder {
    public CSG toCSG() {
        CSG sdCard = new MicroSDCard().toCSG();
        
        double width = sdCard.getBounds().getBounds().x;
        double height = sdCard.getBounds().getBounds().z;
        
        double extensionSize = 11;
        
        CSG extension = Extrude.points(new Vector3d(0,0,height*2), 
                new Vector3d(0,0),
                new Vector3d(width,0),
                new Vector3d(width,-extensionSize),
                new Vector3d(0,-extensionSize)
        ).transformed(Transform.unity().translateZ(-height));
        
        double extensionHeight = 10;
        double extensionThickness = 0.8;
        
        CSG extension2 = Extrude.points(new Vector3d(0,0,extensionHeight), 
                new Vector3d(0,-extensionSize),
                new Vector3d(width,-extensionSize),
                new Vector3d(width,-extensionSize - extensionThickness),
                new Vector3d(0,-extensionSize - extensionThickness)
        ).transformed(Transform.unity().translateZ(-extensionHeight+height));
        
        return sdCard.union(extension.union(extension2));
    }
    
     public static void main(String[] args) throws IOException {

        FileUtil.write(Paths.get("surfac2penholder.stl"), new SurfacePro2PenHolder().toCSG().toStlString());

    }
}
