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

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class MeshContainer {

    private final List<Mesh> meshes;
    private final List<Material> materials;
    private final double width;
    private final double height;
    private final double depth;
    private final Bounds bounds;

    private final Group root = new Group();
    private Pane viewContainer;
    private SubScene subScene;

    MeshContainer(Vector3d min, Vector3d max, Mesh... meshes) {
        this(min, max, Arrays.asList(meshes));
    }

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
     * @return the width
     */
    public double getWidth() {
        return width;
    }

    /**
     * @return the height
     */
    public double getHeight() {
        return height;
    }

    /**
     * @return the depth
     */
    public double getDepth() {
        return depth;
    }

    /**
     * @return the mesh
     */
    public List<Mesh> getMeshes() {
        return meshes;
    }

    @Override
    public String toString() {
        return bounds.toString();
    }

    /**
     * @return the bounds
     */
    public Bounds getBounds() {
        return bounds;
    }

    /**
     * @return the materials
     */
    public List<Material> getMaterials() {
        return materials;
    }

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
