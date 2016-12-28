package eu.mihosoft.vrl.v3d;

import javafx.scene.text.Font;

// TODO: Auto-generated Javadoc
/**
 * The Class TextTest.
 */
public class TextTest {
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String [] args){

		TextExtrude.text(10.0, "Hello", new Font("Helvedica", 18));
		TextExtrude.text(10.0, "Hello World!", new Font("Times New Roman",  18));
	}
}
