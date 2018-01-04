package eu.mihosoft.vrl.v3d;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.vecmath.Matrix4d;

import eu.mihosoft.vrl.v3d.ext.org.poly2tri.DelaunayTriangle;
import eu.mihosoft.vrl.v3d.ext.org.poly2tri.PolygonUtil;
import eu.mihosoft.vrl.v3d.parametrics.LengthParameter;
import eu.mihosoft.vrl.v3d.svg.ImageTracer;
import eu.mihosoft.vrl.v3d.svg.SVGLoad;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Scale;

public class Slice {
	private static ISlice sliceEngine = new ISlice (){
	    
	    int xPix;
	    int yPix;
	    boolean done =false;
	    boolean first=true;
	    int toPix(int x,int y){
	        return ((x)*(yPix+2))+y+1;
	    }
	       /**
	     * Determines whether the specified point lies on tthis edge.
	     *
	     * @param p point to check
	     * @param TOL tolerance
	     * @return <code>true</code> if the specified point lies on this line
	     * segment; <code>false</code> otherwise
	     */
	    public boolean containsPoint(Vector3d p,Vertex p1 ,Vertex p2,double TOL) {

	        double x = p.x;
	        double x1 = p1.pos.x;
	        double x2 = p2.pos.x;

	        double y = p.y;
	        double y1 = p1.pos.y;
	        double y2 = p2.pos.y;

	        double AB = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1) );
	        double AP = Math.sqrt((x - x1) * (x - x1) + (y - y1) * (y - y1) );
	        double PB = Math.sqrt((x2 - x) * (x2 - x) + (y2 - y) * (y2 - y));

