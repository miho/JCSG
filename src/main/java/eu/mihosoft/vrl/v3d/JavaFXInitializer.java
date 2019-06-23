package eu.mihosoft.vrl.v3d;

import javafx.application.Application;
import javafx.stage.Stage;
public class JavaFXInitializer extends javafx.application.Application {
	private static final int NUM_COUNT = 2;
	private final static java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(NUM_COUNT);
	
	public JavaFXInitializer(){
		
	}
	private static void gointernal() {
		if(latch.getCount()!=NUM_COUNT) {
			//System.out.println("ERR initializer already started");
			return;
		}
		System.out.println("Starting JavaFX initializer..."+JavaFXInitializer.class);
		latch.countDown();
		try {
			launch();
		}catch(java.lang.IllegalStateException e) {
			latch.countDown();
		}
	}
	public static void go() {
		if(latch.getCount()!=NUM_COUNT) {
			//System.out.println("ERR initializer already started");
			return;
		}
		new Thread() {
			public void run() {
				gointernal();
			}
		}.start();
		try {
			JavaFXInitializer.latch.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
		StackTraceElement e = stacktrace[2];//maybe this number needs to be corrected
		System.out.println("Finished JavaFX initializing! "+e);
	}
	@Override
	public void start(Stage primaryStage) throws Exception {
		latch.countDown();
	}
}