/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d.samples;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.FileUtil;
import eu.mihosoft.vrl.v3d.RoundedCube;
import java.io.IOException;
import java.nio.file.Paths;


/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class RoundedCubeSample {

   
    public CSG toCSG() {

        return new RoundedCube(3).resolution(8).cornerRadius(0.2).toCSG();
    }

    public static void main(String[] args) throws IOException {

        FileUtil.write(Paths.get("rounded-cube.stl"), new RoundedCubeSample().toCSG().toStlString());

        new RoundedCubeSample().toCSG().toObj().toFiles(Paths.get("rounded-cube.obj"));

    }

}
