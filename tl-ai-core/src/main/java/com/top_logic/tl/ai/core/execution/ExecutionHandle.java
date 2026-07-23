/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.execution;

import com.top_logic.tl.ai.core.execution.model.AgentExecution;

/**
 * Mutable holder for the {@link AgentExecution} of the currently running agent loop.
 *
 * <p>
 * The run id — and with it the {@link AgentExecution} row — only comes into existence when the
 * loop emits its {@code RunStarted} event, after the (immutable) tool context has been built.
 * The context therefore carries this holder by reference: the {@link ExecutionRecorder} fills it
 * at run start, and tools such as the delegation tool read it to attribute child executions to
 * their parent.
 * </p>
 */
public final class ExecutionHandle {

	private volatile AgentExecution _execution;

	private volatile String _runId;

	private volatile String _conversationId;

	/** The recorded execution of the current run, or <code>null</code> if not recorded. */
	public AgentExecution get() {
		return _execution;
	}

	/** Sets the recorded execution of the current run. */
	public void set(AgentExecution execution) {
		_execution = execution;
	}

	/**
	 * The loop-generated run id of the current run, or <code>null</code> before the run's
	 * {@code RunStarted} event.
	 *
	 * <p>
	 * Unlike {@link #get()}, reading the run id does not touch the KnowledgeBase: it is a
	 * plain string set from the {@code RunStarted} event, so it is safe to read from an execution
	 * thread whose interaction revision may be older than the revision in which the persistent
	 * {@link AgentExecution} wrapper was created. It is stable across all tool calls within a run
	 * (the same handle is shared) and distinct across concurrent runs.
	 * </p>
	 */
	public String runId() {
		return _runId;
	}

	/** Sets the loop-generated run id of the current run. */
	public void setRunId(String runId) {
		_runId = runId;
	}

	/**
	 * A stable identifier of the <em>conversation</em> (agent session) this run belongs to, or
	 * <code>null</code> when none is known.
	 *
	 * <p>
	 * Unlike {@link #runId()}, which is distinct for every run (every chat message is a new run),
	 * this id is the same across all runs of one conversation, so a resource keyed on it - notably
	 * the interactive browser session (see {@code BrowserTool}) - is reused across the messages of a
	 * conversation instead of being recreated per message. It is a plain string captured at run
	 * setup from the persistent {@code AgentSession}'s id; reading it here never touches the
	 * KnowledgeBase, so it is safe to read from an execution thread whose interaction revision may be
	 * older than the revision in which that wrapper was created (the same revision-safety guarantee
	 * as {@link #runId()}).
	 * </p>
	 */
	public String conversationId() {
		return _conversationId;
	}

	/** Sets the stable conversation (agent-session) id of the current run. */
	public void setConversationId(String conversationId) {
		_conversationId = conversationId;
	}
}
