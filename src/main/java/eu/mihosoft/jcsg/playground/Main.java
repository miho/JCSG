/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg.playground;

import eu.mihosoft.jcsg.Bounds;
import eu.mihosoft.jcsg.CSG;
import eu.mihosoft.jcsg.Cube;
import eu.mihosoft.jcsg.ObjFile;
import eu.mihosoft.jcsg.Polygon;
import eu.mihosoft.jcsg.STL;
import eu.mihosoft.jcsg.Sphere;
import eu.mihosoft.jcsg.Vertex;
import eu.mihosoft.vvecmath.Plane;
import eu.mihosoft.vvecmath.Transform;
import eu.mihosoft.vvecmath.Vector3d;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Michael Hoffer (info@michaelhoffer.de)
 */
public class Main {

    public static final double EPS = 1e-8;

    public static void main(String[] args) throws IOException {

        testCut();

        CSG c1 = new Cube(Vector3d.zero(), Vector3d.xyz(1, 1, 1)).toCSG();

        CSG c2 = new Cube(Vector3d.xyz(1,1,1), Vector3d.xyz(2, 2, 2)).toCSG()
                .transformed(Transform.unity().rot(Vector3d.ZERO, Vector3d.UNITY, 78));

//        Files.write(Paths.get("c1.stl"), c1.toStlString().getBytes());
//        Files.write(Paths.get("c2.stl"), c2.toStlString().getBytes());
//        c1 = STL.file(Paths.get("c1.stl"));
//        c2 = STL.file(Paths.get("c2.stl"));
//        c1 = new Sphere(Vector3d.x(0.), 0.5, 16, 16).toCSG();
//        c2 = new Sphere(Vector3d.x(0.6), 0.5, 16, 16).toCSG();
        c2 = new Sphere(Vector3d.x(0.0), 0.7, 32, 32).toCSG();
        List<Polygon> result1 = splitPolygons(
                c1.getPolygons(), c2.getPolygons(),
                c1.getBounds(), c2.getBounds()
        );

        List<Polygon> result2 = splitPolygons(
                c2.getPolygons(), c1.getPolygons(),
                c2.getBounds(), c1.getBounds()
        );

 //       result1 = splitPolygons(
 //               result2, c2.getPolygons(),
 //               c1.getBounds(), c2.getBounds());
        List<Polygon> splitted = new ArrayList<>();
        splitted.addAll(result1);
        splitted.addAll(result2);

//        CSG.fromPolygons(splitted).toObj(100).toFiles(Paths.get("test-split1.obj"));
//
        Files.write(Paths.get("test-split1.stl"),
                CSG.fromPolygons(splitted).toStlString().getBytes());
        List<Polygon> inC2 = new ArrayList<>();
        List<Polygon> outC2 = new ArrayList<>();
        List<Polygon> sameC2 = new ArrayList<>();
        List<Polygon> oppositeC2 = new ArrayList<>();

        List<Polygon> unknownOfC1 = new ArrayList<>();

        for (Polygon p : result2) {
            PolygonType pT = classifyPolygon(p, c2.getPolygons(), c2.getBounds());

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
            PolygonType pT = classifyPolygon(p, c1.getPolygons(), c1.getBounds());

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

        System.err.println(">> unknown  polygons in C1: " + unknownOfC1.size());
        System.err.println(">> unknown  polygons in C2: " + unknownOfC2.size());
        System.err.println(">> opposite polygons in C1: " + oppositeC1.size());
        System.err.println(">> opposite polygons in C2: " + oppositeC2.size());
        System.err.println(">> inside   polygons in C1: " + inC1.size());
        System.err.println(">> inside   polygons in C2: " + inC2.size());

        Files.write(Paths.get("test.stl"), result.toStlString().getBytes());

    }

    public static PolygonType classifyPolygon(Polygon p1, List<Polygon> polygons, Bounds b) {

        double TOL = 1e-10;

        // we are definitely outside if bounding boxes don't intersect
        if (!p1.getBounds().intersects(b)) {
            return PolygonType.OUTSIDE;
        }

        Vector3d rayCenter = p1.centroid();
        Vector3d rayDirection = p1.plane.getNormal();

        List<RayIntersection> intersections = getPolygonsThatIntersectWithRay(
                rayCenter, rayDirection, polygons, TOL);

        if (intersections.isEmpty()) {
            return PolygonType.OUTSIDE;
        }

        // find the closest polygon to the centroid of p1 which intersects the
        // ray
        RayIntersection min = null; //intersections.get(0);
        double dist = 0;
        double prevDist = Double.MAX_VALUE; // min.polygon.centroid().minus(rayCenter).magnitude();
        int i = 0;
        for (RayIntersection ri : intersections) {

            int frontOrBack = p1.plane.compare(ri.intersectionPoint, TOL);

            if(frontOrBack < 0) {
                // System.out.println("  -> skipping intersection behind ray " + i);
                continue;
            }

            //try {
            //    ObjFile objF = CSG.fromPolygons(ri.polygon).toObj(3);
            //    objF.toFiles(Paths.get("test-intersection-" + i + ".obj"));
            //} catch (IOException ex) {
            //    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            //}

            dist = ri.polygon.centroid().minus(rayCenter).magnitude();

            //System.out.println("dist-"+i+": " + dist);

            if(dist < TOL && ri.polygon.plane.getNormal().dot(rayDirection) < TOL ) {
                // System.out.println("  -> skipping intersection " + i);
                continue;
            }

            if (dist < prevDist) {
                prevDist = dist;
                min = ri;
            }

            i++;
        }

        if (min==null) {
            return PolygonType.OUTSIDE;
        }

        // try {
        //    ObjFile objF = CSG.fromPolygons(min.polygon).toObj();
        //    objF.toFiles(Paths.get("test-intersection-min.obj"));
        //} catch (IOException ex) {
        //    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        //}

        int frontOrBack = p1.plane.compare(min.intersectionPoint, TOL);

        Vector3d planePoint = p1.plane.getAnchor();

        int sameOrOpposite = p1.plane.compare(
                planePoint.plus(min.polygon.plane.getNormal()), TOL);

        if (frontOrBack > 0 && sameOrOpposite > 0) {
            return PolygonType.INSIDE;
        }

        if (frontOrBack > 0 && sameOrOpposite < 0) {
            return PolygonType.OUTSIDE;
        }

        if (frontOrBack < 0 && sameOrOpposite < 0) {
            return PolygonType.INSIDE;
        }

        if (frontOrBack < 0 && sameOrOpposite > 0) {
            return PolygonType.OUTSIDE;
        }

        if (frontOrBack == 0 && sameOrOpposite > 0) {
            return PolygonType.SAME;
        }

        if (frontOrBack == 0 && sameOrOpposite < 0) {
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
        public final PlaneIntersection.IntersectionType type;

        public RayIntersection(Vector3d intersectionPoint,
                Polygon polygon, PlaneIntersection.IntersectionType type) {
            this.intersectionPoint = intersectionPoint;
            this.polygon = polygon;
            this.type = type;
        }
        
        
        @Override
        public String toString() {
            return ""
                    + "[\n"
                    + " -> point:          " + intersectionPoint+"\n"
                    + " -> polygon-normal: " + polygon.plane.getNormal()+"\n"
                    + " -> type:           " + type + "\n"
                    + "]";
        }

    }

    public static List<RayIntersection> getPolygonsThatIntersectWithRay(
            Vector3d point, Vector3d direction, List<Polygon> polygons, double TOL) {
        List<RayIntersection> intersection = new ArrayList<>();
        for (Polygon p : polygons) {
            PlaneIntersection res = computePlaneIntersection(p.plane, point, direction, TOL);
            if (res.point.isPresent()) {
                if (p.contains(res.point.get())) {
                    intersection.add(new RayIntersection(res.point.get(), p, res.type));
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
        Vector3d normal = plane.getNormal();
        Vector3d planePoint = plane.getAnchor();

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

    /**
     * Splits polygons ps2 with planes from polygons ps1.
     *
     * @param ps1
     * @param ps2
     * @param b1
     * @param b2
     * @return
     */
    public static List<Polygon> splitPolygons(
            List<Polygon> ps1,
            List<Polygon> ps2,
            Bounds b1, Bounds b2) {
        
        System.out.println("#ps1: " + ps1.size() + ", #ps2: " + ps2.size());
        
        if(ps1.isEmpty()||ps2.isEmpty()) return Collections.EMPTY_LIST;

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

                List<Polygon> cutsOfP2WithP1 = cutPolygonWithPlane(p2, p1.plane);

                if (!cutsOfP2WithP1.isEmpty()) {
                    cutsWithP1.addAll(cutsOfP2WithP1);
                    p2ToDelete.add(p2);
                }
            }
            ps2WithCuts.addAll(cutsWithP1);
            ps2WithCuts.removeAll(p2ToDelete);
        }

        return ps2WithCuts;
    }

    private static void cutPolygonWithPlaneAndTypes(Polygon polygon, Plane cutPlane,
            int[] vertexTypes, List<Vector3d> frontPolygon,
            List<Vector3d> backPolygon, List<Vector3d> onPlane) {

//        System.out.println("polygon: \n" + polygon.toStlString());
//        System.out.println("--------------------");
//        System.out.println("plane: \n -> p: " + cutPlane.getAnchor() + "\n -> n: " + cutPlane.getNormal());
//        System.out.println("--------------------");
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
                PlaneIntersection pI = computePlaneIntersection(cutPlane, vi.pos, vj.pos.minus(vi.pos), EPS);

                if (pI.type != PlaneIntersection.IntersectionType.NON_PARALLEL) {
                    throw new RuntimeException("I need help (3)!");
                }

                Vector3d intersectionPoint = pI.point.get();

                // System.out.println("i: " + i + ", j: " + j + " : " + intersectionPoint);

//                double t = (cutPlane.distance(vi.pos))/vi.pos.minus(vj.pos).magnitude();
//                System.out.println("t: " + t + ", i: " + i + ", j: " + j);
//                System.out.println(" : vi-plane-dist: " + cutPlane.distance(vi.pos) + ", vi-vj-dist: " + vi.pos.minus(vj.pos).magnitude());
//                System.out.println(" : vi: " + vi.pos.toStlString());
//                System.out.println(" : vj: " + vj.pos.toStlString());
//                System.out.println(" : " + cutPlane.distance(vi.pos)  + " , " + cutPlane.distance(vj.pos));
//                Vertex v = vi.interpolate(vj, t);
//                System.out.println(" : vs: " + v);
                frontPolygon.add(intersectionPoint);
                backPolygon.add(intersectionPoint);
                onPlane.add(intersectionPoint);
            }
        }
    }

    public static void testCut() {

        Polygon p = Polygon.fromPoints(
                Vector3d.xyz(0, 0, 0),
                Vector3d.xyz(1, 0, 0),
                Vector3d.xyz(1, 0, 1),
                Vector3d.xyz(0, 0, 1)
        );

        try {
            CSG pCSG = STL.file(Paths.get("sphere-test-01.stl"));

            p = pCSG.getPolygons().get(0);
        } catch(Exception ex) {
            //
        }

        CSG cube = new Cube(Vector3d.xyz(1,1,1), Vector3d.xyz(2, 2, 2)).toCSG()
                .transformed(Transform.unity().rot(Vector3d.ZERO, Vector3d.UNITY, 17));

        cube = new Sphere(Vector3d.x(0.), 0.5, 16, 16).toCSG();

//        CSG cube = new Cube(1).toCSG().transformed(
//                Transform.unity().translate(0.5,-0.55,0.5).rot(Vector3d.ZERO, Vector3d.UNITY, 0)
//        );

        int cubePolyFrom = 0;
        int cubePolyTo = 6;

        List<Polygon> cubePolys = cube.getPolygons();//.subList(cubePolyFrom, cubePolyTo);

        System.out.println("p: " + p.toStlString());
        System.out.println("p-centroid: " + p.centroid());

        List<RayIntersection> intersections = 
                getPolygonsThatIntersectWithRay(
                        p.centroid(),
                        p.plane.getNormal(),
                        cubePolys, EPS);
        
        System.out.println("my normal: " + p.plane.getNormal());
        
        System.out.println("#intersections: " + intersections.size());
        for(RayIntersection ri : intersections) {
            System.out.println(ri);
        }
        
        PolygonType pType = classifyPolygon(p, cubePolys, cube.getBounds());

        System.out.println("#pType:");
        System.out.println(" -> "+pType);
        
        List<Polygon> cutsWithCube = splitPolygons(cubePolys,
                Arrays.asList(p), p.getBounds(), cube.getBounds());

        cutsWithCube.addAll(cube.getPolygons()/*.subList(cubePolyFrom, cubePolyTo)*/);

        try {
            ObjFile objF = CSG.fromPolygons(cutsWithCube).toObj(3);
            objF.toFiles(Paths.get("test-split1.obj"));
//            Files.write(Paths.get("test-split1.stl"),
//                    CSG.fromPolygons(cutsWithP1).toStlString().getBytes());
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        // System.exit(0);
    }

    private static List<Polygon> cutPolygonWithPlane(Polygon p, Plane plane) {

        boolean typesEqual = true;
        int types[] = new int[p.vertices.size()];
        for (int i = 0; i < p.vertices.size(); i++) {
            types[i] = plane.compare(p.vertices.get(i).pos, EPS);
//            System.out.println("type " + i + ": " + types[i]);

            if (i > 0 && typesEqual) {
                typesEqual = typesEqual && (types[i] == types[i - 1]);
            }
        }

        // planes are parallel, thus polygons do not intersect
        if (typesEqual) {
            return Collections.EMPTY_LIST;
        }

        List<Vector3d> front = new ArrayList<>();
        List<Vector3d> back = new ArrayList<>();
        List<Vector3d> on = new ArrayList<>();
        cutPolygonWithPlaneAndTypes(p, plane, types, front, back, on);
        
        List<Polygon> cutsWithP1 = new ArrayList<>();
        if (front.size() > 2) {
            Polygon frontCut = Polygon.fromPoints(
                    front);
            if (frontCut.isValid()) {
                cutsWithP1.add(frontCut);
            }
        }
        if (back.size() > 2) {
            Polygon backCut = Polygon.fromPoints(
                    back);
            if (backCut.isValid()) {
                cutsWithP1.add(backCut);
            }
        }
        return cutsWithP1;
    }

    enum PolygonType {
        UNKNOWN,
        INSIDE,
        OUTSIDE,
        OPPOSITE,
        SAME
    }
}
