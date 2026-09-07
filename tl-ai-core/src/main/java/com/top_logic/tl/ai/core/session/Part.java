/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.session;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.message.TokenUsage;

/**
 * A content unit within a {@link Turn}. Each variant represents a different type
 * of event or content produced during an agent run.
 *
 * <p>Parts are the atoms of the conversation log. They are grouped into turns
 * (one turn = one user-assistant cycle) and persisted as the session history.</p>
 */
public sealed interface Part {

	/** Unique identifier for this part. */
	String id();

	/** When this part was created. */
	Instant timestamp();

	// ---- Content parts ----

	/**
	 * LLM text output (final, accumulated — not streaming deltas).
	 *
	 * @param id part identifier
	 * @param timestamp when the text was produced
	 * @param text the text content
	 */
	record TextPart(String id, Instant timestamp, String text) implements Part {}

	/**
	 * A thinking / reasoning block produced by the LLM.
	 *
	 * @param id part identifier
	 * @param timestamp when the thinking block was produced
	 * @param text the reasoning content
	 * @param continuityToken opaque provider token for multi-turn continuity; may be {@code null}
	 */
	record ThinkingPart(String id, Instant timestamp, String text, String continuityToken) implements Part {}

	// ---- Tool call with state machine ----

	/** Status of a tool call through its lifecycle. */
	enum ToolCallStatus {
		/** Tool is currently executing. */
		RUNNING,
		/** Tool finished successfully. */
		COMPLETED,
		/** Tool threw an error. */
		FAILED,
		/** Tool was denied by security or approval. */
		DENIED,
		/** Tool reported success, but the writes of its turn were rolled back and are void. */
		ROLLED_BACK
	}

	/**
	 * A tool invocation with its full lifecycle state.
	 *
	 * <p>Created via {@link #started} or {@link #denied} factory methods.
	 * Transitions via {@link #completed} or {@link #failed}.</p>
	 *
	 * @param id part identifier
	 * @param timestamp when the tool call was created
	 * @param toolName name of the invoked tool
	 * @param argsJson tool arguments as JSON
	 * @param status current lifecycle status
	 * @param result tool execution result, or {@code null}
	 * @param errorMessage error details, or the rollback reason for
	 *        {@link ToolCallStatus#ROLLED_BACK}; {@code null} if neither applies
	 * @param denyReason denial reason, or {@code null}
	 * @param durationMs execution time in milliseconds
	 */
	record ToolCallPart(
			String id, Instant timestamp, String toolName, String argsJson,
			ToolCallStatus status, String result, String errorMessage,
			String denyReason, long durationMs
	) implements Part {

		/** Creates a new tool call in {@link ToolCallStatus#RUNNING} state. */
		public static ToolCallPart started(String id, Instant timestamp,
				String toolName, String argsJson) {
			return new ToolCallPart(id, timestamp, toolName, argsJson,
				ToolCallStatus.RUNNING, null, null, null, 0L);
		}

		/** Creates a tool call that was denied before execution. */
		public static ToolCallPart denied(String id, Instant timestamp,
				String toolName, String argsJson, String denyReason) {
			return new ToolCallPart(id, timestamp, toolName, argsJson,
				ToolCallStatus.DENIED, null, null, denyReason, 0L);
		}

		/** Transitions this call to {@link ToolCallStatus#COMPLETED}. */
		public ToolCallPart completed(String output, Duration duration) {
			return new ToolCallPart(id, timestamp, toolName, argsJson,
				ToolCallStatus.COMPLETED, output, null, null, duration.toMillis());
		}

		/** Transitions this call to {@link ToolCallStatus#FAILED}. */
		public ToolCallPart failed(String error, Duration duration) {
			return new ToolCallPart(id, timestamp, toolName, argsJson,
				ToolCallStatus.FAILED, null, error, null, duration.toMillis());
		}

		/**
		 * Transitions this call to {@link ToolCallStatus#ROLLED_BACK}, keeping what it reported.
		 *
		 * <p>
		 * The real SQL of a turn runs only at the turn boundary - every write tool commits its own
		 * transaction <em>nested</em> inside the turn's, which writes nothing - so a
		 * {@link ToolCallStatus#COMPLETED} write is a provisional claim until the turn commits. Once
		 * the turn is rolled back the claim is void, and the recorded history has to say so;
		 * a transcript reloaded from the database would otherwise show a green tool call for a
		 * change that was never persisted.
		 * </p>
		 *
		 * <p>
		 * The result is kept rather than dropped: it is what the model was shown while the turn ran,
		 * and a reader of the transcript needs to see the claim next to its retraction.
		 * </p>
		 *
		 * @param reason
		 *        Why the turn was rolled back.
		 */
		public ToolCallPart rolledBack(String reason) {
			return new ToolCallPart(id, timestamp, toolName, argsJson,
				ToolCallStatus.ROLLED_BACK, result, reason, null, durationMs);
		}
	}

