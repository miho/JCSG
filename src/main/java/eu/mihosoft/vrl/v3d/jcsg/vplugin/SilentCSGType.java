/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d.jcsg.vplugin;

import eu.mihosoft.vrl.annotation.TypeInfo;
import eu.mihosoft.vrl.reflection.TypeTemplate;
import javax.media.j3d.Shape3D;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
@TypeInfo(type=Shape3D.class, input = true, output = true, style="silent")
public class SilentCSGType extends TypeTemplate{

    public SilentCSGType() {
        setValueName("CSG");
    }
    
}
