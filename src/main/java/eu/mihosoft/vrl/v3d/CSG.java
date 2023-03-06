/**
 * CSG.java
 *
 * Copyright 2014-2014 Michael Hoffer info@michaelhoffer.de. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer info@michaelhoffer.de "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer info@michaelhoffer.de OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of Michael Hoffer
 * info@michaelhoffer.de.
 */
package eu.mihosoft.vrl.v3d;


import eu.mihosoft.vrl.v3d.ext.quickhull3d.HullUtil;
import eu.mihosoft.vrl.v3d.parametrics.CSGDatabase;
import eu.mihosoft.vrl.v3d.parametrics.IParametric;
import eu.mihosoft.vrl.v3d.parametrics.IRegenerate;
import eu.mihosoft.vrl.v3d.parametrics.LengthParameter;
import eu.mihosoft.vrl.v3d.parametrics.Parameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.neuronrobotics.interaction.CadInteractionEvent;

import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Affine;

// TODO: Auto-generated Javadoc
/**
 * Constructive Solid Geometry (CSG).
 *
 * This implementation is a Java port of
 * 
 * href="https://github.com/evanw/csg.js/" https://github.com/evanw/csg.js/ with
 * some additional features like polygon extrude, transformations etc. Thanks to
 * the author for creating the CSG.js library.<br>
 * <br>
 *
 * Implementation Details
 *
 * All CSG operations are implemented in terms of two functions,
 * {@link Node#clipTo(eu.mihosoft.vrl.v3d.Node)} and {@link Node#invert()},
 * which remove parts of a BSP tree inside another BSP tree and swap solid and
 * empty space, respectively. To find the union of {@code a} and {@code b}, we
 * want to remove everything in {@code a} inside {@code b} and everything in
 * {@code b} inside {@code a}, then combine polygons from {@code a} and
 * {@code b} into one solid:
 *
 * 
 * a.clipTo(b); b.clipTo(a); a.build(b.allPolygons());
 * 
 *
 * The only tricky part is handling overlapping coplanar polygons in both trees.
 * The code above keeps both copies, but we need to keep them in one tree and
 * remove them in the other tree. To remove them from {@code b} we can clip the
 * inverse of {@code b} against {@code a}. The code for union now looks like
 * this:
 *
 * 
 * a.clipTo(b); b.clipTo(a); b.invert(); b.clipTo(a); b.invert();
 * a.build(b.allPolygons());
 * 
 *
 * Subtraction and intersection naturally follow from set operations. If union
 * is {@code A | B}, differenceion is {@code A - B = ~(~A | B)} and intersection
 * is {@code A & B =
 * ~(~A | ~B)} where {@code ~} is the complement operator.
 */

@SuppressWarnings("restriction")
public class CSG implements IuserAPI{

	private static int numFacesInOffset = 15;

	/** The polygons. */
	private List<Polygon> polygons;

	/** The default opt type. */
	private static OptType defaultOptType = OptType.CSG_BOUND;

	/** The opt type. */
	private OptType optType = null;

	/** The storage. */
	private PropertyStorage str;
	private PropertyStorage assembly;
	
	/** The current. */
	private MeshView current;
	
	private static Color defaultcolor=Color.web("#007956");

	/** The color. */
	private Color color=getDefaultColor();

	/** The manipulator. */
	private Affine manipulator;
	private Bounds bounds;
	public static final int INDEX_OF_PARAMETRIC_DEFAULT = 0;
	public static final int INDEX_OF_PARAMETRIC_LOWER = 1;
	public static final int INDEX_OF_PARAMETRIC_UPPER = 2;
	private ArrayList<String> groovyFileLines = new ArrayList<>();
	private PrepForManufacturing manufactuing = null;
	private HashMap<String, IParametric> mapOfparametrics = null;
	private IRegenerate regenerate = null;
	private boolean markForRegeneration = false;
	private String name = "";
	private ArrayList<Transform> slicePlanes=null;
	private ArrayList<String> exportFormats=null;
	private ArrayList<Transform> datumReferences=null;
	private static boolean useStackTraces=true;
	
	private static ICSGProgress progressMoniter=new ICSGProgress() {
		@Override
		public void progressUpdate(int currentIndex, int finalIndex, String type, CSG intermediateShape) {
			System.out.println(type+"ing "+currentIndex+" of "+finalIndex);
		}
	};

	/**
	 * Instantiates a new csg.
	 */
	public CSG() {
		setStorage(new PropertyStorage());

		if (useStackTraces) {
			// This is the trace for where this csg was created
			addStackTrace(new Exception());
		}
	}
	
	public CSG addDatumReference(Transform t) {
		if(getDatumReferences()==null)
			setDatumReferences(new ArrayList<Transform>());
		getDatumReferences().add(t);
		return this;
	}

	public CSG prepForManufacturing() {
		if (getManufacturing() == null)
			return this;
		CSG ret = getManufacturing().prep(this);
		if(ret == null)
			return null;
		ret.setName(getName());
		ret.setColor(getColor());
		ret.slicePlanes=slicePlanes;
		ret.mapOfparametrics=mapOfparametrics;
		ret.exportFormats=exportFormats;
		return ret;
	}
	/**
	 * Gets the color.
	 *
	 * @return the color
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Sets the color.
	 *
	 * @param color
	 *            the new color
	 */
	public CSG setColor(Color color) {
		this.color = color;
		if (current != null) {
			PhongMaterial m = new PhongMaterial(getColor());
			current.setMaterial(m);
		}
		return this;
	}
	/**
	 * Sets the Temporary color.
	 *
	 * @param Temporary color
	 *            the new Temporary  color
	 */
	public CSG setTemporaryColor(Color color) {
		if (current != null) {
			PhongMaterial m = new PhongMaterial(color);
			current.setMaterial(m);
		}
		return this;
	}

	/**
	 * Sets the manipulator.
	 *
	 * @param manipulator
	 *            the manipulator
	 * @return the affine
	 */
	public CSG setManipulator(Affine manipulator) {
		if (manipulator == null)
			return this;
		Affine old = manipulator;
		this.manipulator = manipulator;
		if (current != null) {
			current.getTransforms().clear();
			current.getTransforms().add(manipulator);
		}
		return this;
	}

	/**
	 * Gets the mesh.
	 *
	 * @return the mesh
	 */
	public MeshView getMesh() {
		if (current != null)
			return current;
		current = newMesh();
		return current;
	}
	/**
	 * Gets the mesh.
	 *
	 * @return the mesh
	 */
	public MeshView newMesh() {

		MeshContainer meshContainer = toJavaFXMesh(null);

		MeshView current = meshContainer.getAsMeshViews().get(0);

		PhongMaterial m = new PhongMaterial(getColor());
		current.setMaterial(m);
		

		boolean hasManipulator = getManipulator() != null;
		boolean hasAssembly = getAssemblyStorage().getValue("AssembleAffine")!=Optional.empty();

		if (hasManipulator || hasAssembly)
			current.getTransforms().clear();

		if (hasManipulator)
			current.getTransforms().add(getManipulator());
		if (hasAssembly)
			current.getTransforms().add((Affine)getAssemblyStorage().getValue("AssembleAffine").get());

		current.setCullFace(CullFace.NONE);
		return current;
	}
	

	/**
	 * To z min.
	 *
	 * @param target
	 *            the target
	 * @return the csg
	 */
	public CSG toZMin(CSG target) {
		return this.transformed(new Transform().translateZ(-target.getBounds().getMin().z));
	}

	/**
	 * To z max.
	 *
	 * @param target
	 *            the target
	 * @return the csg
	 */
	public CSG toZMax(CSG target) {
		return this.transformed(new Transform().translateZ(-target.getBounds().getMax().z));
	}

	/**
	 * To x min.
	 *
	 * @param target
	 *            the target
	 * @return the csg
	 */
	public CSG toXMin(CSG target) {
		return this.transformed(new Transform().translateX(-target.getBounds().getMin().x));
	}

	/**
	 * To x max.
	 *
	 * @param target
	 *            the target
	 * @return the csg
	 */
	public CSG toXMax(CSG target) {
		return this.transformed(new Transform().translateX(-target.getBounds().getMax().x));
	}

	/**
	 * To y min.
	 *
	 * @param target
	 *            the target
	 * @return the csg
	 */
	public CSG toYMin(CSG target) {
		return this.transformed(new Transform().translateY(-target.getBounds().getMin().y));
	}

	/**
	 * To y max.
	 *
	 * @param target
	 *            the target
	 * @return the csg
	 */
	public CSG toYMax(CSG target) {
		return this.transformed(new Transform().translateY(-target.getBounds().getMax().y));
	}

	/**
	 * To z min.
	 *
	 * @return the csg
	 */
	public CSG toZMin() {
		return toZMin(this);
	}

	/**
	 * To z max.
	 *
	 * @return the csg
	 */
	public CSG toZMax() {
		return toZMax(this);
	}

