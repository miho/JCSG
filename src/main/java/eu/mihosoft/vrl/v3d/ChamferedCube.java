package eu.mihosoft.vrl.v3d;

import java.util.List;

public class ChamferedCube extends Primitive {
  double w, h, d, chamferHeight;

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
  public ChamferedCube(double w, double h, double d, double chamferHeight) {
    this.w = w;
    this.h = h;
    this.d = d;
    this.chamferHeight = chamferHeight;
  }


  /*
   * (non-Javadoc)
   * 
   * @see eu.mihosoft.vrl.v3d.Primitive#toPolygons()
   */
  @Override
  public List<Polygon> toPolygons() {
    CSG cube1 = new Cube(w - chamferHeight * 2, h, d - chamferHeight * 2).toCSG();
    CSG cube2 = new Cube(w, h - chamferHeight * 2, d - chamferHeight * 2).toCSG();
    CSG cube3 = new Cube(w - chamferHeight * 2, h - chamferHeight * 2, d).toCSG();
    return cube1.union(cube2).union(cube3).hull().getPolygons();
  }

}
