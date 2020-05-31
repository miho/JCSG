/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg.playground;

import eu.mihosoft.jcsg.*;
import eu.mihosoft.vvecmath.ModifiableVector3d;
import eu.mihosoft.vvecmath.Plane;
import eu.mihosoft.vvecmath.Transform;
import eu.mihosoft.vvecmath.Vector3d;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Michael Hoffer (info@michaelhoffer.de)
 */
public class Main2 {

    public static final double EPS = 1e-8;
    // s3 = s1 - s2

    // # Step 1:
    //
    // Take all polygons of s2 that have vertices inside the bounding box b 1of s1
    // (polygons with only some vertices inside of b are cut along the planes of b1)
    // All polygons outside of b1 are ignored from now on.
    //
    // Remark: collinear polygons are considered as being outside of b1.
    //
    // # Step 2:
    //
    // Cut all remaining polygons of s2 with the polygons of s1. Only keep the polygons with all vertices
    // inside of b1.
    //
    // # Step 3:
    //
    // a) For each remaining polygon p of s2: cast an orthogonal ray from the center of p (normal) and
    //    count the number of intersecting polygons of s1. If the ray hits a vertex of p or cuts the
    //    boundary, it counts as being intersected by the ray.
    //
    // b) Classify the resulting polygon by whether the number of intersections is even or uneven number
    //    of intersections. An uneven number of intersections indicates that the polygon is inside of s1;
    //    An even number of intersections indicates that the polygon is outside of s2.
    //
    //
    // # Step 5:
    //
    // Repeat these steps (1-4) with s1 and s2 reversed.
    //
    // # Step 6:
    //
    // s3 consists of all polygons of s1 classified as being outside of s2 and all polygons of s2 being
    // classified as being inside of s1.
    //
    //   _________
    //  /   /*\   \
    //  |   |*|   |           * = intersection
    //  \___\*/___/
    //
    //   _________                    ____
    //  /   / \   \                  /   /
    //  |   | |   |            ===   |   |
    //  \___\ /___/                  \___\
    public static void main(String[] args) throws IOException {

        CSG s1 = new Cube(2).toCSG();
        CSG s2 = new Sphere(Vector3d.x(0.0), 1.25, 32, 32).toCSG();

        Files.write(Paths.get("diff-orig.stl"), s2.difference(s1).toStlString().getBytes());

        Classification classification = classify(s1,s2);

        List<Polygon> polygons = new ArrayList<>();

        polygons.addAll(classification.outsideS1);
//        polygons.addAll(classification.outsideS2);
//        polygons.addAll(classification.insideS1);
        polygons.addAll(classification.insideS2);

        CSG difference = CSG.fromPolygons(polygons);

        Files.write(Paths.get("diff.stl"), difference.toStlString().getBytes());
    }

    static class Classification {
        public List<Polygon> insideS1;
        public List<Polygon> outsideS1;
        public List<Polygon> insideS2;
        public List<Polygon> outsideS2;
    }

    static class Classification1 {
        public List<Polygon> inside;
        public List<Polygon> outside;
    }

    public static Classification classify(CSG s1, CSG s2) {
        Classification result = new Classification();

        Classification1 r1 = classify1(s1,s2);
        Classification1 r2 = classify1(s2,s1);

        result.insideS1 = r1.inside;
        result.outsideS1 = r1.outside;
        result.insideS2 = r2.inside;
        result.outsideS2 = r2.outside;

        return result;
    }

    public static Classification1 classify1(CSG s1, CSG s2) {

        // step 1

        // get polygons inside of b
        Bounds b1 = s1.getBounds();
        Bounds b2 = s2.getBounds();
        List<Polygon> ps1 = s1.getPolygons();
        List<Polygon> ps2 = s2.getPolygons();//.stream().filter(p->b1.intersects(p)).collect(Collectors.toList());

        // step 2

        // cut polygons
        ps2 = splitPolygons(ps1, ps2, b1, b2);//.stream().filter(p->p.vertices.stream().filter(v->b1.contains(v)).count()==p.vertices.size()).collect(Collectors.toList());

        // step 3

        double TOL = 1e-10;

        Map<Boolean, List<Polygon>> polygons = ps2.parallelStream().collect(Collectors.partitioningBy(p-> {
            return classifyPolygon(p, ps1, b1) == PolygonType.OUTSIDE;
        }));

        List<Polygon> inside  = polygons.get(false);
        List<Polygon> outside = polygons.get(true);

        Classification1 result = new Classification1();
        result.inside = inside;
        result.outside = outside;

        return result;
    }



