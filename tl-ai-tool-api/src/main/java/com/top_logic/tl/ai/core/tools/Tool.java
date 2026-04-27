/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools;

import java.util.Map;

import com.top_logic.tl.ai.core.provider.ToolSchema;

/**
 * A tool that the agent can invoke.
 */
public interface Tool {

	/** */
	String name();

	/** */
	String description();

	/** */
	ToolSchema schema();

	/**
	 * Execute the tool with the given arguments.
	 *
	 * @param args    deserialized arguments from the LLM (JSON object -> Map)
	 * @param context execution context
	 * @return a {@link ToolResult} carrying the result text and optional metadata
	 */
	ToolResult execute(Map<String, Object> args, ToolExecutionContext context);

	/**
	 * What kind of state change this tool makes.
	 *
	 * <p>Defaults to {@link ToolMode#GLOBAL} (fail-closed). Tool authors
	 * opt in to less restrictive modes explicitly.</p>
	 */
	default ToolMode mode() {
		return ToolMode.GLOBAL;
	}

	/**
	 * Whether this tool can safely run in parallel with other concurrency-safe tools.
	 */
	default boolean isConcurrencySafe() {
		return mode() != ToolMode.GLOBAL;
	}

}
