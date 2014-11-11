/* 
 * VGeometry3DType.java
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009–2012 Steinbeis Forschungszentrum (STZ Ölbronn),
 * Copyright (c) 2006–2012 by Michael Hoffer
 * 
 * This file is part of Visual Reflection Library (VRL).
 *
 * VRL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3
 * as published by the Free Software Foundation.
 * 
 * see: http://opensource.org/licenses/LGPL-3.0
 *      file://path/to/VRL/src/eu/mihosoft/vrl/resources/license/lgplv3.txt
 *
 * VRL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * This version of VRL includes copyright notice and attribution requirements.
 * According to the LGPL this information must be displayed even if you modify
 * the source code of VRL. Neither the VRL Canvas attribution icon nor any
 * copyright statement/attribution may be removed.
 *
 * Attribution Requirements:
 *
 * If you create derived work you must do three things regarding copyright
 * notice and author attribution.
 *
 * First, the following text must be displayed on the Canvas:
 * "based on VRL source code". In this case the VRL canvas icon must be removed.
 * 
 * Second, the copyright notice must remain. It must be reproduced in any
 * program that uses VRL.
 *
 * Third, add an additional notice, stating that you modified VRL. In addition
 * you must cite the publications listed below. A suitable notice might read
 * "VRL source code modified by YourName 2012".
 * 
 * Note, that these requirements are in full accordance with the LGPL v3
 * (see 7. Additional Terms, b).
 *
 * Publications:
 *
 * M. Hoffer, C.Poliwoda, G.Wittum. Visual Reflection Library -
 * A Framework for Declarative GUI Programming on the Java Platform.
 * Computing and Visualization in Science, 2011, in press.
 */

package eu.mihosoft.vrl.v3d.jcsg.vplugin;

import eu.mihosoft.vrl.annotation.TypeInfo;
import eu.mihosoft.vrl.reflection.ComponentUtil;
import eu.mihosoft.vrl.reflection.RepresentationType;
import eu.mihosoft.vrl.types.Shape3DArrayType;
import eu.mihosoft.vrl.v3d.VGeometry3D;
import eu.mihosoft.vrl.v3d.VTriangleArray;

/**
 * TypeRepresentation for {@link eu.mihosoft.vrl.v3d.VGeometry3D}.
 * 
 * <p>
 * This type representation can be used to easily visualize 3D geometries.
 * </p>
 * <p><b>Note:</b> the memory footprint of VGeometry3D based geometries is
 * significantly higher than using Shape3D. Therefore, do not use it for
 * highly complex geometries (#Triangles > 10^5)</p>
 * 
 * <p><b>Example (Groovy code):</b></p>
 * <code>
 * <pre>
 * &#64;ComponentInfo(name="3D Geometry Sample")
 * class GeometrySample implements Serializable {
 *     private static final long serialVersionUID=1
 *
 *     &#64;MethodInfo(callOptions="autoinvoke")
 *     public VGeometry3D getGeometry() {
 *         VTriangleArray result = new VTriangleArray()
 *
 *         Node n1 = new Node(new Point3f(0f,0f,0f))
 *         Node n2 = new Node(new Point3f(10f,0f,0f))
 *         Node n3 = new Node(new Point3f(0f,10f,0f))
 *         Node n4 = new Node(new Point3f(0f,0f,10f))
 *
 *         result.addTriangle(new Triangle(1,n1,n2,n3))
 *         result.addTriangle(new Triangle(2,n1,n2,n4))
 *         result.addTriangle(new Triangle(3,n1,n3,n4))
 *         result.addTriangle(new Triangle(4,n2,n3,n4))
 *
 *         return new VGeometry3D(result,Color.black,Color.green,1F,false)
 *   }
 * }
 * </pre>
 * </code>
 * </p>
 * 
 * <p>The visualization should look like this:</p>
 * <br/>
 * <img src="doc-files/vgeometry3dtype01.png"/>
 * <br/>
 * 
 *
 * @see eu.mihosoft.vrl.v3d.VTriangleArray
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
@TypeInfo(type=VGeometry3D.class, input = true, output = true, style="default")
public class VGeometry3DType extends Shape3DArrayType {

    private VGeometry3D geometryValue;

    public VGeometry3DType() {
        setValueName("");
        setHideConnector(false);
    }

    @Override
    public void setViewValue(Object o) {

        if (o instanceof VGeometry3D) {
            VGeometry3D geometry = (VGeometry3D) o;
            geometryValue = geometry;
            setOrientationFromValues(geometry.getOrientation());
            super.setViewValue(geometry.generateShape3DArray());
        } else {
            super.setViewValue(null);
        }
    }

    @Override
    public void emptyView() {
        geometryValue = null;
        super.emptyView();
        
    }

    @Override
    public Object getViewValue() {
        if (geometryValue != null && isInput()) {
            geometryValue.setOrientation(getOrientationFromUniverse());
        }
        return geometryValue;
    }

    
    
    @Override
    public boolean preferBinarySerialization() {
        return (geometryValue == null)
                ? super.preferBinarySerialization()
                : geometryValue.getGeometry().size() > 500;
    }
    
    /**
     * Indicates whether to prevent value serialization.
     * Sometimes the value may be too big or unimportant to be saved.
     * This property can be controlled via the variable 
     * <code>serialization</code> in the options string of the param info.
     * @return <code>true</code> if the value shall not be serialized;
     *         <code>false</code> otherwise
     */
    @Override
    public boolean noSerialization() {
        
        // TODO join implementations. 
        // current code taken from TypeRepresentationBase
        boolean paramSerialization=true;
                
        if (value!=null) {
            paramSerialization = 
                    ComponentUtil.
                    isParameterSerializationEnabled(value.getClass());
        }
        
        return !serialization || !paramSerialization;
    }
}