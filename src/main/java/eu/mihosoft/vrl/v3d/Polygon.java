/**
 * Polygon.java
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import eu.mihosoft.vrl.v3d.ext.org.poly2tri.PolygonUtil;

// TODO: Auto-generated Javadoc
/**
 * Represents a convex polygon.
 *
 * Each convex polygon has a {@code shared} property, which is shared between
 * all polygons that are clones of each other or where split from the same
 * polygon. This can be used to define per-polygon properties (such as surface
 * color).
 */
public final class Polygon {

    /** Polygon vertices. */
    public final List<Vertex> vertices;
    /**
     * Shared property (can be used for shared color etc.).
     */
    private PropertyStorage shared;
    /**
     * Plane defined by this polygon.
     *
     *  Note:  uses first three vertices to define the plane.
     */
    public final Plane plane;
	//private final Exception creationEventStackTrace = new Exception();
    

    /**
     * Sets the storage.
     *
     * @param storage the new storage
     */
    void setStorage(PropertyStorage storage) {
        this.shared = storage;
    }

    /**
     * Decomposes the specified concave polygon into convex polygons.
     *
     * @param points the points that define the polygon
     * @return the decomposed concave polygon (list of convex polygons)
     */
    public static List<Polygon> fromConcavePoints(Vector3d... points) {
        Polygon p = fromPoints(points);

        return PolygonUtil.concaveToConvex(p);
    }

    /**
     * Decomposes the specified concave polygon into convex polygons.
     *
     * @param points the points that define the polygon
     * @return the decomposed concave polygon (list of convex polygons)
     */
    public static List<Polygon> fromConcavePoints(List<Vector3d> points) {
        Polygon p = fromPoints(points);

        return PolygonUtil.concaveToConvex(p);
    }

    /**
     * Constructor. Creates a new polygon that consists of the specified
     * vertices.
     *
     *  Note:  the vertices used to initialize a polygon must be coplanar
     * and form a convex loop.
     *
     * @param vertices polygon vertices
     * @param shared shared property
     */
    public Polygon(List<Vertex> vertices, PropertyStorage shared) {
        this.vertices = vertices;
        this.shared = shared;
        this.plane = Plane.createFromPoints(
                vertices.get(0).pos,
                vertices.get(1).pos,
                vertices.get(2).pos);
        validateAndInit(vertices);
    }

