/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.execution;

import com.top_logic.basic.Logger;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.tool.api.RunInfo;

/**
 * What the framework publishes about the currently running agent loop, for its tools to read.
 *
 * <p>
 * The run id - and with it the {@link AgentExecution} row - only comes into existence when the
 * loop emits its {@code RunStarted} event, after the (immutable) tool context has been built.
 * The context therefore carries this holder by reference: the loop and the
 * {@link ExecutionRecorder} fill it as the run starts, and every copy of the context - including
 * the ones handed to the tools of a parallel batch - shares the same instance, so all of them see
 * the same run.
 * </p>
 *
 * <h2>Two kinds of value, and why</h2>
 *
 * <p>
 * {@link #runInfo()} is <b>plain data</b>: strings captured on the thread that knew them, readable
 * from anywhere, at any time, without touching the {@link KnowledgeBase}. This is what a tool
 * reads.
 * </p>
 *
 * <p>
 * {@link #get()} is the <b>persistent wrapper</b>, and it is an opaque token, not something to
 * read. The execution store writes that row on its own writer thread, and whether the revision a
 * tool's interaction reads at includes it depends on when it was written and on how the run was
 * started, so an attribute access on it from an execution thread can throw "was deleted before or
 * created after access revision N" (Gitea #405). Hand it to {@link AgentExecutionStore} - which
 * dereferences it on the writer thread, where it is visible - and read nothing off it here.
 * </p>
 *
 * @see RunInfo
 */
public final class ExecutionHandle {

	private volatile AgentExecution _execution;

	private volatile RunInfo _info;

	private volatile Runnable _checkpoint;

	/** Creates a handle for a top-level run. */
	public ExecutionHandle() {
		this(null, 0);
	}

	/**
	 * Creates a handle for a run delegated by another one.
	 *
	 * @param parent
	 *        What is known about the delegating run; {@code null} for a top-level run.
	 * @param delegationDepth
	 *        {@code 0} for a top-level run, the parent's depth plus one for a delegated one.
	 * @see com.top_logic.tl.ai.core.agent.ToolContext#forDelegation
	 */
	public ExecutionHandle(RunInfo parent, int delegationDepth) {
		_info = new RunInfo(null, parent == null ? null : parent.conversationId(), null, null, null,
			null, delegationDepth, parent);
	}

	/**
	 * What a tool may know about this run; never {@code null}, {@link RunInfo#NONE}-like (all
	 * fields {@code null}) before the run starts.
	 *
	 * <p>
	 * Reading this never touches the {@link KnowledgeBase} and therefore never throws - unlike
	 * {@link #get()}. It is the accessor a tool uses.
	 * </p>
	 */
	public RunInfo runInfo() {
		return _info;
	}

	/**
	 * The recorded execution of the current run, or <code>null</code> if not recorded.
	 *
	 * <p>
	 * <b>An opaque token; do not read attributes off it.</b> The row is written by
	 * {@link AgentExecutionStore}'s writer thread, and the revision a tool's interaction reads at
	 * need not include it, so {@code get().getAgentName()} and every other attribute access can throw
	 * "created after access revision N" (Gitea #405). Pass it to the store, which resolves it where
	 * it is visible; to <em>read</em> the run's agent, model, run id or session, use
	 * {@link #runInfo()}.
	 * </p>
	 */
	public AgentExecution get() {
		return _execution;
	}

	/**
	 * Publishes the recorded execution of the current run, and its id as plain data on
	 * {@link #runInfo()}.
	 */
	public void set(AgentExecution execution) {
		_execution = execution;
		setExecutionId(idOf(execution));
	}

	/**
	 * Writes down what the run has recorded so far, before it waits for a person (#598).
	 *
	 * <p>
	 * Called where a run parks its thread on somebody's answer - an approval, an {@code askUser}
	 * question - which can take the rest of the day, and so is where a restart most likely finds
	 * it. Does not wait for the write, and does nothing when the run is not recorded or keeps no
	 * transcript.
	 * </p>
	 *
	 * @see ExecutionRecorder
	 */
	public void checkpoint() {
		Runnable checkpoint = _checkpoint;
		if (checkpoint != null) {
			checkpoint.run();
		}
	}

