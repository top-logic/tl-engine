/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.top_logic.ai.service.agents.OrchestratorAgent;
import com.top_logic.ai.service.tools.ToolCallTracker;
import com.top_logic.ai.service.tools.ToolCallTracking;
import com.top_logic.ai.service.tools.TraceEntry;
import com.top_logic.basic.Logger;
import com.top_logic.basic.thread.ThreadContextManager;

/**
 * Background processing logic for the chat component.
 *
 * <p>
 * Manages AI task execution, polling, and state synchronization between background threads
 * and the UI / SSE handlers. Extracted from {@link ChatControl} to separate processing
 * concerns from rendering.
 * </p>
 */
public class ChatProcessor {

	/** Polling interval for UI updates (milliseconds). */
	static final long POLL_INTERVAL_MS = 500;

	/** Funny placeholder descriptions for in-progress chat tasks. */
	static final String[] CHAT_TASK_PLACEHOLDERS = {
		"Flabbergasting...",
		"Contemplating...",
		"Warming up neurons...",
		"Consulting the oracle...",
		"Assembling thoughts...",
		"Polishing the answer...",
		"Thinking really hard...",
		"Consulting ancient scrolls...",
		"Loading infinite wisdom...",
		"Brewing ideas...",
		"Reticulating splines...",
		"Herding cats...",
		"Convincing the AI...",
		"Spinning up...",
	};

	static String randomChatTaskPlaceholder() {
		return CHAT_TASK_PLACEHOLDERS[ThreadLocalRandom.current().nextInt(CHAT_TASK_PLACEHOLDERS.length)];
	}

	/** Background thread pool for AI operations. */
	private static final ExecutorService BACKGROUND_EXECUTOR =
		Executors.newCachedThreadPool(new ThreadFactory() {
			private final AtomicInteger _counter = new AtomicInteger();

			@Override
			public Thread newThread(Runnable r) {
				Thread thread = new Thread(r);
				thread.setName("ChatProcessor-AI-Worker-" + _counter.incrementAndGet());
				thread.setDaemon(true);
				return thread;
			}
		});

	private ChatProcessor() {
		// Static utility class
	}

	/**
	 * Executes user message through OrchestratorAgent (single chat entrypoint).
	 * Sets trace so AgentCall and ToolCall events are visible in the UI timeline.
	 * Uses state from tracker; taskId must already be set on state.
	 */
	static void executeOrchestrator(TaskProgressTracker taskTracker, AIProcessingState state,
			String userMessage) throws Exception {
		if (state == null) {
			return;
		}
		String taskId = state.getCurrentTaskId();
		if (taskId == null) {
			return;
		}

		ToolCallTracker tracker = new ToolCallTracker();
		ToolCallTracking.setTracker(tracker);
		state.setToolCallTracker(tracker);

		try {
			Logger.info("ChatProcessor: Starting Orchestrator in thread: " + Thread.currentThread().getName(),
				ChatProcessor.class);

			String result = OrchestratorAgent.chat(userMessage);

			state.addPendingMessage(new ChatMessage("Agent", result));
			taskTracker.completeTask(taskId);

		} catch (Exception ex) {
			taskTracker.failTask(taskId, ex.getMessage());
			throw ex;
		} finally {
			ToolCallTracking.setTracker(null);
			state.setToolCallTracker(null);
		}
	}

	/**
	 * Converts a {@link TraceEntry} to a {@link ChatTimelineEvent}.
	 */
	static ChatTimelineEvent toChatTimelineEvent(TraceEntry entry, ToolCallTracker tracker) {
		if (entry instanceof TraceEntry.ToolCallTraceEntry tc) {
			String resultJson = tracker.getRawResultJsons().get(tc.resultRef());
			ChatToolCall chatToolCall = new ChatToolCall(tc.toolName(), tc.args(), resultJson);
			return ChatTimelineEvent.toolCall(chatToolCall);
		}
		if (entry instanceof TraceEntry.AgentCallTraceEntry ac) {
			return ChatTimelineEvent.agentCall(ac.fromAgent(), ac.toAgent(), ac.inputSummary(), ac.resultSummary());
		}
		if (entry instanceof TraceEntry.MutableAgentCallTraceEntry mac) {
			return ChatTimelineEvent.agentCallWithDynamicResult(mac.fromAgent(), mac.toAgent(), mac.inputSummary(),
				mac::resultSummary);
		}
		return null;
	}