    /**
     * Constructor. Creates a new polygon that consists of the specified
     * vertices.
     *
     *  Note:  the vertices used to initialize a polygon must be coplanar
     * and form a convex loop.
     *
     * @param vertices polygon vertices
     */
    public Polygon(List<Vertex> vertices) {
        this.vertices = vertices;
        this.plane = Plane.createFromPoints(
                vertices.get(0).pos,
                vertices.get(1).pos,
                vertices.get(2).pos);
        validateAndInit(vertices);
    }
    private void validateAndInit(List<Vertex> vertices1) {
        for (Vertex v : vertices1) {
            v.normal = plane.normal;
        }
        if (Vector3d.ZERO.equals(plane.normal)) {
            valid = false;
            System.err.println(
                    "Normal is zero! Probably, duplicate points have been specified!\n\n" + toStlString());
//            throw new RuntimeException(
//                    "Normal is zero! Probably, duplicate points have been specified!\n\n"+toStlString());
        }

        if (vertices.size() < 3) {
            throw new RuntimeException(
                    "Invalid polygon: at least 3 vertices expected, got: "
                    + vertices.size());
        }
        
    }
    /**
     * Constructor. Creates a new polygon that consists of the specified
     * vertices.
     *
     *  Note:  the vertices used to initialize a polygon must be coplanar
     * and form a convex loop.
     *
     * @param vertices polygon vertices
     *
     */
    public Polygon(Vertex... vertices) {
        this(Arrays.asList(vertices));
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Polygon clone() {
        List<Vertex> newVertices = new ArrayList<>();
        this.vertices.forEach((vertex) -> {
            newVertices.add(vertex.clone());
        });
        return new Polygon(newVertices, getStorage());
    }

    /**
     * Flips this polygon.
     *
     * @return this polygon
     */
    public Polygon flip() {
        vertices.forEach((vertex) -> {
            vertex.flip();
        });
        Collections.reverse(vertices);

        plane.flip();

        return this;
    }

    /**
     * Returns a flipped copy of this polygon.
     *
     *  Note:  this polygon is not modified.
     *
     * @return a flipped copy of this polygon
     */
    public Polygon flipped() {
        return clone().flip();
    }

    /**
     * Returns this polygon in STL string format.
     *
     * @return this polygon in STL string format
     */
    public String toStlString() {
        return toStlString(new StringBuilder()).toString();
    }

    /**
     * Returns this polygon in STL string format.
     *
     * @param sb string builder
     *
     * @return the specified string builder
     */
    public StringBuilder toStlString(StringBuilder sb) {

        if (this.vertices.size() >= 3) {

            // TODO: improve the triangulation?
            //
            // STL requires triangular polygons.
            // If our polygon has more vertices, create
            // multiple triangles:
            String firstVertexStl = this.vertices.get(0).toStlString();
            for (int i = 0; i < this.vertices.size() - 2; i++) {
                sb.
                        append("  facet normal ").append(
                                this.plane.normal.toStlString()).append("\n").
                        append("    outer loop\n").
                        append("      ").append(firstVertexStl).append("\n").
                        append("      ");
                this.vertices.get(i + 1).toStlString(sb).append("\n").
                        append("      ");
                this.vertices.get(i + 2).toStlString(sb).append("\n").
                        append("    endloop\n").
                        append("  endfacet\n");
            }
        }

        return sb;
    }

    /**
     * Translates this polygon.
     *
     * @param v the vector that defines the translation
     * @return this polygon
     */
    public Polygon translate(Vector3d v) {
        vertices.forEach((vertex) -> {
            vertex.pos = vertex.pos.plus(v);
        });

        Vector3d a = this.vertices.get(0).pos;
        Vector3d b = this.vertices.get(1).pos;
        Vector3d c = this.vertices.get(2).pos;

        this.plane.normal = b.minus(a).cross(c.minus(a));

        return this;
    }

    /**
     * Returns a translated copy of this polygon.
     *
     *  Note:  this polygon is not modified
     *
     * @param v the vector that defines the translation
     *
     * @return a translated copy of this polygon
     */
    public Polygon translated(Vector3d v) {
        return clone().translate(v);
    }

    /**
     * Applies the specified transformation to this polygon.
     *
     *  Note:  if the applied transformation performs a mirror operation
     * the vertex order of this polygon is reversed.
     *
     * @param transform the transformation to apply
     *
     * @return this polygon
     */
    public Polygon transform(Transform transform) {

        this.vertices.stream().forEach(
                (v) -> {
                    v.transform(transform);
                }
        );

        Vector3d a = this.vertices.get(0).pos;
        Vector3d b = this.vertices.get(1).pos;
        Vector3d c = this.vertices.get(2).pos;

        this.plane.normal = b.minus(a).cross(c.minus(a)).normalized();
        this.plane.dist = this.plane.normal.dot(a);

        if (transform.isMirror()) {
            // the transformation includes mirroring. flip polygon
            flip();

        }
        return this;
    }

    /**
     * Returns a transformed copy of this polygon.
     *
     *  Note:  if the applied transformation performs a mirror operation
     * the vertex order of this polygon is reversed.
     *
     *  Note:  this polygon is not modified
     *
     * @param transform the transformation to apply
     * @return a transformed copy of this polygon
     */
    public Polygon transformed(Transform transform) {
        return clone().transform(transform);
    }

    /**
     * Creates a polygon from the specified point list.
     *
     * @param points the points that define the polygon
     * @param shared shared property storage
     * @return a polygon defined by the specified point list
     */
    public static Polygon fromPoints(List<Vector3d> points,
            PropertyStorage shared) {
        return fromPoints(points, shared, null);
    }

    /**
     * Creates a polygon from the specified point list.
     *
     * @param points the points that define the polygon
     * @return a polygon defined by the specified point list
     */
    public static Polygon fromPoints(List<Vector3d> points) {
        return fromPoints(points, new PropertyStorage(), null);
    }

    /**
     * Creates a polygon from the specified points.
     *
     * @param points the points that define the polygon
     * @return a polygon defined by the specified point list
     */
    public static Polygon fromPoints(Vector3d... points) {
        return fromPoints(Arrays.asList(points), new PropertyStorage(), null);
    }

    /**
     * Creates a polygon from the specified point list.
     *
     * @param points the points that define the polygon
     * @param shared the shared
     * @param plane may be null
     * @return a polygon defined by the specified point list
     */
    private static Polygon fromPoints(
            List<Vector3d> points, PropertyStorage shared, Plane plane) {

        Vector3d normal
                = (plane != null) ? plane.normal.clone() : new Vector3d(0, 0, 0);

        List<Vertex> vertices = new ArrayList<>();

        for (Vector3d p : points) {
            Vector3d vec = p.clone();
            Vertex vertex = new Vertex(vec, normal);
            vertices.add(vertex);
        }

        return new Polygon(vertices, shared);
    }

    /**
     * Returns the bounds of this polygon.
     *
     * @return bouds of this polygon
     */
    public Bounds getBounds() {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;

        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < vertices.size(); i++) {

            Vertex vert = vertices.get(i);

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

        return new Bounds(
                new Vector3d(minX, minY, minZ),
                new Vector3d(maxX, maxY, maxZ));
    }

    /**
     * Contains.
     *
     * @param p the p
     * @return true, if successful
     */
    public boolean contains(Vector3d p) {
        // taken from http://www.java-gaming.org/index.php?topic=26013.0
        // and http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
        double px = p.x;
        double py = p.y;
        boolean oddNodes = false;
        double x2 = vertices.get(vertices.size() - 1).pos.x;
        double y2 = vertices.get(vertices.size() - 1).pos.y;
        double x1, y1;
        for (int i = 0; i < vertices.size(); x2 = x1, y2 = y1, ++i) {
            x1 = vertices.get(i).pos.x;
            y1 = vertices.get(i).pos.y;
            if (((y1 < py) && (y2 >= py))
                    || (y1 >= py) && (y2 < py)) {
                if ((py - y1) / (y2 - y1)
                        * (x2 - x1) < (px - x1)) {
                    oddNodes = !oddNodes;
                }
            }
        }
        return oddNodes;
    }
    
    /**
     * Contains.
     *
     * @param p the p
     * @return true, if successful
     */
    public boolean contains(Polygon p) {
        
        for (Vertex v : p.vertices) {
            if (!contains(v.pos)) {
                return false;
            }
        }
        
        return true;
    }

    /**
 * Gets the storage.
 *
 * @return the shared
 */
    public PropertyStorage getStorage() {

        if (shared == null) {
            shared = new PropertyStorage();
        }

        return shared;
    }

//	public Exception getCreationEventStackTrace() {
//		return creationEventStackTrace;
//	}

	public List<Vector3d> getPoints() {
		ArrayList<Vector3d> p =new ArrayList<>();
		for(Vertex v:vertices) {
			p.add(v.pos);
		}
		return p;
	}
	/**
	 * Movey.
	 *
	 * @param howFarToMove
	 *            the how far to move
	 * @return the csg
	 */
	// Helper/wrapper functions for movement
	public Polygon movey(Number howFarToMove) {
		return this.transformed(Transform.unity().translateY(howFarToMove.doubleValue()));
	}

	/**
	 * Movez.
	 *
	 * @param howFarToMove
	 *            the how far to move
	 * @return the csg
	 */
	public Polygon movez(Number howFarToMove) {
		return this.transformed(Transform.unity().translateZ(howFarToMove.doubleValue()));
	}

	/**
	 * Movex.
	 *
	 * @param howFarToMove
	 *            the how far to move
	 * @return the csg
	 */
	public Polygon movex(Number howFarToMove) {
		return this.transformed(Transform.unity().translateX(howFarToMove.doubleValue()));
	}

	/**
	 * Rotz.
	 *
	 * @param degreesToRotate
	 *            the degrees to rotate
	 * @return the csg
	 */
	// Rotation function, rotates the object
	public Polygon rotz(Number degreesToRotate) {
		return this.transformed(new Transform().rotZ(degreesToRotate.doubleValue()));
	}

	/**
	 * Roty.
	 *
	 * @param degreesToRotate
	 *            the degrees to rotate
	 * @return the csg
	 */
	public Polygon roty(Number degreesToRotate) {
		return this.transformed(new Transform().rotY(degreesToRotate.doubleValue()));
	}

	/**
	 * Rotx.
	 *
	 * @param degreesToRotate
	 *            the degrees to rotate
	 * @return the csg
	 */
	public Polygon rotx(Number degreesToRotate) {
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
	public Polygon scalez(Number scaleValue) {
		return this.transformed(new Transform().scaleZ(scaleValue.doubleValue()));
	}

	/**
	 * Scaley.
	 *
	 * @param scaleValue
	 *            the scale value
	 * @return the csg
	 */
	public Polygon scaley(Number scaleValue) {
		return this.transformed(new Transform().scaleY(scaleValue.doubleValue()));
	}

	/**
	 * Scalex.
	 *
	 * @param scaleValue
	 *            the scale value
	 * @return the csg
	 */
	public Polygon scalex(Number scaleValue) {
		return this.transformed(new Transform().scaleX(scaleValue.doubleValue()));
	}

	/**
	 * Scale.
	 *
	 * @param scaleValue
	 *            the scale value
	 * @return the csg
	 */
	public Polygon scale(Number scaleValue) {
		return this.transformed(new Transform().scale(scaleValue.doubleValue()));
	}

	 /**
     * Indicates whether this polyon is valid, i.e., if it
     *
     * @return
     */
    public boolean isValid() {
        return valid;
    }

    private boolean valid = true;
}
