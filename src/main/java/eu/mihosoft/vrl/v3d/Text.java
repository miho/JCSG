package eu.mihosoft.vrl.v3d;

import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.PathIterator;
import java.util.ArrayList;

public class Text {
    /**
     * Extrudes the specified path (convex or concave polygon without holes or
     * intersections, specified in CCW) into the specified direction.
     *
     * @param dir direction of extrusion
     * @param text text
     * @param font font configuration of the text
     *
     * @return a CSG object that consists of the extruded polygon
     */
    public static CSG text(double dir, String text, Font font) {



    	FontRenderContext frc = new FontRenderContext(null,(boolean)true,(boolean)true);
    	TextLayout textLayout = new TextLayout(text, font, frc);
    	Shape s = textLayout.getOutline(null);

    	PathIterator pi = s.getPathIterator(null);
    	ArrayList<Vector3d> points = new ArrayList<Vector3d>();

    	CSG stringOut = null;

    	float [] coords = new float[6];
    	float [] coords2 = new float[6];
    	float [] start = new float[6];
    	  while(pi.isDone() == (boolean)false ) {
    	        int type = pi.currentSegment(coords);
    	        switch(type) {
    	        case PathIterator.SEG_CLOSE:
    				//points.add(new Vector3d(coords2[0], coords2[1] ,0));
    				//points.add(new Vector3d(start[0], start[1],0));
    				if(points.size()>3){
    					System.out.println("Adding "+points); 
    					CSG newLetter = Extrude.points(new Vector3d(0, 0, dir), points);
    					
    					if(stringOut== null)
    						stringOut=newLetter;
    					else
    						stringOut = stringOut.union(newLetter);
    					//return stringOut;
    				}
    	            points = new ArrayList<Vector3d>();
    	            break;
    	        case PathIterator.SEG_LINETO:
    				System.out.println( "SEG_LINETO "+coords2+" and "+coords);
    	            //points.add(new Vector3d(coords2[0], coords2[1],0));
    	            points.add(new Vector3d(coords[0], coords[1],0));
    	            coords2[0] = coords[0];
    	            coords2[1] = coords[1];
    	            break;
    	        case PathIterator.SEG_MOVETO:
    				
    	            // move without drawing
    	            start[0] = coords2[0] = coords[0];
    	            start[1] = coords2[1] = coords[1];
    				System.out.println( "Moving to "+start);
    				points.add(new Vector3d(start[0], start[1],0));
    	            break;
    	        case PathIterator.SEG_CUBICTO:
    	            for(float t=0.0f;t<=1.05f;t+=0.1f) {
    	                // p = a0 + a1*t + a2 * tt + a3*ttt;
    	                float tt=t*t;
    	                float ttt=tt*t;
    	                float p1 = coords2[0] + (coords[0]*t) + (coords[2]*tt) + (coords[4]*ttt);
    	                float p2 = coords2[1] + (coords[1]*t) + (coords[3]*tt) + (coords[5]*ttt);
    	                points.add(new Vector3d(p1, p2,0));
    					System.out.println( "SEG_CUBICTO "+p1+" and "+p2);
    	            }
    	            coords2[0] = coords[4];
    	            coords2[1] = coords[5];
    	            break;
    	        case PathIterator.SEG_QUADTO:
    	            for(float t=0.0f;t<=1.05f;t+=0.1f) {
    	                //(1-t)²*P0 + 2t*(1-t)*P1 + t²*P2
    	                float u = (1.0f-t);
    	                float tt=u*u;
    	                float ttt=2.0f*t*u;
    	                float tttt=t*t;
    	                float p1 = coords2[0]*tt + (coords[0]*ttt) + (coords[2]*tttt);
    	                float p2 = coords2[1]*tt + (coords[1]*ttt) + (coords[3]*tttt);
    	                points.add(new Vector3d(p1, p2,0));
    					System.out.println( "SEG_QUADTO "+p1+" and "+p2);
    	            }
    	            coords2[0] = coords[2];
    	            coords2[1] = coords[3];
    	            break;
    	        }
    	        pi.next();
    			System.out.println( "pi.isDone() "+pi.isDone());
    	    }
    	  
    	return stringOut;
    }
    
    
}
