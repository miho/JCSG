/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d.samples;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Cylinder;
import eu.mihosoft.vrl.v3d.Edge;
import eu.mihosoft.vrl.v3d.FileUtil;
import eu.mihosoft.vrl.v3d.Polygon;
import eu.mihosoft.vrl.v3d.Sphere;
import static eu.mihosoft.vrl.v3d.Transform.unity;
import eu.mihosoft.vrl.v3d.Vector3d;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Average Chicken Egg.
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class EdgeTest {

    public CSG toCSG() {
        double radius = 22;
        double stretch = 1.50;
        int resolution = 64;

        CSG cylinder = new Cylinder(1, 0.3, 8).toCSG();
        
        CSG sphere = new Sphere(0.1,8,4).toCSG().transformed(unity().translateZ(0.15));
        
        CSG csg = cylinder.union(sphere);

        List<List<Polygon>> planeGroups = searchPlangeGroups(csg);

        List<Polygon> boundaryPolygons = boundaryPolygons(planeGroups);

        System.out.println("#groups: " + boundaryPolygons.size());

//        List<Polygon> polys = boundaryPolygons.stream().peek(p->System.out.println("verts: "+p.vertices)).map(p->PolygonUtil.concaveToConvex(p)).flatMap(pList->pList.stream()).collect(Collectors.toList());
//        return CSG.fromPolygons(boundaryPolygons);
        
        return csg;
    }
    
    private List<Polygon> boundaryPolygons(List<List<Polygon>> planeGroups) {
        List<Polygon> result = new ArrayList<>();
        
        for (List<Polygon> polygonGroup : planeGroups) {
            result.add(boundaryPolygon(polygonGroup));
        }
        
        return result;
    }

    private Polygon boundaryPolygon(List<Polygon> planeGroup) {

        List<Edge> edges = new ArrayList<>();

        for (Polygon p : planeGroup) {
            List<Edge> pEdges = Edge.fromPolygon(p);
            edges.addAll(pEdges);
        }

        List<Edge> boundaryEdges = new ArrayList<>();
        edges.stream().forEachOrdered((e) -> {
            int count = Collections.frequency(edges, e);
            if (count == 1) {
                boundaryEdges.add(e);
            }
        });

        System.out.println("#bnd-edges: " + boundaryEdges.size() + ",#edges: " +edges.size());

        return Edge.toPolygons(boundaryEdges,planeGroup.get(0).plane).get(0);
    }

    private List<List<Polygon>> searchPlangeGroups(CSG cylinder) {
        List<List<Polygon>> planeGroups = new ArrayList<>();
        boolean[] used = new boolean[cylinder.getPolygons().size()];
        System.out.println("#polys: " + cylinder.getPolygons().size());
        for (int pOuterI = 0; pOuterI < cylinder.getPolygons().size(); pOuterI++) {

            if (used[pOuterI]) {
                continue;
            }

            Polygon pOuter = cylinder.getPolygons().get(pOuterI);

            List<Polygon> otherPolysInPlane = new ArrayList<>();

            otherPolysInPlane.add(pOuter);

            for (int pInnerI = 0; pInnerI < cylinder.getPolygons().size(); pInnerI++) {

                Polygon pInner = cylinder.getPolygons().get(pInnerI);

                if (pOuter.equals(pInner)) {
                    continue;
                }

                Vector3d nOuter = pOuter.plane.normal;
                Vector3d nInner = pInner.plane.normal;

                double angle = nOuter.angle(nInner);

//                System.out.println("angle: " + angle + " between " + pOuterI+" -> " + pInnerI);
                if (angle < 0.01 /*&& abs(pOuter.plane.dist - pInner.plane.dist) < 0.1*/) {
                    otherPolysInPlane.add(pInner);
                    used[pInnerI] = true;
                    System.out.println("used: " + pOuterI + " -> " + pInnerI);
                }
            }

            if (!otherPolysInPlane.isEmpty()) {
                planeGroups.add(otherPolysInPlane);
            }
        }
        return planeGroups;
    }

    public static void main(String[] args) throws IOException {
        FileUtil.write(Paths.get("edge-test.stl"), new EdgeTest().toCSG().toStlString());
    }
}
