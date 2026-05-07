/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

/**
 * All tuning parameters for an agent's model assignment, resolved from
 * {@link LLMService}.
 *
 * <p>
 * Every field except {@link #model()} is nullable — {@code null} means "use default". The agent
 * definition only carries identity and capabilities; all operational tuning lives here.
 * </p>
 *
 * @param model              the resolved model identifier (never {@code null})
 * @param maxTokens          max output tokens per LLM call, or {@code null} for catalog default
 * @param contextWindow      context window size override, or {@code null} for catalog default
 * @param maxTurns           max agent loop turns, or {@code null} for default
 * @param temperature        temperature, or {@code null} for default
 * @param topP               top-p sampling, or {@code null} for default
 * @param thinkingEffort     Anthropic thinking effort (low/medium/high/max), or {@code null}
 * @param topK               Anthropic top-k, or {@code null}
 * @param reasoningEffort    OpenAI/xAI reasoning effort (low/medium/high), or {@code null}
 * @param reasoningSummary   OpenAI/xAI reasoning summary (auto/detailed), or {@code null}
 * @param toolChoice         tool choice (AUTO/REQUIRED/NONE), or {@code null} for provider default
 * @param timeoutSeconds     request timeout, or {@code null} for default
 * @param maxRetries         max retries on transient errors, or {@code null} for default
 * @param maxToolResultChars max chars per tool result, or {@code null} for default
 *
 * @see LLMService#resolveModelAssignment(String)
 */
public record AgentModelAssignment(
		String model,
		Integer maxTokens,
		Integer contextWindow,
		Integer maxTurns,
		Double temperature,
		Double topP,
		String thinkingEffort,
		Integer topK,
		String reasoningEffort,
		String reasoningSummary,
		String toolChoice,
		Integer timeoutSeconds,
		Integer maxRetries,
		Integer maxToolResultChars) {
}
