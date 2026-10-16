/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.lucene;

import org.apache.lucene.codecs.KnnVectorsFormat;
import org.apache.lucene.codecs.lucene912.Lucene912Codec;
import org.apache.lucene.codecs.lucene99.Lucene99HnswScalarQuantizedVectorsFormat;

/**
 * The codec the index is written with: Lucene's default, with 7-bit quantized HNSW vectors.
 *
 * <p>
 * The codec keeps the name {@code Lucene912}; the vector format is chosen per field and its name is
 * written into each segment. So a plain {@code DirectoryReader.open} reads the index without this
 * class - Solr's {@code SchemaCodecFactory} does the same. The {@code Lucene912Codec} javadoc makes a
 * subclass responsible for backward compatibility; the index is derived data, and the Lucene major
 * version is part of every generation's fingerprint, so another Lucene builds a new generation.
 * </p>
 *
 * <p>
 * The quantized format keeps the raw float vectors beside the quantized bytes, which is what
 * {@link HybridSearch} re-scores the over-fetched candidates with.
 * </p>
 */
final class IndexCodec extends Lucene912Codec {

	/** Neighbours per node of the graph (Lucene's default). */
	private static final int MAX_CONN = 16;

	/** Candidates while the graph is built (Lucene's default). */
	private static final int BEAM_WIDTH = 100;

	/** Bits per dimension of the quantized vector. */
	private static final int BITS = 7;

	private final KnnVectorsFormat _vectors =
		new Lucene99HnswScalarQuantizedVectorsFormat(MAX_CONN, BEAM_WIDTH, 1, BITS, false, null, null);

	@Override
	public KnnVectorsFormat getKnnVectorsFormatForField(String field) {
		return _vectors;
	}

}
