/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.event;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.approval.ApprovalOutcome;
import com.top_logic.tl.ai.core.memory.CompactionResult;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * Sealed hierarchy of lifecycle events emitted by an agent run.
 *
 * <p>Every event carries three correlation fields:</p>
 * <ul>
 *   <li>{@link #runId()} — UUID generated at the top of {@code AgentLoop.run()};
 *       all events from one run share this value.</li>
 *   <li>{@link #turnNumber()} — 1-based loop iteration. {@link RunStarted}
 *       carries {@code 0}; {@link RunCompleted} carries the last turn reached.</li>
 *   <li>{@link #timestamp()} — when the event was emitted.</li>
 * </ul>
 *
 * <h2>Ordering guarantees</h2>
 * <ul>
 *   <li>{@link RunStarted} is the first event; {@link RunCompleted} is the last.</li>
 *   <li>All events between {@link TurnStarted}({@code n}) and
 *       {@link TurnCompleted}({@code n}) carry {@code turnNumber == n}.</li>
 *   <li>Every tool call from the LLM produces exactly one terminal event —
 *       {@link ToolCallCompleted}, {@link ToolCallFailed}, or {@link ToolCallDenied}
 *       — for its {@code toolCallId}. {@link ToolCallStarted} is emitted only once
 *       all security and approval gates have passed and the call is about to run; it
 *       therefore precedes {@link ToolCallCompleted} or {@link ToolCallFailed}, but
 *       is <b>not</b> emitted before {@link ToolCallDenied}.</li>
 *   <li>For each approval, {@link ApprovalRequired} precedes {@link ApprovalResolved},
 *       which precedes {@link ToolCallStarted} or {@link ToolCallDenied} for the same
 *       {@code toolCallId}. When a tool is auto-approved from a previous session grant,
 *       a single {@link ApprovalResolved} with
 *       {@link ApprovalOutcome#APPROVED_FOR_SESSION} is emitted without a preceding
 *       {@link ApprovalRequired}.</li>
 *   <li>If a tool result is truncated before being returned to the LLM,
 *       {@link ToolCallResultTruncated} is emitted immediately before the
 *       corresponding {@link ToolCallCompleted} for the same {@code toolCallId}.</li>
 *   <li>Zero or more {@link ToolProgress} events may be emitted between
 *       {@link ToolCallStarted} and the terminal event ({@link ToolCallCompleted}
 *       or {@link ToolCallFailed}) for the same {@code toolCallId}.</li>
 *   <li>On a turn with writes: {@link TransactionBegan}
 *       &#x2192; write tool's {@link ToolCallCompleted} &#x2192;
 *       {@link TransactionCommitted} / {@link TransactionRolledBack}.
 *       Locking is handled per-object inside individual tools.</li>
 *   <li>Across parallel tool calls in one batch, only per-{@code toolCallId} ordering
 *       is guaranteed; inter-call order is unspecified.</li>
 * </ul>
 *
 * @see AgentEventListener
 */
public sealed interface AgentEvent {

	/** Unique ID of the agent run that emitted this event. */
	String runId();

	/** 1-based turn number. {@code 0} on {@link RunStarted}. */
	int turnNumber();

	/** Wall-clock time when the event was emitted. */
	Instant timestamp();

	// ---------- Run lifecycle ----------

	/**
	 * First event of a run.
	 *
	 * @param runId agent run identifier
	 * @param turnNumber 1-based loop iteration
	 * @param timestamp when the event occurred
	 * @param model LLM model name
	 */
	record RunStarted(String runId, int turnNumber, Instant timestamp, String model)
			implements AgentEvent {}

	/**
	 * Final event of a run. {@code turnNumber} is the last turn reached.
	 *
	 * @param runId agent run identifier
	 * @param turnNumber 1-based loop iteration
	 * @param timestamp when the event occurred
	 * @param reason why the run stopped
	 * @param totalUsage cumulative token usage for the run
	 */
	record RunCompleted(String runId, int turnNumber, Instant timestamp,
			StopReason reason, TokenUsage totalUsage) implements AgentEvent {}

	// ---------- Turn lifecycle ----------

	/**
	 * Start of one LLM-call iteration.
	 *
	 * @param runId agent run identifier
	 * @param turnNumber 1-based loop iteration
	 * @param timestamp when the event occurred
	 */
	record TurnStarted(String runId, int turnNumber, Instant timestamp)
			implements AgentEvent {}

	/**
	 * End of one LLM-call iteration (after tool execution, if any).
	 *
	 * @param runId agent run identifier
	 * @param turnNumber 1-based loop iteration
	 * @param timestamp when the event occurred
	 * @param turnUsage token usage for this turn
	 * @param contentParts assistant message content parts; empty list if no message produced
	 */
	record TurnCompleted(String runId, int turnNumber, Instant timestamp,
			TokenUsage turnUsage, List<ContentPart> contentParts) implements AgentEvent {}

	/**
	 * What the prompt of the request about to be sent is made of.
	 *
	 * <p>
	 * Emitted immediately before every provider call, so a UI can say where the context window
	 * went. The three figures are estimates from the same {@code TokenEstimator} the compaction
	 * budget uses, not provider-reported counts: the provider reports one prompt total and never
	 * breaks it down. Their sum is therefore close to, but not equal to, the
	 * {@link TurnCompleted#turnUsage()} prompt size that follows - a consumer that shows both must
	 * mark these as estimated.
	 * </p>
	 *
	 * @param runId agent run identifier
	 * @param turnNumber 1-based loop iteration
	 * @param timestamp when the event occurred
	 * @param systemPromptTokens estimated tokens of the system prompt
	 * @param toolDefinitionTokens estimated tokens of the tool schemas sent with the request
	 * @param messageTokens estimated tokens of the conversation messages, tool results included
	 * @param toolResultTokens estimated tokens of the tool results within {@code messageTokens} -
	 *        what the agent read back from the tools it ran, which is usually the part of a
	 *        conversation that grows fastest and the part a user least expects
	 * @param toolCount number of tool schemas sent with the request
	 */
	record ContextComposition(String runId, int turnNumber, Instant timestamp,
			int systemPromptTokens, int toolDefinitionTokens, int messageTokens, int toolResultTokens,
			int toolCount)
			implements AgentEvent {

		/** Legacy shape of a composition recorded before tool results were counted apart. */
		public ContextComposition(String runId, int turnNumber, Instant timestamp,
				int systemPromptTokens, int toolDefinitionTokens, int messageTokens, int toolCount) {
			this(runId, turnNumber, timestamp, systemPromptTokens, toolDefinitionTokens, messageTokens, 0,
				toolCount);
		}
	}

	// ---------- LLM streaming ----------

	/**
	 * One chunk of assistant text streamed from the LLM.
	 *
	 * @param runId agent run identifier
	 * @param turnNumber 1-based loop iteration
	 * @param timestamp when the event occurred
	 * @param text the text content
	 */
	record TextDelta(String runId, int turnNumber, Instant timestamp, String text)
			implements AgentEvent {}

	/**
	 * One chunk of extended-thinking text streamed from the LLM.
	 *
	 * @param runId agent run identifier
	 * @param turnNumber 1-based loop iteration
	 * @param timestamp when the event occurred
	 * @param text the text content
	 */
	record ThinkingDelta(String runId, int turnNumber, Instant timestamp, String text)
			implements AgentEvent {}

	// ---------- Tool lifecycle ----------

	/**
	 * Emitted immediately before a tool is invoked.
	 *
	 * @param runId agent run identifier
	 * @param turnNumber 1-based loop iteration
	 * @param timestamp when the event occurred
	 * @param toolCallId tool call identifier
	 * @param toolName name of the tool
	 * @param argsJson tool arguments as JSON
	 * @param mode what kind of state change the tool makes, or {@code null} if the tool could not
	 *        be resolved
	 */
	record ToolCallStarted(String runId, int turnNumber, Instant timestamp,
			String toolCallId, String toolName, String argsJson, ToolMode mode)
			implements AgentEvent {}

	/**
	 * Emitted after a tool completes successfully.
	 *
	 * @param runId agent run identifier
	 * @param turnNumber 1-based loop iteration
	 * @param timestamp when the event occurred
	 * @param toolCallId tool call identifier
	 * @param toolName name of the tool
	 * @param result tool execution result
	 * @param duration execution time
	 */
	record ToolCallCompleted(String runId, int turnNumber, Instant timestamp,
			String toolCallId, String toolName, String result, Duration duration)
			implements AgentEvent {}

	/**
	 * Emitted when a tool throws or returns an error result.
	 *
	 * @param runId agent run identifier
	 * @param turnNumber 1-based loop iteration
	 * @param timestamp when the event occurred
	 * @param toolCallId tool call identifier
	 * @param toolName name of the tool
	 * @param errorMessage error details
	 * @param duration execution time
	 */
	record ToolCallFailed(String runId, int turnNumber, Instant timestamp,
			String toolCallId, String toolName, String errorMessage, Duration duration)
			implements AgentEvent {}

	/**
	 * Emitted when a tool call is blocked by a security or approval gate.
	 *
	 * @param runId agent run identifier
	 * @param turnNumber 1-based loop iteration
	 * @param timestamp when the event occurred
	 * @param toolCallId tool call identifier
	 * @param toolName name of the tool
	 * @param reason why this occurred
	 */
	record ToolCallDenied(String runId, int turnNumber, Instant timestamp,
			String toolCallId, String toolName, String reason) implements AgentEvent {}

	/**
	 * Emitted when a tool result exceeded the configured size limit and was truncated
	 * before being fed back to the LLM. The {@link ToolCallCompleted} event for the
	 * same {@code toolCallId} carries the truncated text; {@code originalLength} lets
	 * listeners surface how much was dropped.
	 *
	 * @param runId agent run identifier
	 * @param turnNumber 1-based loop iteration
	 * @param timestamp when the event occurred
	 * @param toolCallId tool call identifier
	 * @param toolName name of the tool
	 * @param originalLength original result length
	 * @param truncatedLength length after truncation
	 */
	record ToolCallResultTruncated(String runId, int turnNumber, Instant timestamp,
			String toolCallId, String toolName, int originalLength, int truncatedLength)
			implements AgentEvent {}

	/**
	 * Emitted when a tool reports progress during execution.
	 *
	 * @param runId agent run identifier
	 * @param turnNumber 1-based loop iteration
	 * @param timestamp when the event occurred
	 * @param toolCallId tool call identifier
	 * @param toolName name of the tool
	 * @param message detail message
	 */
	record ToolProgress(String runId, int turnNumber, Instant timestamp,
			String toolCallId, String toolName, String message) implements AgentEvent {}

	// ---------- Approval (notification; decisions flow via ApprovalCallback) ----------

	/**
	 * Emitted when a tool call is about to block on user approval.
	 *
	 * @param runId agent run identifier
	 * @param turnNumber 1-based loop iteration
	 * @param timestamp when the event occurred
	 * @param toolCallId tool call identifier
	 * @param approvalRequestId approval request identifier
	 * @param toolName name of the tool
	 * @param argsJson the tool call's raw argument JSON, exactly as the model emitted it
	 * @param displayReason human-readable reason shown to user
	 * @param toolMode what kind of change the tool makes, which is why approval is being asked. A UI
	 *        that speaks the user's language phrases the question from this rather than from
	 *        {@link #displayReason()}, which is written once, in English, by the security context
	 * @param timeout how long the request waits for the user's decision, counted from
	 *        {@link #timestamp()}; a UI shows the remaining time and the request resolves as
	 *        {@link ApprovalOutcome#TIMED_OUT} when it elapses
	 */
	record ApprovalRequired(String runId, int turnNumber, Instant timestamp,
			String toolCallId, String approvalRequestId, String toolName,
			String argsJson, String displayReason, ToolMode toolMode, Duration timeout)
			implements AgentEvent {

		/** Approval events from before the mode was carried; the reason then has to speak for itself. */
		public ApprovalRequired(String runId, int turnNumber, Instant timestamp, String toolCallId,
				String approvalRequestId, String toolName, String argsJson, String displayReason,
				Duration timeout) {
			this(runId, turnNumber, timestamp, toolCallId, approvalRequestId, toolName, argsJson,
				displayReason, null, timeout);
		}
	}

	/**
	 * Emitted after the approval channel returns with a decision.
	 *
	 * <p>Note: for tools already approved for the session, the loop emits a synthetic
	 * {@code ApprovalResolved} with outcome
	 * {@link com.top_logic.tl.ai.core.agent.approval.ApprovalOutcome#APPROVED_FOR_SESSION}
	 * and a fresh {@code approvalRequestId} <em>without</em> a preceding
	 * {@link ApprovalRequired}. Listeners must not assume a request/resolve pairing.</p>
	 *
	 * @param runId agent run identifier
	 * @param turnNumber 1-based loop iteration
	 * @param timestamp when the event occurred
	 * @param toolCallId tool call identifier
	 * @param approvalRequestId approval request identifier
	 * @param outcome the approval outcome
	 */
	record ApprovalResolved(String runId, int turnNumber, Instant timestamp,
			String toolCallId, String approvalRequestId, ApprovalOutcome outcome)
			implements AgentEvent {}

	// ---------- Transactions ----------

	/**
	 * Emitted when a KB transaction begins (first write tool of a turn).
	 *
	 * @param runId agent run identifier
	 * @param turnNumber 1-based loop iteration
	 * @param timestamp when the event occurred
	 */
	record TransactionBegan(String runId, int turnNumber, Instant timestamp)
			implements AgentEvent {}

	/**
	 * Emitted after a KB transaction commits. {@code revisionNumber} is {@code -1} if
	 * no changes were made.
	 *
	 * @param runId agent run identifier
	 * @param turnNumber 1-based loop iteration
	 * @param timestamp when the event occurred
	 * @param revisionNumber KB revision number
	 */
	record TransactionCommitted(String runId, int turnNumber, Instant timestamp,
			long revisionNumber) implements AgentEvent {}

	/**
	 * Emitted after a KB transaction rolls back.
	 *
	 * @param runId agent run identifier
	 * @param turnNumber 1-based loop iteration
	 * @param timestamp when the event occurred
	 * @param reason why this occurred
	 */
	record TransactionRolledBack(String runId, int turnNumber, Instant timestamp,
			String reason) implements AgentEvent {}

	// ---------- Context management ----------

	/**
	 * Emitted immediately before conversation compaction runs.
	 *
	 * @param runId agent run identifier
	 * @param turnNumber 1-based loop iteration
	 * @param timestamp when the event occurred
	 * @param tokensBefore token count before compaction
	 * @param trigger {@code "reactive"} (context-window exceeded) or
	 *                {@code "proactive"} (end-of-turn threshold)
	 */
	record CompactionStarted(String runId, int turnNumber, Instant timestamp,
			int tokensBefore, String trigger) implements AgentEvent {}

	/**
	 * Emitted after compaction completes.
	 *
	 * @param runId agent run identifier
	 * @param turnNumber 1-based loop iteration
	 * @param timestamp when the event occurred
	 * @param result compaction outcome
	 */
	record CompactionCompleted(String runId, int turnNumber, Instant timestamp,
			CompactionResult result) implements AgentEvent {}

	// ---------- Delegation ----------

	/**
	 * Wrapper for child-agent events forwarded to the parent's listener chain.
	 *
	 * <p>
	 * The {@code delegationToolCallId} is the parent agent's tool call ID for the
	 * {@code delegate_to_*} tool, enabling the SSE handler and browser to route child events to
	 * the correct delegation container.
	 * </p>
	 *
	 * @param runId agent run identifier
	 * @param turnNumber 1-based loop iteration
	 * @param timestamp when the event occurred
	 * @param subagentId the async subagent job id, or {@code ""} for synchronous delegation
	 * @param delegationToolCallId parent tool call identifier
	 * @param childEvent the delegated child event
	 */
	record DelegatedEvent(String runId, int turnNumber, Instant timestamp,
			String subagentId, String delegationToolCallId, AgentEvent childEvent) implements AgentEvent {}

	// ---------- Task updates ----------

	/**
	 * Emitted when the task list changes (create, update, or delete).
	 * Carries the full task list as a JSON snapshot ({@code PersistedTaskStore} format).
	 *
	 * @param runId agent run identifier
	 * @param turnNumber 1-based loop iteration
	 * @param timestamp when the event occurred
	 * @param tasksJson task list as JSON
	 */
	record TaskUpdated(String runId, int turnNumber, Instant timestamp,
			String tasksJson) implements AgentEvent {}

	// ---------- Errors ----------

	/**
	 * Emitted when a transient provider error triggers an automatic retry.
	 *
	 * @param runId agent run identifier
	 * @param turnNumber 1-based loop iteration
	 * @param timestamp when the event occurred
	 * @param kind    short machine-readable error kind (e.g., {@code "RATE_LIMITED"})
	 * @param attempt retry attempt number (1-based)
	 * @param waitMs  milliseconds the loop will sleep before retrying
	 */
	record RetryScheduled(String runId, int turnNumber, Instant timestamp,
			String kind, int attempt, long waitMs) implements AgentEvent {}

	/**
	 * Emitted on an unrecoverable provider error. The loop stops after this event.
	 *
	 * @param runId agent run identifier
	 * @param turnNumber 1-based loop iteration
	 * @param timestamp when the event occurred
	 * @param kind    short machine-readable error kind (e.g., {@code "RATE_LIMITED"})
	 * @param message human-readable error message
	 */
	record ProviderError(String runId, int turnNumber, Instant timestamp,
			String kind, String message) implements AgentEvent {}

	// ---------- Subagents ----------

	/**
	 * Emitted when an asynchronous subagent job is registered and dispatched.
	 *
	 * @param runId           agent run identifier
	 * @param turnNumber      1-based loop iteration
	 * @param timestamp       when the event occurred
	 * @param subagentId      the job id within the session
	 * @param targetAgentName the delegated-to agent definition name
	 * @param taskBrief       the task the subagent was given
	 */
	record SubagentStarted(String runId, int turnNumber, Instant timestamp,
			String subagentId, String targetAgentName, String taskBrief) implements AgentEvent {}

	/**
	 * Emitted when a subagent job finishes successfully.
	 *
	 * @param runId      agent run identifier
	 * @param turnNumber 1-based loop iteration
	 * @param timestamp  when the event occurred
	 * @param subagentId the job id
	 * @param result     the structured result envelope
	 */
	record SubagentCompleted(String runId, int turnNumber, Instant timestamp,
			String subagentId,
			com.top_logic.tl.ai.core.agent.subagent.SubagentResult result) implements AgentEvent {}

	/**
	 * Emitted when a subagent job fails.
	 *
	 * @param runId        agent run identifier
	 * @param turnNumber   1-based loop iteration
	 * @param timestamp    when the event occurred
	 * @param subagentId   the job id
	 * @param errorSummary a short human-readable error summary
	 */
	record SubagentFailed(String runId, int turnNumber, Instant timestamp,
			String subagentId, String errorSummary) implements AgentEvent {}

	/**
	 * Emitted when a subagent job is cooperatively cancelled.
	 *
	 * @param runId      agent run identifier
	 * @param turnNumber 1-based loop iteration
	 * @param timestamp  when the event occurred
	 * @param subagentId the job id
	 */
	record SubagentCancelled(String runId, int turnNumber, Instant timestamp,
			String subagentId) implements AgentEvent {}
}
