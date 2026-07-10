/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.tool.api;

import java.util.Map;

/**
 * A tool that the agent can invoke.
 */
public interface Tool {

	/**
	 * The unique name identifying this tool to the LLM.
	 */
	String name();

	/**
	 * Human-readable description of what this tool does, shown to the LLM.
	 */
	String description();

	/**
	 * The JSON schema describing the arguments this tool accepts.
	 */
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
	 * <p>Defaults to {@link ToolMode#STRUCTURAL} (fail-closed). Tool authors
	 * opt in to less restrictive modes explicitly.</p>
	 */
	default ToolMode mode() {
		return ToolMode.STRUCTURAL;
	}

	/**
	 * Whether this tool can safely run in parallel with other concurrency-safe tools.
	 *
	 * <p>Defaults to {@code true}. With locking in place, most tools are safe to
	 * run concurrently. Override to {@code false} only for tools with external
	 * concurrency constraints (e.g. single-connection external systems).</p>
	 */
	default boolean isConcurrencySafe() {
		return true;
	}

}
