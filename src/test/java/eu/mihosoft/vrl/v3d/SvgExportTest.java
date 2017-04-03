package eu.mihosoft.vrl.v3d;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import eu.mihosoft.vrl.v3d.svg.SVGExporter;

public class SvgExportTest {

	@Test
	public void slicetest() throws IOException {
		double normalInsetDistance=0;
		Transform slicePlane = new Transform();
		
		CSG main = new Cube(400, // X dimention
				400, // Y dimention
				400// Z dimention
		).toCSG();// this converts from the geometry to an object we can work
					// with

		CSG cut = new Cube(200, // X dimention
				200, // Y dimention
				200// Z dimention
		).toCSG();// this converts from the geometry to an object we can work
					// with
		
		CSG incoming=main.difference(cut).intersect(new Cube(400, 400, 2).toCSG());
		
		List<Polygon> polygons = Slice.slice(incoming, slicePlane , normalInsetDistance);
		
		SVGExporter.export(polygons, new File("SVGExportTest.svg"));
		
	}

	@Test
	public void test() throws IOException {

		
		List<Polygon> polygons = new ArrayList<Polygon>();
		
		List<Vertex> vertices = new ArrayList<Vertex>();
		vertices.add(new Vertex(new Vector3d(-30, 0), new Vector3d(0, 0)));
		vertices.add(new Vertex(new Vector3d(100, 0), new Vector3d(0, 0)));
		vertices.add(new Vertex(new Vector3d(100, 100), new Vector3d(0, 0)));
		vertices.add(new Vertex(new Vector3d(-30, 100), new Vector3d(0, 0)));

		List<Vertex> vertices2 = new ArrayList<Vertex>();
		vertices2.add(new Vertex(new Vector3d(50, 50), new Vector3d(0, 0)));
		vertices2.add(new Vertex(new Vector3d(75, 50), new Vector3d(0, 0)));
		vertices2.add(new Vertex(new Vector3d(75, 75), new Vector3d(0, 0)));
		vertices2.add(new Vertex(new Vector3d(50, 75), new Vector3d(0, 0)));

		
		Polygon outline2 = new Polygon(vertices2 );
		Polygon outline = new Polygon(vertices );
		polygons.add(outline2);
		polygons.add(outline);
		SVGExporter.export(polygons, new File("SVGExportTest2.svg"));
		
	}
}
