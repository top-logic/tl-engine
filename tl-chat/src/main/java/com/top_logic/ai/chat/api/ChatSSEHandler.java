/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;

import com.top_logic.ai.chat.api.messages.ApprovalRequiredEvent;
import com.top_logic.ai.chat.api.messages.ApprovalResolvedEvent;
import com.top_logic.ai.chat.api.messages.CompactionStartedEvent;
import com.top_logic.ai.chat.api.messages.ConnectedEvent;
import com.top_logic.ai.chat.api.messages.HeartbeatEvent;
import com.top_logic.ai.chat.api.messages.MessageCompleteEvent;
import com.top_logic.ai.chat.api.messages.ProviderErrorEvent;
import com.top_logic.ai.chat.api.messages.RunCompletedEvent;
import com.top_logic.ai.chat.api.messages.RunStartedEvent;
import com.top_logic.ai.chat.api.messages.TextDeltaEvent;
import com.top_logic.ai.chat.api.messages.ThinkingDeltaEvent;
import com.top_logic.ai.chat.api.messages.TokenUsageDto;
import com.top_logic.ai.chat.api.messages.ToolCompletedEvent;
import com.top_logic.ai.chat.api.messages.ToolDeniedEvent;
import com.top_logic.ai.chat.api.messages.ToolFailedEvent;
import com.top_logic.ai.chat.api.messages.ToolStartedEvent;
import com.top_logic.ai.chat.api.messages.TransactionCommittedEvent;
import com.top_logic.ai.chat.api.messages.TransactionRolledBackEvent;
import com.top_logic.ai.chat.api.messages.TurnCompletedEvent;
import com.top_logic.ai.chat.api.messages.TurnStartedEvent;
import com.top_logic.ai.chat.layout.Chat;
import com.top_logic.ai.chat.layout.ChatEntry;
import com.top_logic.ai.chat.layout.ChatProcessor;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.ai.service.approval.UserInteractionRequest;
import com.top_logic.ai.service.approval.UserInteractionStore;
import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.message.TokenUsage;

/**
 * Push-based SSE handler that implements {@link AgentEventListener}.
 *
 * <p>
 * Receives agent events directly via {@link #onEvent(AgentEvent)}, buffers them in a
 * {@link ConcurrentLinkedQueue}, and flushes to the SSE stream every 50ms. User interaction
 * events from {@link UserInteractionStore} are still polled during the flush cycle because
 * they originate outside the agent event system.
 * </p>
 *
 * <p>
 * SSE event types emitted:
 * </p>
 * <ul>
 * <li>{@code connected} - Initial connection state</li>
 * <li>{@code run_started}, {@code run_completed} - Run lifecycle</li>
 * <li>{@code turn_started}, {@code turn_completed} - Turn lifecycle</li>
 * <li>{@code text_delta}, {@code thinking_delta} - LLM streaming</li>
 * <li>{@code tool_started}, {@code tool_completed}, {@code tool_failed}, {@code tool_denied} - Tool lifecycle</li>
 * <li>{@code approval_required}, {@code approval_resolved} - Approval flow</li>
 * <li>{@code lock_acquired}, {@code lock_failed} - Edit lock</li>
 * <li>{@code tx_began}, {@code tx_committed}, {@code tx_rollback} - Transactions</li>
 * <li>{@code compaction_started} - Context compaction</li>
 * <li>{@code provider_error} - Unrecoverable provider errors</li>
 * <li>{@code message_complete} - Final assistant message with HTML</li>
 * <li>{@code user_interaction} - Polled from UserInteractionStore</li>
 * <li>{@code heartbeat} - Keep-alive ping</li>
 * </ul>
 */
public class ChatSSEHandler implements AgentEventListener {

	private static final long FLUSH_INTERVAL_MS = 50;
	private static final long HEARTBEAT_INTERVAL_MS = 15000;

	private static final AtomicInteger SSE_THREAD_COUNTER = new AtomicInteger();

	private static final ScheduledExecutorService SSE_EXECUTOR =
		Executors.newScheduledThreadPool(
			Math.max(4, Runtime.getRuntime().availableProcessors()),
			r -> {
				Thread t = new Thread(r);
				t.setName("ChatSSE-Worker-" + SSE_THREAD_COUNTER.incrementAndGet());
				t.setDaemon(true);
				return t;
			});

