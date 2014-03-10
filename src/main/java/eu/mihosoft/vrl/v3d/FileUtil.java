/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * File util class.
 * 
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class FileUtil {

    private FileUtil() {
        throw new AssertionError("Don't instantiate me", null);
    }

    /**
     * Writes the specified string to a file.
     *
     * @param p file destination (existing files will be overwritten)
     * @param s string to save
     * 
     * @throws IOException if writing to file fails
     */
    public static void write(Path p, String s) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(p, Charset.forName("UTF-8"),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write(s, 0, s.length());
        }
    }

    /**
     * Reads the specified file to a string.
     *
     * @param p file to read
     * @return the content of the file
     * 
     * @throws IOException if reading from file failed
     */
    public static String read(Path p) throws IOException {
        return new String(Files.readAllBytes(p), Charset.forName("UTF-8"));
    }
}
