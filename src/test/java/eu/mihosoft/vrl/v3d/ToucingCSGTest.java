package eu.mihosoft.vrl.v3d;

import static org.junit.Assert.*;

import org.junit.Test;

public class ToucingCSGTest {

	@Test
	public void test() {
		
		CSG cubeA = new Cube(	10,10,10).toCSG();
		CSG cubeb = cubeA.movex(2);
		CSG cubec = cubeA.movex(20);
		assertTrue(cubeA.touching(cubeb));
		assertFalse(cubeA.touching(cubec));
		

	}

}
