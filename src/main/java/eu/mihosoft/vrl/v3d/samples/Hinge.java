/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d.samples;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Cube;
import eu.mihosoft.vrl.v3d.Cylinder;
import eu.mihosoft.vrl.v3d.FileUtil;
import eu.mihosoft.vrl.v3d.Plane;
import eu.mihosoft.vrl.v3d.Transform;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.nio.file.Paths;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class Hinge {

    private double jointLength = 10;
    private double jointRadius = 5;
    private double coneLength = 5;
    private double jointHoleLength = 5;
    private double jointConnectionThickness = jointRadius * 0.5;
    private int resolution = 16;

    public Hinge() {
        //
    }

    public static void main(String[] args) throws IOException {
        FileUtil.write(Paths.get("hinge.stl"), new Hinge().toCSG().toStlString());
    }

    public CSG toCSG() {

        CSG sideConeR = new Cylinder(getJointRadius(), 0, getConeLength(), getResolution()).toCSG().
                transformed(Transform.unity().translateZ(getJointLength() * 0.5));
        CSG sideConeL = new Cylinder(getJointRadius(), 0, getConeLength(), getResolution()).toCSG().
                transformed(Transform.unity().translateZ(-getJointLength() * 0.5).rotX(180));

        CSG sideCones = sideConeL.union(sideConeR);
        
        CSG conesAndCyl = sideCones.hull();


        CSG cylinderHole = new Cube(getJointRadius() * 2, getJointHoleLength() * 2, getJointHoleLength()).toCSG().
                transformed(Transform.unity().translate(getJointConnectionThickness(), 0, -getJointHoleLength() * 0.0));

        CSG joint = conesAndCyl.difference(cylinderHole);

        return joint;
    }

    public Hinge setJointLength(double jointLength) {
        this.jointLength = jointLength;
        return this;
    }

    public Hinge setJointRadius(double jointRadius) {
        this.jointRadius = jointRadius;
        return this;
    }

    public Hinge setConeLength(double coneLength) {
        this.coneLength = coneLength;
        return this;
    }

    public Hinge setJointHoleLength(double jointHoleLength) {
        this.jointHoleLength = jointHoleLength;
        return this;
    }

    public Hinge setJointConnectionThickness(double jointConnectionThickness) {
        this.jointConnectionThickness = jointConnectionThickness;
        return this;
    }

    public Hinge setResolution(int resolution) {
        this.resolution = resolution;
        return this;
    }

    /**
     * @return the jointLength
     */
    public double getJointLength() {
        return jointLength;
    }

    /**
     * @return the jointRadius
     */
    public double getJointRadius() {
        return jointRadius;
    }

    /**
     * @return the coneLength
     */
    public double getConeLength() {
        return coneLength;
    }

    /**
     * @return the jointHoleLength
     */
    public double getJointHoleLength() {
        return jointHoleLength;
    }

    /**
     * @return the jointConnectionThickness
     */
    public double getJointConnectionThickness() {
        return jointConnectionThickness;
    }

    /**
     * @return the resolution
     */
    public int getResolution() {
        return resolution;
    }
}