	/** Active handlers, keyed for lookup by tracker. */
	private static final CopyOnWriteArraySet<ChatSSEHandler> ACTIVE_HANDLERS = new CopyOnWriteArraySet<>();

	private final TaskProgressTracker _tracker;
	private final AsyncContext _asyncContext;
	private final PrintWriter _writer;
	private final AtomicBoolean _closed = new AtomicBoolean(false);

	/** Thread-safe event buffer between agent thread and SSE flush thread. */
	private final ConcurrentLinkedQueue<AgentEvent> _eventQueue = new ConcurrentLinkedQueue<>();

	/** Tracks already-emitted user interaction request IDs to avoid duplicates. */
	private final Set<String> _emittedInteractionIds = new HashSet<>();

	private long _lastActivityTime = System.currentTimeMillis();
	private ScheduledFuture<?> _flushTask;

	/**
	 * Creates a new SSE handler.
	 *
	 * @param tracker
	 *        Session-scoped task progress tracker.
	 * @param asyncContext
	 *        Async context from the servlet.
	 * @param writer
	 *        Response writer for SSE output.
	 */
	public ChatSSEHandler(TaskProgressTracker tracker, AsyncContext asyncContext, PrintWriter writer) {
		_tracker = tracker;
		_asyncContext = asyncContext;
		_writer = writer;
	}

	/**
	 * Returns the tracker associated with this handler.
	 */
	public TaskProgressTracker getTracker() {
		return _tracker;
	}

	/**
	 * Finds the active handler for the given tracker, or {@code null} if none.
	 */
	public static ChatSSEHandler findHandler(TaskProgressTracker tracker) {
		for (ChatSSEHandler handler : ACTIVE_HANDLERS) {
			if (handler._tracker == tracker) {
				return handler;
			}
		}
		return null;
	}

	/**
	 * Starts the SSE event stream.
	 */
	public void start() {
		ACTIVE_HANDLERS.add(this);

		_asyncContext.addListener(new AsyncListener() {
			@Override
			public void onComplete(AsyncEvent event) {
				stop();
			}

			@Override
			public void onTimeout(AsyncEvent event) {
				stop();
			}

			@Override
			public void onError(AsyncEvent event) {
				stop();
			}

			@Override
			public void onStartAsync(AsyncEvent event) {
				// Nothing
			}
		});

		sendConnectedEvent();

		_flushTask = SSE_EXECUTOR.scheduleWithFixedDelay(
			this::flushAndPoll,
			FLUSH_INTERVAL_MS,
			FLUSH_INTERVAL_MS,
			TimeUnit.MILLISECONDS);
	}

	/**
	 * Stops the SSE handler and releases resources.
	 */
	public void stop() {
		if (_closed.compareAndSet(false, true)) {
			ACTIVE_HANDLERS.remove(this);
			if (_flushTask != null) {
				_flushTask.cancel(false);
			}
			try {
				_asyncContext.complete();
			} catch (Exception ignored) {
				// Already completed or invalid state
			}
		}
	}

	@Override
	public void onEvent(AgentEvent event) {
		if (_closed.get()) {
			return;
		}
		_eventQueue.add(event);
	}

	/**
	 * Sends a {@link MessageCompleteEvent} directly to the SSE stream. Called from
	 * {@link ChatProcessor} after the run completes, not via {@link #onEvent(AgentEvent)}.
	 */
	public void sendMessageComplete(MessageCompleteEvent event) {
		sendEvent("message_complete", DtoMapper.toJson(event));
	}

	private void sendConnectedEvent() {
		Chat chat = _tracker.getChat();
		int messageCount = 0;
		for (ChatEntry entry : chat.getEntries()) {
			if (entry instanceof ChatEntry.MessageEntry) {
				messageCount++;
			}
		}

		String currentTaskId = ChatProcessor.getCurrentTaskId(_tracker);
		boolean processing = false;
		ChatProcessor.AIProcessingState procState = _tracker.getAnyActiveProcessingState();
		if (procState != null && procState.isProcessing()) {
			String procSessionId = procState.getSessionId();
			String activeSessionId = _tracker.getActiveSessionId();
			if (procSessionId == null || procSessionId.equals(activeSessionId)) {
				processing = true;
			}
		}

		try {
			ConnectedEvent event = ConnectedEvent.create()
				.setMessageSeq(messageCount)
				.setTimelineSeq(0)
				.setProcessing(processing)
				.setTaskId(currentTaskId != null ? currentTaskId : "");
			sendEvent("connected", DtoMapper.toJson(event));
		} catch (Exception e) {
			Logger.error("Failed to send connected event", e, ChatSSEHandler.class);
		}

		String mode = _tracker.getApprovalMode().name();
		sendEvent("approval_mode", "{\"mode\":\"" + mode + "\"}");
	}

