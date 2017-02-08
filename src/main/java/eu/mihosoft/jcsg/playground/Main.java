/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg.playground;

import eu.mihosoft.jcsg.Bounds;
import eu.mihosoft.jcsg.CSG;
import eu.mihosoft.jcsg.Cube;
import eu.mihosoft.jcsg.Polygon;
import eu.mihosoft.jcsg.Sphere;
import eu.mihosoft.jcsg.Vertex;
import eu.mihosoft.vvecmath.Plane;
import eu.mihosoft.vvecmath.Transform;
import eu.mihosoft.vvecmath.Vector3d;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author Michael Hoffer (info@michaelhoffer.de)
 */
public class Main {

    public static final double EPS = 1e-8;

    public static void main(String[] args) throws IOException {

        CSG c1 = new Cube(Vector3d.zero(), Vector3d.xyz(2, 2, 2)).toCSG().
                transformed(Transform.unity().rot(Vector3d.ZERO, Vector3d.UNITY, 45));
        CSG c2 = new Cube(Vector3d.xyz(1, 1, 1), Vector3d.xyz(2, 2, 2)).toCSG();
//        c1 = new Sphere(Vector3d.x(0.), 1.25, 10, 10).toCSG();
//        c2 = new Sphere(Vector3d.x(0.75), 1.25, 10, 10).toCSG();
        c2 = new Sphere(Vector3d.x(0.0), 1.25, 32, 32).toCSG();

        List<Polygon> result1 = splitPolygons(
                c1.getPolygons(), c2.getPolygons(),
                c1.getBounds(), c2.getBounds());

        List<Polygon> result2 = splitPolygons(
                c2.getPolygons(), c1.getPolygons(),
                c2.getBounds(), c1.getBounds());
        
        List<Polygon> splitted = new ArrayList<>();
        splitted.addAll(result1);
        splitted.addAll(result2);
        
        Files.write(Paths.get("test-split1.stl"),
                CSG.fromPolygons(splitted).toStlString().getBytes());

        List<Polygon> inC2 = new ArrayList<>();
        List<Polygon> outC2 = new ArrayList<>();
        List<Polygon> sameC2 = new ArrayList<>();
        List<Polygon> oppositeC2 = new ArrayList<>();

        List<Polygon> unknownOfC1 = new ArrayList<>();

        for (Polygon p : result2) {
            PolygonType pT = classifyPolygon(p, result1, c2.getBounds());

            if (pT == PolygonType.INSIDE) {
                inC2.add(p);
            }

            if (pT == PolygonType.SAME) {
                sameC2.add(p);
            }

            if (pT == PolygonType.OPPOSITE) {
                oppositeC2.add(p);
            }

            if (pT == PolygonType.OUTSIDE) {
                outC2.add(p);
            }

            if (pT == PolygonType.UNKNOWN) {
                unknownOfC1.add(p);
            }
        }

        List<Polygon> inC1 = new ArrayList<>();
        List<Polygon> outC1 = new ArrayList<>();
        List<Polygon> sameC1 = new ArrayList<>();
        List<Polygon> oppositeC1 = new ArrayList<>();

        List<Polygon> unknownOfC2 = new ArrayList<>();

        for (Polygon p : result1) {
            PolygonType pT = classifyPolygon(p, result2, c1.getBounds());

            if (pT == PolygonType.INSIDE) {
                inC1.add(p);
            }

            if (pT == PolygonType.OUTSIDE) {
                outC1.add(p);
            }

            if (pT == PolygonType.SAME) {
                sameC1.add(p);
            }

            if (pT == PolygonType.OPPOSITE) {
                oppositeC1.add(p);
            }

            if (pT == PolygonType.UNKNOWN) {
                unknownOfC2.add(p);
            }
        }

        List<Polygon> difference = new ArrayList<>();
        difference.addAll(outC2);
        difference.addAll(oppositeC2);
        for (Polygon p : inC1) {
            p.flip();
        }
        for (Polygon p : inC2) {
            p.flip();
        }

        difference.addAll(inC1);

        System.err.println(">> creating CSG");

        CSG result = CSG.fromPolygons(difference);

        System.err.println(">> unknown polygons in C1: " + unknownOfC1.size());
        System.err.println(">> unknown polygons in C2: " + unknownOfC2.size());
        System.err.println(">> inside  polygons in C1: " + inC1.size());
        System.err.println(">> inside  polygons in C2: " + inC2.size());

        Files.write(Paths.get("test.stl"), result.toStlString().getBytes());

    }

