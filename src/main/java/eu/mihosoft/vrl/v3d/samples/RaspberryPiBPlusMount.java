/**
 * RaspberryPiMount.java
 *
 * Copyright 2014-2014 Michael Hoffer <info@michaelhoffer.de>. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info@michaelhoffer.de> "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info@michaelhoffer.de> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of Michael Hoffer <info@michaelhoffer.de>.
 */ 

package eu.mihosoft.vrl.v3d.samples;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Extrude;
import eu.mihosoft.vrl.v3d.FileUtil;
import eu.mihosoft.vrl.v3d.Polygon;
import eu.mihosoft.vrl.v3d.Transform;
import static eu.mihosoft.vrl.v3d.Transform.unity;
import eu.mihosoft.vrl.v3d.Vector3d;
import java.io.IOException;
import java.nio.file.Paths;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class RaspberryPiBPlusMount {
    

    public static CSG board() {
        double board_thickness = 2;
        double bottom_thickness = 3;

        double board_mounting_height = 4;

        double outer_offset = 4;
        double inner_offset = 4;

        double board_width = 85;
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
//                new Vector3d(bw+ox1,bh-sd1),
//                new Vector3d(bw-sd3,bh-sd1),
//                new Vector3d(bw-sd3,sd2),
                new Vector3d(bw+ox1,sd2),
                new Vector3d(bw+ox1,0-oy1)
        );
        
        CSG inner = Extrude.points(
                new Vector3d(0, 0, bottom_thickness),
                new Vector3d(0+ox2,0+oy2),
                new Vector3d(0+ox2,bh-oy2),
                new Vector3d(bw-ox2,bh-oy2),
//                new Vector3d(bw-ox2,bh-sd1+oy2),
//                new Vector3d(bw-sd3-ox2,bh-sd1+oy2),
//                new Vector3d(bw-sd3-ox2,sd2-oy2),
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
        
        double bottom_thickness = 3;
        
        CSG board = board();
        
        CSG peg1 = RaspberryPeg.peg().transformed(unity().scaleY(0.9)).transformed(Transform.unity().translate(0,bh-36,-bottom_thickness));

        CSG peg2 = RaspberryPeg.peg().transformed(unity().scaleY(2)).transformed(Transform.unity().translate(22,bh,-bottom_thickness).rotZ(90));
        
        
        CSG peg3 = RaspberryPeg.peg().transformed(Transform.unity().translate(bw-outer_offset,bh,-bottom_thickness).rotZ(90));
        
//        translate([bw,outer_offset,0])
//rotate([0,0,180])
        CSG peg4 = RaspberryPeg.peg().transformed(Transform.unity().translate(bw,bh-outer_offset*2,-bottom_thickness).rotZ(180));
        CSG peg4b = RaspberryPeg.peg().transformed(Transform.unity().translate(bw,outer_offset,-bottom_thickness).rotZ(180));

        CSG peg5 = RaspberryPeg.peg().transformed(unity().scaleY(2)).transformed(Transform.unity().translate(bw-19,0,-bottom_thickness).rotZ(270));

        CSG peg6 = RaspberryPeg.peg().transformed(Transform.unity().translate(bw-62,0,-bottom_thickness).rotZ(270));
        
        CSG union = board.union(peg1,peg2,peg3,peg4,peg4b,peg5,peg6);
        
        return union;
        
//        return peg1;
    }
    
        public static void main(String[] args) throws IOException {

        // save union as stl
//        FileUtil.write(Paths.get("sample.stl"), new ServoHead().servoHeadFemale().transformed(Transform.unity().scale(1.0)).toStlString());
        
            CSG board = RaspberryPiBPlusMount.boardAndPegs().transformed(Transform.unity().rotX(180));
            FileUtil.write(Paths.get("raspberry-pi-bplus-mount-3mm.stl"), board.toStlString());
            
            board.toObj().toFiles(Paths.get("raspberry-pi-bplus-mount-3mm.obj"));

    }
}
