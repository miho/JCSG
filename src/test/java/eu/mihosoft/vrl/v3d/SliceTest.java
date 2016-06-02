package eu.mihosoft.vrl.v3d;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Ryan Benasutti on 5/30/2016.
 */

public class SliceTest
{
    @Test
    public void slice() throws Exception
    {
        //Create a CSG to slice
        CSG sliced = new Cube(10, 10, 10).toCSG().union(new Sphere(6.5).toCSG());

        //Get a slice
        List<Polygon> polygons = Slice.slice(sliced, new Transform(), 0);

        //Construct a CSG from that Polygon List
        CSG finished = CSG.fromPolygons(polygons);

        System.out.println(finished.toObjString());
    }

}