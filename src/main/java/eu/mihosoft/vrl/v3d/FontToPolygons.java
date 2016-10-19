package eu.mihosoft.vrl.v3d;

import com.interactivemesh.j3d.community.utils.geometry.AWTShapeExtruder;
import com.interactivemesh.j3d.community.utils.geometry.AWTShapeExtrusion;
import com.interactivemesh.j3d.community.utils.geometry.String3D;
import static com.interactivemesh.j3d.community.utils.geometry.String3D.Alignment.CENTER;
import static com.interactivemesh.j3d.community.utils.geometry.String3D.Path.RIGHT;
import eu.mihosoft.ext.j3d.Font3DUtil;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import eu.mihosoft.ext.j3d.javax.vecmath.Point3f;
import java.awt.geom.GeneralPath;

public class FontToPolygons {

    private FontToPolygons() {
        throw new AssertionError("Don't instantiate me!");
    }

    public static List<Polygon> charToPolygons(char c, Font font, double depth, double resolution) {

        GeneralPath extrPath = new GeneralPath();

        float extend = 0.0f;
        float depth1 = 5.0f;
        float cut = depth1 / 50.0f;

        extrPath.moveTo(0.0f, 0.0f);
        extrPath.lineTo(cut, extend);
        extrPath.lineTo(depth1 - cut, extend);
        extrPath.lineTo(depth1, 0.0f);

        AWTShapeExtrusion extrusion = new AWTShapeExtrusion(extrPath); // or 300
        AWTShapeExtruder extruder = new AWTShapeExtruder(0.15, extrusion, Math.toRadians(15));

//        Font font = new Font("Dialog", Font.PLAIN, 100);
        String3D string3D = new String3D(font, extruder);

        // 3D
        string3D.setPosition(new Point3f(0, 0, 100));
        string3D.setCharacterSpacing(0f);
        string3D.setAlignment(CENTER);
        string3D.setPath(RIGHT);

        return toPolygons(Font3DUtil.charToTriangles(c, font, depth, resolution));
//        String3D s3d = string3D;
//        TriangleArray ta = (TriangleArray) s3d.getStringGeometry(s);
//        double[] taVerts = new double[ta.getVertexCount()];
//        ta.getCoordinates(0, taVerts);
//        return toPolygons(taVerts);
    }

    private static List<Polygon> toPolygons(double[] vertices) {

        List<Polygon> polygons
                = new ArrayList<>(vertices.length / 3);

        Polygon p = null;

        System.out.println("verts: " + vertices.length);

        for (int i = 0; i < vertices.length / 3 - 2; i += 3) {

            int i1 = i * 3 + 0;
            int i2 = (i + 1) * 3 + 0;
            int i3 = (i + 2) * 3 + 0;

            System.out.println("i1: " + i1);
            System.out.println("i2: " + i2);
            System.out.println("i3: " + i3);

            p = Polygon.fromPoints(
                    new Vector3d(
                            vertices[i * 3 + 0],
                            vertices[i * 3 + 1],
                            vertices[i * 3 + 2]),
                    new Vector3d(
                            vertices[(i + 1) * 3 + 0],
                            vertices[(i + 1) * 3 + 1],
                            vertices[(i + 1) * 3 + 2]),
                    new Vector3d(
                            vertices[(i + 2) * 3 + 0],
                            vertices[(i + 2) * 3 + 1],
                            vertices[(i + 2) * 3 + 2])
            );

            if (p.vertices.get(0).pos.equals(Vector3d.ZERO)
                    && p.vertices.get(1).pos.equals(Vector3d.ZERO)
                    && p.vertices.get(2).pos.equals(Vector3d.ZERO)) {
                continue;
            }

//            p = new Polygon(
//                    new Vertex(
//                            new Vector3d(
//                                    vertices[i * 3 + 0],
//                                    vertices[i * 3 + 1],
//                                    vertices[i * 3 + 2]),
//                            Vector3d.ZERO),
//                    new Vertex(
//                            new Vector3d(
//                                    vertices[(i + 1) * 3 + 0],
//                                    vertices[(i + 1) * 3 + 1],
//                                    vertices[(i + 1) * 3 + 2]),
//                            Vector3d.ZERO),
//                    new Vertex(
//                            new Vector3d(
//                                    vertices[(i + 2) * 3 + 0],
//                                    vertices[(i + 2) * 3 + 1],
//                                    vertices[(i + 2) * 3 + 2]),
//                            Vector3d.ZERO)
//            );
            polygons.add(p);
        }

        return polygons;
    }
}
