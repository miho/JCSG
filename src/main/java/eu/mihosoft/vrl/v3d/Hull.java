/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.mihosoft.vrl.v3d;

import eu.mihosoft.vrl.v3d.ext.quickhull3d.HullUtil;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class Hull {

    private Hull() {
        throw new AssertionError("Don't instantiate me!", null);
    }
    
    public static CSG fromCSG(CSG csg) {
        return HullUtil.hull(csg);
    }
}
