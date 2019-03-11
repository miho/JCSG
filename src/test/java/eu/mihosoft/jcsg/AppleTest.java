package eu.mihosoft.jcsg;

import java.io.IOException;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.mihosoft.jcsg.CSG;
import eu.mihosoft.jcsg.Cube;
import eu.mihosoft.jcsg.STL;
import eu.mihosoft.vvecmath.Transform;
import eu.mihosoft.vvecmath.Vector3d;

public class AppleTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppleTest.class);

  String originalFilename = "Apple.stl";

  @After
  public void tearDown() throws Exception {}

  @Test
  public void test() throws IOException {
    CSG apple = STL.file(Paths.get(originalFilename)).optimization(CSG.OptType.POLYGON_BOUND);
    
    // center model around zero point
    
    double xDim = apple.getBounds().getMax().getX()-apple.getBounds().getMin().getX();
    double yDim = apple.getBounds().getMax().getY()-apple.getBounds().getMin().getY();
    double zDim = apple.getBounds().getMax().getZ()-apple.getBounds().getMin().getZ();

    Vector3d moveToCenterVector = Vector3d.xyz(xDim/2-apple.getBounds().getMax().getX(), yDim/2-apple.getBounds().getMax().getY(), zDim/2-apple.getBounds().getMax().getZ());
    
    LOGGER.info("center=" + apple.getBounds().getCenter().getX() + ":" 
        + apple.getBounds().getCenter().getY() + ":" 
        + apple.getBounds().getCenter().getZ());

    LOGGER.info("max=" + apple.getBounds().getMax().getX() + ":" 
        + apple.getBounds().getMax().getY() + ":" 
        + apple.getBounds().getMax().getZ());

    LOGGER.info("min=" + apple.getBounds().getMin().getX() + ":" 
        + apple.getBounds().getMin().getY() + ":" 
        + apple.getBounds().getMin().getZ());

    apple = apple
        .transformed(Transform.unity()
            .translate(moveToCenterVector)
            );
    
    Vector3d max =  apple.getBounds().getMax();
    Vector3d min =  apple.getBounds().getMin();

    double cubeMaxDimension = Math.max(Math.max(Math.max(max.x(), Math.abs(min.x())),Math.max(max.y(), Math.abs(min.y()))),Math.max(max.z(), Math.abs(min.z()))) ; 
    cubeMaxDimension+=1;

    CSG cube = new Cube(cubeMaxDimension*2).toCSG().
        transformed(Transform.unity().
                translateZ(cubeMaxDimension));

    CSG diff = apple.difference(cube);
    
  }

}
