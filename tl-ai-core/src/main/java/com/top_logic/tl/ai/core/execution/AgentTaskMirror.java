/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.execution;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.TaskState;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.execution.model.AgentTask;
import com.top_logic.tl.ai.core.tools.task.TaskStore;

/**
 * Keeps a session's {@link AgentTask} rows saying what the run's {@link TaskStore} says.
 *
 * <p>
 * The store is the truth of the running turn - the agent's {@code createTask} / {@code updateTask}
 * tools write it, and its serialized form is what the chat streams to the browser. Its plan,
 * however, lives only in memory and in the session's metadata blob, which is written when the run
 * ends. This mirror makes the same plan exist as model objects while the run is going on, so a view
 * can bind to it, a reload finds it, and any other part of the application can query it.
 * </p>
 *
 * <p>
 * Mirroring is best-effort and never disturbs the run: a persistence failure is logged and the
 * store keeps going. The mirror hands the store's state to
 * {@link AgentExecutionStore#syncTasks(AgentSession, List, com.top_logic.tl.ai.core.execution.model.AgentExecution)}
 * as plain data, so all reading and writing of persistent objects happens on the store's writer
 * thread - the agent thread, whose view of the database is pinned to an older revision, never
 * dereferences a row.
 * </p>
 *
 * @see com.top_logic.tl.ai.core.tools.task.TaskStore.Mirror
 */
public final class AgentTaskMirror implements TaskStore.Mirror, AutoCloseable {

	private final TaskStore _store;

	private final AgentSession _session;

	private final ExecutionHandle _handle;

	/**
	 * What the last successful sync wrote, so an unchanged store costs no database round trip.
	 * <code>null</code> until the first sync.
	 */
	private volatile List<TaskState> _written;

	private AgentTaskMirror(TaskStore store, AgentSession session, ExecutionHandle handle) {
		_store = store;
		_session = session;
		_handle = handle;
	}

	/**
	 * Mirrors {@code store} into the tasks of {@code session} until the returned handle is closed.
	 *
	 * <p>
	 * A run without a recorded session, or one started while the {@link AgentExecutionStore} is
	 * inactive, is not mirrored; the returned handle is then an inert one that installs nothing.
	 * </p>
	 *
	 * @param store
	 *        The run's task store; must not be <code>null</code>.
	 * @param session
	 *        The session whose plan the store is; <code>null</code> disables mirroring.
	 * @param handle
	 *        The run's execution holder, used to attribute changed rows; may be <code>null</code>.
	 */
	public static AgentTaskMirror attach(TaskStore store, AgentSession session, ExecutionHandle handle) {
		if (session == null || !AgentExecutionStore.Module.INSTANCE.isActive()) {
			return new AgentTaskMirror(null, null, null);
		}
		AgentTaskMirror mirror = new AgentTaskMirror(store, session, handle);
		store.setMirror(mirror);
		return mirror;
	}

	@Override
	public void tasksChanged(List<TaskStore.AgentTask> tasks) {
		List<TaskState> wanted = statesOf(tasks);
		if (wanted.equals(_written)) {
			// The mutation did not touch anything the model carries (e.g. only the active form
			// changed): nothing to write, and nothing to wake a view for.
			return;
		}
		try {
			AgentExecutionStore.getInstance().syncTasks(_session, wanted, _handle == null ? null : _handle.get());
			_written = wanted;
		} catch (RuntimeException ex) {
			Logger.warn("Failed to mirror the agent's tasks into the model: " + ex.getMessage(), ex,
				AgentTaskMirror.class);
		}
	}

	/**
	 * The store's tasks as plain data, numbered from 1 in plan order.
	 *
	 * <p>
	 * The position is computed here rather than taken from the task id, so deleting a task
	 * renumbers the ones behind it and the plan reads 1..n at all times.
	 * </p>
	 */
	private static List<TaskState> statesOf(List<TaskStore.AgentTask> tasks) {
		List<TaskState> states = new ArrayList<>(tasks.size());
		int ordinal = 1;
		for (TaskStore.AgentTask task : tasks) {
			states.add(new TaskState(task.getId(), task.getSubject(), task.getDescription(),
				task.getStatus(), ordinal++));
		}
		return states;
	}

	@Override
	public void close() {
		if (_store != null) {
			_store.setMirror(null);
		}
	}
}
