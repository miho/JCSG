/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d.jcsg.vplugin;

import eu.mihosoft.vrl.system.InitPluginAPI;
import eu.mihosoft.vrl.system.PluginAPI;
import eu.mihosoft.vrl.system.PluginDependency;
import eu.mihosoft.vrl.system.PluginIdentifier;
import eu.mihosoft.vrl.system.VPluginAPI;
import eu.mihosoft.vrl.system.VPluginConfigurator;
import eu.mihosoft.vrl.v3d.jcsg.CSG;
import eu.mihosoft.vrl.v3d.jcsg.Cube;
import eu.mihosoft.vrl.v3d.jcsg.Cylinder;
import eu.mihosoft.vrl.v3d.jcsg.Extrude;
import eu.mihosoft.vrl.v3d.jcsg.Sphere;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class JCSGPluginConfigurator extends VPluginConfigurator{  

    public JCSGPluginConfigurator() {
          //specify the plugin name and version
       setIdentifier(new PluginIdentifier("JCSG", "0.1"));

       // optionally allow other plugins to use the api of this plugin
       // you can specify packages that shall be
       // exported by using the exportPackage() method:
       //
       exportPackage("eu.mihosoft.vrl.v3d.jcsg");
       exportPackage("eu.mihosoft.vrl.v3d.jcsg.samples");

       // describe the plugin
       setDescription("JCSG Plugin (Constructive Solid Geometry)");

       // copyright info
       setCopyrightInfo("JCSG",
               "(c) Michael Hoffer",
               "www.mihosoft.eu", "BSD", "");

       // specify dependencies
        addDependency(new PluginDependency("VRL", "0.4.2.8.6", "0.4.x"));
    }
    

    @Override
    public void register(PluginAPI api) {
       // register plugin with canvas
       if (api instanceof VPluginAPI) {
           VPluginAPI vapi = (VPluginAPI) api;

           // Register visual components:
           //
           // Here you can add additional components,
           // type representations, styles etc.
           //
           // ** NOTE **
           //
           // To ensure compatibility with future versions of VRL,
           // you should only use the vapi or api object for registration.
           // If you directly use the canvas or its properties, please make
           // sure that you specify the VRL versions you are compatible with
           // in the constructor of this plugin configurator because the
           // internal api is likely to change.
           //
           // examples:
           //
           // vapi.addComponent(MyComponent.class);
           // vapi.addTypeRepresentation(MyType.class);
           
           vapi.addComponent(Cube.class);
           vapi.addComponent(Sphere.class);
           vapi.addComponent(Cylinder.class);
           
           vapi.addComponent(Extrude.class);
           vapi.addComponent(CSG.class);
//           vapi.addComponent(Transform.class);
           
           vapi.addComponent(CubeCreator.class);
           vapi.addComponent(CylinderCreator.class);
           vapi.addComponent(SphereCreator.class);
           
           vapi.addComponent(Union.class);
           vapi.addComponent(Difference.class);
           vapi.addComponent(Intersection.class);
           vapi.addComponent(Hull.class);
           vapi.addComponent(Transformation.class);
           vapi.addComponent(STLSaver.class);
           
           vapi.addTypeRepresentation(SilentCSGInputType.class);
           vapi.addTypeRepresentation(CSGOutputType.class);
           vapi.addTypeRepresentation(SilentCSGType.class);
           
       }
   }

    @Override
   public void unregister(PluginAPI api) {
       // nothing to unregister
   }

    @Override
    public void init(InitPluginAPI iApi) {
       // nothing to init
   }
 }
