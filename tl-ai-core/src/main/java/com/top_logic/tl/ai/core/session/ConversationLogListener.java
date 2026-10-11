/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.session;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.top_logic.basic.StringServices;
import com.top_logic.tl.ai.core.agent.AgentResult;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.approval.ApprovalOutcome;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.agent.subagent.SubagentNews;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.ThinkingContent;
import com.top_logic.tl.ai.core.provider.LLMProviderService;
import com.top_logic.tl.ai.tool.api.ToolMode;

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

	/**
	 * Guards {@link #_textBuffer} and {@link #_thinkingBuffer}.
	 *
	 * <p>
	 * The buffers are filled and flushed by the agent thread alone, but {@link #bufferedText()} is
	 * read from a request thread while that is happening - a window opening the chat asks what the
	 * running turn has streamed so far. Reading a {@link StringBuilder} another thread is appending
	 * to is not merely stale, it can fail, so the two operations take the same monitor.
	 * </p>
	 */
	private final Object _buffers = new Object();

	private final StringBuilder _textBuffer = new StringBuilder();

	private final StringBuilder _thinkingBuffer = new StringBuilder();

	/**
	 * How many characters of text this run has streamed so far, recorded or not.
	 *
	 * <p>
	 * Only ever grows, and grows together with {@link #_textBuffer}, under the same monitor. That is
	 * what makes it a position in the stream: {@link #streamedText()} reads both at once, so a
	 * reader knows exactly which of the deltas still to come its snapshot already holds (issue
	 * #544).
	 * </p>
	 */
	private long _streamedChars;

	private final Map<String, Part.DelegationPart> _activeDelegations = new ConcurrentHashMap<>();

	/**
	 * Where the {@link Part.SubagentPart} of each running background subagent was recorded, by
	 * subagent id: the turn that started it, which is no longer the current one once the child ends.
	 */
	private final Map<String, SubagentPlace> _runningSubagents = new ConcurrentHashMap<>();

	/**
	 * The ids of the background subagents that were started into the queue and have not left it yet.
	 *
	 * <p>
	 * Kept apart from {@link #_runningSubagents}: a subagent started while no turn was current has no
	 * part, but the chat was still told it waits, and must be told when it starts.
	 * </p>
	 */
	private final Set<String> _queuedSubagents = ConcurrentHashMap.newKeySet();

	/**
	 * @param leadExecution
	 *        The lead's recorded run, whose transcript holds the part; <code>null</code> when the
	 *        run is not recorded.
	 */
	private record SubagentPlace(Turn turn, String partId, AgentExecution leadExecution) {
		// Plain data.
	}

	/**
	 * The tool calls inside the currently open KB transaction, or <code>null</code> when none is
	 * open.
	 *
	 * <p>
	 * Tracked explicitly rather than derived from the part order, because the position of the
	 * {@link Part.TransactionType#BEGAN} part does not delimit the transaction: the loop opens the
	 * transaction while executing the first write tool, so that tool's
	 * {@link com.top_logic.tl.ai.core.agent.event.AgentEvent.ToolCallStarted} arrives
	 * <em>before</em> {@link com.top_logic.tl.ai.core.agent.event.AgentEvent.TransactionBegan} -
	 * and it is the call that matters most.
	 * </p>
	 */
	private volatile Set<String> _openTransactionCalls;

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

	/** The conversation log this listener records into. */
	public ConversationLog log() {
		return _log;
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
		} else if (event instanceof AgentEvent.RunCompleted e) {
			handleRunCompleted(e);
		} else if (event instanceof AgentEvent.LeadMessageDelivered e) {
			recordInboxMessage(Part.SystemEventType.LEAD_MESSAGE, e.text());
		} else if (event instanceof AgentEvent.FinalTurnRequested e) {
			recordContinuationRequested(e.text());
		} else if (event instanceof AgentEvent.UserMessageDelivered e) {
			recordInboxMessage(Part.SystemEventType.USER_MESSAGE, e.text());
		} else if (event instanceof AgentEvent.LeadWoken e) {
			recordLeadWoken(e);
		} else if (event instanceof AgentEvent.TurnStarted) {
			// AgentLoop internal turn — do NOT create a new ConversationLog Turn
		} else if (event instanceof AgentEvent.ContextComposition e) {
			handleContextComposition(e);
		} else if (event instanceof AgentEvent.TurnCompleted e) {
			handleTurnCompleted(e);
		} else if (event instanceof AgentEvent.TextDelta e) {
			synchronized (_buffers) {
				_textBuffer.append(e.text());
				_streamedChars += e.text().length();
			}
		} else if (event instanceof AgentEvent.ThinkingDelta e) {
			synchronized (_buffers) {
				_thinkingBuffer.append(e.text());
			}
		} else if (event instanceof AgentEvent.TurnContextComposed e) {
			handleTurnContextComposed(e);
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
		} else if (event instanceof AgentEvent.ModelSwitched e) {
			handleModelSwitched(e);
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
		turn.setProvider(resolveProviderId(e.model()));
	}

	/**
	 * Best-effort lookup of the provider id serving the given model; <code>null</code> outside a
	 * running application (e.g. unit tests).
	 */
	private static String resolveProviderId(String model) {
		if (!LLMProviderService.Module.INSTANCE.isActive()) {
			return null;
		}
		try {
			return LLMProviderService.getInstance().getProviderId(model);
		} catch (RuntimeException ex) {
			return null;
		}
	}

	/**
	 * Closes the turn, and says on it when the user stopped the run.
	 *
	 * <p>
	 * The cancel is recorded from the run's own outcome, after what it streamed: a
	 * {@link StopReason#ABORTED} run is one that did stop on the cancel, which the request to
	 * cancel alone does not know - the run may have finished in the meantime.
	 * </p>
	 */
	private void handleRunCompleted(AgentEvent.RunCompleted e) {
		Turn turn = _log.currentTurn();
		if (turn != null) {
			flushTextBuffer(turn);
			flushThinkingBuffer(turn);
			if (e.reason() == StopReason.ABORTED) {
				turn.addAssistantPart(new Part.SystemPart(
					UUID.randomUUID().toString(), e.timestamp(),
					Part.SystemEventType.CANCELLED, "User cancelled processing"));
			}
		}
		_log.completeTurn();
	}

	/**
	 * Records what the run told the model about this turn.
	 *
	 * <p>
	 * Written only once per turn. A delegate and a subagent are told the same thing and emit the
	 * same event into this log, and the first one to arrive is the entry point's own.
	 * </p>
	 */
	private void handleTurnContextComposed(AgentEvent.TurnContextComposed e) {
		Turn turn = _log.currentTurn();
		if (turn != null && turn.turnContext() == null) {
			turn.setTurnContext(e.turnContext());
		}
	}

	private void handleContextComposition(AgentEvent.ContextComposition e) {
		Turn turn = _log.currentTurn();
		if (turn != null) {
			turn.setContextComposition(e);
		}
	}

	/**
	 * Closes what the turn streamed and writes the completed message's continuity tokens into it.
	 *
	 * <p>
	 * The buffers are flushed here first, and not left to the tool call or the run end: a turn that
	 * answers without calling a tool has its thinking flushed only by {@code RunCompleted}, which
	 * arrives after this event. The backfill would then find no thinking part to write the
	 * signature into, and the block would be persisted unsigned - stripped on the next reload,
	 * exactly for the turns that reason without acting.
	 * </p>
	 */
	private void handleTurnCompleted(AgentEvent.TurnCompleted e) {
		Turn turn = _log.currentTurn();
		if (turn != null) {
			flushTextBuffer(turn);
			flushThinkingBuffer(turn);
			turn.addUsage(e.turnUsage());
			backfillContinuityTokens(turn, e.contentParts());
		}
	}

	/**
	 * Writes the continuity tokens of a completed turn into the {@link Part.ThinkingPart}s recorded
	 * for it.
	 *
	 * <p>
	 * The parts are built from streamed deltas, which carry no token - the provider only reveals it
	 * once the thinking block is complete. Without this correction a reloaded session replays the
	 * thinking text without its token, and the provider cannot continue its own reasoning.
	 * </p>
	 *
	 * <p>
	 * Parts and contents are paired positionally as far as both sides reach: a part exists only
	 * once its deltas were flushed, so the completed message may hold more thinking blocks than the
	 * turn holds parts.
	 * </p>
	 */
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
			ThinkingContent content = thinkingContents.get(i);
			String token = content.continuityToken();
			if (token != null) {
				Part.ThinkingPart old = thinkingParts.get(i);
				// The origin travels with the token: a signature says nothing about which provider
				// will accept it back unless it is recorded next to it.
				turn.updatePart(old.id(), new Part.ThinkingPart(
					old.id(), old.timestamp(), old.text(), token, content.provider()));
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
				e.toolCallId(), e.timestamp(), e.toolName(), e.argsJson(), e.mode());
			turn.addAssistantPart(part);
			Set<String> open = _openTransactionCalls;
			if (open != null) {
				open.add(e.toolCallId());
			}
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
				e.toolCallId(), e.timestamp(), e.toolName(), "", null, e.reason());
			turn.addAssistantPart(denied);
		}
	}

	// ---- Approvals ----

	/**
	 * Records the approval the run now waits for.
	 *
	 * <p>
	 * What the turn streamed before is flushed first, as at a tool call: the approval comes before
	 * the call it is about, and the run may wait here for the rest of the day. The text above the
	 * card is what the user reads to decide, and it has to be in the log the run's checkpoint
	 * writes - otherwise a restart during the wait loses exactly that (#598).
	 * </p>
	 */
	private void handleApprovalRequired(AgentEvent.ApprovalRequired e) {
		Turn turn = _log.currentTurn();
		if (turn != null) {
			flushTextBuffer(turn);
			flushThinkingBuffer(turn);
			Part.ApprovalPart part = Part.ApprovalPart.pending(
				e.approvalRequestId(), e.timestamp(), e.toolCallId(),
				e.toolName(), e.argsJson(), e.displayReason(), e.toolMode());
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
			// The compaction marks the end of the history it replaces, so anything still
			// buffered belongs before it - the sidecar is fingerprinted against the parts
			// recorded up to this position and must not be missing the last of them.
			flushTextBuffer(turn);
			flushThinkingBuffer(turn);
			turn.addAssistantPart(Part.CompactionPart.started(
				UUID.randomUUID().toString(), e.timestamp(),
				e.tokensBefore(), e.trigger()));
		}
	}

	private void handleCompactionCompleted(AgentEvent.CompactionCompleted e) {
		Turn turn = _log.currentTurn();
		if (turn != null) {
			List<Part> parts = turn.assistantParts();
			for (int i = parts.size() - 1; i >= 0; i--) {
				Part p = parts.get(i);
				if (p instanceof Part.CompactionPart cp) {
					turn.updatePart(cp.id(), cp.completed(e.result().tokensAfter(),
						sidecarFor(cp.id(), e.result().replacementMessages())));
					break;
				}
			}
		}
	}

	/**
	 * The {@link CompactionSidecar} to record on the given compaction, so that a reloaded
	 * session comes back compacted rather than at the size this compaction just escaped.
	 *
	 * <p>
	 * The replaced history is taken from the log rather than from the live conversation the
	 * compaction actually ran on: a reader has nothing but the log, so the fingerprint has to
	 * be over what the reader will reconstruct from it. The two agree by construction, both
	 * going through {@link ConversationLogReplay}.
	 * </p>
	 */
	private CompactionSidecar sidecarFor(String compactionPartId, List<Message> replacement) {
		return CompactionSidecar.of(
			ConversationLogReplay.replayedHistoryBefore(_log, compactionPartId), replacement);
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

	/**
	 * Records that the rest of the run is answered by another model than the one it started on.
	 *
	 * <p>
	 * A {@link Part.SystemEventType#MODEL_CHANGED} part rather than an
	 * {@link Part.ErrorPart}: the run did not fail, it continued somewhere else, and an operator
	 * reading the transcript needs to know from which part of it onwards the answers come from a
	 * different model. The details carry the reason, which is where the models the run passed over
	 * are named.
	 * </p>
	 *
	 * <p>
	 * The wording is the one the chat composes from the live event, character for character, so a
	 * notice reads the same while it happens as it does after a reload - the lesson of #429.
	 * </p>
	 */
	private void handleModelSwitched(AgentEvent.ModelSwitched e) {
		Turn turn = _log.currentTurn();
		if (turn != null) {
			turn.addAssistantPart(new Part.SystemPart(
				UUID.randomUUID().toString(), e.timestamp(),
				Part.SystemEventType.MODEL_CHANGED,
				e.fromModel() + " -> " + e.toModel() + ": " + e.reason()));
		}
	}

	// ---- Transactions ----

	private void handleTransactionBegan(AgentEvent.TransactionBegan e) {
		Turn turn = _log.currentTurn();
		if (turn != null) {
			// The write tool that made the loop open this transaction is still running, so it is
			// seeded into the window here; every call started while the window is open joins it.
			Set<String> open = ConcurrentHashMap.newKeySet();
			for (Part part : turn.assistantParts()) {
				if (part instanceof Part.ToolCallPart tc
					&& tc.status() == Part.ToolCallStatus.RUNNING) {
					open.add(tc.id());
				}
			}
			_openTransactionCalls = open;
			turn.addAssistantPart(new Part.TransactionPart(
				UUID.randomUUID().toString(), e.timestamp(),
				Part.TransactionType.BEGAN, -1, null));
		}
	}

	private void handleTransactionCommitted(AgentEvent.TransactionCommitted e) {
		_openTransactionCalls = null;
		Turn turn = _log.currentTurn();
		if (turn != null) {
			turn.addAssistantPart(new Part.TransactionPart(
				UUID.randomUUID().toString(), e.timestamp(),
				Part.TransactionType.COMMITTED, e.revisionNumber(), null));
		}
	}

	private void handleTransactionRolledBack(AgentEvent.TransactionRolledBack e) {
		Set<String> open = _openTransactionCalls;
		_openTransactionCalls = null;
		Turn turn = _log.currentTurn();
		if (turn != null) {
			retractCompletedWrites(turn, open, e.reason());
			turn.addAssistantPart(new Part.TransactionPart(
				UUID.randomUUID().toString(), e.timestamp(),
				Part.TransactionType.ROLLED_BACK, -1, e.reason()));
		}
	}

	/**
	 * Marks every tool call that reported a <em>write</em> success inside the rolled-back
	 * transaction as {@link Part.ToolCallStatus#ROLLED_BACK}.
	 *
	 * <p>
	 * A write tool commits only a transaction nested inside the turn's, which writes no SQL, so its
	 * {@code COMPLETED} was a claim about a commit that had not happened yet. Once the turn is rolled
	 * back the claim is void, and the recorded history has to say so - otherwise a transcript
	 * reloaded from the database shows a successful write, and
	 * {@link ConversationLogReplay} feeds the model a successful tool result, for a change that was
	 * never persisted.
	 * </p>
	 *
	 * <p>
	 * Only the calls of <em>this</em> transaction are retracted: the scope lives for the whole run,
	 * one KB transaction per loop turn, and the writes of an earlier turn whose own commit succeeded
	 * are genuinely persisted - retracting those would be the same lie in the other direction. The
	 * amendment is a pure in-memory edit; the log is persisted once, after the run.
	 * </p>
	 *
	 * <p>
	 * Only a call whose {@link Part.ToolCallPart#mode()}
	 * {@link ToolMode#writesApplicationState() writes this application's state} is retracted - the
	 * same question the agent loop asked when it decided whether to open the transaction, so the two
	 * cannot disagree. A {@link ToolMode#READ} call wrote nothing and a {@link ToolMode#EXTERNAL} one
	 * wrote to a system this rollback cannot reach; both keep their {@code COMPLETED} result, and a
	 * reloaded conversation keeps what they did. For {@code EXTERNAL} that is not leniency but
	 * accuracy: the remote effect happened and still stands, and marking it rolled back would tell
	 * the model something untrue about the world.
	 * </p>
	 *
	 * <p>
	 * An <em>unknown</em> ({@code null}) mode - a log written before the mode was recorded, or a tool
	 * the loop could not resolve - is retracted. Unknown counts as a write on purpose: guessing wrong
	 * the other way tells the model a write landed that did not.
	 * </p>
	 */
	private static void retractCompletedWrites(Turn turn, Set<String> openCalls, String reason) {
		if (openCalls == null || openCalls.isEmpty()) {
			return;
		}
		List<Part.ToolCallPart> retracted = new ArrayList<>();
		for (Part part : turn.assistantParts()) {
			if (part instanceof Part.ToolCallPart tc
				&& tc.status() == Part.ToolCallStatus.COMPLETED
				&& openCalls.contains(tc.id())
				&& (tc.mode() == null || tc.mode().writesApplicationState())) {
				retracted.add(tc);
			}
		}
		for (Part.ToolCallPart tc : retracted) {
			turn.updatePart(tc.id(), tc.rolledBack(reason));
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

	/**
	 * Records what the turn-start injection told the model about its finished subagents.
	 *
	 * <p>
	 * Written as a {@link Part.SystemEventType#SUBAGENT_RESULT} part rather than a user part:
	 * it is not the user's text, so the chat must not draw it as a user bubble, but it <em>is</em>
	 * conversation content, so the transcript has to hold it and
	 * {@link ConversationLogReplay} has to put it back on reload.
	 * </p>
	 *
	 * @param text
	 *        Verbatim the text the model was shown.
	 *
	 * @see com.top_logic.tl.ai.core.agent.subagent.SubagentResultDelivery
	 */
	public void recordSubagentResultsDelivered(String text) {
		Turn turn = _log.currentTurn();
		if (turn == null) {
			return;
		}
		// The injection happens between the model's messages, so whatever the previous turn was
		// still buffering ends before it - as for any other part that is a message boundary.
		flushTextBuffer(turn);
		flushThinkingBuffer(turn);
		turn.addAssistantPart(new Part.SystemPart(
			UUID.randomUUID().toString(), Instant.now(),
			Part.SystemEventType.SUBAGENT_RESULT, text));
	}

	/**
	 * Records a message of a subagent's lead or of the user that the subagent read at the start of a
	 * turn.
	 *
	 * <p>
	 * A {@link Part.SystemEventType#LEAD_MESSAGE} or {@link Part.SystemEventType#USER_MESSAGE} part,
	 * for the reason {@link #recordSubagentResultsDelivered(String)} gives. Recorded from the
	 * {@link com.top_logic.tl.ai.core.agent.event.AgentEvent.LeadMessageDelivered} and
	 * {@link com.top_logic.tl.ai.core.agent.event.AgentEvent.UserMessageDelivered} events rather than
	 * through the thread-bound listener: on a subagent's thread that one records the lead's log, and
	 * the message belongs in the subagent's.
	 * </p>
	 */
	private void recordInboxMessage(Part.SystemEventType type, String text) {
		Turn turn = _log.currentTurn();
		if (turn == null) {
			return;
		}
		flushTextBuffer(turn);
		flushThinkingBuffer(turn);
		turn.addAssistantPart(new Part.SystemPart(UUID.randomUUID().toString(), Instant.now(), type, text));
	}

	/**
	 * Records which subagents' news a lead turn no message of the user started opens with, so that a
	 * reloaded chat says who reported back and why, as the live one did.
	 */
	private void recordLeadWoken(AgentEvent.LeadWoken e) {
		Turn turn = _log.currentTurn();
		if (turn == null || e.news().isEmpty()) {
			return;
		}
		turn.addAssistantPart(new Part.SystemPart(
			UUID.randomUUID().toString(), Instant.now(),
			Part.SystemEventType.LEAD_WOKEN, SubagentNews.toJson(e.news())));
	}

	/**
	 * Records that the run asked the model to continue after a reply that said nothing.
	 *
	 * <p>
	 * A {@link Part.SystemEventType#CONTINUATION_REQUESTED} part, for the reason
	 * {@link #recordSubagentResultsDelivered(String)} gives: it is not the user's text, but it is
	 * conversation content.
	 * </p>
	 *
	 * @param text
	 *        Verbatim the text the model was shown.
	 */
	public void recordContinuationRequested(String text) {
		Turn turn = _log.currentTurn();
		if (turn == null) {
			return;
		}
		flushTextBuffer(turn);
		flushThinkingBuffer(turn);
		turn.addAssistantPart(new Part.SystemPart(
			UUID.randomUUID().toString(), Instant.now(),
			Part.SystemEventType.CONTINUATION_REQUESTED, text));
	}

	// ---- Delegation lifecycle ----

	/**
	 * Record the start of a delegation. Returns the delegation part ID.
	 *
	 * @param parentToolCallId
	 *        The id of the lead's {@code delegate_to_*} call, where the chat draws the delegation's
	 *        card; may be <code>null</code>.
	 */
	public String recordDelegationStarted(String fromAgent, String toAgent, String inputSummary,
			String parentToolCallId) {
		Turn turn = _log.currentTurn();
		if (turn == null) {
			return null;
		}
		flushTextBuffer(turn);
		flushThinkingBuffer(turn);
		String id = "delegation-" + UUID.randomUUID().toString().substring(0, 8);
		Part.DelegationPart part =
			Part.DelegationPart.started(id, Instant.now(), fromAgent, toAgent, inputSummary, parentToolCallId);
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
		resolveDelegation(delegationId, resultSummary, null, durationMs, agentResult);
	}

	/**
	 * Resolve a completed delegation with result summary, the child run's id, duration, and
	 * sub-agent metadata.
	 *
	 * @param childRunId
	 *        The child agent loop's run id, linking the delegation part to the child's recorded
	 *        execution; may be <code>null</code> if the child run was not recorded.
	 */
	public void resolveDelegation(String delegationId, String resultSummary, String childRunId,
			long durationMs, AgentResult agentResult) {
		_activeDelegations.remove(delegationId);
		Turn turn = _log.currentTurn();
		if (turn != null) {
			Part existing = turn.findPart(delegationId);
			if (existing instanceof Part.DelegationPart dp) {
				turn.updatePart(delegationId, dp.completed(resultSummary, childRunId, durationMs,
					agentResult.turnsUsed(), agentResult.totalUsage(), agentResult.stopReason()));
			}
		}
	}

	// ---- Subagent lifecycle ----

	/**
	 * Records that the current turn started a background subagent, and forwards a
	 * {@link com.top_logic.tl.ai.core.agent.event.AgentEvent.SubagentStarted} lifecycle event to the
	 * parent listener.
	 *
	 * <p>
	 * The turn gets a {@link Part.SubagentPart}, which the chat rebuilds the subagent's card from when
	 * the conversation is reloaded; it is updated in place when the subagent ends.
	 * </p>
	 *
	 * @param parentToolCallId
	 *        The id of the tool call that started the subagent; empty if unknown.
	 */
	public void recordSubagentStarted(String subagentId, String targetAgentName, String taskBrief,
			String parentToolCallId) {
		recordSubagentStarted(subagentId, targetAgentName, taskBrief, parentToolCallId, false);
	}

	/**
	 * Records that the current turn started a background subagent that may wait in the queue first.
	 *
	 * @param queued
	 *        Whether it waits for a free slot rather than starting at once.
	 * @see #recordSubagentStarted(String, String, String, String)
	 */
	public void recordSubagentStarted(String subagentId, String targetAgentName, String taskBrief,
			String parentToolCallId, boolean queued) {
		recordSubagentStarted(subagentId, targetAgentName, taskBrief, parentToolCallId, queued, null);
	}

	/**
	 * Records that the current turn of a recorded run started a background subagent.
	 *
	 * @param leadExecution
	 *        The recorded run of the current turn, which the subagent's end is written into once the
	 *        run itself has ended; <code>null</code> when the run is not recorded. Only handed back to
	 *        the {@link AgentExecutionStore}, never read here.
	 * @see #recordSubagentStarted(String, String, String, String, boolean)
	 */
	public void recordSubagentStarted(String subagentId, String targetAgentName, String taskBrief,
			String parentToolCallId, boolean queued, AgentExecution leadExecution) {
		Turn turn = _log.currentTurn();
		if (turn != null) {
			Part.SubagentPart part = new Part.SubagentPart(UUID.randomUUID().toString(), Instant.now(),
				subagentId, targetAgentName, taskBrief, StringServices.nonNull(parentToolCallId), null,
				queued ? Part.SubagentStatus.QUEUED : Part.SubagentStatus.RUNNING, null);
			turn.addAssistantPart(part);
			_runningSubagents.put(subagentId, new SubagentPlace(turn, part.id(), leadExecution));
		}
		if (queued) {
			_queuedSubagents.add(subagentId);
		}
		AgentEventListener parent = _parentListener;
		if (parent != null) {
			parent.onEvent(new AgentEvent.SubagentStarted("", 0, Instant.now(), subagentId, targetAgentName,
				taskBrief, StringServices.nonNull(parentToolCallId), queued));
		}
	}

	/**
	 * Records that a background subagent that waited in the queue has started, and forwards a
	 * {@link com.top_logic.tl.ai.core.agent.event.AgentEvent.SubagentRunning} lifecycle event to the
	 * parent listener.
	 *
	 * <p>
	 * Its part says {@code RUNNING} from now on, in the turn that started it and in the recorded
	 * transcript of that turn's run - once that run has written it, see
	 * {@link AgentExecutionStore#replaceTranscriptPart(AgentExecution, Part)} - so that a reloaded card
	 * shows it at work rather than waiting. The part is written before the event is sent, so a window
	 * that is told and reads the conversation back finds it. A subagent that started at once is left
	 * as it is and announced no second time: its start already said it runs.
	 * </p>
	 */
	public void recordSubagentRunning(String subagentId) {
		if (!_queuedSubagents.remove(subagentId)) {
			return;
		}
		String parentToolCallId = "";
		SubagentPlace place = _runningSubagents.get(subagentId);
		if (place != null && place.turn().findPart(place.partId()) instanceof Part.SubagentPart part) {
			parentToolCallId = StringServices.nonNull(part.parentToolCallId());
			if (part.status() == Part.SubagentStatus.QUEUED) {
				replaceSubagentPart(place, part.running());
			}
		}
		AgentEventListener parent = _parentListener;
		if (parent != null) {
			parent.onEvent(new AgentEvent.SubagentRunning("", 0, Instant.now(), subagentId, parentToolCallId));
		}
	}

	/**
	 * The highest number of a subagent id this log names, 0 if it names none.
	 *
	 * <p>
	 * The log of a conversation outlives the registries that numbered its subagents, so a new subagent
	 * counts on from here - see
	 * {@link com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry#countOnFrom(int)}.
	 * </p>
	 */
	public int highestSubagentNumber() {
		int highest = 0;
		for (Turn turn : _log.turns()) {
			for (Part part : turn.assistantParts()) {
				if (part instanceof Part.SubagentPart subagent) {
					highest = Math.max(highest,
						com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry.numberOf(subagent.subagentId()));
				}
			}
		}
		return highest;
	}

	/**
	 * Records that a background subagent finished, and forwards a
	 * {@link com.top_logic.tl.ai.core.agent.event.AgentEvent.SubagentCompleted} lifecycle event to the
	 * parent listener.
	 *
	 * @param childRunId
	 *        The child's run id, its transcript's key; <code>null</code> if unknown.
	 */
	public void recordSubagentCompleted(String subagentId,
			com.top_logic.tl.ai.core.agent.subagent.SubagentResult result, String childRunId) {
		// An orderly stop at a limit has an answer, but maybe not the whole one; the card says so (#786).
		boolean atLimit = result != null && result.stoppedAtLimit();
		String parentToolCallId = endSubagent(subagentId,
			atLimit ? Part.SubagentStatus.LIMIT : Part.SubagentStatus.DONE,
			result == null ? null : result.finalText(), childRunId);
		AgentEventListener parent = _parentListener;
		if (parent != null) {
			parent.onEvent(new AgentEvent.SubagentCompleted("", 0, Instant.now(), subagentId, result,
				parentToolCallId, StringServices.nonNull(childRunId)));
		}
	}

	/**
	 * Records that a background subagent failed, and forwards a
	 * {@link com.top_logic.tl.ai.core.agent.event.AgentEvent.SubagentFailed} lifecycle event to the
	 * parent listener.
	 *
	 * @param childRunId
	 *        The child's run id; <code>null</code> if unknown.
	 */
	public void recordSubagentFailed(String subagentId, String errorSummary, String childRunId) {
		String parentToolCallId = endSubagent(subagentId, Part.SubagentStatus.FAILED, errorSummary, childRunId);
		AgentEventListener parent = _parentListener;
		if (parent != null) {
			parent.onEvent(new AgentEvent.SubagentFailed("", 0, Instant.now(), subagentId, errorSummary,
				parentToolCallId, StringServices.nonNull(childRunId)));
		}
	}

	/**
	 * Records that a background subagent was cancelled, and forwards a
	 * {@link com.top_logic.tl.ai.core.agent.event.AgentEvent.SubagentCancelled} lifecycle event to the
	 * parent listener.
	 *
	 * @param childRunId
	 *        The child's run id; <code>null</code> if unknown.
	 */
	public void recordSubagentCancelled(String subagentId, String childRunId) {
		String parentToolCallId = endSubagent(subagentId, Part.SubagentStatus.CANCELLED, null, childRunId);
		AgentEventListener parent = _parentListener;
		if (parent != null) {
			parent.onEvent(new AgentEvent.SubagentCancelled("", 0, Instant.now(), subagentId,
				parentToolCallId, StringServices.nonNull(childRunId)));
		}
	}

	/**
	 * Replaces the subagent's part with its ended form, in the turn that started it, and in the
	 * recorded transcript of that turn's run.
	 *
	 * <p>
	 * Only the first {@value #SUBAGENT_SUMMARY_CHARS} characters of the summary are kept: the part is a
	 * pointer to the child's transcript, not a copy of its report.
	 * </p>
	 *
	 * <p>
	 * The lead's run has usually written its transcript long before - with the part saying the
	 * subagent is at work - so the end is written there too, and before the end is announced: a
	 * window that is told and reads the conversation back must find it.
	 * </p>
	 *
	 * @return The id of the lead's tool call that started the subagent; empty if unknown.
	 */
	private String endSubagent(String subagentId, Part.SubagentStatus status, String summary,
			String childRunId) {
		// Cancelled while it was queued: it never leaves the queue.
		_queuedSubagents.remove(subagentId);
		SubagentPlace place = _runningSubagents.remove(subagentId);
		if (place == null) {
			return "";
		}
		if (place.turn().findPart(place.partId()) instanceof Part.SubagentPart part) {
			String shortSummary = summary == null || summary.length() <= SUBAGENT_SUMMARY_CHARS ? summary
				: summary.substring(0, SUBAGENT_SUMMARY_CHARS) + "...";
			replaceSubagentPart(place, part.ended(status, shortSummary, childRunId));
			return StringServices.nonNull(part.parentToolCallId());
		}
		return "";
	}

	/** Puts the given form of a subagent's part in place of the old one, in its turn and its run. */
	private static void replaceSubagentPart(SubagentPlace place, Part.SubagentPart replacement) {
		place.turn().updatePart(place.partId(), replacement);
		if (place.leadExecution() != null && AgentExecutionStore.Module.INSTANCE.isActive()) {
			AgentExecutionStore.getInstance().replaceTranscriptPart(place.leadExecution(), replacement);
		}
	}

	/** How much of a finished subagent's report its part keeps. */
	private static final int SUBAGENT_SUMMARY_CHARS = 200;

	/**
	 * Returns a lightweight forwarding listener that wraps every child event as a
	 * {@link com.top_logic.tl.ai.core.agent.event.AgentEvent.DelegatedEvent} carrying the
	 * {@code subagentId} and forwards it to the parent listener.
	 *
	 * <p>The child's steps are not recorded in this log: they are in the child's own transcript. What
	 * this log keeps of the child is one {@link Part.SubagentPart}, written by
	 * {@link #recordSubagentStarted(String, String, String, String)}.</p>
	 */
	public AgentEventListener subagentListener(String subagentId) {
		return childEvent -> {
			AgentEventListener parent = _parentListener;
			if (parent != null) {
				parent.onEvent(new AgentEvent.DelegatedEvent(
					childEvent.runId(), childEvent.turnNumber(), childEvent.timestamp(),
					subagentId, "", childEvent));
			}
		};
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
					e.toolCallId(), e.timestamp(), e.toolName(), e.argsJson(), e.mode()));
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
				|| event instanceof AgentEvent.ProviderError
				|| event instanceof AgentEvent.ModelSwitched) {
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
					"", _delegationToolCallId, childEvent));
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
		String text;
		synchronized (_buffers) {
			if (_textBuffer.length() == 0) {
				return;
			}
			text = _textBuffer.toString();
			_textBuffer.setLength(0);
		}
		turn.addAssistantPart(new Part.TextPart(
			UUID.randomUUID().toString(), Instant.now(), text));
	}

	private void flushThinkingBuffer(Turn turn) {
		String text;
		synchronized (_buffers) {
			if (_thinkingBuffer.length() == 0) {
				return;
			}
			text = _thinkingBuffer.toString();
			_thinkingBuffer.setLength(0);
		}
		turn.addAssistantPart(new Part.ThinkingPart(
			UUID.randomUUID().toString(), Instant.now(), text, null));
	}

	/**
	 * What the running turn has streamed but not yet recorded as a {@link Part.TextPart}.
	 *
	 * <p>
	 * Text arrives as deltas and is written into the turn at the next message boundary - a tool
	 * call, the end of an iteration, the end of the run - so between those points the log holds
	 * everything the turn has said except its last, unfinished paragraph. A window that opens the
	 * chat while the run is going has to show that paragraph as well, or a reader who closed the tab
	 * mid-sentence is shown a turn that stops earlier than the one they were watching (issue #512).
	 * </p>
	 *
	 * <p>
	 * A snapshot, and only ever a snapshot: the agent thread goes on appending, and what is returned
	 * here is what had arrived when it was asked. It is never written into the log - the flush that
	 * records it is the agent's own.
	 * </p>
	 *
	 * @return The unrecorded text, or the empty string when the turn has none pending.
	 */
	public String bufferedText() {
		return streamedText().pending();
	}

	/**
	 * {@link #bufferedText()} together with how far the stream had got when it was read.
	 *
	 * <p>
	 * A window that opens the chat mid-run is shown this snapshot and then goes on receiving the
	 * run's deltas. Which of those deltas the snapshot already holds is not a question of time - the
	 * snapshot and the stream travel on different connections - but of position:
	 * {@link StreamedText#end()} is the position of the last character in the snapshot, and every
	 * delta is sent with the position of its own last character (see {@link #streamedChars()}).
	 * </p>
	 */
	public StreamedText streamedText() {
		synchronized (_buffers) {
			return new StreamedText(_textBuffer.toString(), _streamedChars);
		}
	}

	/**
	 * How many characters of text this run has streamed so far.
	 *
	 * <p>
	 * Read right after a {@link com.top_logic.tl.ai.core.agent.event.AgentEvent.TextDelta} has
	 * passed this listener, it is the position of that delta's last character.
	 * </p>
	 */
	public long streamedChars() {
		synchronized (_buffers) {
			return _streamedChars;
		}
	}

	/**
	 * What {@link #streamedText()} answers.
	 *
	 * @param pending
	 *        The text streamed but not yet recorded in the log; never <code>null</code>.
	 * @param end
	 *        How many characters the run had streamed when this was read, recorded or not.
	 */
	public record StreamedText(String pending, long end) {
		// Pure data.
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
}
