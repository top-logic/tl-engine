/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tool;

import java.util.Map;

import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.provider.ToolSchema;

/**
 * A tool that the agent can invoke.
 *
 * <p>Every tool in the system implements this interface — whether created from
 * {@link ToolMethod} annotations, script configuration, or programmatically.</p>
 *
 * @see ToolRegistry
 * @see ToolMode
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
	 * @param args    deserialized arguments from the LLM (JSON object → Map)
	 * @param context execution context (security, transaction, progress)
	 * @return the tool's result as a string
	 */
	String execute(Map<String, Object> args, ToolContext context);

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
	 *
	 * <p>Defaults to {@code true} for {@link ToolMode#READ} and {@link ToolMode#USER_SCOPED},
	 * {@code false} for {@link ToolMode#GLOBAL}.</p>
	 */
	default boolean isConcurrencySafe() {
		return mode() != ToolMode.GLOBAL;
	}

}
