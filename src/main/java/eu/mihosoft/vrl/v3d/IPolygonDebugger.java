package eu.mihosoft.vrl.v3d;

import java.util.Arrays;
import java.util.List;

public interface IPolygonDebugger {
	void display( List<Polygon> poly);
	default void  display( Polygon... poly) {
		display(Arrays.asList(poly));
	}
}
