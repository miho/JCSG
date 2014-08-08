package eu.mihosoft.vrl.v3d.samples;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.FileUtil;
import eu.mihosoft.vrl.v3d.Polygon;
import eu.mihosoft.vrl.v3d.Transform;
import eu.mihosoft.vrl.v3d.Vector3d;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author cpoliwoda
 */
public class FractalStructure {

    // decides which kind of polygon base should be created: triangle, hexagon, general n-polygon with n>2
    int numberOfGroundEdges = 3;
//    double height = 1.0;
    double thickness = 1.0;
    // divider 5 makes a good look for the structure
    // divider bigger 5 makes the structure thinner, lower than 5 makes it wider
    double NextThicknessDivider = 6.0;
    // the thickness of the child tubes in the next level
    double NextThickness = thickness / NextThicknessDivider;
    // list which gives the user the controll of thickness in each level
    static ArrayList<Double> thicknessList = null;

    // decides who many connections there should be in the next level between
    // two subFractalStructures (position parent edge and center)
    int crossConnectionsRate = 25; //percent
    // maxAngleForCrossConections dominates crossConnectionsRate
    int maxAngleForCrossConections = 45;//degree

    //the distance between groundCenter and topCenter decides about the height of the tu
    //the center of the bottom polygon of the first FractalStructure (level=0) / tube
    Vector3d groundCenter = null;
    //the center of the top polygon of the first FractalStructure (level=0) / tube
    Vector3d topCenter = null;

    //collection of the bottom polygon points of a FractalStructure (edges & center)
    //used for the new bottom centers of the child FractalStructures
    List<Vector3d> groundPoints = null;
    //collection of the top polygon points of a FractalStructure (edges & center)
    List<Vector3d> topPoints = null;

    //collection of all child tubes, together they build the fractal structure we want
    List<CSG> subStructures = null;

    //how many recursion should be done before drawing (level 0), level 2 means draw after 2 refinments
    int level = 0;

    static {
        thicknessList = new ArrayList<>();
        thicknessList.add(0.01);//level 0
        thicknessList.add(0.1);//level 1
        thicknessList.add(4.0);//level 2
        thicknessList.add(80.0);//level 3
        thicknessList.add(160.0);//level 4
    }

    //we need two vectors which span the plane where the circle lies in       
    Vector3d orthoVecToRotAxis1 = null;
    Vector3d orthoVecToRotAxis2 = null;
    //if dot of two vectors is lower than threshhold we assume they are orthogonal
    double orthoThreshhold = 1E-16;

