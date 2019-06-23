package eu.mihosoft.vrl.v3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.scene.image.WritableImage;
import java.util.HashMap;
import eu.mihosoft.vrl.v3d.CSG;
import javafx.application.Platform;
//import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.scene.SnapshotParameters;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.scene.image.PixelReader;

public class Slice {
	private static int maxRes = 3000;
	private static class DefaultSliceImp implements ISlice {
		double sizeinPixelSpace = 1500;
		HashMap<WritableImage, PixelReader> readers = new HashMap<>();
		// pixelData=new HashMap<>();
		ArrayList<int[]> usedPixels = new ArrayList<>();
		int minRes = 1000;
		private boolean done;

		Object[] toPixMap(CSG slicePart) {

			// BowlerStudioController.getBowlerStudio()
			// .addObject((Object)slicePart.movez(1),(File)null)
			// BowlerStudioController.getBowlerStudio()
			// .addObject((Object)rawPolygons,(File)null)
			double ratio = slicePart.getTotalY() / slicePart.getTotalX();
			boolean ratioOrentation = slicePart.getTotalX() > slicePart.getTotalY();
			if (ratioOrentation)
				ratio = slicePart.getTotalX() / slicePart.getTotalY();
			ratio = 1 / ratio;
			;
			double mySize = slicePart.getTotalX() > slicePart.getTotalY() ? slicePart.getTotalX()
					: slicePart.getTotalY();
			List<Polygon> polys = slicePart.getPolygons();
			double size = sizeinPixelSpace * (mySize / 200) * (polys.size() / 300);
			if (size < minRes)
				size = minRes;
			if (size > getMaxRes())
				size = getMaxRes();
			// println "Vectorizing "+polys.size()+" polygons at pixel resolution: "+size

			double xPix = size * (ratioOrentation ? 1.0 : ratio);
			double yPix = size * (!ratioOrentation ? 1.0 : ratio);
			double xOffset = slicePart.getMinX();
			double yOffset = slicePart.getMinY();
			double scaleX = slicePart.getTotalX() / xPix;
			double scaleY = slicePart.getTotalY() / yPix;

			// println "New Slicer Image x=" +xPix+" by y="+yPix+" at x="+xOffset+"
			// y="+yOffset

			double imageOffset = 180.0;
			double imageOffsetMotion = imageOffset * scaleX / 2;
			int imgx = (int) (xPix + imageOffset);
			int imgy = (int) (yPix + imageOffset);
			WritableImage obj_img = new WritableImage(imgx, imgy);
			// int snWidth = (int) 4096;
			// int snHeight = (int) 4096;

			MeshView sliceMesh = slicePart.getMesh();
			sliceMesh.getTransforms()
					.add(javafx.scene.transform.Transform.translate(imageOffsetMotion, imageOffsetMotion));
			AnchorPane anchor = new AnchorPane(sliceMesh);
			AnchorPane.setBottomAnchor(sliceMesh, (double) 0);
			AnchorPane.setTopAnchor(sliceMesh, (double) 0);
			AnchorPane.setLeftAnchor(sliceMesh, (double) 0);
			AnchorPane.setRightAnchor(sliceMesh, (double) 0);
			Pane snapshotGroup = new Pane(anchor);
			snapshotGroup.prefHeight((double) (yPix + imageOffset));
			snapshotGroup.prefWidth((double) (xPix + imageOffset));
			snapshotGroup
					.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

			SnapshotParameters snapshotParameters = new SnapshotParameters();
			snapshotParameters.setTransform(new Scale(1 / scaleX, 1 / scaleY));
			snapshotParameters.setDepthBuffer(true);
			snapshotParameters.setFill(Color.TRANSPARENT);
			done = false;
			Runnable r = new Runnable() {
				@Override
				public void run() {
					snapshotGroup.snapshot(snapshotParameters, obj_img);
					done = true;
				}
			};
			Platform.runLater(r);
			while (done == false) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// println "Find boundries "

			return new Object[] { obj_img, scaleX, xOffset - imageOffsetMotion, scaleY, yOffset - imageOffsetMotion,
					imageOffsetMotion, imageOffset };
		}
		int[] toPixels(double absX, double absY, double xOff, double yOff, double scaleX, double scaleY) {
			return new int[] { (int) ((absX - xOff) / scaleX), (int) ((absY - yOff) / scaleY) };
		}

		boolean pixelBlack(double absX, double absY, WritableImage obj_img) {
			if (readers.get(obj_img) == null) {
				readers.put(obj_img, obj_img.getPixelReader());
			}
			PixelReader pixelReader = readers.get(obj_img);
			return pixelReader.getColor((int) absX, (int) absY).getOpacity() != 0;
		}

