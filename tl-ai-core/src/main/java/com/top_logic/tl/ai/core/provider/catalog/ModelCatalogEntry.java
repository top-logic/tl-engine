/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.catalog;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.ReadOnly;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.tl.ai.core.provider.LLMProviderService;

/**
 * Configuration view for a single model's catalog metadata in the {@link LLMProviderService}.
 *
 * <p>
 * Populated automatically from the remote url's catalog on startup. All properties are
 * {@link ReadOnly} — the catalog is ground truth and must not be edited by the user.
 * </p>
 */
public class ModelCatalogEntry {

	/**
	 * Model metadata configuration.
	 */
	@DisplayOrder({
		Config.CONTEXT_WINDOW,
		Config.MAX_OUTPUT,
		Config.MAX_INPUT,
		Config.REASONING,
		Config.TOOL_CALL,
		Config.TEMPERATURE,
		Config.ATTACHMENT,
		Config.STRUCTURED_OUTPUT,
		Config.OPEN_WEIGHTS,
		Config.STATUS,
		Config.KNOWLEDGE_CUTOFF,
		Config.RELEASE_DATE,
		Config.LAST_UPDATED,
		Config.INPUT_PRICE,
		Config.OUTPUT_PRICE,
		Config.CACHE_READ_PRICE,
		Config.CACHE_WRITE_PRICE,
		Config.REASONING_PRICE,
	})
	public interface Config extends NamedConfigMandatory, PriceProperties {

		/** @see #getContextWindow() */
		String CONTEXT_WINDOW = "context-window";

		/** @see #getMaxOutput() */
		String MAX_OUTPUT = "max-output";

		/** @see #getMaxInput() */
		String MAX_INPUT = "max-input";

		/** @see #getReasoning() */
		String REASONING = "reasoning";

		/** @see #getToolCall() */
		String TOOL_CALL = "tool-call";

		/** @see #getTemperature() */
		String TEMPERATURE = "temperature";

		/** @see #getAttachment() */
		String ATTACHMENT = "attachment";

		/** @see #getStructuredOutput() */
		String STRUCTURED_OUTPUT = "structured-output";

		/** @see #getOpenWeights() */
		String OPEN_WEIGHTS = "open-weights";

		/** @see #getStatus() */
		String STATUS = "status";

		/** @see #getKnowledgeCutoff() */
		String KNOWLEDGE_CUTOFF = "knowledge-cutoff";

		/** @see #getReleaseDate() */
		String RELEASE_DATE = "release-date";

		/** @see #getLastUpdated() */
		String LAST_UPDATED = "last-updated";

		// --- Limits ---

		/**
		 * Maximum input context window size in tokens.
		 */
		@Name(CONTEXT_WINDOW)
		@Nullable
		@ReadOnly
		Integer getContextWindow();

		/** @see #getContextWindow() */
		void setContextWindow(Integer value);

		/**
		 * Maximum output tokens per response.
		 */
		@Name(MAX_OUTPUT)
		@Nullable
		@ReadOnly
		Integer getMaxOutput();

		/** @see #getMaxOutput() */
		void setMaxOutput(Integer value);

		/**
		 * Maximum input tokens, if the model distinguishes input from context limits.
		 */
		@Name(MAX_INPUT)
		@Nullable
		@ReadOnly
		Integer getMaxInput();

		/** @see #getMaxInput() */
		void setMaxInput(Integer value);

		// --- Capabilities ---

		/**
		 * Whether the model supports extended thinking / chain-of-thought reasoning.
		 */
		@Name(REASONING)
		@Label("Reasoning support")
		@Nullable
		@ReadOnly
		Boolean getReasoning();

		/** @see #getReasoning() */
		void setReasoning(Boolean value);

		/**
		 * Whether the model supports tool/function calling.
		 */
		@Name(TOOL_CALL)
		@Label("Tool calling")
		@Nullable
		@ReadOnly
		Boolean getToolCall();

