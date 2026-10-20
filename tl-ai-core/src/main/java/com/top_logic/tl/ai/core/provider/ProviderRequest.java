/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * A request to send to an {@link LLMProvider}.
 *
 * @param model           the model identifier (e.g. "claude-haiku-4-5-20251001")
 * @param systemPrompt    system prompt, or {@code null} for none
 * @param messages        conversation history
 * @param tools           tool definitions the LLM may call (empty list for none)
 * @param maxOutputTokens maximum tokens in the response
 * @param modelOptions    provider-specific options (temperature, {@code topP},
 *                        {@code thinkingBudgetTokens}, etc.)
 * @param responseFormat  desired response format, or {@code null} for provider default
 * @param toolChoice      tool selection behaviour, or {@code null} for provider default
 * @param timeout         HTTP request timeout, or {@code null} for the provider default
 * @param stableToolCount how many leading entries of {@code tools} are the same on every request
 *                        of this conversation. The prompt-cache boundary of the tool array
 *                        (Gitea #346): everything behind it is a skill the run loaded and can
 *                        still appear or disappear, so a provider that places explicit cache
 *                        breakpoints places one here. {@code 0} means "make no promise", which is
 *                        what every caller that does not know says.
 */
public record ProviderRequest(
		String model,
		String systemPrompt,
		List<Message> messages,
		List<ToolSchema> tools,
		int maxOutputTokens,
		Map<String, Object> modelOptions,
		ResponseFormat responseFormat,
		ToolChoice toolChoice,
		Duration timeout,
		int stableToolCount) {

	/** */
	public ProviderRequest {
		Objects.requireNonNull(model, "Model must not be null.");
		Objects.requireNonNull(messages, "Messages must not be null.");
		Objects.requireNonNull(tools, "Tools must not be null.");
		Objects.requireNonNull(modelOptions, "Model options must not be null.");
		messages = List.copyOf(messages);
		tools = List.copyOf(tools);
		modelOptions = Map.copyOf(modelOptions);
		if (maxOutputTokens <= 0) {
			throw new IllegalArgumentException("maxOutputTokens must be positive.");
		}
		if (timeout != null && (timeout.isZero() || timeout.isNegative())) {
			throw new IllegalArgumentException("timeout must be positive.");
		}
		if (stableToolCount < 0 || stableToolCount > tools.size()) {
			throw new IllegalArgumentException(
				"stableToolCount must name a prefix of the tool list, was " + stableToolCount
					+ " of " + tools.size() + ".");
		}
	}

	/**
	 * Convenience constructor for a caller that makes no promise about the tool array.
	 */
	public ProviderRequest(String model, String systemPrompt, List<Message> messages,
			List<ToolSchema> tools, int maxOutputTokens, Map<String, Object> modelOptions,
			ResponseFormat responseFormat, ToolChoice toolChoice, Duration timeout) {
		this(model, systemPrompt, messages, tools, maxOutputTokens, modelOptions,
			responseFormat, toolChoice, timeout, 0);
	}

	/**
	 * Convenience constructor without {@code timeout}.
	 */
	public ProviderRequest(String model, String systemPrompt, List<Message> messages,
			List<ToolSchema> tools, int maxOutputTokens, Map<String, Object> modelOptions,
			ResponseFormat responseFormat, ToolChoice toolChoice) {
		this(model, systemPrompt, messages, tools, maxOutputTokens, modelOptions,
			responseFormat, toolChoice, null, 0);
	}

	/**
	 * Convenience constructor without {@code responseFormat} and {@code toolChoice}.
	 */
	public ProviderRequest(String model, String systemPrompt, List<Message> messages,
			List<ToolSchema> tools, int maxOutputTokens, Map<String, Object> modelOptions) {
		this(model, systemPrompt, messages, tools, maxOutputTokens, modelOptions, null, null, null, 0);
	}

	/**
	 * Convenience constructor without {@code modelOptions}, {@code responseFormat}, and
	 * {@code toolChoice}.
	 */
	public ProviderRequest(String model, String systemPrompt, List<Message> messages,
			List<ToolSchema> tools, int maxOutputTokens) {
		this(model, systemPrompt, messages, tools, maxOutputTokens, Map.of(), null, null, null, 0);
	}
}
