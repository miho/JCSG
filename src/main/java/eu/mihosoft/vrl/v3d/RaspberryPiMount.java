/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.mihosoft.vrl.v3d;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class RaspberryPiMount {
    

    public static CSG board() {
        double board_thickness = 2;
        double bottom_thickness = 2;

        double board_mounting_height = 4;

        double outer_offset = 4;
        double inner_offset = 4;

        double board_width = 85.6;
        double board_height = 56;
        double bw = board_width;
        double bh = board_height;

        double sd1 = 14;
        double sd2 = 11;
        double sd3 = 18;

        Polygon board_points_exact = Polygon.fromPoints(
                new Vector3d(0, 0),
                new Vector3d(0, bh),
                new Vector3d(bw, bh),
                new Vector3d(bw, bh - sd1),
                new Vector3d(bw - sd3, bh - sd1),
                new Vector3d(bw - sd3, sd2),
                new Vector3d(bw, sd2),
                new Vector3d(bw, 0)
        );

// outer offset 
        double ox1 = outer_offset;
        double oy1 = outer_offset;

// inner offset
        double ox2 = inner_offset;
        double oy2 = inner_offset;
        
        CSG outer = Extrude.points(
                new Vector3d(0, 0, bottom_thickness),
                new Vector3d(0-ox1,0-oy1),
                new Vector3d(0-ox1,bh+oy1),
                new Vector3d(bw+ox1,bh+oy1),
                new Vector3d(bw+ox1,bh-sd1),
                new Vector3d(bw-sd3,bh-sd1),
                new Vector3d(bw-sd3,sd2),
                new Vector3d(bw+ox1,sd2),
                new Vector3d(bw+ox1,0-oy1)
        );
        
        CSG inner = Extrude.points(
                new Vector3d(0, 0, bottom_thickness),
                new Vector3d(0+ox2,0+oy2),
                new Vector3d(0+ox2,bh-oy2),
                new Vector3d(bw-ox2,bh-oy2),
                new Vector3d(bw-ox2,bh-sd1+oy2),
                new Vector3d(bw-sd3-ox2,bh-sd1+oy2),
                new Vector3d(bw-sd3-ox2,sd2-oy2),
                new Vector3d(bw-ox2,sd2-oy2),
                new Vector3d(bw-ox2,0+oy2)
        );
        
        return outer.difference(inner).transformed(Transform.unity().rotX(180).translateY(-bh));
    }
    
    public static CSG boardAndPegs() {
        
        double board_width = 85.6;
        double board_height = 56;
        double bw = board_width;
        double bh = board_height;
        
        double outer_offset = 4;
        
        double bottom_thickness = 2;
        
        CSG board = board();
        
        CSG peg1 = Peg.peg().transformed(Transform.unity().translate(0,bh-8,-bottom_thickness));

        CSG peg2 = Peg.peg().transformed(Transform.unity().translate(8,bh,-bottom_thickness).rotZ(90));
        
        
        CSG peg3 = Peg.peg().transformed(Transform.unity().translate(bw/2,bh,-bottom_thickness).rotZ(90));
        
//        translate([bw,outer_offset,0])
//rotate([0,0,180])
        CSG peg4 = Peg.peg().transformed(Transform.unity().translate(bw,bh-outer_offset,-bottom_thickness).rotZ(180));
        
//        translate([bw-12,bh,0])
//rotate([0,0,270])
        CSG peg5 = Peg.peg().transformed(Transform.unity().translate(bw-12,0,-bottom_thickness).rotZ(270));
        
//        translate([30,bh,0])
//rotate([0,0,270])
        CSG peg6 = Peg.peg().transformed(Transform.unity().translate(30,0,-bottom_thickness).rotZ(270));
        
        CSG union = board.union(peg1).union(peg2).union(peg3).union(peg4).union(peg5).union(peg6);
        
        return union;
        
//        return peg1;
    }
}