	        return Math.abs(AB - (AP + PB)) < TOL;
	    }
	        /**
	     * Contains.
	     *
	     * @param p the p
	     * @return true, if successful
	     */
	    public boolean polygonContains(Vector3d p,Polygon poly) {
	        // taken from http://www.java-gaming.org/index.php?topic=26013.0
	        // and http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
	        double px = p.x;
	        double py = p.y;
	        boolean oddNodes = false;
	        double x2 = poly.vertices.get(poly.vertices.size() - 1).pos.x;
	        double y2 = poly.vertices.get(poly.vertices.size() - 1).pos.y;
	        double x1, y1;
	        for (int i = 0; i < poly.vertices.size();  ++i) {
	            Vertex v1 = poly.vertices.get(i);
	        Vertex v2;
	        if(i<poly.vertices.size()-1){
	            v2=poly.vertices.get(i+1);
	        }else
	            v2=poly.vertices.get(0);
	        
	         if (containsPoint(p,v1,v2,0.0000001)) {
	             return true;
	         }
	            x1 = poly.vertices.get(i).pos.x;
	            y1 = poly.vertices.get(i).pos.y;
	            if (((y1 < py) && (y2 >= py))
	                    || (y1 >= py) && (y2 < py)) {
	                if ((py - y1) / (y2 - y1)
	                        * (x2 - x1) < (px - x1)) {
	                    oddNodes = !oddNodes;
	                }
	            }
	            x2 = x1;
	            y2 = y1;
	            
	        }
	        return oddNodes;
	    }
	/**
	     * An interface for slicking CSG objects into lists of points that can be extruded back out
	     * @param incoming            Incoming CSG to be sliced
	     * @param slicePlane          Z coordinate of incoming CSG to slice at
	     * @param normalInsetDistance Inset for sliced output
	     * @return                    A set of polygons defining the sliced shape
	     */
	    public List<Polygon> slice(CSG incoming, Transform slicePlane, double normalInsetDistance){
	      if(first){
	        new JFXPanel();
	      }
	      first=false;
	        List<Polygon> rawPolygons = new ArrayList<>();

	        // Actual slice plane
	        CSG planeCSG = incoming.getBoundingBox()
	                .toZMin();
	        // Loop over each polygon in the slice of the incoming CSG
	        // Add the polygon to the final slice if it lies entirely in the z plane
	        System.out. println ("Preparing CSG slice");
	        CSG slicePart =incoming
	                .transformed(slicePlane)
	                .intersect(planeCSG);
	        for(Polygon p: slicePart                        
	                .getPolygons()){
	            if(Slice.isPolygonAtZero(p)){
	                rawPolygons.add(p);
	            }
	        }
	        //BowlerStudioController.getBowlerStudio() .addObject((Object)slicePart.movez(1),(File)null)
	        //BowlerStudioController.getBowlerStudio() .addObject((Object)rawPolygons,(File)null)
	        double ratio = slicePart.getTotalY()/slicePart.getTotalX(); 
	        
	        //LengthParameter printerOffset           = new LengthParameter("printerOffset",0.5,[1.2,0]);
	        double scalePixel = 0.25;
	        double size = slicePart.getTotalX()/0.5/scalePixel;
	        if(slicePart.getTotalY()>slicePart.getTotalX() ){
	            size = slicePart.getTotalY()/0.5/scalePixel;
	            ratio = slicePart.getTotalX()/slicePart.getTotalY();
	        }
	        xPix = (int) (size*(ratio>1?1.0:ratio));
	        yPix = (int) (size*(ratio<1?1.0:ratio));
	        int pixels = (xPix+2)*(yPix+2);
	        double xOffset = slicePart.getMinX();
	        double yOffset = slicePart.getMinY();
	        double scale = slicePart.getTotalX()/xPix;
	        
	        boolean [] pix =new boolean [pixels];
	        System.out. println ("Image x=" +xPix+" by y="+yPix+" at x="+xOffset+" y="+yOffset);
	        long start = System.currentTimeMillis();
	        int imageOffset =20;
	        WritableImage obj_img = new WritableImage(xPix+imageOffset, yPix+imageOffset);
	        //int snWidth = (int) 4096;
	        //int snHeight = (int) 4096;

	        MeshView sliceMesh = slicePart.getMesh();
	        sliceMesh.getTransforms().add(javafx.scene.transform.Transform.translate(imageOffset/10, imageOffset/10));
	        AnchorPane anchor = new AnchorPane(sliceMesh);
	        AnchorPane.setBottomAnchor(sliceMesh, (double) 0);
	        AnchorPane.setTopAnchor(sliceMesh, (double) 0);
	        AnchorPane.setLeftAnchor(sliceMesh, (double) 0);
	        AnchorPane.setRightAnchor(sliceMesh, (double) 0);
	        Pane snapshotGroup = new Pane(anchor);
	        snapshotGroup.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));


	        SnapshotParameters snapshotParameters = new SnapshotParameters();
	        snapshotParameters.setTransform(new Scale(1/scale, 1/scale));
	        snapshotParameters.setDepthBuffer(true);
	        snapshotParameters.setFill(Color.TRANSPARENT);
	        done =false;
	        Runnable r =new Runnable() {
	            @Override
	            public void run() {
	                snapshotGroup.snapshot(snapshotParameters, obj_img);
	                done=true;
	            }
	        };
	        Platform.runLater(r);
	        while(done== false){
	            try {
                Thread.sleep(10);
              } catch (InterruptedException e) {
                throw new RuntimeException(e);
              }
	        }
	        
	            byte alpha = (byte) 0;
	        for(int i=0;i<xPix;i++){
	            for(int j=0;j<yPix;j++){
	                int color = obj_img.getPixelReader().getArgb(i, j);
	                if((color & 0xff000000)>0)
	                    pix[toPix(i,j)]=true;
	                else
	                    pix[toPix(i,j)]=false;
	            }
	        }
	        System.out. println ("Find boundries ");
	        
	    
	        
	        ImageView sliceImage = new ImageView(obj_img);
	        
	        sliceImage.getTransforms().add(javafx.scene.transform.Transform.translate(xOffset-imageOffset/10, yOffset-imageOffset/10));
	        sliceImage.getTransforms().add(javafx.scene.transform.Transform.scale(scale,scale ));
	        //BowlerStudioController.getBowlerStudio() .addNode(sliceImage);
	        //

	        double MMTOPX = 3.5409643774783404;
	        float outputScale = (float) (MMTOPX / scale);
	        // Options
	        HashMap<String, Float> options = new HashMap<String, Float>();

	        // Tracing
	        options.put("ltres", 1f);// Error treshold for
	                                    // straight lines.
	        options.put("qtres", 1f);// Error treshold for
	                                    // quadratic splines.
	        options.put("pathomit", 0.02f);// Edge node paths
	                                    // shorter than this
	                                    // will be discarded for
	                                    // noise reduction.

	        // Color quantization
	        options.put("colorsampling", 1f); // 1f means true ;
	                                            // 0f means
	                                            // false:
	                                            // starting with
	                                            // generated
	                                            // palette
	        options.put("numberofcolors", 16f);// Number of
	                                            // colors to use
	                                            // on palette if
	                                            // pal object is
	                                            // not defined.
	        options.put("mincolorratio", 0.02f);// Color
	                                            // quantization
	                                            // will
	                                            // randomize a
	                                            // color if
	                                            // fewer pixels
	                                            // than (total
	                                            // pixels*mincolorratio)
	                                            // has it.
	        options.put("colorquantcycles", 1f);// Color
	                                            // quantization
	                                            // will be
	                                            // repeated this
	                                            // many times.
	        //
	        // SVG rendering
	        options.put("scale", outputScale);// Every
	                                            // coordinate
	                                            // will be
	                                            // multiplied
	                                            // with this, to
	                                            // scale the
	                                            // SVG.
	        options.put("simplifytolerance", 1f);//
	        options.put("roundcoords", 2f); // 1f means rounded
	                                        // to 1 decimal
	                                        // places, like 7.3
	                                        // ; 3f means
	                                        // rounded to 3
	                                        // places, like
	                                        // 7.356 ; etc.
	        options.put("lcpr", 0f);// Straight line control
	                                // point radius, if this is
	                                // greater than zero, small
	                                // circles will be drawn in
	                                // the SVG. Do not use this
	                                // for big/complex images.
	        options.put("qcpr",0f);// Quadratic spline control
	                                // point radius, if this is
	                                // greater than zero, small
	                                // circles and lines will be
	                                // drawn in the SVG. Do not
	                                // use this for big/complex
	                                // images.
	        options.put("desc", 0f); // 1f means true ; 0f means
	                                    // false: SVG
	                                    // descriptions
	                                    // deactivated
	        options.put("viewbox", 1f); // 1f means true ; 0f
	                                    // means false: fixed
	                                    // width and height

	        // Selective Gauss Blur
	        options.put("blurradius", 0f); // 0f means
	                                        // deactivated; 1f
	                                        // .. 5f : blur with
	                                        // this radius
	        options.put("blurdelta", 20f); // smaller than this
	                                    // RGB difference
	                                    // will be blurred
	        System.out.print ("\nTracing...");
	        BufferedImage bi = SwingFXUtils.fromFXImage(obj_img,(BufferedImage)null);
	        try{
    	        String svg = ImageTracer.imageToSVG(bi,options,(byte[][])null);
    	        int headerStart = svg.indexOf(">")+1;
    	        int headerEnd = svg.lastIndexOf("<");
    	        //println "headerStart "+headerStart+ " headerEnd "+headerEnd
    	        String header = svg.substring(0,headerStart);
    	        String footer = svg.substring(headerEnd,svg.length());
    	        String body = svg.substring(headerStart,headerEnd);
    	        body = "<g id=\"g37\">\n"+body+"</g>\n";
    	        svg=header+body+footer;
    	        //println header+"\n\n"
    	        //println body+"\n\n"
    	        //println footer+"\n\n"
    	        File tmpsvg = new File( System.getProperty("java.io.tmpdir")+"/"+Math.random());
    	        tmpsvg.createNewFile();
    	        FileWriter fw = new FileWriter(tmpsvg.getAbsoluteFile());
    	        BufferedWriter bw = new BufferedWriter(fw);
    	        bw.write(svg);
    	        bw.close();
    	        Transform tr = new Transform()
    	                    .translate(xOffset-imageOffset/10, yOffset-imageOffset/10,0)
    	                    .scale(scale/28.3);
    	        List<Polygon>  svgPolys = SVGLoad.toPolygons(tmpsvg);
    	        for(Polygon P:svgPolys){
    	             P.transform(tr);
    	        }
    	        tmpsvg.delete();
    	        System.out.print( "Done Slicing! Took "+((double)(System.currentTimeMillis()-start)/1000.0)+"\n\n");
    	        svgPolys.remove(0);
    	        //println svg
    	        //BowlerStudioController.getBowlerStudio() .addObject((Object)svgPolys,(File)null)
    	        return  svgPolys;
	        }catch(Exception ex){
	          ex.printStackTrace();
	        }
	        return rawPolygons;
	    }
	};
	/**
	 * Returns true if this polygon lies entirely in the z plane
	 *
	 * @param polygon
	 *            The polygon to check
	 * @return True if this polygon is entirely in the z plane
	 */
	private static boolean isPolygonAtZero(Polygon polygon) {
		// Return false if there is a vertex in this polygon which is not at
		// zero
		// Else, the polygon is at zero if every vertex in it is at zero
		for (Vertex v : polygon.vertices)
			if (!isVertexAtZero(v))
				return false;

		return true;
	}

	/**
	 * Returns true if this vertex is at z coordinate zero
	 *
	 * @param vertex
	 *            The vertex to check
	 * @return True if this vertex is at z coordinate zero
	 */
	private static boolean isVertexAtZero(Vertex vertex) {
		// The upper and lower bounds for checking the vertex z coordinate
		// against
		final double SLICE_UPPER_BOUND = 0.001, SLICE_LOWER_BOUND = -0.001;

		// The vertex is at zero if it is within tight bounds (to account for
		// floating point error)
		return vertex.getZ() < SLICE_UPPER_BOUND && vertex.getZ() > SLICE_LOWER_BOUND;
	}

	public static List<Polygon> slice(CSG incoming, Transform slicePlane, double normalInsetDistance) {
		return getSliceEngine().slice(incoming, slicePlane, normalInsetDistance);
	}

	public static ISlice getSliceEngine() {
		return sliceEngine;
	}

	public static void setSliceEngine(ISlice sliceEngine) {
		Slice.sliceEngine = sliceEngine;
	}
}
