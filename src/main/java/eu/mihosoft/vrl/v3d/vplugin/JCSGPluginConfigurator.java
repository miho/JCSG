/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d.vplugin;

import eu.mihosoft.vrl.system.InitPluginAPI;
import eu.mihosoft.vrl.system.PluginAPI;
import eu.mihosoft.vrl.system.PluginIdentifier;
import eu.mihosoft.vrl.system.VPluginAPI;
import eu.mihosoft.vrl.system.VPluginConfigurator;

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
       exportPackage("eu.mihosoft.vrl.v3d");

       // describe the plugin
       setDescription("JCSG Plugin (Constructive Solid Geometry)");

       // copyright info
       setCopyrightInfo("JCSG",
               "(c) Michael Hoffer",
               "www.mihosoft.eu", "BSD", "");

       // specify dependencies
       // addDependency(new PluginDependency("VRL", "0.4.0", "0.4.0"));
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
           
           vapi.addComponent(PointCloud2ConvexHull.class);
           vapi.addComponent(JCSGSample01.class);
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
