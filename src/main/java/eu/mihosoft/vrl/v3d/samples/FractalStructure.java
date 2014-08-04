/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d.samples;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.FileUtil;
import eu.mihosoft.vrl.v3d.Polygon;
import eu.mihosoft.vrl.v3d.Vector3d;
import eu.mihosoft.vrl.v3d.Matrix3d;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
//import javax.vecmath.Matrix3d;

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
    double NextThicknessDivider = 5.0;
    double NextThickness = thickness / NextThicknessDivider;

    Vector3d groundCenter = null;
    Vector3d topCenter = null;
    List<Vector3d> groundPoints = null;
    List<Vector3d> topPoints = null;

    List<CSG> subStructures = null;

    int level = 0;

    /* BACKUP 20140801
    
     public FractalStructure(Vector3d groundCenter, Vector3d topCenter,
     int numberOfGroundEdges, double thickness, int level) {

     NextThickness = thickness / NextThicknessDivider;

     if (numberOfGroundEdges < 3) {
     numberOfGroundEdges = 3;
     System.err.println("numberOfGroundEdges need to be at least 3 and is set therefore to 3.");
     }
     this.numberOfGroundEdges = numberOfGroundEdges;

     this.level = level;

     // save the centers
     this.groundCenter = groundCenter;
     this.topCenter = topCenter;

     // create point lists
     groundPoints = new ArrayList<Vector3d>();
     topPoints = new ArrayList<Vector3d>();

     // x, y, z
     //the first point is the most in the north in the x-y-plane
     Vector3d circlePoint = null;

     double angel = 0.0;
     double radians = 0.0;
     double radius = thickness / 2.0;

     Matrix3d rotationMatrix = null;

     Vector3d rotationAxis = new Vector3d(
     topCenter.x - groundCenter.x,
     topCenter.y - groundCenter.y,
     topCenter.z - groundCenter.z).normalized();

     //helper to reduce space and increase a little bit performance
     double OmCa = 0;
     double cosA = 0;
     double sinA = 0;
     double x = rotationAxis.x;
     double y = rotationAxis.y;
     double z = rotationAxis.z;

     //we need one starting point which we can rotate on the circle        
     Vector3d orthoVecToRotAxis = null;
     //        //http://www.gutefrage.net/tipp/orthogonalen-vektor-zu-einem-vektor-finden
     //        if (z != 0) {
     //            orthoVecToRotAxis = new Vector3d(y, x, -2 * x * y / z);
     //        } else if (y != 0) {
     //            orthoVecToRotAxis = new Vector3d(z, -2 * x * z / y, x);
     //        } else if (x != 0) {
     //            orthoVecToRotAxis = new Vector3d(-2 * y * z / x, z, y);
     //        } else {
     //            System.err.println("ERROR no orthogonal vector found");
     //        }

     if (!rotationAxis.equals(Vector3d.X_ONE)) {
     orthoVecToRotAxis = rotationAxis.cross(Vector3d.X_ONE);

     } else if (!rotationAxis.equals(Vector3d.Y_ONE)) {
     orthoVecToRotAxis = rotationAxis.cross(Vector3d.Y_ONE);

     } else if (!rotationAxis.equals(Vector3d.Z_ONE)) {
     orthoVecToRotAxis = rotationAxis.cross(Vector3d.Z_ONE);
     }

     orthoVecToRotAxis = orthoVecToRotAxis.normalized().times(radius);

     //        /// NEW TRY go in angle step sizes
     //        angel = 360.0 / numberOfGroundEdges;

     // add/create the points around the center 
     for (int i = 0; i < numberOfGroundEdges; i++) {

     angel = 360.0 * i / numberOfGroundEdges;
     radians = (angel - 90) * Math.PI / 180;
     //            angel *= i;

     //            OmCa = (1 - Math.cos(angel));
     //            cosA = Math.cos(angel);
     //            sinA = Math.sin(angel);
     //
     //            //http://de.wikipedia.org/wiki/Drehmatrix#Drehmatrizen_des_Raumes_R.C2.B3
     //            rotationMatrix = new Matrix3d(
     //                    x * x * OmCa + cosA, x * y * OmCa - z * sinA, x * z * OmCa + y * sinA,
     //                    y * x * OmCa + z * sinA, y * y * OmCa + cosA, y * z * OmCa - x * sinA,
     //                    z * x * OmCa - y * sinA, z * y * OmCa + x * sinA, z * z * OmCa + cosA);
     //
     //            circlePoint = rotationMatrix.times(orthoVecToRotAxis);
     //
     //            //ground points
     //            groundPoints.add(circlePoint.plus(groundCenter));

     circlePoint = new Vector3d(
     groundCenter.x + radius * Math.cos(radians),
     groundCenter.y + radius * Math.sin(radians),
     groundCenter.z);
            
     groundPoints.add(circlePoint);
     System.out.println("ground circlePoint: " + circlePoint);

     //top points
     //            topPoints.add(circlePoint.plus(topCenter));

     circlePoint = new Vector3d(
     topCenter.x + radius * Math.cos(radians),
     topCenter.y + radius * Math.sin(radians),
     topCenter.z);
     topPoints.add(circlePoint);
     }

     //the last points in the list are the center points 
     groundPoints.add(groundCenter);

     topPoints.add(topCenter);

     //here we want to save the substructures
     subStructures = new ArrayList<>();

     if (level == 0) {
     subStructures.add(createStructure());
     } else {
     ArrayList<FractalStructure> subFractals = createSubStructures();

     for (int i = 0; i < subFractals.size(); i++) {
     subStructures.add(subFractals.get(i).toCSG());

     }
     }

     }
    
     */
    public FractalStructure(Vector3d groundCenter, Vector3d topCenter,
            int numberOfGroundEdges, double thickness, int level) {

        NextThickness = thickness / NextThicknessDivider;

        if (numberOfGroundEdges < 3) {
            numberOfGroundEdges = 3;
            System.err.println("numberOfGroundEdges need to be at least 3 and is set therefore to 3.");
        }
        this.numberOfGroundEdges = numberOfGroundEdges;

        this.level = level;

        // save the centers
        this.groundCenter = groundCenter;
        this.topCenter = topCenter;

        // create point lists
        groundPoints = new ArrayList<Vector3d>();
        topPoints = new ArrayList<Vector3d>();

        // Circle equation C_r_(x,y):  x^2 + y^2 = r^2
        // with x = r * cos(angle)
        //      y = r * sin(angle)
        // 
        // Plane equation E(x,y) = S + P * x + Q * y 
        // with vectors S, P, Q and  P othogonal to Q
        // 
        Vector3d rotationAxis = new Vector3d(
                topCenter.x - groundCenter.x,
                topCenter.y - groundCenter.y,
                topCenter.z - groundCenter.z).normalized();

        //we need two vectors which span the plane where the circle lies in       
        Vector3d orthoVecToRotAxis1 = rotationAxis.orthogonal().normalized();
        Vector3d orthoVecToRotAxis2 = rotationAxis.cross(orthoVecToRotAxis1).normalized();

        System.out.println(" orthoVecToRotAxis1 = " + orthoVecToRotAxis1);
        System.out.println(" orthoVecToRotAxis2 = " + orthoVecToRotAxis2);
        
        // x, y, z
        //the first point is the most in the north in the x-y-plane
        Vector3d circlePoint = null;

        double angelStepSize = 360.0 / numberOfGroundEdges;
        double angel = 0;
        double radians = 0;// needed for cos & sin
        double radius = thickness / 2.0;
        double x = 0;
        double y = 0;

        System.out.println(" angelStepSize = "+ angelStepSize);
        
        // add/create the points around the ground and top center 
        for (int i = 0; i < numberOfGroundEdges; i++) {

            angel = i * angelStepSize;
            radians = (angel - 90) * Math.PI / 180;
            x = radius * Math.cos(radians);
            y = radius * Math.sin(radians);

            System.out.println(" angel = "+ angel);
            System.out.println(" radians = "+ radians);
            System.out.println(" Math.cos(radians) = "+ Math.cos(radians));
            System.out.println(" Math.sin(radians) = "+ Math.sin(radians));
            
            
            
            // Plane equation E(x,y) = S + P * x + Q * y
            // with P,Q orthogonal to the center rotation axis and
            // with x,y from the cirlce gives use the cirlce in 3d space
            
            //ground points
            System.out.println("groundCenter = "+ groundCenter);
            System.out.println("orthoVecToRotAxis1.times(x) = "+ orthoVecToRotAxis1.times(x));
            System.out.println("orthoVecToRotAxis2.times(y) = "+ orthoVecToRotAxis2.times(y));
            
            circlePoint = groundCenter.plus(orthoVecToRotAxis1.times(x)).plus(orthoVecToRotAxis2.times(y));

            groundPoints.add(circlePoint);
            System.out.println("ground circlePoint "+i+": " + circlePoint);

            //top points
            circlePoint = topCenter.plus(orthoVecToRotAxis1.times(x)).plus(orthoVecToRotAxis2.times(y));
            
            topPoints.add(circlePoint);
        }

        //the last points in the list are the center points 
        groundPoints.add(groundCenter);

        topPoints.add(topCenter);

        //here we want to save the substructures
        subStructures = new ArrayList<>();

        if (level == 0) {
            subStructures.add(createStructure());
        } else {
            ArrayList<FractalStructure> subFractals = createSubStructures();

            for (int i = 0; i < subFractals.size(); i++) {
                subStructures.add(subFractals.get(i).toCSG());

            }
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
        //flip is needed to set the normal int the right direction (out)
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

        return CSG.fromPolygons(polygonList);

    }

    private ArrayList<FractalStructure> createSubStructures() {

        Vector3d subGroundCenter = null;
        Vector3d subTopCenter = null;

        ArrayList<FractalStructure> subFractalStructures = new ArrayList<>();

        Vector3d tmpGroundPoint = null;
        Vector3d tmpTopPoint = null;

        // is a bias the new center points needs to lie a bit more to the center
        // co the diameter of the fractal won't be increased.
        double correction = -1 * NextThickness / 2.0;

        // create the edge subStructures 
        for (int i = 0; i < numberOfGroundEdges; i++) {

            tmpGroundPoint = groundPoints.get(i);

            // one of the new a bit translated groundCenterpoint 
//            subGroundCenter = new Vector3d(
//                    tmpGroundPoint.x - correction * (groundCenter.x - tmpGroundPoint.x),
//                    tmpGroundPoint.y - correction * (groundCenter.y - tmpGroundPoint.y),
//                    tmpGroundPoint.z - correction * (groundCenter.z - tmpGroundPoint.z));
            subGroundCenter = tmpGroundPoint.minus(groundCenter.minus(tmpGroundPoint).times(correction));

            tmpTopPoint = topPoints.get(i);

            // one of the new a bit translated topCenterpoint 
//            subTopCenter = new Vector3d(
//                    tmpTopPoint.x - correction * (topCenter.x - tmpTopPoint.x),
//                    tmpTopPoint.y - correction * (topCenter.y - tmpTopPoint.y),
//                    tmpTopPoint.z - correction * (topCenter.z - tmpTopPoint.z));
            subTopCenter = tmpTopPoint.minus(topCenter.minus(tmpTopPoint).times(correction));

            // create the new subFractalStructure
            subFractalStructures.add(
                    new FractalStructure(subGroundCenter,
                            subTopCenter,
                            numberOfGroundEdges,
                            NextThickness,
                            level - 1));

        }
        // create the subStructure in the center
        subFractalStructures.add(
                new FractalStructure(groundCenter,
                        topCenter,
                        numberOfGroundEdges,
                        NextThickness,
                        level - 1));

        return subFractalStructures;

    }

    public CSG toCSG() {

        List<Polygon> polygons = new ArrayList<>();

        subStructures.stream().forEach(csg -> polygons.addAll(csg.getPolygons()));

        return CSG.fromPolygons(polygons);
    }

    public static void main(String[] args) throws IOException {

//        FractalStructure frac = new FractalStructure();
//        frac.collectGroundAndTopPoints();
        CSG csg = new FractalStructure(Vector3d.ZERO, Vector3d.Z_ONE, 6, 4, 0).createStructure();
//        CSG csg = new FractalStructure(Vector3d.ZERO, Vector3d.Z_ONE, 7, 2, 1).toCSG();
//        CSG csg = new FractalStructure(Vector3d.ZERO, new Vector3d(5, 5, 1), 7, 2, 2).toCSG();

        FileUtil.write(Paths.get("fractal-structure.stl"), csg.toStlString());

        csg.toObj().toFiles(Paths.get("fractal-structure.obj"));

    }

}

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
