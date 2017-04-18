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
public class CSG {

	/** The polygons. */
	private List<Polygon> polygons;

	/** The default opt type. */
	private static OptType defaultOptType = OptType.CSG_BOUND;

	/** The opt type. */
	private OptType optType = null;

	/** The storage. */
	private PropertyStorage storage;
	/** The current. */
	private MeshView current;
	
	private static Color defaultcolor=Color.web("#007956");

	/** The color. */
	private Color color=getDefaultColor();

	/** The manipulator. */
	private Affine manipulator;
	private Bounds bounds;
	/**
	 * This is the trace for where this csg was created
	 */
	private final Exception creationEventStackTrace = new Exception();
	public static final int INDEX_OF_PARAMETRIC_DEFAULT = 0;
	public static final int INDEX_OF_PARAMETRIC_LOWER = 1;
	public static final int INDEX_OF_PARAMETRIC_UPPER = 2;
	private ArrayList<String> groovyFileLines = new ArrayList<>();
	private PrepForManufacturing manufactuing = null;
	private HashMap<String, IParametric> mapOfparametrics = null;
	private IRegenerate regenerate = null;
	private boolean markForRegeneration = false;
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
		storage = new PropertyStorage();
		addStackTrace(creationEventStackTrace);
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
		MeshContainer meshContainer = toJavaFXMesh(null);

		current = meshContainer.getAsMeshViews().get(0);

		PhongMaterial m = new PhongMaterial(getColor());
		current.setMaterial(m);
		

		if (getManipulator() != null) {
			current.getTransforms().clear();
			current.getTransforms().add(getManipulator());
		}

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

