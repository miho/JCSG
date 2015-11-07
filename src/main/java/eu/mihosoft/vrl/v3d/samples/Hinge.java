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

// TODO: Auto-generated Javadoc
/**
 * The Class Hinge.
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class Hinge {

    /** The joint length. */
    private double jointLength = 10;
    
    /** The joint radius. */
    private double jointRadius = 5;
    
    /** The cone length. */
    private double coneLength = 5;
    
    /** The joint hole length. */
    private double jointHoleLength = 5;
    
    /** The joint connection thickness. */
    private double jointConnectionThickness = jointRadius * 0.5;
    
    /** The resolution. */
    private int resolution = 16;

    /**
     * Instantiates a new hinge.
     */
    public Hinge() {
        //
    }

    /**
     * The main method.
     *
     * @param args the arguments
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void main(String[] args) throws IOException {
        FileUtil.write(Paths.get("hinge.stl"), new Hinge().toCSG().toStlString());
    }

    /**
     * To csg.
     *
     * @return the csg
     */
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

    /**
     * Sets the joint length.
     *
     * @param jointLength the joint length
     * @return the hinge
     */
    public Hinge setJointLength(double jointLength) {
        this.jointLength = jointLength;
        return this;
    }

    /**
     * Sets the joint radius.
     *
     * @param jointRadius the joint radius
     * @return the hinge
     */
    public Hinge setJointRadius(double jointRadius) {
        this.jointRadius = jointRadius;
        return this;
    }

    /**
     * Sets the cone length.
     *
     * @param coneLength the cone length
     * @return the hinge
     */
    public Hinge setConeLength(double coneLength) {
        this.coneLength = coneLength;
        return this;
    }

    /**
     * Sets the joint hole length.
     *
     * @param jointHoleLength the joint hole length
     * @return the hinge
     */
    public Hinge setJointHoleLength(double jointHoleLength) {
        this.jointHoleLength = jointHoleLength;
        return this;
    }

    /**
     * Sets the joint connection thickness.
     *
     * @param jointConnectionThickness the joint connection thickness
     * @return the hinge
     */
    public Hinge setJointConnectionThickness(double jointConnectionThickness) {
        this.jointConnectionThickness = jointConnectionThickness;
        return this;
    }

    /**
     * Sets the resolution.
     *
     * @param resolution the resolution
     * @return the hinge
     */
    public Hinge setResolution(int resolution) {
        this.resolution = resolution;
        return this;
    }

    /**
     * Gets the joint length.
     *
     * @return the jointLength
     */
    public double getJointLength() {
        return jointLength;
    }

    /**
     * Gets the joint radius.
     *
     * @return the jointRadius
     */
    public double getJointRadius() {
        return jointRadius;
    }

    /**
     * Gets the cone length.
     *
     * @return the coneLength
     */
    public double getConeLength() {
        return coneLength;
    }

    /**
     * Gets the joint hole length.
     *
     * @return the jointHoleLength
     */
    public double getJointHoleLength() {
        return jointHoleLength;
    }

    /**
     * Gets the joint connection thickness.
     *
     * @return the jointConnectionThickness
     */
    public double getJointConnectionThickness() {
        return jointConnectionThickness;
    }

    /**
     * Gets the resolution.
     *
     * @return the resolution
     */
    public int getResolution() {
        return resolution;
    }
}