		/** @see #getToolCall() */
		void setToolCall(Boolean value);

		/**
		 * Whether the model supports the temperature sampling parameter.
		 */
		@Name(TEMPERATURE)
		@Label("Temperature support")
		@Nullable
		@ReadOnly
		Boolean getTemperature();

		/** @see #getTemperature() */
		void setTemperature(Boolean value);

		/**
		 * Whether the model supports file attachments.
		 */
		@Name(ATTACHMENT)
		@Label("File attachments")
		@Nullable
		@ReadOnly
		Boolean getAttachment();

		/** @see #getAttachment() */
		void setAttachment(Boolean value);

		/**
		 * Whether the model supports structured (JSON) output.
		 */
		@Name(STRUCTURED_OUTPUT)
		@Label("Structured output support")
		@Nullable
		@ReadOnly
		Boolean getStructuredOutput();

		/** @see #getStructuredOutput() */
		void setStructuredOutput(Boolean value);

		// --- Metadata ---

		/**
		 * Whether the model weights are publicly available.
		 */
		@Name(OPEN_WEIGHTS)
		@Nullable
		@ReadOnly
		Boolean getOpenWeights();

		/** @see #getOpenWeights() */
		void setOpenWeights(Boolean value);

		/**
		 * Lifecycle status of the model, e.g. "alpha", "beta", or "deprecated". Empty when the
		 * model is generally available.
		 */
		@Name(STATUS)
		@Nullable
		@ReadOnly
		String getStatus();

		/** @see #getStatus() */
		void setStatus(String value);

		/**
		 * Training data knowledge cutoff date (e.g. "2026-01-31").
		 */
		@Name(KNOWLEDGE_CUTOFF)
		@Nullable
		@ReadOnly
		String getKnowledgeCutoff();

		/** @see #getKnowledgeCutoff() */
		void setKnowledgeCutoff(String value);

		/**
		 * Model release date.
		 */
		@Name(RELEASE_DATE)
		@Nullable
		@ReadOnly
		String getReleaseDate();

		/** @see #getReleaseDate() */
		void setReleaseDate(String value);

		/**
		 * Date of the last update to this model.
		 */
		@Name(LAST_UPDATED)
		@Nullable
		@ReadOnly
		String getLastUpdated();

		/** @see #getLastUpdated() */
		void setLastUpdated(String value);

		// --- Prices (read-only copies of the catalog's) ---

		@Override
		@ReadOnly
		Double getInputPrice();

		/** @see #getInputPrice() */
		void setInputPrice(Double value);

		@Override
		@ReadOnly
		Double getOutputPrice();

		/** @see #getOutputPrice() */
		void setOutputPrice(Double value);

		@Override
		@ReadOnly
		Double getCacheReadPrice();

		/** @see #getCacheReadPrice() */
		void setCacheReadPrice(Double value);

		@Override
		@ReadOnly
		Double getCacheWritePrice();

		/** @see #getCacheWritePrice() */
		void setCacheWritePrice(Double value);

		@Override
		@ReadOnly
		Double getReasoningPrice();

		/** @see #getReasoningPrice() */
		void setReasoningPrice(Double value);

	}

	/**
	 * The prices of a model, per million tokens, in US dollars - the unit and the currency of the
	 * models.dev catalog.
	 *
	 * <p>
	 * What a run costs is computed from these: each kind of token the provider reports is charged at
	 * its own price. A price that is not given falls back - cache read and cache write to the input
	 * price, reasoning to the output price - because the provider charges those tokens all the same.
	 * </p>
	 */
	public interface PriceProperties extends ConfigurationItem {

		/** @see #getInputPrice() */
		String INPUT_PRICE = "input-price";

		/** @see #getOutputPrice() */
		String OUTPUT_PRICE = "output-price";

		/** @see #getCacheReadPrice() */
		String CACHE_READ_PRICE = "cache-read-price";

