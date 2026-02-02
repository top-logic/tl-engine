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
import java.util.function.Consumer;

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

	private final Chat _chat = new Chat();

	/**
	 * Returns the chat for this window. Same instance for add and render so task traces persist.
	 */
	public Chat getChat() {
		return _chat;
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