		boolean pixelEdge(double absX, double absY, WritableImage obj_img) {
			for (int i = -1; i < 2; i++) {
				int x = (int) (absX + i);
				for (int j = -1; j < 2; j++) {
					int y = (int) (absY + j);
					try {
						if (!pixelBlack(x, y, obj_img)) {
							return true;
						}
					} catch (Throwable t) {
						// BowlerStudio.printStackTrace(t);
					}
				}
			}
			return false;
		}

		/**
		 * An interface for slicking CSG objects into lists of points that can be
		 * extruded back out
		 * 
		 * @param incoming
		 *            Incoming CSG to be sliced
		 * @param slicePlane
		 *            Z coordinate of incoming CSG to slice at
		 * @param normalInsetDistance
		 *            Inset for sliced output
		 * @return A set of polygons ining the sliced shape
		 */
		public List<Polygon> slice(CSG incoming, Transform slicePlane, double normalInsetDistance) {
			if (Thread.interrupted()) {
				return null;
			}
			long startTime = System.currentTimeMillis();
			// if(display)BowlerStudioController.getBowlerStudio().getJfx3dmanager().clearUserNode()
			List<Polygon> rawPolygons = new ArrayList<>();
			CSG finalPart = incoming.transformed(slicePlane.inverse()
					).toolOffset(normalInsetDistance);
			// Actual slice plane
			CSG planeCSG = finalPart.getBoundingBox().toZMin();
			planeCSG = planeCSG
					.intersect(planeCSG
							.toZMax()
							.movez(0.00001)
							);
			// Loop over each polygon in the slice of the incoming CSG
			// Add the polygon to the final slice if it lies entirely in the z plane
			// println "Preparing CSG slice"
			CSG slicePart = finalPart
					
					.intersect(planeCSG);
			for (Polygon p : slicePart.getPolygons()) {
				if (Slice.isPolygonAtZero(p)) {
					rawPolygons.add(p);
				}
			}

			Object[] parts = toPixMap(slicePart);
			WritableImage obj_img = (WritableImage) parts[0];
			double scaleX = (double) parts[1];
			double xOffset = (double) parts[2];
			double scaleY = (double) parts[3];
			double yOffset = (double) parts[4];

			ArrayList<Vector3d> points = new ArrayList<>();
			for (Polygon p : rawPolygons) {
				for (Vertex v : p.vertices) {
					points.add(v.pos);
				}
			}

			ArrayList<Polygon> polys = new ArrayList<>();
			ArrayList<int[]> pixelVersionOfPoints = new ArrayList<>();
			for (Vector3d it : points) {
				int[] pix = toPixels(it.x, it.y, xOffset, yOffset, scaleX, scaleY);
				if (pixelEdge(pix[0], pix[1], obj_img)) {
					pixelVersionOfPoints.add(pix);
				}
			}

			ArrayList<int[]> pixelVersionOfPointsFiltered = new ArrayList<>();
			for (int[] d : pixelVersionOfPoints) {
				boolean testIt = false;
				for (int[] x : pixelVersionOfPointsFiltered) {
					if (withinAPix(x, d)) {
						testIt = true;
					}

				}
				if (!testIt) {
					pixelVersionOfPointsFiltered.add(d);
				}
			}
			pixelVersionOfPoints = pixelVersionOfPointsFiltered;
			// if(display)showPoints(pixelVersionOfPoints)
			int[] pixStart = pixelVersionOfPoints.get(0);
			pixelVersionOfPoints.remove(0);
			int[] nextPoint = pixStart;
			ArrayList<int[]> listOfPointsForThisPoly = new ArrayList<>();
			listOfPointsForThisPoly.add(pixStart);

			// if(display)showPoints([nextPoint],20,javafx.scene.paint.Color.ORANGE)
			int lastSearchIndex = 0;
			while ((pixelVersionOfPoints.size() > 0 || listOfPointsForThisPoly.size() > 0) && !Thread.interrupted()) {

				Object[] results = searchNext(nextPoint, obj_img, lastSearchIndex);
				// println "Searching "+results
				if (results == null) {
					listOfPointsForThisPoly.clear();
					if (pixelVersionOfPoints.size() > 0) {
						pixStart = pixelVersionOfPoints.remove(0);
						nextPoint = pixStart;
						listOfPointsForThisPoly.clear();
						listOfPointsForThisPoly.add(nextPoint);
						// if(display)showPoints([nextPoint],40,javafx.scene.paint.Color.BLACK)
					} else
						break;
					continue;
				}
				nextPoint = (int[]) results[0];
				lastSearchIndex = (int) results[1];
				// if(display)showPoints([nextPoint],2,javafx.scene.paint.Color.YELLOW)
				// Thread.sleep(10)
				ArrayList<int[]> toRemove = new ArrayList<>();
				for (int[] it : pixelVersionOfPoints) {
					if (withinAPix(nextPoint, it)) {
						toRemove.add(it);
					}
				}

				if (toRemove.size() > 0) {
					// println "Found "+toRemove
					for (int[] d : toRemove) {
						// if(display)showPoints([d],30,javafx.scene.paint.Color.GREEN)
						pixelVersionOfPoints.remove(d);
						listOfPointsForThisPoly.add(d);
					}

				} else {
					if (listOfPointsForThisPoly.size() > 2) {
						if (withinAPix(nextPoint, pixStart)) {
							// if(display)println "Closed Polygon Found!"
							// Thread.sleep(1000)
							List<Vector3d> p = new ArrayList<>();
							for (int[] it : listOfPointsForThisPoly) {
								p.add(new Vector3d((it[0] * scaleX) + xOffset, (it[1] * scaleY) + yOffset, 0));
							}

							Polygon polyNew = Polygon.fromPoints(p);
							polys.add(polyNew);
							listOfPointsForThisPoly.clear();
							if (pixelVersionOfPoints.size() > 0) {
								pixStart = pixelVersionOfPoints.remove(0);
								nextPoint = pixStart;
								listOfPointsForThisPoly.add(nextPoint);
							}
							// if(display)showPoints([nextPoint],20,javafx.scene.paint.Color.ORANGE)
						}
					}
				}

			}
			if (listOfPointsForThisPoly.size() > 0) {
				// println "Spare Polygon Found!"
				// Thread.sleep(1000)
				List<Vector3d> p = new ArrayList<>();
				for (int[] it : listOfPointsForThisPoly) {
					p.add(new Vector3d((it[0] * scaleX) + xOffset, (it[1] * scaleY) + yOffset, 0));
				}
				polys.add(Polygon.fromPoints(p));
				// if(display)BowlerStudioController.getBowlerStudio() .addObject(polys, new
				// File("."))
			}

			readers.clear();
			// pixelData.clear();
			usedPixels.clear();
			// if(display)BowlerStudioController.getBowlerStudio().getJfx3dmanager().clearUserNode()
			// BowlerStudioController.getBowlerStudio() .addObject(polys, new File("."));
			System.out.println(
					"Slice took: " + (((double) (System.currentTimeMillis() - startTime)) / 1000.0) + " seconds");
			return polys;
		}