    public static PolygonType classifyPolygon(Polygon p1, List<Polygon> polygons, Bounds b) {

        double TOL = 1e-8;

        // we are definitely outside if bounding boxes don't intersect
        if (!p1.getBounds().intersects(b)) {
            return PolygonType.OUTSIDE;
        }

        Vector3d rayCenter = p1.centroid();
        Vector3d rayDirection = p1.plane.normal;

        List<RayIntersection> intersections = getPolygonsThatIntersectWithRay(
                rayCenter, rayDirection, polygons, TOL);

        if (intersections.isEmpty()) {
            return PolygonType.OUTSIDE;
        }

        // find the closest polygon to the centroid of p1 which intersects thr
        // ray
        RayIntersection min = intersections.get(0);
        double prevDist = min.polygon.centroid().minus(rayCenter).magnitude();
        for (RayIntersection ri : intersections) {
            double dist = ri.polygon.centroid().minus(rayCenter).magnitude();
            if (dist < prevDist) {
                prevDist = dist;
                min = ri;
            }
        }

        int frontOrBack = p1.plane.compare(min.polygon.centroid(), TOL);

        Vector3d planePoint = p1.plane.normal.normalized().times(p1.plane.getDist());

        int towardsOrAwayFrom = p1.plane.compare(
                planePoint.plus(min.polygon.plane.normal), TOL);

        if (frontOrBack > 0 && towardsOrAwayFrom < 0) {
            return PolygonType.INSIDE;
        }

        if (frontOrBack < 0 && towardsOrAwayFrom > 0) {
            return PolygonType.OUTSIDE;
        }

        if (frontOrBack < 0 && towardsOrAwayFrom < 0) {
            return PolygonType.OUTSIDE;
        }

        if (frontOrBack > 0 && towardsOrAwayFrom > 0) {
            return PolygonType.INSIDE;
        }

        if (frontOrBack == 0 && towardsOrAwayFrom > 0) {
            return PolygonType.SAME;
        }

        if (frontOrBack == 0 && towardsOrAwayFrom < 0) {
            return PolygonType.OPPOSITE;
        }

        System.err.println("I need help (2) !");

        return PolygonType.UNKNOWN;
    }

    public static final class PlaneIntersection {

        public final IntersectionType type;
        public Optional<Vector3d> point;

        public PlaneIntersection(
                IntersectionType type, Optional<Vector3d> point) {
            this.type = type;
            this.point = point;
        }

        public static enum IntersectionType {
            ON,
            PARALLEL,
            NON_PARALLEL
        }
    }

    public static final class RayIntersection {

        public final Vector3d intersectionPoint;
        public final Polygon polygon;

        public RayIntersection(Vector3d intersectionPoint, Polygon polygon) {
            this.intersectionPoint = intersectionPoint;
            this.polygon = polygon;
        }

    }

    public static List<RayIntersection> getPolygonsThatIntersectWithRay(
            Vector3d point, Vector3d direction, List<Polygon> polygons, double TOL) {
        List<RayIntersection> intersection = new ArrayList<>();
        for (Polygon p : polygons) {
            PlaneIntersection res = computePlaneIntersection(p.plane, point, direction, TOL);
            if (res.type == PlaneIntersection.IntersectionType.NON_PARALLEL) {
                if (p.contains(res.point.get())) {
                    intersection.add(new RayIntersection(res.point.get(), p));
                }
            }
        }

        return intersection;
    }

    public static PlaneIntersection computePlaneIntersection(
            Plane plane, Vector3d point, Vector3d direction, double TOL) {

        //Ax + By + Cz + D = 0
        //x = x0 + t(x1  x0)
        //y = y0 + t(y1  y0)
        //z = z0 + t(z1  z0)
        //(x1 - x0) = dx, (y1 - y0) = dy, (z1 - z0) = dz
        //t = -(A*x0 + B*y0 + C*z0 )/(A*dx + B*dy + C*dz)
        Vector3d normal = plane.normal;
        Vector3d planePoint = normal.normalized().times(plane.getDist());

        double A = normal.x();
        double B = normal.y();
        double C = normal.z();
        double D = -(normal.x() * planePoint.x() + normal.y() * planePoint.y() + normal.z() * planePoint.z());

        double numerator = A * point.x() + B * point.y() + C * point.z() + D;
        double denominator = A * direction.x() + B * direction.y() + C * direction.z();

        //if line is paralel to the plane...
        if (Math.abs(denominator) < TOL) {
            //if line is contained in the plane...
            if (Math.abs(numerator) < TOL) {
                return new PlaneIntersection(
                        PlaneIntersection.IntersectionType.ON,
                        Optional.of(point));
            } else {
                return new PlaneIntersection(
                        PlaneIntersection.IntersectionType.PARALLEL,
                        Optional.empty());
            }
        } //if line intercepts the plane...
        else {
            double t = -numerator / denominator;
            Vector3d resultPoint = Vector3d.xyz(
                    point.x() + t * direction.x(),
                    point.y() + t * direction.y(),
                    point.z() + t * direction.z());

            return new PlaneIntersection(
                    PlaneIntersection.IntersectionType.NON_PARALLEL,
                    Optional.of(resultPoint));
        }
    }

