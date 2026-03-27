/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import dev.langchain4j.memory.ChatMemory;

import com.top_logic.ai.chat.layout.ChatProcessor.AIProcessingState;
import com.top_logic.ai.service.approval.ApprovalMode;
import com.top_logic.layout.WindowScope;

/**
 * Session-scoped storage for task progress tracking.
 *
 * <p>
 * This tracker stores task progress information associated with a {@link WindowScope}, making it
 * persist across UI navigation within the same session. It implements an LRU eviction strategy to
 * prevent unbounded growth.
 * </p>
 */
public class TaskProgressTracker {

	/** Maximum number of tasks to keep in memory. */
	private static final int MAX_TASKS = 50;

	/** Static storage keyed by WindowScope (weak references for automatic cleanup). */
	private static final Map<WindowScope, TaskProgressTracker> TRACKERS = new WeakHashMap<>();

	private final Object _lock = new Object();

	private final LinkedList<ChatTask> _tasks = new LinkedList<>();

	private final ChatSessionManager _sessionManager = new ChatSessionManager();

	private final ConcurrentHashMap<String, AtomicBoolean> _backgroundAgentInProgress = new ConcurrentHashMap<>();

	/**
	 * Active AI processing states keyed by task ID. Supports concurrent processing (e.g. when two
	 * users share a session) and eliminates the single-slot overwrite race where a second request
	 * could overwrite the first request's state before its background task starts.
	 */
	private final ConcurrentHashMap<String, AIProcessingState> _processingStates = new ConcurrentHashMap<>();

	/** User display name resolved from TLContext (set where context is available, used as fallback elsewhere). */
	private volatile String _userName = null;

	/** User's first name for the welcome screen greeting. */
	private volatile String _welcomeName = null;

	/** Session-scoped approval mode. Persists across messages/processing states. */
	private volatile ApprovalMode _approvalMode = ApprovalMode.MANUAL;

	/**
	 * Returns the chat for this window. Same instance for add and render so task traces persist.
	 */
	public Chat getChat() {
		return _sessionManager.getActiveChat();
	}

	/**
	 * Registers a processing state for the given task ID.
	 *
	 * @param taskId
	 *        The task ID (must not be null).
	 * @param state
	 *        The processing state to register.
	 */
	public void addProcessingState(String taskId, AIProcessingState state) {
		_processingStates.put(taskId, state);
	}

	/**
	 * Removes the processing state for the given task ID (called when processing finishes).
	 *
	 * @param taskId
	 *        The task ID to remove.
	 */
	public void removeProcessingState(String taskId) {
		if (taskId != null) {
			_processingStates.remove(taskId);
		}
	}

	/**
	 * Returns the processing state for the given task ID, or null if not found.
	 *
	 * @param taskId
	 *        The task ID.
	 * @return The processing state, or null.
	 */
	public AIProcessingState getProcessingState(String taskId) {
		return _processingStates.get(taskId);
	}

	/**
	 * Returns any active processing state, or null if none. Used by SSE handlers, polling
	 * endpoints, and UI rendering that need to find "the" current state without knowing the task
	 * ID.
	 *
	 * @return An active processing state, or any remaining state, or null.
	 */
	public AIProcessingState getAnyActiveProcessingState() {
		for (AIProcessingState state : _processingStates.values()) {
			if (state.isProcessing()) {
				return state;
			}
		}
		// Fall back to any remaining state (completed but not yet cleaned up)
		var it = _processingStates.values().iterator();
		return it.hasNext() ? it.next() : null;
	}

