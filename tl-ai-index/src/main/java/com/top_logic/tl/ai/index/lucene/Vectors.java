/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.FloatVectorValues;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.ReaderUtil;
import org.apache.lucene.util.VectorUtil;

/**
 * Vector arithmetic and reading the raw vectors back from the index.
 */
public final class Vectors {

	private Vectors() {
		// Static utility.
	}

	/**
	 * The dot product of two vectors of the same length; their cosine when both are normalized.
	 */
	public static float dot(float[] a, float[] b) {
		return VectorUtil.dotProduct(a, b);
	}

	/**
	 * The given vector scaled to length 1; the vector itself when it already has that length.
	 *
	 * <p>
	 * The index compares vectors by their dot product, which is their cosine only for normalized
	 * vectors.
	 * </p>
	 */
	public static float[] normalized(float[] vector) {
		double sum = 0;
		for (float x : vector) {
			sum += x * x;
		}
		if (Math.abs(sum - 1) < 1e-4 || sum == 0) {
			return vector;
		}
		float norm = (float) Math.sqrt(sum);
		float[] result = new float[vector.length];
		for (int i = 0; i < vector.length; i++) {
			result[i] = vector[i] / norm;
		}
		return result;
	}

	/**
	 * The normalized mean of the given vectors, or <code>null</code> for none.
	 */
	public static float[] mean(List<float[]> vectors) {
		if (vectors.isEmpty()) {
			return null;
		}
		float[] sum = new float[vectors.get(0).length];
		for (float[] vector : vectors) {
			for (int i = 0; i < sum.length; i++) {
				sum[i] += vector[i];
			}
		}
		return normalized(sum);
	}

	/**
	 * The raw float vectors of the given documents, by document number; a document without a vector
	 * is missing from the result.
	 *
	 * <p>
	 * The quantized vector format keeps these beside the quantized bytes. Read leaf by leaf in
	 * ascending document order, because the values are an iterator that only moves forward.
	 * </p>
	 *
	 * @param reader
	 *        The reader the document numbers belong to.
	 * @param field
	 *        The vector field.
	 * @param docs
	 *        Top-level document numbers.
	 */
	public static Map<Integer, float[]> read(IndexReader reader, String field, int[] docs) throws IOException {
		int[] sorted = docs.clone();
		Arrays.sort(sorted);
		List<LeafReaderContext> leaves = reader.leaves();
		Map<Integer, float[]> result = new HashMap<>();
		FloatVectorValues values = null;
		int currentLeaf = -1;
		LeafReaderContext leaf = null;
		for (int doc : sorted) {
			int leafIndex = ReaderUtil.subIndex(doc, leaves);
			if (leafIndex != currentLeaf) {
				currentLeaf = leafIndex;
				leaf = leaves.get(leafIndex);
				values = leaf.reader().getFloatVectorValues(field);
			}
			if (values == null) {
				continue;
			}
			int local = doc - leaf.docBase;
			int found = values.docID() >= local ? values.docID() : values.advance(local);
			if (found == local) {
				result.put(doc, values.vectorValue().clone());
			}
		}
		return result;
	}

	/**
	 * The vectors of {@link #read(IndexReader, String, int[])} in the order of the given documents.
	 */
	public static List<float[]> readAll(IndexReader reader, String field, int[] docs) throws IOException {
		Map<Integer, float[]> byDoc = read(reader, field, docs);
		List<float[]> result = new ArrayList<>();
		for (int doc : docs) {
			float[] vector = byDoc.get(doc);
			if (vector != null) {
				result.add(vector);
			}
		}
		return result;
	}

}
