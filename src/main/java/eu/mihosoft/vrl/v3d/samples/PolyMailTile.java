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
import static eu.mihosoft.vrl.v3d.Transform.unity;
import eu.mihosoft.vrl.v3d.Vector3d;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

// TODO: Auto-generated Javadoc
enum TileType {

    MALE,
    FEMALE,
    COMBINED
}

/**
 * The Class PolyMailTile.
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class PolyMailTile {

    /** The tile type. */
    private TileType tileType = TileType.MALE;

    /** The radius. */
    private double radius = 10;

    /** The thickness. */
    private double thickness = 2.2;

    /** The joint radius. */
    private double jointRadius = 1.1;
    
    /** The cone length. */
    private double coneLength = 1.8;
    
    /** The hinge hole scale. */
    private double hingeHoleScale = 1.16;

    /** The pin length. */
    private double pinLength = 1;
    
    /** The pin thickness. */
    private double pinThickness = 2.0;

    /** The num edges. */
    private int numEdges = 3;

    /**
     * To csg.
     *
     * @return the csg
     */
    public CSG toCSG() {

//        CSG.setDefaultOptType(CSG.OptType.POLYGON_BOUND);
        double step = 360.0 / numEdges;
        double initialRot = step * 0.5;

        CSG mainPrism = new Cylinder(getRadius(), getThickness(), numEdges).toCSG().
                transformed(unity().translateZ(-getThickness() * 0.5).rotZ(initialRot));

        Hinge hingePrototype = new Hinge().setJointRadius(getJointRadius()).
                setJointLength(getPinThickness()).setConeLength(getConeLength());
        hingePrototype.setJointConnectionThickness(
                hingePrototype.getJointRadius() * 2);

        CSG hinge1 = hingePrototype.toCSG();

        Vector3d hingeBounds = hinge1.getBounds().getBounds();

        hinge1 = hinge1.intersect(new Cube(hingeBounds.x,
                Math.min(hingeBounds.y, getThickness()), hingeBounds.z).toCSG());

        hinge1 = hinge1.transformed(unity().rotX(90));

        CSG pin = new Cube(getPinLength() + hingePrototype.getJointRadius(), getPinThickness(), getThickness()).toCSG().transformed(unity().
                translateX(-(jointRadius + pinLength) * 0.5));

        hinge1 = hinge1.union(pin);

        double apothem = getApothem();

        hinge1 = hinge1.transformed(unity().
                translateX(apothem + hingePrototype.getJointRadius()
                        + getPinLength()));

        List<CSG> hinges = new ArrayList<>();

        hinges.add(hinge1);

        for (int i = 1; i < numEdges; i++) {
            CSG hinge = hinge1.transformed(unity().rotZ(i * step));
            hinges.add(hinge);
        }

        CSG hingeHole1 = hinge1.transformed(unity().translateX(
                -apothem - hingePrototype.getJointRadius()
                - getPinLength()));
        hingeHole1 = hingeHole1.transformed(unity().scale(getHingeHoleScale()));
        hingeHole1 = hingeHole1.transformed(unity().translateX(
                -apothem + getJointRadius() * getHingeHoleScale()));

        // TODO get rid of this
        if (numEdges % 2 != 0) {
            hingeHole1 = hingeHole1.transformed(unity().rotZ(initialRot));
        }

        List<CSG> hingeHoles = new ArrayList<>();

        hingeHoles.add(hingeHole1);

        for (int i = 1; i < numEdges; i++) {
            CSG hole = hingeHole1.transformed(unity().rotZ(i * step));
            hingeHoles.add(hole);
        }

        CSG malePart = mainPrism.union(hinges);
        CSG femalePart = mainPrism.difference(hingeHoles);

        CSG combinedPart = mainPrism.clone();
        
        for (int i = 0; i < numEdges; i++) {
            if (i % 2 == 0) {
                combinedPart = combinedPart.union(hinges.get(i));
            } else {
                combinedPart = combinedPart.difference(
                        hingeHoles.get(i).transformed(unity().rotZ(step)));
            }
        }

        if (numEdges % 2 != 0 && isCombined()) {
            throw new IllegalArgumentException(
                    "Combined type can only be used for even edge numbers.");
        }

        if (isMale()) {
            return malePart;
        } else if (isFemale()) {
            return femalePart;
        } else if (isCombined()) {
            return combinedPart;
        }
        
        return mainPrism;
    }

    /**
     * The main method.
     *
     * @param args the arguments
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void main(String[] args) throws IOException {
        FileUtil.write(Paths.get("triangularmail.stl"), new PolyMailTile().setNumEdges(6).setCombined().toCSG().toStlString());
    }

    /**
     * Checks if is male.
     *
     * @return the male
     */
    public boolean isMale() {
        return tileType == TileType.MALE;
    }

    /**
     * Checks if is female.
     *
     * @return the male
     */
    public boolean isFemale() {
        return tileType == TileType.FEMALE;
    }

    /**
     * Checks if is combined.
     *
     * @return true, if is combined
     */
    public boolean isCombined() {
        return tileType == TileType.COMBINED;
    }

    /**
     * Sets the male.
     *
     * @return the poly mail tile
     */
    public PolyMailTile setMale() {
        this.tileType = TileType.MALE;

        return this;
    }

    /**
     * Sets the female.
     *
     * @return the poly mail tile
     */
    public PolyMailTile setFemale() {
        this.tileType = TileType.FEMALE;

        return this;
    }

    /**
     * Sets the combined.
     *
     * @return the poly mail tile
     */
    public PolyMailTile setCombined() {
        this.tileType = TileType.COMBINED;

        return this;
    }

    /**
     * Gets the radius.
     *
     * @return the radius
     */
    public double getRadius() {
        return radius;
    }

    /**
     * Sets the radius.
     *
     * @param radius the radius to set
     * @return the poly mail tile
     */
    public PolyMailTile setRadius(double radius) {
        this.radius = radius;

        return this;
    }

    /**
     * Gets the thickness.
     *
     * @return the thickness
     */
    public double getThickness() {
        return thickness;
    }

    /**
     * Sets the thickness.
     *
     * @param thickness the thickness to set
     * @return the poly mail tile
     */
    public PolyMailTile setThickness(double thickness) {
        this.thickness = thickness;

        return this;
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
     * Sets the joint radius.
     *
     * @param jointRadius the jointRadius to set
     * @return the poly mail tile
     */
    public PolyMailTile setJointRadius(double jointRadius) {
        this.jointRadius = jointRadius;

        return this;
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
     * Sets the cone length.
     *
     * @param coneLength the coneLength to set
     * @return the poly mail tile
     */
    public PolyMailTile setConeLength(double coneLength) {
        this.coneLength = coneLength;

        return this;
    }

    /**
     * Gets the hinge hole scale.
     *
     * @return the hingeHoleScale
     */
    public double getHingeHoleScale() {
        return hingeHoleScale;
    }

    /**
     * Sets the hinge hole scale.
     *
     * @param hingeHoleScale the hingeHoleScale to set
     * @return the poly mail tile
     */
    public PolyMailTile setHingeHoleScale(double hingeHoleScale) {
        this.hingeHoleScale = hingeHoleScale;

        return this;
    }

    /**
     * Gets the pin length.
     *
     * @return the pinLength
     */
    public double getPinLength() {
        return pinLength;
    }

    /**
     * Sets the pin length.
     *
     * @param pinLength the pinLength to set
     * @return the poly mail tile
     */
    public PolyMailTile setPinLength(double pinLength) {
        this.pinLength = pinLength;

        return this;
    }

    /**
     * Gets the pin thickness.
     *
     * @return the pinThickness
     */
    public double getPinThickness() {
        return pinThickness;
    }

    /**
     * Sets the pin thickness.
     *
     * @param pinThickness the pinThickness to set
     * @return the poly mail tile
     */
    public PolyMailTile setPinThickness(double pinThickness) {
        this.pinThickness = pinThickness;

        return this;
    }

    /**
     * Sets the num edges.
     *
     * @param numEdges the num edges
     * @return the poly mail tile
     */
    public PolyMailTile setNumEdges(int numEdges) {
        this.numEdges = numEdges;

        return this;
    }

    /**
     * Gets the num edges.
     *
     * @return the num edges
     */
    public int getNumEdges() {
        return this.numEdges;
    }

    /**
     * Gets the side length.
     *
     * @return the side length
     */
    public double getSideLength() {
        return 2 * radius * Math.sin(Math.toRadians(180.0 / numEdges));
    }

    /**
     * Gets the apothem.
     *
     * @return the apothem
     */
    public double getApothem() {
        return getRadius() * Math.cos(Math.toRadians(180.0 / numEdges));
    }
}
