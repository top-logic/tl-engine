/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.event;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.approval.ApprovalOutcome;
import com.top_logic.tl.ai.core.memory.CompactionResult;
import com.top_logic.tl.ai.core.message.TokenUsage;

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
 *   <li>On a turn with writes: {@link LockAcquired} &rarr; {@link TransactionBegan}
 *       &rarr; write tool's {@link ToolCallCompleted} &rarr;
 *       {@link TransactionCommitted} / {@link TransactionRolledBack}. If
 *       {@link LockFailed} is emitted instead, no transaction events follow.</li>
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

	/** First event of a run. */
	record RunStarted(String runId, int turnNumber, Instant timestamp, String model)
			implements AgentEvent {}

	/** Final event of a run. {@code turnNumber} is the last turn reached. */
	record RunCompleted(String runId, int turnNumber, Instant timestamp,
			StopReason reason, TokenUsage totalUsage) implements AgentEvent {}

	// ---------- Turn lifecycle ----------

	/** Start of one LLM-call iteration. */
	record TurnStarted(String runId, int turnNumber, Instant timestamp)
			implements AgentEvent {}

	/** End of one LLM-call iteration (after tool execution, if any). */
	record TurnCompleted(String runId, int turnNumber, Instant timestamp,
			TokenUsage turnUsage) implements AgentEvent {}

	// ---------- LLM streaming ----------

	/** One chunk of assistant text streamed from the LLM. */
	record TextDelta(String runId, int turnNumber, Instant timestamp, String text)
			implements AgentEvent {}

	/** One chunk of extended-thinking text streamed from the LLM. */
	record ThinkingDelta(String runId, int turnNumber, Instant timestamp, String text)
			implements AgentEvent {}

	// ---------- Tool lifecycle ----------

	/** Emitted immediately before a tool is invoked. */
	record ToolCallStarted(String runId, int turnNumber, Instant timestamp,
			String toolCallId, String toolName, String argsJson) implements AgentEvent {}

	/** Emitted after a tool completes successfully. */
	record ToolCallCompleted(String runId, int turnNumber, Instant timestamp,
			String toolCallId, String toolName, String result, Duration duration)
			implements AgentEvent {}

	/** Emitted when a tool throws or returns an error result. */
	record ToolCallFailed(String runId, int turnNumber, Instant timestamp,
			String toolCallId, String toolName, String errorMessage, Duration duration)
			implements AgentEvent {}

	/** Emitted when a tool call is blocked by a security or approval gate. */
	record ToolCallDenied(String runId, int turnNumber, Instant timestamp,
			String toolCallId, String toolName, String reason) implements AgentEvent {}

	/**
	 * Emitted when a tool result exceeded the configured size limit and was truncated
	 * before being fed back to the LLM. The {@link ToolCallCompleted} event for the
	 * same {@code toolCallId} carries the truncated text; {@code originalLength} lets
	 * listeners surface how much was dropped.
	 */
	record ToolCallResultTruncated(String runId, int turnNumber, Instant timestamp,
			String toolCallId, String toolName, int originalLength, int truncatedLength)
			implements AgentEvent {}

	/**
	 * Emitted when a tool reports progress during execution.
	 */
	record ToolProgress(String runId, int turnNumber, Instant timestamp,
			String toolCallId, String toolName, String message) implements AgentEvent {}

	// ---------- Approval (notification; decisions flow via ApprovalCallback) ----------

	/** Emitted when a tool call is about to block on user approval. */
	record ApprovalRequired(String runId, int turnNumber, Instant timestamp,
			String toolCallId, String approvalRequestId, String toolName,
			Map<String, Object> args, String displayReason) implements AgentEvent {}

	/** Emitted after the approval channel returns with a decision. */
	record ApprovalResolved(String runId, int turnNumber, Instant timestamp,
			String toolCallId, String approvalRequestId, ApprovalOutcome outcome)
			implements AgentEvent {}

	// ---------- Transactions and lock ----------

	/** Emitted once the global edit lock has been acquired. */
	record LockAcquired(String runId, int turnNumber, Instant timestamp)
			implements AgentEvent {}

	/** Emitted when the global edit lock could not be acquired. */
	record LockFailed(String runId, int turnNumber, Instant timestamp, String reason)
			implements AgentEvent {}

	/** Emitted when a KB transaction begins (first write tool of a turn). */
	record TransactionBegan(String runId, int turnNumber, Instant timestamp)
			implements AgentEvent {}

	/** Emitted after a KB transaction commits. {@code revisionNumber} is {@code -1} if
	 *  no changes were made. */
	record TransactionCommitted(String runId, int turnNumber, Instant timestamp,
			long revisionNumber) implements AgentEvent {}

	/** Emitted after a KB transaction rolls back. */
	record TransactionRolledBack(String runId, int turnNumber, Instant timestamp,
			String reason) implements AgentEvent {}

	// ---------- Context management ----------

	/**
	 * Emitted immediately before conversation compaction runs.
	 *
	 * @param trigger {@code "reactive"} (context-window exceeded) or
	 *                {@code "proactive"} (end-of-turn threshold)
	 */
	record CompactionStarted(String runId, int turnNumber, Instant timestamp,
			int tokensBefore, String trigger) implements AgentEvent {}

	/** Emitted after compaction completes. */
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
	 */
	record DelegatedEvent(String runId, int turnNumber, Instant timestamp,
			String delegationToolCallId, AgentEvent childEvent) implements AgentEvent {}

	// ---------- Task updates ----------

	/**
	 * Emitted when the task list changes (create, update, or delete).
	 * Carries the full task list as a JSON snapshot (PersistedTaskStore format).
	 */
	record TaskUpdated(String runId, int turnNumber, Instant timestamp,
			String tasksJson) implements AgentEvent {}

	// ---------- Errors ----------

	/**
	 * Emitted when a transient provider error triggers an automatic retry.
	 *
	 * @param kind    short machine-readable error kind (e.g., {@code "RATE_LIMITED"})
	 * @param attempt retry attempt number (1-based)
	 * @param waitMs  milliseconds the loop will sleep before retrying
	 */
	record RetryScheduled(String runId, int turnNumber, Instant timestamp,
			String kind, int attempt, long waitMs) implements AgentEvent {}

	/**
	 * Emitted on an unrecoverable provider error. The loop stops after this event.
	 *
	 * @param kind    short machine-readable error kind (e.g., {@code "RATE_LIMITED"})
	 * @param message human-readable error message
	 */
	record ProviderError(String runId, int turnNumber, Instant timestamp,
			String kind, String message) implements AgentEvent {}
}
