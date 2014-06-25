/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public final class ObjFile {

    private String obj;
    private final String mtl;
    private InputStream objStream;
    private InputStream mtlStream;

    static final String MTL_NAME = "$JCSG_MTL_NAME$";

    ObjFile(String obj, String mtl) {
        this.obj = obj;
        this.mtl = mtl;
    }

    public void toFiles(Path p) throws IOException {

        Path parent = p.getParent();

        String fileName = p.getFileName().toString();

        if (fileName.toLowerCase().endsWith(".obj")
                || fileName.toLowerCase().endsWith(".mtl")) {
            fileName = fileName.substring(0, fileName.length() - 4);
        }

        String objName = fileName + ".obj";
        String mtlName = fileName + ".mtl";

        obj = obj.replace(MTL_NAME, mtlName);
        objStream = null;

        if (parent == null) {
            FileUtil.write(Paths.get(objName), obj);
            FileUtil.write(Paths.get(mtlName), mtl);
        } else {
            FileUtil.write(Paths.get(parent.toString(), objName), obj);
            FileUtil.write(Paths.get(parent.toString(), mtlName), mtl);
        }

    }

    public String getObj() {
        return this.obj;
    }

    public String getMtl() {
        return this.mtl;
    }
    
    public InputStream getObjStream() {
        if (objStream == null) {
           objStream = new ByteArrayInputStream(obj.getBytes(StandardCharsets.UTF_8));
        }
        
        return objStream;
    }
    
    public InputStream getMtlStream() {
        if (mtlStream == null) {
           mtlStream = new ByteArrayInputStream(mtl.getBytes(StandardCharsets.UTF_8));
        }
        
        return mtlStream;
    }
}
