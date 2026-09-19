/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.tool.api;

import com.top_logic.model.TLObject;

/**
 * Execution context available to all tools, regardless of which module defines them.
 *
 * <p>Tools in {@code tl-ai-core} may downcast to the full {@code ToolContext} for
 * access to security, transactions, and approval. Tools in external modules use
 * only this interface.</p>
 */
public interface ToolExecutionContext {

	/**
	 * Report progress to the UI during long-running operations.
	 */
	ProgressReporter progressReporter();

	/**
	 * Token for cooperative cancellation. Tools should check this periodically
	 * during long-running operations.
	 */
	CancellationToken cancellationToken();

	/**
	 * Which agent run this tool call belongs to - run id, conversation, agent, model, and the
	 * delegating run for a sub-agent.
	 *
	 * <p>
	 * Never {@code null}: a tool called outside an agent run gets {@link RunInfo#NONE}, whose fields
	 * are all {@code null}. Reading any of them is always safe, which is the whole point - the run's
	 * persistent {@code AgentExecution} row cannot be dereferenced from a tool (Gitea #405), so this
	 * hands over the same facts as plain data captured before the tool call.
	 * </p>
	 */
	default RunInfo runInfo() {
		return RunInfo.NONE;
	}

	/**
	 * The model object the given identifier names, if the run's principal may read it.
	 *
	 * <p>
	 * What turns the {@code "TypeName:id"} a language model wrote back into an object, and the point
	 * at which that read is checked. A tool declaring a {@link TLObject} parameter needs neither: the
	 * framework resolves the argument through this method before the tool method is invoked, so the
	 * check cannot be forgotten in one tool out of sixteen (Gitea #381).
	 * </p>
	 *
	 * <p>
	 * An object the principal may not read is reported exactly like one that does not exist - saying
	 * "it exists and you may not see it" would disclose the very thing the read check withholds.
	 * </p>
	 *
	 * <p>
	 * There is no permissive default: a context that cannot name the principal it serves would have
	 * to resolve everything or nothing, and either answer would be wrong, so it refuses. Every
	 * context of an agent run answers.
	 * </p>
	 *
	 * @param id
	 *        The identifier, in the form a tool result reports it. Never <code>null</code>.
	 * @return The object; never <code>null</code>.
	 * @throws IllegalArgumentException
	 *         If the identifier is malformed, names no object, or names one the principal may not
	 *         read.
	 * @throws IllegalStateException
	 *         If this context belongs to no run and therefore names no principal.
	 */
	default TLObject resolveObject(String id) {
		throw new IllegalStateException("Resolving an object identifier is a read check, and this"
			+ " context names no principal to check it for. Execute the tool with the context of an"
			+ " agent run.");
	}
}
