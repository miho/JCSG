/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d.jcsg.vplugin;

import eu.mihosoft.vrl.annotation.ComponentInfo;
import eu.mihosoft.vrl.annotation.ParamInfo;
import eu.mihosoft.vrl.v3d.jcsg.CSG;
import eu.mihosoft.vrl.v3d.jcsg.Cube;
import java.io.Serializable;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
@ComponentInfo(name="CubeCreator", category="JCSG")
public class CubeCreator implements Serializable{
    public CSG create(
            @ParamInfo(name="W", options="value=1") double w,
            @ParamInfo(name="H", options="value=1") double h,
            @ParamInfo(name="D", options="value=1") double d) {
        return new Cube(w, h, d).toCSG();
    }
}
