package eu.mihosoft.vrl.v3d;

import static org.junit.Assert.*;

import org.junit.Test;

public class Objtest {

	@Test
	public void test() {
		String s = new Cube (1).toCSG().toObjString()
				.split("# Faces")[1]
				.split("# End")[0].trim();
		assertFalse(s.length()<4);		
	}

}