		/** @see #getCacheWritePrice() */
		String CACHE_WRITE_PRICE = "cache-write-price";

		/** @see #getReasoningPrice() */
		String REASONING_PRICE = "reasoning-price";

		/**
		 * The price of one million prompt tokens that are not read from the provider's cache, in US
		 * dollars.
		 *
		 * @see #INPUT_PRICE
		 */
		@Name(INPUT_PRICE)
		@Label("Input price (USD per million tokens)")
		@Nullable
		Double getInputPrice();

		/**
		 * The price of one million response tokens, in US dollars.
		 *
		 * @see #OUTPUT_PRICE
		 */
		@Name(OUTPUT_PRICE)
		@Label("Output price (USD per million tokens)")
		@Nullable
		Double getOutputPrice();

		/**
		 * The price of one million prompt tokens read from the provider's cache, in US dollars.
		 * Empty means the input price.
		 *
		 * @see #CACHE_READ_PRICE
		 */
		@Name(CACHE_READ_PRICE)
		@Label("Cache read price (USD per million tokens)")
		@Nullable
		Double getCacheReadPrice();

		/**
		 * The price of one million prompt tokens written to the provider's cache, in US dollars.
		 * Empty means the input price.
		 *
		 * @see #CACHE_WRITE_PRICE
		 */
		@Name(CACHE_WRITE_PRICE)
		@Label("Cache write price (USD per million tokens)")
		@Nullable
		Double getCacheWritePrice();

		/**
		 * The price of one million reasoning tokens, in US dollars. Empty means the output price.
		 *
		 * @see #REASONING_PRICE
		 */
		@Name(REASONING_PRICE)
		@Label("Reasoning price (USD per million tokens)")
		@Nullable
		Double getReasoningPrice();
	}

	/**
	 * Editable model metadata for manually configured models (e.g. local or <code>OpenAI</code>-compatible
	 * endpoints) that are not in the remote catalog.
	 *
	 * <p>
	 * Unlike {@link Config}, these properties are writable in the admin UI and are not cleared
	 * on startup.
	 * </p>
	 *
	 * <p>
	 * <b>Prices.</b> A local model has no price in the catalog and so costs nothing in a money
	 * budget. The price properties give it one, in US dollars per million tokens like the catalog.
	 * An override that sets an input or an output price replaces the catalog's prices of that model;
	 * one that sets neither keeps them.
	 * </p>
	 */
	@DisplayOrder({
		OverrideConfig.CONTEXT_WINDOW,
		OverrideConfig.MAX_OUTPUT,
		OverrideConfig.REASONING,
		OverrideConfig.TOOL_CALL,
		OverrideConfig.TEMPERATURE,
		OverrideConfig.INPUT_PRICE,
		OverrideConfig.OUTPUT_PRICE,
		OverrideConfig.CACHE_READ_PRICE,
		OverrideConfig.CACHE_WRITE_PRICE,
		OverrideConfig.REASONING_PRICE,
	})
	public interface OverrideConfig extends NamedConfigMandatory, PriceProperties {

		/** @see #getContextWindow() */
		String CONTEXT_WINDOW = "context-window";

		/** @see #getMaxOutput() */
		String MAX_OUTPUT = "max-output";

		/** @see #getReasoning() */
		String REASONING = "reasoning";

		/** @see #getToolCall() */
		String TOOL_CALL = "tool-call";

		/** @see #getTemperature() */
		String TEMPERATURE = "temperature";

		/**
		 * Maximum input context window size in tokens.
		 *
		 * @see #CONTEXT_WINDOW
		 */
		@Name(CONTEXT_WINDOW)
		@IntDefault(128_000)
		int getContextWindow();

		/**
		 * Maximum output tokens per response.
		 *
		 * @see #MAX_OUTPUT
		 */
		@Name(MAX_OUTPUT)
		@IntDefault(16_384)
		int getMaxOutput();

