package eu.mihosoft.vrl.v3d.svg;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;


import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Cube;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

@SuppressWarnings("restriction")
public class SVGExporter extends Application {

	private static Pane snapshotGroup;
	private static final double MMTOPX = 3.5409643774783404;

	private static void launchUI(){
		launch(new String[] {});
	}
	@SuppressWarnings("static-access")
	public static File export(CSG currentCsg, File defaultDir) {
		
		currentCsg = currentCsg.toYMin().toXMin();// prep CSG so it is in frame in the smae orentation as the z0 plane

		CSG slice = currentCsg.movez(-.1)
				.intersect(new Cube(currentCsg.getMaxX() - currentCsg.getMinX(),
						currentCsg.getMaxY() - currentCsg.getMinY(), .2)
						.toCSG()
						.toXMin()// allign to corner
						.toYMin())
				.setColor(Color.BLACK);
		System.out.println("Object bounds  y=" + (currentCsg.getMaxY() - currentCsg.getMinY()));
		System.out.println("Object bounds  x=" + (currentCsg.getMaxX() - currentCsg.getMinX()));
		MeshView sliceMesh = slice.getMesh();
		// sliceMesh.getTransforms().add(Transform.translate(centerX, centerY));
		AnchorPane anchor = new AnchorPane(sliceMesh);
		AnchorPane.setBottomAnchor(sliceMesh, (double) 0);
		AnchorPane.setTopAnchor(sliceMesh, (double) 0);
		AnchorPane.setLeftAnchor(sliceMesh, (double) 0);
		AnchorPane.setRightAnchor(sliceMesh, (double) 0);

		snapshotGroup = new Pane(anchor);
		snapshotGroup.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

		File baseDirForFiles =defaultDir;// FileSelectionFactory.GetFile(defaultDir, true);

		if (!baseDirForFiles.getAbsolutePath().toLowerCase().endsWith(".svg"))
			baseDirForFiles = new File(baseDirForFiles.getAbsolutePath() + ".svg");
		String imageName = baseDirForFiles.getAbsolutePath() + ".png";

		int snWidth = (int) 4096;
		int snHeight = (int) 4096;

		double realWidth = snapshotGroup.getBoundsInLocal().getWidth();
		double realHeight = snapshotGroup.getBoundsInLocal().getHeight();

		double scaleX = snWidth / realWidth;
		double scaleY = snHeight / realHeight;

		double scale = Math.min(scaleX, scaleY);

		SnapshotParameters snapshotParameters = new SnapshotParameters();
		snapshotParameters.setTransform(new Scale(scale, scale));
		snapshotParameters.setDepthBuffer(true);
		snapshotParameters.setFill(Color.TRANSPARENT);

		WritableImage snapshot = new WritableImage(snWidth, (int) (realHeight * scale));
		File finalDir = baseDirForFiles;
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				snapshotGroup.snapshot(snapshotParameters, snapshot);
//				new Thread(new Runnable() {
//					@Override
//					public void run() {
//						launchUI();
//
//					}
//				}).start();

				new Thread(new Runnable() {
					@Override
					public void run() {
						System.out.println("Converting CSG to image...");
						try {
							ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", new File(imageName));
						} catch (IOException ex) {
							ex.printStackTrace();
							//Log.error(ex.getMessage());
						}
						try {
							float outputScale = (float) (MMTOPX / scale);
							// Options
							HashMap<String, Float> options = new HashMap<String, Float>();

							// Tracing
							options.put("ltres", 1f);// Error treshold for
														// straight lines.
							options.put("qtres", 1f);// Error treshold for
														// quadratic splines.
							options.put("pathomit", 0.02f);// Edge node paths
														// shorter than this
														// will be discarded for
														// noise reduction.

							// Color quantization
							options.put("colorsampling", 1f); // 1f means true ;
																// 0f means
																// false:
																// starting with
																// generated
																// palette
							options.put("numberofcolors", 16f);// Number of
																// colors to use
																// on palette if
																// pal object is
																// not defined.
							options.put("mincolorratio", 0.02f);// Color
																// quantization
																// will
																// randomize a
																// color if
																// fewer pixels
																// than (total
																// pixels*mincolorratio)
																// has it.
							options.put("colorquantcycles", 3f);// Color
																// quantization
																// will be
																// repeated this
																// many times.
							//
							// SVG rendering
							options.put("scale", outputScale);// Every
																// coordinate
																// will be
																// multiplied
																// with this, to
																// scale the
																// SVG.
							options.put("simplifytolerance", 1f);//
							options.put("roundcoords", 2f); // 1f means rounded
															// to 1 decimal
															// places, like 7.3
															// ; 3f means
															// rounded to 3
															// places, like
															// 7.356 ; etc.
							options.put("lcpr", 0f);// Straight line control
													// point radius, if this is
													// greater than zero, small
													// circles will be drawn in
													// the SVG. Do not use this
													// for big/complex images.
							options.put("qcpr",0f);// Quadratic spline control
													// point radius, if this is
													// greater than zero, small
													// circles and lines will be
													// drawn in the SVG. Do not
													// use this for big/complex
													// images.
							options.put("desc", 0f); // 1f means true ; 0f means
														// false: SVG
														// descriptions
														// deactivated
							options.put("viewbox", 0f); // 1f means true ; 0f
														// means false: fixed
														// width and height

							// Selective Gauss Blur
							options.put("blurradius", 0f); // 0f means
															// deactivated; 1f
															// .. 5f : blur with
															// this radius
							options.put("blurdelta", 20f); // smaller than this
															// RGB difference
															// will be blurred

							// Palette
							// This is an example of a grayscale palette
							// please note that signed byte values [ -128 .. 127
							// ] will be converted to [ 0 .. 255 ] in the
							// getsvgstring function
							byte[][] palette = new byte[2][4];
							palette[0][0]= 0;
							palette[0][0]= 0;
							palette[0][0]= 0;
							palette[0][0]= (byte) 255;
							palette[1][0]= (byte) 255;
							palette[1][0]= (byte) 255;
							palette[1][0]= (byte) 255;
							palette[1][0]= (byte) 255;
							System.out.println("Begin processing image...");
//							IndexedImage traces = ImageTracer.imagedataToTracedata(ImageTracer.loadImageData(imageName),
//									options, null);
//							
//							DXFDocument dxfdocument = new DXFDocument();
//
//							for (ArrayList<ArrayList<Double[]>> layer : traces.layers) {
//								DXFLayer dxflayer = new DXFLayer();
//								dxfdocument.addDXFLayer(dxflayer);
//								for (ArrayList<Double[]> feature : layer) {
//									DXFPolyline dxfline = new DXFPolyline();
//									dxflayer.addDXFEntity(dxfline);
//									System.out.println("Feature found " + feature.size());
//									for (Double[] point : feature) {
//										DXFVertex vert = new DXFVertex(
//												new Point(point[2] / scale, point[1] / scale, 0));
//										dxfline.addVertex(vert);
//									}
//								}
//							}
//							ProcessingManager pm = new ProcessingManager();
//							pm.process(dxfdocument, new HashMap<Object, Object>(), "svg", new FileOutputStream(finalDir));
							ImageTracer.saveString(finalDir.getAbsolutePath(),
					                ImageTracer.imageToSVG(imageName,options,null)
					              );
							System.out.println("SVG Export Done! "+finalDir.getAbsolutePath());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();

						}

					}
				}).start();
			}
		});

		return baseDirForFiles.getParentFile();
	}

	@Override
	public void start(Stage stage) throws Exception {
		Scene scene = new Scene(snapshotGroup);

		System.out.println("Launching viewer");
		stage.setTitle("Viewed Image");
		stage.setScene(scene);
		stage.sizeToScene();
		stage.show();
		stage.setOnCloseRequest(event -> {
			System.exit(0);
		});
	}

	public static void main(String[] args) {
		new JFXPanel();
		CSG main = new Cube(400, // X dimention
				400, // Y dimention
				400// Z dimention
		).toCSG();// this converts from the geometry to an object we can work
					// with

		CSG cut = new Cube(200, // X dimention
				200, // Y dimention
				200// Z dimention
		).toCSG();// this converts from the geometry to an object we can work
					// with

		export(main.difference(cut).intersect(new Cube(400, 400, 2).toCSG()), new File("export.svg"));

		//
	}
}
