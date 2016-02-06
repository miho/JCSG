package eu.mihosoft.vrl.v3d;

import java.io.File;

public class DXFTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		CSG logo = new DXFExtrude(new File("/home/hephaestus/git/Graphics/Graphics/HanddrawnLogo/logo_fulldesign_outline.dxf"),5).toCSG();
		
	}

}
