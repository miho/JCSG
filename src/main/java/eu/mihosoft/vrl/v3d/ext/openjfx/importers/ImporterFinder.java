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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class ImporterFinder.
 */
public class ImporterFinder {

    /**
     * Adds the url to class path.
     *
     * @return the URL class loader
     */
    public URLClassLoader addUrlToClassPath() {
        final Class<?> referenceClass = ImporterFinder.class;
        final URL url = referenceClass.getProtectionDomain().getCodeSource().getLocation();

        File libDir = null;
        try {
            File currentDir = new File(url.toURI()).getParentFile();
            libDir = new File(currentDir, "lib");
        } catch (URISyntaxException ue) {
            ue.printStackTrace();
            throw new RuntimeException("Could not import library. Failed to determine library location. URL = " + url.getPath());
        }
        if (libDir != null) {
            File[] files = libDir.listFiles();
            final List<URL> urlList = new ArrayList<>();
            if (files != null) {
                for (File file : files) {
                    try {
                        urlList.add(file.toURI().toURL());
                    } catch (MalformedURLException me) {
                        me.printStackTrace();
                    }
                }
            }
            URLClassLoader cl = new URLClassLoader((URL[]) urlList.toArray(new URL[0]), this.getClass().getClassLoader());
            return cl;
        } else {
            throw new RuntimeException("Could not import library. Failed to determine importer library location ");
        }
    }
}
