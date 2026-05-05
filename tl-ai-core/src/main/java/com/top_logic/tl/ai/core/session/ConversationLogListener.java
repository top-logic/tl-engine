/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.session;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.top_logic.tl.ai.core.agent.AgentResult;
import com.top_logic.tl.ai.core.agent.approval.ApprovalOutcome;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.ThinkingContent;

/**
 * Converts {@link AgentEvent}s into {@link Part}s and appends them to a
 * {@link ConversationLog}.
 *
 * <p>Replaces {@code TimelineAgentListener} and {@code ToolCallTracking}.</p>
 *
 * <h2>Turn mapping</h2>
 * <p>One {@link ConversationLog} {@link Turn} = one user request = one agent Run.
 * The {@code AgentLoop}'s internal turns (multiple LLM iterations) all accumulate
 * parts within the same {@link Turn}. The {@link Turn} is created by {@code ChatProcessor}
 * before the agent starts; this listener reuses it.</p>
 *
 * <h2>Text streaming</h2>
 * <p>Text deltas are buffered internally and flushed to a {@link Part.TextPart}
 * before the first tool call or at run end. Real-time SSE streaming of deltas
 * is handled separately by {@code ChatSSEHandler}.</p>
 *
 * <h2>{@code AskUser} interactions</h2>
 * <p>Not modeled as {@link AgentEvent}s. {@code AskUserTool} calls
 * {@link #recordInteractionStarted} and {@link #resolveInteraction} directly
 * via a thread-local reference to this listener.</p>
 */
public class ConversationLogListener implements AgentEventListener {

	/**
	 * Returns the {@link ConversationLogListener} bound to the current thread, or {@code null}.
	 *
	 * <p>
	 * Delegates to {@link ConversationLogListenerBinding#getListener()} so that the listener set by
	 * {@code ChatProcessor} is visible to tools like {@code DelegationTool}.
	 * </p>
	 */
	public static ConversationLogListener current() {
		return ConversationLogListenerBinding.getListener();
	}

	private final ConversationLog _log;

	private final StringBuilder _textBuffer = new StringBuilder();

	private final StringBuilder _thinkingBuffer = new StringBuilder();

	private final Map<String, Part.DelegationPart> _activeDelegations = new ConcurrentHashMap<>();

	/**
	 * Optional parent listener that receives delegated child-agent events which must reach the
	 * browser (e.g. approval requests). Set by the chat layer after constructing the composite
	 * listener so that sub-agent approval prompts are forwarded to the SSE handler.
	 */
	private volatile AgentEventListener _parentListener;

	/** Creates a {@link ConversationLogListener}. */
	public ConversationLogListener(ConversationLog log) {
		_log = log;
	}

	/**
	 * Sets the parent listener that child-agent events (approval requests) should be forwarded to.
	 *
	 * <p>
	 * Must be called after the composite listener (including the SSE handler) has been assembled.
	 * </p>
	 */
	public void setParentListener(AgentEventListener parentListener) {
		_parentListener = parentListener;
	}

	/** */
	@Override
	public void onEvent(AgentEvent event) {
		if (event instanceof AgentEvent.RunStarted e) {
			handleRunStarted(e);
		} else if (event instanceof AgentEvent.RunCompleted) {
			handleRunCompleted();
		} else if (event instanceof AgentEvent.TurnStarted) {
			// AgentLoop internal turn — do NOT create a new ConversationLog Turn
		} else if (event instanceof AgentEvent.TurnCompleted e) {
			handleTurnCompleted(e);
		} else if (event instanceof AgentEvent.TextDelta e) {
			_textBuffer.append(e.text());
		} else if (event instanceof AgentEvent.ThinkingDelta e) {
			_thinkingBuffer.append(e.text());
		} else if (event instanceof AgentEvent.ToolCallStarted e) {
			handleToolCallStarted(e);
		} else if (event instanceof AgentEvent.ToolCallCompleted e) {
			handleToolCallCompleted(e);
		} else if (event instanceof AgentEvent.ToolCallFailed e) {
			handleToolCallFailed(e);
		} else if (event instanceof AgentEvent.ToolCallDenied e) {
			handleToolCallDenied(e);
		} else if (event instanceof AgentEvent.ToolCallResultTruncated) {
			// Informational — not stored as a separate part
		} else if (event instanceof AgentEvent.ApprovalRequired e) {
			handleApprovalRequired(e);
		} else if (event instanceof AgentEvent.ApprovalResolved e) {
			handleApprovalResolved(e);
		} else if (event instanceof AgentEvent.CompactionStarted e) {
			handleCompactionStarted(e);
		} else if (event instanceof AgentEvent.CompactionCompleted e) {
			handleCompactionCompleted(e);
		} else if (event instanceof AgentEvent.ProviderError e) {
			handleProviderError(e);
		} else if (event instanceof AgentEvent.TransactionBegan e) {
			handleTransactionBegan(e);
		} else if (event instanceof AgentEvent.TransactionCommitted e) {
			handleTransactionCommitted(e);
		} else if (event instanceof AgentEvent.TransactionRolledBack e) {
			handleTransactionRolledBack(e);
		}
	}