		Object[] searchNext(int[] pixStart, WritableImage obj_img, int lastSearchIndex) {

			double index = 1;
			Object[] ret = searchNextDepth(pixStart, obj_img, index, lastSearchIndex);

			while (ret == null && index < 10 && !Thread.interrupted()) {
				index += 0.5;
				ret = searchNextDepth(pixStart, obj_img, index, 0);
			}
			return ret;

		}

		Object[] searchNextDepth(int[] pixStart, WritableImage obj_img, double searchSize, int lastSearchIndex) {
			ArrayList<int[]> locations = new ArrayList<>();
			double inc = Math.toDegrees(Math.atan2(1, searchSize));
			if (searchSize > 2) {
				for (double i = 0; i < 360 + inc; i += inc) {
					int x = (int) Math.round(Math.cos(Math.toRadians(i)) * searchSize);
					int y = (int) Math.round(Math.sin(Math.toRadians(i)) * searchSize);
					locations.add(new int[] { pixStart[0] + x, pixStart[1] + y });
				}
			} else {

				// arrange the pixels in the data array based on a CCW search
				for (int i = (int) -searchSize; i < searchSize + 1; i++) {
					locations.add(new int[] { (int) (pixStart[0] + searchSize), pixStart[1] + i });
				}
				// after the firat loop, leave off the first index to avoid duplicates
				for (int i = (int) (searchSize - 1); i > -searchSize - 1; i--) {
					locations.add(new int[] { pixStart[0] + i, (int) (pixStart[1] + searchSize) });
				}
				for (int i = (int) (searchSize - 1); i > -searchSize - 1; i--) {
					locations.add(new int[] { (int) (pixStart[0] - searchSize), pixStart[1] + i });
				}
				for (int i = (int) (-searchSize + 1); i < searchSize + 1; i++) {
					locations.add(new int[] { pixStart[0] + i, (int) (pixStart[1] - searchSize) });
				}

			}
			// println inc+" "+locations
			// if(searchSize>2)println "\t\t "+searchSize
			int searchArraySize = locations.size();
			if (lastSearchIndex >= searchArraySize) {
				lastSearchIndex = 0;
			}
			int end = lastSearchIndex - 1;
			if (end < 0)
				end = searchArraySize - 1;
			// rotate throught he data looking for CCW edge
			for (int i = lastSearchIndex; i != end
					&& !Thread.interrupted(); i = (i + 1 >= searchArraySize ? 0 : i + 1)) {
				// println "\t\t "+i+" start = " +lastSearchIndex+" end = "+end+" array size =
				// "+searchArraySize
				int counterCW = i - 1;
				if (counterCW < 0)
					counterCW = searchArraySize - 1;
				int[] ccw = locations.get(counterCW);
				int[] self = locations.get(i);
				boolean w = !pixelBlack(self[0], self[1], obj_img);
				boolean b = pixelBlack(ccw[0], ccw[1], obj_img);

				boolean useMe = true;
				for (int[] it : usedPixels) {
					if (it[0] == self[0] && it[1] == self[1]) {
						useMe = false;
						break;
					}
				}
				if (w && b && useMe) {
					usedPixels.add(self);
					// edge detected doing a ccw rotation search
					return new Object[] { self, i };
				} else {
					// if(display)showPoints([self],1,javafx.scene.paint.Color.WHITE) ;
				}
			}
			return null;
			/*
			 * //println "From "+pixStart x= pixStart[0] y=pixStart[1] ul =
			 * pixelBlack(x+1,y+1,obj_img) uc = pixelBlack(x+1,y,obj_img) ur =
			 * pixelBlack(x+1,y-1,obj_img) l= pixelBlack(x,y+1,obj_img) r=
			 * pixelBlack(x,y-1,obj_img) bl = pixelBlack(x-1,y+1,obj_img) bc =
			 * pixelBlack(x-1,y,obj_img) br = pixelBlack(x-1,y-1,obj_img) me =
			 * pixelBlack(x,y,obj_img) println
			 * "Ul = "+ul+" uc "+uc+" ur "+ur+" \r\nl "+l+" c "+me+" r "+r+"\r\nbl "+bl+
			 * " bc "+bc+" br "+br
			 */

		}

