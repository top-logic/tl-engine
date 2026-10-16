/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.embed;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;

/**
 * A deterministic {@link Embedder} for tests: each word of a text adds a fixed signed unit to one
 * dimension chosen by its hash.
 *
 * <p>
 * Texts that share words are close, texts that share none are nearly orthogonal. It needs no model
 * and no network, and it counts what it was asked to embed, so a test can assert that unchanged
 * chunks are not embedded again.
 * </p>
 */
public class HashingEmbedder extends AbstractEmbedder<HashingEmbedder.Config<?>> {

	/**
	 * Configuration of a {@link HashingEmbedder}.
	 */
	public interface Config<I extends HashingEmbedder> extends AbstractEmbedder.Config<I> {

		/**
		 * The model name stored with the vectors.
		 */
		@Name("model")
		@StringDefault("hashing")
		String getModel();
	}

	private final AtomicInteger _passages = new AtomicInteger();

	private final AtomicInteger _calls = new AtomicInteger();

	/**
	 * Creates a {@link HashingEmbedder} from configuration.
	 */
	@CalledByReflection
	public HashingEmbedder(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	/**
	 * A {@link HashingEmbedder} of the given number of dimensions.
	 */
	public static HashingEmbedder create(int dimensions) {
		return create("hashing", dimensions);
	}

	/**
	 * A {@link HashingEmbedder} with the given model name and number of dimensions.
	 */
	@SuppressWarnings("unchecked")
	public static HashingEmbedder create(String model, int dimensions) {
		Config<HashingEmbedder> config = TypedConfiguration.newConfigItem(Config.class);
		config.update(config.descriptor().getProperty(AbstractEmbedder.Config.DIMENSIONS), dimensions);
		config.update(config.descriptor().getProperty("model"), model);
		return new HashingEmbedder(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
	}

	@Override
	public String modelId() {
		return getConfig().getModel();
	}

	/**
	 * How many texts were embedded so far, queries included.
	 */
	public int texts() {
		return _passages.get();
	}

	/**
	 * How many batches were embedded so far.
	 */
	public int calls() {
		return _calls.get();
	}

	@Override
	protected List<float[]> embedBatch(List<String> texts) {
		_calls.incrementAndGet();
		_passages.addAndGet(texts.size());
		List<float[]> result = new ArrayList<>(texts.size());
		for (String text : texts) {
			result.add(embed(text, dimensions()));
		}
		return result;
	}

	/**
	 * The hashing vector of a text.
	 */
	public static float[] embed(String text, int dimensions) {
		float[] vector = new float[dimensions];
		for (String word : text.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+")) {
			if (word.isEmpty()) {
				continue;
			}
			int hash = word.hashCode();
			vector[Math.floorMod(hash, dimensions)] += (hash & 0x10000) == 0 ? 1 : -1;
		}
		if (allZero(vector)) {
			vector[0] = 1;
		}
		return vector;
	}

	private static boolean allZero(float[] vector) {
		for (float x : vector) {
			if (x != 0) {
				return false;
			}
		}
		return true;
	}

}
