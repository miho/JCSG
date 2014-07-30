/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d.samples;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Extrude;
import eu.mihosoft.vrl.v3d.FileUtil;
import eu.mihosoft.vrl.v3d.Polygon;
import eu.mihosoft.vrl.v3d.Vector3d;
import eu.mihosoft.vrl.v3d.Vertex;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//public CSG toCSG() {
//        
//        Polygon p = Polygon.fromPoints(
//                new Vector3d(0, 0, 0),
//                 new Vector3d(0, 0, 0),
//                  new Vector3d(0, 0, 0)
//                );
//        
//        CSG prism = Extrude.points(Vector3d.Z_ONE,  new Vector3d(0, 0, 0),
//                 new Vector3d(0, 0, 0),
//                  new Vector3d(0, 0, 0));
//        
//        CSG result = CSG.fromPolygons(p);
//        
//        return null;
//    }
/**
 *
 * @author cpoliwoda
 */
public class FractalStructure {

//    List<Vector3d> ground = null;
    int numberOfGroundEdges = 3;
    double height = 1.0;
    double thickness = 1.0;
    double NextThickness = thickness / 5.0;
    CSG polyeder = null;

    Vector3d groundCenter = null;
    Vector3d topCenter = null;
    List<Vector3d> groundPoints = null;
    List<Vector3d> topPoints = null;

    List<CSG> subStructures = null;

    int level = 0;

//    public FractalStructure() {
//
//        // x, y, z
//        Vector3d p1 = new Vector3d(0, 1, 0);
//        Vector3d p2 = new Vector3d(-1, 0, 0);
//        Vector3d p3 = new Vector3d(1, 0, 0);
//
//        Vector3d p4 = new Vector3d(0, 1, 1);
//        Vector3d p5 = new Vector3d(-1, 0, 1);
//        Vector3d p6 = new Vector3d(1, 0, 1);
//
//        Polygon ground = Polygon.fromPoints(p1, p2, p3);
//        Polygon leftSide = Polygon.fromPoints(p1, p2, p5, p4);
//        Polygon groundSide = Polygon.fromPoints(p2, p3, p6, p5);
//        Polygon rightSide = Polygon.fromPoints(p1, p4, p6, p3);
//        Polygon top = Polygon.fromPoints(p4, p5, p6);
//
//        polyeder = CSG.fromPolygons(ground, leftSide, groundSide, rightSide, top);
//    }
//
//    private void collectGroundAndTopPoints() {
//        groundPoints = new ArrayList<Vector3d>();
//
//        //the ground points
//        List vertexList = polyeder.getPolygons().get(0).vertices;
//
//        // helpers to calculate the center
//        double cx = 0;
//        double cy = 0;
//        double cz = 0;
//
//        Vector3d tmp = null;
//        int size = vertexList.size();
//
//        for (int i = 0; i < size; i++) {
//            tmp = ((Vertex) vertexList.get(i)).pos;
//
//            //collect the edges of the ground
//            groundPoints.add(tmp);
//
//            //first part calculate the center of the ground
//            cx += tmp.x;
//            cy += tmp.y;
//            cz += tmp.z;
//        }
//        //second part calculate the center of the ground
//        tmp.x = cx / size;
//        tmp.y = cy / size;
//        tmp.z = cz / size;
//
//        // add center of the ground to the ground points
//        groundPoints.add(tmp);
//
//        int polySize = polyeder.getPolygons().size();
//
//        // the top points
//        vertexList = polyeder.getPolygons().get(polySize - 1).vertices;
//
////        did not need to recalculate because ground and top 
////        need to have the same count of points
////        size = vertexList.size();
//        // reset/clear helpers 
//        cx = 0;
//        cy = 0;
//        cz = 0;
//
//        for (int i = 0; i < size; i++) {
//            tmp = ((Vertex) vertexList.get(i)).pos;
//
//            //collect the edges of the ground
//            topPoints.add(tmp);
//
//            //first part calculate the center of the ground
//            cx += tmp.x;
//            cy += tmp.y;
//            cz += tmp.z;
//        }
//        //second part calculate the center of the ground
//        tmp.x = cx / size;
//        tmp.y = cy / size;
//        tmp.z = cz / size;
//
//        // add center of the ground to the ground points
//        topPoints.add(tmp);
//    }
    public FractalStructure(Vector3d groundCenter, Vector3d topCenter,
            int numberOfGroundEdges, double thickness, int level) {

        this.level = level;

        // save the centers
        this.groundCenter = groundCenter;
        this.topCenter = topCenter;

        // create point lists
        groundPoints = new ArrayList<Vector3d>();
        topPoints = new ArrayList<Vector3d>();

        // x, y, z
        //the first point is the most in the north in the x-y-plane
        Vector3d p = null;

        double angel = 0.0;
        double radians = 0.0;
        double radius = thickness / 2.0;

        // add/create the points around the center 
        for (int i = 0; i < numberOfGroundEdges; i++) {

            angel = 360 * i / numberOfGroundEdges;
            radians = (angel - 90) * Math.PI / 180;

            //ground points
            p = new Vector3d(
                    groundCenter.x + radius + radius * Math.cos(radians),
                    groundCenter.y + radius + radius * Math.sin(radians),
                    groundCenter.z);

            groundPoints.add(p);

            //top points
            p = new Vector3d(
                    topCenter.x + radius + radius * Math.cos(radians),
                    topCenter.y + radius + radius * Math.sin(radians),
                    topCenter.z);

            topPoints.add(p);
        }

        //the last points in the list are the center points 
        groundPoints.add(groundCenter);

        topPoints.add(topCenter);

        //where we want to save the substructures
        subStructures = new ArrayList<>();

        if (level == 0) {
            polyeder = createStructure();
            subStructures.add(polyeder);
        } else {
            createSubStructures();
        }
    }