		boolean withinAPix(int[] incoming, int[] out) {
			int pixSize = 2;
			for (int i = -pixSize; i < pixSize + 1; i++) {
				int x = incoming[0] + i;
				for (int j = -pixSize; j < pixSize + 1; j++) {
					int y = incoming[1] + j;
					if (x == out[0] && y == out[1]) {
						return true;
					}
				}
			}
			return false;
		}
	};

	private static ISlice sliceEngine = new DefaultSliceImp();

	/**
	 * Returns true if this polygon lies entirely in the z plane
	 *
	 * @param polygon
	 *            The polygon to check
	 * @return True if this polygon is entirely in the z plane
	 */
	private static boolean isPolygonAtZero(Polygon polygon) {
		// Return false if there is a vertex in this polygon which is not at
		// zero
		// Else, the polygon is at zero if every vertex in it is at zero
		for (Vertex v : polygon.vertices)
			if (!isVertexAtZero(v))
				return false;

		return true;
	}

	/**
	 * Returns true if this vertex is at z coordinate zero
	 *
	 * @param vertex
	 *            The vertex to check
	 * @return True if this vertex is at z coordinate zero
	 */
	private static boolean isVertexAtZero(Vertex vertex) {
		// The upper and lower bounds for checking the vertex z coordinate
		// against
		final double SLICE_UPPER_BOUND = 0.001, SLICE_LOWER_BOUND = -0.001;

		// The vertex is at zero if it is within tight bounds (to account for
		// floating point error)
		return vertex.getZ() < SLICE_UPPER_BOUND && vertex.getZ() > SLICE_LOWER_BOUND;
	}

	public static List<Polygon> slice(CSG incoming, Transform slicePlane, double normalInsetDistance) {
		try {
			if(DefaultSliceImp.class.isInstance(sliceEngine)) {
				// avoid concurrecy issues
				try {
					return new DefaultSliceImp().slice(incoming, slicePlane, normalInsetDistance);
				}catch(IllegalStateException e) {
					JavaFXInitializer.go();
					
					return new DefaultSliceImp().slice(incoming, slicePlane, normalInsetDistance);
				}
			}
			return getSliceEngine().slice(incoming, slicePlane, normalInsetDistance);
		}catch(Throwable e) {
			return incoming.getPolygons();
		}
	}
	public static List<Polygon> slice(CSG incoming) {
		return slice(incoming, new Transform(),0);
	}
	public static List<Polygon> slice(CSG incoming, double normalInsetDistance) {
		return slice(incoming, new Transform(),normalInsetDistance);
	}
	public static ISlice getSliceEngine() {
		return sliceEngine;
	}

	public static void setSliceEngine(ISlice sliceEngine) {
		Slice.sliceEngine = sliceEngine;
	}

	public static int getMaxRes() {
		return maxRes;
	}
	
	public static void setNumFacesInOffset(int numFacesInOffset) {
		CSG.setNumFacesInOffset( numFacesInOffset);
	}
	public static void setMaxRes(int mr) {
		maxRes = mr;
	}
}