	public CSG move(double x, double y, double z) {
		return transformed(new Transform().translate(x,y,z));
	}
	public CSG move(Vertex v) {
		return transformed(new Transform().translate(v.getX(),v.getY(),v.getZ()));
	}
	public CSG move(Vector3d v) {
		return transformed(new Transform().translate(v.x,v.y,v.z));
	}
	public CSG move(double[] posVector) {
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
	public CSG movey(double howFarToMove) {
		return this.transformed(Transform.unity().translateY(howFarToMove));
	}

	/**
	 * Movez.
	 *
	 * @param howFarToMove
	 *            the how far to move
	 * @return the csg
	 */
	public CSG movez(double howFarToMove) {
		return this.transformed(Transform.unity().translateZ(howFarToMove));
	}

	/**
	 * Movex.
	 *
	 * @param howFarToMove
	 *            the how far to move
	 * @return the csg
	 */
	public CSG movex(double howFarToMove) {
		return this.transformed(Transform.unity().translateX(howFarToMove));
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


	public CSG rot(double x, double y, double z) {
		return rotx(x).roty(y).rotz(z);
	}

	public CSG rot(double[] posVector) {
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
	public CSG rotz(double degreesToRotate) {
		return this.transformed(new Transform().rotZ(degreesToRotate));
	}

	/**
	 * Roty.
	 *
	 * @param degreesToRotate
	 *            the degrees to rotate
	 * @return the csg
	 */
	public CSG roty(double degreesToRotate) {
		return this.transformed(new Transform().rotY(degreesToRotate));
	}

	/**
	 * Rotx.
	 *
	 * @param degreesToRotate
	 *            the degrees to rotate
	 * @return the csg
	 */
	public CSG rotx(double degreesToRotate) {
		return this.transformed(new Transform().rotX(degreesToRotate));
	}

	/**
	 * Scalez.
	 *
	 * @param scaleValue
	 *            the scale value
	 * @return the csg
	 */
	// Scale function, scales the object
	public CSG scalez(double scaleValue) {
		return this.transformed(new Transform().scaleZ(scaleValue));
	}

	/**
	 * Scaley.
	 *
	 * @param scaleValue
	 *            the scale value
	 * @return the csg
	 */
	public CSG scaley(double scaleValue) {
		return this.transformed(new Transform().scaleY(scaleValue));
	}

	/**
	 * Scalex.
	 *
	 * @param scaleValue
	 *            the scale value
	 * @return the csg
	 */
	public CSG scalex(double scaleValue) {
		return this.transformed(new Transform().scaleX(scaleValue));
	}

	/**
	 * Scale.
	 *
	 * @param scaleValue
	 *            the scale value
	 * @return the csg
	 */
	public CSG scale(double scaleValue) {
		return this.transformed(new Transform().scale(scaleValue));
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

		csg.storage = storage;

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

		CSG result = this;

		for (int i=0;i<csgs.size();i++) {
			CSG csg = csgs.get(i);
			result = result.union(csg);
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

		return HullUtil.hull(this, storage).historySync(this);
	}
	
	public static CSG unionAll(CSG... csgs){
		return unionAll(Arrays.asList(csgs));
	}
	public static CSG unionAll(List<CSG> csgs){
		CSG first = csgs.remove(0);
		return first.union(csgs);
	}
	
	public static CSG hullAll(CSG... csgs){
		return hullAll(Arrays.asList(csgs));
	}
	public static CSG hullAll(List<CSG> csgs){
		CSG first = csgs.remove(0);
		return first.hull(csgs);
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
		csgsUnion.storage = storage;
		csgsUnion.optType = optType;
		csgsUnion.setPolygons(this.clone().getPolygons());

		csgs.stream().forEach((csg) -> {
			csgsUnion.getPolygons().addAll(csg.clone().getPolygons());
			csgsUnion.historySync(csg);
		});

		csgsUnion.getPolygons().forEach(p -> p.setStorage(storage));
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
		return CSG.fromPolygons(allPolygons).optimization(getOptType());
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

		return CSG.fromPolygons(allPolygons).optimization(getOptType());
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
		return CSG.fromPolygons(a.allPolygons()).optimization(getOptType());
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
			System.err.println("CSG difference failed");
			ex.printStackTrace();
			return this;
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

		return a2._differenceNoOpt(b)._unionIntersectOpt(a1).optimization(getOptType());
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

		return CSG.fromPolygons(allPolygons).optimization(getOptType());
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
		return CSG.fromPolygons(a.allPolygons()).optimization(getOptType()).historySync(csg).historySync(this);
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
		storage.set("material:color", "" + c.getRed() + " " + c.getGreen() + " " + c.getBlue());

		return this;
	}

	/**
	 * To obj.
	 *
	 * @return the obj file
	 */
	public ObjFile toObj() {

		StringBuilder objSb = new StringBuilder();

		objSb.append("mtllib " + ObjFile.MTL_NAME);

		objSb.append("# Group").append("\n");
		objSb.append("g v3d.csg\n");

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

		objSb.append("\n# Vertices\n");

		Map<PropertyStorage, Integer> materialNames = new HashMap<>();

		int materialIndex = 0;

		for (Polygon p : getPolygons()) {
			List<Integer> polyIndices = new ArrayList<>();

			p.vertices.stream().forEach((v) -> {
				if (!vertices.contains(v)) {
					vertices.add(v);
					v.toObjString(objSb);
					polyIndices.add(vertices.size());
				} else {
					polyIndices.add(vertices.indexOf(v) + 1);
				}
			});

			if (!materialNames.containsKey(p.getStorage())) {
				materialIndex++;
				materialNames.put(p.getStorage(), materialIndex);
				p.getStorage().set("material:name", materialIndex);
			}

			indices.add(
					new PolygonStruct(p.getStorage(), polyIndices, "material-" + materialNames.get(p.getStorage())));
		}

		objSb.append("\n# Faces").append("\n");

		for (PolygonStruct ps : indices) {

			// add mtl info
			ps.storage.getValue("material:color")
					.ifPresent((v) -> objSb.append("usemtl ").append(ps.materialName).append("\n"));

			// we triangulate the polygon to ensure
			// compatibility with 3d printer software
			List<Integer> pVerts = ps.indices;
			int index1 = pVerts.get(0);
			for (int i = 0; i < pVerts.size() - 2; i++) {
				int index2 = pVerts.get(i + 1);
				int index3 = pVerts.get(i + 2);

				objSb.append("f ").append(index1).append(" ").append(index2).append(" ").append(index3).append("\n");
			}
		}

		objSb.append("\n# End Group v3d.csg").append("\n");

		StringBuilder mtlSb = new StringBuilder();

		materialNames.keySet().forEach(s -> {
			if (s.contains("material:color")) {
				mtlSb.append("newmtl material-").append(s.getValue("material:name").get()).append("\n");
				mtlSb.append("Kd ").append(s.getValue("material:color").get()).append("\n");
			}
		});

		return new ObjFile(objSb.toString(), mtlSb.toString());
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

		CSG result = CSG.fromPolygons(newpolygons).optimization(getOptType());

		result.storage = storage;

		return result.historySync(this);
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

		TriangleMesh mesh = new TriangleMesh();

		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double minZ = Double.POSITIVE_INFINITY;

		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		double maxZ = Double.NEGATIVE_INFINITY;

		int counter = 0;
		for (Polygon p : getPolygons()) {
			if (p.vertices.size() >= 3) {

				// TODO: improve the triangulation?
				//
				// JavaOne requires triangular polygons.
				// If our polygon has more vertices, create
				// multiple triangles:
				Vertex firstVertex = p.vertices.get(0);
				for (int i = 0; i < p.vertices.size() - 2; i++) {

					if (firstVertex.pos.x < minX) {
						minX = firstVertex.pos.x;
					}
					if (firstVertex.pos.y < minY) {
						minY = firstVertex.pos.y;
					}
					if (firstVertex.pos.z < minZ) {
						minZ = firstVertex.pos.z;
					}

					if (firstVertex.pos.x > maxX) {
						maxX = firstVertex.pos.x;
					}
					if (firstVertex.pos.y > maxY) {
						maxY = firstVertex.pos.y;
					}
					if (firstVertex.pos.z > maxZ) {
						maxZ = firstVertex.pos.z;
					}

					mesh.getPoints().addAll((float) firstVertex.pos.x, (float) firstVertex.pos.y,
							(float) firstVertex.pos.z);

					mesh.getTexCoords().addAll(0); // texture (not covered)
					mesh.getTexCoords().addAll(0);

					Vertex secondVertex = p.vertices.get(i + 1);

					if (secondVertex.pos.x < minX) {
						minX = secondVertex.pos.x;
					}
					if (secondVertex.pos.y < minY) {
						minY = secondVertex.pos.y;
					}
					if (secondVertex.pos.z < minZ) {
						minZ = secondVertex.pos.z;
					}

					if (secondVertex.pos.x > maxX) {
						maxX = firstVertex.pos.x;
					}
					if (secondVertex.pos.y > maxY) {
						maxY = firstVertex.pos.y;
					}
					if (secondVertex.pos.z > maxZ) {
						maxZ = firstVertex.pos.z;
					}

					mesh.getPoints().addAll((float) secondVertex.pos.x, (float) secondVertex.pos.y,
							(float) secondVertex.pos.z);

					mesh.getTexCoords().addAll(0); // texture (not covered)
					mesh.getTexCoords().addAll(0);

					Vertex thirdVertex = p.vertices.get(i + 2);

					mesh.getPoints().addAll((float) thirdVertex.pos.x, (float) thirdVertex.pos.y,
							(float) thirdVertex.pos.z);

					if (thirdVertex.pos.x < minX) {
						minX = thirdVertex.pos.x;
					}
					if (thirdVertex.pos.y < minY) {
						minY = thirdVertex.pos.y;
					}
					if (thirdVertex.pos.z < minZ) {
						minZ = thirdVertex.pos.z;
					}

					if (thirdVertex.pos.x > maxX) {
						maxX = firstVertex.pos.x;
					}
					if (thirdVertex.pos.y > maxY) {
						maxY = firstVertex.pos.y;
					}
					if (thirdVertex.pos.z > maxZ) {
						maxZ = firstVertex.pos.z;
					}

					mesh.getTexCoords().addAll(0); // texture (not covered)
					mesh.getTexCoords().addAll(0);

					mesh.getFaces().addAll(counter, // first vertex
							0, // texture (not covered)
							counter + 1, // second vertex
							0, // texture (not covered)
							counter + 2, // third vertex
							0 // texture (not covered)
					);
					counter += 3;
				} // end for
			} // end if #verts >= 3

		} // end for polygon

		return new MeshContainer(new Vector3d(minX, minY, minZ), new Vector3d(maxX, maxY, maxZ), mesh);
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
	public ArrayList<CSG> minkowski( CSG travelingShape){
		ArrayList<CSG> allFaces = new ArrayList<CSG>();
		for(Polygon p: getPolygons()){
			ArrayList<CSG> corners =new ArrayList<CSG>();
			for(Vertex v:p.vertices){
				corners.add(travelingShape.move(v));
			}
			CSG face = corners.remove(0);
			face=face.hull(corners);
			allFaces.add(face);
		}
		return allFaces;
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
		
		ArrayList<CSG> csgDiff = intersection.mink(minkowskiObject);
		CSG result = this.difference(intersection);
		for (int i=0;i<csgDiff.size();i++){
			result= result.difference(csgDiff.get(i));
			progressMoniter.progressUpdate(i, csgDiff.size(), "Minkowski difference", result);
		}
		return result;
	}
	/**
	 * minkowskiDifference performs an efficient difference of the minkowski transform 
	 * of the intersection of an object. if you have 2 objects and need them to fit with a 
	 * specific tolerance as described as the distance from he normal of the surface, then 
	 * this function will effectinatly compute that value. 
	 * @param itemToDifference the object that needs to fit
	 * @param tolerance the tolerance distance
	 * @return
	 */
	public CSG minkowskiDifference(CSG itemToDifference, double tolerance) {
		double shellThickness = Math.abs(tolerance);
		if(shellThickness<0.001)
			return this;
		return minkowskiDifference(itemToDifference,new Icosahedron(shellThickness).toCSG());
	}
	public CSG toolOffset(double shellThickness) {
		
		boolean cut =shellThickness<0;
		shellThickness=Math.abs(shellThickness);
		if(shellThickness<0.001)
			return this;
		CSG printNozzel = new Icosahedron(shellThickness).toCSG();
		
		if(cut)
			return difference(minkowski(printNozzel));
		return union(minkowski(printNozzel));
	}

	public CSG makeKeepaway(double shellThickness) {

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
				if (el.getFileName().contains(".groovy") && el.getLineNumber() > 0) {
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
		this.addCreationEventStringList(dyingCSG.getCreationEventStackTraceList());
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
		return this;
	}

	public CSG addCreationEventStringList(ArrayList<String> incoming) {

		for (String s : incoming) {
			addCreationEventString(s);
		}

		return this;
	}

	public CSG addCreationEventString(String thisline) {

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

		return this;
	}

	public ArrayList<String> getCreationEventStackTraceList() {
		return groovyFileLines;
	}

	public CSG prepForManufacturing() {
		if (getManufacturing() == null)
			return this;
		return getManufacturing().prep(this);
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
		return regenerate.regenerate(this).setManipulator(this.getManipulator()).setColor(this.getColor());
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

}
