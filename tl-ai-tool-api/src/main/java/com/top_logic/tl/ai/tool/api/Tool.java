/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
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
	 * What kind of state change the given call of this tool makes.
	 *
	 * <p>
	 * {@link #mode()} answers for every call. A tool whose arguments can make a call change nothing -
	 * the dry run of a write - answers {@link ToolMode#READ} here for that call, so that it runs
	 * without an approval prompt and outside the turn's transaction. Every other call keeps
	 * {@link #mode()}: the answer is one of the two and never a third mode, because the access policy,
	 * the background allowlist and the hints an MCP client is given are decided by {@link #mode()}.
	 * </p>
	 *
	 * @param arguments
	 *        The call's arguments, as the model sent them.
	 * @return {@link #mode()}, or {@link ToolMode#READ} for a call that changes nothing.
	 * @see ToolMethod#dryRun()
	 */
	default ToolMode mode(Map<String, Object> arguments) {
		return mode();
	}

	/**
	 * Why the given call is refused before it runs, or <code>null</code> to let it run.
	 *
	 * <p>
	 * Asked after the access policy admitted the call and before anybody is asked to approve it, so
	 * that a call that cannot succeed - an argument the tool rejects, a capability the account lacks -
	 * is answered without an approval prompt the user would answer in vain. A refused call never
	 * runs, so its failure leaves the other changes of the turn standing.
	 * </p>
	 *
	 * <p>
	 * An early answer and never the only one: {@link #execute(Map, ToolExecutionContext)} refuses the
	 * same calls itself, because not every caller asks this first.
	 * </p>
	 *
	 * @param arguments
	 *        The call's arguments, as the model sent them.
	 * @param context
	 *        The context the call would run with.
	 * @return The refusal, phrased for the model, or <code>null</code>.
	 * @see ToolMethod#check()
	 */
	default String refusal(Map<String, Object> arguments, ToolExecutionContext context) {
		return null;
	}

	/**
	 * Why this tool can do nothing in the given run, or <code>null</code> when it can.
	 *
	 * <p>
	 * {@link #refusal(Map, ToolExecutionContext)} answers for one call; this answers for the run, before
	 * any call is made. A tool that answers is left out of the tool schemas the model is sent, because a
	 * tool the model can only be refused by is an invitation to a wasted call - and a call that names it
	 * anyway is answered with this reason, without the tool running. Nothing is withheld from the user
	 * here, which is the difference to a permission: the run lacks what the tool needs, such as a page
	 * that has a place to show something on.
	 * </p>
	 *
	 * <p>
	 * Asked for every request of the run, so it has to be cheap. Its answer is part of the tool array
	 * every provider caches by prefix, so it should change only with what the run is attached to, not
	 * from one request to the next.
	 * </p>
	 *
	 * @param context
	 *        The run the tool would be offered to.
	 * @return The reason, phrased for the model, or <code>null</code>.
	 * @see ToolMethod#unavailable()
	 */
	default String unavailableReason(ToolExecutionContext context) {
		return null;
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
