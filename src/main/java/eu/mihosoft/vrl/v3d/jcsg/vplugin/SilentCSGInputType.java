/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d.jcsg.vplugin;

import eu.mihosoft.vrl.annotation.TypeInfo;
import eu.mihosoft.vrl.reflection.TypeTemplate;
import eu.mihosoft.vrl.v3d.jcsg.CSG;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
@TypeInfo(type=CSG.class, input = true, output = false, style="default")
public class SilentCSGInputType extends TypeTemplate{

    public SilentCSGInputType() {
        setValueName("CSG");
    }
    
}
