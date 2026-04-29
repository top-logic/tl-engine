/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

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
 */
public record ProviderRequest(
		String model,
		String systemPrompt,
		List<Message> messages,
		List<ToolSchema> tools,
		int maxOutputTokens,
		Map<String, Object> modelOptions,
		ResponseFormat responseFormat,
		ToolChoice toolChoice) {

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
	}

	/**
	 * Convenience constructor without {@code responseFormat} and {@code toolChoice}.
	 */
	public ProviderRequest(String model, String systemPrompt, List<Message> messages,
			List<ToolSchema> tools, int maxOutputTokens, Map<String, Object> modelOptions) {
		this(model, systemPrompt, messages, tools, maxOutputTokens, modelOptions, null, null);
	}

	/**
	 * Convenience constructor without {@code modelOptions}, {@code responseFormat}, and
	 * {@code toolChoice}.
	 */
	public ProviderRequest(String model, String systemPrompt, List<Message> messages,
			List<ToolSchema> tools, int maxOutputTokens) {
		this(model, systemPrompt, messages, tools, maxOutputTokens, Map.of(), null, null);
	}
}
