package eu.mihosoft.vrl.v3d;

import java.util.ArrayList;

public class Parabola {

  double Radius, w, a, b, FocalLength;
  boolean fromEq = true;


  // from https://www.mathsisfun.com/geometry/parabola.html
  private Parabola() {

  }

  private double computeY(double x) {
    if (fromEq)
      return (a * x * x) + (b * x);
    else {
      return (x * x) / (FocalLength * 4.0);
    }
  }

  public Parabola fromEquation(double Radius, double a, double b) {
	  if(Radius<=0)
	  		throw new NumberFormatException("radius can not be negative");
    this.Radius = Radius;
    if (Math.abs(a) == 0) {
      throw new RuntimeException("A value in parabola must be non zero");
    }
    this.a = a;
    this.b = b;
    fromEq = true;
    return this;
  }

  public Parabola fromFocalLength(double Radius, double Focus) {
    this.Radius = Radius;
    if (Math.abs(Focus) == 0) {
      throw new RuntimeException("A value in parabola must be non zero");
    }
    FocalLength = Focus;
    fromEq = false;
    return this;
  }

  public ArrayList<Vector3d> getpoints() {
    ArrayList<Vector3d> points = new ArrayList<>();
    points.add(new Vector3d(0, computeY(Radius)));
    for (double i = 0; i <= 1; i += 0.05) {
      double x = Radius * i;
      double y = computeY(x);
      points.add(new Vector3d(x, y));
    }
    points.add(new Vector3d(Radius, computeY(Radius)));
    return points;
  }

  public static CSG coneByEquation(double Radius, double a, double b) {
    ArrayList<Vector3d> points = new Parabola().fromEquation(Radius, a, b).getpoints();// upper
                                                                                       // right
                                                                                       // corner

    ArrayList<Vector3d> pointsOut = new ArrayList<>();
    for (double i = 0; i <= 360; i += 10) {
      Transform transform = new Transform().roty(i);
      for (Vector3d p : points)
        pointsOut.add(p.transformed(transform));

    }
    return eu.mihosoft.vrl.v3d.ext.quickhull3d.HullUtil.hull(pointsOut);
  }
  
  public static CSG cone(double Radius, double height) {
	    return coneByHeight(Radius,height,0).rotx(90).toZMin();
  }
  public static CSG cone(double Radius, double height, double b) {
		    return coneByHeight(Radius,height,b).rotx(90).toZMin();
  }
  public static CSG coneByHeight(double Radius, double height) {
    return coneByHeight(Radius,height,0);
  }
  
  public static CSG coneByHeight(double Radius, double height, double b) {
    double a=(height-(b*Radius))/(Radius*Radius);
    ArrayList<Vector3d> points = new Parabola().fromEquation(Radius, a, b).getpoints();// upper
    // right
    // corner
    ArrayList<Vector3d> pointsOut = new ArrayList<>();
    for (double i = 0; i <= 360; i += 10) {
      Transform transform = new Transform().roty(i);
      for (Vector3d p : points)
        pointsOut.add(p.transformed(transform));

    }
    return eu.mihosoft.vrl.v3d.ext.quickhull3d.HullUtil.hull(pointsOut);
  }

  public static CSG coneByFocalLength(double Radius, double FocalLength) {
    ArrayList<Vector3d> points = new Parabola().fromFocalLength(Radius, FocalLength).getpoints();// upper
    // right
    // corner

    ArrayList<Vector3d> pointsOut = new ArrayList<>();
    for (double i = 0; i <= 360; i += 10) {
      Transform transform = new Transform().roty(i);
      for (Vector3d p : points)
        pointsOut.add(p.transformed(transform));

    }
    return eu.mihosoft.vrl.v3d.ext.quickhull3d.HullUtil.hull(pointsOut);
  }

  public static CSG extrudeByEquation(double Radius, double a, double b, double thickness) {
    return Extrude.points(new Vector3d(0, 0, thickness), // This is the extrusion depth
        new Parabola().fromEquation(Radius, a, b).getpoints()// upper right corner
    );
  }

}
