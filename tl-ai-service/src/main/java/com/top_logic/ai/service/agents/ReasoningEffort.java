/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents;

/**
 * Reasoning effort levels supported by OpenAI reasoning models (o-series, GPT-5).
 *
 * <p>
 * This is a superset of all known values across model families. Not every model supports every
 * level; the available options are filtered dynamically based on the selected model.
 * </p>
 *
 * @see AgentConfig#getReasoningEffort()
 */
public enum ReasoningEffort {

	/** No reasoning. */
	NONE("none"),

	/** Minimal reasoning effort (GPT-5 family). */
	MINIMAL("minimal"),

	/** Low reasoning effort. */
	LOW("low"),

	/** Medium reasoning effort (default for most reasoning models). */
	MEDIUM("medium"),

	/** High reasoning effort. */
	HIGH("high"),

	/** Extra-high reasoning effort (GPT-5.1-codex-max, GPT-5.2). */
	XHIGH("xhigh");

	private final String _value;

	ReasoningEffort(String value) {
		_value = value;
	}

	/**
	 * Returns the API-compatible string value (e.g. {@code "medium"}).
	 */
	public String getValue() {
		return _value;
	}
}
