/**
 * Peg.java
 *
 * Copyright 2014-2014 Michael Hoffer <info@michaelhoffer.de>. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info@michaelhoffer.de> "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info@michaelhoffer.de> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of Michael Hoffer
 * <info@michaelhoffer.de>.
 */
package eu.mihosoft.vrl.v3d.samples;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Extrude;
import eu.mihosoft.vrl.v3d.Transform;
import eu.mihosoft.vrl.v3d.Vector3d;

/**
 *
 * <pre>
 *       ol
 *      | |
 *       __    _
 *      |  \   ptoph
 *      |   \  _
 *      |   /  pth 
 *      |  /   _
 *      | |    bt
 *      | |__  _
 *      |    | bh
 *      -------
 *      |pw  |
 *   
 *      pw    = peg width
 *      bh    = board mounting height
 *      bt    = board thickness
 *      pth   = peg tooth hight
 *      ptoph = peg top height
 *      ol    = overlap between board and peg
 * </pre>
 * 
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class Peg {

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
    private double outerOffset = 4;
    private double innerOffset = 4;
    private double boardMountingHeight = 4;
    private double boardThickness = 2;
    private double overlap = 1;
    private double pegDepth = 3;
    private double pegToothHeight = 1;
    private double pegTopHeight = 2;
    private double boardSpacing = 0.2;

    public CSG toCSG() {

        // inner offset
        double oi = getInnerOffset();
        //outer offset
        double oo = getOuterOffset();
        double bh = getBoardMountingHeight();
        double bt = getBoardThickness();
        double ol = getOverlap();

        double pth = getPegToothHeight();
        double ptoph = getPegTopHeight();

        // board spacing (small spacing between peg and board, should be < 0.5mm)
        double bs = getBoardSpacing();

        double pd = getPegDepth();

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
                transformed(Transform.unity().rotX(90).translateZ(-pd / 2));
    }

    /**
     * @return the outerOffset
     */
    public double getOuterOffset() {
        return outerOffset;
    }

    /**
     * @param outerOffset the outerOffset to set
     */
    public Peg setOuterOffset(double outerOffset) {
        this.outerOffset = outerOffset;
        
        return this;
    }

    /**
     * @return the innerOffset
     */
    public double getInnerOffset() {
        return innerOffset;
    }

    /**
     * @param innerOffset the innerOffset to set
     */
    public Peg setInnerOffset(double innerOffset) {
        this.innerOffset = innerOffset;
        return this;
    }

    /**
     * @return the boardMountingHeight
     */
    public double getBoardMountingHeight() {
        return boardMountingHeight;
    }

    /**
     * @param boardMountingHeight the boardMountingHeight to set
     */
    public Peg setBoardMountingHeight(double boardMountingHeight) {
        this.boardMountingHeight = boardMountingHeight;
        return this;
    }

    /**
     * @return the boardThickness
     */
    public double getBoardThickness() {
        return boardThickness;
    }

    /**
     * @param boardThickness the boardThickness to set
     */
    public Peg setBoardThickness(double boardThickness) {
        this.boardThickness = boardThickness;
        return this;
    }

    /**
     * @return the overlap
     */
    public double getOverlap() {
        return overlap;
    }

    /**
     * @param overlap the overlap to set
     */
    public Peg setOverlap(double overlap) {
        this.overlap = overlap;
        return this;
    }

    /**
     * @return the pegDepth
     */
    public double getPegDepth() {
        return pegDepth;
    }

    /**
     * @param pegDepth the pegDepth to set
     */
    public Peg setPegDepth(double pegDepth) {
        this.pegDepth = pegDepth;
        return this;
    }

    /**
     * @return the pegToothHeight
     */
    public double getPegToothHeight() {
        return pegToothHeight;
    }

    /**
     * @param pegToothHeight the pegToothHeight to set
     */
    public Peg setPegToothHeight(double pegToothHeight) {
        this.pegToothHeight = pegToothHeight;
        return this;
    }

    /**
     * @return the pegTopHeight
     */
    public double getPegTopHeight() {
        return pegTopHeight;
    }

    /**
     * @param pegTopHeight the pegTopHeight to set
     */
    public Peg setPegTopHeight(double pegTopHeight) {
        this.pegTopHeight = pegTopHeight;
        return this;
    }

    /**
     * @return the boardSpacing
     */
    public double getBoardSpacing() {
        return boardSpacing;
    }

    /**
     * @param boardSpacing the boardSpacing to set
     */
    public Peg setBoardSpacing(double boardSpacing) {
        this.boardSpacing = boardSpacing;
        return this;
    }
    
    
}
