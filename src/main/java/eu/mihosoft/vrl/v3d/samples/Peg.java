/**
 * Peg.java
 *
 * Copyright 2014-2014 Michael Hoffer info@michaelhoffer.de. All rights
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
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer info@michaelhoffer.de "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer info@michaelhoffer.de OR
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
 * info@michaelhoffer.de.
 */
package eu.mihosoft.vrl.v3d.samples;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Extrude;
import eu.mihosoft.vrl.v3d.Transform;
import eu.mihosoft.vrl.v3d.Vector3d;

// TODO: Auto-generated Javadoc
/**
 *  
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
 *  .
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
    /** The outer offset. */
    // ol    = overlap between board and peg
    private double outerOffset = 4;
    
    /** The inner offset. */
    private double innerOffset = 4;
    
    /** The board mounting height. */
    private double boardMountingHeight = 4;
    
    /** The board thickness. */
    private double boardThickness = 2;
    
    /** The overlap. */
    private double overlap = 1;
    
    /** The peg depth. */
    private double pegDepth = 3;
    
    /** The peg tooth height. */
    private double pegToothHeight = 1;
    
    /** The peg top height. */
    private double pegTopHeight = 2;
    
    /** The board spacing. */
    private double boardSpacing = 0.2;

    /**
     * To csg.
     *
     * @return the csg
     */
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
     * Gets the outer offset.
     *
     * @return the outerOffset
     */
    public double getOuterOffset() {
        return outerOffset;
    }

    /**
     * Sets the outer offset.
     *
     * @param outerOffset the outerOffset to set
     * @return the peg
     */
    public Peg setOuterOffset(double outerOffset) {
        this.outerOffset = outerOffset;
        
        return this;
    }

    /**
     * Gets the inner offset.
     *
     * @return the innerOffset
     */
    public double getInnerOffset() {
        return innerOffset;
    }

    /**
     * Sets the inner offset.
     *
     * @param innerOffset the innerOffset to set
     * @return the peg
     */
    public Peg setInnerOffset(double innerOffset) {
        this.innerOffset = innerOffset;
        return this;
    }

    /**
     * Gets the board mounting height.
     *
     * @return the boardMountingHeight
     */
    public double getBoardMountingHeight() {
        return boardMountingHeight;
    }

    /**
     * Sets the board mounting height.
     *
     * @param boardMountingHeight the boardMountingHeight to set
     * @return the peg
     */
    public Peg setBoardMountingHeight(double boardMountingHeight) {
        this.boardMountingHeight = boardMountingHeight;
        return this;
    }

    /**
     * Gets the board thickness.
     *
     * @return the boardThickness
     */
    public double getBoardThickness() {
        return boardThickness;
    }

    /**
     * Sets the board thickness.
     *
     * @param boardThickness the boardThickness to set
     * @return the peg
     */
    public Peg setBoardThickness(double boardThickness) {
        this.boardThickness = boardThickness;
        return this;
    }

    /**
     * Gets the overlap.
     *
     * @return the overlap
     */
    public double getOverlap() {
        return overlap;
    }

    /**
     * Sets the overlap.
     *
     * @param overlap the overlap to set
     * @return the peg
     */
    public Peg setOverlap(double overlap) {
        this.overlap = overlap;
        return this;
    }

    /**
     * Gets the peg depth.
     *
     * @return the pegDepth
     */
    public double getPegDepth() {
        return pegDepth;
    }

    /**
     * Sets the peg depth.
     *
     * @param pegDepth the pegDepth to set
     * @return the peg
     */
    public Peg setPegDepth(double pegDepth) {
        this.pegDepth = pegDepth;
        return this;
    }

    /**
     * Gets the peg tooth height.
     *
     * @return the pegToothHeight
     */
    public double getPegToothHeight() {
        return pegToothHeight;
    }

    /**
     * Sets the peg tooth height.
     *
     * @param pegToothHeight the pegToothHeight to set
     * @return the peg
     */
    public Peg setPegToothHeight(double pegToothHeight) {
        this.pegToothHeight = pegToothHeight;
        return this;
    }

    /**
     * Gets the peg top height.
     *
     * @return the pegTopHeight
     */
    public double getPegTopHeight() {
        return pegTopHeight;
    }

    /**
     * Sets the peg top height.
     *
     * @param pegTopHeight the pegTopHeight to set
     * @return the peg
     */
    public Peg setPegTopHeight(double pegTopHeight) {
        this.pegTopHeight = pegTopHeight;
        return this;
    }

    /**
     * Gets the board spacing.
     *
     * @return the boardSpacing
     */
    public double getBoardSpacing() {
        return boardSpacing;
    }

    /**
     * Sets the board spacing.
     *
     * @param boardSpacing the boardSpacing to set
     * @return the peg
     */
    public Peg setBoardSpacing(double boardSpacing) {
        this.boardSpacing = boardSpacing;
        return this;
    }
    
    
}
