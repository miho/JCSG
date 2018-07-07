package eu.mihosoft.vrl.v3d.svg;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.vecmath.Matrix4d;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.SVGItem;
import org.apache.batik.dom.svg.SVGOMGElement;
import org.apache.batik.dom.svg.SVGOMPathElement;
import org.apache.batik.dom.svg.SVGOMSVGElement;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGPathSegList;
import com.piro.bezier.BezierPath;
import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Edge;
import eu.mihosoft.vrl.v3d.Extrude;
import eu.mihosoft.vrl.v3d.Plane;
import eu.mihosoft.vrl.v3d.Polygon;
import eu.mihosoft.vrl.v3d.Transform;
import eu.mihosoft.vrl.v3d.Vector3d;
import javafx.scene.paint.Color;

// CSG.setDefaultOptType(CSG.OptType.CSG_BOUND);
/**
 * Responsible for converting all SVG path elements into MetaPost curves.
 */
public class SVGLoad {
	private static final String PATH_ELEMENT_NAME = "path";
	private static final String GROUP_ELEMENT_NAME = "g";
	private Document svgDocument;
	boolean hp = true;
	private ArrayList<CSG> sections = null;
	private ArrayList<CSG> holes = null;

	private List<Polygon> polygons = null;
	private ISVGLoadProgress progress = null;
	private double thickness;
	private boolean negativeThickness = false;
	private double height = 0;
	private double width = 0;

	private static ISVGLoadProgress progressDefault = new ISVGLoadProgress() {

		@Override
		public void onShape(CSG newShape) {
			// TODO Auto-generated method stub

		}
	};

	public void setHolePolarity(boolean p) {
		hp = p;
	}

	/**
	 * Responsible for converting an SVG path element to MetaPost. This will convert
	 * just the bezier curve portion of the path element, not its style. Typically
	 * the SVG path data is provided from the "d" attribute of an SVG path node.
	 */
	class MetaPostPath2 {
		private SVGOMPathElement pathElement;
		private String transform;

		/**
		 * Use to create an instance of a class that can parse an SVG path element to
		 * produce MetaPost code.
		 *
		 * @param pathNode
		 *            The path node containing a "d" attribute (output as MetaPost
		 *            code).
		 */
		public MetaPostPath2(Node pathNode) {
			setPathNode(pathNode);
		}

		/**
		 * Converts this object's SVG path to a MetaPost draw statement.
		 * 
		 * @return A string that represents the MetaPost code for a path element.
		 */
		public String toCode() {
			String sb = "";
			SVGOMPathElement pathElement = getPathElement();
			SVGPathSegList pathList = pathElement.getNormalizedPathSegList();
			// String offset = pathElement.getOwnerSVGElement();

			int pathObjects = pathList.getNumberOfItems();
			/*
			 * sb.append( "M "+offset .replaceAll("translate", "") .replaceAll("(", "")
			 * .replaceAll(")", "") +"\n");
			 */
			// sb.append( "//"+getId()+"\n");

			for (int i = 0; i < pathObjects; i++) {
				SVGItem item = (SVGItem) pathList.getItem(i);
				String itemLine = String.format("%s%n", item.getValueAsString());
				sb += itemLine;
			}

			return sb.toString();
		}

		/**
		 * Typecasts the given pathNode to an SVGOMPathElement for later analysis.
		 * 
		 * @param pathNode
		 *            The path element that contains curves, lines, and other SVG
		 *            instructions.
		 */
		private void setPathNode(Node pathNode) {
			this.pathElement = (SVGOMPathElement) pathNode;
		}

		/**
		 * Returns an SVG document element that contains path instructions (usually for
		 * drawing on a canvas).
		 * 
		 * @return An object that contains a list of items representing pen movements.
		 */
		private SVGOMPathElement getPathElement() {
			return this.pathElement;
		}
	}

