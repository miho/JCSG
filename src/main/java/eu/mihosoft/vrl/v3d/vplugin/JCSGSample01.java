/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d.vplugin;

import eu.mihosoft.vrl.annotation.ComponentInfo;
import eu.mihosoft.vrl.annotation.ParamInfo;
import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.FileUtil;
import eu.mihosoft.vrl.v3d.VGeometry3D;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
@ComponentInfo(name = "JCSGSample01", category = "JCSG")
public class JCSGSample01 implements Serializable {

    private static final long serialVersionUID = 1L;

    public VGeometry3D hull(
            @ParamInfo(style = "save-dialog", options = "endings=[\".stl\"]; description=\".stl File\"") File outputFile) throws IOException {
        CSG result = new eu.mihosoft.vrl.v3d.samples.ServoWheel().toCSG();

        FileUtil.write(outputFile, result.toStlString());
        
        STL2Geometry objReader = new STL2Geometry();
        
        return new VGeometry3D(objReader.loadAsVTriangleArray(outputFile));
    }
}
