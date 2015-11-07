/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;

// TODO: Auto-generated Javadoc
/**
 * The Class MeshContainer.
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class MeshContainer {

    /** The meshes. */
    private final List<Mesh> meshes;
    
    /** The materials. */
    private final List<Material> materials;
    
    /** The width. */
    private final double width;
    
    /** The height. */
    private final double height;
    
    /** The depth. */
    private final double depth;
    
    /** The bounds. */
    private final Bounds bounds;

    /** The root. */
    private final Group root = new Group();
    
    /** The view container. */
    private Pane viewContainer;
    
    /** The sub scene. */
    private SubScene subScene;

    /**
     * Instantiates a new mesh container.
     *
     * @param min the min
     * @param max the max
     * @param meshes the meshes
     */
    MeshContainer(Vector3d min, Vector3d max, Mesh... meshes) {
        this(min, max, Arrays.asList(meshes));
    }

    /**
     * Instantiates a new mesh container.
     *
     * @param min the min
     * @param max the max
     * @param meshes the meshes
     */
    MeshContainer(Vector3d min, Vector3d max, List<Mesh> meshes) {
        this.meshes = meshes;
        this.materials = new ArrayList<>();
        this.bounds = new Bounds(min, max);
        this.width = bounds.getBounds().x;
        this.height = bounds.getBounds().y;
        this.depth = bounds.getBounds().z;

        PhongMaterial material = new PhongMaterial(Color.RED);
        for (Mesh mesh : meshes) {
            materials.add(material);
        }
    }

    /**
     * Instantiates a new mesh container.
     *
     * @param min the min
     * @param max the max
     * @param meshes the meshes
     * @param materials the materials
     */
    MeshContainer(Vector3d min, Vector3d max, List<Mesh> meshes, List<Material> materials) {
        this.meshes = meshes;
        this.materials = materials;
        this.bounds = new Bounds(min, max);
        this.width = bounds.getBounds().x;
        this.height = bounds.getBounds().y;
        this.depth = bounds.getBounds().z;

        if (materials.size() != meshes.size()) {
            throw new IllegalArgumentException("Mesh list and Material list must not differ in size!");
        }

    }

    /**
     * Gets the width.
     *
     * @return the width
     */
    public double getWidth() {
        return width;
    }

    /**
     * Gets the height.
     *
     * @return the height
     */
    public double getHeight() {
        return height;
    }

    /**
     * Gets the depth.
     *
     * @return the depth
     */
    public double getDepth() {
        return depth;
    }

    /**
     * Gets the meshes.
     *
     * @return the mesh
     */
    public List<Mesh> getMeshes() {
        return meshes;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return bounds.toString();
    }

    /**
     * Gets the bounds.
     *
     * @return the bounds
     */
    public Bounds getBounds() {
        return bounds;
    }

    /**
     * Gets the materials.
     *
     * @return the materials
     */
    public List<Material> getMaterials() {
        return materials;
    }

    /**
     * Gets the as mesh views.
     *
     * @return the as mesh views
     */
    public List<MeshView> getAsMeshViews() {
        List<MeshView> result = new ArrayList<>(meshes.size());

        for (int i = 0; i < meshes.size(); i++) {

            Mesh mesh = meshes.get(i);
            Material mat = materials.get(i);

            MeshView view = new MeshView(mesh);
            view.setMaterial(mat);
            view.setCullFace(CullFace.NONE);

            result.add(view);
        }

        return result;
    }

//    public javafx.scene.Node getAsInteractiveSubSceneNode() {
//
//        if (viewContainer != null) {
//            return viewContainer;
//        }
//
//        viewContainer = new Pane();
//
//        SubScene subScene = new SubScene(getRoot(), 100, 100, true, SceneAntialiasing.BALANCED);
////        subScene.setFill(Color.BLACK);
//
//        subScene.widthProperty().bind(viewContainer.widthProperty());
//        subScene.heightProperty().bind(viewContainer.heightProperty());
//
//        PerspectiveCamera subSceneCamera = new PerspectiveCamera(false);
//        subScene.setCamera(subSceneCamera);
//
//        viewContainer.getChildren().add(subScene);
//
//        getRoot().layoutXProperty().bind(viewContainer.widthProperty().divide(2));
//        getRoot().layoutYProperty().bind(viewContainer.heightProperty().divide(2));
//
//        viewContainer.boundsInLocalProperty().addListener(
//                (ObservableValue<? extends javafx.geometry.Bounds> ov, javafx.geometry.Bounds t, javafx.geometry.Bounds t1) -> {
//                    setMeshScale(this, t1, getRoot());
//                });
//
//        VFX3DUtil.addMouseBehavior(getRoot(), viewContainer, MouseButton.PRIMARY);
//
//        return viewContainer;
//    }
//
//    private void setMeshScale(MeshContainer meshContainer, javafx.geometry.Bounds t1, final Group meshView) {
//        double maxDim
//                = Math.max(meshContainer.getWidth(),
//                        Math.max(meshContainer.getHeight(), meshContainer.getDepth()));
//
//        double minContDim = Math.min(t1.getWidth(), t1.getHeight());
//
//        double scale = minContDim / (maxDim * 2);
//
//        //System.out.println("scale: " + scale + ", maxDim: " + maxDim + ", " + meshContainer);
//        meshView.setScaleX(scale);
//        meshView.setScaleY(scale);
//        meshView.setScaleZ(scale);
//    }
}