		/**
		 * Whether the model supports extended thinking / chain-of-thought reasoning.
		 *
		 * @see #REASONING
		 */
		@Name(REASONING)
		@BooleanDefault(false)
		boolean getReasoning();

		/**
		 * Whether the model supports tool/function calling.
		 *
		 * @see #TOOL_CALL
		 */
		@Name(TOOL_CALL)
		@BooleanDefault(true)
		boolean getToolCall();

		/**
		 * Whether the model supports the temperature sampling parameter.
		 *
		 * @see #TEMPERATURE
		 */
		@Name(TEMPERATURE)
		@BooleanDefault(true)
		boolean getTemperature();

	}

	/**
	 * Converts an {@link OverrideConfig} into a {@link ModelInfo} record.
	 *
	 * @param config
	 *        The override.
	 * @param catalog
	 *        What the catalog says about the same model, or {@code null} when it does not list it.
	 *        Only its prices are read: an override that names no price keeps them.
	 */
	public static ModelInfo overrideToModelInfo(OverrideConfig config, ModelInfo catalog) {
		ModelInfo.ModelCost cost = priceOf(config);
		if (cost == null && catalog != null) {
			cost = catalog.cost();
		}
		return new ModelInfo(
			config.getName(),
			null,
			new ModelInfo.ModelLimits(config.getContextWindow(), config.getMaxOutput(), 0),
			new ModelInfo.ModelCapabilities(
				config.getReasoning(), config.getToolCall(), config.getTemperature(),
				false, false, null, null),
			cost, null, false, null);
	}

	/**
	 * The prices an override names, or {@code null} when it names neither an input nor an output
	 * price.
	 *
	 * <p>
	 * Cache and reasoning prices alone do not make a price list: without the price of the tokens
	 * every request has, there is nothing to fall back to.
	 * </p>
	 */
	static ModelInfo.ModelCost priceOf(PriceProperties config) {
		Double input = config.getInputPrice();
		Double output = config.getOutputPrice();
		if (input == null && output == null) {
			return null;
		}
		return new ModelInfo.ModelCost(
			input == null ? 0 : input.doubleValue(),
			output == null ? 0 : output.doubleValue(),
			config.getCacheReadPrice(),
			config.getCacheWritePrice(),
			config.getReasoningPrice(),
			null, null, null);
	}

	/**
	 * Converts a fetched {@link ModelInfo} into a read-only config item.
	 */
	public static Config toConfig(String modelId, ModelInfo info) {
		Config config = com.top_logic.basic.config.TypedConfiguration.newConfigItem(Config.class);
		config.setName(modelId);

		config.setContextWindow(info.limits().contextWindow());
		config.setMaxOutput(info.limits().maxOutput());
		if (info.limits().maxInput() > 0) {
			config.setMaxInput(info.limits().maxInput());
		}

		config.setReasoning(info.capabilities().reasoning());
		config.setToolCall(info.capabilities().toolCall());
		config.setTemperature(info.capabilities().temperature());
		config.setAttachment(info.capabilities().attachment());
		config.setStructuredOutput(info.capabilities().structuredOutput());

		config.setOpenWeights(info.openWeights());
		config.setStatus(info.status());

		if (info.dates() != null) {
			config.setKnowledgeCutoff(info.dates().knowledgeCutoff());
			config.setReleaseDate(info.dates().releaseDate());
			config.setLastUpdated(info.dates().lastUpdated());
		}

		ModelInfo.ModelCost cost = info.cost();
		if (cost != null) {
			config.setInputPrice(Double.valueOf(cost.input()));
			config.setOutputPrice(Double.valueOf(cost.output()));
			config.setCacheReadPrice(cost.cacheRead());
			config.setCacheWritePrice(cost.cacheWrite());
			config.setReasoningPrice(cost.reasoning());
		}

		return config;
	}
}
