/*
 * Copyright (c) 2014, Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package eu.mihosoft.vrl.v3d.ext.openjfx.importers;

import java.io.IOException;
import javafx.animation.Timeline;
import javafx.scene.Group;

// TODO: Auto-generated Javadoc
/**
 * The Class Importer.
 */
public abstract class Importer {
    
    /**
     * Loads the 3D file.
     *
     * @param url The url of the 3D file to load
     * @param asPolygonMesh When true load as a PolygonMesh if the loader
     * supports. 
     * @throws IOException If issue loading file
     */
    public abstract void load(String url, boolean asPolygonMesh) throws IOException; 
    
    /**
     * Gets the 3D node that was loaded earlier through the load() call.
     *
     * @return The loaded node
     */
    public abstract Group getRoot();
    /**
     * Tests if the given 3D file extension is supported (e.g. "ma", "ase", 
     * "obj", "fxml", "dae"). 
     * 
     * @param supportType The file extension (e.g. "ma", "ase", "obj", "fxml", 
     * "dae")
     * @return True if the extension is of a supported type. False otherwise.
     */
    public abstract boolean isSupported(String supportType);
    
    /**
     * Can be overridden to return a timeline animation for the 3D file.
     *
     * @return A timeline animation. Null if there is no timeline animation.
     */
    public Timeline getTimeline() {
        return null;
    }
}