	/**
	 * Creates an SVG Document given a URI.
	 *
	 * @param uri
	 *            Path to the file.
	 * @throws Exception
	 *             Something went wrong parsing the SVG file.
	 */
	public SVGLoad(URI uri) throws IOException {
		setSVGDocument(createSVGDocument(uri));
	}

	/**
	 * Creates an SVG Document given a URI.
	 *
	 * @param uri
	 *            Path to the file.
	 * @throws Exception
	 *             Something went wrong parsing the SVG file.
	 */
	public SVGLoad(File f) throws IOException {
		setSVGDocument(createSVGDocument(f.toURI()));
	}

	/**
	 * Creates an SVG Document String of SVG data.
	 *
	 * @param data
	 *            Contents of an svg file
	 * @throws Exception
	 *             Something went wrong parsing the SVG file.
	 */
	public SVGLoad(String data) throws IOException {
		File tmpsvg = new File(System.getProperty("java.io.tmpdir") + "/" + Math.random());
		tmpsvg.createNewFile();
		FileWriter fw = new FileWriter(tmpsvg.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(data);
		bw.close();
		setSVGDocument(createSVGDocument(tmpsvg.toURI()));
		tmpsvg.deleteOnExit();
	}

	public ArrayList<CSG> extrude(double thickness) throws IOException {

		return extrude(thickness, 0.005);

	}

	public static ArrayList<CSG> extrude(File f, double thickness) throws IOException {
		return new SVGLoad(f.toURI()).extrude(thickness);

	}

	/**
	 * This function will create a list of polygons that can be exported back to an
	 * SVG
	 * 
	 * @param f
	 *            the file containing the SVG data
	 * @return
	 * @throws IOException
	 */
	public static List<Polygon> toPolygons(File f) throws IOException {
		return new SVGLoad(f.toURI()).toPolygons();

	}

	public List<Polygon> toPolygons(double resolution) {
		if (polygons == null) {
			try {
				loadAllGroups(resolution, new Transform());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return polygons;
	}

	public List<Polygon> toPolygons() {
		return toPolygons(0.001);
	}

	public static ArrayList<CSG> extrude(File f, double thickness, double resolution) throws IOException {
		return new SVGLoad(f.toURI()).extrude(thickness, resolution);
	}

	public static ArrayList<CSG> extrude(URI uri, double thickness) throws IOException {

		return new SVGLoad(uri).extrude(thickness);

	}

	public static ArrayList<CSG> extrude(URI uri, double thickness, double resolution) throws IOException {
		return new SVGLoad(uri).extrude(thickness, resolution);
	}

	private void loadAllGroups(double resolution, Transform startingFrame) {

		NodeList pn = getSVGDocument().getDocumentElement().getChildNodes();// .getElementsByTagName("g");
		try {
			height = Double.parseDouble(getSVGDocument().getDocumentElement().getAttribute("height").split("mm")[0]);
			width = Double.parseDouble(getSVGDocument().getDocumentElement().getAttribute("width").split("mm")[0]);
		} catch (Throwable t) {
			t.printStackTrace();
			height = 0;
			width = 0;
		}
		// println "Loading groups from "+pn.getClass()
		int pnCount = pn.getLength();
		for (int j = 0; j < pnCount; j++) {
			if (SVGOMGElement.class.isInstance(pn.item(j))) {
				SVGOMGElement element = (SVGOMGElement) pn.item(j);
				loadGroup(element, resolution, startingFrame);
			}
			// else
			// println "UNKNOWN ELEMENT "+pn.item(j).getClass()
		}

	}

	private void loadGroup(SVGOMGElement element, double resolution, Transform startingFrame) {
		Node transforms = element.getAttributes().getNamedItem("transform");
		Transform newFrame = getNewframe(startingFrame, transforms);
		System.out.println("Group " + element.getAttribute("id") + " root " + newFrame.getX() + " " + newFrame.getY());
		NodeList children = element.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);
			if (SVGOMGElement.class.isInstance(n)) {
				loadGroup((SVGOMGElement) n, resolution, newFrame);
			} else {
				// System.out.println("\tNot group:
				// "+n.getAttributes().getNamedItem("id").getNodeValue());
				try {
					loadPath(n, resolution, newFrame);
				} catch (Throwable t) {

					t.printStackTrace();
				}
			}
		}
	}

	private Transform getNewframe(Transform startingFrame, Node transforms) {
		if (transforms == null)
			return startingFrame;
		Transform newFrame = new Transform().apply(startingFrame);
		String transformValue = transforms.getNodeValue();
		System.out.println("\tApply " + transformValue + " root " + startingFrame.getX() + " " + startingFrame.getY());
		if (transformValue.contains("translate")) {
			String[] transformValues = transformValue.replaceAll("translate", "").replaceAll("\\(", "")
					.replaceAll("\\)", "").split("\\,");
			// System.out.println(id.getNodeValue() + " " + transformValues);
			// mvx +=Double.parseDouble(transformValues[0]);
			// mvy += Double.parseDouble(transformValues[1]);
			// newFrame.getInternalMatrix().m03=Double.parseDouble(transformValues[0]);
			// newFrame.getInternalMatrix().m13=Double.parseDouble(transformValues[1]);
			newFrame.apply(new Transform().translate(Double.parseDouble(transformValues[0]),
					Double.parseDouble(transformValues[1]), 0));

		} else if (transformValue.contains("scale")) {
			String[] transformValues = transformValue.replaceAll("scale", "").replaceAll("\\(", "")
					.replaceAll("\\)", "").split("\\,");
			// System.out.println(id.getNodeValue() + " " + transformValues);
			double scalex = Double.parseDouble(transformValues[0]);
			double scaley = Double.parseDouble(transformValues[1]);
			newFrame.scale(scalex, scaley, 1);

		} else if (transformValue.contains("matrix")) {
			String[] transformValues = transformValue.replaceAll("matrix", "").replaceAll("\\(", "")
					.replaceAll("\\)", "").split("\\,");
			// System.out.println("Matrix found " +new
			// ArrayList<>(Arrays.asList(transformValues)));
			double a = Double.parseDouble(transformValues[0]);
			double b = Double.parseDouble(transformValues[1]);
			double c = Double.parseDouble(transformValues[2]);
			double d = Double.parseDouble(transformValues[3]);
			double e = Double.parseDouble(transformValues[4]);
			double f = Double.parseDouble(transformValues[5]);
			double elemenents[] = { a, c, 0, e, b, d, 0, f, 0, 0, 1, 0, 0, 0, 0, 1 };
			newFrame.apply(new Transform(new Matrix4d(elemenents)));

		}
		return newFrame;
	}

	// SVGOMGElement
	private void loadPath(Node pathNode, double resolution, Transform startingFrame) {
		Transform newFrame;
		// NodeList pathNodes = element.getElementsByTagName("path");
		// Node transforms = element.getAttributes().getNamedItem("transform");
		if (pathNode != null) {
			// System.out.println("\tPath
			// "+pathNode.getAttributes().getNamedItem("id").getNodeValue());
			if (pathNode.getAttributes() != null) {
				Node transforms = pathNode.getAttributes().getNamedItem("transform");
				newFrame = getNewframe(startingFrame, transforms);
				MetaPostPath2 mpp = new MetaPostPath2(pathNode);
				String code = mpp.toCode();

				loadComposite(code, resolution, newFrame);
			} else {
				newFrame = startingFrame;
			}

		}

	}

	private void loadComposite(String code, double resolution, Transform startingFrame) {
		// Count the occourences of M
		int count = code.length() - code.replace("M", "").length();
		if (count < 2) {
			// println "Single path found"

			// setHolePolarity(true);
			try {
				loadSingle(code, resolution, startingFrame);
			} catch (Exception ex) {
				// BowlerStudio.printStackTrace(ex);
			}
		} else {

			// setHolePolarity(false);
			String[] pathParts = code.split("M");
			// println "Complex path found "+ pathParts.length ;
			for (int i = 0; i < pathParts.length; i++) {
				String sectionedPart = "M" + pathParts[i];
				// reapply the split char
				if (sectionedPart.length() > 1) {

					// println "Seperated complex: "
					loadSingle(sectionedPart, resolution, startingFrame);
				}
			}
		}
		// System.out.println("SVG has this many elements loaded: "+sections.size());
		// BowlerStudioController.setCsg(sections,null);
	}
	public static boolean isCCW(Polygon polygon) {
		double runningTotal=0;
		List<Edge> edges = Edge.fromPolygon(polygon);
		for(Edge e:edges) {
			//runningTotal+=((e.getP1().pos.x-e.getP2().pos.x)*(e.getP1().pos.y-e.getP2().pos.y));
			runningTotal+=e.getP1().pos.x*e.getP2().pos.y;
			runningTotal-=e.getP2().pos.x*e.getP1().pos.y;
		}
		
		return runningTotal<0;
	}
	private void loadSingle(String code, double resolution, Transform startingFrame) {
		// println code
		BezierPath path = new BezierPath();
		path.parsePathString(code);

		ArrayList<Vector3d> p = path.evaluate();
		for (Vector3d point : p) {
			point.transform(startingFrame);
			point.transform(new Transform().scale((1.0 / SVGExporter.Scale)));
			point.transform(new Transform().translate(0, -height, 0));
			 point.transform(new Transform().rotZ(-180));
			point.transform(new Transform().rotY(180));
		}

		// System.out.println(" Path " + code);
		Polygon poly = Polygon.fromPoints(p);
		if (polygons == null)
			polygons = new ArrayList<Polygon>();

		boolean hole = isCCW(poly);
		poly = Polygon.fromPoints(Extrude.toCCW(poly.getPoints()));

		polygons.add(poly);
		if (!hp)
			hole = !hole;
		CSG newbit;

		newbit = Extrude.getExtrusionEngine().extrude(new Vector3d(0, 0, thickness), poly);//.rotz(180);
		// to
		// mm

		if (negativeThickness) {
			newbit = newbit.toZMax();
		}
		// scale

		try {

			if (!hole) {
				// println "NOT hole"
				getSections().add(newbit);
			} else {
				// println "Hole"
				
				//getHoles().add(newbit);
				newbit.setColor(Color.RED);
				sections.add(newbit);
			}

			//
			//
		} catch (Exception ex) {
			// System.out.println(" Path "+code );
			// BowlerStudio.printStackTrace(ex);
			//
		}
		if (progress != null) {
			progress.onShape(newbit);
		} else {
			progressDefault.onShape(newbit);
		}
	}

	public ArrayList<CSG> extrude(double t, double resolution) throws IOException {
		this.thickness = t;

		if (thickness < 0) {
			thickness = -thickness;
			negativeThickness = true;
		} else {
			negativeThickness = false;
		}
		/**
		 * Reads a file and parses the path elements.
		 * 
		 * @param args
		 *            args[0] - Filename to parse.
		 * @throws IOException
		 *             Error reading the SVG file.
		 */

		// SVGLoad converter = new eu.mihosoft.vrl.v3d.svg.SVGLoad(uri.toString());
		// println "Loading converter"

		if (getSections() == null)
			setSections(new ArrayList<CSG>());
		if (getHoles() == null)
			setHoles(new ArrayList<CSG>());
		getSections().clear();
		getHoles().clear();

		loadAllGroups(resolution, new Transform());
		loadExtrusionSectoins();

		return getSections();
	}

	private void loadExtrusionSectoins() {
		// sections.addAll(holes);
		// return;

		if (getSections().size() == 0 && getHoles().size() != 0) {
			getSections().addAll(getHoles());
			getHoles().clear();
		}
		ArrayList<CSG> notHoles = new ArrayList<>();
		for (CSG c : holes) {
			boolean touchesSomething = false;
			for (CSG p : sections) {
				if (p.touching(c)) {
					touchesSomething = true;
					break;
				}
			}
			if (!touchesSomething) {
				notHoles.add(c);
			}
		}
		for (CSG c : notHoles) {
			holes.remove(c);
			sections.add(c);
		}
		if (getSections().size() == 0)
			return;
		double ymax = getSections().get(0).getMaxY();
		for (CSG c : getSections()) {
			if (c.getMaxY() > ymax) {
				ymax = c.getMaxY();
			}
		}
		for (int i = 0; i < getSections().size(); i++) {
			CSG tmp = getSections().get(i);
			// boolean touching = false;
			for (int j = 0; j < getHoles().size(); j++) {
				CSG c = getHoles().get(j);
				if (tmp.touching(c) && tmp.getPolygons().size() > 0) {
					CSG intermTmp = tmp.difference(c);
					if (intermTmp.getPolygons().size() > 0) {
						tmp = intermTmp; // getHoles().remove(h);
					} else {
						// only apply holes that dont obliterate the part
						// getSections().add(c);
						// getHoles().remove(c);
						// j--;
					}
				}
			}

			// tmp = tmp.rotx(180).toZMin().movey(height);
			
			getSections().set(i, tmp);
		}

	}

	/**
	 * This will set the document to parse. This method also initializes the SVG DOM
	 * enhancements, which are necessary to perform SVG and CSS manipulations. The
	 * initialization is also required to extract information from the SVG path
	 * elements.
	 *
	 * @param document
	 *            The document that contains SVG content.
	 */
	public void setSVGDocument(Document document) {
		initSVGDOM(document);
		this.svgDocument = document;
	}

	/**
	 * Returns the SVG document parsed upon instantiating this class.
	 * 
	 * @return A valid, parsed, non-null SVG document instance.
	 */
	public Document getSVGDocument() {
		return this.svgDocument;
	}

	/**
	 * Enhance the SVG DOM for the given document to provide CSS- and SVG-specific
	 * DOM interfaces.
	 * 
	 * @param document
	 *            The document to enhance.
	 * @link http://wiki.apache.org/xmlgraphics-batik/BootSvgAndCssDom
	 */
	private void initSVGDOM(Document document) {
		UserAgent userAgent = new UserAgentAdapter();
		DocumentLoader loader = new DocumentLoader(userAgent);
		BridgeContext bridgeContext = new BridgeContext(userAgent, loader);
		bridgeContext.setDynamicState(BridgeContext.DYNAMIC);

		// Enable CSS- and SVG-specific enhancements.
		(new GVTBuilder()).build(bridgeContext, document);
	}

	/**
	 * Use the SAXSVGDocumentFactory to parse the given URI into a DOM.
	 * 
	 * @param uri
	 *            The path to the SVG file to read.
	 * @return A Document instance that represents the SVG file.
	 * @throws Exception
	 *             The file could not be read.
	 */
	private Document createSVGDocument(URI uri) throws IOException {
		String parser = XMLResourceDescriptor.getXMLParserClassName();
		SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
		return factory.createDocument(uri.toString());
	}

	public ISVGLoadProgress getProgress() {
		return progress;
	}

	public void setProgress(ISVGLoadProgress progress) {
		this.progress = progress;
	}

	public static ISVGLoadProgress getProgressDefault() {
		return progressDefault;
	}

	public static void setProgressDefault(ISVGLoadProgress progressDefault) {
		SVGLoad.progressDefault = progressDefault;
	}

	public ArrayList<CSG> getHoles() {
		return holes;
	}

	public void setHoles(ArrayList<CSG> holes) {
		this.holes = holes;
	}

	public ArrayList<CSG> getSections() {
		return sections;
	}

	public void setSections(ArrayList<CSG> sections) {
		this.sections = sections;
	}
}