	// ---- Run lifecycle ----

	private void handleRunStarted(AgentEvent.RunStarted e) {
		Turn turn = _log.currentTurn();
		if (turn == null) {
			turn = _log.startTurn();
		}
		turn.setModel(e.model());
	}

	private void handleRunCompleted() {
		Turn turn = _log.currentTurn();
		if (turn != null) {
			flushTextBuffer(turn);
			flushThinkingBuffer(turn);
		}
		_log.completeTurn();
	}

	private void handleTurnCompleted(AgentEvent.TurnCompleted e) {
		Turn turn = _log.currentTurn();
		if (turn != null) {
			turn.addUsage(e.turnUsage());
			backfillContinuityTokens(turn, e.contentParts());
		}
	}

	private void backfillContinuityTokens(Turn turn, List<ContentPart> contentParts) {
		List<ThinkingContent> thinkingContents = contentParts.stream()
			.filter(ThinkingContent.class::isInstance)
			.map(ThinkingContent.class::cast)
			.toList();
		if (thinkingContents.isEmpty()) {
			return;
		}
		List<Part.ThinkingPart> thinkingParts = turn.assistantParts().stream()
			.filter(Part.ThinkingPart.class::isInstance)
			.map(Part.ThinkingPart.class::cast)
			.toList();
		int count = Math.min(thinkingParts.size(), thinkingContents.size());
		for (int i = 0; i < count; i++) {
			String token = thinkingContents.get(i).continuityToken();
			if (token != null) {
				Part.ThinkingPart old = thinkingParts.get(i);
				int idx = turn.assistantParts().indexOf(old);
				if (idx >= 0) {
					turn.assistantParts().set(idx, new Part.ThinkingPart(
						old.id(), old.timestamp(), old.text(), token));
				}
			}
		}
	}

	// ---- Tool calls ----

	private void handleToolCallStarted(AgentEvent.ToolCallStarted e) {
		Turn turn = _log.currentTurn();
		if (turn != null) {
			flushTextBuffer(turn);
			flushThinkingBuffer(turn);
			Part.ToolCallPart part = Part.ToolCallPart.started(
				e.toolCallId(), e.timestamp(), e.toolName(), e.argsJson());
			turn.addAssistantPart(part);
		}
	}

	private void handleToolCallCompleted(AgentEvent.ToolCallCompleted e) {
		Turn turn = _log.currentTurn();
		if (turn != null) {
			Part existing = turn.findPart(e.toolCallId());
			if (existing instanceof Part.ToolCallPart tc) {
				turn.updatePart(e.toolCallId(),
					tc.completed(e.result(), e.duration()));
			}
		}
	}

	private void handleToolCallFailed(AgentEvent.ToolCallFailed e) {
		Turn turn = _log.currentTurn();
		if (turn != null) {
			Part existing = turn.findPart(e.toolCallId());
			if (existing instanceof Part.ToolCallPart tc) {
				turn.updatePart(e.toolCallId(),
					tc.failed(e.errorMessage(), e.duration()));
			}
		}
	}

	private void handleToolCallDenied(AgentEvent.ToolCallDenied e) {
		Turn turn = _log.currentTurn();
		if (turn != null) {
			flushTextBuffer(turn);
			Part.ToolCallPart denied = Part.ToolCallPart.denied(
				e.toolCallId(), e.timestamp(), e.toolName(), "", e.reason());
			turn.addAssistantPart(denied);
		}
	}

	// ---- Approvals ----

	private void handleApprovalRequired(AgentEvent.ApprovalRequired e) {
		Turn turn = _log.currentTurn();
		if (turn != null) {
			Part.ApprovalPart part = Part.ApprovalPart.pending(
				e.approvalRequestId(), e.timestamp(), e.toolCallId(),
				e.toolName(), argsMapToJson(e.args()), e.displayReason());
			turn.addAssistantPart(part);
		}
	}

