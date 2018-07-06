package eu.mihosoft.vrl.v3d.svg;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
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
import eu.mihosoft.vrl.v3d.Extrude;
import eu.mihosoft.vrl.v3d.Polygon;
import eu.mihosoft.vrl.v3d.Transform;
import eu.mihosoft.vrl.v3d.Vector3d;

// CSG.setDefaultOptType(CSG.OptType.CSG_BOUND);
/**
 * Responsible for converting all SVG path elements into MetaPost curves.
 */
public class SVGLoad {
  private static final String PATH_ELEMENT_NAME = "path";
  private static final String GROUP_ELEMENT_NAME = "g";
  private Document svgDocument;
  boolean holePolarity = true;
  private ArrayList<CSG> sections = null;
  private ArrayList<CSG> holes = null;

  private List<Polygon> polygons = null;

  public void setHolePolarity(boolean p) {
    holePolarity = p;
  }

  /**
   * Responsible for converting an SVG path element to MetaPost. This will convert just the bezier
   * curve portion of the path element, not its style. Typically the SVG path data is provided from
   * the "d" attribute of an SVG path node.
   */
  class MetaPostPath2 {
    private SVGOMPathElement pathElement;
    private String transform;

    /**
     * Use to create an instance of a class that can parse an SVG path element to produce MetaPost
     * code.
     *
     * @param pathNode The path node containing a "d" attribute (output as MetaPost code).
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
       * sb.append( "M "+offset .replaceAll("translate", "") .replaceAll("(", "") .replaceAll(")",
       * "") +"\n");
       */
      // sb.append( "//"+getId()+"\n");

      for (int i = 0; i < pathObjects; i++) {
        SVGItem item = (SVGItem) pathList.getItem(i);
        String itemLine = String.format("%s%n", item.getValueAsString());
        sb+=itemLine;
      }

      return sb.toString();
    }

    /**
     * Typecasts the given pathNode to an SVGOMPathElement for later analysis.
     * 
     * @param pathNode The path element that contains curves, lines, and other SVG instructions.
     */
    private void setPathNode(Node pathNode) {
      this.pathElement = (SVGOMPathElement) pathNode;
    }

    /**
     * Returns an SVG document element that contains path instructions (usually for drawing on a
     * canvas).
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
   * @param uri Path to the file.
   * @throws Exception Something went wrong parsing the SVG file.
   */
  public SVGLoad(URI uri) throws IOException {
    setSVGDocument(createSVGDocument(uri));
  }

  /**
   * Creates an SVG Document given a URI.
   *
   * @param uri Path to the file.
   * @throws Exception Something went wrong parsing the SVG file.
   */
  public SVGLoad(File f) throws IOException {
    setSVGDocument(createSVGDocument(f.toURI()));
  }