	/**
	 * To x min.
	 *
	 * @return the csg
	 */
	public CSG toXMin() {
		return toXMin(this);
	}

	/**
	 * To x max.
	 *
	 * @return the csg
	 */
	public CSG toXMax() {
		return toXMax(this);
	}

	/**
	 * To y min.
	 *
	 * @return the csg
	 */
	public CSG toYMin() {
		return toYMin(this);
	}

	/**
	 * To y max.
	 *
	 * @return the csg
	 */
	public CSG toYMax() {
		return toYMax(this);
	}

	public CSG move(Number x, Number y, Number z) {
		return transformed(new Transform().translate(x.doubleValue(),y.doubleValue(),z.doubleValue()));
	}
	public CSG move(Vertex v) {
		return transformed(new Transform().translate(v.getX(),v.getY(),v.getZ()));
	}
	public CSG move(Vector3d v) {
		return transformed(new Transform().translate(v.x,v.y,v.z));
	}
	public CSG move(Number[] posVector) {
		return move(posVector[0], posVector[1], posVector[2]);
	}

	/**
	 * Movey.
	 *
	 * @param howFarToMove
	 *            the how far to move
	 * @return the csg
	 */
	// Helper/wrapper functions for movement
	public CSG movey(Number howFarToMove) {
		return this.transformed(Transform.unity().translateY(howFarToMove.doubleValue()));
	}

	/**
	 * Movez.
	 *
	 * @param howFarToMove
	 *            the how far to move
	 * @return the csg
	 */
	public CSG movez(Number howFarToMove) {
		return this.transformed(Transform.unity().translateZ(howFarToMove.doubleValue()));
	}

	/**
	 * Movex.
	 *
	 * @param howFarToMove
	 *            the how far to move
	 * @return the csg
	 */
	public CSG movex(Number howFarToMove) {
		return this.transformed(Transform.unity().translateX(howFarToMove.doubleValue()));
	}

	/**
	 * Helper function moving CSG to center X
	 * moveToCenterX.
	 *
	 * @return the csg
	 */
	public CSG moveToCenterX() {
		return this.movex(-this.getCenterX());
	}

	/**
	 * Helper function moving CSG to center Y
	 * moveToCenterY.
	 *
	 * @return the csg
	 */
	public CSG moveToCenterY() {
		return this.movey(-this.getCenterY());
	}

	/**
	 * Helper function moving CSG to center Z
	 * moveToCenterZ.
	 *
	 * @return the csg
	 */
	public CSG moveToCenterZ() {
		return this.movez(-this.getCenterZ());
	}

	/**
	 * Helper function moving CSG to center X, Y, Z
	 * moveToCenter.
	 * Moves in x, y, z
	 *
	 * @return the csg
	 */
	public CSG moveToCenter() {
		return this.movex(-this.getCenterX()).movey(-this.getCenterY()).movez(-this.getCenterZ());
	}
	
	public  ArrayList<CSG> move( ArrayList<Transform> p) {
		ArrayList<CSG> bits = new ArrayList<CSG>();
		for (Transform t : p) {
			bits.add(this.clone());
		}
		return move(bits, p);
	}
	public static ArrayList<CSG> move(ArrayList<CSG> slice, ArrayList<Transform> p) {
		ArrayList<CSG> s = new ArrayList<CSG>();
		// s.add(slice.get(0));
		for (int i = 0; i < slice.size() && i < p.size(); i++) {
			s.add(slice.get(i).transformed(p.get(i)));
		}
		return s;
	}
	/**
	 * mirror about y axis.
	 *

	 * @return the csg
	 */
	// Helper/wrapper functions for movement
	public CSG mirrory() {
		return this.scaley(-1);
	}

	/**
	 * mirror about z axis.
	 *
	 * @return the csg
	 */
	public CSG mirrorz() {
		return this.scalez(-1);
	}

	/**
	 * mirror about  x axis.
	 *
	 * @return the csg
	 */
	public CSG mirrorx() {
		return this.scalex(-1);
	}


	public CSG rot(Number x, Number y, Number z) {
		return rotx(x.doubleValue()).roty(y.doubleValue()).rotz(z.doubleValue());
	}

	public CSG rot(Number[] posVector) {
		return rot(posVector[0], posVector[1], posVector[2]);
	}

	/**
	 * Rotz.
	 *
	 * @param degreesToRotate
	 *            the degrees to rotate
	 * @return the csg
	 */
	// Rotation function, rotates the object
	public CSG rotz(Number degreesToRotate) {
		return this.transformed(new Transform().rotZ(degreesToRotate.doubleValue()));
	}

	/**
	 * Roty.
	 *
	 * @param degreesToRotate
	 *            the degrees to rotate
	 * @return the csg
	 */
	public CSG roty(Number degreesToRotate) {
		return this.transformed(new Transform().rotY(degreesToRotate.doubleValue()));
	}

	/**
	 * Rotx.
	 *
	 * @param degreesToRotate
	 *            the degrees to rotate
	 * @return the csg
	 */
	public CSG rotx(Number degreesToRotate) {
		return this.transformed(new Transform().rotX(degreesToRotate.doubleValue()));
	}

	/**
	 * Scalez.
	 *
	 * @param scaleValue
	 *            the scale value
	 * @return the csg
	 */
	// Scale function, scales the object
	public CSG scalez(Number scaleValue) {
		return this.transformed(new Transform().scaleZ(scaleValue.doubleValue()));
	}

	/**
	 * Scaley.
	 *
	 * @param scaleValue
	 *            the scale value
	 * @return the csg
	 */
	public CSG scaley(Number scaleValue) {
		return this.transformed(new Transform().scaleY(scaleValue.doubleValue()));
	}

	/**
	 * Scalex.
	 *
	 * @param scaleValue
	 *            the scale value
	 * @return the csg
	 */
	public CSG scalex(Number scaleValue) {
		return this.transformed(new Transform().scaleX(scaleValue.doubleValue()));
	}

	/**
	 * Scale.
	 *
	 * @param scaleValue
	 *            the scale value
	 * @return the csg
	 */
	public CSG scale(Number scaleValue) {
		return this.transformed(new Transform().scale(scaleValue.doubleValue()));
	}

	/**
	 * Constructs a CSG from a list of {@link Polygon} instances.
	 *
	 * @param polygons
	 *            polygons
	 * @return a CSG instance
	 */
	public static CSG fromPolygons(List<Polygon> polygons) {

		CSG csg = new CSG();
		csg.setPolygons(polygons);

		return csg;
	}

	/**
	 * Constructs a CSG from the specified {@link Polygon} instances.
	 *
	 * @param polygons
	 *            polygons
	 * @return a CSG instance
	 */
	public static CSG fromPolygons(Polygon... polygons) {
		return fromPolygons(Arrays.asList(polygons));
	}

	/**
	 * Constructs a CSG from a list of {@link Polygon} instances.
	 *
	 * @param storage
	 *            shared storage
	 * @param polygons
	 *            polygons
	 * @return a CSG instance
	 */
	public static CSG fromPolygons(PropertyStorage storage, List<Polygon> polygons) {

		CSG csg = new CSG();
		csg.setPolygons(polygons);

		csg.setStorage(storage);

		for (Polygon polygon : polygons) {
			polygon.setStorage(storage);
		}

		return csg;
	}

