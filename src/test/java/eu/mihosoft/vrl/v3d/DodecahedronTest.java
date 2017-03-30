package eu.mihosoft.vrl.v3d;

import static org.junit.Assert.*;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

import eu.mihosoft.vrl.v3d.FileUtil;


public class DodecahedronTest {

	@Test
	public void test() throws IOException {
		double radius = 10;
		
		CSG dodecahedron = new Dodecahedron(radius).toCSG();
		CSG box = new Cube(3*radius).toCSG().difference(new Cube(2*radius).toCSG());
		CSG insphere = new Sphere(0.794654472292*radius).toCSG();
		
		assertTrue(dodecahedron.intersect(box).getPolygons().size() == 0);
		assertTrue(insphere.difference(dodecahedron).getPolygons().size() == 0);
		
		FileUtil.write(Paths.get("dodecahedron.stl"),
			dodecahedron.toStlString());
	}

}