	private void handleApprovalResolved(AgentEvent.ApprovalResolved e) {
		Turn turn = _log.currentTurn();
		if (turn != null) {
			Part existing = turn.findPart(e.approvalRequestId());
			if (existing instanceof Part.ApprovalPart ap) {
				Part.ApprovalDecision decision = mapOutcome(e.outcome());
				turn.updatePart(ap.id(),
					ap.resolve(decision, null, Instant.now()));
			}
		}
	}

	// ---- Compaction ----

	private void handleCompactionStarted(AgentEvent.CompactionStarted e) {
		Turn turn = _log.currentTurn();
		if (turn != null) {
			turn.addAssistantPart(new Part.CompactionPart(
				UUID.randomUUID().toString(), e.timestamp(),
				e.tokensBefore(), 0, e.trigger()));
		}
	}

	private void handleCompactionCompleted(AgentEvent.CompactionCompleted e) {
		Turn turn = _log.currentTurn();
		if (turn != null) {
			List<Part> parts = turn.assistantParts();
			for (int i = parts.size() - 1; i >= 0; i--) {
				Part p = parts.get(i);
				if (p instanceof Part.CompactionPart cp) {
					turn.updatePart(cp.id(), new Part.CompactionPart(
						cp.id(), cp.timestamp(), cp.tokensBefore(),
						e.result().tokensAfter(), cp.trigger()));
					break;
				}
			}
		}
	}

	// ---- Errors ----

	private void handleProviderError(AgentEvent.ProviderError e) {
		Turn turn = _log.currentTurn();
		if (turn != null) {
			turn.addAssistantPart(new Part.ErrorPart(
				UUID.randomUUID().toString(), e.timestamp(),
				e.kind(), e.message()));
		}
	}

	// ---- Transactions ----

	private void handleTransactionBegan(AgentEvent.TransactionBegan e) {
		Turn turn = _log.currentTurn();
		if (turn != null) {
			turn.addAssistantPart(new Part.TransactionPart(
				UUID.randomUUID().toString(), e.timestamp(),
				Part.TransactionType.BEGAN, -1, null));
		}
	}

	private void handleTransactionCommitted(AgentEvent.TransactionCommitted e) {
		Turn turn = _log.currentTurn();
		if (turn != null) {
			turn.addAssistantPart(new Part.TransactionPart(
				UUID.randomUUID().toString(), e.timestamp(),
				Part.TransactionType.COMMITTED, e.revisionNumber(), null));
		}
	}

	private void handleTransactionRolledBack(AgentEvent.TransactionRolledBack e) {
		Turn turn = _log.currentTurn();
		if (turn != null) {
			turn.addAssistantPart(new Part.TransactionPart(
				UUID.randomUUID().toString(), e.timestamp(),
				Part.TransactionType.ROLLED_BACK, -1, e.reason()));
		}
	}

	// ---- AskUser interaction support ----

	/**
	 * Record the start of an {@code askUser} interaction. Returns the part ID.
	 */
	public String recordInteractionStarted(String interactionType, String question,
			String header, List<String> options, boolean multiSelect) {
		Turn turn = _log.currentTurn();
		if (turn == null) {
			return null;
		}
		String id = "interaction-" + UUID.randomUUID().toString().substring(0, 8);
		Part.InteractionPart part = Part.InteractionPart.pending(
			id, Instant.now(), interactionType, question, header, options, multiSelect);
		turn.addAssistantPart(part);
		return id;
	}

	/**
	 * Resolve a pending interaction with the user's response.
	 */
	public void resolveInteraction(String partId, String userResponse) {
		Turn turn = _log.currentTurn();
		if (turn != null) {
			Part existing = turn.findPart(partId);
			if (existing instanceof Part.InteractionPart ip) {
				turn.updatePart(partId, ip.resolve(userResponse, Instant.now()));
			}
		}
	}

	/**
	 * Resolve an approval with a user comment.
	 */
	public void resolveApproval(String approvalRequestId,
			Part.ApprovalDecision decision, String userComment) {
		Turn turn = _log.currentTurn();
		if (turn != null) {
			Part existing = turn.findPart(approvalRequestId);
			if (existing instanceof Part.ApprovalPart ap) {
				turn.updatePart(ap.id(),
					ap.resolve(decision, userComment, Instant.now()));
			}
		}
	}

