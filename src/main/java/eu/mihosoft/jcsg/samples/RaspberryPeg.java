/**
 * Peg.java
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

package eu.mihosoft.jcsg.samples;

import eu.mihosoft.jcsg.Extrude;
import eu.mihosoft.vvecmath.Transform;
import eu.mihosoft.jcsg.CSG;
import eu.mihosoft.vvecmath.Vector3d;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class RaspberryPeg {

    public static CSG peg() {
        //      ol
        //     | |
        //   __    _
        //  |  \   ptoph
        //  |   \  _
        //  |   /  pth 
        //  |  /   _
        //  | |    bt
        //  | |__  _
        //  |    | bh
        //  ------ -
        //  |pw |

        // pw    = peg width
        // bh    = board mounting height
        // bt    = board thickness
        // pth   = peg tooth hight
        // ptoph = peg top height
        // ol    = overlap between board and peg
        double outer_offset = 4;
        double inner_offset = 4;
        double board_mounting_height = 5.5;
        double board_thickness = 2;
        double overlap = 1;
        double peg_depth = 3;
        double peg_tooth_height = 1;
        double peg_top_height = 2;
        double board_spacing = 0.2;

        // inner offset
        double oi = inner_offset;
        //outer offset
        double oo = outer_offset;
        double bh = board_mounting_height;
        double bt = board_thickness;
        double ol = overlap;

        double pth = peg_tooth_height;
        double ptoph = peg_top_height;

        // board spacing (small spacing between peg and board, should be < 0.5mm)
        double bs = board_spacing;

        double pd = peg_depth;

        double pw = oo + oi;

        CSG peg_points = Extrude.points(
                Vector3d.xyz(0, 0, pd),
                Vector3d.xy(0, 0),
                Vector3d.xy(pw, 0),
                Vector3d.xy(pw, bh / 5),
                Vector3d.xy(pw - oi / 2, bh),
                Vector3d.xy(oo - bs, bh),
                Vector3d.xy(oo - bs, bh + bt),
                Vector3d.xy(oo + ol, bh + bt + pth),
                Vector3d.xy(oo, bh + bt + pth + ptoph),
                Vector3d.xy(0, bh + bt + pth + ptoph)
        );

        return peg_points.
                transformed(Transform.unity().translateX(-oo)).
                transformed(Transform.unity().rotX(90).translateZ(-pd / 2));
    }
}
