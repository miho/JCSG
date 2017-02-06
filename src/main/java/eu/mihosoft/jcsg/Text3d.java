/**
 * Text3d.java
 *
 * Copyright 2014-2016 Michael Hoffer <info@michaelhoffer.de>. All rights
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
package eu.mihosoft.jcsg;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.text.Font;
/**
 * 3d text primitive. 
 * 
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public class Text3d implements Primitive {

    private final PropertyStorage properties = new PropertyStorage();
    ArrayList<CSG> letters;
    /**
     * Constructor.
     * 
     * @param text text
     */
    public Text3d(String text) {
        this(text, "Arial", 12, 1.0);
    }

    /**
     * Constructor.
     * 
     * @param text text
     * @param depth text depth (z thickness)
     */
    public Text3d(String text, double depth) {
        this(text, "Arial", 12, depth);
    }

    /**
     * Constructor. 
     * 
     * @param text text
     * @param fontName font name, e.g., "Arial"
     * @param fontSize font size
     * @param depth text depth (z thickness)
     */
    public Text3d(String text, String fontName, double fontSize, double depth) {

    	Font font = new Font(fontName,  (int) fontSize);
    	letters = TextExtrude.text( depth,  text,  font);
    	for (int i=0;i<letters.size();i++){
    		letters.set(i, letters.get(i)
    				.rotx(180)
    				.toZMin()
    				);
    	}
    	
    	
    }

    @Override
    public List<Polygon> toPolygons() {
    	return letters.get(0).union(letters).getPolygons();
    }

    @Override
    public PropertyStorage getProperties() {
        return properties;
    }

    public Text3d noCenter() {
        return this;
    }

}