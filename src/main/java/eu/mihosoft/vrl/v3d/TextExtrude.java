package eu.mihosoft.vrl.v3d;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.PathIterator;
import java.util.ArrayList;

// TODO: Auto-generated Javadoc
/**
 * The Class Text.
 */

public class TextExtrude {
	private static final String default_font = "FreeSerif";

	/**
	 * Extrudes the specified path (convex or concave polygon without holes or
	 * intersections, specified in CCW) into the specified direction.
	 *
	 * @param dir
	 *            direction of extrusion
	 * @param text
	 *            text
	 * @param font
	 *            font configuration of the text
	 *
	 * @return a CSG object that consists of the extruded polygon
	 */
	public static ArrayList<CSG> text(double dir, String text, Font font) {

		String default_font = "FreeSerif";
		String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

		boolean found = false;
		for (String f : fonts) {
			if (f.contentEquals(font.getFontName())) {
				found = true;

			}
		}
		if (!found) {
			for (String f : fonts) {

				System.out.println(f);

			}
			System.out.println("Font not found! " + font.getFontName() + " using " + default_font);
			font = new Font(default_font, font.getStyle(), font.getSize());
		}
		FontRenderContext frc = new FontRenderContext(null, (boolean) true, (boolean) true);
		TextLayout textLayout = new TextLayout(text, font, frc);
		Shape s = textLayout.getOutline(null);

		PathIterator pi = s.getPathIterator(null);
		ArrayList<Vector3d> points = new ArrayList<Vector3d>();

		int connectorDepth = 10;

		float[] coords = new float[6];
		float[] start = new float[6];

		ArrayList<CSG> sections = new ArrayList<CSG>();
		ArrayList<CSG> holes = new ArrayList<CSG>();

		float tmp = 0, tmp1 = 0;
		while (pi.isDone() == (boolean) false) {
			coords = new float[6];
			int type = pi.currentSegment(coords);
			switch (type) {
			case PathIterator.SEG_CLOSE:
				points.add(new Vector3d(start[0], start[1], 0));

				if (points.size() > 3) {
					try {
						points.remove(points.size() - 1);
						points.remove(points.size() - 1);
						boolean hole = !Extrude.isCCW(Polygon.fromPoints(points));
						CSG newLetter = Extrude.points(new Vector3d(0, 0, connectorDepth), points);

						if (!hole)
							sections.add(newLetter);
						else
							holes.add(newLetter);
						// ThreadUtil.wait(10)
						// stringOut = new Sphere(2).toCSG();
						// return stringOut;
					} catch (NullPointerException e) {
						e.printStackTrace();
					}
				}
				points = new ArrayList<Vector3d>();
				break;
			case PathIterator.SEG_QUADTO:
				// println "SEG_QUADTO from ( "+coords[0]+" , "+coords[1]+" ) to
				// ( "+coords[2]+" , "+coords[3]+" )";
				for (float t = 0.05f; t <= 1.0f; t += 0.05f) {
					// (1-t)²*P0 + 2t*(1-t)*P1 + t²*P2
					float u = (1.0f - t);
					float tt = u * u;
					float ttt = 2.0f * t * u;
					float tttt = t * t;
					float p1 = tmp * tt + (coords[0] * ttt) + (coords[2] * tttt);
					float p2 = tmp1 * tt + (coords[1] * ttt) + (coords[3] * tttt);
					points.add(new Vector3d(p1, p2, 0));
					// println "SEG_QUADTO "+p1+" and "+p2
				}
				tmp = coords[2];
				tmp1 = coords[3];
				points.add(new Vector3d(tmp, tmp1, 0));
				break;
			case PathIterator.SEG_LINETO:
				// println "SEG_LINETO "+coords
				tmp = coords[0];
				tmp1 = coords[1];
				points.add(new Vector3d(tmp, tmp1, 0));
				break;
			case PathIterator.SEG_MOVETO:

				// move without drawing
				start[0] = tmp = coords[0];
				start[1] = tmp1 = coords[1];
				// println "Moving to "+start
				points.add(new Vector3d(tmp, tmp1, 0));
				break;
			case PathIterator.SEG_CUBICTO:
				for (float t = 0.0f; t <= 1.05f; t += 0.1f) {
					// p = a0 + a1*t + a2 * tt + a3*ttt;
					float tt = t * t;
					float ttt = tt * t;
					float p1 = tmp + (coords[0] * t) + (coords[2] * tt) + (coords[4] * ttt);
					float p2 = tmp1 + (coords[1] * t) + (coords[3] * tt) + (coords[5] * ttt);
					points.add(new Vector3d(p1, p2, 0));
					// println "SEG_CUBICTO "+p1+" and "+p2
				}
				tmp = coords[4];
				tmp1 = coords[5];
				break;

			default:
				throw new RuntimeException("Unknown iterator type: " + type);
			}
			pi.next();
			// println "pi.isDone() "+pi.isDone()
		}
		for (int i = 0; i < sections.size(); i++) {
			for (CSG h : holes) {
				try {
					if (sections.get(i).touching(h)) {
						// println "Hole found "
						CSG nl = sections.get(i).difference(h);

						sections.set(i, nl);
					}
				} catch (Exception e) {

				}
			}
		}
		return sections;
	}

}
