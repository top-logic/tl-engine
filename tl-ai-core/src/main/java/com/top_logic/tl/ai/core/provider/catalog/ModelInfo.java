/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.catalog;

import java.util.Comparator;
import java.util.List;

/**
 * Immutable metadata about a model, resolved from the models.dev catalog.
 *
 * @param id           model identifier (e.g. "claude-sonnet-4-20250514")
 * @param family       model family for grouping (nullable, e.g. "claude-sonnet")
 * @param limits       context window and output limits
 * @param capabilities what the model supports
 * @param cost         pricing information (nullable — informational)
 * @param dates        knowledge cutoff, release, and last-updated dates (nullable)
 * @param openWeights  whether the model weights are publicly available
 * @param status       lifecycle status as reported by the provider catalog ({@code null} = GA /
 *                     production-ready, {@code "alpha"} = early preview, {@code "beta"} =
 *                     feature-complete preview, {@code "deprecated"} = end-of-life announced)
 */
public record ModelInfo(
		String id,
		String family,
		ModelLimits limits,
		ModelCapabilities capabilities,
		ModelCost cost,
		ModelDates dates,
		boolean openWeights,
		String status) {

	/**
	 * Token limits for a model.
	 *
	 * @param contextWindow maximum input context in tokens
	 * @param maxOutput     maximum output tokens per response
	 * @param maxInput      explicit input token limit (0 if unspecified)
	 */
	public record ModelLimits(int contextWindow, int maxOutput, int maxInput) {}

	/**
	 * Capability flags for a model.
	 *
	 * @param reasoning        supports extended thinking / chain-of-thought
	 * @param toolCall         supports tool/function calling
	 * @param temperature      supports temperature sampling parameter
	 * @param attachment       supports file attachments
	 * @param structuredOutput supports structured (JSON) output
	 * @param modalities       supported input/output modalities (nullable — informational)
	 * @param interleaved      interleaved reasoning support (nullable — absent means unsupported)
	 */
	public record ModelCapabilities(
			boolean reasoning,
			boolean toolCall,
			boolean temperature,
			boolean attachment,
			boolean structuredOutput,
			ModelModalities modalities,
			InterleavedConfig interleaved) {}

	/**
	 * Interleaved reasoning configuration.
	 *
	 * <p>
	 * When a model supports interleaved reasoning, it can return reasoning tokens alongside
	 * regular output. Some models require the reasoning to be passed back via a specific field
	 * name (e.g. {@code "reasoning_content"} or {@code "reasoning_details"}).
	 * </p>
	 *
	 * @param field the provider-specific field name for reasoning content (nullable — when
	 *              {@code null}, the model supports interleaving without a special field)
	 */
	public record InterleavedConfig(String field) {}

	/**
	 * Input/output modality support.
	 *
	 * @param input  supported input types (e.g. "text", "image", "pdf")
	 * @param output supported output types (e.g. "text")
	 */
	public record ModelModalities(List<String> input, List<String> output) {
		/** */
		public ModelModalities {
			input = List.copyOf(input);
			output = List.copyOf(output);
		}
	}

	/**
	 * Per-million-token pricing (informational).
	 *
	 * @param input      cost per million input tokens
	 * @param output     cost per million output tokens
	 * @param cacheRead  cost per million cached-read tokens (nullable)
	 * @param cacheWrite cost per million cached-write tokens (nullable)
	 * @param reasoning  cost per million reasoning tokens (nullable)
	 * @param inputAudio  cost per million audio input tokens (nullable)
	 * @param outputAudio cost per million audio output tokens (nullable)
	 * @param tiers       the price lists for long prompts, sorted by their
	 *                    {@link TieredCost#threshold() thresholds}; empty when the prices do not
	 *                    depend on the prompt size. {@code null} counts as empty.
	 */
	public record ModelCost(
			double input,
			double output,
			Double cacheRead,
			Double cacheWrite,
			Double reasoning,
			Double inputAudio,
			Double outputAudio,
			List<TieredCost> tiers) {

		/**
		 * Creates a {@link ModelCost}, sorting the tiers by their thresholds.
		 */
		public ModelCost {
			tiers = tiers == null ? List.of()
				: tiers.stream().sorted(Comparator.comparingInt(TieredCost::threshold)).toList();
		}

		/**
		 * The price list a request with the given prompt size is charged at.
		 *
		 * @param promptTokens
		 *        The number of tokens of the request's prompt.
		 * @return The tier with the highest {@link TieredCost#threshold()} the prompt exceeds, or
		 *         {@code null} when it exceeds none, so the base prices apply.
		 */
		public TieredCost tierFor(int promptTokens) {
			TieredCost result = null;
			for (TieredCost tier : tiers) {
				if (promptTokens <= tier.threshold()) {
					break;
				}
				result = tier;
			}
			return result;
		}
	}

	/**
	 * Pricing that applies to a whole request when its prompt exceeds a threshold.
	 *
	 * @param threshold  the number of prompt tokens a request must exceed for these prices to apply
	 * @param input      cost per million input tokens
	 * @param output     cost per million output tokens
	 * @param cacheRead  cost per million cached-read tokens (nullable)
	 * @param cacheWrite cost per million cached-write tokens (nullable)
	 */
	public record TieredCost(int threshold, double input, double output, Double cacheRead, Double cacheWrite) {}

	/**
	 * Temporal metadata about a model.
	 *
	 * @param knowledgeCutoff training data cutoff date (nullable, e.g. "2026-01-31")
	 * @param releaseDate     model release date (nullable)
	 * @param lastUpdated     last update date (nullable)
	 */
	public record ModelDates(String knowledgeCutoff, String releaseDate, String lastUpdated) {}
}
