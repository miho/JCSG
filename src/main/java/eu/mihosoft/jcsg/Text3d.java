///**
// * Text3d.java
// *
// * Copyright 2014-2016 Michael Hoffer <info@michaelhoffer.de>. All rights
// * reserved.
// *
// * Redistribution and use in source and binary forms, with or without
// * modification, are permitted provided that the following conditions are met:
// *
// * 1. Redistributions of source code must retain the above copyright notice,
// * this list of conditions and the following disclaimer.
// *
// * 2. Redistributions in binary form must reproduce the above copyright notice,
// * this list of conditions and the following disclaimer in the documentation
// * and/or other materials provided with the distribution.
// *
// * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info@michaelhoffer.de> "AS IS"
// * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// * ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info@michaelhoffer.de> OR
// * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
// * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
// * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
// * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
// * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
// * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
// * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// *
// * The views and conclusions contained in the software and documentation are
// * those of the authors and should not be interpreted as representing official
// * policies, either expressed or implied, of Michael Hoffer
// * <info@michaelhoffer.de>.
// */
//package eu.mihosoft.jcsg;
//
//import java.lang.reflect.Field;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.CountDownLatch;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import javafx.application.Platform;
//import javafx.embed.swing.JFXPanel;
//import javax.swing.SwingUtilities;
//import org.fxyz.shapes.primitives.Text3DMesh;
//import org.fxyz.shapes.primitives.TexturedMesh;
//import org.fxyz.utils.MeshUtils;
//
///**
// * 3d text primitive.
// *
// * @author Michael Hoffer <info@michaelhoffer.de>
// */
//public class Text3d implements Primitive {
//
//    private final PropertyStorage properties = new PropertyStorage();
//
//    private final String text;
//    private String fontName;
//    private double size;
//    private double depth;
//    Text3DMesh t3dMesh;
//    private boolean noCenter;
//
//    // 08.11.2016
//    // TODO report bug to FXyz authors
//    // factor by which we multiply text generation to minimize rendering errors
//    double scaleFactor = 100;
//
//    /**
//     * Constructor.
//     *
//     * @param text text
//     */
//    public Text3d(String text) {
//        this(text, "Arial", 12, 1.0);
//    }
//
//    /**
//     * Constructor.
//     *
//     * @param text text
//     * @param depth text depth (z thickness)
//     */
//    public Text3d(String text, double depth) {
//        this(text, "Arial", 12, depth);
//    }
//
//    /**
//     * Constructor.
//     *
//     * @param text text
//     * @param fontName font name, e.g., "Arial"
//     * @param fontSize font size
//     * @param depth text depth (z thickness)
//     */
//    public Text3d(String text, String fontName, double fontSize, double depth) {
//        this.text = text;
//        this.fontName = fontName;
//        this.size = fontSize;
//        this.depth = depth;
//
//        // scaled size and height (see scaleFactor docs)
//        int realSize = (int) (fontSize * scaleFactor);
//        double realHeight = depth * scaleFactor;
//
//        invokeAndWait(() -> {
//            t3dMesh = new Text3DMesh(
//                    text, fontName, realSize, false, realHeight, 0, 0);
//        });
//
//    }
//
//    @Override
//    public List<Polygon> toPolygons() {
//        return new MeshRetriever(this).toCSG(noCenter).getPolygons();
//    }
//
//    @Override
//    public PropertyStorage getProperties() {
//        return properties;
//    }
//
//    public Text3d noCenter() {
//        this.noCenter = true;
//
//        return this;
//    }
//
//    private static void invokeAndWait(Runnable action) {
//
//        if (action == null) {
//            throw new NullPointerException("action");
//        }
//
//        // run synchronously on JavaFX thread
//        if (Platform.isFxApplicationThread()) {
//            action.run();
//            return;
//        }
//
//        // init JavaFX toolkit
//        Platform.setImplicitExit(true);
//
//        setupJavaFX();
//
//        // queue on JavaFX thread and wait for completion
//        final CountDownLatch doneLatch = new CountDownLatch(1);
//        Platform.runLater(() -> {
//            try {
//                action.run();
//            } finally {
//                doneLatch.countDown();
//            }
//        });
//
//        try {
//            doneLatch.await();
//        } catch (InterruptedException e) {
//            // ignore exception
//        }
//    }
//
//    private static void setupJavaFX() throws RuntimeException {
//        final CountDownLatch latch = new CountDownLatch(1);
//        SwingUtilities.invokeLater(() -> {
//            new JFXPanel(); // initializes JavaFX environment
//            latch.countDown();
//        });
//
//        try {
//            latch.await();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//}
//
//class MeshRetriever {
//
//    private final Text3d t3dMesh;
//
//    public MeshRetriever(Text3d t3dMesh) {
//        this.t3dMesh = t3dMesh;
//    }
//
//    public CSG toCSG(boolean noCenter) {
//        List<CSG> csgs = new ArrayList<>();
//
//        CSG result = null;
//
//        List<TexturedMesh> meshes = getMeshes();
//
//        for (int i = 0; i < meshes.size(); i++) {
//
//            TexturedMesh mesh = meshes.get(i);
//
//            CSG csg = MeshUtils.mesh2CSG(mesh);
//
//            double xTransform = mesh.getTransforms().stream().
//                    mapToDouble(tr -> tr.getTx()).sum();
//
//            csg = csg.transformed(Transform.unity().translateX(xTransform));
//
//            // rescale final mesh (see scaleFactor docs)
//            double xScale = 1.0 / t3dMesh.scaleFactor;
//            csg = csg.transformed(Transform.unity().
//                    scale(xScale, -xScale, xScale));
//
//            if (result == null) {
//                result = csg;
//            } else {
//                result = result.dumbUnion(csg);
//            }
//        }
//
//        if (!noCenter) {
//            result = result.transformed(
//                    Transform.unity().translate(
//                            -result.getBounds().getBounds().x * 0.5,
//                            -result.getBounds().getBounds().y * 0.5,
//                            -result.getBounds().getBounds().z * 0.5)
//            );
//        }
//
//        return result;
//    }
//
//    public List<TexturedMesh> getMeshes() {
//        try {
//            Field field = Text3DMesh.class.getDeclaredField("meshes");
//
//            field.setAccessible(true);
//
//            return (List<TexturedMesh>) field.get(t3dMesh.t3dMesh);
//        } catch (NoSuchFieldException ex) {
//            Logger.getLogger(MeshRetriever.class.getName()).
//                    log(Level.SEVERE, null, ex);
//        } catch (SecurityException ex) {
//            Logger.getLogger(MeshRetriever.class.getName()).
//                    log(Level.SEVERE, null, ex);
//        } catch (IllegalArgumentException ex) {
//            Logger.getLogger(MeshRetriever.class.getName()).
//                    log(Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            Logger.getLogger(MeshRetriever.class.getName()).
//                    log(Level.SEVERE, null, ex);
//        }
//
//        return null;
//    }
//
//}
