/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.top_logic.ai.chat.layout.Chat;
import com.top_logic.ai.chat.layout.ChatEntry;
import com.top_logic.ai.chat.layout.ChatProcessor;
import com.top_logic.ai.chat.layout.ChatMessage;
import com.top_logic.ai.chat.layout.ChatTask;
import com.top_logic.ai.chat.layout.ChatTimelineEvent;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.basic.Logger;
import com.top_logic.basic.thread.ThreadContextManager;

/**
 * SSE (Server-Sent Events) handler for real-time chat updates.
 *
 * <p>
 * Replaces polling with push-based updates. Connects to session-scoped {@link TaskProgressTracker},
 * polls internally for changes, and emits delta events to the client.
 * </p>
 *
 * <p>
 * SSE Event types:
 * </p>
 * <ul>
 * <li>{@code connected} - Initial connection with current sequence numbers</li>
 * <li>{@code message} - New chat message</li>
 * <li>{@code tool_call} - Tool call start or completion</li>
 * <li>{@code agent_call} - Agent handoff event</li>
 * <li>{@code task_status} - Task status change (completed/error)</li>
 * <li>{@code heartbeat} - Keep-alive ping</li>
 * </ul>
 */
public class ChatSSEHandler {

	private static final long POLL_INTERVAL_MS = 200;
	private static final long HEARTBEAT_INTERVAL_MS = 15000;

	private static final ObjectMapper MAPPER = new ObjectMapper();

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

	private final TaskProgressTracker _tracker;
	private final AsyncContext _asyncContext;
	private final PrintWriter _writer;
	private final AtomicBoolean _closed = new AtomicBoolean(false);

	private int _lastMessageSeq = 0;
	private int _lastTimelineSeq = 0;
	private String _lastTaskId = null;
	private ChatTask.TaskStatus _lastTaskStatus = null;
	private long _lastHeartbeatTime = 0;

	/** Tracks agent call result summaries to detect updates (key = event index "e0", "e1", etc.). */
	private final Map<String, String> _agentCallResults = new HashMap<>();

	private ScheduledFuture<?> _pollTask;

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
	 * Starts the SSE event stream.
	 */
	public void start() {
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

		// Send initial connected event with current sequence numbers
		sendConnectedEvent();

		// Start polling for updates
		_pollTask = SSE_EXECUTOR.scheduleWithFixedDelay(
			this::pollAndEmit,
			POLL_INTERVAL_MS,
			POLL_INTERVAL_MS,
			TimeUnit.MILLISECONDS);
	}

	/**
	 * Stops the SSE handler and releases resources.
	 */
	public void stop() {
		if (_closed.compareAndSet(false, true)) {
			if (_pollTask != null) {
				_pollTask.cancel(false);
			}
			try {
				_asyncContext.complete();
			} catch (Exception ignored) {
				// Already completed or invalid state
			}
		}
	}

	private void sendConnectedEvent() {
		// Count current messages
		Chat chat = _tracker.getChat();
		int messageCount = 0;
		for (ChatEntry entry : chat.getEntries()) {
			if (entry instanceof ChatEntry.MessageEntry) {
				messageCount++;
			}
		}
		_lastMessageSeq = messageCount;

		// Get current task timeline count and status
		String currentTaskId = ChatProcessor.getCurrentTaskId(_tracker);
		int timelineCount = 0;
		ChatTask.TaskStatus currentTaskStatus = null;
		if (currentTaskId != null) {
			ChatTask task = _tracker.getTask(currentTaskId);
			if (task != null) {
				timelineCount = task.getTimelineEvents().size();
				currentTaskStatus = task.getStatus();
			}
		}
		_lastTimelineSeq = timelineCount;
		_lastTaskId = currentTaskId;
		_lastTaskStatus = currentTaskStatus;

		boolean processing = ChatProcessor.isProcessing(_tracker);

		try {
			String data = MAPPER.writeValueAsString(new ConnectedEvent(messageCount, timelineCount, processing, currentTaskId));
			sendEvent("connected", data);
		} catch (Exception e) {
			Logger.error("Failed to send connected event", e, ChatSSEHandler.class);
		}
	}

	private void pollAndEmit() {
		if (_closed.get()) {
			return;
		}

		try {
			ThreadContextManager.inSystemInteraction(ChatSSEHandler.class, () -> {
				// Sync timeline events from ToolCallTracker to TaskProgressTracker
				ChatProcessor.AIProcessingState state = _tracker.getCurrentProcessingState();
				if (state != null) {
					ChatProcessor.applyPollUpdates(state, _tracker.getChat(), _tracker);
				}

				emitDeltas();
				return null;
			});
		} catch (Exception e) {
			Logger.error("Error in SSE poll", e, ChatSSEHandler.class);
		}

		// Send heartbeat if needed
		long now = System.currentTimeMillis();
		if (now - _lastHeartbeatTime > HEARTBEAT_INTERVAL_MS) {
			sendHeartbeat();
			_lastHeartbeatTime = now;
		}
	}

