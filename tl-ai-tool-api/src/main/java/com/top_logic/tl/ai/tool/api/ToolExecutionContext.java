/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.tool.api;

import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.merge.MergeConflictException;
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

	/**
	 * Whether a human is present in this run who could be asked a question.
	 *
	 * <p>
	 * {@code false} for a run nobody is watching - a scheduled {@code AgentTask}, a request through
	 * the MCP endpoint - and {@code true} for every run started from a chat or a script, where the
	 * account that started it is there to answer. The default is {@code true}: a context that says
	 * nothing about this is an attended one.
	 * </p>
	 *
	 * <p>
	 * A tool that would otherwise park its thread waiting for an answer asks here first and fails
	 * with an error the model can act on instead. Without it an unattended run does not fail - it
	 * waits out the full interaction timeout and then reports a timeout, which is 30 minutes of a
	 * scheduler thread spent on a question nobody was ever going to read.
	 * </p>
	 *
	 * <p>
	 * This is not a security decision and nothing is authorized by it; what an unattended run may
	 * <em>do</em> is decided by its security context's background ceiling.
	 * </p>
	 */
	default boolean attended() {
		return true;
	}

	/**
	 * Puts a source this call read on the run's reading list, so that the answer may point at it.
	 *
	 * <p>
	 * An answer cites by writing a link to a source into its text, and the framework keeps the link
	 * as a footnote only when the source is on the reading list of the run - or of a sub-agent whose
	 * result the run received. The framework registers what its own tools read: every object a read
	 * tool shows the model, every page {@code webFetch} or the browser read. A tool of an application
	 * that reads something of its own - a record found by its code, a document section - registers
	 * it here, with the kind the application declared for it and the reference its answer will write.
	 * </p>
	 *
	 * <p>
	 * The default does nothing: a context outside an agent run has no reading list, and a tool must
	 * not fail for registering what it read.
	 * </p>
	 *
	 * @param source
	 *        What was read. Must not be <code>null</code>.
	 */
	default void readSource(ReadSource source) {
		// Outside an agent run there is nothing to cite.
	}

	/**
	 * Lets this call read what other threads committed since its turn began - the helpers it started
	 * and waited for, most of all.
	 *
	 * <p>
	 * The calls of one agent turn read one state of the database: the framework moves the run's view
	 * to the newest commit before the turn's first call, and not again until the next turn (Gitea
	 * #647). A tool that starts work on other threads and waits for it - helpers that write and commit
	 * in transactions of their own - therefore reads, after the wait, what was there before the
	 * helpers wrote. And a change it then makes to what they wrote is a change of an outdated version,
	 * which fails when the turn commits. Such a tool calls this once the work it waited for is done. A
	 * tool that waits for no other thread does not need it, and neither does the next turn, which reads
	 * the newer state anyway.
	 * </p>
	 *
	 * <p>
	 * What the turn has changed and not yet committed is kept: the newer commits are merged into it. A
	 * commit that changed an object the turn has changed as well is a conflict the turn cannot be saved
	 * with, and it is rolled back: this method throws on it, or - for a commit made in the run's own
	 * sub-session, which the knowledge base compares only when the turn commits - the turn's commit
	 * fails.
	 * </p>
	 *
	 * @throws IllegalStateException
	 *         If a commit of another session changed an object the turn has changed as well.
	 */
	default void catchUpWithCommits() {
		if (ThreadContextManager.getSubSession() == null || !PersistencyLayer.Module.INSTANCE.isActive()) {
			// No interaction, or no knowledge base: nothing reads at an old revision.
			return;
		}
		try {
			HistoryUtils.updateSessionRevision();
		} catch (MergeConflictException ex) {
			throw new IllegalStateException("Objects this turn has changed were changed and saved by"
				+ " somebody else in the meantime, so the changes of this turn cannot be saved: "
				+ ex.getMessage(), ex);
		}
	}
}
