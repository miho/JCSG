package eu.mihosoft.vrl.v3d;

import java.util.List;

public class Fillet extends Primitive {

  double w, h;

  /** The properties. */
  private final PropertyStorage properties = new PropertyStorage();

public PropertyStorage getProperties(){
  return properties;
}

  /**
   * Constructor. Creates a new cuboid with center {@code [0,0,0]} and with
   * the specified dimensions.
   *
   * @param w width
   * @param h height
   * @param d depth
   */
  public Fillet(double w, double h) {
     this.w=w;
     this.h=h;
  }


  /*
   * (non-Javadoc)
   * 
   * @see eu.mihosoft.vrl.v3d.Primitive#toPolygons()
   */
  @Override
  public List<Polygon> toPolygons() {
     CSG simpleSyntax =new Cylinder(w,h+1).toCSG() // a one line Cylinder
                  .rotx(90)
                  .toXMin()
                  .toZMin()
                  .movey(-0.5);
     CSG cubeSection = new Cube(w-0.1,h,w-0.1).toCSG()
                  .toXMin()
                  .toZMin()
                  .toYMin();
      return cubeSection.difference(simpleSyntax).getPolygons();
  }
}
