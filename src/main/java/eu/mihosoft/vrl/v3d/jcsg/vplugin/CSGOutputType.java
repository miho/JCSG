/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d.jcsg.vplugin;

import eu.mihosoft.vrl.annotation.TypeInfo;
import eu.mihosoft.vrl.types.VGeometry3DType;
import eu.mihosoft.vrl.v3d.VGeometry3D;
import eu.mihosoft.vrl.v3d.jcsg.CSG;
import java.awt.Color;


/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
@TypeInfo(input = false, output = true,style = "default", type = CSG.class)
public class CSGOutputType extends VGeometry3DType{
    private CSG viewValue;
    
    @Override
    public void setViewValue(Object o) {
        if (o instanceof CSG) {
            CSG csg = (CSG) o;
            super.setViewValue(new VGeometry3D(
                    csg.toVTriangleArray(), Color.RED, null, 1.0f, false));
        }
    }

    @Override
    public void emptyView() {
        viewValue=null;
        super.emptyView();
    }
    
    
    
    @Override
    public Object getViewValue() {
        return viewValue;
    }
}
