package eu.mihosoft.vrl.v3d;

public class JavaFXInitializer {
	private static final int NUM_COUNT = 2;
	private final static java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(NUM_COUNT);
	public static boolean errored=false;
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
			 final javafx.embed.swing.JFXPanel fxPanel = new javafx.embed.swing.JFXPanel();
			 latch.countDown();
		}catch(Throwable e) {
			latch.countDown();
			errored=true;
			e.printStackTrace();
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
					t.printStackTrace();
					errored=true;
				}
			}
		}.start();
		try {
			JavaFXInitializer.latch.await();
		} catch (Throwable e) {
			e.printStackTrace();
			errored=true;
		}
		StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
		StackTraceElement e = stacktrace[2];//maybe this number needs to be corrected
		System.out.println("Finished JavaFX initializing! "+e);
	}

}