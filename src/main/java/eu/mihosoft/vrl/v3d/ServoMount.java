/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.mihosoft.vrl.v3d;

/**
 *
 * @author miho
 */
public class ServoMount {
    // mini servo
//    private double servoWidth = 22.9;
//    private double servoThickness = 12.0;
    //standard servo
    private double servoWidth = 40.0;
    private double servoThickness = 19.0;
    private double borderThickness = 2;
    private double overlap = 3;
    private double servoMountHeight = 10;
    
    private double boardMountingThickness = 2;
    private double boardHolderLength = 4;
    
    public CSG toCSGSimple() {
        return Extrude.points(Vector3d.Z_ONE.times(servoMountHeight), 
                Vector3d.ZERO,
                new Vector3d(servoWidth,0),
                new Vector3d(servoWidth, servoThickness),
                new Vector3d(servoWidth-overlap, servoThickness),
                new Vector3d(servoWidth+borderThickness,servoThickness+borderThickness),
                new Vector3d(servoWidth+borderThickness,-borderThickness),
                new Vector3d(-borderThickness,-borderThickness),
                new Vector3d(-borderThickness, servoThickness+borderThickness),
                new Vector3d(overlap,servoThickness),
                new Vector3d(0,servoThickness)
                );
    }
    
    public CSG toCSG() {
        CSG bm = boardMount().transformed(Transform.unity().rotY(90).rotZ(90).translateZ(10)).transformed(Transform.unity().scale(0.08));
        
        CSG sm = toCSGSimple().transformed(Transform.unity().scale(0.08));
        
        return sm.union(bm);
    }
    
        private CSG boardMount() {
            
        double h = servoMountHeight;    
            
        return Extrude.points(Vector3d.Z_ONE.times(boardHolderLength), 
                Vector3d.ZERO,
                new Vector3d(0,-borderThickness),
                new Vector3d(boardMountingThickness+borderThickness, -borderThickness),
                new Vector3d(boardMountingThickness+borderThickness, h),
                 new Vector3d(boardMountingThickness, h),
              new Vector3d(boardMountingThickness, 0)
                );
    }
}