	/** Records a user cancellation as a system event on the current turn. */
	public void recordCancellation() {
		Turn turn = _log.currentTurn();
		if (turn != null) {
			turn.addAssistantPart(new Part.SystemPart(
				UUID.randomUUID().toString(), Instant.now(),
				Part.SystemEventType.CANCELLED, "User cancelled processing"));
		}
	}

	// ---- Delegation lifecycle ----

	/**
	 * Record the start of a delegation. Returns the delegation part ID.
	 */
	public String recordDelegationStarted(String fromAgent, String toAgent, String inputSummary) {
		Turn turn = _log.currentTurn();
		if (turn == null) {
			return null;
		}
		flushTextBuffer(turn);
		flushThinkingBuffer(turn);
		String id = "delegation-" + UUID.randomUUID().toString().substring(0, 8);
		Part.DelegationPart part = Part.DelegationPart.started(id, Instant.now(), fromAgent, toAgent, inputSummary);
		turn.addAssistantPart(part);
		_activeDelegations.put(id, part);
		return id;
	}

	/**
	 * Returns a listener that forwards events into the delegation's child parts and bubbles
	 * UI-relevant events (tool calls, text, approval) to the parent listener wrapped in
	 * {@link com.top_logic.tl.ai.core.agent.event.AgentEvent.DelegatedEvent}.
	 *
	 * @param delegationId
	 *        the {@link ConversationLog} part ID for this delegation
	 * @param delegationToolCallId
	 *        the parent agent's tool call ID for the {@code delegate_to_*} tool, used by the SSE
	 *        handler to route events to the correct delegation in the browser
	 */
	public AgentEventListener delegationListener(String delegationId, String delegationToolCallId) {
		return new DelegationForwardingListener(delegationId, delegationToolCallId);
	}

	/**
	 * Resolve a completed delegation with result summary, duration, and sub-agent metadata.
	 */
	public void resolveDelegation(String delegationId, String resultSummary,
			long durationMs, AgentResult agentResult) {
		_activeDelegations.remove(delegationId);
		Turn turn = _log.currentTurn();
		if (turn != null) {
			Part existing = turn.findPart(delegationId);
			if (existing instanceof Part.DelegationPart dp) {
				turn.updatePart(delegationId, dp.completed(resultSummary, null, durationMs,
					agentResult.turnsUsed(), agentResult.totalUsage(), agentResult.stopReason()));
			}
		}
	}

	// ---- Delegation forwarding inner class ----

	private class DelegationForwardingListener implements AgentEventListener {

		private final String _delegationId;

		private final String _delegationToolCallId;

		private final StringBuilder _childTextBuffer = new StringBuilder();

		private final StringBuilder _childThinkingBuffer = new StringBuilder();

		DelegationForwardingListener(String delegationId, String delegationToolCallId) {
			_delegationId = delegationId;
			_delegationToolCallId = delegationToolCallId;
		}

		@Override
		public void onEvent(AgentEvent event) {
			Part.DelegationPart dp = _activeDelegations.get(_delegationId);
			if (dp == null) {
				return;
			}

			if (event instanceof AgentEvent.TextDelta e) {
				_childTextBuffer.append(e.text());
				forwardToParent(event);
			} else if (event instanceof AgentEvent.ThinkingDelta e) {
				_childThinkingBuffer.append(e.text());
				forwardToParent(event);
			} else if (event instanceof AgentEvent.ToolCallStarted e) {
				flushChildBuffers(dp);
				dp.childParts().add(Part.ToolCallPart.started(
					e.toolCallId(), e.timestamp(), e.toolName(), e.argsJson()));
				forwardToParent(event);
			} else if (event instanceof AgentEvent.ToolCallCompleted e) {
				updateChildPart(dp, e.toolCallId(), p -> {
					if (p instanceof Part.ToolCallPart tc) {
						return tc.completed(e.result(), e.duration());
					}
					return p;
				});
				forwardToParent(event);
			} else if (event instanceof AgentEvent.ToolCallFailed e) {
				updateChildPart(dp, e.toolCallId(), p -> {
					if (p instanceof Part.ToolCallPart tc) {
						return tc.failed(e.errorMessage(), e.duration());
					}
					return p;
				});
				forwardToParent(event);
			} else if (event instanceof AgentEvent.ToolCallDenied) {
				forwardToParent(event);
			} else if (event instanceof AgentEvent.ApprovalRequired
				|| event instanceof AgentEvent.ApprovalResolved) {
				forwardToParent(event);
			} else if (event instanceof AgentEvent.RunStarted
				|| event instanceof AgentEvent.TurnStarted
				|| event instanceof AgentEvent.ProviderError) {
				forwardToParent(event);
			} else if (event instanceof AgentEvent.RunCompleted) {
				flushChildBuffers(dp);
				forwardToParent(event);
			} else if (event instanceof AgentEvent.TurnCompleted) {
				forwardToParent(event);
			}
		}

