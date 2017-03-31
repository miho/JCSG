package eu.mihosoft.vrl.v3d;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class SvgExportTest {

	@Test
	public void test() {
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
		
		
		for(Polygon p:polygons){
			for (Vertex v:p.vertices){
				Vector3d position = v.pos;
				// load this point into the SVG
				
			}
		}
		
	}

}
