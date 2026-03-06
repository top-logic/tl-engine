/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe in-memory store for agent tasks within a single AI processing session.
 *
 * <p>
 * Maintains tasks with auto-incrementing string IDs and a version counter for SSE delta detection.
 * All public methods are synchronized to ensure thread safety. Tasks are stored in creation order
 * using a {@link LinkedHashMap}.
 * </p>
 */
public class TaskStore {

	/** Status value for a newly created task. */
	public static final String STATUS_PENDING = "pending";

	/** Status value for a task currently being worked on. */
	public static final String STATUS_IN_PROGRESS = "in_progress";

	/** Status value for a completed task. */
	public static final String STATUS_COMPLETED = "completed";

	/** Status value that causes a task to be removed from the store. */
	public static final String STATUS_DELETED = "deleted";

	private final Map<String, AgentTask> _tasks = new LinkedHashMap<>();

	private final AtomicInteger _version = new AtomicInteger(0);

	private int _nextId = 1;

	/**
	 * Creates a new task with the given properties and status {@value #STATUS_PENDING}.
	 *
	 * @param subject
	 *        Brief title for the task.
	 * @param description
	 *        Detailed description of what needs to be done.
	 * @param activeForm
	 *        Present continuous form shown while the task is in progress.
	 * @return The newly created task.
	 */
	public synchronized AgentTask createTask(String subject, String description, String activeForm) {
		String id = String.valueOf(_nextId++);
		AgentTask task = new AgentTask(id);
		task.setSubject(subject);
		task.setDescription(description);
		task.setActiveForm(activeForm);
		task.setStatus(STATUS_PENDING);
		_tasks.put(id, task);
		_version.incrementAndGet();
		return task;
	}

	/**
	 * Returns the task with the given ID.
	 *
	 * @param taskId
	 *        The task ID.
	 * @return The task, or {@code null} if no task with that ID exists.
	 */
	public synchronized AgentTask getTask(String taskId) {
		return _tasks.get(taskId);
	}

	/**
	 * Returns a copy of all tasks in creation order.
	 *
	 * @return An unmodifiable list of all tasks.
	 */
	public synchronized List<AgentTask> getAllTasks() {
		return Collections.unmodifiableList(new ArrayList<>(_tasks.values()));
	}

	/**
	 * Updates the task with the given ID. Only non-null parameters are applied. If the status is
	 * {@value #STATUS_DELETED}, the task is removed from the store.
	 *
	 * @param taskId
	 *        The ID of the task to update.
	 * @param status
	 *        New status, or {@code null} to keep existing.
	 * @param subject
	 *        New subject, or {@code null} to keep existing.
	 * @param description
	 *        New description, or {@code null} to keep existing.
	 * @param activeForm
	 *        New active form, or {@code null} to keep existing.
	 * @param owner
	 *        New owner, or {@code null} to keep existing.
	 * @param addBlocks
	 *        Task IDs to add to the blocks set, or {@code null} to skip.
	 * @param addBlockedBy
	 *        Task IDs to add to the {@code blockedBy} set, or {@code null} to skip.
	 * @return The updated task, or {@code null} if no task with that ID exists.
	 */
	public synchronized AgentTask updateTask(String taskId, String status, String subject,
			String description, String activeForm, String owner,
			Collection<String> addBlocks, Collection<String> addBlockedBy) {
		AgentTask task = _tasks.get(taskId);
		if (task == null) {
			return null;
		}

		if (subject != null) {
			task.setSubject(subject);
		}
		if (description != null) {
			task.setDescription(description);
		}
		if (activeForm != null) {
			task.setActiveForm(activeForm);
		}
		if (owner != null) {
			task.setOwner(owner);
		}
		if (addBlocks != null) {
			task._blocks.addAll(addBlocks);
		}
		if (addBlockedBy != null) {
			task._blockedBy.addAll(addBlockedBy);
		}
		if (status != null) {
			if (STATUS_DELETED.equals(status)) {
				_tasks.remove(taskId);
			} else {
				task.setStatus(status);
			}
		}

		_version.incrementAndGet();
		return task;
	}

	/**
	 * Returns the current version counter. The version is incremented on every create or update
	 * operation, allowing clients to detect changes via SSE polling.
	 *
	 * @return The current version number.
	 */
	public synchronized int getVersion() {
		return _version.get();
	}

	/**
	 * A mutable agent task with an ID, subject, description, status, owner, and dependency sets.
	 */
	public static class AgentTask {

		private final String _id;

		private String _subject;

		private String _description;

		private String _activeForm;

		private String _status;

		private String _owner;

		final LinkedHashSet<String> _blocks = new LinkedHashSet<>();

		final LinkedHashSet<String> _blockedBy = new LinkedHashSet<>();

		/**
		 * Creates a new task with the given ID.
		 *
		 * @param id
		 *        The unique task identifier.
		 */
		AgentTask(String id) {
			_id = id;
		}

		/**
		 * The unique task identifier.
		 */
		public String getId() {
			return _id;
		}

		/**
		 * A brief title for the task.
		 */
		public String getSubject() {
			return _subject;
		}

		/**
		 * @see #getSubject()
		 */
		public void setSubject(String subject) {
			_subject = subject;
		}

		/**
		 * A detailed description of what needs to be done.
		 */
		public String getDescription() {
			return _description;
		}

		/**
		 * @see #getDescription()
		 */
		public void setDescription(String description) {
			_description = description;
		}

		/**
		 * Present continuous form shown in the spinner while the task is in progress (e.g.
		 * "Running tests").
		 */
		public String getActiveForm() {
			return _activeForm;
		}

		/**
		 * @see #getActiveForm()
		 */
		public void setActiveForm(String activeForm) {
			_activeForm = activeForm;
		}

		/**
		 * The current status of the task (e.g. "pending", "in_progress", "completed").
		 */
		public String getStatus() {
			return _status;
		}

		/**
		 * @see #getStatus()
		 */
		public void setStatus(String status) {
			_status = status;
		}

		/**
		 * The agent that owns this task, or {@code null} if unassigned.
		 */
		public String getOwner() {
			return _owner;
		}

		/**
		 * @see #getOwner()
		 */
		public void setOwner(String owner) {
			_owner = owner;
		}

		/**
		 * Task IDs that are blocked by this task (cannot start until this task completes).
		 *
		 * @return An unmodifiable view of the blocks set.
		 */
		public Set<String> getBlocks() {
			return Collections.unmodifiableSet(_blocks);
		}

		/**
		 * Task IDs that must complete before this task can start.
		 *
		 * @return An unmodifiable view of the {@code blockedBy} set.
		 */
		public Set<String> getBlockedBy() {
			return Collections.unmodifiableSet(_blockedBy);
		}
	}
}