	// ---- User interaction parts ----

	/** Approval decision made by the user. */
	enum ApprovalDecision {
		/** User approved this tool call. */
		APPROVED,
		/** User approved this tool for the remainder of the session. */
		APPROVED_FOR_SESSION,
		/** User denied this tool call. */
		DENIED,
		/** Approval request timed out without a response. */
		TIMED_OUT
	}

	/**
	 * An approval request and the user's decision.
	 *
	 * @param id part identifier
	 * @param timestamp when the approval was requested
	 * @param toolCallId associated tool call identifier
	 * @param toolName name of the tool requiring approval
	 * @param argsJson tool arguments as JSON
	 * @param displayReason human-readable reason shown to user
	 * @param decision user's decision, or {@code null} if pending
	 * @param userComment optional user comment
	 * @param resolvedAt when the decision was made, or {@code null}
	 */
	record ApprovalPart(
			String id, Instant timestamp, String toolCallId,
			String toolName, String argsJson, String displayReason,
			ApprovalDecision decision, String userComment, Instant resolvedAt
	) implements Part {

		/** Creates a pending approval request awaiting user decision. */
		public static ApprovalPart pending(String id, Instant timestamp,
				String toolCallId, String toolName, String argsJson,
				String displayReason) {
			return new ApprovalPart(id, timestamp, toolCallId, toolName,
				argsJson, displayReason, null, null, null);
		}

		/** Records the user's approval decision. */
		public ApprovalPart resolve(ApprovalDecision choice,
				String comment, Instant resolvedTime) {
			return new ApprovalPart(id, timestamp, toolCallId, toolName,
				argsJson, displayReason, choice, comment, resolvedTime);
		}
	}

	/**
	 * An {@code askUser} interaction (text input, choice selection, etc.).
	 *
	 * @param id part identifier
	 * @param timestamp when the interaction was created
	 * @param interactionType kind of interaction (text, choice, etc.)
	 * @param question the question posed to the user
	 * @param header optional header text
	 * @param options available choices, or empty
	 * @param multiSelect whether multiple selections are allowed
	 * @param userResponse user's response, or {@code null} if pending
	 * @param resolvedAt when the user responded, or {@code null}
	 */
	record InteractionPart(
			String id, Instant timestamp, String interactionType,
			String question, String header, List<String> options,
			boolean multiSelect, String userResponse, Instant resolvedAt
	) implements Part {

		/** Creates a pending interaction awaiting user response. */
		public static InteractionPart pending(String id, Instant timestamp,
				String interactionType, String question, String header,
				List<String> options, boolean multiSelect) {
			return new InteractionPart(id, timestamp, interactionType,
				question, header, options, multiSelect, null, null);
		}

		/** Records the user's response to this interaction. */
		public InteractionPart resolve(String answer, Instant resolvedTime) {
			return new InteractionPart(id, timestamp, interactionType,
				question, header, options, multiSelect, answer, resolvedTime);
		}
	}

