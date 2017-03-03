package eu.mihosoft.vrl.v3d;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.QuadCurveTo;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.paint.Color;

// TODO: Auto-generated Javadoc
/**
 * The Class Text.
 */

@SuppressWarnings("restriction")
public class TextExtrude {
	private static final String default_font = "FreeSerif";
	private final static int POINTS_CURVE = 10;

	private final String text;
	private List<Vector3d> points;
	private Vector3d p0;
	private final List<LineSegment> polis = new ArrayList<>();
	ArrayList<CSG> sections = new ArrayList<CSG>();
	ArrayList<CSG> holes = new ArrayList<CSG>();
	private double dir;

	class LineSegment {

		/*
		 * Given one single character in terms of Path, LineSegment stores a
		 * list of points that define the exterior of one of its polygons
		 * (!isHole). It can contain reference to one or several holes inside
		 * this polygon. Or it can define the perimeter of a hole (isHole), with
		 * no more holes inside.
		 */

		private boolean hole;
		private List<Vector3d> points;
		private Path path;
		private Vector3d origen;
		private List<LineSegment> holes = new ArrayList<>();
		private String letter;

		public LineSegment(String text) {
			letter = text;
		}

		public String getLetter() {
			return letter;
		}

		public void setLetter(String letter) {
			this.letter = letter;
		}

		public boolean isHole() {
			return hole;
		}

		public void setHole(boolean isHole) {
			this.hole = isHole;
		}

		public List<Vector3d> getPoints() {
			return points;
		}

		public void setPoints(List<Vector3d> points) {
			this.points = points;
		}

		public Path getPath() {
			return path;
		}

		public void setPath(Path path) {
			this.path = path;
		}

		public Vector3d getOrigen() {
			return origen;
		}

		public void setOrigen(Vector3d origen) {
			this.origen = origen;
		}

		public List<LineSegment> getHoles() {
			return holes;
		}

		public void setHoles(List<LineSegment> holes) {
			this.holes = holes;
		}

		public void addHole(LineSegment hole) {
			holes.add(hole);
		}

		@Override
		public String toString() {
			return "Poly{" + "points=" + points + ", path=" + path + ", origen=" + origen + ", holes=" + holes + '}';
		}
	}

	private TextExtrude(String text, Font font, double dir) {
		this.dir = dir;
		points = new ArrayList<>();
		this.text=text;
		Text textNode = new Text(text);
		textNode.setFont(font);

		// Convert Text to Path
		Path subtract = (Path) (Shape.subtract(textNode, new Rectangle(0, 0)));
		// Convert Path elements into lists of points defining the perimeter
		// (exterior or interior)
		subtract.getElements().forEach(this::getPoints);

		// Group exterior polygons with their interior polygons
//		polis.stream().filter(LineSegment::isHole).forEach(hole -> {
//			polis.stream().filter(poly -> !poly.isHole())
//					.filter(poly -> !((Path) Shape.intersect(poly.getPath(), hole.getPath())).getElements().isEmpty())
//					.filter(poly -> poly.getPath().contains(new Point2D(hole.getOrigen().x, hole.getOrigen().y)))
//					.forEach(poly -> poly.addHole(hole));
//		});
		//polis.removeIf(LineSegment::isHole);
		
		for (int i = 0; i < sections.size(); i++) {
			for (CSG h : holes) {
				try {
					if (sections.get(i).touching(h)) {
						// println "Hole found "
						CSG nl = sections.get(i).difference(h);

						sections.set(i, nl);
					}
				} catch (Exception e) {

				}
			}
		}
	}

	/**
	 * Extrudes the specified path (convex or concave polygon without holes or
	 * intersections, specified in CCW) into the specified direction.
	 *
	 * @param dir
	 *            direction of extrusion
	 * @param text
	 *            text
	 * @param font
	 *            font configuration of the text
	 *
	 * @return a CSG object that consists of the extruded polygon
	 */
	@SuppressWarnings("restriction")
	public static ArrayList<CSG> text(double dir, String text, Font font) {

		TextExtrude te = new TextExtrude(text, font, dir);

		return te.sections;
	}

	 public List<LineSegment> getLineSegment() {        
	        return polis; 
	    }
	    
	    public List<Vector3d> getOffset(){
	        return polis.stream().sorted((p1,p2)->(int)(p1.getOrigen().x-p2.getOrigen().x))
	                .map(LineSegment::getOrigen).collect(Collectors.toList());
	    }
	    
