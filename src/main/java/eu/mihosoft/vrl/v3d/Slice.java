package eu.mihosoft.vrl.v3d;

import java.util.ArrayList;
import java.util.List;

public class Slice  {
	private static ISlice sliceEngine = new ISlice() {
		
		@Override
		public List<Vector3d> slice(CSG incoming, Transform slicePlane, double normalInsetDistance) {
			List<Vector3d> slicedPoints = new ArrayList<>();
			System.out.println("This is a dummy slice engine and is not implemented yet");
			return slicedPoints;
		}
	};
	public static  List<Vector3d> slice(CSG incoming, Transform slicePlane, double normalInsetDistance) {
		return getSliceEngine().slice(incoming, slicePlane, normalInsetDistance);
	}
	
	
	public static ISlice getSliceEngine() {
		return sliceEngine;
	}
	public static void setSliceEngine(ISlice sliceEngine) {
		Slice.sliceEngine = sliceEngine;
	}

}