    /**  
     *
     *  EXAMPLE: 
     *  FractalStructure(Vector3d.ZERO, Vector3d.Z_ONE.times(2), 6, 1, 0) creates a tube with a
     *  top and botton polygon consist of 6 point, a length of 2. The orintation in space is that kind that
     *  z axis goes through the center of the bottom and top polygon. Level 0 means draw these tube.
     *  A Level bigger than 0 means create new tubes with one level decresed, same Lenght and same 
     *  orintation in space in the edges and center.
     *     1 ____6
     *     /          \
     *  2/      c      \5
     *    \             /
     *     \______/
     *      3      4
     * @param groundCenter the center point of the bottom polygon
     * @param topCenter the center point of the top polygon
     * @param numberOfGroundEdges  number which defines polygon should be created (circle divided in N equal parts)
     * @param thickness the distance between the center and all edge points of the bottom and/or top polygon
     * @param level is the number which defines how many recursion should be done
     * @param orthoVecToRotAxis1 is an orthogonal vector to the roation axis (connection line between 
     * groundCenter and topCenter) and normalized, null is valid
     * @param orthoVecToRotAxis2 is an orthogonal vector to the roation axis (connection line between 
     * groundCenter and topCenter) and the orthoVecToRotAxis1 vector and normalized, null is valid
     */
    public FractalStructure(Vector3d groundCenter, Vector3d topCenter,
            int numberOfGroundEdges, double thickness, int level,
            Vector3d orthoVecToRotAxis1, Vector3d orthoVecToRotAxis2) {

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

        //        
        //we need two vectors which span the plane where the circle lies in   
        //        
        //if the user did not give us an orthogonal vector to the rotation axis we need to calculate one
        if (orthoVecToRotAxis1 != null) {

            //checking EQUAL to ZERO is a BAD IDEA
            if (Math.abs(orthoVecToRotAxis1.dot(rotationAxis)) < orthoThreshhold) {
                this.orthoVecToRotAxis1 = orthoVecToRotAxis1.normalized();
            } else {
                this.orthoVecToRotAxis1 = rotationAxis.orthogonal().normalized();
            }
        } else {
            this.orthoVecToRotAxis1 = rotationAxis.orthogonal().normalized();
        }

        //if the user did not give us an second orthogonal vector to the rotation axis and orthoVecToRotAxis1 we need to calculate one
        if (orthoVecToRotAxis2 != null) {
            //checking EQUAL to ZERO is a BAD IDEA
            if ((Math.abs(orthoVecToRotAxis2.dot(this.orthoVecToRotAxis1)) < orthoThreshhold)
                    && Math.abs(orthoVecToRotAxis2.dot(rotationAxis)) < orthoThreshhold) {
                this.orthoVecToRotAxis2 = orthoVecToRotAxis2.normalized();
            } else {
                this.orthoVecToRotAxis2 = rotationAxis.cross(this.orthoVecToRotAxis1).normalized();
            }
        } else {
            this.orthoVecToRotAxis2 = rotationAxis.cross(this.orthoVecToRotAxis1).normalized();
        }

        // x, y, z
        //the first point is the most in the north in the x-y-plane
        Vector3d circlePoint = null;

        double angleStepSize = 360.0 / numberOfGroundEdges;
        double angle = 0;
        double radians = 0;// needed for cos & sin
        double radius = thickness / 2.0;  // fallback rule if the user did not give a thickness for a level

        try {
            radius = thicknessList.get(level);
        } catch (Exception e) {
            System.out.println("no entry found in thicknessList for level = " + level + ", therefore rule used: radius = thickness / 2.0");
        }

        double x = 0;
        double y = 0;

        // add/create the points around the ground and top center 
        for (int i = 0; i < numberOfGroundEdges; i++) {

            angle = i * angleStepSize;
            radians = Math.toRadians(angle);
            x = radius * Math.cos(radians);
            y = radius * Math.sin(radians);

            // Plane equation E(x,y) = S + P * x + Q * y
            // with P,Q orthogonal to the center rotation axis and
            // with x,y from the cirlce gives use the cirlce in 3d space
            //ground points
            circlePoint = groundCenter.plus(this.orthoVecToRotAxis1.times(x)).plus(this.orthoVecToRotAxis2.times(y));

            groundPoints.add(circlePoint);

            //top points
            circlePoint = topCenter.plus(this.orthoVecToRotAxis1.times(x)).plus(this.orthoVecToRotAxis2.times(y));

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

    /**
     * Helper methode which creates and draw structure into CSG.
     * Do NOT call this method by your self. This method is called by the constructor.
     * 
     * @return a fractal structure as CSG
     */
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

    /**
     * Helper methode which creates in the center and at the edges new smaller structures
     * with the same orientation in space as the parent structure (one level above) and cross
     * connections between them.
     * Do NOT call this method by your self. This method is called by the constructor.
     * 
     * @return a list with smaller child structures
     */
    private ArrayList<FractalStructure> createSubStructures() {

        //
        // PART 01 - creating subStructures parallel to rotation axis
        //
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
            // subGc = groundEdge - (NextThickness / 2) * (groundCenter - groundEdge )
            subGroundCenter = tmpGroundPoint.minus(groundCenter.minus(tmpGroundPoint).times(correction));

            tmpTopPoint = topPoints.get(i);

            // one of the new a bit translated topCenterpoint 
            // subTc = topEdge - (NextThickness / 2) * (topCenter - topEdge )
            subTopCenter = tmpTopPoint.minus(topCenter.minus(tmpTopPoint).times(correction));

            // create the new subFractalStructure on the edge
            subFractalStructures.add(
                    new FractalStructure(subGroundCenter,
                            subTopCenter,
                            numberOfGroundEdges,
                            NextThickness,
                            level - 1,
                            orthoVecToRotAxis1,
                            orthoVecToRotAxis2));

        }
        // create the subStructure in the center
        subFractalStructures.add(
                new FractalStructure(groundCenter,
                        topCenter,
                        numberOfGroundEdges,
                        NextThickness,
                        level - 1,
                        orthoVecToRotAxis1,
                        orthoVecToRotAxis2));

        //
        // PART 02 - creating stabilizing subStructures (cross connections)
        //
        /**
         * 
         *               ET  CT
         *                 |  /|hCP2
         *                 | / |
         *         hEP2  |/  |
         *                 |\  |
         *                 | \ |
         *                 |  \|
         *                 |  /|hCP1
         *                 | / |
         *    __  hEP1 |/  |
         *     |           |\  |
         *                 | \ |
         *   sSoCL      |  \|
         *                 |  /|hCP0
         *                 | / |
         *    _|_ hEP0 |/  |
         *               EG  CG
         * 
         */
        ArrayList<FractalStructure> crossSubFractalStructures = new ArrayList<>();

        FractalStructure centerStructure = subFractalStructures.get(subFractalStructures.size() - 1);
        FractalStructure tmpStructure = null;

        //
        //helper points for creating the cross connections to the center
        //
        // top and ground of the center subStructure
        Vector3d centerGroundPoint = centerStructure.groundCenter;
        Vector3d centerTopPoint = centerStructure.topCenter;
        // top and ground of the edge subStructures
        tmpGroundPoint = null;
        tmpTopPoint = null;
        // helper points on connection between edge ground and edge top
        Vector3d helpEdgePoint = null;
        // helper points on connection between center ground and center top
        Vector3d helpCenterPoint = null;

        // vector that shows / discribes the connection line from ground to top
        // the same for edge line and for center line because parallel and have the same lenght
        Vector3d connectionLineVector = centerTopPoint.minus(centerGroundPoint);
        Vector3d connectionLineVectorNormalized = connectionLineVector.normalized();

        //discribes where the help(Edge/Center)Points should lie on connection line of ground and top
        double stepSizeOnConnectionLine = 10.0 / crossConnectionsRate * connectionLineVector.magnitude(); // sSoCL
        double stepSizeOnConnectionLineHalf = stepSizeOnConnectionLine / 2.0;

        // create cross connections from all edge subStructures to the center subStructure
        for (int i = 0; i < subFractalStructures.size() - 1; i++) {

            tmpStructure = subFractalStructures.get(i);
            tmpGroundPoint = tmpStructure.groundCenter;

            // optional part , needed to reduce cross connections with big increase
            // 
            // check maxAngleForCrossConections for angle a
            // 
            //                |    / |hCP0
            //                |   /  |
            //         hEP0 | /a  |
            //              EG    CG
            //
            // in rectangular triangle EG, CG, hCP0
            // cos(a) = | ANKATHETE | / | HYPOTHENUSE |
            // cos(a) = | CG - EG | / | hCP0 - EG |
            //
            //hCP0
            helpCenterPoint = connectionLineVectorNormalized.times(stepSizeOnConnectionLineHalf).plus(centerGroundPoint);
            // tmpGroundPoint = EG
            // centerGroundPoint = CG

            double ankathete = centerGroundPoint.minus(tmpGroundPoint).magnitude();
            double hypothenuse = helpCenterPoint.minus(tmpGroundPoint).magnitude();
            double angle = Math.toDegrees(Math.acos(ankathete / hypothenuse));

            //check maxAngleForCrossConections for angle a and recalculate stepsize until angle
            while (angle >= maxAngleForCrossConections) {
                stepSizeOnConnectionLine = stepSizeOnConnectionLineHalf;
                stepSizeOnConnectionLineHalf /= 2.0;

                helpCenterPoint = connectionLineVectorNormalized.times(stepSizeOnConnectionLineHalf).plus(centerGroundPoint);
                hypothenuse = helpCenterPoint.minus(tmpGroundPoint).magnitude();
                angle = Math.toDegrees(Math.acos(ankathete / hypothenuse));
            }

            // prevent that the cross connactions are to low in the bottom plane
            Vector3d correctionInRotationAxisDirection = connectionLineVectorNormalized.times(stepSizeOnConnectionLineHalf / 2.0);

            // help vector to reduce the calculations of second orthogonal vector in sub structures
            // and make the orientation of the cross connections 'north pole'
            Vector3d secondOrthoVec = null;

            // create multiple cross connections from ONE edge subStructure to the center subStructure
            for (double j = 0; j < connectionLineVector.magnitude(); j += stepSizeOnConnectionLine) {

                //from bottom left to top right beginning at the ground point position
                //hEP0,2,4,....
                helpEdgePoint = connectionLineVectorNormalized.times(j).plus(tmpGroundPoint).plus(correctionInRotationAxisDirection);
                //hCP0,1,2,....
                helpCenterPoint = connectionLineVectorNormalized.times(j).plus(connectionLineVectorNormalized.times(stepSizeOnConnectionLineHalf)).plus(centerGroundPoint).plus(correctionInRotationAxisDirection);

                if (secondOrthoVec == null) {
                    secondOrthoVec = connectionLineVectorNormalized.cross(helpCenterPoint.minus(helpEdgePoint));
                }

                // prevent that the last cross connactions from bottom left to top right has a to above end point in the top plane
                if (connectionLineVector.magnitude() > helpCenterPoint.minus(centerGroundPoint).magnitude()) {
                    // collects the cross subStructure from bottom left to top right
                    crossSubFractalStructures.add(
                            new FractalStructure(helpEdgePoint,
                                    helpCenterPoint,
                                    numberOfGroundEdges,
                                    NextThickness,
                                    level - 1,
                                    connectionLineVectorNormalized, secondOrthoVec));
                }

                //from top left to bottom right beginning at the ground point position
                //hEP1,3,5,....
                helpEdgePoint = connectionLineVectorNormalized.times(j + stepSizeOnConnectionLine).plus(tmpGroundPoint).plus(correctionInRotationAxisDirection);

                // prevent that the last cross connactions from top left to bottom right has a to above end point in the top plane
                if (connectionLineVector.magnitude() > helpEdgePoint.minus(tmpGroundPoint).magnitude()) {
//
                    // collects the cross subStructure from top left to bottom right
                    crossSubFractalStructures.add(
                            new FractalStructure(helpEdgePoint,
                                    helpCenterPoint,
                                    numberOfGroundEdges,
                                    NextThickness,
                                    level - 1,
                                    connectionLineVectorNormalized, secondOrthoVec));
                }

            }//for cross connections to center

        }//for edges

        subFractalStructures.addAll(crossSubFractalStructures);

        // create cross connections from one edge subStructures to the neighbour edge subStructure
        return subFractalStructures;

    }

    public CSG toCSG() {

        List<Polygon> polygons = new ArrayList<>();

        subStructures.stream().forEach(csg -> polygons.addAll(csg.getPolygons()));

        return CSG.fromPolygons(polygons);
    }

    public static void main(String[] args) throws IOException {

        CSG csg = new FractalStructure(Vector3d.ZERO, Vector3d.Z_ONE.times(1), 4, 15, 2,
                                Vector3d.X_ONE, Vector3d.Y_ONE
//                null, null
        ).toCSG();
//        CSG csg = new FractalStructure(Vector3d.ZERO, Vector3d.Z_ONE, 7, 2, 1).toCSG();
//        CSG csg = new FractalStructure(new Vector3d(-1, -1, -1), new Vector3d(1, 1, 1), 7, 4, 3).toCSG();

        FileUtil.write(Paths.get("fractal-structure.stl"), csg.toStlString());

        csg.toObj().toFiles(Paths.get("fractal-structure.obj"));

    }

}
