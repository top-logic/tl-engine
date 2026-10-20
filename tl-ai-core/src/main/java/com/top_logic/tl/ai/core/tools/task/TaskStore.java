/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.task;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.tl.ai.core.tools.interaction.messages.PersistedTask;
import com.top_logic.tl.ai.core.tools.interaction.messages.PersistedTaskStore;
import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonReader;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * Thread-safe in-memory store for agent tasks within a single AI processing session.
 *
 * <p>
 * Maintains tasks with auto-incrementing string IDs. All public methods are synchronized to ensure
 * thread safety. Tasks are stored in creation order using a {@link LinkedHashMap}.
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

	private int _highWaterMark = 0;

	private int _nextId = 1;

	private AgentEventListener _listener;

	private Mirror _mirror;

	/**
	 * Receives the store's whole task list after every mutation - the seam a durable copy of the
	 * plan follows.
	 *
	 * <p>
	 * The store stays the truth of the running turn; a mirror only writes what the store says
	 * somewhere else (the {@code tl.ai.execution:AgentTask} rows a view can bind to). It is called
	 * while the store is locked, so it must read the list it is given and return, and it must not
	 * call back into the store.
	 * </p>
	 */
	public interface Mirror {

		/**
		 * Called after every task mutation.
		 *
		 * @param tasks
		 *        The store's tasks in plan order. The elements are the store's own mutable tasks:
		 *        read them now, do not keep them.
		 */
		void tasksChanged(List<AgentTask> tasks);
	}

	/**
	 * Sets the event listener that receives
	 * {@link com.top_logic.tl.ai.core.agent.event.AgentEvent.TaskUpdated} events on every task
	 * mutation. Pass {@code null} to clear.
	 */
	public synchronized void setListener(AgentEventListener listener) {
		_listener = listener;
	}

	/**
	 * Sets the {@link Mirror} that follows every task mutation. Pass {@code null} to clear.
	 */
	public synchronized void setMirror(Mirror mirror) {
		_mirror = mirror;
	}

	private void fireTaskUpdated() {
		AgentEventListener listener = _listener;
		if (listener != null) {
			try {
				listener.onEvent(new AgentEvent.TaskUpdated("", 0, Instant.now(), toJson()));
			} catch (Exception ex) {
				Logger.error("Failed to fire TaskUpdated event.", ex, TaskStore.class);
			}
		}
		Mirror mirror = _mirror;
		if (mirror != null) {
			try {
				// After the browser push: the mirror writes to the database, and the step the user
				// is watching should not wait for that.
				mirror.tasksChanged(new ArrayList<>(_tasks.values()));
			} catch (Exception ex) {
				Logger.error("Failed to mirror the task store.", ex, TaskStore.class);
			}
		}
	}

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
		_highWaterMark = Math.max(_highWaterMark, Integer.parseInt(id));
		AgentTask task = new AgentTask(id);
		task.setSubject(subject);
		task.setDescription(description);
		task.setActiveForm(activeForm);
		task.setStatus(STATUS_PENDING);
		_tasks.put(id, task);
		fireTaskUpdated();
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
				_highWaterMark = Math.max(_highWaterMark, Integer.parseInt(taskId));
				_tasks.remove(taskId);
			} else {
				task.setStatus(status);
			}
		}

		fireTaskUpdated();
		return task;
	}

	/**
	 * Returns the number of tasks that are not yet completed (status is {@value #STATUS_PENDING}
	 * or {@value #STATUS_IN_PROGRESS}).
	 */
	public synchronized int countUncompleted() {
		int count = 0;
		for (AgentTask task : _tasks.values()) {
			String status = task.getStatus();
			if (STATUS_PENDING.equals(status) || STATUS_IN_PROGRESS.equals(status)) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Serializes this store to a JSON string using msgbuf's {@link PersistedTaskStore} format.
	 *
	 * @return The JSON representation of this store.
	 */
	public synchronized String toJson() {
		PersistedTaskStore persisted = PersistedTaskStore.create()
			.setHighWaterMark(_highWaterMark);

		for (AgentTask task : _tasks.values()) {
			PersistedTask pt = PersistedTask.create()
				.setId(task.getId())
				.setSubject(task.getSubject() != null ? task.getSubject() : "")
				.setDescription(task.getDescription() != null ? task.getDescription() : "")
				.setActiveForm(task.getActiveForm() != null ? task.getActiveForm() : "")
				.setStatus(task.getStatus() != null ? task.getStatus() : "")
				.setOwner(task.getOwner() != null ? task.getOwner() : "");

			for (String blockId : task._blocks) {
				pt.addBlock(blockId);
			}
			for (String blockedById : task._blockedBy) {
				pt.addBlockedBy(blockedById);
			}
			persisted.addTask(pt);
		}

		StringW sw = new StringW();
		try (JsonWriter jw = new JsonWriter(sw)) {
			persisted.writeTo(jw);
		} catch (IOException ex) {
			Logger.error("Failed to serialize TaskStore to JSON.", ex, TaskStore.class);
			return "{}";
		}
		return sw.toString();
	}

	/**
	 * Deserializes a {@link TaskStore} from a JSON string previously produced by {@link #toJson()}.
	 *
	 * @param json
	 *        The JSON string, or {@code null}/{@code ""} for an empty store.
	 * @return The restored store, never {@code null}.
	 */
	public static TaskStore fromJson(String json) {
		TaskStore store = new TaskStore();
		if (json == null || json.isEmpty()) {
			return store;
		}

		try {
			PersistedTaskStore persisted =
				PersistedTaskStore.readPersistedTaskStore(new JsonReader(new StringR(json)));

			store._highWaterMark = persisted.getHighWaterMark();
			store._nextId = persisted.getHighWaterMark() + 1;

			for (PersistedTask pt : persisted.getTasks()) {
				AgentTask task = new AgentTask(pt.getId());
				task.setSubject(pt.getSubject());
				task.setDescription(pt.getDescription());
				task.setActiveForm(pt.getActiveForm());
				task.setStatus(pt.getStatus());
				task.setOwner(pt.getOwner().isEmpty() ? null : pt.getOwner());

				task._blocks.addAll(pt.getBlocks());
				task._blockedBy.addAll(pt.getBlockedBy());

				store._tasks.put(pt.getId(), task);
			}
		} catch (IOException ex) {
			Logger.error("Failed to deserialize TaskStore from JSON.", ex, TaskStore.class);
		}
		return store;
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
