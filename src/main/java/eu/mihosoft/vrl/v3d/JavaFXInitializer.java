package eu.mihosoft.vrl.v3d;

public class JavaFXInitializer {
	private static final int NUM_COUNT = 2;
	private final static java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(NUM_COUNT);
	private static boolean errored=false;
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
			class initApp extends javafx.application.Application{
				@Override
				public void start(javafx.stage.Stage primaryStage) throws Exception {
					latch.countDown();
				}
			}
			initApp.launch();
		}catch(Throwable e) {
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
				try {
					gointernal();
				}catch(Throwable t) {
					errored=true;
				}
			}
		}.start();
		try {
			JavaFXInitializer.latch.await();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
		StackTraceElement e = stacktrace[2];//maybe this number needs to be corrected
		System.out.println("Finished JavaFX initializing! "+e);
	}

}