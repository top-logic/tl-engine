/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.embed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.tl.ai.index.lucene.Vectors;

/**
 * What every {@link Embedder} does the same way: prefixes, batches, the number of dimensions and
 * normalization.
 *
 * <p>
 * A model that is trained with Matryoshka representation learning (arctic-embed v2, Qwen3-Embedding)
 * keeps most of its quality when its vector is cut to fewer dimensions. {@link Config#getDimensions()}
 * smaller than the model's output cuts every vector to that length and normalizes it again; that
 * makes the index smaller. A model without that training must be configured with its own length.
 * </p>
 *
 * @param <C>
 *        The configuration type.
 */
public abstract class AbstractEmbedder<C extends AbstractEmbedder.Config<?>> extends AbstractConfiguredInstance<C>
		implements Embedder {

	/**
	 * Configuration of an {@link AbstractEmbedder}.
	 *
	 * @param <I>
	 *        The embedder type.
	 */
	public interface Config<I extends AbstractEmbedder<?>> extends PolymorphicConfiguration<I> {

		/** Configuration name of {@link #getQueryPrefix()}. */
		String QUERY_PREFIX = "query-prefix";

		/** Configuration name of {@link #getPassagePrefix()}. */
		String PASSAGE_PREFIX = "passage-prefix";

		/** Configuration name of {@link #getDimensions()}. */
		String DIMENSIONS = "dimensions";

		/** Configuration name of {@link #getBatchSize()}. */
		String BATCH_SIZE = "batch-size";

		/**
		 * Text put in front of every query before it is embedded, e.g. {@code "query: "} for the e5
		 * and arctic-embed models.
		 *
		 * @see #QUERY_PREFIX
		 */
		@Name(QUERY_PREFIX)
		String getQueryPrefix();

		/**
		 * Text put in front of every passage before it is embedded, e.g. {@code "passage: "} for
		 * the e5 models; empty for arctic-embed v2.
		 *
		 * @see #PASSAGE_PREFIX
		 */
		@Name(PASSAGE_PREFIX)
		String getPassagePrefix();

		/**
		 * The number of components of every vector.
		 *
		 * <p>
		 * The model's own length, or fewer for a model trained to be cut (Matryoshka).
		 * </p>
		 *
		 * @see #DIMENSIONS
		 */
		@Name(DIMENSIONS)
		@Mandatory
		int getDimensions();

		/**
		 * How many texts go into one call of the model.
		 *
		 * @see #BATCH_SIZE
		 */
		@Name(BATCH_SIZE)
		@IntDefault(32)
		int getBatchSize();
	}

	/**
	 * Creates an {@link AbstractEmbedder}.
	 */
	public AbstractEmbedder(InstantiationContext context, C config) {
		super(context, config);
		if (config.getDimensions() <= 0) {
			context.error("The number of dimensions of an embedder must be positive, not " + config.getDimensions() + ".");
		}
		if (config.getBatchSize() <= 0) {
			context.error("The batch size of an embedder must be positive, not " + config.getBatchSize() + ".");
		}
	}

	@Override
	public int dimensions() {
		return getConfig().getDimensions();
	}

	@Override
	public String fingerprint() {
		return getClass().getName() + "|" + modelId() + "|" + dimensions() + "|" + prefix(getConfig().getQueryPrefix())
			+ "|" + prefix(getConfig().getPassagePrefix()) + "|" + fingerprintDetails();
	}

	/**
	 * What else makes this embedder's vectors what they are, e.g. its pooling.
	 */
	protected String fingerprintDetails() {
		return "";
	}

	@Override
	public List<float[]> embedPassages(List<String> texts) throws EmbeddingException {
		List<float[]> result = new ArrayList<>(texts.size());
		int batch = getConfig().getBatchSize();
		String prefix = prefix(getConfig().getPassagePrefix());
		for (int start = 0; start < texts.size(); start += batch) {
			List<String> part = texts.subList(start, Math.min(texts.size(), start + batch));
			List<String> prefixed = new ArrayList<>(part.size());
			for (String text : part) {
				prefixed.add(prefix + text);
			}
			result.addAll(finish(embedBatch(prefixed), part.size()));
		}
		return result;
	}

	@Override
	public float[] embedQuery(String text) throws EmbeddingException {
		return finish(embedBatch(List.of(prefix(getConfig().getQueryPrefix()) + text)), 1).get(0);
	}

	/**
	 * Embeds one batch of texts, prefixes already applied.
	 *
	 * @return One vector per text, at least {@link #dimensions()} long, in the order of the texts;
	 *         need not be normalized.
	 */
	protected abstract List<float[]> embedBatch(List<String> texts) throws EmbeddingException;

	private List<float[]> finish(List<float[]> raw, int expected) throws EmbeddingException {
		if (raw.size() != expected) {
			throw new EmbeddingException(
				"Model '" + modelId() + "' returned " + raw.size() + " vectors for " + expected + " texts.");
		}
		int dimensions = dimensions();
		List<float[]> result = new ArrayList<>(raw.size());
		for (float[] vector : raw) {
			if (vector.length < dimensions) {
				throw new EmbeddingException("Model '" + modelId() + "' returned vectors of " + vector.length
					+ " dimensions, but " + dimensions + " are configured.");
			}
			float[] cut = vector.length == dimensions ? vector : Arrays.copyOf(vector, dimensions);
			result.add(Vectors.normalized(cut));
		}
		return result;
	}

	private static String prefix(String prefix) {
		return prefix == null ? "" : prefix;
	}

}
