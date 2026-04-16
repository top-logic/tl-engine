/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.session;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A content unit within a {@link Turn}. Each variant represents a different type
 * of event or content produced during an agent run.
 *
 * <p>Parts are the atoms of the conversation log. They are grouped into turns
 * (one turn = one user-assistant cycle) and persisted as the session history.</p>
 */
public sealed interface Part {

	/** */
	String id();

	/** */
	Instant timestamp();

	// ---- Content parts ----

	/** LLM text output (final, accumulated — not streaming deltas). */
	record TextPart(String id, Instant timestamp, String text) implements Part {}

	/** Extended thinking / reasoning block. */
	record ThinkingPart(String id, Instant timestamp, String text) implements Part {}

	// ---- Tool call with state machine ----

	/** Status of a tool call through its lifecycle. */
	enum ToolCallStatus { RUNNING, COMPLETED, FAILED, DENIED }

	/**
	 * A tool invocation with its full lifecycle state.
	 *
	 * <p>Created via {@link #started} or {@link #denied} factory methods.
	 * Transitions via {@link #completed} or {@link #failed}.</p>
	 */
	record ToolCallPart(
			String id, Instant timestamp, String toolName, String argsJson,
			ToolCallStatus status, String result, String errorMessage,
			String denyReason, long durationMs
	) implements Part {

		/** */
		public static ToolCallPart started(String id, Instant timestamp,
				String toolName, String argsJson) {
			return new ToolCallPart(id, timestamp, toolName, argsJson,
				ToolCallStatus.RUNNING, null, null, null, 0L);
		}

		/** */
		public static ToolCallPart denied(String id, Instant timestamp,
				String toolName, String argsJson, String denyReason) {
			return new ToolCallPart(id, timestamp, toolName, argsJson,
				ToolCallStatus.DENIED, null, null, denyReason, 0L);
		}

		/** */
		public ToolCallPart completed(String result, Duration duration) {
			return new ToolCallPart(id, timestamp, toolName, argsJson,
				ToolCallStatus.COMPLETED, result, null, null, duration.toMillis());
		}

		/** */
		public ToolCallPart failed(String errorMessage, Duration duration) {
			return new ToolCallPart(id, timestamp, toolName, argsJson,
				ToolCallStatus.FAILED, null, errorMessage, null, duration.toMillis());
		}
	}

	// ---- User interaction parts ----

	/** Approval decision made by the user. */
	enum ApprovalDecision { APPROVED, APPROVED_FOR_SESSION, DENIED, TIMED_OUT }

	/** An approval request and the user's decision. */
	record ApprovalPart(
			String id, Instant timestamp, String toolCallId,
			String toolName, String argsJson, String displayReason,
			ApprovalDecision decision, String userComment, Instant resolvedAt
	) implements Part {

		/** */
		public static ApprovalPart pending(String id, Instant timestamp,
				String toolCallId, String toolName, String argsJson,
				String displayReason) {
			return new ApprovalPart(id, timestamp, toolCallId, toolName,
				argsJson, displayReason, null, null, null);
		}

		/** */
		public ApprovalPart resolve(ApprovalDecision decision,
				String userComment, Instant resolvedAt) {
			return new ApprovalPart(id, timestamp, toolCallId, toolName,
				argsJson, displayReason, decision, userComment, resolvedAt);
		}
	}

	/** An askUser interaction (text input, choice selection, etc.). */
	record InteractionPart(
			String id, Instant timestamp, String interactionType,
			String question, String header, List<String> options,
			boolean multiSelect, String userResponse, Instant resolvedAt
	) implements Part {

		/** */
		public static InteractionPart pending(String id, Instant timestamp,
				String interactionType, String question, String header,
				List<String> options, boolean multiSelect) {
			return new InteractionPart(id, timestamp, interactionType,
				question, header, options, multiSelect, null, null);
		}

		/** */
		public InteractionPart resolve(String userResponse, Instant resolvedAt) {
			return new InteractionPart(id, timestamp, interactionType,
				question, header, options, multiSelect, userResponse, resolvedAt);
		}
	}

	// ---- Lifecycle parts ----

	/** Types of system events. */
	enum SystemEventType {
		/** */
		CANCELLED,
		/** */
		APPROVAL_MODE_CHANGED,
		/** */
		MODEL_CHANGED
	}

	/** System-level event (cancellation, mode change, etc.). */
	record SystemPart(String id, Instant timestamp,
			SystemEventType eventType, String details) implements Part {}

	/** Sub-agent delegation with nested child parts for full event streaming. */
	record DelegationPart(
			String id, Instant timestamp, String fromAgent, String toAgent,
			String inputSummary, String resultSummary,
			List<Part> childParts,
			String childSessionId, long durationMs
	) implements Part {

		/** */
		public static DelegationPart started(String id, Instant timestamp,
				String fromAgent, String toAgent, String inputSummary) {
			return new DelegationPart(id, timestamp, fromAgent, toAgent,
				inputSummary, null, new CopyOnWriteArrayList<>(), null, 0L);
		}

		/** */
		public DelegationPart completed(String resultSummary,
				String childSessionId, long durationMs) {
			return new DelegationPart(id, timestamp, fromAgent, toAgent,
				inputSummary, resultSummary, childParts, childSessionId, durationMs);
		}
	}

	// ---- Observability parts ----

	/** Context compaction event. */
	record CompactionPart(String id, Instant timestamp,
			int tokensBefore, int tokensAfter, String trigger) implements Part {}

	/** Provider error. */
	record ErrorPart(String id, Instant timestamp,
			String kind, String message) implements Part {}

	/** KB transaction type. */
	enum TransactionType {
		/** */
		BEGAN,
		/** */
		COMMITTED,
		/** */
		ROLLED_BACK
	}

	/** KB transaction event. */
	record TransactionPart(String id, Instant timestamp,
			TransactionType type, long revisionNumber, String reason) implements Part {}
}
