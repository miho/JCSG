package eu.mihosoft.vrl.v3d;

import java.util.List;

public class Isosceles extends Primitive {
  double w, h, d;

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
  public Isosceles(double w, double h, double d) {
    this.w = w;
    this.h = h;
    this.d = d;
  }


  /*
   * (non-Javadoc)
   * 
   * @see eu.mihosoft.vrl.v3d.Primitive#toPolygons()
   */
  @Override
  public List<Polygon> toPolygons() {
	  if(w<=0)
  		throw new NumberFormatException("w can not be negative");
	  if(h<=0)
	  		throw new NumberFormatException("h can not be negative");
	  if(d<=0)
	  		throw new NumberFormatException("d can not be negative");
      CSG polygon = Extrude.points(new Vector3d(0, 0, w),// This is the  extrusion depth
              new Vector3d(0,0),// All values after this are the points in the polygon
              new Vector3d(0,-h/2),// upper right corner
              new Vector3d(d,0),// Bottom right corner
              new Vector3d(0,h/2)// upper right corner
      ).roty(90)
      .rotz(180);
      return polygon.getPolygons();
  }

}
