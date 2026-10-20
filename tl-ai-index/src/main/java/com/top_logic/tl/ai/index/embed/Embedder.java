/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.embed;

import java.util.List;

/**
 * Turns texts into vectors whose dot product says how close their meanings are.
 *
 * <p>
 * The index embeds every chunk with {@link #embedPassages(List)} and every query with
 * {@link #embedQuery(String)}; many models want a different prefix for each. Every vector this
 * interface returns is normalized to length 1 and has {@link #dimensions()} components.
 * </p>
 *
 * <p>
 * The {@link #fingerprint()} is part of the index generation: a change of the model, its dimensions
 * or its prefixes builds the index again, because vectors of two embeddings are not comparable.
 * </p>
 *
 * @see AbstractEmbedder
 */
public interface Embedder extends AutoCloseable {

	/**
	 * The name of the model, as stored with each vector.
	 */
	String modelId();

	/**
	 * The number of components of every vector.
	 */
	int dimensions();

	/**
	 * Everything that makes two vectors of this embedder comparable: model, dimensions, prefixes,
	 * pooling.
	 */
	String fingerprint();

	/**
	 * Embeds texts that are searched for, in one or more batches.
	 *
	 * @return One normalized vector per text, in the order of the texts.
	 * @throws EmbeddingException
	 *         When the texts cannot be embedded now.
	 */
	List<float[]> embedPassages(List<String> texts) throws EmbeddingException;

	/**
	 * Embeds a query.
	 *
	 * @return The normalized vector.
	 * @throws EmbeddingException
	 *         When the query cannot be embedded now.
	 */
	float[] embedQuery(String text) throws EmbeddingException;

	/**
	 * Frees what the embedder holds - a native session, a connection pool.
	 */
	@Override
	default void close() {
		// Nothing to free by default.
	}

}