	/**
	 * Installs what {@link #checkpoint()} does; set by the {@link ExecutionRecorder} of the run.
	 */
	void setCheckpoint(Runnable checkpoint) {
		_checkpoint = checkpoint;
	}

	/**
	 * Makes {@link #checkpoint()} write the transcript of the run that delegated this one, as long
	 * as this run does not record a transcript of its own.
	 *
	 * <p>
	 * For a delegation the delegating run waits for: what the user is looking at while the child
	 * asks for an approval is the parent's turn, and it is the parent's row that has to hold it
	 * when the process ends during that wait.
	 * </p>
	 *
	 * @param parent
	 *        The handle of the delegating run.
	 */
	public void checkpointThrough(ExecutionHandle parent) {
		_checkpoint = parent::checkpoint;
	}

	/**
	 * The loop-generated run id of the current run, or <code>null</code> before the run's
	 * {@code RunStarted} event.
	 *
	 * @see RunInfo#runId()
	 */
	public String runId() {
		return _info.runId();
	}

	/**
	 * Publishes the loop-generated run id of the current run.
	 *
	 * @see com.top_logic.tl.ai.core.agent.AgentLoop#run
	 */
	public synchronized void setRunId(String runId) {
		_info = _info.withRunId(runId);
	}

	/**
	 * A stable identifier of the <em>conversation</em> (agent session) this run belongs to, or
	 * <code>null</code> when none is known.
	 *
	 * <p>
	 * Unlike {@link #runId()}, which is distinct for every run (every chat message is a new run),
	 * this id is the same across all runs of one conversation, so a resource keyed on it - notably
	 * the interactive browser session (see {@code BrowserTool}) - is reused across the messages of a
	 * conversation instead of being recreated per message.
	 * </p>
	 *
	 * @see RunInfo#conversationId()
	 */
	public String conversationId() {
		return _info.conversationId();
	}

	/** Publishes the stable conversation (agent-session) id of the current run. */
	public synchronized void setConversationId(String conversationId) {
		_info = _info.withConversationId(conversationId);
	}

	/**
	 * Publishes the name of the agent handling this run.
	 *
	 * <p>
	 * Done by whoever resolved the agent: {@code AgentFactory} for a named run, the delegation tool
	 * and {@code AgentRunner} for a pre-built child loop, and {@link ExecutionRecorder} for every
	 * recorded run (which covers a loop built by hand, such as the {@code aiChat} script
	 * function's). All of them publish the same name, and between them they cover the
	 * <em>unrecorded</em> run too - a test, a run without a session - which is the case where the
	 * name used to be unavailable, and is why #405 stayed invisible to every test.
	 * </p>
	 */
	public synchronized void setAgentName(String agentName) {
		_info = _info.withAgentName(agentName);
	}

	/** Publishes the model this run talks to and the id of the provider serving it. */
	public synchronized void setModel(String model, String provider) {
		_info = _info.withModel(model, provider != null ? provider : _info.provider());
	}

	private synchronized void setExecutionId(String executionId) {
		_info = _info.withExecutionId(executionId);
	}

	/**
	 * The persistent id of {@code execution} as a plain String, or <code>null</code> when it cannot
	 * be derived.
	 *
	 * <p>
	 * Reads the object's identity, never one of its attributes: the identity is carried by the
	 * wrapper itself and needs no revision-checked access, which is what makes this safe on a thread
	 * whose view predates the row. Best-effort - a failure degrades to <code>null</code> rather than
	 * disturbing the run.
	 * </p>
	 */
	private static String idOf(AgentExecution execution) {
		if (execution == null) {
			return null;
		}
		try {
			return KBUtils.getWrappedObjectName(execution).toString();
		} catch (RuntimeException ex) {
			Logger.info("Could not derive the id of the run's execution row: " + ex.getMessage(),
				ExecutionHandle.class);
			return null;
		}
	}
}
