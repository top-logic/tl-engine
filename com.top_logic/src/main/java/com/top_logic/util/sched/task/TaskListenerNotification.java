/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task;

import com.top_logic.basic.listener.Listener;
import com.top_logic.util.sched.task.result.TaskResult;

/**
 * Groups the information a {@link Listener} for {@link Task} {@link TaskState state} changes will
 * receive.
 * <p>
 * <b>Immutable</b>
 * </p>
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class TaskListenerNotification {

	private final Task _task;

	private final TaskState _newState;

	private final TaskState _oldState;

	private final TaskResult _newResult;

	/**
	 * Creates a {@link TaskListenerNotification}.
	 * 
	 * @param task
	 *        Must not be <code>null</code>.
	 * @param newState
	 *        Must not be <code>null</code>.
	 * @param oldState
	 *        Must not be <code>null</code>.
	 * @param newResult
	 *        Is allowed to be <code>null</code>.
	 */
	public TaskListenerNotification(Task task, TaskState newState, TaskState oldState, TaskResult newResult) {
		if (task == null) {
			throw new NullPointerException("Task must not be null.");
		}
		if (newState == null) {
			throw new NullPointerException("newState must not be null.");
		}
		if (oldState == null) {
			throw new NullPointerException("oldState must not be null.");
		}
		_task = task;
		_newState = newState;
		_oldState = oldState;
		_newResult = newResult;
	}

	/**
	 * The {@link Task} whose {@link TaskState} changed.
	 * 
	 * @return Never <code>null</code>
	 */
	public Task getTask() {
		return _task;
	}

	/**
	 * The new {@link TaskState}.
	 * 
	 * @return Never <code>null</code>
	 */
	public TaskState getNewState() {
		return _newState;
	}

	/**
	 * The old {@link TaskState}.
	 * 
	 * @return Never <code>null</code>
	 */
	public TaskState getOldState() {
		return _oldState;
	}

	/**
	 * The new {@link TaskResult}.
	 * 
	 * @return Is <code>null</code> when the {@link Task} is executed for the first time.
	 */
	public TaskResult getNewResult() {
		return _newResult;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + _task.getName() + ": " + _oldState + " -> " + _newState
			+ ", new result: " + _newResult + ")";
	}

}
