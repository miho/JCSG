/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d.jcsg.vplugin;

import eu.mihosoft.vrl.annotation.ComponentInfo;
import eu.mihosoft.vrl.annotation.ParamInfo;
import eu.mihosoft.vrl.v3d.jcsg.CSG;
import eu.mihosoft.vrl.v3d.jcsg.Cylinder;
import java.io.Serializable;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
@ComponentInfo(name="CylinderCreator", category="JCSG")
public class CylinderCreator implements Serializable{
    public CSG create(
            @ParamInfo(name="Radius (Top)", options="value=1") double radiusTop,
            @ParamInfo(name="Radius (Bottom)", options="value=1") double radiusBottom,
            @ParamInfo(name="Height", options="value=1") double height,
            @ParamInfo(name="Resolution", style="slider", options="min=3;max=128;value=16") int resolution) {
        return new Cylinder(radiusTop, radiusTop, height, resolution).toCSG();
    }
}
