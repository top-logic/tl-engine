/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.tool.api;

import java.util.List;
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

	/**
	 * The tool group this tool declares for itself - the unit an administrator grants to a security
	 * role.
	 *
	 * <p>
	 * The empty string means ungrouped, which is the fail-closed default: with deny-by-default tool
	 * authorization, a tool that declares no group cannot slip into an existing grant by accident and
	 * is reachable only through an explicit per-tool grant. Tools that arrive at runtime and have no
	 * annotation to carry a group - remote MCP tools above all - are ungrouped by construction and
	 * are classified in the access policy instead.
	 * </p>
	 *
	 * @see ToolMethod#group()
	 */
	default String group() {
		return "";
	}

	/**
	 * The qualified names of the model types this tool declares it works on.
	 *
	 * <p>
	 * Empty means "makes no claim", which is the default and costs nothing: the tool is offered and
	 * runs, and what it may touch is decided per object as it touches it. A non-empty declaration is
	 * checked before the tool runs - the acting principal must have READ access to at least one of
	 * the declared types, or the call is refused and the tool is left out of the model's tool
	 * schemas.
	 * </p>
	 *
	 * <p>
	 * Tools that arrive at runtime and have no annotation to carry a declaration - TL-Script tools
	 * and remote MCP tools - declare nothing and are therefore never refused on this ground.
	 * </p>
	 *
	 * @see ToolMethod#types()
	 */
	default List<String> types() {
		return List.of();
	}

}
