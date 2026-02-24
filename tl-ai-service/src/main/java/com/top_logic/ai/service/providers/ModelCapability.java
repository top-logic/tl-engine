/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.providers;

/**
 * Capabilities that an LLM model may support, beyond basic text generation.
 *
 * <p>
 * Used by {@link com.top_logic.ai.service.ChatModelFactory#getModelCapabilities(String)} to report
 * per-model capabilities. The agent configuration UI uses these to control field visibility and
 * validate parameter combinations.
 * </p>
 *
 * @see com.top_logic.ai.service.ChatModelFactory#getModelCapabilities(String)
 */
public enum ModelCapability {

	/**
	 * Supports explicit extended thinking with a token budget.
	 *
	 * <p>
	 * Anthropic {@code thinking: {type: "enabled", budget_tokens: N}}.
	 * </p>
	 */
	THINKING_ENABLED,

	/**
	 * Supports adaptive thinking where the model decides when and how much to think.
	 *
	 * <p>
	 * Anthropic {@code thinking: {type: "adaptive"}}. Only supported by newer Claude models
	 * (Opus 4.6, Sonnet 4.6).
	 * </p>
	 */
	THINKING_ADAPTIVE,

	/**
	 * Supports the reasoning effort parameter.
	 *
	 * <p>
	 * OpenAI {@code reasoning_effort} for o-series and GPT-5 models.
	 * </p>
	 */
	REASONING_EFFORT;
}
