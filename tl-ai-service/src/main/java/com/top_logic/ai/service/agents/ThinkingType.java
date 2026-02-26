/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents;

/**
 * Extended thinking modes supported by LLM providers (currently Anthropic).
 *
 * @see AgentConfig#getThinkingType()
 */
public enum ThinkingType {

	/**
	 * The model decides autonomously when and how much to think.
	 */
	ADAPTIVE("adaptive"),

	/**
	 * The model always uses extended thinking. Requires
	 * {@link AgentConfig#getThinkingBudgetTokens()} to be set.
	 */
	ENABLED("enabled");

	private final String _value;

	ThinkingType(String value) {
		_value = value;
	}

	/**
	 * Returns the SDK-compatible string value (e.g. {@code "adaptive"}).
	 */
	public String getValue() {
		return _value;
	}
}