	/**
	 * Returns whether any AI task is currently in progress for this tracker.
	 */
	public boolean hasActiveProcessingState() {
		for (AIProcessingState state : _processingStates.values()) {
			if (state.isProcessing()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the user display name stored on this tracker, or null if not yet set.
	 *
	 * <p>
	 * Set from contexts where {@code TLContext} is available (e.g. during component creation). Used
	 * as fallback in contexts where {@code TLContext} is not available (e.g. {@code ChatServlet}).
	 * </p>
	 */
	public String getUserName() {
		return _userName;
	}

	/**
	 * Stores the user display name on this tracker for cross-context resolution.
	 *
	 * @param userName
	 *        The display name of the current user.
	 */
	public void setUserName(String userName) {
		_userName = userName;
	}

	/**
	 * Returns the welcome name (first name) for the welcome screen greeting, or null if not set.
	 */
	public String getWelcomeName() {
		return _welcomeName;
	}

	/**
	 * Stores the welcome name (first name) for the welcome screen greeting.
	 *
	 * @param welcomeName
	 *        The first name to display in the welcome greeting.
	 */
	public void setWelcomeName(String welcomeName) {
		_welcomeName = welcomeName;
	}

	/** Returns the session-scoped approval mode. */
	public ApprovalMode getApprovalMode() {
		return _approvalMode;
	}

	/** Sets the session-scoped approval mode. Also updates all active processing stores. */
	public void setApprovalMode(ApprovalMode mode) {
		_approvalMode = mode;
		for (AIProcessingState state : _processingStates.values()) {
			var store = state.getUserInteractionStore();
			if (store != null) {
				store.setMode(mode);
			}
		}
	}

	/**
	 * Attempts to start a background agent. Returns true if the agent was not already running.
	 * Uses compare-and-set for thread-safe debouncing per agent.
	 */
	public boolean tryStartBackgroundAgent(String agentName) {
		return _backgroundAgentInProgress
			.computeIfAbsent(agentName, k -> new AtomicBoolean(false))
			.compareAndSet(false, true);
	}

	/**
	 * Marks a background agent as finished, allowing it to be triggered again.
	 */
	public void finishBackgroundAgent(String agentName) {
		AtomicBoolean flag = _backgroundAgentInProgress.get(agentName);
		if (flag != null) {
			flag.set(false);
		}
	}

	/**
	 * Returns the active session ID for persistence, or null if no session is selected.
	 */
	public String getActiveSessionId() {
		return _sessionManager.getActiveSessionId();
	}

	/**
	 * Sets the active session ID for persistence. Triggers session switching via
	 * {@link ChatSessionManager}.
	 *
	 * @param sessionId
	 *        The session ID; null to clear.
	 */
	public void setActiveSessionId(String sessionId) {
		_sessionManager.switchSession(sessionId);
	}

	/**
	 * Returns the session manager for this tracker.
	 */
	public ChatSessionManager getSessionManager() {
		return _sessionManager;
	}

	/**
	 * Returns the conversation memory for this session, creating it on first access. The memory
	 * keeps the last 20 messages (user + assistant turns) so the orchestrator can follow multi-turn
	 * conversations.
	 */
	public ChatMemory getChatMemory() {
		return _sessionManager.getActiveChatMemory();
	}

	/**
	 * Returns the session-scoped sub-agent memory map. Sub-agents retain conversation context
	 * across multiple delegations within the same session via this map.
	 */
	public Map<String, ChatMemory> getSubAgentMemories() {
		return _sessionManager.getActiveSubAgentMemories();
	}

	/**
	 * Gets or creates a {@link TaskProgressTracker} from the given {@link WindowScope}.
	 *
	 * @param windowScope
	 *        The window scope to get/store the tracker.
	 * @return The tracker instance, never null.
	 */
	public static TaskProgressTracker getInstance(WindowScope windowScope) {
		synchronized (TRACKERS) {
			TaskProgressTracker tracker = TRACKERS.get(windowScope);
			if (tracker == null) {
				tracker = new TaskProgressTracker();
				TRACKERS.put(windowScope, tracker);
			}
			return tracker;
		}
	}

	/**
	 * Starts a new task with the given description.
	 *
	 * @param description
	 *        The task description.
	 * @return The unique task ID.
	 */
	public String startTask(String description) {
		ChatTask task = new ChatTask(description);
		ChatTask startedTask = task.toBuilder()
			.withStatus(ChatTask.TaskStatus.IN_PROGRESS)
			.build();

		synchronized (_lock) {
			_tasks.addFirst(startedTask);
			enforceMaxSize();
		}

		return startedTask.getId();
	}

	/**
	 * Updates an existing task using the provided updater function.
	 *
	 * @param taskId
	 *        The task ID to update.
	 * @param updater
	 *        Consumer that modifies the task builder.
	 */
	public void updateTask(String taskId, Consumer<ChatTask.Builder> updater) {
		synchronized (_lock) {
			for (int i = 0; i < _tasks.size(); i++) {
				ChatTask task = _tasks.get(i);
				if (task.getId().equals(taskId)) {
					ChatTask.Builder builder = task.toBuilder();
					updater.accept(builder);
					_tasks.set(i, builder.build());
					break;
				}
			}
		}
	}

	/**
	 * Adds a tool call to the specified task.
	 *
	 * @param taskId
	 *        The task ID to add the tool call to.
	 * @param toolCall
	 *        The tool call to add.
	 */
	public void addToolCall(String taskId, ChatToolCall toolCall) {
		updateTask(taskId, builder -> builder.addToolCall(toolCall));
	}

	/**
	 * Adds a timeline event (tool call or agent call) to the specified task.
	 *
	 * @param taskId
	 *        The task ID to add the event to.
	 * @param event
	 *        The timeline event to add.
	 */
	public void addTimelineEvent(String taskId, ChatTimelineEvent event) {
		updateTask(taskId, builder -> builder.addTimelineEvent(event));
	}

	/**
	 * Marks a task as completed successfully.
	 *
	 * @param taskId
	 *        The task ID to complete.
	 */
	public void completeTask(String taskId) {
		updateTask(taskId, builder -> {
			builder.withStatus(ChatTask.TaskStatus.COMPLETED);
			builder.withEndTime(Instant.now());
		});
	}

	/**
	 * Marks a task as failed with an error message.
	 *
	 * @param taskId
	 *        The task ID to fail.
	 * @param error
	 *        The error message.
	 */
	public void failTask(String taskId, String error) {
		updateTask(taskId, builder -> {
			builder.withStatus(ChatTask.TaskStatus.ERROR);
			builder.withError(error);
			builder.withEndTime(Instant.now());
		});
	}

	/**
	 * Returns a task by ID, or null if not found.
	 *
	 * @param taskId
	 *        The task ID.
	 * @return The task, or null.
	 */
	public ChatTask getTask(String taskId) {
		synchronized (_lock) {
			for (ChatTask task : _tasks) {
				if (task.getId().equals(taskId)) {
					return task;
				}
			}
			return null;
		}
	}

	/**
	 * Returns a copy of all tasks for rendering.
	 *
	 * @return Unmodifiable list of all tasks (most recent first).
	 */
	public List<ChatTask> getAllTasks() {
		synchronized (_lock) {
			return new ArrayList<>(_tasks);
		}
	}

	/**
	 * Enforces the maximum task limit by removing oldest tasks.
	 */
	private void enforceMaxSize() {
		while (_tasks.size() > MAX_TASKS) {
			_tasks.removeLast();
		}
	}
}
