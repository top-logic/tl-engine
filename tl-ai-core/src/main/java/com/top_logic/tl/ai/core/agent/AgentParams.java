/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.provider.ResponseFormat;
import com.top_logic.tl.ai.core.provider.ToolChoice;

/**
 * Immutable parameters for an {@link AgentLoop} run.
 *
 * @param model               the model identifier (e.g. "claude-sonnet-4-20250514")
 * @param systemPrompt        system prompt, or {@code null} for none
 * @param maxTurns            maximum LLM call iterations before stopping (safety cap)
 * @param maxOutputTokens     maximum tokens per LLM response
 * @param modelOptions        provider-specific options (temperature, {@code topP},
 *                            {@code thinking_effort}, etc.)
 * @param contextWindowTokens total context window size in tokens (used for memory/compaction)
 * @param maxRetries          automatic retry attempts on transient provider errors (rate
 *                            limited, overloaded) before the run fails or moves on to a fallback
 *                            model of its failover chain; a rate-limited run with no fallback left
 *                            retries for up to {@link AgentTuning#DEFAULT_RATE_LIMIT_WAIT_SECONDS
 *                            the rate-limit wait} instead; {@code 0} disables retries
 * @param timeoutSeconds      HTTP request timeout per LLM call in seconds
 * @param toolChoice          tool selection behaviour, or {@code null} for provider default
 * @param responseValidator   predicate tested after each LLM response; returning {@code false}
 *                            stops the loop with {@link StopReason#REJECTED}
 * @param environment         the composed {@code <environment>} block that is part of
 *                            {@link #systemPrompt()}, kept separately so a run can show an
 *                            administrator what it told the model about the situation;
 *                            {@code null} for a run that composed none
 * @param turnContext         the composed {@code <turn-context>} block this run's turn carries
 *                            beside the user's text - the facts that change while the conversation
 *                            runs, which must stay out of {@link #systemPrompt()};
 *                            {@code null} for a run whose host stated none
 * @param responseFormat      the format every response of this run must have, or {@code null} for
 *                            the provider default; a provider that cannot honour it refuses the
 *                            request rather than answering in another shape
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
		int maxRetries,
		int timeoutSeconds,
		ToolChoice toolChoice,
		Predicate<AssistantMessage> responseValidator,
		String environment,
		String turnContext,
		ResponseFormat responseFormat) {

	/**
	 * The same parameters, but for another model.
	 *
	 * <p>
	 * What a run keeps when it fails over to another model (Gitea #344): its prompt, its turn
	 * budget, its validator, its timeouts - everything that describes the <em>work</em>. What it
	 * takes from the target instead are the three things that are properties of the model rather
	 * than of the work: its name, the limits it accepts, and the sampling options its provider
	 * understands.
	 * </p>
	 *
	 * @param model
	 *        The model to continue on; must not be <code>null</code>.
	 * @param maxOutputTokens
	 *        What to ask the target for at most.
	 * @param contextWindowTokens
	 *        The target's context window.
	 * @param modelOptions
	 *        The options rebuilt for the target's provider; see
	 *        {@link com.top_logic.tl.ai.core.provider.health.ModelOptions}.
	 */
	public AgentParams withModel(String model, int maxOutputTokens, int contextWindowTokens,
			Map<String, Object> modelOptions) {
		return new AgentParams(model, systemPrompt, maxTurns, maxOutputTokens, modelOptions,
			contextWindowTokens, maxRetries, timeoutSeconds, toolChoice, responseValidator,
			environment, turnContext, responseFormat);
	}

	/** Default maximum turns. */
	public static final int DEFAULT_MAX_TURNS = 25;

	/** Default maximum output tokens per LLM call. */
	public static final int DEFAULT_MAX_OUTPUT_TOKENS = 8192;

	/** Default context window size (used when catalog is not available). */
	public static final int DEFAULT_CONTEXT_WINDOW_TOKENS = 200_000;

	/** Default maximum retries on transient errors. */
	public static final int DEFAULT_MAX_RETRIES = 3;

	/** Default request timeout in seconds. */
	public static final int DEFAULT_TIMEOUT_SECONDS = 120;

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
		if (maxRetries < 0) {
			throw new IllegalArgumentException("maxRetries must not be negative.");
		}
		if (timeoutSeconds <= 0) {
			throw new IllegalArgumentException("timeoutSeconds must be positive.");
		}
	}

	/**
	 * Parameters of a run that states no response format - every caller but the {@code aiChat}
	 * TL-Script function, which is the one entry point that lets its caller ask for one.
	 */
	public AgentParams(String model, String systemPrompt, int maxTurns,
			int maxOutputTokens, Map<String, Object> modelOptions,
			int contextWindowTokens, int maxRetries, int timeoutSeconds,
			ToolChoice toolChoice, Predicate<AssistantMessage> responseValidator,
			String environment, String turnContext) {
		this(model, systemPrompt, maxTurns, maxOutputTokens, modelOptions, contextWindowTokens,
			maxRetries, timeoutSeconds, toolChoice, responseValidator, environment, turnContext, null);
	}

	/**
	 * Parameters of a run that composed neither block - every caller that builds an
	 * {@link AgentLoop} directly rather than through
	 * {@link AgentFactory#buildConfig(AgentDefinition, com.top_logic.tl.ai.core.provider.AgentModelAssignment, AgentCapabilities)}.
	 */
	public AgentParams(String model, String systemPrompt, int maxTurns,
			int maxOutputTokens, Map<String, Object> modelOptions,
			int contextWindowTokens, int maxRetries, int timeoutSeconds,
			ToolChoice toolChoice, Predicate<AssistantMessage> responseValidator) {
		this(model, systemPrompt, maxTurns, maxOutputTokens, modelOptions, contextWindowTokens,
			maxRetries, timeoutSeconds, toolChoice, responseValidator, null, null, null);
	}

	/**
	 * Parameters of a run that composed an environment block but no per-turn context block.
	 */
	public AgentParams(String model, String systemPrompt, int maxTurns,
			int maxOutputTokens, Map<String, Object> modelOptions,
			int contextWindowTokens, int maxRetries, int timeoutSeconds,
			ToolChoice toolChoice, Predicate<AssistantMessage> responseValidator,
			String environment) {
		this(model, systemPrompt, maxTurns, maxOutputTokens, modelOptions, contextWindowTokens,
			maxRetries, timeoutSeconds, toolChoice, responseValidator, environment, null, null);
	}

	/**
	 * Convenience constructor without model options or response validator.
	 */
	public AgentParams(String model, String systemPrompt, int maxTurns, int maxOutputTokens) {
		this(model, systemPrompt, maxTurns, maxOutputTokens, Map.of(),
			DEFAULT_CONTEXT_WINDOW_TOKENS, DEFAULT_MAX_RETRIES, DEFAULT_TIMEOUT_SECONDS,
			null, msg -> true);
	}

	/**
	 * Convenience constructor without response validator.
	 */
	public AgentParams(String model, String systemPrompt, int maxTurns,
			int maxOutputTokens, Map<String, Object> modelOptions) {
		this(model, systemPrompt, maxTurns, maxOutputTokens, modelOptions,
			DEFAULT_CONTEXT_WINDOW_TOKENS, DEFAULT_MAX_RETRIES, DEFAULT_TIMEOUT_SECONDS,
			null, msg -> true);
	}

	/**
	 * Convenience constructor with model options and a response format, defaults for the rest.
	 */
	public AgentParams(String model, String systemPrompt, int maxTurns,
			int maxOutputTokens, Map<String, Object> modelOptions, ResponseFormat responseFormat) {
		this(model, systemPrompt, maxTurns, maxOutputTokens, modelOptions,
			DEFAULT_CONTEXT_WINDOW_TOKENS, DEFAULT_MAX_RETRIES, DEFAULT_TIMEOUT_SECONDS,
			null, msg -> true, null, null, responseFormat);
	}

	/**
	 * Convenience constructor with model options and response validator but defaults for the rest.
	 */
	public AgentParams(String model, String systemPrompt, int maxTurns,
			int maxOutputTokens, Map<String, Object> modelOptions,
			Predicate<AssistantMessage> responseValidator) {
		this(model, systemPrompt, maxTurns, maxOutputTokens, modelOptions,
			DEFAULT_CONTEXT_WINDOW_TOKENS, DEFAULT_MAX_RETRIES, DEFAULT_TIMEOUT_SECONDS,
			null, responseValidator);
	}

	/**
	 * Convenience constructor with context window but defaults for retries/timeout/{@code toolChoice}.
	 */
	public AgentParams(String model, String systemPrompt, int maxTurns,
			int maxOutputTokens, Map<String, Object> modelOptions,
			int contextWindowTokens, Predicate<AssistantMessage> responseValidator) {
		this(model, systemPrompt, maxTurns, maxOutputTokens, modelOptions,
			contextWindowTokens, DEFAULT_MAX_RETRIES, DEFAULT_TIMEOUT_SECONDS,
			null, responseValidator);
	}
}