  /**
   * Creates an SVG Document String of SVG data.
   *
   * @param data Contents of an svg file
   * @throws Exception Something went wrong parsing the SVG file.
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
   * This function will create a list of polygons that can be exported back to an SVG
   * 
   * @param f the file containing the SVG data
   * @return
   * @throws IOException
   */
  public static List<Polygon> toPolygons(File f) throws IOException {
    return new SVGLoad(f.toURI()).toPolygons();

  }
  public List<Polygon> toPolygons(double resolution) {
    if (polygons == null) {
      try {
        loadAllGroups(resolution, 0, 0);
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return polygons;
  }
  public List<Polygon> toPolygons() {
    return  toPolygons(0.001);
  }



  public static ArrayList<CSG> extrude(File f, double thickness, double resolution)
      throws IOException {
    return new SVGLoad(f.toURI()).extrude(thickness, resolution);
  }

  public static ArrayList<CSG> extrude(URI uri, double thickness) throws IOException {

    return new SVGLoad(uri).extrude(thickness);

  }

  public static ArrayList<CSG> extrude(URI uri, double thickness, double resolution)
      throws IOException {
    return new SVGLoad(uri).extrude(thickness, resolution);
  }


  private void loadAllGroups(double resolution, double mvx, double mvy) {

    NodeList pn = getSVGDocument().getDocumentElement().getElementsByTagName("g");
    // println "Loading groups from "+pn.getClass()
    int pnCount = pn.getLength();
    for (int j = 0; j < pnCount; j++) {
      if (SVGOMGElement.class.isInstance(pn.item(j))) {
        SVGOMGElement element = (SVGOMGElement) pn.item(j);
        NodeList pathNodes = element.getElementsByTagName("path");
        Node transforms = element.getAttributes().getNamedItem("transform");
        Node id = element.getAttributes().getNamedItem("id");
        NodeList subgroups = element.getElementsByTagName("g");
        // System.out.println(pathNodes.getClass());
        String transformValue = "translate(0,0)";
        if (transforms != null) {
          transformValue = transforms.getNodeValue();
        }
        if (transformValue.contains("matrix")) {
          transformValue = "translate(0,0)";
          // println "Replacing matrix"
        }

        String[] transformValues = transformValue.replaceAll("translate", "").replaceAll("\\(", "")
            .replaceAll("\\)", "").split("\\,");
        //System.out.println(id.getNodeValue() + " " + transformValues);
        mvx = Double.parseDouble(transformValues[0]);
        mvy = Double.parseDouble(transformValues[1]);

        if (subgroups != null) {
          if (subgroups.getLength() < 1) {
            // loadAllGroups(subgroups, sections, thickness, resolution, mvx, mvy);
            // break;
            // println "NO SubGroups"

            loadAllPaths((SVGOMGElement) pn.item(j), resolution, mvx, mvy);

          } else {
            // println "Has "+subgroups.getLength()+" SubGroups"
          }
        }
        if (pathNodes != null) {
          loadAllPaths(element, resolution, mvx, mvy);
        }



      }
      // else
      // println "UNKNOWN ELEMENT "+pn.item(j).getClass()
    }

  }

  // SVGOMGElement
  private void loadAllPaths(SVGOMGElement element, double resolution, double mvx, double mvy) {

    NodeList pathNodes = element.getElementsByTagName("path");
    // Node transforms = element.getAttributes().getNamedItem("transform");

    if (pathNodes != null) {


      ArrayList<Vector3d> p = new ArrayList<Vector3d>();
      int pathNodeCount = pathNodes.getLength();
      // BowlerStudioController.setCsg(null,null);
      // println "Loading paths: "+pathNodeCount
      for (int iPathNode = 0; iPathNode < pathNodeCount; iPathNode++) {

        Node pathNode = pathNodes.item(iPathNode);
        MetaPostPath2 mpp = new MetaPostPath2(pathNode);
        String code = mpp.toCode();

        loadComposite(code, resolution, mvx, mvy);
      }
    }

  }

  private void loadComposite(String code, double resolution, double mvx, double mvy) {
    // Count the occourences of M
    int count = code.length() - code.replace("M", "").length();
    if (count < 2) {
      // println "Single path found"

      setHolePolarity(true);
      try {
        loadSingle(code, resolution, mvx, mvy);
      } catch (Exception ex) {
        // BowlerStudio.printStackTrace(ex);
      }
    } else {

      setHolePolarity(false);
      String[] pathParts = code.split("M");
      // println "Complex path found "+ pathParts.length ;
      for (int i = 0; i < pathParts.length; i++) {
        String sectionedPart = "M" + pathParts[i];
        // reapply the split char
        if (sectionedPart.length() > 1) {

          // println "Seperated complex: "
          loadSingle(sectionedPart, resolution, mvx, mvy);
        }
      }
    }
    // System.out.println("SVG has this many elements loaded: "+sections.size());
    // BowlerStudioController.setCsg(sections,null);
  }

  private void loadSingle(String code, double resolution, double mvx, double mvy) {
    // println code
    BezierPath path = new BezierPath();
    path.parsePathString(code);
    ArrayList<Vector3d> p = new ArrayList<Vector3d>();
    for (double i = 0; i < 1.0; i += resolution) {
      Vector3d point = path.eval((float) i);
      //point.y=-point.y;
      // println point
      p.add(point);
    }
    p.add(path.eval((float) (1.0 - resolution / 2.0)));
    // System.out.println(" Path " + code);
    Polygon poly = Polygon.fromPoints(p);
    Transform transform = new Transform();
    transform.translate(mvx, mvy, 0);
    poly.transform(transform);
    if (polygons == null)
      polygons = new ArrayList<Polygon>();
    polygons.add(poly);

  }

  public ArrayList<CSG> extrude(double thickness, double resolution) throws IOException {

    /**
     * Reads a file and parses the path elements.
     * 
     * @param args args[0] - Filename to parse.
     * @throws IOException Error reading the SVG file.
     */

    // SVGLoad converter = new eu.mihosoft.vrl.v3d.svg.SVGLoad(uri.toString());
    // println "Loading converter"

    ;
    if (sections == null) {
      loadAllGroups(resolution, 0, 0);
      loadExtrusionSectoins(thickness);
    }
    return sections;
  }

  private void loadExtrusionSectoins(double thickness) {
    if (sections == null)
      sections = new ArrayList<CSG>();
    if (holes == null)
      holes = new ArrayList<CSG>();
    sections.clear();
    holes.clear();
    for (Polygon p : polygons) {
      boolean hole = Extrude.isCCW(p);
      CSG newbit =
          Extrude.getExtrusionEngine().extrude(new Vector3d(0, 0, thickness), p).scale(0.376975);// SVG
                                                                                                 // to
                                                                                                 // mm
                                                                                                 // scale
      if (!holePolarity)
        hole = !hole;
      try {


        if (!hole) {
          // println "NOT hole"
          sections.add(newbit);
        } else {
          // println "Hole"
          holes.add(newbit);
        }
        //
        //
      } catch (Exception ex) {
        // System.out.println(" Path "+code );
        // BowlerStudio.printStackTrace(ex);
        //
      }
    }
    for (int i = 0; i < sections.size(); i++) {

      for (CSG h : holes) {
        CSG cut = sections.get(i).difference(h);
        if (cut.getPolygons().size() > 0) {
          // only apply cuts that to not obliterate the object, this should preserve islands
          sections.set(i, cut);
        }
      }


    }
  }

  /**
   * This will set the document to parse. This method also initializes the SVG DOM enhancements,
   * which are necessary to perform SVG and CSS manipulations. The initialization is also required
   * to extract information from the SVG path elements.
   *
   * @param document The document that contains SVG content.
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
   * Enhance the SVG DOM for the given document to provide CSS- and SVG-specific DOM interfaces.
   * 
   * @param document The document to enhance.
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
   * @param uri The path to the SVG file to read.
   * @return A Document instance that represents the SVG file.
   * @throws Exception The file could not be read.
   */
  private Document createSVGDocument(URI uri) throws IOException {
    String parser = XMLResourceDescriptor.getXMLParserClassName();
    SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
    return factory.createDocument(uri.toString());
  }
}