	/**
	 * Drains the event queue, emits SSE frames, polls for user interactions, and sends
	 * heartbeats when idle.
	 */
	private void flushAndPoll() {
		if (_closed.get()) {
			return;
		}

		try {
			// Drain agent events
			AgentEvent event;
			while ((event = _eventQueue.poll()) != null) {
				emitAgentEvent(event);
			}

			// Poll for user interactions (from AskUserTool, not part of AgentEvent)
			pollUserInteractions();
		} catch (Exception e) {
			Logger.error("Error in SSE flush", e, ChatSSEHandler.class);
		}

		// Heartbeat if no activity for 15s
		long now = System.currentTimeMillis();
		if (now - _lastActivityTime > HEARTBEAT_INTERVAL_MS) {
			sendHeartbeat();
		}
	}

	private void emitAgentEvent(AgentEvent event) {
		if (event instanceof AgentEvent.RunStarted e) {
			sendEvent("run_started", DtoMapper.toJson(RunStartedEvent.create()
				.setRunId(e.runId())
				.setModel(e.model())));
		} else if (event instanceof AgentEvent.RunCompleted e) {
			sendEvent("run_completed", DtoMapper.toJson(RunCompletedEvent.create()
				.setRunId(e.runId())
				.setReason(e.reason().name())
				.setTotalUsage(mapTokenUsage(e.totalUsage()))));
		} else if (event instanceof AgentEvent.TurnStarted e) {
			sendEvent("turn_started", DtoMapper.toJson(TurnStartedEvent.create()
				.setTurnNumber(e.turnNumber())));
		} else if (event instanceof AgentEvent.TurnCompleted e) {
			sendEvent("turn_completed", DtoMapper.toJson(TurnCompletedEvent.create()
				.setTurnNumber(e.turnNumber())
				.setTurnUsage(mapTokenUsage(e.turnUsage()))));
		} else if (event instanceof AgentEvent.TextDelta e) {
			sendEvent("text_delta", DtoMapper.toJson(TextDeltaEvent.create()
				.setText(e.text())));
		} else if (event instanceof AgentEvent.ThinkingDelta e) {
			sendEvent("thinking_delta", DtoMapper.toJson(ThinkingDeltaEvent.create()
				.setText(e.text())));
		} else if (event instanceof AgentEvent.ToolCallStarted e) {
			sendEvent("tool_started", DtoMapper.toJson(ToolStartedEvent.create()
				.setToolCallId(e.toolCallId())
				.setToolName(e.toolName())
				.setArgsJson(e.argsJson())));
		} else if (event instanceof AgentEvent.ToolCallCompleted e) {
			sendEvent("tool_completed", DtoMapper.toJson(ToolCompletedEvent.create()
				.setToolCallId(e.toolCallId())
				.setToolName(e.toolName())
				.setResult(e.result())
				.setDurationMs(e.duration().toMillis())));
		} else if (event instanceof AgentEvent.ToolCallFailed e) {
			sendEvent("tool_failed", DtoMapper.toJson(ToolFailedEvent.create()
				.setToolCallId(e.toolCallId())
				.setToolName(e.toolName())
				.setErrorMessage(e.errorMessage())
				.setDurationMs(e.duration().toMillis())));
		} else if (event instanceof AgentEvent.ToolCallDenied e) {
			sendEvent("tool_denied", DtoMapper.toJson(ToolDeniedEvent.create()
				.setToolCallId(e.toolCallId())
				.setToolName(e.toolName())
				.setReason(e.reason())));
		} else if (event instanceof AgentEvent.ToolCallResultTruncated) {
			// Not surfaced to UI
		} else if (event instanceof AgentEvent.ApprovalRequired e) {
			sendEvent("approval_required", DtoMapper.toJson(ApprovalRequiredEvent.create()
				.setToolCallId(e.toolCallId())
				.setApprovalRequestId(e.approvalRequestId())
				.setToolName(e.toolName())
				.setArgsJson(DtoMapper.mapToJsonString(e.args()))
				.setDisplayReason(e.displayReason())));
		} else if (event instanceof AgentEvent.ApprovalResolved e) {
			sendEvent("approval_resolved", DtoMapper.toJson(ApprovalResolvedEvent.create()
				.setToolCallId(e.toolCallId())
				.setApprovalRequestId(e.approvalRequestId())
				.setOutcome(e.outcome().name())));
		} else if (event instanceof AgentEvent.LockAcquired) {
			sendEvent("lock_acquired", "{}");
		} else if (event instanceof AgentEvent.LockFailed e) {
			sendEvent("lock_failed", "{\"reason\":\"" + escapeJson(e.reason()) + "\"}");
		} else if (event instanceof AgentEvent.TransactionBegan) {
			sendEvent("tx_began", "{}");
		} else if (event instanceof AgentEvent.TransactionCommitted e) {
			sendEvent("tx_committed", DtoMapper.toJson(TransactionCommittedEvent.create()
				.setRevisionNumber(e.revisionNumber())));
		} else if (event instanceof AgentEvent.TransactionRolledBack e) {
			sendEvent("tx_rollback", DtoMapper.toJson(TransactionRolledBackEvent.create()
				.setReason(e.reason())));
		} else if (event instanceof AgentEvent.CompactionStarted e) {
			sendEvent("compaction_started", DtoMapper.toJson(CompactionStartedEvent.create()
				.setTokensBefore(e.tokensBefore())
				.setTrigger(e.trigger())));
		} else if (event instanceof AgentEvent.CompactionCompleted) {
			// Not surfaced to UI
		} else if (event instanceof AgentEvent.ProviderError e) {
			sendEvent("provider_error", DtoMapper.toJson(ProviderErrorEvent.create()
				.setKind(e.kind())
				.setErrorMessage(e.message())));
		}
	}

