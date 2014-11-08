/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d.vplugin;

import eu.mihosoft.vrl.annotation.ComponentInfo;
import eu.mihosoft.vrl.annotation.ParamInfo;
import eu.mihosoft.vrl.io.IOUtil;
import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.FileUtil;
import eu.mihosoft.vrl.v3d.PropertyStorage;
import eu.mihosoft.vrl.v3d.Vector3d;
import eu.mihosoft.vrl.v3d.ext.quickhull3d.HullUtil;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
@ComponentInfo(name = "PointCloud2ConvexHull", category = "JCSG")
public class PointCloud2ConvexHull implements Serializable {

    private static final long serialVersionUID = 1L;

    public void hull(
            @ParamInfo(name = "List<Vector3d> points") List<Vector3d> points,
            @ParamInfo(style = "save-dialog", options = "endings=[\".stl\"]; description=\".stl File\"") File outputFile) throws IOException {
        CSG result = HullUtil.hull(points, new PropertyStorage());

        FileUtil.write(outputFile, result.toStlString());
    }

    public void hull(
            @ParamInfo(style = "load-dialog") File pointFile,
            @ParamInfo(style = "save-dialog", options = "endings=[\".stl\"]; description=\".stl File\"") File outputFile) throws IOException {

        List<String> lines = IOUtil.readFileToStringList(pointFile);

        List<Vector3d> points = new ArrayList<Vector3d>(lines.size());

        int lineCount = -1;

        for (String l : lines) {
            lineCount++;
            if (l.trim().startsWith("#")) {
                continue;
            }

            String[] coordString = l.split(" ");

            if (coordString.length != 3) {
                throw new RuntimeException(
                        "Syntax error in line " + lineCount + ": too many entries.");
            }
            try {
                Vector3d vec = new Vector3d(
                        Double.parseDouble(coordString[0]),
                        Double.parseDouble(coordString[1]),
                        Double.parseDouble(coordString[2]));
                points.add(vec);
            } catch (NumberFormatException ex) {
                throw new RuntimeException(
                        "Syntax error in line " + lineCount + ": illegal number format.");
            }
        }

        CSG result = HullUtil.hull(points, new PropertyStorage());

        FileUtil.write(outputFile, result.toStlString());
    }

    public CSG hull(
            @ParamInfo(name = "List<Vector3d> points") List<Vector3d> points) {
        return HullUtil.hull(points, new PropertyStorage());
    }
}
