package eu.mihosoft.vrl.v3d;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Paths;

import org.junit.Test;

import javafx.scene.text.Font;

public class textTest {

	@Test
	public void test() throws IOException {
		TextExtrude.text(10.0, "Hello", new Font("Helvedica",  18));
		TextExtrude.text(10.0, "Hello World!", new Font("Times New Roman", 18));
		
		Text3d text = new Text3d("Hello world", 10);
		FileUtil.write(Paths.get("exampleText.stl"),
				text.toCSG().toStlString());
	}

}
