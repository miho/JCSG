package eu.mihosoft.vrl.v3d.svg;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.sun.javafx.geom.transform.Affine3D;
import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Polygon;
import eu.mihosoft.vrl.v3d.Slice;
import eu.mihosoft.vrl.v3d.Transform;
import eu.mihosoft.vrl.v3d.Vector3d;
import eu.mihosoft.vrl.v3d.Vertex;
import javafx.scene.paint.Color;

@SuppressWarnings("restriction")
public class SVGExporter {
	private String footer = "</svg>";
	private String section = "";
	//0.376975
	//public static final double Scale = 3.543307;// SVG px to MM scale facto
	public static final double Scale = 3.543307;
	private int colorTicker=0;
	private  List<String> colorNames = Arrays.asList("crimson","gray","black","darkmagenta","darkolivegreen","darkgreen",
			"darkblue",
			"deeppink",
			"chartreuse",
			"green",
			"orange",
			"lime",
			"tomato"); 
	double min[] = { 0, 0 };
	double max[] = { 0, 0 };
	private ArrayList<String> polylines= new ArrayList<>() ;
	private ArrayList<String> groups= new ArrayList<>() ;
	private ArrayList<String> layers= new ArrayList<>() ;
	private int layerCounter = 1;
	private int groupCounter = 1;
	private int lineCounter=0;
	private String name="";
	public SVGExporter(){
	
	}
	public String make(){
		makeLayer();// make the final group
		String output = "";
		for(String s:layers){
			output+=s+"\n";
		}
		
		String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
				+ "<svg    xmlns:dc=\"http://purl.org/dc/elements/1.1/\""+
   " xmlns:cc=\"http://creativecommons.org/ns#\""+
   " xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\""+
  " xmlns:svg=\"http://www.w3.org/2000/svg\""+
  " xmlns=\"http://www.w3.org/2000/svg\""+
  " xmlns:sodipodi=\"http://sodipodi.sourceforge.net/DTD/sodipodi-0.dtd\""+
  " xmlns:inkscape=\"http://www.inkscape.org/namespaces/inkscape\" version=\"1.1\" viewBox=\"" + (min[0] - 1) + " "
				+ (min[1] - 1) + " " + (Math.abs(max[0]) + Math.abs(min[0]) + 2) + " "+ 
				(Math.abs(max[1]) + Math.abs(min[1]) + 2) + "\""+ 
				" id=\"svg2\" "+
				 ">\n";
		header+= " <defs  \n"+
				"  id=\"defs4\" /> \n"+
				" <sodipodi:namedview \n"+
				"  id=\"base\" \n"+
				"  pagecolor=\"#ffffff\" \n"+
				"  bordercolor=\"#666666\" \n"+
				"  borderopacity=\"1.0\" \n"+
				" inkscape:pageopacity=\"0.0\" \n"+
				" inkscape:pageshadow=\"2\" \n"+
				"  inkscape:document-units=\"mm\" \n"+
				"  inkscape:current-layer=\"layer1\" \n"+
				" showgrid=\"false\" \n"+
				"  />\n";

		output += footer;
		output = header + output;
		return output;
	}
	
	private void colorTick(){
		colorTicker++;
		if(colorTicker==colorNames.size())
			colorTicker=0;
		makeLayer();
	}
	
	public void makeGroup(){
		if(polylines.size()==0)
			return;
		String  groupsLine=	"<g\nid=\"g37"+groupCounter+"\">\n"
				;
		for(String  p:polylines){
			groupsLine+=p+"\n";
		}
		groupsLine+="</g>";
		groupCounter++;
		groups.add(groupsLine);
		polylines.clear();
	}
	private void makeLayer(){
		makeGroup();
		if(groups.size()==0)
			return;
		String  groupsLine="<g\n"+
							"inkscape:label=\""+name+" Slice "+layerCounter+"\"\n"+
							"inkscape:groupmode=\"layer\" \n"+
							"id=\"layer"+layerCounter+"\" \n"+
							">"
				;
		for(String  p:groups){
			groupsLine+=p+"\n";
		}
		groupsLine+="</g>";
		layerCounter++;
		layers.add(groupsLine);
		groups.clear();
	}
	
	private  void toPolyLine(Polygon p){

		String color = colorNames.get(colorTicker);
		String section = "  <polyline points=\"";

		for (Vertex v : p.vertices) {
			Vector3d position = v.pos;
			double x = (position.x * Scale);
			double y = -(position.y * Scale);
			section += x + "," + y + " ";
			if (x > max[0]) {
				max[0] = x;
			}
			if (x < min[0]) {
				min[0] = x;
			}
			if (y > max[1]) {
				max[1] = y;
			}
			if (y < min[1]) {
				min[1] = y;
			}
		}
		// Close loop
		Vector3d position = p.vertices.get(0).pos;
		double x = (position.x * Scale);
		double y = -(position.y * Scale);
		section += x + "," + y + " ";
		section= section + "\" \nstroke=\""+color+"\" \nstroke-width=\"1\" \nfill=\"none\"\nid=\"line"+(lineCounter++)+"\" />\n";
		polylines.add(section);
	}

//	public static void export(List<Polygon> polygons, File defaultDir) throws IOException {
//		export(polygons,defaultDir,true);
//	}
	public static void export(List<Polygon> polygons, File defaultDir, boolean groupAll) throws IOException {
		SVGExporter svg = new SVGExporter();
		
		for( Polygon p: polygons){
			svg.toPolyLine(p);
			if(!groupAll)
				svg.colorTick();
		}

		write(svg.make(), defaultDir);
	}
	private static void write(String output, File defaultDir) throws IOException{
		// if file doesnt exists, then create it
		if (!defaultDir.exists()) {
			defaultDir.createNewFile();
		}
		FileWriter fw = new FileWriter(defaultDir.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(output);
		bw.close();
	}
	public static void export(CSG currentCsg, File defaultDir) throws IOException {
		SVGExporter svg = new SVGExporter();
		addCsg(currentCsg,svg);
		write(svg.make(), defaultDir);
	}
	public static void export(List<CSG> currentCsg, File defaultDir) throws IOException {
		SVGExporter svg = new SVGExporter();
		int i=0;
		long start = System.currentTimeMillis();
		for(CSG tmp:currentCsg){
			System.out.println("Slicing CSG "+tmp.getName()+" "+(i+1)+" of "+(currentCsg.size()));
			addCsg(tmp,svg);
			i++;
		}
		
		write(svg.make(), defaultDir);
		System.out.println("Finished slicing CSGs took "+((((double)(System.currentTimeMillis()-start)))/1000.0)+" seconds");
	}
	private static void addCsg(CSG currentCsg, SVGExporter svg) throws IOException {
		svg.setName(currentCsg.getName());
		for(Transform slicePlane:currentCsg.getSlicePlanes()){
			List<Polygon> polygons = Slice.slice(currentCsg.prepMfg(), slicePlane, 0);
			for( Polygon p: polygons){
				svg.toPolyLine(p );
			}
			svg.colorTick();// group the polygons from the single CSG together by layer
		}

	}
	private void setName(String name) {
		if(name == null)
			return;
		this.name = name;	
	}

}