    public static List<Polygon> splitPolygons(
            List<Polygon> ps1,
            List<Polygon> ps2,
            Bounds b1, Bounds b2) {

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

//                // compute distance of polygon 1 vertices to polygon 2 plane
//                int[] typesP1 = new int[p1.vertices.size()];
//
//                boolean typesEqual1 = true;
//                int prevType1 = 0;
//                for (int i = 0; i < p1.vertices.size(); i++) {
//                    typesP1[i] = p2.plane.compare(p1.vertices.get(i).pos, EPS);
//
//                    if (i > 0 && typesEqual1) {
//                        typesEqual1 &= prevType1 == typesP1[i];
//                    }
//
//                    prevType1 = typesP1[i];
//                }
//
//                // planes do not intersect, thus polygons do not intersect
//                if (typesEqual1) {
//                    continue;
//                }
                // compute distance of polygon 2 vertices to polygon 1 plane
                int[] typesP2 = new int[p2.vertices.size()];

                boolean typesEqual2 = true;
                int prevType2 = 0;

                for (int i = 0; i < p2.vertices.size(); i++) {
                    typesP2[i] = p1.plane.compare(p2.vertices.get(i).pos, EPS);

                    if (i > 0 && typesEqual2) {
                        typesEqual2 = typesEqual2 && (prevType2 == typesP2[i]);
                    }

                    prevType2 = typesP2[i];
                }

                // planes do not intersect, thus polygons do not intersect
                if (typesEqual2) {
                    continue;
                }

                // if at least two vertices of convex polygon are on the plane
                // then there's no intersection
                int countZeroTypes2 = 0;
                for (int i = 0; i < typesP2.length; i++) {
                    if (typesP2[i] == 0) {
                        countZeroTypes2++;
                    }
                }
                if (countZeroTypes2 > 1) {
                    continue;
                }

                if (p1.plane.normal.angle(p2.plane.normal) < EPS
                        || Math.abs(p1.plane.normal.angle(p2.plane.normal) - 180) < EPS) {
                    System.err.println("HERE");
//                    System.exit(-1);
                    continue;
                } else {
                    System.err.println("a: " + p1.plane.normal.angle(p2.plane.normal));
                }

                // cut p2 with plane1
                List<Vector3d> segmentPointsP2 = new ArrayList<>();
                List<Vector3d> frontPolygonP2 = new ArrayList<>();
                List<Vector3d> backPolygonP2 = new ArrayList<>();
                cutPolygonWithPlane(p2, p1.plane, typesP2,
                        frontPolygonP2, backPolygonP2, segmentPointsP2);
//                List<Vector3d> segmentPointsP1 = new ArrayList<>();
//                List<Vector3d> frontPolygonP1 = new ArrayList<>();
//                List<Vector3d> backPolygonP1 = new ArrayList<>();
//                cutPolygonWithPlane(p1, p2.plane, typesP1,
//                        frontPolygonP1, backPolygonP1, segmentPointsP1);

                // TODO check segment intersection for accurate results
                //      now, we might have more cuts than necessary
                p2ToDelete.add(p2);

                if (frontPolygonP2.size() > 2) {

                    Polygon p = Polygon.fromPoints(
                            frontPolygonP2, p2.getStorage());
                    if (p.isValid()) {
                        cutsWithP1.add(p);
                    }
                }
                if (backPolygonP2.size() > 2) {
                    Polygon p = Polygon.fromPoints(
                            backPolygonP2, p2.getStorage());
                    if (p.isValid()) {
                        cutsWithP1.add(p);
                    }
                }
            }
            ps2WithCuts.addAll(cutsWithP1);
            ps2WithCuts.removeAll(p2ToDelete);
        }

        return ps2WithCuts;
    }

    private static void cutPolygonWithPlane(Polygon polygon, Plane cutPlane,
            int[] vertexTypes, List<Vector3d> frontPolygon,
            List<Vector3d> backPolygon, List<Vector3d> segmentPoints) {

        for (int i = 0; i < polygon.vertices.size(); i++) {
            int j = (i + 1) % polygon.vertices.size();
            int ti = vertexTypes[i];
            int tj = vertexTypes[j];
            Vertex vi = polygon.vertices.get(i);
            Vertex vj = polygon.vertices.get(j);
            if (ti == 1 /*front*/) {
                frontPolygon.add(vi.pos);
            }
            if (ti == -1 /*back*/) {
                backPolygon.add(vi.pos);
            }

            if (ti == 0) {
                frontPolygon.add(vi.pos);
                backPolygon.add(vi.pos);
//                segmentPoints.add(vi.pos);
            }

            if (ti != tj && (ti != 0 && tj != 0)/*spanning*/) {
                double t = (cutPlane.getDist() - cutPlane.normal.dot(vi.pos))
                        / cutPlane.normal.dot(vj.pos.minus(vi.pos));
                Vertex v = vi.interpolate(vj, t);
                frontPolygon.add(v.pos);
                backPolygon.add(v.pos);
                segmentPoints.add(v.pos);
            }
        }
    }

    enum PolygonType {
        UNKNOWN,
        INSIDE,
        OUTSIDE,
        OPPOSITE,
        SAME
    }
}
