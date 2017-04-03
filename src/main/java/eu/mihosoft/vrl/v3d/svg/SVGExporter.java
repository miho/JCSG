package eu.mihosoft.vrl.v3d.svg;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Polygon;
import eu.mihosoft.vrl.v3d.Slice;
import eu.mihosoft.vrl.v3d.Transform;
import eu.mihosoft.vrl.v3d.Vector3d;
import eu.mihosoft.vrl.v3d.Vertex;


@SuppressWarnings("restriction")
public class SVGExporter {


	public static void export(CSG currentCsg, File defaultDir) throws IOException {
		Transform slicePlane = new Transform();

		List<Polygon> polygons = Slice.slice(currentCsg, slicePlane , 0);
		export(polygons,defaultDir);
	}
	
	public static void export(List<Polygon> polygons , File defaultDir) throws IOException {
		

		String header="<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"+
		"<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">\n";
		String footer="</svg>";
		String section	="";	
		String output = header;
		for(Polygon p:polygons){
			section = "  <polyline points=\"";
			double Scale = 25.4 / 96.0;
			for (Vertex v:p.vertices){
				Vector3d position = v.pos;
				section+=((int)position.x*Scale)+","+((int)position.y*Scale)+" ";
			}
			
			output+=section+"\" stroke=\"red\" stroke-width=\"1\" fill=\"none\" />\n";
		}
		output+=footer;
		
		// if file doesnt exists, then create it
		if (!defaultDir.exists()) {
			defaultDir.createNewFile();
		}
		FileWriter fw = new FileWriter(defaultDir.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(output);
		bw.close();
	}


}
