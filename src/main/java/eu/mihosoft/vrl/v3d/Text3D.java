///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package eu.mihosoft.vrl.v3d;
//
//import java.awt.Font;
//import java.io.IOException;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
//
//
//import java.awt.Font;
//import java.awt.Shape;
//import java.awt.font.FontRenderContext;
//import java.awt.font.TextLayout;
//import java.awt.geom.PathIterator;
//import java.util.ArrayList;
//
///**
// * 3D text.
// *
// *
// *
// * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
// */
//public class Text3D implements Primitive {
//
//    private final PropertyStorage properties = new PropertyStorage();
//    private final String text;
//    private final Font font;
//    private final double depth;
//
//    /**
//     * Constructor. Creates a text object with depth of 1.
//     *
//     * @param text text
//     */
//    public Text3D(String text) {
//        this(text, 1);
//    }
//
//    /**
//     * Constructor. Creates a text object.
//     *
//     * @param text text
//     * @param depth depth of this 3D object
//     */
//    public Text3D(String text, double depth) {
//        this(text, new Font("Sans", Font.PLAIN, 12), depth);
//    }
//
//    /**
//     * Constructor. Creates a text object.
//     *
//     * @param text text
//     * @param font font that shall be used for geometry generation
//     * @param depth depth of this 3D object
//     */
//    public Text3D(String text, Font font, double depth) {
//        this.text = text;
//        this.font = font;
//        this.depth = depth;
//    }
//
//    @Override
//    public List<Polygon> toPolygons() {
//        
////         CSG charCSG = CSG.fromPolygons(
////                    FontToPolygons.charToPolygons(text, font, depth, 0.5));
////         
////         return charCSG.getPolygons();
//
//        double offset = 0;
//        double spacing = 1;
//
//        CSG resultCSG = null;
//
//        for (int i = 0; i < text.length(); i++) {
//
//            char c = text.charAt(i);
//
//            CSG charCSG = CSG.fromPolygons(
//                    FontToPolygons.charToPolygons(c, font, depth, 0.5));
//
//            charCSG = charCSG.transformed(Transform.unity().
//                    translateX(offset));
//            offset += charCSG.getBounds().getBounds().x + spacing;
//
//            if (resultCSG == null) {
//                resultCSG = charCSG;
//            } else {
//                resultCSG = resultCSG.dumbUnion(charCSG);
//            }
//        }
//
//        if (resultCSG != null) {
//            return resultCSG.getPolygons();
//        } else {
//            return new ArrayList<>();
//        }
//    }
//
//    public static void main(String[] args) {
//
//        CSG t = new Text3D("o").toCSG();
//        
////        t = t.transformed(Transform.unity().scaleZ(4));
//
//        CSG cube = new Cube(50, 20, 3).toCSG().
//                transformed(Transform.unity().translateZ(-0.8));
//
////        cube = cube.difference(t);
////
////        cube = t;
//        
//        try {
//            FileUtil.write(Paths.get("out.stl"), t.toStlString());
//            t.toObj().toFiles(Paths.get("out.obj"));
//            CSG csg = STL.file(Paths.get("out.stl"));
//            cube = cube.difference(csg);
//            FileUtil.write(Paths.get("out.stl"), cube.toStlString());
//        } catch (IOException ex) {
//            Logger.getLogger(Text3D.class.getName()).
//                    log(Level.SEVERE, null, ex);
//        }
//        
//        
//    }
//
//    @Override
//    public PropertyStorage getProperties() {
//        return properties;
//    }
//
//}
//
//
//
//
// class Text {
//    /**
//     * Extrudes the specified path (convex or concave polygon without holes or
//     * intersections, specified in CCW) into the specified direction.
//     *
//     * @param dir direction of extrusion
//     * @param text text
//     * @param font font configuration of the text
//     *
//     * @return a CSG object that consists of the extruded polygon
//     */
//    public static CSG text(double dir, String text, Font font) {
//
//
//
//    	FontRenderContext frc = new FontRenderContext(null,(boolean)true,(boolean)true);
//    	TextLayout textLayout = new TextLayout(text, font, frc);
//    	Shape s = textLayout.getOutline(null);
//
//    	PathIterator pi = s.getPathIterator(null);
//    	ArrayList<Vector3d> points = new ArrayList<Vector3d>();
//
//    	CSG stringOut = null;
//
//    	float [] coords = new float[6];
//    	float [] coords2 = new float[6];
//    	float [] start = new float[6];
//    	  while(pi.isDone() == (boolean)false ) {
//    	        int type = pi.currentSegment(coords);
//    	        switch(type) {
//    	        case PathIterator.SEG_CLOSE:
//    				//points.add(new Vector3d(coords2[0], coords2[1] ,0));
//    				//points.add(new Vector3d(start[0], start[1],0));
//    				if(points.size()>3){
//    					System.out.println("Adding "+points); 
//    					CSG newLetter = Extrude.points(new Vector3d(0, 0, dir), points);
//    					
//    					if(stringOut== null)
//    						stringOut=newLetter;
//    					else
//    						stringOut = stringOut.union(newLetter);
//    					//return stringOut;
//    				}
//    	            points = new ArrayList<Vector3d>();
//    	            break;
//    	        case PathIterator.SEG_LINETO:
//    				System.out.println( "SEG_LINETO "+coords2+" and "+coords);
//    	            //points.add(new Vector3d(coords2[0], coords2[1],0));
//    	            points.add(new Vector3d(coords[0], coords[1],0));
//    	            coords2[0] = coords[0];
//    	            coords2[1] = coords[1];
//    	            break;
//    	        case PathIterator.SEG_MOVETO:
//    				
//    	            // move without drawing
//    	            start[0] = coords2[0] = coords[0];
//    	            start[1] = coords2[1] = coords[1];
//    				System.out.println( "Moving to "+start);
//    				points.add(new Vector3d(start[0], start[1],0));
//    	            break;
//    	        case PathIterator.SEG_CUBICTO:
//    	            for(float t=0.0f;t<=1.05f;t+=0.1f) {
//    	                // p = a0 + a1*t + a2 * tt + a3*ttt;
//    	                float tt=t*t;
//    	                float ttt=tt*t;
//    	                float p1 = coords2[0] + (coords[0]*t) + (coords[2]*tt) + (coords[4]*ttt);
//    	                float p2 = coords2[1] + (coords[1]*t) + (coords[3]*tt) + (coords[5]*ttt);
//    	                points.add(new Vector3d(p1, p2,0));
//    					System.out.println( "SEG_CUBICTO "+p1+" and "+p2);
//    	            }
//    	            coords2[0] = coords[4];
//    	            coords2[1] = coords[5];
//    	            break;
//    	        case PathIterator.SEG_QUADTO:
//    	            for(float t=0.0f;t<=1.05f;t+=0.1f) {
//    	                //(1-t)²*P0 + 2t*(1-t)*P1 + t²*P2
//    	                float u = (1.0f-t);
//    	                float tt=u*u;
//    	                float ttt=2.0f*t*u;
//    	                float tttt=t*t;
//    	                float p1 = coords2[0]*tt + (coords[0]*ttt) + (coords[2]*tttt);
//    	                float p2 = coords2[1]*tt + (coords[1]*ttt) + (coords[3]*tttt);
//    	                points.add(new Vector3d(p1, p2,0));
//    					System.out.println( "SEG_QUADTO "+p1+" and "+p2);
//    	            }
//    	            coords2[0] = coords[2];
//    	            coords2[1] = coords[3];
//    	            break;
//    	        }
//    	        pi.next();
//    			System.out.println( "pi.isDone() "+pi.isDone());
//    	    }
//    	  
//    	return stringOut;
//    }
//    
//    
//}
//
////return Text.text(10.0, "Hello", new Font("Helvedica", Font.PLAIN, 18));
