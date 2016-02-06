package eu.mihosoft.vrl.v3d;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.FileInputStream;
import org.kabeja.dxf.DXFConstants;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFLayer;
import org.kabeja.dxf.DXFLine;
import org.kabeja.dxf.DXFPolyline;
import org.kabeja.dxf.DXFSpline;
import org.kabeja.dxf.DXFVertex;
import org.kabeja.dxf.helpers.Point;
import org.kabeja.dxf.helpers.SplinePoint;
import org.kabeja.parser.DXFParser;
import org.kabeja.parser.Parser;
import org.kabeja.parser.ParserBuilder;

public class DXFExtrude implements Primitive {

	private File source;
	/** The properties. */
	private final PropertyStorage properties = new PropertyStorage();
	private double extrudeDistance;

	public DXFExtrude(File source, double extrudeDistance) {
		this.source = source;
		this.extrudeDistance = extrudeDistance;

	}

	@Override
	public CSG toCSG() {
		Parser parser = ParserBuilder.createDefaultParser();
		ArrayList<Vector3d> points = new ArrayList<Vector3d>();
		try {

			// parse
			parser.parse(new FileInputStream(source), DXFParser.DEFAULT_ENCODING);

			// get the documnet and the layer
			DXFDocument doc = parser.getDocument();
			Iterator layerIterable = doc.getDXFLayerIterator();
			if (layerIterable != null) {
				for (; layerIterable.hasNext();) {
					// iterate over all the layers
					DXFLayer layer = (DXFLayer) layerIterable.next();
					Iterator entityIterator = layer.getDXFEntityTypeIterator();
					if (entityIterator != null) {
						for (; entityIterator.hasNext();) {
							String entityType = (String) entityIterator.next();
							if (entityType.contentEquals(DXFConstants.ENTITY_TYPE_POLYLINE)) {
								// get all polylines from the layer
								List plines = layer.getDXFEntities(entityType);
								if (plines != null) {
									for (Object p : plines) {
										DXFPolyline pline = (DXFPolyline) p;
										for (int i = 0; i < pline.getVertexCount(); i++) {
											DXFVertex vertex = pline.getVertex(i);
											Point point = vertex.getPoint();
											points.add(new Vector3d(point.getX(), point.getY(), point.getZ()));
										}
									}
								}
							}
							else if (entityType.contentEquals(DXFConstants.ENTITY_TYPE_LINE)) {
								// get all polylines from the layer
								List plines = layer.getDXFEntities(entityType);
								if (plines != null) {
									for (Object p : plines) {
										DXFLine pline = (DXFLine) p;
										Point point = pline.getStartPoint();
										points.add(new Vector3d(point.getX(), point.getY(), point.getZ()));
										point = pline.getEndPoint();
										points.add(new Vector3d(point.getX(), point.getY(), point.getZ()));
										
									}
								}
							}
							else if (entityType.contentEquals(DXFConstants.ENTITY_TYPE_SPLINE)) {
								// get all polylines from the layer
								List plines = layer.getDXFEntities(entityType);
								if (plines != null) {
									for (Object p : plines) {
										DXFSpline pline = (DXFSpline) p;
										Iterator splinePointIterator = pline.getSplinePointIterator();
										if(splinePointIterator!=null)
											for (;splinePointIterator.hasNext();) {
												SplinePoint point =(SplinePoint) splinePointIterator.next();
												points.add(new Vector3d(point.getX(), point.getY(), point.getZ()));
											}
									}
								}
							}
							else {
								System.out.println("Found type: " + entityType);

							}
						}
					}
					//return Extrude.points(new Vector3d(0, 0, extrudeDistance), points);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public PropertyStorage getProperties() {
		return properties;
	}

	@Override
	public List<Polygon> toPolygons() {
		throw new RuntimeException("This Primitive uses the extrude to make polygons");
	}

}
