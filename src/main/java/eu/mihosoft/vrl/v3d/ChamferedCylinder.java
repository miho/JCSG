package eu.mihosoft.vrl.v3d;

import java.util.List;

public class ChamferedCylinder extends Primitive {
  double r, h, chamferHeight;

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
  public ChamferedCylinder(double r, double h, double chamferHeight) {
    this.r = r;
    this.h = h;
    this.chamferHeight = chamferHeight;
  }


  /*
   * (non-Javadoc)
   * 
   * @see eu.mihosoft.vrl.v3d.Primitive#toPolygons()
   */
  @Override
  public List<Polygon> toPolygons() {
    CSG cube1 = new Cylinder(r - chamferHeight, h).toCSG();
    CSG cube2 = new Cylinder(r, h - chamferHeight * 2).toCSG().movez(chamferHeight);
    return cube1.union(cube2).hull().getPolygons();
  }
}
