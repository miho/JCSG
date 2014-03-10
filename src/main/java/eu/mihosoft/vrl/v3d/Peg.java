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
public class Peg {
    
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
        double board_mounting_height = 4;
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
                new Vector3d(0, 0, pd),
                new Vector3d(0, 0),
                new Vector3d(pw, 0),
                new Vector3d(pw, bh / 5),
                new Vector3d(pw - oi / 2, bh),
                new Vector3d(oo - bs, bh),
                new Vector3d(oo - bs, bh + bt),
                new Vector3d(oo + ol, bh + bt + pth),
                new Vector3d(oo, bh + bt + pth + ptoph),
                new Vector3d(0, bh + bt + pth + ptoph)
        );

        return peg_points.
                transformed(Transform.unity().translateX(-oo)).
                transformed(Transform.unity().rotX(90).translateZ(-pd/2));
    }
}
