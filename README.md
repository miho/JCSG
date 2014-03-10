JavaCSG
=======

Java implementation of BSP based CSG (Constructive Solid Geometry). It is the only Java implementation I am aware of. This implementation uses the same CSG algorithm as [csg.js](https://github.com/evanw/csg.js) (see `CSG` and `Node` classes). Thanks to the author for creating the [csg.js](https://github.com/evanw/csg.js) library.

In addition to [csg.js](https://github.com/evanw/csg.js) this library provides the following features:

- extrusion of concave, non-intersecting polygons. Uses [Poly2Tri](https://code.google.com/p/poly2tri/) for triangulation.
- Transformations (Scale, Rotation, Translation and Mirror)
- STL import and export (STLLoader from [Fiji](https://github.com/fiji/fiji/blob/master/src-plugins/3D_Viewer/src/main/java/customnode/STLLoader.java))


![](/resources/screenshot1.png)

## How to Build JavaCSG

### Requirements

- Java >= 1.8
- Internet connection (dependencies are downloaded automatically)
- IDE: [Gradle](http://www.gradle.org/) Plugin (not necessary for command line usage)

### IDE

Open the `JavaCSG` [Gradle](http://www.gradle.org/) project in your favourite IDE (tested with NetBeans 7.4) and build it
by calling the `assemble` task.

### Command Line

Navigate to the [Gradle](http://www.gradle.org/) project (e.g., `path/to/JavaCSG`) and enter the following command

#### Bash (Linux/OS X/Cygwin/other Unix-like shell)

    sh gradlew assemble
    
#### Windows (CMD)

    gradlew assemble

## Sample:


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
