///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package eu.mihosoft.jcsg.samples;
//
//import eu.mihosoft.jcsg.CSG;
//import eu.mihosoft.jcsg.Cube;
//import eu.mihosoft.jcsg.FileUtil;
//import eu.mihosoft.jcsg.Text3d;
//
//import java.io.IOException;
//import java.nio.file.Paths;
//
///**
// *
// * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
// */
//public class Text3dSample {
//
//    public CSG toCSG(String text) {
//        
//        double border = 5;
//
//        CSG text3d = new Text3d(text, "Arial", 12, 1).toCSG();
//        
//        double boxWidth = text3d.getBounds().getBounds().x+border*2;
//        double boxHeight = text3d.getBounds().getBounds().y+border*2;
//        double boxDepth = text3d.getBounds().getBounds().z;
//        
//        CSG box = new Cube(boxWidth, boxHeight, boxDepth).toCSG();
//        
//        return box.difference(text3d);
//    }
//
//    public static void main(String[] args) throws IOException {
//        FileUtil.write(Paths.get("text3d-sample.stl"),
//                new Text3dSample().toCSG("JCSG - Text3d").toStlString());
//    }
//}
