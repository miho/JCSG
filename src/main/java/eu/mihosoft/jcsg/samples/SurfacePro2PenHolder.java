/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.mihosoft.jcsg.samples;

import eu.mihosoft.jcsg.CSG;
import eu.mihosoft.jcsg.Extrude;
import eu.mihosoft.jcsg.FileUtil;
import eu.mihosoft.vvecmath.Transform;
import eu.mihosoft.vvecmath.Vector3d;
import java.io.IOException;
import java.nio.file.Paths;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class SurfacePro2PenHolder {
    public CSG toCSG() {
        CSG sdCard = new MicroSDCard().toCSG();
        
        double width = sdCard.getBounds().getBounds().x();
        double height = sdCard.getBounds().getBounds().z();
        
        double extensionSize = 11;
        
        CSG extension = Extrude.points(Vector3d.xyz(0,0,height*2), 
                Vector3d.xy(0,0),
                Vector3d.xy(width,0),
                Vector3d.xy(width,-extensionSize),
                Vector3d.xy(0,-extensionSize)
        ).transformed(Transform.unity().translateZ(-height));
        
        double extensionHeight = 10;
        double extensionThickness = 0.8;
        
        CSG extension2 = Extrude.points(Vector3d.xyz(0,0,extensionHeight), 
                Vector3d.xy(0,-extensionSize),
                Vector3d.xy(width,-extensionSize),
                Vector3d.xy(width,-extensionSize - extensionThickness),
                Vector3d.xy(0,-extensionSize - extensionThickness)
        ).transformed(Transform.unity().translateZ(-extensionHeight+height));
        
        return sdCard.union(extension.union(extension2));
    }
    
     public static void main(String[] args) throws IOException {

        FileUtil.write(Paths.get("surfac2penholder.stl"), new SurfacePro2PenHolder().toCSG().toStlString());

    }
}
