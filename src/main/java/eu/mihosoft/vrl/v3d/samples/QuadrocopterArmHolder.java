/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.mihosoft.vrl.v3d.samples;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Cube;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class QuadrocopterArmHolder {
    
    public CSG toCSG(double armHeight, double armScaleFactor, double armCubeWidth) {
        
        double armCubeToOuterOffset = 5;
        double armOffset = 10;
        
        CSG holder = new Cube( armScaleFactor*armHeight, armOffset,armHeight+armCubeToOuterOffset).toCSG();
        
        return holder;
    }
    
    public static void main(String[] args) {
        
    }
}
