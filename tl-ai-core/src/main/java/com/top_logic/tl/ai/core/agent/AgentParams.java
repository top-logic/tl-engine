/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import com.top_logic.tl.ai.core.message.AssistantMessage;

/**
 * Immutable parameters for an {@link AgentLoop} run.
 *
 * @param model               the model identifier (e.g. "claude-sonnet-4-20250514")
 * @param systemPrompt        system prompt, or {@code null} for none
 * @param maxTurns            maximum LLM call iterations before stopping (safety cap)
 * @param maxOutputTokens     maximum tokens per LLM response
 * @param modelOptions        provider-specific options (temperature, {@code topP},
 *                            {@code thinkingBudgetTokens}, etc.)
 * @param contextWindowTokens total context window size in tokens (used for memory/compaction)
 * @param responseValidator   predicate tested after each LLM response; returning {@code false}
 *                            stops the loop with {@link StopReason#REJECTED}
 *
 * @see AgentLoop
 */
public record AgentParams(
		String model,
		String systemPrompt,
		int maxTurns,
		int maxOutputTokens,
		Map<String, Object> modelOptions,
		int contextWindowTokens,
		Predicate<AssistantMessage> responseValidator) {

	/** Default maximum turns. */
	public static final int DEFAULT_MAX_TURNS = 25;

	/** Default maximum output tokens per LLM call. */
	public static final int DEFAULT_MAX_OUTPUT_TOKENS = 8192;

	/** Default context window size (used when catalog is not available). */
	public static final int DEFAULT_CONTEXT_WINDOW_TOKENS = 200_000;

	/** */
	public AgentParams {
		Objects.requireNonNull(model, "Model must not be null.");
		Objects.requireNonNull(modelOptions, "Model options must not be null.");
		Objects.requireNonNull(responseValidator, "Response validator must not be null.");
		modelOptions = Map.copyOf(modelOptions);
		if (maxTurns <= 0) {
			throw new IllegalArgumentException("maxTurns must be positive.");
		}
		if (maxOutputTokens <= 0) {
			throw new IllegalArgumentException("maxOutputTokens must be positive.");
		}
		if (contextWindowTokens <= 0) {
			throw new IllegalArgumentException("contextWindowTokens must be positive.");
		}
	}

	/**
	 * Convenience constructor without model options or response validator.
	 */
	public AgentParams(String model, String systemPrompt, int maxTurns, int maxOutputTokens) {
		this(model, systemPrompt, maxTurns, maxOutputTokens, Map.of(),
			DEFAULT_CONTEXT_WINDOW_TOKENS, msg -> true);
	}

	/**
	 * Convenience constructor without response validator.
	 */
	public AgentParams(String model, String systemPrompt, int maxTurns,
			int maxOutputTokens, Map<String, Object> modelOptions) {
		this(model, systemPrompt, maxTurns, maxOutputTokens, modelOptions,
			DEFAULT_CONTEXT_WINDOW_TOKENS, msg -> true);
	}

	/**
	 * Convenience constructor with model options and response validator but default context window.
	 */
	public AgentParams(String model, String systemPrompt, int maxTurns,
			int maxOutputTokens, Map<String, Object> modelOptions,
			Predicate<AssistantMessage> responseValidator) {
		this(model, systemPrompt, maxTurns, maxOutputTokens, modelOptions,
			DEFAULT_CONTEXT_WINDOW_TOKENS, responseValidator);
	}
}