    private CSG createStructure() {
        ArrayList<Polygon> polygonList = new ArrayList();
        ArrayList<Vector3d> tmpList = new ArrayList();

        //all ground points without the center point
        for (int i = 0; i < groundPoints.size() - 1; i++) {
            tmpList.add(groundPoints.get(i));
        }

        //add the ground polygon
        polygonList.add(Polygon.fromPoints(tmpList).flip());

        Vector3d groundP1 = null;
        Vector3d groundP2 = null;
        Vector3d topP1 = null;
        Vector3d topP2 = null;

        //collect the points of the edge planes
        for (int i = 0; i < tmpList.size() - 1; i++) {
            groundP1 = groundPoints.get(i);
            groundP2 = groundPoints.get(i + 1);

            topP1 = topPoints.get(i);
            topP2 = topPoints.get(i + 1);

            // added in counter clockwise orientation: groundP1, groundP2, topP2, topP1
            polygonList.add(Polygon.fromPoints(groundP1, groundP2, topP2, topP1));
        }

        //collect the points of the last edge plane
        groundP1 = groundPoints.get(tmpList.size() - 1);
        groundP2 = groundPoints.get(0);

        topP1 = topPoints.get(tmpList.size() - 1);
        topP2 = topPoints.get(0);

        // added in counter clockwise orientation: groundP1, groundP2, topP2, topP1
        polygonList.add(Polygon.fromPoints(groundP1, groundP2, topP2, topP1));

        //clear tmp list
        tmpList = new ArrayList<>();

        //all top points without the center point
        for (int i = 0; i < topPoints.size() - 1; i++) {
            tmpList.add(topPoints.get(i));
        }

        //add the top polygon
        polygonList.add(Polygon.fromPoints(tmpList));

        polyeder = CSG.fromPolygons(polygonList);

        return polyeder;

    }

    private void createSubStructures() {

    }

    public CSG toCSG() {

        return polyeder;
    }

    public static void main(String[] args) throws IOException {

//        FractalStructure frac = new FractalStructure();
//        frac.collectGroundAndTopPoints();
//         CSG csg =  new FractalStructure(Vector3d.ZERO, Vector3d.Z_ONE, 3, 2, 0).createStructure();
        CSG csg = new FractalStructure(Vector3d.ZERO, Vector3d.Z_ONE, 3, 2, 0).toCSG();

        FileUtil.write(Paths.get("fractal-structure.stl"), csg.toStlString());

        csg.toObj().toFiles(Paths.get("fractal-structure.obj"));

    }

}
