/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.tool.api;

/**
 * What a tool may know about the agent run it is executing in: plain, already-read data.
 *
 * <p>
 * <b>Why this is a record of strings and not the run's persistent {@code AgentExecution}.</b> The
 * framework writes that row on the execution store's writer thread, in a revision <em>newer</em>
 * than the one the tool's own interaction reads at - the interaction is opened before the run
 * starts, and a TopLogic sub-session's stable view only advances at sanctioned points. Any
 * attribute read on that wrapper from a tool therefore fails with "was deleted before or created
 * after access revision N" (Gitea #405). Everything here is captured as a {@link String} at the
 * moment it becomes known, on the thread that knows it, so reading it touches no
 * {@code KnowledgeBase} at all and can never throw.
 * </p>
 *
 * <p>
 * Every field may be {@code null}: a run that is not recorded has no {@link #executionId()}, a
 * headless script run may have no {@link #conversationId()}, and a bare {@code AgentLoop} built by
 * a test knows no {@link #agentName()}. Reading a {@code null} field is the normal way to learn
 * that the run does not have that fact - it is never an error. {@link #NONE} is what a tool
 * executed outside any agent run sees.
 * </p>
 *
 * @param runId
 *        The loop-generated id of this run, unique per run (every chat message is a new run).
 *        {@code null} only outside a run.
 * @param conversationId
 *        Id of the conversation (agent session) this run belongs to - the same across all runs of
 *        one chat, which is what a per-conversation resource is keyed on. {@code null} when the run
 *        has no recorded session.
 * @param agentName
 *        Name of the agent handling this run, as declared in the configuration.
 * @param model
 *        The model this run talks to, e.g. {@code claude-opus-5}.
 * @param provider
 *        Id of the provider serving {@link #model()}, or {@code null} when it cannot be resolved.
 * @param executionId
 *        Id of the persistent {@code AgentExecution} row of this run, for handing to a service that
 *        resolves it on <em>its own</em> thread. {@code null} when the run is not recorded.
 * @param delegationDepth
 *        {@code 0} for a top-level run, incremented by one per level of sub-agent delegation.
 * @param parent
 *        The run that delegated this one, or {@code null} for a top-level run.
 */
public record RunInfo(String runId, String conversationId, String agentName, String model,
		String provider, String executionId, int delegationDepth, RunInfo parent) {

	/** What a tool executed outside any agent run is told: nothing, readably. */
	public static final RunInfo NONE = new RunInfo(null, null, null, null, null, null, 0, null);

	/**
	 * Whether this describes an actual run.
	 *
	 * <p>
	 * {@code false} for {@link #NONE} - a tool called from a test harness, a TL-Script expression or
	 * the MCP endpoint rather than from an agent loop.
	 * </p>
	 */
	public boolean isKnown() {
		return runId != null;
	}

	/** A copy of this with {@link #runId()} replaced. */
	public RunInfo withRunId(String newRunId) {
		return new RunInfo(newRunId, conversationId, agentName, model, provider, executionId,
			delegationDepth, parent);
	}

	/** A copy of this with {@link #conversationId()} replaced. */
	public RunInfo withConversationId(String newConversationId) {
		return new RunInfo(runId, newConversationId, agentName, model, provider, executionId,
			delegationDepth, parent);
	}

	/** A copy of this with {@link #agentName()} replaced. */
	public RunInfo withAgentName(String newAgentName) {
		return new RunInfo(runId, conversationId, newAgentName, model, provider, executionId,
			delegationDepth, parent);
	}

	/** A copy of this with {@link #model()} and {@link #provider()} replaced. */
	public RunInfo withModel(String newModel, String newProvider) {
		return new RunInfo(runId, conversationId, agentName, newModel, newProvider, executionId,
			delegationDepth, parent);
	}

	/** A copy of this with {@link #executionId()} replaced. */
	public RunInfo withExecutionId(String newExecutionId) {
		return new RunInfo(runId, conversationId, agentName, model, provider, newExecutionId,
			delegationDepth, parent);
	}

	/**
	 * The top-level run this one descends from - itself when {@link #parent()} is {@code null}.
	 */
	public RunInfo root() {
		RunInfo root = this;
		while (root.parent() != null) {
			root = root.parent();
		}
		return root;
	}
}