	/**
	 * Applies poll updates for the given state to chat and taskTracker. Clears tracker and
	 * cancels polling when processing is done.
	 *
	 * <p>
	 * Used by the SSE handler, the servlet, and the scheduled polling runnable so updates
	 * continue regardless of navigation.
	 * </p>
	 */
	public static boolean applyPollUpdates(AIProcessingState state, Chat chat,
			TaskProgressTracker taskTracker) {
		if (state == null) {
			return false;
		}
		boolean hasUpdates = false;
		String currentTaskId = state.getCurrentTaskId();

		// Check for new messages (agent response when task completes)
		List<ChatMessage> newMessages = state.consumePendingMessages();
		if (!newMessages.isEmpty()) {
			if (currentTaskId != null) {
				ChatTask task = taskTracker.getTask(currentTaskId);
				chat.addTaskSnapshot(task);
			}
			for (ChatMessage msg : newMessages) {
				chat.addMessage(msg);
			}
			hasUpdates = true;
		}

		// Check for new timeline events (tool calls and agent calls) in real-time
		ToolCallTracker tracker = state.getToolCallTracker();
		if (currentTaskId != null && tracker != null) {
			List<TraceEntry> timeline = tracker.getTimeline();
			int currentCount = timeline.size();
			int lastSeen = state.getLastSeenToolCallCount();
			if (currentCount > lastSeen) {
				for (int i = lastSeen; i < currentCount; i++) {
					TraceEntry entry = timeline.get(i);
					ChatTimelineEvent chatEv = toChatTimelineEvent(entry, tracker);
					if (chatEv != null) {
						taskTracker.addTimelineEvent(currentTaskId, chatEv);
					}
				}
				state.setLastSeenToolCallCount(currentCount);
				hasUpdates = true;
			}
		}

		// Check for completion
		if (!state.isProcessing()) {
			if (state._pollingTask != null) {
				state._pollingTask.cancel(false);
				state._pollingTask = null;
			}
			Throwable error = state.getError();
			if (error != null) {
				if (currentTaskId != null) {
					ChatTask task = taskTracker.getTask(currentTaskId);
					chat.addTaskSnapshot(task);
				}
				String errorMsg = "Error: " + error.getMessage();
				if (error instanceof InterruptedException) {
					errorMsg = "Processing was cancelled";
				}
				chat.addMessage(new ChatMessage("AI", errorMsg));
				hasUpdates = true;
			}
			taskTracker.clearCurrentProcessingState();
		}
		return hasUpdates;
	}

	/**
	 * Shared send logic: adds user message, starts task, submits background orchestrator, optionally
	 * schedules UI polling.
	 *
	 * @param tracker
	 *        Task progress tracker for this session.
	 * @param userName
	 *        Display name of the user sending the message.
	 * @param userMessage
	 *        The message text.
	 * @param repaintRequester
	 *        Called when polling has updates (e.g. requestRepaint); may be null for API-only.
	 * @param uiExecutor
	 *        TopLogic UI executor for server-side polling; if null, no polling is scheduled
	 *        (client polls via SSE).
	 * @return The send result (task ID, or busy/error); never null.
	 */
	public static SendResult sendMessage(TaskProgressTracker tracker, String userName, String userMessage,
			Runnable repaintRequester, ScheduledExecutorService uiExecutor) {
		if (isProcessing(tracker)) {
			tracker.getChat().addMessage(new ChatMessage("System", "Please wait for the current response to complete."));
			if (repaintRequester != null) {
				repaintRequester.run();
			}
			return SendResult.busy();
		}

		ChatMessage message = new ChatMessage(userName, userMessage);
		tracker.getChat().addMessage(message);

		AIProcessingState state = new AIProcessingState();
		state.startProcessing();
		String taskId = tracker.startTask(randomChatTaskPlaceholder());
		state.setCurrentTaskId(taskId);
		tracker.setCurrentProcessingState(state);
		if (repaintRequester != null) {
			tracker.setRepaintRequester(repaintRequester);
		}

		Future<?> future = BACKGROUND_EXECUTOR.submit(new AIBackgroundTask(tracker, userMessage));
		state._backgroundTask = future;

		if (uiExecutor != null) {
			try {
				ScheduledFuture<?> pollingTask = uiExecutor.scheduleWithFixedDelay(
					() -> {
						AIProcessingState s = (AIProcessingState) tracker.getCurrentProcessingState();
						if (s == null) {
							return;
						}
						boolean hasUpdates = applyPollUpdates(s, tracker.getChat(), tracker);
						if (hasUpdates) {
							tracker.requestRepaintIfNeeded();
						}
					},
					POLL_INTERVAL_MS,
					POLL_INTERVAL_MS,
					TimeUnit.MILLISECONDS);
				state._pollingTask = pollingTask;
			} catch (Exception ex) {
				Logger.error("Failed to schedule UI polling: " + ex.getMessage(), ex, ChatProcessor.class);
				future.cancel(true);
				tracker.clearCurrentProcessingState();
				return SendResult.error();
			}
		}
		return SendResult.ok(taskId);
	}

	/**
	 * Returns whether an AI task is currently in progress for the given tracker.
	 */
	public static boolean isProcessing(TaskProgressTracker tracker) {
		Object state = tracker.getCurrentProcessingState();
		return state instanceof AIProcessingState s && s.isProcessing();
	}

