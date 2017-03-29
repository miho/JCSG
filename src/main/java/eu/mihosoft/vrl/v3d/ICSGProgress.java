package eu.mihosoft.vrl.v3d;

public interface ICSGProgress {
	public void progressUpdate(int currentIndex,int finalIndex,String type, CSG intermediateShape);
}