		/**
		 * Wraps the child event in a
		 * {@link com.top_logic.tl.ai.core.agent.event.AgentEvent.DelegatedEvent} and forwards it to
		 * the parent listener so the SSE handler can route it to the correct delegation in the
		 * browser.
		 */
		private void forwardToParent(AgentEvent childEvent) {
			AgentEventListener parent = _parentListener;
			if (parent != null) {
				parent.onEvent(new AgentEvent.DelegatedEvent(
					childEvent.runId(), childEvent.turnNumber(), childEvent.timestamp(),
					_delegationToolCallId, childEvent));
			}
		}

		private void flushChildBuffers(Part.DelegationPart dp) {
			if (_childTextBuffer.length() > 0) {
				dp.childParts().add(new Part.TextPart(
					UUID.randomUUID().toString(), Instant.now(), _childTextBuffer.toString()));
				_childTextBuffer.setLength(0);
			}
			if (_childThinkingBuffer.length() > 0) {
				dp.childParts().add(new Part.ThinkingPart(
					UUID.randomUUID().toString(), Instant.now(), _childThinkingBuffer.toString(), null));
				_childThinkingBuffer.setLength(0);
			}
		}

		private void updateChildPart(Part.DelegationPart dp, String partId,
				Function<Part, Part> updater) {
			List<Part> parts = dp.childParts();
			for (int i = parts.size() - 1; i >= 0; i--) {
				if (parts.get(i).id().equals(partId)) {
					parts.set(i, updater.apply(parts.get(i)));
					break;
				}
			}
		}
	}

	// ---- Buffer flushing ----

	private void flushTextBuffer(Turn turn) {
		if (_textBuffer.length() > 0) {
			turn.addAssistantPart(new Part.TextPart(
				UUID.randomUUID().toString(), Instant.now(),
				_textBuffer.toString()));
			_textBuffer.setLength(0);
		}
	}

	private void flushThinkingBuffer(Turn turn) {
		if (_thinkingBuffer.length() > 0) {
			turn.addAssistantPart(new Part.ThinkingPart(
				UUID.randomUUID().toString(), Instant.now(),
				_thinkingBuffer.toString(), null));
			_thinkingBuffer.setLength(0);
		}
	}

	private static Part.ApprovalDecision mapOutcome(ApprovalOutcome outcome) {
		switch (outcome) {
			case APPROVED:
				return Part.ApprovalDecision.APPROVED;
			case APPROVED_FOR_SESSION:
				return Part.ApprovalDecision.APPROVED_FOR_SESSION;
			case REJECTED:
				return Part.ApprovalDecision.DENIED;
			case TIMED_OUT:
				return Part.ApprovalDecision.TIMED_OUT;
			default:
				throw new IllegalArgumentException("Unknown outcome: " + outcome);
		}
	}

	private static String argsMapToJson(Map<String, Object> map) {
		if (map == null || map.isEmpty()) {
			return "{}";
		}
		StringBuilder sb = new StringBuilder("{");
		boolean first = true;
		for (var entry : map.entrySet()) {
			if (!first) sb.append(",");
			sb.append("\"");
			appendEscaped(sb, entry.getKey());
			sb.append("\":\"");
			appendEscaped(sb, String.valueOf(entry.getValue()));
			sb.append("\"");
			first = false;
		}
		sb.append("}");
		return sb.toString();
	}

	private static void appendEscaped(StringBuilder sb, String value) {
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			switch (c) {
				case '"':
					sb.append("\\\"");
					break;
				case '\\':
					sb.append("\\\\");
					break;
				case '\n':
					sb.append("\\n");
					break;
				case '\r':
					sb.append("\\r");
					break;
				case '\t':
					sb.append("\\t");
					break;
				default:
					sb.append(c);
					break;
			}
		}
	}
}
