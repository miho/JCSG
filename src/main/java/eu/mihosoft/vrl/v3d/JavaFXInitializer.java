package eu.mihosoft.vrl.v3d;

import javafx.application.Application;
import javafx.stage.Stage;

public class JavaFXInitializer extends javafx.application.Application {
	public final static java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(2);
	public JavaFXInitializer(){
		
	}
	public static void go() {
		if(latch.getCount()>0)
			return;
		latch.countDown();
		launch();
	}
	@Override
	public void start(Stage primaryStage) throws Exception {
		latch.countDown();
	}
}