	private void pollUserInteractions() {
		ChatProcessor.AIProcessingState procState = _tracker.getAnyActiveProcessingState();
		if (procState == null) {
			return;
		}
		UserInteractionStore interactionStore = procState.getUserInteractionStore();
		if (interactionStore == null || !interactionStore.hasPending()) {
			return;
		}
		for (UserInteractionRequest req : interactionStore.getPendingRequests()) {
			if (!_emittedInteractionIds.contains(req.getId())) {
				try {
					sendEvent("user_interaction", DtoMapper.toJson(
						DtoMapper.toUserInteractionEvent(req)));
				} catch (Exception e) {
					Logger.error("Failed to emit user interaction event", e, ChatSSEHandler.class);
				}
				_emittedInteractionIds.add(req.getId());
			}
		}
	}

	private void sendHeartbeat() {
		try {
			HeartbeatEvent event = HeartbeatEvent.create()
				.setTs(System.currentTimeMillis());
			sendEvent("heartbeat", DtoMapper.toJson(event));
		} catch (Exception e) {
			Logger.error("Failed to send heartbeat", e, ChatSSEHandler.class);
		}
	}

	private void sendEvent(String eventType, String data) {
		if (_closed.get()) {
			return;
		}

		try {
			synchronized (_writer) {
				_writer.write("event: " + eventType + "\n");
				_writer.write("data: " + data + "\n\n");
				_writer.flush();
			}

			if (_writer.checkError()) {
				stop();
				return;
			}

			_lastActivityTime = System.currentTimeMillis();
		} catch (Exception e) {
			Logger.error("Failed to write SSE event", e, ChatSSEHandler.class);
			stop();
		}
	}

	private static TokenUsageDto mapTokenUsage(TokenUsage usage) {
		if (usage == null) {
			return null;
		}
		return TokenUsageDto.create()
			.setInputTokens(usage.inputTokens())
			.setOutputTokens(usage.outputTokens())
			.setThinkingTokens(usage.thinkingTokens())
			.setCacheReadTokens(usage.cacheReadTokens())
			.setCacheWriteTokens(usage.cacheWriteTokens());
	}

	/**
	 * Minimal JSON string escaping for inline JSON values.
	 */
	private static String escapeJson(String value) {
		if (value == null) {
			return "";
		}
		return value
			.replace("\\", "\\\\")
			.replace("\"", "\\\"")
			.replace("\n", "\\n")
			.replace("\r", "\\r")
			.replace("\t", "\\t");
	}
}
