package eu.mihosoft.vrl.v3d;

import java.util.Arrays;
import java.util.List;

import javafx.scene.shape.TriangleMesh;

public class CSGtoJavafx {
	
	public static MeshContainer meshFromPolygon(Polygon... poly) {
		return meshFromPolygon(Arrays.asList(poly));
	}
	public static MeshContainer meshFromPolygon(List<Polygon> poly) {
		TriangleMesh mesh = new TriangleMesh();

		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double minZ = Double.POSITIVE_INFINITY;

		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		double maxZ = Double.NEGATIVE_INFINITY;

		int counter = 0;
		for (Polygon p : poly) {
			if (p.vertices.size() >= 3) {

				// TODO: improve the triangulation?
				//
				// JavaOne requires triangular polygons.
				// If our polygon has more vertices, create
				// multiple triangles:
				Vertex firstVertex = p.vertices.get(0);
				for (int i = 0; i < p.vertices.size() - 2; i++) {

					if (firstVertex.pos.x < minX) {
						minX = firstVertex.pos.x;
					}
					if (firstVertex.pos.y < minY) {
						minY = firstVertex.pos.y;
					}
					if (firstVertex.pos.z < minZ) {
						minZ = firstVertex.pos.z;
					}

					if (firstVertex.pos.x > maxX) {
						maxX = firstVertex.pos.x;
					}
					if (firstVertex.pos.y > maxY) {
						maxY = firstVertex.pos.y;
					}
					if (firstVertex.pos.z > maxZ) {
						maxZ = firstVertex.pos.z;
					}

					mesh.getPoints().addAll((float) firstVertex.pos.x, (float) firstVertex.pos.y,
							(float) firstVertex.pos.z);

					mesh.getTexCoords().addAll(0); // texture (not covered)
					mesh.getTexCoords().addAll(0);

					Vertex secondVertex = p.vertices.get(i + 1);

					if (secondVertex.pos.x < minX) {
						minX = secondVertex.pos.x;
					}
					if (secondVertex.pos.y < minY) {
						minY = secondVertex.pos.y;
					}
					if (secondVertex.pos.z < minZ) {
						minZ = secondVertex.pos.z;
					}

					if (secondVertex.pos.x > maxX) {
						maxX = firstVertex.pos.x;
					}
					if (secondVertex.pos.y > maxY) {
						maxY = firstVertex.pos.y;
					}
					if (secondVertex.pos.z > maxZ) {
						maxZ = firstVertex.pos.z;
					}

					mesh.getPoints().addAll((float) secondVertex.pos.x, (float) secondVertex.pos.y,
							(float) secondVertex.pos.z);

					mesh.getTexCoords().addAll(0); // texture (not covered)
					mesh.getTexCoords().addAll(0);

					Vertex thirdVertex = p.vertices.get(i + 2);

					mesh.getPoints().addAll((float) thirdVertex.pos.x, (float) thirdVertex.pos.y,
							(float) thirdVertex.pos.z);

					if (thirdVertex.pos.x < minX) {
						minX = thirdVertex.pos.x;
					}
					if (thirdVertex.pos.y < minY) {
						minY = thirdVertex.pos.y;
					}
					if (thirdVertex.pos.z < minZ) {
						minZ = thirdVertex.pos.z;
					}

					if (thirdVertex.pos.x > maxX) {
						maxX = firstVertex.pos.x;
					}
					if (thirdVertex.pos.y > maxY) {
						maxY = firstVertex.pos.y;
					}
					if (thirdVertex.pos.z > maxZ) {
						maxZ = firstVertex.pos.z;
					}

					mesh.getTexCoords().addAll(0); // texture (not covered)
					mesh.getTexCoords().addAll(0);

					mesh.getFaces().addAll(counter, // first vertex
							0, // texture (not covered)
							counter + 1, // second vertex
							0, // texture (not covered)
							counter + 2, // third vertex
							0 // texture (not covered)
					);
					counter += 3;
				} // end for
			} // end if #verts >= 3

		} // end for polygon

		return new MeshContainer(new Vector3d(minX, minY, minZ), new Vector3d(maxX, maxY, maxZ), mesh);
	}

}
