JCSG
=======

[![Build Status](https://travis-ci.org/miho/JCSG.png?branch=master)](https://travis-ci.org/miho/JCSG)
[![Javadocs](https://www.javadoc.io/badge/eu.mihosoft.vrl.jcsg/jcsg.svg)](https://www.javadoc.io/doc/eu.mihosoft.vrl.jcsg/jcsg)
<a href="https://foojay.io/today/works-with-openjdk">
   <img align="right" 
        src="https://github.com/foojayio/badges/raw/main/works_with_openjdk/Works-with-OpenJDK.png"   
        width="100">
</a>

<br>


Java implementation of BSP based CSG (Constructive Solid Geometry). It is the only simple and free Java implementation I am aware of. This implementation uses an optimized CSG algorithm based on [csg.js](https://github.com/evanw/csg.js) (see `CSG` and `Node` classes). Thanks to the author for creating the [csg.js](https://github.com/evanw/csg.js) library.

There's also the related [VCSG](https://github.com/miho/VCSG) library, a [plugin for VRL-Studio](https://github.com/VRL-Studio/VRL-JCSG) and two extension libraries: [JCSG-MeshExtension](https://github.com/miho/JCSG-MeshExtensions) and [JCSG-PathExtension](https://github.com/miho/JCSG-PathExtensions).

In addition to CSG this library provides the following features:

- optimized `difference()` and `union()` operations (many thanks to Sebastian Reiter)
- extrusion of concave, non-intersecting polygons (uses [Poly2Tri](https://code.google.com/p/poly2tri/) for triangulation)
- convex hull (uses [QuickHull3D](https://www.cs.ubc.ca/~lloyd/java/quickhull3d.html))
- weighted transformations (Scale, Rotation, Translation and Mirror)
- STL import and export (STLLoader from [Fiji](https://github.com/fiji/fiji/blob/master/src-plugins/3D_Viewer/src/main/java/customnode/STLLoader.java))
- OBJ export including material information (see screenshot below)
- supports conversion of CSG's to `JavaFX 3D` nodes
- 3d text support (using [FXyz](https://github.com/FXyz/FXyz))

**JCSG** on [stackoverflow](http://stackoverflow.com/search?q=jcsg).

![](/resources/screenshot2.png)

To see what's possible with JCSG try [JFXScad](https://github.com/miho/JFXScad).

![](https://raw.githubusercontent.com/miho/JFXScad/master/resources/img/screenshot-02.png)

## How to Build JCSG

### Requirements

- Java >= 11
- Internet connection (dependencies are downloaded automatically)
- IDE: [Gradle](http://www.gradle.org/) Plugin (not necessary for command line usage)

### IDE

Open the `JCSG` [Gradle](http://www.gradle.org/) project in your favourite IDE (tested with NetBeans 7.4) and build it
by calling the `assemble` task.

### Command Line

Navigate to the [Gradle](http://www.gradle.org/) project (e.g., `path/to/JCSG`) and enter the following command

#### Bash (Linux/OS X/Cygwin/other Unix-like shell)

    bash gradlew assemble
    
#### Windows (CMD)

    gradlew assemble

## Code Sample:


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

## Thanks to

 - [JetBrains](https://www.jetbrains.com) for their [IntelliJ IDEA](https://www.jetbrains.com/idea/) license(s)

 - Evan Wallace for creating the JavaScript library [csg.js](https://github.com/evanw/csg.js)
