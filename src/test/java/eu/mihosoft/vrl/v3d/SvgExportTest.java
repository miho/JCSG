package eu.mihosoft.vrl.v3d;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import eu.mihosoft.vrl.v3d.svg.SVGExporter;

public class SvgExportTest {

	@Test
	public void slicetest() throws IOException {
		double normalInsetDistance = 0;
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

		CSG incoming = main.difference(cut).intersect(new Cube(400, 400, 2).toCSG());

		List<Polygon> polygons = Slice.slice(incoming, slicePlane, normalInsetDistance);

		SVGExporter.export(polygons, new File("SVGExportTest.svg"), false);

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

		Polygon outline2 = new Polygon(vertices2);
		Polygon outline = new Polygon(vertices);
		polygons.add(outline2);
		polygons.add(outline);
		SVGExporter.export(polygons, new File("SVGExportTest2.svg"), false);

	}

	@Test
	public void testSlices() throws IOException {

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

		Polygon outline2 = new Polygon(vertices2);
		Polygon outline = new Polygon(vertices);
		polygons.add(outline2);
		polygons.add(outline);

		SVGExporter.export(polygons, new File("SVGExportTest3.svg"), false);

	}

	@Test
	public void testCSGSlices() throws IOException {

		CSG carrot = new Cube(10, 10, 10).toCSG()
				// .toXMin()
				.difference(new Cube(4, 4, 100).toCSG()
		// .toXMin()
		);
		// .roty(30)
		// .rotx(30)

		Transform slicePlane = new Transform();
		slicePlane.rotY(30);
		slicePlane.rotX(30);
		carrot.addSlicePlane(new Transform());
		carrot.addSlicePlane(slicePlane);
		
		SVGExporter.export(carrot, new File("SVGExportTest4.svg"));
	}	
	@Test
	public void testManyCSGSlices() throws IOException {

		CSG carrot = new Cube(10, 10, 10).toCSG()
				.difference(new Cube(4, 4, 100).toCSG()
		);

		Transform slicePlane = new Transform();
		slicePlane.rotY(30);
		slicePlane.rotX(30);
		carrot.addSlicePlane(new Transform());
		carrot.addSlicePlane(slicePlane);
		
		CSG sphere = new Sphere(10,80,80)
						.setCenter(new Vector3d(30, 30))
						.toCSG();
		for(int i=0;i<10;i++){
			Transform sp = new Transform();
			sp.translateZ(0.9*i);
			sphere.addSlicePlane(sp);
		}
		carrot.setName("Square Sections");
		sphere.setName("Circle Sections");
		SVGExporter.export(Arrays.asList(carrot,sphere), new File("SVGExportTest5.svg"));
	}
}
