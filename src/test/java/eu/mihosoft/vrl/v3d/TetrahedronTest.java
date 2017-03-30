package eu.mihosoft.vrl.v3d;

import static org.junit.Assert.*;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

import eu.mihosoft.vrl.v3d.FileUtil;


public class TetrahedronTest {

	@Test
	public void test() throws IOException {
		double radius = 10;
		
		CSG tetrahedron = new TetrahedronTest(radius).toCSG();
		CSG box = new Cube(3*radius).toCSG().difference(new Cube(2*radius).toCSG());
		CSG insphere = new Sphere(Math.sqrt(6)/6*radius).toCSG();
		
		assertTrue(tetrahedron.intersect(box).getPolygons().size() == 0);
		assertTrue(insphere.difference(tetrahedron).getPolygons().size() == 0);
		
		FileUtil.write(Paths.get("tetrahedron.stl"),
			tetrahedron.toStlString());
	}

}
