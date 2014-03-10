JavaCSG
=======

Java implementation of BSP based CSG (Constructive Solid Geometry). This implementation uses the same CSG algorithm as [csg.js](https://github.com/evanw/csg.js). Thanks to the author for creating the [csg.js](https://github.com/evanw/csg.js) library.

In addition to [csg.js](https://github.com/evanw/csg.js) this library provides the following features:

- extrusion of concave, non-intersecting polygons. Uses [Poly2Tri](https://code.google.com/p/poly2tri/) for triangulation.
- Transformations (Scale, Rotation, Translation and Mirror)
- STL import and export (STLLoader from [Fiji](https://github.com/fiji/fiji/blob/master/src-plugins/3D_Viewer/src/main/java/customnode/STLLoader.java))


![](/resources/screenshot1.png)


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
