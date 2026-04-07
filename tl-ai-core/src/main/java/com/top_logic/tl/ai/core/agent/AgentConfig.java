/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.List;
import java.util.Objects;

import com.top_logic.tl.ai.core.provider.ToolSchema;

/**
 * Immutable configuration for an {@link AgentLoop} run.
 *
 * @param model           the model identifier (e.g. "claude-sonnet-4-20250514")
 * @param systemPrompt    system prompt, or {@code null} for none
 * @param tools           tool schemas the LLM may call (empty list for no tools)
 * @param maxTurns        maximum LLM call iterations before stopping (safety cap)
 * @param maxOutputTokens maximum tokens per LLM response
 *
 * @see AgentLoop
 */
public record AgentConfig(
		String model,
		String systemPrompt,
		List<ToolSchema> tools,
		int maxTurns,
		int maxOutputTokens) {

	/** Default maximum turns. */
	public static final int DEFAULT_MAX_TURNS = 25;

	/** Default maximum output tokens per LLM call. */
	public static final int DEFAULT_MAX_OUTPUT_TOKENS = 8192;

	/** */
	public AgentConfig {
		Objects.requireNonNull(model, "Model must not be null.");
		Objects.requireNonNull(tools, "Tools must not be null.");
		tools = List.copyOf(tools);
		if (maxTurns <= 0) {
			throw new IllegalArgumentException("maxTurns must be positive.");
		}
		if (maxOutputTokens <= 0) {
			throw new IllegalArgumentException("maxOutputTokens must be positive.");
		}
	}
}