	private void emitDeltas() {
		Chat chat = _tracker.getChat();
		List<ChatEntry> entries = chat.getEntries();

		// Emit new messages
		int messageIndex = 0;
		for (ChatEntry entry : entries) {
			if (entry instanceof ChatEntry.MessageEntry me) {
				if (messageIndex >= _lastMessageSeq) {
					emitMessage(me.message(), "m" + messageIndex);
				}
				messageIndex++;
			}
		}
		_lastMessageSeq = messageIndex;

		// Emit timeline events and task status
		String currentTaskId = ChatProcessor.getCurrentTaskId(_tracker);
		if (currentTaskId != null) {
			ChatTask task = _tracker.getTask(currentTaskId);
			if (task != null) {
				// Emit new timeline events
				List<ChatTimelineEvent> timeline = task.getTimelineEvents();
				for (int i = _lastTimelineSeq; i < timeline.size(); i++) {
					ChatTimelineEvent ev = timeline.get(i);
					String eventId = "e" + i;
					emitTimelineEvent(ev, eventId);
					// Track agent call results for update detection
					if (ev.getKind() == ChatTimelineEvent.Kind.AGENT_CALL) {
						_agentCallResults.put(eventId, ev.getResultSummary());
					}
				}
				_lastTimelineSeq = timeline.size();

				// Check for updated agent call results (re-emit if changed)
				for (int i = 0; i < timeline.size(); i++) {
					ChatTimelineEvent ev = timeline.get(i);
					if (ev.getKind() == ChatTimelineEvent.Kind.AGENT_CALL) {
						String eventId = "e" + i;
						String currentResult = ev.getResultSummary();
						String lastResult = _agentCallResults.get(eventId);
						if (lastResult != null && !lastResult.equals(currentResult)) {
							// Result has changed - re-emit with updated data
							emitTimelineEvent(ev, eventId);
							_agentCallResults.put(eventId, currentResult);
						}
					}
				}

				// Emit task status change
				if (_lastTaskId == null || !_lastTaskId.equals(currentTaskId) ||
					_lastTaskStatus != task.getStatus()) {
					emitTaskStatus(task);
					_lastTaskId = currentTaskId;
					_lastTaskStatus = task.getStatus();
				}
			}
		} else {
			// Processing state cleared - but we may still need to emit final status
			// The task still exists in tracker with COMPLETED/ERROR status
			if (_lastTaskId != null) {
				ChatTask task = _tracker.getTask(_lastTaskId);
				if (task != null && _lastTaskStatus != task.getStatus()) {
					// Emit the final status change (e.g. IN_PROGRESS -> COMPLETED)
					emitTaskStatus(task);
					_lastTaskStatus = task.getStatus();
				}
				// Reset for next task
				_lastTimelineSeq = 0;
				_lastTaskId = null;
				_lastTaskStatus = null;
				_agentCallResults.clear();
			}
		}
	}

	private void emitMessage(ChatMessage msg, String id) {
		try {
			MessageDto dto = DtoMapper.toMessageDto(msg, id);
			String data = MAPPER.writeValueAsString(dto);
			sendEvent("message", data);
		} catch (Exception e) {
			Logger.error("Failed to emit message event", e, ChatSSEHandler.class);
		}
	}

	private void emitTimelineEvent(ChatTimelineEvent ev, String id) {
		try {
			TimelineEventDto dto = DtoMapper.toTimelineEventDto(ev, id);
			String eventType = ev.getKind() == ChatTimelineEvent.Kind.TOOL_CALL ? "tool_call" : "agent_call";
			String data = MAPPER.writeValueAsString(dto);
			sendEvent(eventType, data);
		} catch (Exception e) {
			Logger.error("Failed to emit timeline event", e, ChatSSEHandler.class);
		}
	}

	private void emitTaskStatus(ChatTask task) {
		try {
			TaskStatusEvent status = new TaskStatusEvent(
				task.getId(),
				task.getStatus().name().toLowerCase(),
				task.getErrorMessage());
			String data = MAPPER.writeValueAsString(status);
			sendEvent("task_status", data);
		} catch (Exception e) {
			Logger.error("Failed to emit task status event", e, ChatSSEHandler.class);
		}
	}

	private void sendHeartbeat() {
		try {
			String data = MAPPER.writeValueAsString(new HeartbeatEvent(System.currentTimeMillis()));
			sendEvent("heartbeat", data);
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
			}
		} catch (Exception e) {
			Logger.error("Failed to write SSE event", e, ChatSSEHandler.class);
			stop();
		}
	}

	// Event DTOs for SSE

	private record ConnectedEvent(int messageSeq, int timelineSeq, boolean processing, String taskId) {
	}

	private record TaskStatusEvent(String taskId, String status, String errorMessage) {
	}

	private record HeartbeatEvent(long ts) {
	}
}
