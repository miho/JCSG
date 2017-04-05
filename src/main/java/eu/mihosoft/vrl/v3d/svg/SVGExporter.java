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

	private static final double Scale = 3.543335;// SVG px to MM scale factor

	public static void export(CSG currentCsg, File defaultDir) throws IOException {
		Transform slicePlane = new Transform();

		List<Polygon> polygons = Slice.slice(currentCsg, slicePlane, 0);
		export(polygons, defaultDir);
	}

	public static void export(List<Polygon> polygons, File defaultDir) throws IOException {
		String footer = "</svg>";
		String section = "";
		String output = "";
		double min[] = { 0, 0 };
		double max[] = { 0, 0 };
		for (Polygon p : polygons) {
			section = "  <polyline points=\"";

			for (Vertex v : p.vertices) {
				Vector3d position = v.pos;
				double x = (position.x * Scale);
				double y = (position.y * Scale);
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
			double y = (position.y * Scale);
			section += x + "," + y + " ";
			output += section + "\" stroke=\"red\" stroke-width=\"0.1\" fill=\"none\" />\n";

		}
		String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
				+ "<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" viewBox=\"" + (min[0] - 1) + " "
				+ (min[1] - 1) + " " + (Math.abs(max[0]) + Math.abs(min[0]) + 2) + " "
				+ (Math.abs(max[1]) + Math.abs(min[1]) + 2) + "\">\n";

		output += footer;
		output = header + output;

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