	    private void getPoints(PathElement elem){
	        if(elem instanceof MoveTo){
	        	loadPoints();
	            p0=new Vector3d((float)((MoveTo)elem).getX(),(float)((MoveTo)elem).getY(),0f);
	            points.add(p0);
	        } else if(elem instanceof LineTo){
	            points.add(new Vector3d((float)((LineTo)elem).getX(),(float)((LineTo)elem).getY(),0f));
	        } else if(elem instanceof CubicCurveTo){
	            Vector3d ini = (points.size()>0?points.get(points.size()-1):p0);
	            IntStream.rangeClosed(1, POINTS_CURVE).forEach(i->points.add(evalCubicBezier((CubicCurveTo)elem, ini, ((double)i)/POINTS_CURVE)));
	        } else if(elem instanceof QuadCurveTo){
	            Vector3d ini = (points.size()>0?points.get(points.size()-1):p0);
	            IntStream.rangeClosed(1, POINTS_CURVE).forEach(i->points.add(evalQuadBezier((QuadCurveTo)elem, ini, ((double)i)/POINTS_CURVE)));
	        } else if(elem instanceof ClosePath){
	            points.add(p0);
	            // Every closed path is a polygon (exterior or interior==hole)
	            // the text, the list of points and a new path between them are
	            // stored in a LineSegment: a continuous line that can change direction
	            if(Math.abs(getArea())>0.001){
	                LineSegment line = new LineSegment(text);
	                line.setHole(isHole());
	                line.setPoints(points);
	                line.setPath(generatePath());
	                line.setOrigen(p0);
	                polis.add(line);
	            }
	            loadPoints();
	            
	        } 
	    }
	    
	    private void loadPoints(){
        	if(points.size()>4){
	        	points.remove(points.size() - 1);
				//points.remove(points.size() - 1);
				boolean hole = Extrude.isCCW(Polygon.fromPoints(points));
				CSG newLetter = Extrude.points(new Vector3d(0, 0, dir), points);

				if (!hole)
					sections.add(newLetter);
				else
					holes.add(newLetter);
        	}
            points=new ArrayList<>();
	    }
	    
	    private Vector3d evalCubicBezier(CubicCurveTo c, Vector3d ini, double t){
	        Vector3d p=new Vector3d((float)(Math.pow(1-t,3)*ini.x+
	                3*t*Math.pow(1-t,2)*c.getControlX1()+
	                3*(1-t)*t*t*c.getControlX2()+
	                Math.pow(t, 3)*c.getX()),
	                (float)(Math.pow(1-t,3)*ini.y+
	                3*t*Math.pow(1-t, 2)*c.getControlY1()+
	                3*(1-t)*t*t*c.getControlY2()+
	                Math.pow(t, 3)*c.getY()),
	                0f);
	        return p;
	    }
	    
	    private Vector3d evalQuadBezier(QuadCurveTo c, Vector3d ini, double t){
	        Vector3d p=new Vector3d((float)(Math.pow(1-t,2)*ini.x+
	                2*(1-t)*t*c.getControlX()+
	                Math.pow(t, 2)*c.getX()),
	                (float)(Math.pow(1-t,2)*ini.y+
	                2*(1-t)*t*c.getControlY()+
	                Math.pow(t, 2)*c.getY()),
	                0f);
	        return p;
	    }
	    
	    private double getArea(){
	        DoubleProperty res=new SimpleDoubleProperty();
	        IntStream.range(0, points.size()-1)
	                .forEach(i->res.set(res.get()+points.get(i).cross(points.get(i+1)).z));
	        // System.out.println("path: "+res.doubleValue()/2);
	        
	        return res.doubleValue()/2d;
	    }
	    
	    private boolean isHole(){
	        // area>0 -> the path is a hole, clockwise (y up)
	        // area<0 -> the path is a polygon, counterclockwise (y up)
	        return getArea()>0;
	    }
	    
	    private Path generatePath(){
	        Path path = new Path(new MoveTo(points.get(0).x,points.get(0).y));
	        points.stream().skip(1).forEach(p->path.getElements().add(new LineTo(p.x,p.y)));
	        path.getElements().add(new ClosePath());
	        path.setStroke(Color.GREEN);
	        // Path must be filled to allow Shape.intersect
	        path.setFill(Color.RED);
	        return path;
	    }
}