	/**
	 * Returns the current task ID for the given tracker, or null if none.
	 */
	public static String getCurrentTaskId(TaskProgressTracker tracker) {
		Object state = tracker.getCurrentProcessingState();
		return state instanceof AIProcessingState s ? s.getCurrentTaskId() : null;
	}

	/**
	 * Background task for AI processing with detailed progress tracking.
	 */
	static class AIBackgroundTask implements Runnable {
		private final TaskProgressTracker _taskTracker;
		private final String _userMessage;

		AIBackgroundTask(TaskProgressTracker taskTracker, String userMessage) {
			_taskTracker = taskTracker;
			_userMessage = userMessage;
		}

		@Override
		public void run() {
			AIProcessingState state = (AIProcessingState) _taskTracker.getCurrentProcessingState();
			if (state == null) {
				return;
			}
			try {
				ThreadContextManager.inSystemInteraction(ChatProcessor.class, () -> {
					try {
						executeOrchestrator(_taskTracker, state, _userMessage);
						state.completeSuccessfully();
						return null;
					} catch (InterruptedException ex) {
						Thread.currentThread().interrupt();
						state.completeWithError(ex);
						return null;
					} catch (Exception ex) {
						Logger.error("AI processing failed: " + ex.getMessage(), ex, ChatProcessor.class);
						state.completeWithError(ex);
						return null;
					}
				});
			} catch (Exception ex) {
				Logger.error("Failed to run background task: " + ex.getMessage(), ex, ChatProcessor.class);
				state.completeWithError(ex);
			}
		}
	}

	/**
	 * Thread-safe container for AI processing state shared between background and UI threads.
	 */
	public static class AIProcessingState {
		private final Object _lock = new Object();

		// Processing status
		private volatile boolean _processing = false;
		private volatile boolean _completed = false;
		private volatile Throwable _error = null;

		// Current task ID for progress tracking
		private volatile String _currentTaskId = null;

		// Tool call tracking for real-time updates
		private volatile ToolCallTracker _toolCallTracker = null;
		private final AtomicInteger _lastSeenToolCallCount = new AtomicInteger(0);

		// Result accumulation
		private final List<ChatMessage> _pendingMessages = new ArrayList<>();

		// Lifecycle management
		volatile Future<?> _backgroundTask = null;
		volatile ScheduledFuture<?> _pollingTask = null;

		public boolean isProcessing() {
			synchronized (_lock) {
				return _processing && !_completed;
			}
		}

		public void startProcessing() {
			synchronized (_lock) {
				_processing = true;
				_completed = false;
				_error = null;
				_pendingMessages.clear();
			}
		}

		public void addPendingMessage(ChatMessage message) {
			synchronized (_lock) {
				_pendingMessages.add(message);
			}
		}

		public List<ChatMessage> consumePendingMessages() {
			synchronized (_lock) {
				List<ChatMessage> result = new ArrayList<>(_pendingMessages);
				_pendingMessages.clear();
				return result;
			}
		}

		public void completeSuccessfully() {
			synchronized (_lock) {
				_processing = false;
				_completed = true;
			}
		}

		public void completeWithError(Throwable error) {
			synchronized (_lock) {
				_processing = false;
				_completed = true;
				_error = error;
			}
		}

		public void cancel() {
			synchronized (_lock) {
				if (_backgroundTask != null) {
					_backgroundTask.cancel(true);
				}
				if (_pollingTask != null) {
					_pollingTask.cancel(false);
				}
			}
		}

		public Throwable getError() {
			return _error;
		}

		public void setCurrentTaskId(String taskId) {
			_currentTaskId = taskId;
		}

		public String getCurrentTaskId() {
			return _currentTaskId;
		}

		public void setToolCallTracker(ToolCallTracker tracker) {
			_toolCallTracker = tracker;
			_lastSeenToolCallCount.set(0);
		}

		public ToolCallTracker getToolCallTracker() {
			return _toolCallTracker;
		}

		public int getLastSeenToolCallCount() {
			return _lastSeenToolCallCount.get();
		}

		public void setLastSeenToolCallCount(int count) {
			_lastSeenToolCallCount.set(count);
		}
	}

	/** Result of a send attempt: success (taskId), busy (already processing), or error. */
	public static final class SendResult {
		private final String _taskId;
		private final boolean _busy;

		private SendResult(String taskId, boolean busy) {
			_taskId = taskId;
			_busy = busy;
		}

		public static SendResult ok(String taskId) {
			return new SendResult(taskId, false);
		}

		public static SendResult busy() {
			return new SendResult(null, true);
		}

		public static SendResult error() {
			return new SendResult(null, false);
		}

		public String getTaskId() {
			return _taskId;
		}

		public boolean isBusy() {
			return _busy;
		}
	}
}