	/**
	 * Constructs a CSG from the specified {@link Polygon} instances.
	 *
	 * @param storage
	 *            shared storage
	 * @param polygons
	 *            polygons
	 * @return a CSG instance
	 */
	public static CSG fromPolygons(PropertyStorage storage, Polygon... polygons) {
		return fromPolygons(storage, Arrays.asList(polygons));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public CSG clone() {
		CSG csg = new CSG();

		csg.setOptType(this.getOptType());

		// sequential code
		// csg.polygons = new ArrayList<>();
		// polygons.forEach((polygon) -> {
		// csg.polygons.add(polygon.clone());
		// });
		Stream<Polygon> polygonStream;

		if (getPolygons().size() > 200) {
			polygonStream = getPolygons().parallelStream();
		} else {
			polygonStream = getPolygons().stream();
		}

		csg.setPolygons(polygonStream.map((Polygon p) -> p.clone()).collect(Collectors.toList()));

		return csg.historySync(this);
	}

	/**
	 * Gets the polygons.
	 *
	 * @return the polygons of this CSG
	 */
	public List<Polygon> getPolygons() {
		return polygons;
	}

	/**
	 * Defines the CSg optimization type.
	 *
	 * @param type
	 *            optimization type
	 * @return this CSG
	 */
	public CSG optimization(OptType type) {
		this.setOptType(type);
		return this;
	}

	/**
	 * Return a new CSG solid representing the union of this csg and the
	 * specified csg.
	 *
	 * Note: Neither this csg nor the specified csg are weighted.
	 *
	 * 
	 * A.union(B)
	 *
	 * +-------+ +-------+ | | | | | A | | | | +--+----+ = | +----+ +----+--+ |
	 * +----+ | | B | | | | | | | +-------+ +-------+
	 * 
	 *
	 *
	 * @param csg
	 *            other csg
	 *
	 * @return union of this csg and the specified csg
	 */
	public CSG union(CSG csg) {

		switch (getOptType()) {
		case CSG_BOUND:
			return _unionCSGBoundsOpt(csg).historySync(this).historySync(csg);
		case POLYGON_BOUND:
			return _unionPolygonBoundsOpt(csg).historySync(this).historySync(csg);
		default:
			// return _unionIntersectOpt(csg);
			return _unionNoOpt(csg).historySync(this).historySync(csg);
		}
	}

	/**
	 * Returns a csg consisting of the polygons of this csg and the specified
	 * csg.
	 * 
	 * The purpose of this method is to allow fast union operations for objects
	 * that do not intersect.
	 * 
	 * WARNING: this method does not apply the csg algorithms. Therefore, please
	 * ensure that this csg and the specified csg do not intersect.
	 * 
	 * @param csg
	 *            csg
	 * 
	 * @return a csg consisting of the polygons of this csg and the specified
	 *         csg
	 */
	public CSG dumbUnion(CSG csg) {

		CSG result = this.clone();
		CSG other = csg.clone();

		result.getPolygons().addAll(other.getPolygons());
		bounds = null;
		return result.historySync(other);
	}

	/**
	 * Return a new CSG solid representing the union of this csg and the
	 * specified csgs.
	 *
	 * Note: Neither this csg nor the specified csg are weighted.
	 *
	 * 
	 * A.union(B)
	 *
	 * +-------+ +-------+ | | | | | A | | | | +--+----+ = | +----+ +----+--+ |
	 * +----+ | | B | | | | | | | +-------+ +-------+
	 * 
	 *
	 *
	 * @param csgs
	 *            other csgs
	 *
	 * @return union of this csg and the specified csgs
	 */
	public CSG union(List<CSG> csgs) {
//		ArrayList<Polygon> incomingPolys = new ArrayList<>();
//		for(int i=0;i<csgs.size();i++) {
//			incomingPolys.addAll(csgs.get(i).getPolygons());
//		}
//		//System.out.println("Node list A");
//		Node a = new Node(this.clone().getPolygons());
//		//System.out.println("Node list B");
//		Node b = new Node(incomingPolys);
//		//System.out.println("a.clipTo(b)");
//		a.clipTo(b);
//		//System.out.println("b.clipTo(a)");
//		b.clipTo(a);
//		//System.out.println("b.invert();");
//		b.invert();
//		//System.out.println("b.clipTo(a);");
//		b.clipTo(a);
//		//System.out.println("b.invert();");
//		b.invert();
//		//System.out.println("a.build(b.allPolygons());");
//		a.build(b.allPolygons());
//		//System.out.println("CSG.fromPolygons(a.allPolygons()).optimization(getOptType())");
//		return CSG.fromPolygons(a.allPolygons()).optimization(getOptType());

		CSG result = this;

		for (int i=0;i<csgs.size();i++) {
			CSG csg = csgs.get(i);
			result = result.union(csg);
			if(Thread.interrupted())
				break;
			progressMoniter.progressUpdate(i, csgs.size(), "Union", result);
		}

		return result;
	}

	/**
	 * Return a new CSG solid representing the union of this csg and the
	 * specified csgs.
	 *
	 * Note: Neither this csg nor the specified csg are weighted.
	 *
	 * 
	 * A.union(B)
	 *
	 * +-------+ +-------+ | | | | | A | | | | +--+----+ = | +----+ +----+--+ |
	 * +----+ | | B | | | | | | | +-------+ +-------+
	 * 
	 *
	 *
	 * @param csgs
	 *            other csgs
	 *
	 * @return union of this csg and the specified csgs
	 */
	public CSG union(CSG... csgs) {
		return union(Arrays.asList(csgs));
	}

	/**
	 * Returns the convex hull of this csg.
	 *
	 * @return the convex hull of this csg
	 */
	public CSG hull() {

		return HullUtil.hull(this, getStorage()).historySync(this);
	}
	
	public static CSG unionAll(CSG... csgs){
		return unionAll(Arrays.asList(csgs));
	}
	public static CSG unionAll(List<CSG> csgs){
		CSG first = csgs.get(0);
		return first.union(csgs.stream().skip(1).collect(Collectors.toList()));
	}
	
	public static CSG hullAll(CSG... csgs){
		return hullAll(Arrays.asList(csgs));
	}
	public static CSG hullAll(List<CSG> csgs){
		//CSG first = csgs.remove(0);
		return HullUtil.hull(csgs);// first.hull(csgs);
	}
	/**
	 * Returns the convex hull of this csg and the union of the specified csgs.
	 *
	 * @param csgs
	 *            csgs
	 * @return the convex hull of this csg and the specified csgs
	 */
	public CSG hull(List<CSG> csgs) {

		CSG csgsUnion = new CSG();
		//csgsUnion.setStorage(storage);
		csgsUnion.optType = optType;
		csgsUnion.setPolygons(this.clone().getPolygons());

		csgs.stream().forEach((csg) -> {
			csgsUnion.getPolygons().addAll(csg.clone().getPolygons());
			csgsUnion.historySync(csg);
		});

		csgsUnion.getPolygons().forEach(p -> p.setStorage(getStorage()));
		bounds = null;
		return csgsUnion.hull();

		// CSG csgsUnion = this;
		//
		// for (CSG csg : csgs) {
		// csgsUnion = csgsUnion.union(csg);
		// }
		//
		// return csgsUnion.hull();
	}

	/**
	 * Returns the convex hull of this csg and the union of the specified csgs.
	 *
	 * @param csgs
	 *            csgs
	 * @return the convex hull of this csg and the specified csgs
	 */
	public CSG hull(CSG... csgs) {

		return hull(Arrays.asList(csgs));
	}

	/**
	 * _union csg bounds opt.
	 *
	 * @param csg
	 *            the csg
	 * @return the csg
	 */
	private CSG _unionCSGBoundsOpt(CSG csg) {
		// System.err.println("WARNING: using " + CSG.OptType.NONE
		// + " since other optimization types missing for union operation.");
		return _unionIntersectOpt(csg);
	}

	/**
	 * _union polygon bounds opt.
	 *
	 * @param csg
	 *            the csg
	 * @return the csg
	 */
	private CSG _unionPolygonBoundsOpt(CSG csg) {
		List<Polygon> inner = new ArrayList<>();
		List<Polygon> outer = new ArrayList<>();

		Bounds b = csg.getBounds();

		this.getPolygons().stream().forEach((p) -> {
			if (b.intersects(p.getBounds())) {
				inner.add(p);
			} else {
				outer.add(p);
			}
		});

		List<Polygon> allPolygons = new ArrayList<>();

		if (!inner.isEmpty()) {
			CSG innerCSG = CSG.fromPolygons(inner);

			allPolygons.addAll(outer);
			allPolygons.addAll(innerCSG._unionNoOpt(csg).getPolygons());
		} else {
			allPolygons.addAll(this.getPolygons());
			allPolygons.addAll(csg.getPolygons());
		}
		bounds = null;
		CSG back =CSG.fromPolygons(allPolygons).optimization(getOptType());
		if(getName().length()!=0 && csg.getName().length()!=0 ) {
			back.setName(name+" unioned to "+csg.getName());
		}
		return back;
	}

	/**
	 * Optimizes for intersection. If csgs do not intersect create a new csg
	 * that consists of the polygon lists of this csg and the specified csg. In
	 * this case no further space partitioning is performed.
	 *
	 * @param csg
	 *            csg
	 * @return the union of this csg and the specified csg
	 */
	private CSG _unionIntersectOpt(CSG csg) {
		boolean intersects = false;

		Bounds bounds = csg.getBounds();

		for (Polygon p : getPolygons()) {
			if (bounds.intersects(p.getBounds())) {
				intersects = true;
				break;
			}
		}

		List<Polygon> allPolygons = new ArrayList<>();

		if (intersects) {
			return _unionNoOpt(csg);
		} else {
			allPolygons.addAll(this.getPolygons());
			allPolygons.addAll(csg.getPolygons());
		}
		CSG back =CSG.fromPolygons(allPolygons).optimization(getOptType());
		if(getName().length()!=0 && csg.getName().length()!=0 ) {
			back.setName(name+" unioned to "+csg.getName());
		}
		return back;
	}

	/**
	 * _union no opt.
	 *
	 * @param csg
	 *            the csg
	 * @return the csg
	 */
	private CSG _unionNoOpt(CSG csg) {
		Node a = new Node(this.clone().getPolygons());
		Node b = new Node(csg.clone().getPolygons());
		a.clipTo(b);
		b.clipTo(a);
		b.invert();
		b.clipTo(a);
		b.invert();
		a.build(b.allPolygons());
		CSG back = CSG.fromPolygons(a.allPolygons()).optimization(getOptType());
		if(getName().length()!=0 && csg.getName().length()!=0 ) {
			back.setName(name+" unioned to "+csg.getName());
		}
		return back;
	}

	/**
	 * Return a new CSG solid representing the difference of this csg and the
	 * specified csgs.
	 *
	 * Note: Neither this csg nor the specified csgs are weighted.
	 *
	 * 
	 * A.difference(B)
	 *
	 * +-------+ +-------+ | | | | | A | | | | +--+----+ = | +--+ +----+--+ |
	 * +----+ | B | | | +-------+
	 * 
	 *
	 * @param csgs
	 *            other csgs
	 * @return difference of this csg and the specified csgs
	 */
	public CSG difference(List<CSG> csgs) {

		if (csgs.isEmpty()) {
			return this.clone();
		}

		CSG csgsUnion = csgs.get(0);

		for (int i = 1; i < csgs.size(); i++) {
			csgsUnion = csgsUnion.union(csgs.get(i));
			progressMoniter.progressUpdate(i, csgs.size(), "Difference", csgsUnion);
			csgsUnion.historySync(csgs.get(i));
			if(Thread.interrupted())
				break;
		}

		return difference(csgsUnion);
	}

	/**
	 * Return a new CSG solid representing the difference of this csg and the
	 * specified csgs.
	 *
	 * Note: Neither this csg nor the specified csgs are weighted.
	 *
	 * 
	 * A.difference(B)
	 *
	 * +-------+ +-------+ | | | | | A | | | | +--+----+ = | +--+ +----+--+ |
	 * +----+ | B | | | +-------+
	 * 
	 *
	 * @param csgs
	 *            other csgs
	 * @return difference of this csg and the specified csgs
	 */
	public CSG difference(CSG... csgs) {

		return difference(Arrays.asList(csgs));
	}

	/**
	 * Return a new CSG solid representing the difference of this csg and the
	 * specified csg.
	 *
	 * Note: Neither this csg nor the specified csg are weighted.
	 *
	 * 
	 * A.difference(B)
	 *
	 * +-------+ +-------+ | | | | | A | | | | +--+----+ = | +--+ +----+--+ |
	 * +----+ | B | | | +-------+
	 * 
	 *
	 * @param csg
	 *            other csg
	 * @return difference of this csg and the specified csg
	 */
	public CSG difference(CSG csg) {
		try {
			// Check to see if a CSG operation is attempting to difference with
			// no
			// polygons
			if (this.getPolygons().size() > 0 && csg.getPolygons().size() > 0) {
				switch (getOptType()) {
				case CSG_BOUND:
					return _differenceCSGBoundsOpt(csg).historySync(this).historySync(csg);
				case POLYGON_BOUND:
					return _differencePolygonBoundsOpt(csg).historySync(this).historySync(csg);
				default:
					return _differenceNoOpt(csg).historySync(this).historySync(csg);
				}
			} else
				return this;
		} catch (Exception ex) {
			//ex.printStackTrace();
			try {
			//System.err.println("CSG difference failed, performing workaround");
			//ex.printStackTrace();
			CSG intersectingParts = csg
					.intersect(this);
			
			if (intersectingParts.getPolygons().size() > 0) {
				switch (getOptType()) {
				case CSG_BOUND:
					return _differenceCSGBoundsOpt(intersectingParts).historySync(this).historySync(intersectingParts);
				case POLYGON_BOUND:
					return _differencePolygonBoundsOpt(intersectingParts).historySync(this).historySync(intersectingParts);
				default:
					return _differenceNoOpt(intersectingParts).historySync(this).historySync(intersectingParts);
				}
			} else
				return this;
			}catch(Exception e) {
				e.printStackTrace();
				return this;
			}
		}

	}

	/**
	 * _difference csg bounds opt.
	 *
	 * @param csg
	 *            the csg
	 * @return the csg
	 */
	private CSG _differenceCSGBoundsOpt(CSG csg) {
		CSG b = csg;

		CSG a1 = this._differenceNoOpt(csg.getBounds().toCSG());
		CSG a2 = this.intersect(csg.getBounds().toCSG());
		CSG BACK =a2._differenceNoOpt(b)._unionIntersectOpt(a1).optimization(getOptType());
		if(getName().length()!=0 && csg.getName().length()!=0 ) {
			BACK.setName(csg.getName()+" differenced from "+name);
		}
		return BACK;
	}

	/**
	 * _difference polygon bounds opt.
	 *
	 * @param csg
	 *            the csg
	 * @return the csg
	 */
	private CSG _differencePolygonBoundsOpt(CSG csg) {
		List<Polygon> inner = new ArrayList<>();
		List<Polygon> outer = new ArrayList<>();

		Bounds bounds = csg.getBounds();

		this.getPolygons().stream().forEach((p) -> {
			if (bounds.intersects(p.getBounds())) {
				inner.add(p);
			} else {
				outer.add(p);
			}
		});

		CSG innerCSG = CSG.fromPolygons(inner);

		List<Polygon> allPolygons = new ArrayList<>();
		allPolygons.addAll(outer);
		allPolygons.addAll(innerCSG._differenceNoOpt(csg).getPolygons());
		CSG BACK =CSG.fromPolygons(allPolygons).optimization(getOptType());
		if(getName().length()!=0 && csg.getName().length()!=0 ) {
			BACK.setName(csg.getName()+" differenced from "+name);
		}
		return BACK;
	}

	/**
	 * _difference no opt.
	 *
	 * @param csg
	 *            the csg
	 * @return the csg
	 */
	private CSG _differenceNoOpt(CSG csg) {

		Node a = new Node(this.clone().getPolygons());
		Node b = new Node(csg.clone().getPolygons());

		a.invert();
		a.clipTo(b);
		b.clipTo(a);
		b.invert();
		b.clipTo(a);
		b.invert();
		a.build(b.allPolygons());
		a.invert();

		CSG csgA = CSG.fromPolygons(a.allPolygons()).optimization(getOptType());
		if(getName().length()!=0 && csg.getName().length()!=0 ) {
			csgA.setName(csg.getName()+" differenced from "+name);
		}
		return csgA;
	}

	/**
	 * Return a new CSG solid representing the intersection of this csg and the
	 * specified csg.
	 *
	 * Note: Neither this csg nor the specified csg are weighted.
	 *
	 * 
	 * A.intersect(B)
	 *
	 * +-------+ | | | A | | +--+----+ = +--+ +----+--+ | +--+ | B | | |
	 * +-------+ }
	 * 
	 *
	 * @param csg
	 *            other csg
	 * @return intersection of this csg and the specified csg
	 */
	public CSG intersect(CSG csg) {

		Node a = new Node(this.clone().getPolygons());
		Node b = new Node(csg.clone().getPolygons());
		a.invert();
		b.clipTo(a);
		b.invert();
		a.clipTo(b);
		b.clipTo(a);
		a.build(b.allPolygons());
		a.invert();
		CSG back = CSG.fromPolygons(a.allPolygons()).optimization(getOptType()).historySync(csg).historySync(this);
		if(getName().length()!=0 && csg.getName().length()!=0 ) {
			back.setName(csg.getName()+" intersection with "+name);
		}
		return back;
	}

	/**
	 * Return a new CSG solid representing the intersection of this csg and the
	 * specified csgs.
	 *
	 * Note: Neither this csg nor the specified csgs are weighted.
	 *
	 * 
	 * A.intersect(B)
	 *
	 * +-------+ | | | A | | +--+----+ = +--+ +----+--+ | +--+ | B | | |
	 * +-------+ }
	 * 
	 *
	 * @param csgs
	 *            other csgs
	 * @return intersection of this csg and the specified csgs
	 */
	public CSG intersect(List<CSG> csgs) {

		if (csgs.isEmpty()) {
			return this.clone();
		}

		CSG csgsUnion = csgs.get(0);

		for (int i = 1; i < csgs.size(); i++) {
			csgsUnion = csgsUnion.union(csgs.get(i));
			progressMoniter.progressUpdate(i, csgs.size(), "Intersect", csgsUnion);
			csgsUnion.historySync(csgs.get(i));
			if(Thread.interrupted())
				break;
		}

		return intersect(csgsUnion);
	}

	/**
	 * Return a new CSG solid representing the intersection of this csg and the
	 * specified csgs.
	 *
	 * Note: Neither this csg nor the specified csgs are weighted.
	 *
	 * 
	 * A.intersect(B)
	 *
	 * +-------+ | | | A | | +--+----+ = +--+ +----+--+ | +--+ | B | | |
	 * +-------+ }
	 * 
	 *
	 * @param csgs
	 *            other csgs
	 * @return intersection of this csg and the specified csgs
	 */
	public CSG intersect(CSG... csgs) {

		return intersect(Arrays.asList(csgs));
	}

	/**
	 * Returns this csg in STL string format.
	 *
	 * @return this csg in STL string format
	 */
	public String toStlString() {
		StringBuilder sb = new StringBuilder();
		toStlString(sb);
		return sb.toString();
	}

	/**
	 * Returns this csg in STL string format.
	 *
	 * @param sb
	 *            string builder
	 *
	 * @return the specified string builder
	 */
	public StringBuilder toStlString(StringBuilder sb) {
		sb.append("solid v3d.csg\n");
		this.getPolygons().stream().forEach((Polygon p) -> {
			p.toStlString(sb);
		});
		sb.append("endsolid v3d.csg\n");
		return sb;
	}

	/**
	 * Color.
	 *
	 * @param c
	 *            the c
	 * @return the csg
	 */
	public CSG color(Color c) {
		getStorage().set("material:color", "" + c.getRed() + " " + c.getGreen() + " " + c.getBlue());

		return this;
	}



	/**
	 * Returns this csg in OBJ string format.
	 *
	 * @param sb
	 *            string builder
	 * @return the specified string builder
	 */
	public StringBuilder toObjString(StringBuilder sb) {
		sb.append("# Group").append("\n");
		sb.append("g v3d.csg\n");
		sb.append("o "+(name==null||name.length()==0?"CSG Export":getName())+"\n");
		class PolygonStruct {

			PropertyStorage storage;
			List<Integer> indices;
			String materialName;

			public PolygonStruct(PropertyStorage storage, List<Integer> indices, String materialName) {
				this.storage = storage;
				this.indices = indices;
				this.materialName = materialName;
			}
		}

		List<Vertex> vertices = new ArrayList<>();
		List<PolygonStruct> indices = new ArrayList<>();

		sb.append("\n# Vertices\n");

		for (Polygon p : getPolygons()) {
			List<Integer> polyIndices = new ArrayList<>();

			p.vertices.stream().forEach((v) -> {
				if (!vertices.contains(v)) {
					vertices.add(v);
					v.toObjString(sb);
					polyIndices.add(vertices.size());
				} else {
					polyIndices.add(vertices.indexOf(v) + 1);
				}
			});
			indices.add(new PolygonStruct(getStorage(), polyIndices, " "));

		}
		HashMap<Vertex,Integer> mapping = new HashMap<Vertex, Integer>();
		HashMap<Transform,Vertex> mappingTF = new HashMap<>();
		if(datumReferences!=null) {
			int startingIndex = vertices.size()+1;
			sb.append("\n# Reference Datum").append("\n");
			for(Transform t:datumReferences) {
				Vertex v=new Vertex(new Vector3d(0, 0, 0), new Vector3d(0, 0, 1))
							.transform(t);
				Vertex v1=new Vertex(new Vector3d(0, 0, 1), new Vector3d(0, 0, 1))
						.transform(t);
				mapping.put(v,startingIndex++);
				mapping.put(v1,startingIndex++);
				mappingTF.put(t, v);
				v.toObjString(sb);
				v1.toObjString(sb);
			}
			sb.append("\n# Datum Lines").append("\n");
			for(Transform t:mappingTF.keySet()) {
				Vertex key = mappingTF.get(t);
				Integer obj = mapping.get(key);
				sb.append("\nl ").append(obj+" ").append(obj+1).append("\n");
			}
		}
		
		sb.append("\n# Faces").append("\n");

		for (PolygonStruct ps : indices) {
			// we triangulate the polygon to ensure
			// compatibility with 3d printer software
			List<Integer> pVerts = ps.indices;
			int index1 = pVerts.get(0);
			for (int i = 0; i < pVerts.size() - 2; i++) {
				int index2 = pVerts.get(i + 1);
				int index3 = pVerts.get(i + 2);

				sb.append("f ").append(index1).append(" ").append(index2).append(" ").append(index3).append("\n");
			}
		}

		sb.append("\n# End Group v3d.csg").append("\n");

		return sb;
	}

	/**
	 * Returns this csg in OBJ string format.
	 *
	 * @return this csg in OBJ string format
	 */
	public String toObjString() {
		StringBuilder sb = new StringBuilder();
		return toObjString(sb).toString();
	}

	/**
	 * Weighted.
	 *
	 * @param f
	 *            the f
	 * @return the csg
	 */
	public CSG weighted(WeightFunction f) {
		return new Modifier(f).modified(this);
	}

	/**
	 * Returns a transformed copy of this CSG.
	 *
	 * @param transform
	 *            the transform to apply
	 *
	 * @return a transformed copy of this CSG
	 */
	public CSG transformed(Transform transform) {

		if (getPolygons().isEmpty()) {
			return clone();
		}

		List<Polygon> newpolygons = this.getPolygons().stream().map(p -> p.transformed(transform))
				.collect(Collectors.toList());

		CSG csg = CSG.fromPolygons(newpolygons).optimization(getOptType());

		//csg.setStorage(storage);
		
		if(getName().length()!=0 ) {
			csg.setName(name+" transformed by["+transform+"]");
		}

		return csg.historySync(this);
	}

	/**
	 * To java fx mesh.
	 *
	 * @param interact
	 *            the interact
	 * @return the mesh container
	 */
	// TODO finish experiment (20.7.2014)
	public MeshContainer toJavaFXMesh(CadInteractionEvent interact) {

		return toJavaFXMeshSimple(interact);

		// TODO test obj approach with multiple materials
		// try {
		// ObjImporter importer = new ObjImporter(toObj());
		//
		// List<Mesh> meshes = new ArrayList<>(importer.getMeshCollection());
		// return new MeshContainer(getBounds().getMin(), getBounds().getMax(),
		// meshes, new ArrayList<>(importer.getMaterialCollection()));
		// } catch (IOException ex) {
		// Logger.getLogger(CSG.class.getName()).log(Level.SEVERE, null, ex);
		// }
		// // we have no backup strategy for broken streams :(
		// return null;
	}

	/**
	 * Returns the CSG as JavaFX triangle mesh.
	 *
	 * @param interact
	 *            the interact
	 * @return the CSG as JavaFX triangle mesh
	 */
	public MeshContainer toJavaFXMeshSimple(CadInteractionEvent interact) {

		return CSGtoJavafx.meshFromPolygon(getPolygons());
	}


	/**
	 * Returns the bounds of this csg. SIDE EFFECT bounds is created and simply
	 * returned if existing
	 *
	 * @return bouds of this csg
	 */
	public Bounds getBounds() {
		if (bounds != null)
			return bounds;
		if (getPolygons().isEmpty()) {
			bounds = new Bounds(Vector3d.ZERO, Vector3d.ZERO);
			return bounds;
		}

		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double minZ = Double.POSITIVE_INFINITY;

		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		double maxZ = Double.NEGATIVE_INFINITY;

		for (Polygon p : getPolygons()) {

			for (int i = 0; i < p.vertices.size(); i++) {

				Vertex vert = p.vertices.get(i);

				if (vert.pos.x < minX) {
					minX = vert.pos.x;
				}
				if (vert.pos.y < minY) {
					minY = vert.pos.y;
				}
				if (vert.pos.z < minZ) {
					minZ = vert.pos.z;
				}

				if (vert.pos.x > maxX) {
					maxX = vert.pos.x;
				}
				if (vert.pos.y > maxY) {
					maxY = vert.pos.y;
				}
				if (vert.pos.z > maxZ) {
					maxZ = vert.pos.z;
				}

			} // end for vertices

		} // end for polygon

		bounds = new Bounds(new Vector3d(minX, minY, minZ), new Vector3d(maxX, maxY, maxZ));
		return bounds;
	}
	
	public Vector3d getCenter(){
		return new Vector3d(
				getCenterX(),
				getCenterY(),
				getCenterZ());
	}
	
	/**
	 * Helper function wrapping bounding box values
	 * 
	 * @return CenterX
	 */
	public double getCenterX() {
		return ((getMinX()/2)+(getMaxX()/2));
	}

	/**
	 * Helper function wrapping bounding box values
	 * 
	 * @return CenterY
	 */
	public double getCenterY() {
		return  ((getMinY()/2)+(getMaxY()/2));
	}

	/**
	 * Helper function wrapping bounding box values
	 * 
	 * @return CenterZ
	 */
	public double getCenterZ() {
		return  ((getMinZ()/2)+(getMaxZ()/2));
	}

	/**
	 * Helper function wrapping bounding box values
	 * 
	 * @return MaxX
	 */
	public double getMaxX() {
		return getBounds().getMax().x;
	}

	/**
	 * Helper function wrapping bounding box values
	 * 
	 * @return MaxY
	 */
	public double getMaxY() {
		return getBounds().getMax().y;
	}

	/**
	 * Helper function wrapping bounding box values
	 * 
	 * @return MaxZ
	 */
	public double getMaxZ() {
		return getBounds().getMax().z;
	}

	/**
	 * Helper function wrapping bounding box values
	 * 
	 * @return MinX
	 */
	public double getMinX() {
		return getBounds().getMin().x;
	}

	/**
	 * Helper function wrapping bounding box values
	 * 
	 * @return MinY
	 */
	public double getMinY() {
		return getBounds().getMin().y;
	}

	/**
	 * Helper function wrapping bounding box values
	 * 
	 * @return tMinZ
	 */
	public double getMinZ() {
		return getBounds().getMin().z;
	}
	/**
	 * Helper function wrapping bounding box values
	 * 
	 * @return MinX
	 */
	public double getTotalX() {
		return (-this.getMinX()+this.getMaxX());
	}

	/**
	 * Helper function wrapping bounding box values
	 * 
	 * @return MinY
	 */
	public double getTotalY() {
		return (-this.getMinY()+this.getMaxY());
	}

	/**
	 * Helper function wrapping bounding box values
	 * 
	 * @return tMinZ
	 */
	public double getTotalZ() {
		return (-this.getMinZ()+this.getMaxZ());
	}
	
	/**
	 * Gets the opt type.
	 *
	 * @return the optType
	 */
	private OptType getOptType() {
		return optType != null ? optType : defaultOptType;
	}

	/**
	 * Sets the default opt type.
	 *
	 * @param optType
	 *            the optType to set
	 */
	public static void setDefaultOptType(OptType optType) {
		defaultOptType = optType;
	}

	/**
	 * Sets the opt type.
	 *
	 * @param optType
	 *            the optType to set
	 */
	public void setOptType(OptType optType) {
		this.optType = optType;
	}

	/**
	 * Sets the polygons.
	 *
	 * @param polygons
	 *            the new polygons
	 */
	public void setPolygons(List<Polygon> polygons) {
		bounds = null;
		this.polygons = polygons;
	}

	/**
	 * The Enum OptType.
	 */
	public static enum OptType {

		/** The csg bound. */
		CSG_BOUND,

		/** The polygon bound. */
		POLYGON_BOUND,

		/** The none. */
		NONE
	}
	/**
	 * Hail Zeon! In case you forget the name of minkowski and are a Gundam fan
	 * @param travelingShape
	 * @return
	 */
	@Deprecated
	public ArrayList<CSG> minovsky( CSG travelingShape){
		System.out.println("Hail Zeon!");
		return minkowski(travelingShape);
	}
	/**
	 * Shortened name In case you forget the name of minkowski 
	 * @param travelingShape
	 * @return
	 */
	public ArrayList<CSG> mink( CSG travelingShape){
		return minkowski(travelingShape);
	}
	/**
	 * This is a simplified version of a minkowski transform using convex hull and the internal list of convex polygons
	 * The shape is placed at the vertex of each point on a polygon, and the result is convex hulled together. 
	 * This collection is returned.
	 *  To make a normal insets, difference this collection
	 *  To make an outset by the normals, union this collection with this object. 
	 * 
	 * @param travelingShape a shape to sweep around
	 * @return
	 */
	public ArrayList<CSG> minkowskiHullShape( CSG travelingShape){
		ArrayList<CSG> bits = new ArrayList<>();
		for(Polygon p: this.getPolygons()){
			List<Vector3d> plist = new ArrayList<>();
			for(Vertex v:p.vertices){
				CSG newSHape = travelingShape.move(v);
				for(Polygon np: newSHape.getPolygons()) {
					for(Vertex nv:np.vertices) {
						plist.add(nv.pos);
					}
				}
			}
			bits.add(HullUtil.hull(plist));
		}
		return  bits;
	}
	/**
	 * This is a simplified version of a minkowski transform using convex hull and the internal list of convex polygons
	 * The shape is placed at the vertex of each point on a polygon, and the result is convex hulled together. 
	 * This collection is returned.
	 *  To make a normal insets, difference this collection
	 *  To make an outset by the normals, union this collection with this object. 
	 * 
	 * @param travelingShape a shape to sweep around
	 * @return
	 */
	public ArrayList<CSG> minkowski( CSG travelingShape){
		HashMap<Vertex,CSG> map= new HashMap<>();
		for(Polygon p: travelingShape.getPolygons()){
			for(Vertex v:p.vertices){
				if(map.get(v)==null)// use hashmap to avoid duplicate locations
					map.put(v,this.move(v));
			}
		}
		return  new ArrayList<CSG>(map.values());
	}
	/**
	 * minkowskiDifference performs an efficient difference of the minkowski transform 
	 * of the intersection of an object. if you have 2 objects and need them to fit with a 
	 * specific tolerance as described as the distance from he normal of the surface, then 
	 * this function will effectinatly compute that value. 
	 * @param itemToDifference the object that needs to fit
	 * @param minkowskiObject the object to represent the offset
	 * @return
	 */
	public CSG minkowskiDifference(CSG itemToDifference, CSG minkowskiObject) {
		CSG intersection = this.intersect(itemToDifference);
		
		ArrayList<CSG> csgDiff = intersection.minkowskiHullShape(minkowskiObject);
		CSG result = this;
		for (int i=0;i<csgDiff.size();i++){
			result= result.difference(csgDiff.get(i));
			progressMoniter.progressUpdate(i, csgDiff.size(), "Minkowski difference", result);
		}
		return result;
	}
	/**
	 * minkowskiDifference performs an efficient difference of the minkowski transform 
	 * of the intersection of an object. if you have 2 objects and need them to fit with a 
	 * specific tolerance as described as the distance from the normal of the surface, then 
	 * this function will effectinatly compute that value. 
	 * @param itemToDifference the object that needs to fit
	 * @param tolerance the tolerance distance
	 * @return
	 */
	public CSG minkowskiDifference(CSG itemToDifference, double tolerance) {
		double shellThickness = Math.abs(tolerance);
		if(shellThickness<0.001)
			return this;
		return minkowskiDifference(itemToDifference,new Sphere(shellThickness/2.0,8,4).toCSG());
	}
	public CSG toolOffset(Number sn) {
		double shellThickness =sn.doubleValue();
		boolean cut =shellThickness<0;
		shellThickness=Math.abs(shellThickness);
		if(shellThickness<0.001)
			return this;
		double z = shellThickness;
		if(z>this.getTotalZ()/2)
			z=this.getTotalZ()/2;
		CSG printNozzel = new Sphere(z/2.0,8,4).toCSG();
		
		if(cut){
			ArrayList<CSG> mikObjs = minkowski(printNozzel);
			CSG remaining = this;
			for(CSG bit: mikObjs){
				remaining=remaining.intersect(bit);
			}
			return remaining;
		}
		return union(minkowskiHullShape(printNozzel));
	}
	private int getNumFacesForOffsets() {
		return getNumfacesinoffset();
	}

	public CSG makeKeepaway(Number sn) {
		double shellThickness =sn.doubleValue();

		double x = Math.abs(this.getBounds().getMax().x) + Math.abs(this.getBounds().getMin().x);
		double y = Math.abs(this.getBounds().getMax().y) + Math.abs(this.getBounds().getMin().y);

		double z = Math.abs(this.getBounds().getMax().z) + Math.abs(this.getBounds().getMin().z);

		double xtol = (x + shellThickness) / x;
		double ytol = (y + shellThickness) / y;
		double ztol = (z + shellThickness) / z;

		double xPer = -(Math.abs(this.getBounds().getMax().x) - Math.abs(this.getBounds().getMin().x)) / x;
		double yPer = -(Math.abs(this.getBounds().getMax().y) - Math.abs(this.getBounds().getMin().y)) / y;
		double zPer = -(Math.abs(this.getBounds().getMax().z) - Math.abs(this.getBounds().getMin().z)) / z;

		// println " Keep away x = "+y+" new = "+ytol
		return this.transformed(new Transform().scale(xtol, ytol, ztol))
				.transformed(new Transform().translateX(shellThickness * xPer))
				.transformed(new Transform().translateY(shellThickness * yPer))
				.transformed(new Transform().translateZ(shellThickness * zPer)).historySync(this);

	}

	public Affine getManipulator() {
		if (manipulator == null)
			manipulator = new Affine();
		return manipulator;
	}

	public CSG addCreationEventStackTraceList(ArrayList<Exception> incoming) {
		for (Exception ex : incoming) {
			addStackTrace(ex);

		}
		return this;
	}

	private void addStackTrace(Exception creationEventStackTrace2) {
		for (StackTraceElement el : creationEventStackTrace2.getStackTrace()) {
			try {
				if (!el.getFileName().endsWith(".java") && el.getLineNumber() > 0) {
					boolean dupLine = false;
					String thisline = el.getFileName() + ":" + el.getLineNumber();
					for (String s : groovyFileLines) {
						if (s.contentEquals(thisline)) {
							dupLine = true;
							// System.err.println("Dupe: "+thisline);
							break;
						}
					}
					if (dupLine == false) {
						groovyFileLines.add(thisline);
						// System.err.println("Line: "+thisline);
						// for(String s:groovyFileLines){
						// //System.err.println("\t\t "+s);
						// creationEventStackTrace2.printStackTrace();
						// }
					}
				}
			} catch (NullPointerException ex) {

			}
		}
	}

	public CSG historySync(CSG dyingCSG) {
		if(useStackTraces) {
			this.addCreationEventStringList(dyingCSG.getCreationEventStackTraceList());
		}
		Set<String> params = dyingCSG.getParameters();
		for (String param : params) {
			boolean existing = false;
			for (String s : this.getParameters()) {
				if (s.contentEquals(param))
					existing = true;
			}
			if (!existing) {
				Parameter vals = CSGDatabase.get(param);
				if (vals != null)
					this.setParameter(vals, dyingCSG.getMapOfparametrics().get(param));
			}
		}
		this.setColor(dyingCSG.getColor());
		if(getName().length()==0)
			setName(dyingCSG.getName());
		return this;
	}

	public CSG addCreationEventStringList(ArrayList<String> incoming) {
		if(useStackTraces) 
		for (String s : incoming) {
			addCreationEventString(s);
		}

		return this;
	}

	public CSG addCreationEventString(String thisline) {
		if(useStackTraces) {
		boolean dupLine = false;
		for (String s : groovyFileLines) {
			if (s.contentEquals(thisline)) {
				dupLine = true;
				break;
			}
		}
		if (!dupLine) {
			groovyFileLines.add(thisline);
		}
		}

		return this;
	}

	public ArrayList<String> getCreationEventStackTraceList() {
		return groovyFileLines;
	}



	public CSG prepMfg() {
		return prepForManufacturing();
	}

	public PrepForManufacturing getManufacturing() {
		return manufactuing;
	}

	public PrepForManufacturing getMfg() {
		return getManufacturing();
	}

	public CSG setMfg(PrepForManufacturing manufactuing) {
		return setManufacturing(manufactuing);
	}

	public CSG setManufacturing(PrepForManufacturing manufactuing) {
		this.manufactuing = manufactuing;
		return this;
	}

	@Deprecated
	public PrepForManufacturing getManufactuing() {
		return getManufacturing();
	}

	@Deprecated
	public CSG setManufactuing(PrepForManufacturing manufactuing) {
		return setManufacturing(manufactuing);
	}

	public CSG setParameter(Parameter w, IParametric function) {
		if (w == null)
			return this;
		if (CSGDatabase.get(w.getName()) == null)
			CSGDatabase.set(w.getName(), w);
		if (getMapOfparametrics().get(w.getName()) == null)
			getMapOfparametrics().put(w.getName(), function);
		return this;
	}

	public CSG setParameter(Parameter w) {
		setParameter(w, new IParametric() {
			@Override
			public CSG change(CSG oldCSG, String parameterKey, Long newValue) {
				if (parameterKey.contentEquals(w.getName()))
					CSGDatabase.get(w.getName()).setValue(newValue);
				return oldCSG;
			}
		});
		return this;
	}

	public CSG setParameter(String key, double defaultValue, double upperBound, double lowerBound,
			IParametric function) {
		ArrayList<Double> vals = new ArrayList<Double>();
		vals.add(upperBound);
		vals.add(lowerBound);
		setParameter(new LengthParameter(key, defaultValue, vals), function);
		return this;
	}

	public CSG setParameterIfNull(String key) {
		if (getMapOfparametrics().get(key) == null)
			getMapOfparametrics().put(key, new IParametric() {

				@Override
				public CSG change(CSG oldCSG, String parameterKey, Long newValue) {
					CSGDatabase.get(key).setValue(newValue);
					return oldCSG;
				}
			});
		return this;
	}

	public Set<String> getParameters() {

		return getMapOfparametrics().keySet();
	}

	public CSG setParameterNewValue(String key, double newValue) {
		IParametric function = getMapOfparametrics().get(key);
		if (function != null)
			return function.change(this, key, new Long((long) (newValue * 1000))).setManipulator(this.getManipulator())
					.setColor(this.getColor());
		return this;
	}

	public CSG setRegenerate(IRegenerate function) {
		regenerate = function;
		return this;
	}

	public CSG regenerate() {
		this.markForRegeneration = false;
		if (regenerate == null)
			return this;
		CSG regenerate2 = regenerate.regenerate(this);
		if(regenerate2!=null)
			return regenerate2.setManipulator(this.getManipulator()).setColor(this.getColor());
		return this;
	}

	public HashMap<String, IParametric> getMapOfparametrics() {
		if (mapOfparametrics == null) {
			mapOfparametrics = new HashMap<>();
		}
		return mapOfparametrics;
	}

	public boolean isMarkedForRegeneration() {
		return markForRegeneration;
	}

	public void markForRegeneration() {
		this.markForRegeneration = true;
	}

	/**
	 * A test to see if 2 CSG's are touching. The fast-return is a bounding box
	 * check If bounding boxes overlap, then an intersection is performed and
	 * the existance of an interscting object is returned
	 * 
	 * @param incoming
	 * @return
	 */
	public boolean touching(CSG incoming) {
		// Fast bounding box overlap check, quick fail if not intersecting
		// bounding boxes
		if (this.getMaxX() > incoming.getMinX() && this.getMinX() < incoming.getMaxX()
				&& this.getMaxY() > incoming.getMinY() && this.getMinY() < incoming.getMaxY()
				&& this.getMaxZ() > incoming.getMinZ() && this.getMinZ() < incoming.getMaxZ()) {
			// Run a full intersection
			CSG inter = this.intersect(incoming);
			if (inter.getPolygons().size() > 0) {
				// intersection success
				return true;
			}
		}
		return false;
	}

	public static ICSGProgress getProgressMoniter() {
		return progressMoniter;
	}

	public static void setProgressMoniter(ICSGProgress progressMoniter) {
		CSG.progressMoniter = progressMoniter;
	}

	public static Color getDefaultColor() {
		return defaultcolor;
	}

	public static void setDefaultColor(Color defaultcolor) {
		CSG.defaultcolor = defaultcolor;
	}
	/**
	 * Get Bounding box
	 * @return A CSG that completely encapsulates the base CSG, centered around it
	 */
	public CSG getBoundingBox(){
		return new Cube(   (-this.getMinX()+this.getMaxX()),
				(-this.getMinY()+this.getMaxY()),
				(-this.getMinZ()+this.getMaxZ()))
				.toCSG()
				.toXMax()
				.movex(this.getMaxX())
				.toYMax()
				.movey(this.getMaxY())
				.toZMax()
				.movez(this.getMaxZ());
	}
	public String getName() {
		return name;
	}

	public CSG setName(String name) {
		this.name = name;
		return this;
	}
	@Override
	public String toString(){
		if(name==null)
			return getColor().toString();
		return getName()+" "+getColor().toString();
	}

	public ArrayList<Transform> getSlicePlanes() {
		return slicePlanes;
	}

	public void addSlicePlane(Transform slicePlane) {
		if(slicePlanes==null)
			slicePlanes=new ArrayList<>();
		this.slicePlanes.add( slicePlane);
	
	}

	/**
	 * @return the exportFormats
	 */
	public ArrayList<String> getExportFormats() {
		return exportFormats;
	}
	/**
	 * @return the exportFormats
	 */
	public void clearExportFormats() {
		if(exportFormats!=null)
			exportFormats.clear();
	}

	/**
	 * @param exportFormat the exportFormat to add
	 */
	public void addExportFormat(String exportFormat) {
		if(this.exportFormats==null)
			this.exportFormats= new ArrayList<>();
		for(String f:exportFormats){
			if(f.toLowerCase().contains(exportFormat.toLowerCase())){
				return;
			}
		}
		this.exportFormats.add(exportFormat.toLowerCase());
	}
	public static int getNumfacesinoffset() {
		return getNumFacesInOffset();
	}
	public static int getNumFacesInOffset() {
		return numFacesInOffset;
	}
	public static void setNumFacesInOffset(int numFacesInOffset) {
		CSG.numFacesInOffset = numFacesInOffset;
	}

	public static boolean isUseStackTraces() {
		return useStackTraces;
	}

	public static void setUseStackTraces(boolean useStackTraces) {
		CSG.useStackTraces = useStackTraces;
	}

	public ArrayList<Transform> getDatumReferences() {
		return datumReferences;
	}

	private void setDatumReferences(ArrayList<Transform> datumReferences) {
		this.datumReferences = datumReferences;
	}

	public PropertyStorage getStorage() {
		return str;
	}

	public void setStorage(PropertyStorage storage) {
		this.str = storage;
	}
	
	/**
	 * Adds construction tabs to a given CSG object in order to facilitate connection with other boards and returns the CSG with tabs added plus separate fastener objects interspersed between tabs.
	 * Assumes board thickness is the thinnest dimension.
	 * Assumes board thickness can be arbitrary but uniform height.
	 * Assumes the edge having tabs added extends fully between Min and Max in that dimension.
	 * 
	 * TODO: Find the polygon defined by the XY plane slice that is perhaps 0.5mm into the normalized +Y. Add tabs to THAT polygon's minX/maxX instead of part's global minX/maxX.
	 * 
	 * Example usage:
	 * 	// Create a temporary copy of the target object, without any tabs
	 *	CSG boardTemp = board
	 *	
	 *	// Instantiate a bucket to hold fastener CSG objects in
	 *	ArrayList<CSG> fasteners = []
	 * 	
	 * 	// Define the direction of the edge to be tabbed using a Vector3d object, in this case the edge facing in the negative Y direction
	 * 	Vector3d edgeDirection = new Vector3d(0, -1, 0);
	 * 
	 * 	// Define the diameter of the fastener holes to be added using a LengthParameter object
	 * 	LengthParameter screwDiameter = new LengthParameter("Screw Hole Diameter (mm)", 3, [0, 20])
	 * 
	 * 	// Add tabs to the temporary object using the edgeDirection and screwDiameter parameters
	 * 	ArrayList<CSG> returned = boardTemp.addTabs(edgeDirection, screwDiameter);
	 * 
	 * 	// Combine the modified temporary object with the original object, to add the new tabs
	 * 	board = boardTemp.union(returned.get(0));
	 * 
	 * 	// Add the separate fastener hole objects to the list
	 * 	fasteners = returned.subList(1, returned.size());
	 *
	 * @param boardInput the original CSG object to add tabs to
	 * @param edgeDirection a Vector3d object representing the direction of the edge of the board to which tabs and fastener holes will be added
	 * @param fastenerHoleDiameter a LengthParameter object representing the diameter of the fastener holes to be added
	 * @return an ArrayList of CSG objects representing the original board with added tabs and separate fastener hole objects
	 * @throws Exception if the edgeDirection parameter is not a cartesian unit Vector3d object or uses an unimplemented orientation
	 */
	public ArrayList<CSG> addTabs(Vector3d edgeDirection, LengthParameter fastenerHoleDiameter) throws Exception {
		
		ArrayList<CSG> result = new ArrayList<CSG>();
		ArrayList<CSG> fasteners = new ArrayList<CSG>();
		
		// Instantiate a new transformation which will capture cumulative transformations being operated on the input board, to be reversed later
		Transform boardTrans = new Transform();
		
		// Determine orientation transformation, based on edgeDirection vector
		// TODO: instead of JUST edgeDirection, use max values to also try to determine which is the cutting direction
		if (edgeDirection.equals(Vector3d.X_ONE)) {
			boardTrans = boardTrans.rotz(90);
		} else if (edgeDirection.equals(Vector3d.X_ONE.negated())) {
			boardTrans = boardTrans.rotz(-90);
		} else if (edgeDirection.equals(Vector3d.Y_ONE)) {
			boardTrans = boardTrans.rotz(180);
		} else if (edgeDirection.equals(Vector3d.Y_ONE.negated())) {
			//boardTrans = boardTrans;											// original addTabs orientation, so no transformation needed
		} else if (edgeDirection.equals(Vector3d.Z_ONE)) {
			boardTrans = boardTrans.rotx(-90);
		} else if (edgeDirection.equals(Vector3d.Z_ONE.negated())) {
			boardTrans = boardTrans.rotx(90);
		} else {
			throw new Exception("Invalid edge direction: edgeDirection must be a cartesian unit Vector3d object. Try Vector3d.Y_ONE.negated() - Current value: " + edgeDirection.toString());
		}
		
		// Apply orientation transformation
	    CSG boardTemp = this.transformed(boardTrans);
		
	    // Translate the boardTemp object so that its minimum corner is at the origin, adding to cumulative transformation
	    boardTrans = boardTrans.movex(-boardTemp.getMinX()).movey(-boardTemp.getMinY()).movez(-boardTemp.getMinZ());
		
		// Apply translation transformation
	    boardTemp = this.transformed(boardTrans);
	    
	    // If the board is larger in Z than in X, assume that the board is oriented into the XY plane and rotate to flatten it onto the XY plane
	    if (boardTemp.getTotalZ() > boardTemp.getTotalX()) {
	    	boardTrans = boardTrans.roty(-90).movez(boardTemp.getMaxX());
	    }
		
		// Apply final cumulative transformation to the boardInput
	    boardTemp = this.transformed(boardTrans);
	    
	    // TODO: Here, find the polygon defined by the XY plane slice that is perhaps 0.5mm into the +Y. Add tabs to THAT polygon's minX/maxX instead of part's global minX/maxX.
	    
	    // Define the size of the tabs and the distance between tab cycles
	    double tabSize = boardTemp.getMaxZ() * 2;
	    double cycleSize = tabSize * 3;
	    
	    // Determine the minimum buffer space between the edge of the board and the tabs
	    double minBuffer = boardTemp.getMaxZ();
	    
	    // Create a temporary CSG object for a single tab
	    CSG tabTemp = new Cube(tabSize, boardTemp.getMaxZ(), boardTemp.getMaxZ()).toCSG();
	    
	    // Position the temporary tab object at the first tab location
	    tabTemp = tabTemp.movex(tabTemp.getMaxX())
	                     .movey(-tabTemp.getMaxY() + boardTemp.getMinY())
	                     .movez(tabTemp.getMaxZ());

		// Create a temporary CSG object for a single fastener hole
		double fastenerHoleRadius = fastenerHoleDiameter.getMM() / 2.0;
		double fastenerHoleDepth = boardTemp.getMaxZ();
		CSG fastenerHoleTemp = new Cylinder(fastenerHoleRadius, fastenerHoleDepth).toCSG();
		
	    // Position the temporary fastener hole object at an initial fastener hole location that does not actually render (analogous to the first tab location, but the first tab is not associated with a fastener)
		fastenerHoleTemp = fastenerHoleTemp.rotx(-90)
											.movex(-tabSize)
											.movey(0)
											.movez(boardTemp.getMaxZ()/2);
	    
	    // Calculate the number of full tab-space cycles to add, not including the first tab (this is also the number of fastener objects to return)
	    int iterNum = (int) Math.floor((boardTemp.getMaxX() - tabSize - minBuffer*2) / cycleSize);	// Round down to ensure an integer value
	    
	    // Calculate the clearance beyond the outermost tabs, equal on both sides and never more than minBuffer
	    double bufferVal = (boardTemp.getMaxX() - (tabSize + cycleSize * iterNum)) / 2;
		
		// Add the first tab if there is enough room, which due to not being paired with a fastener is removed from the loop
		if (boardTemp.getTotalX() > tabSize + 2 * bufferVal) {
			boardTemp = boardTemp.union(tabTemp.movex(bufferVal));
		}
	    
	    // Add the desired number of tabs & fasteners at regular intervals
	    for(int i=1; i<=iterNum; i++) {
	        double xVal = bufferVal + i * cycleSize;
	        boardTemp = boardTemp.union(tabTemp.movex(xVal));
			fasteners.add(fastenerHoleTemp.movex(xVal).transformed(boardTrans.inverse()));
	    }
	    
	    // Translate the boardTemp object back to its original position
	    boardTemp = boardTemp.transformed(boardTrans.inverse());
		
		result.add(boardTemp);
		result.addAll(fasteners);
	    
	    return result;
	}
	
	CSG addAssemblyStep(int stepNumber, Transform explodedPose) {
		String key = "AssemblySteps";
		PropertyStorage incomingGetStorage = getAssemblyStorage();
		if(incomingGetStorage.getValue(key)==Optional.empty()) {
			HashMap<Integer,Transform> map= new HashMap<>();
			incomingGetStorage.set(key, map);
		}
		if(incomingGetStorage.getValue("MaxAssemblyStep")==Optional.empty()) {
			incomingGetStorage.set("MaxAssemblyStep", Integer.valueOf(stepNumber));
		}
		Integer max = (Integer) incomingGetStorage.getValue("MaxAssemblyStep").get();
		if(stepNumber>max.intValue()) {
			incomingGetStorage.set("MaxAssemblyStep", Integer.valueOf(stepNumber));
		}
		HashMap<Integer,Transform> map=(HashMap<Integer, Transform>) incomingGetStorage.getValue(key).get();
		map.put(stepNumber, explodedPose);
		if(incomingGetStorage.getValue("AssembleAffine")==Optional.empty())
			incomingGetStorage.set("AssembleAffine", new Affine());
		return this;
	}

	public PropertyStorage getAssemblyStorage() {
		if(assembly==null)
			assembly= new PropertyStorage();
		return assembly;
	}
}