	// ---- Lifecycle parts ----

	/** Types of system events. */
	enum SystemEventType {
		/** Agent run was cancelled by the user. */
		CANCELLED,
		/** Approval mode was changed during the run. */
		APPROVAL_MODE_CHANGED,
		/** LLM model was switched during the run. */
		MODEL_CHANGED
	}

	/**
	 * System-level event (cancellation, mode change, etc.).
	 *
	 * @param id part identifier
	 * @param timestamp when the event occurred
	 * @param eventType the system event type
	 * @param details human-readable event details
	 */
	record SystemPart(String id, Instant timestamp,
			SystemEventType eventType, String details) implements Part {}

	/**
	 * Sub-agent delegation with nested child parts for full event streaming.
	 *
	 * @param id part identifier
	 * @param timestamp when the delegation started
	 * @param fromAgent parent agent name
	 * @param toAgent child agent name
	 * @param inputSummary summary of delegated input
	 * @param resultSummary summary of delegation result, or {@code null}
	 * @param childParts nested parts from the child agent
	 * @param childSessionId child session identifier, or {@code null}
	 * @param durationMs delegation time in milliseconds
	 * @param turnsUsed number of LLM iterations the sub-agent executed, or -1 if unknown
	 * @param totalUsage cumulative token usage of the sub-agent, or {@code null}
	 * @param stopReason why the sub-agent stopped, or {@code null}
	 */
	record DelegationPart(
			String id, Instant timestamp, String fromAgent, String toAgent,
			String inputSummary, String resultSummary,
			List<Part> childParts,
			String childSessionId, long durationMs,
			int turnsUsed, TokenUsage totalUsage, StopReason stopReason
	) implements Part {

		/** Creates a delegation that is in progress. */
		public static DelegationPart started(String id, Instant timestamp,
				String fromAgent, String toAgent, String inputSummary) {
			return new DelegationPart(id, timestamp, fromAgent, toAgent,
				inputSummary, null, new CopyOnWriteArrayList<>(), null, 0L,
				-1, null, null);
		}

		/** Records the delegation result after the child agent finishes. */
		public DelegationPart completed(String summary, String sessionId,
				long elapsedMs, int childTurnsUsed, TokenUsage childUsage,
				StopReason childStopReason) {
			return new DelegationPart(id, timestamp, fromAgent, toAgent,
				inputSummary, summary, childParts, sessionId, elapsedMs,
				childTurnsUsed, childUsage, childStopReason);
		}
	}

	// ---- Observability parts ----

	/**
	 * Context compaction event.
	 *
	 * @param id part identifier
	 * @param timestamp when compaction occurred
	 * @param tokensBefore token count before compaction
	 * @param tokensAfter token count after compaction
	 * @param trigger what triggered the compaction
	 */
	record CompactionPart(String id, Instant timestamp,
			int tokensBefore, int tokensAfter, String trigger) implements Part {}

	/**
	 * Provider error.
	 *
	 * @param id part identifier
	 * @param timestamp when the error occurred
	 * @param kind error category
	 * @param message error details
	 */
	record ErrorPart(String id, Instant timestamp,
			String kind, String message) implements Part {}

	/** KB transaction type. */
	enum TransactionType {
		/** Transaction was started. */
		BEGAN,
		/** Transaction was committed successfully. */
		COMMITTED,
		/** Transaction was rolled back. */
		ROLLED_BACK
	}

	/**
	 * KB transaction event.
	 *
	 * @param id part identifier
	 * @param timestamp when the transaction event occurred
	 * @param type the transaction event type
	 * @param revisionNumber KB revision number, or 0
	 * @param reason human-readable reason
	 */
	record TransactionPart(String id, Instant timestamp,
			TransactionType type, long revisionNumber, String reason) implements Part {}
}