    public static PolygonType classifyPolygon(Polygon p1, List<Polygon> polygons, Bounds b) {

        double TOL = 1e-10;

        // we are definitely outside if bounding boxes don't intersect
        if (!p1.getBounds().intersects(b)) {
            return PolygonType.OUTSIDE;
        }

        Vector3d rayCenter = p1.centroid();
        Vector3d rayDirection = p1.getPlane().getNormal();

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

            int frontOrBack = p1.getPlane().compare(ri.intersectionPoint, TOL);

            if (frontOrBack < 0) {
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

            if (dist < TOL && ri.polygon.getPlane().getNormal().dot(rayDirection) < TOL) {
                // System.out.println("  -> skipping intersection " + i);
                continue;
            }

            if (dist < prevDist) {
                prevDist = dist;
                min = ri;
            }

            i++;
        }

        if (min == null) {
            return PolygonType.OUTSIDE;
        }

        // try {
        //    ObjFile objF = CSG.fromPolygons(min.polygon).toObj();
        //    objF.toFiles(Paths.get("test-intersection-min.obj"));
        //} catch (IOException ex) {
        //    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        //}

        int frontOrBack = p1.getPlane().compare(min.intersectionPoint, TOL);

        Vector3d planePoint = p1.getPlane().getAnchor();

        int sameOrOpposite = p1.getPlane().compare(
                planePoint.plus(min.polygon.getPlane().getNormal()), TOL
        );

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
                    + " -> point:          " + intersectionPoint + "\n"
                    + " -> polygon-normal: " + polygon.getPlane().getNormal() + "\n"
                    + " -> type:           " + type + "\n"
                    + "]";
        }

    }

    public static List<RayIntersection> getPolygonsThatIntersectWithRay(
            Vector3d point, Vector3d direction, List<Polygon> polygons, double TOL) {
        List<RayIntersection> intersection = new ArrayList<>();
        for (Polygon p : polygons) {
            PlaneIntersection res = computePlaneIntersection(p.getPlane(), point, direction, TOL);
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

        //if line is parallel to the plane...
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

        if (ps1.isEmpty() || ps2.isEmpty()) return Collections.EMPTY_LIST;

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

                List<Polygon> cutsOfP2WithP1 = cutPolygonWithPlaneIf(p2, p1.getPlane(),
                        (Predicate<List<Vector3d>>) segments -> {

                            //if(true)return true;
                            if(segments.size()!=2) return true;

                            Vector3d s1 = segments.get(0);
                            Vector3d s2 = segments.get(1);

                            int numIntersectionsPoly1 = 0;
                            for(int i = 0; i< p1.vertices.size()-1;i++) {
                                //System.out.println("i,j : " + i + ", " + (i+1%p1.vertices.size()));
                                Vector3d e1 = p1.vertices.get(i).pos;
                                Vector3d e2 = p1.vertices.get(i+1%p1.vertices.size()).pos;
                                LineIntersectionResult iRes = calculateLineLineIntersection(e1,e2,s1,s2);
                                if(iRes.type == LineIntersectionResult.IntersectionType.INTERSECTING &&
                                        p1.contains(iRes.segmentPoint1.get())) {
                                    numIntersectionsPoly1++;
                                }
                            }

                            int numIntersectionsPoly2 = 0;
                            for(int i = 0; i< p2.vertices.size()-1;i++) {
                                Vector3d e1 = p2.vertices.get(i).pos;
                                Vector3d e2 = p2.vertices.get(i+1%p2.vertices.size()).pos;
                                LineIntersectionResult iRes = calculateLineLineIntersection(e1,e2,s1,s2);
                                if(iRes.type == LineIntersectionResult.IntersectionType.INTERSECTING &&
                                        p2.contains(iRes.segmentPoint1.get())) {
                                    numIntersectionsPoly2++;
                                }
                            }

                            return numIntersectionsPoly1 > 0 && numIntersectionsPoly2 > 0;
                        });

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
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        CSG cube = new Cube(Vector3d.xyz(1, 1, 1), Vector3d.xyz(2, 2, 2)).toCSG()
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
                        p.getPlane().getNormal(),
                        cubePolys, EPS);

        System.out.println("my normal: " + p.getPlane().getNormal());

        System.out.println("#intersections: " + intersections.size());
        for (RayIntersection ri : intersections) {
            System.out.println(ri);
        }

        PolygonType pType = classifyPolygon(p, cubePolys, cube.getBounds());

        System.out.println("#pType:");
        System.out.println(" -> " + pType);

        List<Polygon> cutsWithCube = splitPolygons(cubePolys,
                Arrays.asList(p), p.getBounds(), cube.getBounds());

        cutsWithCube.addAll(cube.getPolygons()/*.subList(cubePolyFrom, cubePolyTo)*/);

        try {
            ObjFile objF = CSG.fromPolygons(cutsWithCube).toObj(3);
            objF.toFiles(Paths.get("test-split1.obj"));
//            Files.write(Paths.get("test-split1.stl"),
//                    CSG.fromPolygons(cutsWithP1).toStlString().getBytes());
        } catch (IOException ex) {
            Logger.getLogger(Main2.class.getName()).log(Level.SEVERE, null, ex);
        }

        ModifiableVector3d segmentP1 = Vector3d.zero().asModifiable();
        ModifiableVector3d segmentP2 = Vector3d.zero().asModifiable();
        LineIntersectionResult lineRes = calculateLineLineIntersection(
                Vector3d.xyz(-1, 0, 0), Vector3d.xyz(1, 0, 0),
                Vector3d.xyz(0, -1, 0), Vector3d.xyz(0, 1, 0));

        System.out.println("l1 intersect l2: ");
        System.out.println(lineRes);

        // System.exit(0);
    }

    private static List<Polygon> cutPolygonWithPlaneIf(Polygon p, Plane plane, Predicate<List<Vector3d>> check) {

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

        boolean checkResult = check == null;

        if (check != null) {
            checkResult = check.test(on);
        }

        if (!checkResult) return Collections.EMPTY_LIST;

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


    static class LineIntersectionResult {

        public final IntersectionType type;

        public final Optional<Vector3d> segmentPoint1;
        public final Optional<Vector3d> segmentPoint2;

        LineIntersectionResult(IntersectionType type, Vector3d segmentPoint1, Vector3d segmentPoint2) {
            this.type = type;
            this.segmentPoint1 = Optional.ofNullable(segmentPoint1);
            this.segmentPoint2 = Optional.ofNullable(segmentPoint2);
        }

        static enum IntersectionType {
            PARALLEL,
            NON_PARALLEL,
            INTERSECTING
        }

        static final LineIntersectionResult PARALLEL =
                new LineIntersectionResult(IntersectionType.PARALLEL, null, null);

        @Override
        public String toString() {
            return "[\n -> type: " + type
                    + "\n -> segmentP1: " + (segmentPoint1.isPresent() ? segmentPoint1.get() : "none")
                    + "\n -> segmentP2: " + (segmentPoint2.isPresent() ? segmentPoint2.get() : "none")
                    + "\n]";
        }
    }

    /**
     * Calculates the intersection line segment between two lines.
     *
     * @param line1Point1
     * @param line1Point2
     * @param line2Point1
     * @param line2Point2
     * @return {@code true} if the intersection line segment exists; {@code false} otherwise
     */
    public static LineIntersectionResult calculateLineLineIntersection(Vector3d line1Point1, Vector3d line1Point2,
                                                                       Vector3d line2Point1, Vector3d line2Point2) {
        // Algorithm is ported from the C algorithm of
        // Paul Bourke at http://local.wasp.uwa.edu.au/~pbourke/geometry/lineline3d/

        Vector3d p1 = line1Point1;
        Vector3d p2 = line1Point2;
        Vector3d p3 = line2Point1;
        Vector3d p4 = line2Point2;
        Vector3d p13 = p1.minus(p3);
        Vector3d p43 = p4.minus(p3);

        if (p43.magnitudeSq() < EPS) {
            return LineIntersectionResult.PARALLEL;
        }
        Vector3d p21 = p2.minus(p1);
        if (p21.magnitudeSq() < EPS) {
            return LineIntersectionResult.PARALLEL;
        }

        double d1343 = p13.x() * (double) p43.x() + (double) p13.y() * p43.y() + (double) p13.z() * p43.z();
        double d4321 = p43.x() * (double) p21.x() + (double) p43.y() * p21.y() + (double) p43.z() * p21.z();
        double d1321 = p13.x() * (double) p21.x() + (double) p13.y() * p21.y() + (double) p13.z() * p21.z();
        double d4343 = p43.x() * (double) p43.x() + (double) p43.y() * p43.y() + (double) p43.z() * p43.z();
        double d2121 = p21.x() * (double) p21.x() + (double) p21.y() * p21.y() + (double) p21.z() * p21.z();

        double denom = d2121 * d4343 - d4321 * d4321;
        if (Math.abs(denom) < EPS) {
            return LineIntersectionResult.PARALLEL;
        }
        double numer = d1343 * d4321 - d1321 * d4343;

        double mua = numer / denom;
        double mub = (d1343 + d4321 * (mua)) / d4343;

        ModifiableVector3d resultSegmentPoint1 = Vector3d.zero().asModifiable();
        ModifiableVector3d resultSegmentPoint2 = Vector3d.zero().asModifiable();

        resultSegmentPoint1.setX(p1.x() + mua * p21.x());
        resultSegmentPoint1.setY(p1.y() + mua * p21.y());
        resultSegmentPoint1.setZ(p1.z() + mua * p21.z());
        resultSegmentPoint2.setX(p3.x() + mub * p43.x());
        resultSegmentPoint2.setY(p3.y() + mub * p43.y());
        resultSegmentPoint2.setZ(p3.z() + mub * p43.z());

        if (resultSegmentPoint1.equals(resultSegmentPoint2)) {
            return new LineIntersectionResult(LineIntersectionResult.IntersectionType.INTERSECTING,
                    resultSegmentPoint1, resultSegmentPoint2);
        } else {
            return new LineIntersectionResult(LineIntersectionResult.IntersectionType.NON_PARALLEL,
                    resultSegmentPoint1, resultSegmentPoint2);
        }
    }

}
