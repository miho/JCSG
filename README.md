JavaCSG
=======

Java implementation of BSP based CSG (Constructive Solid Geometry). This implementation uses the same CSG algorithm as [csg.js](https://github.com/evanw/csg.js). In addition to [csg.js](https://github.com/evanw/csg.js) this library supports extrusion of concave, non-intersecting polygons. It uses [Poly2Tri](https://code.google.com/p/poly2tri/) for triangulation.


```java

        // we use cube and sphere as base geometries
        CSG cube = new Cube(2).toCSG();
        CSG sphere = new Sphere(1.25).toCSG();

        // perform union, difference and intersection
        CSG cubePlusSphere = cube.union(sphere);
        CSG cubeMinusSphere = cube.difference(sphere);
        CSG cubeIntersectSphere = cube.intersect(sphere);
        
        // translate geometries to prevent overlapping 
        CSG union = cube.
                union(sphere.transformed(Transform.unity().translateX(3))).
                union(cubePlusSphere.transformed(Transform.unity().translateX(6))).
                union(cubeMinusSphere.transformed(Transform.unity().translateX(9))).
                union(cubeIntersectSphere.transformed(Transform.unity().translateX(12)));
        
        // save union as stl
        try {
            FileUtil.write(
                    Paths.get("sample.stl"),
                    union.toStlString()
            );
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
```
