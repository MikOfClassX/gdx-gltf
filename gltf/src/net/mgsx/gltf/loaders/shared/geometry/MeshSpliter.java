package net.mgsx.gltf.loaders.shared.geometry;

import java.util.Arrays;

import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntIntMap;

class MeshSpliter {

	public static void split(Array<float[]> splitVertices, Array<short[]> splitIndices, float[] vertices, VertexAttributes attributes, int[] indices,
			int verticesPerPrimitive) {

		// Keep every primitive together while assigning its source vertices to a 16-bit output group.
		final int maxGroupVertices = 65535;
		final int stride = attributes.vertexSize / 4;
		final int maxGroupFloats = maxGroupVertices * stride;
		float[] groupVertices = new float[maxGroupFloats];
		IntArray groupIndices = new IntArray();
		IntIntMap reindex = new IntIntMap();
		int groupVertexCount = 0;
		int[] newVertices = new int[verticesPerPrimitive];

		for (int i = 0; i < indices.length; i += verticesPerPrimitive) {
			// Count new unique vertices first so a primitive is never split across output groups.
			int newVertexCount = 0;
			for (int j = 0; j < verticesPerPrimitive; j++) {
				int sourceIndex = indices[i + j];
				boolean alreadyQueued = false;
				for (int k = 0; k < newVertexCount; k++) {
					if (newVertices[k] == sourceIndex) {
						alreadyQueued = true;
						break;
					}
				}
				if (!reindex.containsKey(sourceIndex) && !alreadyQueued) {
					newVertices[newVertexCount++] = sourceIndex;
				}
			}

			if (groupVertexCount + newVertexCount > maxGroupVertices) {
				// Finalize the current group before placing this complete primitive in a fresh group.
				splitVertices.add(Arrays.copyOf(groupVertices, groupVertexCount * stride));
				short[] outputIndices = new short[groupIndices.size];
				for (int j = 0; j < groupIndices.size; j++) {
					outputIndices[j] = (short) groupIndices.get(j);
				}
				splitIndices.add(outputIndices);
				groupVertices = new float[maxGroupFloats];
				groupIndices = new IntArray();
				reindex = new IntIntMap();
				groupVertexCount = 0;
			}

			// Remap this primitive and copy each newly encountered source vertex once.
			for (int j = 0; j < verticesPerPrimitive; j++) {
				int sourceIndex = indices[i + j];
				int outputIndex = reindex.get(sourceIndex, -1);
				if (outputIndex < 0) {
					outputIndex = groupVertexCount++;
					reindex.put(sourceIndex, outputIndex);
					System.arraycopy(vertices, sourceIndex * stride, groupVertices, outputIndex * stride, stride);
				}
				groupIndices.add(outputIndex);
			}
		}

		// Emit the final non-empty group without including unused capacity.
		if (groupIndices.size > 0) {
			splitVertices.add(Arrays.copyOf(groupVertices, groupVertexCount * stride));
			short[] outputIndices = new short[groupIndices.size];
			for (int i = 0; i < groupIndices.size; i++) {
				outputIndices[i] = (short) groupIndices.get(i);
			}
			splitIndices.add(outputIndices);
		}
	}
}
