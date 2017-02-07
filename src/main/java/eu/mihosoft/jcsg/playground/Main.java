/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg.playground;

import eu.mihosoft.jcsg.Bounds;
import eu.mihosoft.jcsg.CSG;
import eu.mihosoft.jcsg.Polygon;
import eu.mihosoft.jcsg.Sphere;
import eu.mihosoft.jcsg.Vertex;
import eu.mihosoft.vvecmath.Vector3d;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Michael Hoffer (info@michaelhoffer.de)
 */
public class Main {

    public static final double EPS = 1e-8;

    public static void main(String[] args) throws IOException {

        CSG c1 = new Sphere(Vector3d.zero(), 1.0, 32, 32).toCSG();
        CSG c2 = new Sphere(Vector3d.x(0.75), 1.0, 32, 32).toCSG();

        SplitResult result1 = splitPolygons(c1.getPolygons(), c2.getPolygons(),
                c1.getBounds(), c2.getBounds());
        SplitResult result2 = splitPolygons(c2.getPolygons(), c1.getPolygons(),
                c2.getBounds(), c1.getBounds());

        result1.p2.addAll(result2.p2);

        CSG result = CSG.fromPolygons(result1.p2);

        Files.write(Paths.get("test.stl"), result.toStlString().getBytes());

    }

    public static SplitResult splitPolygons(
            List<Polygon> ps1,
            List<Polygon> ps2,
            Bounds b1, Bounds b2) {

        SplitResult result = new SplitResult();
        List<Polygon> ps2WithCuts = new ArrayList<>(ps2);

        for (Polygon p1 : ps1) {

            // return early if polygon bounds do not intersect object bounds
            if (!p1.getBounds().intersects(b2)) {
                continue;
            }

            List<Polygon> cutsWithP1 = new ArrayList<>();
            List<Polygon> p2ToDelete = new ArrayList<>();
            for (Polygon p2 : ps2WithCuts) {

                // return early if polygon bounds do not intersect other polygon bound
                if (!p1.getBounds().intersects(p2.getBounds())) {
                    continue;
                }

                // compute distance of polygon 1 vertices to polygon 2 plane
                int[] typesP1 = new int[p1.vertices.size()];

                boolean typesEqual1 = true;
                int prevType1 = 0;
                for (int i = 0; i < p1.vertices.size(); i++) {
                    typesP1[i] = p2.plane.compare(p1.vertices.get(i).pos, EPS);

                    if (i > 0 && typesEqual1) {
                        typesEqual1 &= prevType1 == typesP1[i];
                    }

                    prevType1 = typesP1[i];
                }

                // planes do not intersect, thus polygons do not intersect
                if (typesEqual1) {
                    continue;
                }
                // compute distance of polygon 2 vertices to polygon 1 plane
                int[] typesP2 = new int[p2.vertices.size()];

                boolean typesEqual2 = true;
                int prevType2 = 0;

                for (int i = 0; i < p2.vertices.size(); i++) {
                    typesP2[i] = p1.plane.compare(p2.vertices.get(i).pos, EPS);

                    if (i > 0 && typesEqual2) {
                        typesEqual2 &= prevType2 == typesP2[i];
                    }

                    prevType2 = typesP2[i];
                }

                // planes do not intersect, thus polygons do not intersect
                if (typesEqual2) {
                    continue;
                }
                
                p2ToDelete.add(p2);

                // cut p2 with plane1
                List<Vector3d> frontPolygon = new ArrayList<>();
                List<Vector3d> backPolygon = new ArrayList<>();
                for (int i = 0; i < p2.vertices.size(); i++) {
                    int j = (i + 1) % p2.vertices.size();
                    int ti = typesP2[i];
                    int tj = typesP2[j];
                    Vertex vi = p2.vertices.get(i);
                    Vertex vj = p2.vertices.get(j);
                    if (ti != -1 /*front*/) {
                        frontPolygon.add(vi.pos);
                    }
                    if (ti != 1 /*back*/) {
                        backPolygon.add(vi.pos);
                    }
                    if (ti != tj && (ti != 0 && tj != 0)/*spanning*/) {
                        double t = (p1.plane.getDist() - p1.plane.normal.dot(vi.pos))
                                / p1.plane.normal.dot(vj.pos.minus(vi.pos));
                        Vertex v = vi.interpolate(vj, t);
                        frontPolygon.add(v.pos);
                        backPolygon.add(v.pos);
                    }
                }

                if (frontPolygon.size() > 2) {
                    cutsWithP1.add(Polygon.fromPoints(frontPolygon, p1.getStorage()));
                }
                if (backPolygon.size() > 2) {
                    cutsWithP1.add(Polygon.fromPoints(backPolygon, p1.getStorage()));
                }
            }
            ps2WithCuts.addAll(cutsWithP1);
             ps2WithCuts.removeAll(p2ToDelete);
        }
        result.p1 = ps1;
        result.p2 = ps2WithCuts;

        return result;
    }

    public static class SplitResult {

        List<Polygon> p1 = new ArrayList<>();
        List<Polygon> p2 = new ArrayList<>();

    }

    enum PolygonType {
        UNKNOWN,
        INSIDE_1,
        INSIDE_2,
        OUTSIDE,
        OPPOSITE
    }
}
