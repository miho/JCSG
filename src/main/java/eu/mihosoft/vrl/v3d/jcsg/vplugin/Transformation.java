/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d.jcsg.vplugin;

import eu.mihosoft.vrl.annotation.ComponentInfo;
import eu.mihosoft.vrl.annotation.ParamInfo;
import eu.mihosoft.vrl.v3d.jcsg.CSG;
import eu.mihosoft.vrl.v3d.jcsg.Transform;
import java.io.Serializable;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
@ComponentInfo(name = "Transform", category = "JCSG")
public class Transformation implements Serializable {

    public CSG translateX(CSG csg1, @ParamInfo(name = "X") double x) {
        return csg1.transformed(Transform.unity().translateX(x));
    }

    public CSG translateY(CSG csg1, @ParamInfo(name = "Y") double y) {
        return csg1.transformed(Transform.unity().translateY(y));
    }

    public CSG translateZ(CSG csg1, @ParamInfo(name = "Z") double z) {
        return csg1.transformed(Transform.unity().translateZ(z));
    }

    public CSG rotX(CSG csg1, @ParamInfo(name = "X") double x) {
        return csg1.transformed(Transform.unity().rotX(x));
    }

    public CSG rotY(CSG csg1, @ParamInfo(name = "Y") double y) {
        return csg1.transformed(Transform.unity().rotY(y));
    }

    public CSG rotZ(CSG csg1, @ParamInfo(name = "Z") double z) {
        return csg1.transformed(Transform.unity().rotZ(z));
    }

    public CSG scaleX(CSG csg1, @ParamInfo(name = "X") double x) {
        return csg1.transformed(Transform.unity().scaleX(x));
    }

    public CSG scaleY(CSG csg1, @ParamInfo(name = "Y") double y) {
        return csg1.transformed(Transform.unity().scaleY(y));
    }

    public CSG scaleZ(CSG csg1, @ParamInfo(name = "Z") double z) {
        return csg1.transformed(Transform.unity().scaleZ(z));
    }

    public CSG scale(CSG csg1, @ParamInfo(name = "Scale") double s) {
        return csg1.transformed(Transform.unity().scale(s));
    }
}
