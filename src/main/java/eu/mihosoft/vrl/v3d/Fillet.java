package eu.mihosoft.vrl.v3d;

import java.util.ArrayList;
import java.util.List;

public class Fillet extends Primitive {

  double w, h;

  /** The properties. */
  private final PropertyStorage properties = new PropertyStorage();

  public PropertyStorage getProperties() {
    return properties;
  }

  /**
   * Constructor. Creates a new cuboid with center {@code [0,0,0]} and with the specified
   * dimensions.
   *
   * @param w width
   * @param h height
   * @param d depth
   */
  public Fillet(double w, double h) {
    this.w = w;
    this.h = h;
  }


  public static CSG corner(double rad, double angle) {
    return CSG.unionAll(Extrude.revolve(new Fillet(rad, 0.01).toCSG().rotz(-90), 0, angle, 4))
        .difference(Extrude.revolve(new Sphere(rad).toCSG().toYMin().toZMin(), 0, angle, 4));
    // .rotz(180)
  }

  public static CSG outerFillet(CSG base, double rad) {
    List<Polygon> polys = Slice.slice(base);
    return base.union(outerFillet(polys, rad));
  }

  public static CSG outerFillet(List<Polygon> polys, double rad) {

    ArrayList<CSG> parts = new ArrayList<>();
    for (Polygon p : polys) {
      int size = p.vertices.size();
      for (int i = 0; i < size; i++) {
        // if(i>1)
        // continue;
        int next = i + 1;
        if (next == size)
          next = 0;
        int nextNext = next + 1;
        if (nextNext == size)
          nextNext = 0;
        Vector3d position0 = p.vertices.get(i).pos;
        Vector3d position1 = p.vertices.get(next).pos;
        Vector3d position2 = p.vertices.get(nextNext).pos;
        Vector3d seg1 = position0.minus(position1);
        Vector3d seg2 = position2.minus(position1);
        double len = seg1.magnitude();
        double angle = Math.toDegrees(seg1.angle(seg2));
        double angleAbs = Math.toDegrees(seg1.angle(Vector3d.Y_ONE));
        CSG fillet = new Fillet(rad, len).toCSG().toYMax();
        // .roty(90)
        if (seg1.x < 0) {
          angleAbs = 360 - angleAbs;
          // fillet=fillet.toYMax()
        }
        if (Math.abs(angle) > 0.01 && Math.abs(angle) < 180) {
          parts.add(corner(rad, angle).rotz(angleAbs).move(position0));
        }
        // println "Fillet corner Angle = "+angle
        parts.add(fillet.rotz(angleAbs).move(position0));
      }
    }
    return CSG.unionAll(parts);
  }

  /*
   * (non-Javadoc)
   * 
   * @see eu.mihosoft.vrl.v3d.Primitive#toPolygons()
   */
  @Override
  public List<Polygon> toPolygons() {
    CSG simpleSyntax = new Cylinder(w, h + 1).toCSG() // a one line Cylinder
        .rotx(90).toXMin().toZMin().movey(-0.5);
    CSG cubeSection = new Cube(w - 0.1, h, w - 0.1).toCSG().toXMin().toZMin().toYMin();
    return cubeSection.difference(simpleSyntax).getPolygons();
  }
}
