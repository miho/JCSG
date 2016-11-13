package eu.mihosoft.vrl.v3d;

import java.io.File;
import java.util.ArrayList;

public class DXFTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		ArrayList<CSG> logo = DXF.toParts(new File("/home/hephaestus/git/Graphics/Graphics/SimplifiedLogo/simplified logo.svg"),5);
		
	}

}
