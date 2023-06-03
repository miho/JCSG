/**
 * Cylinder.java
 *
 * Copyright 2014-2014 Michael Hoffer info@michaelhoffer.de. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer info@michaelhoffer.de "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer info@michaelhoffer.de OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of Michael Hoffer
 * info@michaelhoffer.de.
 */
package eu.mihosoft.vrl.v3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.mihosoft.vrl.v3d.ext.org.poly2tri.PolygonUtil;
import eu.mihosoft.vrl.v3d.parametrics.LengthParameter;

// TODO: Auto-generated Javadoc
/**
 * A solid cylinder.
 *
 * The tessellation can be controlled via the {@link #numSlices} parameter.
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */

public class Toroid extends Primitive {

	/** The properties. */
	private final PropertyStorage properties = new PropertyStorage();
	List<Polygon> polys;
	public Toroid(double innerRadius, double OuterRadius) {
		this(innerRadius,OuterRadius,20,16);
	}
	public Toroid(double innerRadius, double OuterRadius, int numSlices, int facets) {
		if (innerRadius < 0)
			throw new RuntimeException("Inner radius must be positive");
		if (innerRadius >= OuterRadius)
			throw new RuntimeException("Outer radius must be larger than inner radius");
		double crossSecRad = OuterRadius - innerRadius;
		ArrayList<Vertex> vertices = new ArrayList<>();
		double f = facets;

		for (int i = 0; i < facets; i++) {
			double index = i;
			double rad = index / f * 2 * Math.PI;
			double a = Math.cos(rad) * crossSecRad;
			double b = Math.sin(rad) * crossSecRad;
			vertices.add(new Vertex(new Vector3d(a,b), new Vector3d(-1, 0,0)));
		}
		Polygon poly = new Polygon(vertices, properties);
		ArrayList<Polygon> slices = new ArrayList<Polygon>();

		for (int i = 0; i < numSlices; i++) {
			double angle = 360.0 / ((double) numSlices) * ((double) i);
			slices.add(poly.transformed(new Transform().movex(innerRadius+crossSecRad).roty(angle)));
		}
		List<Polygon> newPolygons = new ArrayList<>();
		for (int j = 0; j < slices.size(); j++) {
			int next = j + 1;
			if (next == slices.size())
				next = 0;
			// println "Extruding "+i+" to "+next
			Polygon polygon1=slices.get(j);
			Polygon polygon2=slices.get(next);
			if (polygon1.vertices.size() != polygon2.vertices.size()) {
				throw new RuntimeException("These polygons do not match");
			}
	
			int numvertices = polygon1.vertices.size();
			for (int i = 0; i < numvertices; i++) {
	
				int nexti = (i + 1) % numvertices;
	
				Vector3d bottomV1 = polygon1.vertices.get(i).pos;
				Vector3d topV1 = polygon2.vertices.get(i).pos;
				Vector3d bottomV2 = polygon1.vertices.get(nexti).pos;
				Vector3d topV2 = polygon2.vertices.get(nexti).pos;
	
				List<Vector3d> pPoints = Arrays.asList(bottomV2, topV2, topV1, bottomV1);
	
				newPolygons.add(Polygon.fromPoints(pPoints, polygon1.getStorage()));
	
			}
	
			polygon2 = polygon2.flipped();
	
		}
		polys = newPolygons;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.mihosoft.vrl.v3d.Primitive#toPolygons()
	 */
	@Override
	public List<Polygon> toPolygons() {
		return polys;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.mihosoft.vrl.v3d.Primitive#getProperties()
	 */
	@Override
	public PropertyStorage getProperties() {
		return properties;
	}

}
